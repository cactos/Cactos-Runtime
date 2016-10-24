package eu.cactosfp7.vmi.controller.openstack.worker;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.cdo.util.CommitException;
import org.eclipse.emf.cdo.util.CommitConflictException;
import org.eclipse.emf.cdo.view.CDOView;
import org.eclipse.emf.common.util.EMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.cactosfp7.cdosession.CactosCdoSession;
import eu.cactosfp7.cdosession.settings.CactosUser;
import eu.cactosfp7.cdosession.util.CdoHelper;
import eu.cactosfp7.cdosessionclient.CdoSessionClient;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.ComputeNode;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.PhysicalDCModel;
import eu.cactosfp7.ossession.service.OsSession;
import eu.cactosfp7.ossessionclient.OsSessionClient;
import eu.cactosfp7.ossessionclient.VmiOpenstackConstants;
import eu.cactosfp7.runtimemanagement.util.HttpForwarder;

public final class ExecuteInitialPlacement implements VMIWorker{

	private static final Logger logger = Logger.getLogger(ExecuteInitialPlacement.class.getName());
	
	private final String vmUuid;
	private final String computeNodeUuid;
	private final CactosCdoSession cactosCdoSession;
	private final OsSession openstackSession;
	
	public ExecuteInitialPlacement(String _vmUuid, String _computeNodeUuid){
		vmUuid = _vmUuid;
		computeNodeUuid = _computeNodeUuid;
		cactosCdoSession = CdoSessionClient.INSTANCE
				.getService().getCactosCdoSession(CactosUser.CACTOSCALE);
		openstackSession = OsSessionClient.INSTANCE.getService().getCactosOsSession();
	}
	
	static class Temp {
		String vmOsId = null;
		String tenantId = null;
		String pmName = null;
		String origJsonBody = null;
		Map<String,String> requestHeaders = null;
		String osEndpointAsProxy = null;
		
		public static Temp create(CactosCdoSession cactosCdoSession, String _vmUuid, String _computeNodeUuid) {
			CDOView cdoCon = cactosCdoSession.createView();
			// lookup variables
				
			try {
				Temp data = new Temp();
				VirtualMachine vm = getVm(cdoCon, _vmUuid, cactosCdoSession);
				data.vmOsId = vm.getName();
				logger.log(Level.INFO, "vm has name: '" + data.vmOsId + "'");
				ComputeNode pm = getPn(cdoCon, _computeNodeUuid, cactosCdoSession); // might return null!
				data.tenantId = vm.getInputParameters().get(VmiOpenstackConstants.TENANTID);
				data.pmName = (pm == null) ? 
						"FAILBOOT"	// if pm is null, place on not existing compute node to get an error state in openstack 
						: pm.getName();
				logger.log(Level.INFO, "found computenode '" + pm + "' for vm with name '" + data.pmName + "' for tenant '" + data.tenantId + "'");
				data.origJsonBody = vm.getInputParameters().get(VmiOpenstackConstants.REQUEST_JSON);
				data.requestHeaders = getRequestHeaders(vm.getInputParameters());
				return data;
			} catch(Exception ex) {
				logger.log(Level.WARNING, "exception when creating data container", ex);
				//return null;
			} finally {
				cdoCon.close();
			}
			return null;
		}

		private String fillJson() {
			// Parse jsonBody for modifications
			Gson gson = new GsonBuilder().disableHtmlEscaping().create();
			JsonElement element = new JsonParser().parse(origJsonBody);
			JsonObject  jobjectServer = element.getAsJsonObject().getAsJsonObject("server");
			addAvailZone(jobjectServer, pmName);
			addUserData(jobjectServer, vmOsId);
			return gson.toJson(element);
		}
		
		private void addAvailZone(JsonObject jobjectServer, String pmName) {
			// Update availability zone in json of request body
			String availabilityzone = "";
			if(jobjectServer.has("availability_zone"))
				availabilityzone = jobjectServer.get("availability_zone").getAsString();
			jobjectServer.addProperty("availability_zone", availabilityzone + ":" + pmName);
		}
		
		private void addUserData(JsonObject jobjectServer, String vmOsId) {
			// Update user_data to inject Chukwa
		
			String userDataEnc = "";
			String userDataDec = "";
			if(jobjectServer.has("user_data")){
				userDataEnc = jobjectServer.get("user_data").getAsString();
				byte[] valueDecoded = DatatypeConverter.parseBase64Binary(userDataEnc); // undecode base64
				userDataDec = new String(valueDecoded); // userData now undecoded
				userDataDec += "\n\n";
			}
			userDataDec += buildChukwaInstaller(vmOsId);
			logger.info("going to encode: " + userDataDec);
			userDataEnc = DatatypeConverter.printBase64Binary(userDataDec.getBytes());
			jobjectServer.addProperty("user_data", userDataEnc);
			logger.info("encoded: " + userDataEnc);
		}
		
		private String buildChukwaInstaller(String vmOsId) {
			// TODO
			return "echo \"Install Chukwa Agent for VM\"";
		}

		public Map<String,Object> invoke(OsSession openstackSession, String jsonBody) {
			String osEndpointAsProxy = openstackSession.getEndpointAsProxy();
			String targetUrl = osEndpointAsProxy + "" + tenantId + "/servers";
			logger.info("Building target URL: " + targetUrl );
			
			// Update http request headers
			requestHeaders.put("Content-Length", String.valueOf(jsonBody.length()));
			logger.info("Forward Boot Request Headers: " + requestHeaders + " Body: " + jsonBody);
			// Fire request to openstack API !
			Map<String,Object> result = HttpForwarder.forward("POST", targetUrl, requestHeaders, jsonBody.getBytes());
			logger.info("HttpForwarder return '"+ result + "'");
			return result;
		}
	}
	
	private boolean storeResult(String result, boolean parseName) {
		final int MAX_TRIES = 10;
		for(int i = 0; i < MAX_TRIES; i++) {
			Boolean b = doStoreResult(result, parseName);
			if(b == null) return false;
			if(b == Boolean.TRUE) return true;
			try{ Thread.sleep(2000);
			} catch(InterruptedException ie) {
				// ignore for now ///
			}
			// session refresh???
		}
		return false;
	}

	/**
	 * @return TRUE if everything was alright, FALSE, if failed, but retrying
	 * might solve the problem, null if everything is broken
	 */
	private Boolean doStoreResult(String result, boolean parseName) {
		// store result in vm model
		Boolean retVal = null;
		CDOTransaction cdoTx = cactosCdoSession.createTransaction();
		try {
			VirtualMachine vmTx = getVm(cdoTx, vmUuid, cactosCdoSession);
			ComputeNode pmTx = getPn(cdoTx, computeNodeUuid, cactosCdoSession);
			vmTx.getInstantiationProperties().put("output", result);
			String name = parseName ? parseUuidFromResult(result) : result;
			logger.log(Level.INFO, "setting vm name to '" + name + "'");
			vmTx.setName(name);
			// update vm model (hypervisor and id)
			if(pmTx != null) {
				// vmTx.setHypervisor(pmTx.getHypervisor());
				String nodename = (vmTx.getHypervisor() == null ? "no hypervisor" : 
						(vmTx.getHypervisor().getNode() == null ? "no node" : vmTx.getHypervisor().getNode().getName()));
				logger.log(Level.INFO, "silently assuming that hypervisors match: " + computeNodeUuid + " vs " + nodename );
				retVal = Boolean.TRUE;
			} else {
				logger.log(Level.INFO, "setting vm hypervisor to null");
				vmTx.setHypervisor(null);
				retVal = null;
			}
			try {
				cactosCdoSession.commitAndCloseConnection(cdoTx);
			} catch (CommitConflictException e) {
				retVal = Boolean.FALSE;
				logger.warning("views: " + cdoTx.getSession().getViews());
				logger.warning("transactions: " + cdoTx.getSession().getTransactions());
				logger.warning("Could not store output in vm. " + e);
				logger.warning("rolling back transaction");
				cdoTx.rollback();
			} catch (CommitException e) {
				logger.warning("views: " + cdoTx.getSession().getViews());
				logger.warning("transactions: " + cdoTx.getSession().getTransactions());
				retVal = Boolean.FALSE;
				// 	throw new RuntimeException(e);
				logger.warning("Could not store output in vm. " + e);
				cdoTx.rollback();
			}
			return retVal;
		} finally {
			// just to make sure
			cactosCdoSession.closeConnection(cdoTx);
		}
	}
	
	@Override
	public boolean work() {
		
		Temp data = Temp.create(cactosCdoSession, vmUuid, computeNodeUuid);
		if(data == null) {
			logger.log(Level.SEVERE, "could not read initial data, returning null");
			return false;
		}
		
		// get new jsonBody
		String jsonBody = data.fillJson();
		logger.info(jsonBody);
		if(jsonBody == null || jsonBody.isEmpty()) {
			return false;
		}
		
		Map<String,Object> result = data.invoke(openstackSession, jsonBody);
		if(result == null) {
			logger.log(Level.SEVERE, "invokation returned null");
			return false;
		} else {
			Integer i = (Integer) result.get(HttpForwarder.RESPONSE_CODE_KEY);
			if(i == null) throw new IllegalStateException();
			if(199 < i.intValue() && i.intValue() < 300 && i.intValue() != 204) {
				return storeResult((String)result.get(HttpForwarder.RESPONSE_OUTPUT_KEY), true);
			} else if(204 == i.intValue()) {
				logger.log(Level.INFO, "received a response with no content (204)");
				return storeResult("", false);
			} else {
				logger.log(Level.SEVERE, "received a return value != 200");
				storeResult((String)result.get(HttpForwarder.RESPONSE_OUTPUT_KEY), false);
				return false;
			}
		}
	}

	private static Map<String, String> getRequestHeaders(EMap<String, String> inputParameters) {
		Map<String, String> map = new HashMap<String,String>();
		for(Map.Entry<String, String> entry : inputParameters.entrySet()){
			String prefixedkey = entry.getKey();
			if(prefixedkey.indexOf(VmiOpenstackConstants.REQUEST_HEADERS) == 0){
				String headerkey = prefixedkey.substring(VmiOpenstackConstants.REQUEST_HEADERS.length());
				map.put(headerkey, entry.getValue());
			}
		}
		return map;
	}

	private static ComputeNode getPn(CDOView cdoCon, String computeNodeUuid, CactosCdoSession cactosCdoSession) {
		if(computeNodeUuid == null){
			logger.log(Level.WARNING, "Asking for computenode without uuid impossible, returning null");
			return null;
		}
		PhysicalDCModel physicalDCModel = 
				(PhysicalDCModel) cactosCdoSession.
				getRepository(cdoCon, cactosCdoSession.getPhysicalModelPath());
		ComputeNode cn = CdoHelper.getComputeNodeById(physicalDCModel, computeNodeUuid);
		if(cn == null)
			throw new RuntimeException("getPn for computeNodeUuid " + computeNodeUuid + " failed!");
		return cn;
	}

	private static VirtualMachine getVm(CDOView cdoCon, String vmUuid, CactosCdoSession cactosCdoSession) {
		LogicalDCModel logicalDCModel = 
				(LogicalDCModel) cactosCdoSession.
				getRepository(cdoCon, cactosCdoSession.getLogicalModelPath());
		VirtualMachine vm = CdoHelper.getVirtualMachineById(logicalDCModel, vmUuid);
		if(vm == null)
			throw new RuntimeException("getVm for vmUuid " + vmUuid + " failed!");
		logger.log(Level.INFO, "found vm: " + vm);
		return vm;
	}	
	
	/**
	 * Methods takes the JSON response from OpenStack after a nova boot request
	 * and parses the virtual machine's OpenStack UUID out of it
	 * @param response
	 * @return virtual machine's OpenStack UUID
	 */
	private String parseUuidFromResult(String response) {
		/* FIXME: this only works if the result is really a 200 response.
		 * JSON to parse looks like
		 * {"server": { ... "id": "1491fa92-de96-4b08-9192-1829e6043686" ... } }
		 */
	    JsonObject  jobject = new JsonParser().parse(response).getAsJsonObject();
	    String result = jobject.getAsJsonObject("server").get("id").getAsString();
		return result;
	}	
}
