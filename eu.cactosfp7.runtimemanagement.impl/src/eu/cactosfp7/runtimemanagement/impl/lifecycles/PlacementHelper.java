package eu.cactosfp7.runtimemanagement.impl.lifecycles;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.cdo.common.id.CDOID;
import org.eclipse.emf.cdo.view.CDOView;

import eu.cactosfp7.cactoopt.placementservice.IPlacementService;
import eu.cactosfp7.cactoopt.placementservice.PlacementResult;
import eu.cactosfp7.cactoopt.placementservice.PlacementResult.Status;
import eu.cactosfp7.cdosession.CactosCdoSession;
import eu.cactosfp7.cdosession.util.CdoHelper;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.ComputeNode;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.PhysicalDCModel;
import eu.cactosfp7.optimisationplan.ExecutionStatus;
import eu.cactosfp7.optimisationplan.OptimisationPlan;
import eu.cactosfp7.optimisationplan.OptimisationPlanRepository;
import eu.cactosfp7.runtimemanagement.impl.PlacementClient;
import eu.cactosfp7.runtimemanagement.service.RuntimeManagementException;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.Hypervisor;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;



final class PlacementHelper {

	private static final Logger logger = Logger.getLogger(PlacementHelper.class.getCanonicalName());

	private static final int MAX_TRIES = 10;
	private static final int SLEEP_SECONDS = 15;
	
	private final String _uuid;
	private final CactosCdoSession _cdoSession;
	private final CDOID _vmId;
	private volatile PlacementResult _placementResult;
	private volatile String errorMessage = null;
	
	private PlacementHelper(String uuid, CactosCdoSession cdoSession, CDOID vmId) {
		_uuid = uuid;
		_cdoSession = cdoSession;
		_vmId = vmId;
	}
	
	private CDOID handleSuccessfulPlacement() {
		final CDOView view = _cdoSession.createView();
		try {
			String nodeId = _placementResult.getUuid();
			PhysicalDCModel physicalDCModel = (PhysicalDCModel) _cdoSession.getRepository(view, _cdoSession.getPhysicalModelPath());
			logger.log(Level.INFO, "PlacementService decided to place VM on host " + nodeId);
			ComputeNode cn = CdoHelper.getComputeNodeById(physicalDCModel, nodeId);
			return cn.cdoID();
		} finally {
			_cdoSession.closeConnection(view);
		}
	}
	
	private CDOID handleSuccessfulPlacement2() {
		final CDOView view = _cdoSession.createView();
		try {
			String nodeId = _placementResult.getUuid();
			PhysicalDCModel physicalDCModel = (PhysicalDCModel) _cdoSession.getRepository(view, _cdoSession.getPhysicalModelPath());
			ComputeNode cn = CdoHelper.getComputeNodeById(physicalDCModel, nodeId);
			if(cn == null) throw new IllegalStateException("computenode '" + nodeId + "' not found.");
			logger.log(Level.INFO, "PlacementService decided to place VM on host " + nodeId);
			
			LogicalDCModel logicalDCModel = (LogicalDCModel) _cdoSession.getRepository(view, _cdoSession.getLogicalModelPath());
			try {
				Hypervisor hv = CdoHelper.getModelByIdentifier(logicalDCModel.getHypervisors(), cn.getName());
				return hv.cdoID();
			} catch(Exception ex) {
				logger.log(Level.INFO, "cannot retrieve hypervisor for host: " + nodeId);
				return null;
			}
		} finally {
			_cdoSession.closeConnection(view);
		}
	}
	
	private void sleepFor(int seconds) {
		logger.log(Level.INFO, "Sleeping for " + seconds + " seconds.");
		final long toSleep = seconds * 1000 - 100;
		long start = System.currentTimeMillis();
		while(true) {
			long end = System.currentTimeMillis();
			long diff = end - start;
			if(diff > toSleep) {
				logger.log(Level.INFO, "Done with sleeping.");
				return;
			}
			try { Thread.sleep(toSleep - diff);
			} catch (InterruptedException e) {
				// ignore this one as the method will be left anyway at some point
			}
		}
	}
	
	private PlacementHelper next() throws RuntimeManagementException {
		logger.log(Level.WARNING, "running next iteration of placement");
		if(errorMessage != null) {
			throw new RuntimeManagementException(errorMessage);
		}
		if(Status.SUCCESSFUL.equals(_placementResult.getStatus())) {
			throw new IllegalStateException("no iteration foreseen when result was successful");
		}
		if(Status.FAILED_CONCURRENT_OPTIMISATION.equals(_placementResult.getStatus())) {
			logger.log(Level.WARNING, "status is " + _placementResult.getStatus() + ": waiting");
			for(int tries = MAX_TRIES; tries > 0; tries--) {
				Boolean b = hasRemainingPlans();
				if(b == null) {
					throw new IllegalStateException("no plan repository was found.");		
				} else if(b == Boolean.TRUE) {
					sleepFor(SLEEP_SECONDS);
				} else {
					return new PlacementHelper(_uuid, _cdoSession, _vmId);		
				}
			}
			logger.log(Level.WARNING, "After " + MAX_TRIES + " iterations PlacementService did not find a solution. giving up.");
			throw new RuntimeManagementException("After " + MAX_TRIES + " iterations PlacementService did not find a solution. giving up.");
		}
		throw new IllegalStateException("case not covered: " + this);
	}

	private CDOID run(IPlacementService service) throws RuntimeManagementException {
		_placementResult = service.determinePlacement(_uuid);
		switch(_placementResult.getStatus()){
			case SUCCESSFUL:
				// return handleSuccessfulPlacement();
				return handleSuccessfulPlacement2();
			case FAILED_IMPOSSIBLE:
				// give up and send try again later to the client
				logger.log(Level.WARNING, "PlacementService cannot suggest a host to place VM (FAILED_IMPOSSIBLE)!");
				errorMessage = "PlacementService cannot suggest a host to place VM (FAILED_IMPOSSIBLE)!";
				return null;
			case FAILED_CONCURRENT_OPTIMISATION:
				// no error message. just retry.
				return null;
			case FAILED_TRANSACTION_EXCEPTION:
				logger.log(Level.WARNING, "PlacementService not found, returned nothing or invalid state.");
				errorMessage = "PlacementService not found, returned nothing or invalid state";
				logFailedTransactionExceptionCase();
				return null;
			default:
				throw new IllegalStateException("cannot do anything. unknown return value.");
		}
	}

	private void logFailedTransactionExceptionCase() {
		logger.log(Level.WARNING, "PlacementService not found: [looking for CDOID:" + _vmId + "]");
		String newId = null;
		try { newId = getVmId(_vmId, _cdoSession); }
		catch(RuntimeManagementException ex){
			logger.log(Level.SEVERE, "cannot retrieve vmid for " + _vmId, ex);
		}
		logger.log(Level.WARNING, "PlacementService not found: [looking for " + _uuid + " now it says: " + newId );
		final CDOView view = _cdoSession.createView();
		try{
			VirtualMachine vm = (VirtualMachine) view.getObject(_vmId);
			logger.log(Level.WARNING, "PlacementService not found: " + vm + ":" + (vm == null ? "null" : vm.getName()));
			logger.log(Level.WARNING, "PlacementService not found: " + (vm == null ? null :  vm.cdoRevision()));
			vm = getVm(view, _uuid, _cdoSession);
			logger.log(Level.WARNING, "found VM through CDO helper: " + vm);
		} finally {
			_cdoSession.closeConnection(view);
		}

	}
		
	private static String getVmId(CDOID vmCdoId, CactosCdoSession cdoSession) throws RuntimeManagementException {
		final CDOView view = cdoSession.createView();
		try {
			// This happens in another transaction that is opened later -> we
			// need to work in a new transaction afterwards. 
			// (FIXME: is this comment still valid?)
			VirtualMachine vm = (VirtualMachine) view.getObject(vmCdoId);
			return vm.getId();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Cannot retrieve virtual machine!", e);
			throw new RuntimeManagementException("Cannot instantiate new virtual machine.", e);
		} finally { 
			cdoSession.closeConnection(view);
		}
	}

	private Boolean hasRemainingPlans() {
		logger.log(Level.INFO, "searching session at path: " + _cdoSession.getOptimisationPlanPath());
		final CDOView view = _cdoSession.createView();
		OptimisationPlanRepository planRepository = (OptimisationPlanRepository) _cdoSession.getRepository(view, _cdoSession.getOptimisationPlanPath());
		try { // if no planRepo found, quit
			if (planRepository == null) {
				logger.severe("OptimisationPlanRepository not found on CDO server.");
				return null;
			} else {// Look for Plans with state IN_EXECUTION	
				for (OptimisationPlan plan : planRepository.getOptimisationPlans()) {
					if (ExecutionStatus.IN_EXECUTION.equals(plan.getExecutionStatus())
							|| ExecutionStatus.READY.equals(plan.getExecutionStatus())) {
						// if any plan is in execution or planed to be executed, quit
						return Boolean.TRUE;
					}
				}
			}
		} finally {
			_cdoSession.closeConnection(view);
		}
		return Boolean.FALSE;
	}
	
	@Override
	public String toString() {
		return "PlacementHelper [_uuid=" + _uuid + ", errorMessage=" + errorMessage + ", _placementResult=" + _placementResult + "]";
	}
	
	static CDOID getInitialPlacement(CactosCdoSession cdoSession, CDOID vmCdoId) throws RuntimeManagementException {
		IPlacementService service = PlacementClient.INSTANCE.getService();
		final String vmId = getVmId(vmCdoId, cdoSession);
		CDOID resValue = null;
		PlacementHelper ph = new PlacementHelper(vmId, cdoSession, vmCdoId);
		int tries;
		for(tries = MAX_TRIES; tries > 0; tries--) {
			resValue = ph.run(service);
			if(resValue != null) break;
			ph = ph.next();
		}
		if(tries == 0){
			logger.log(Level.WARNING, "After " + MAX_TRIES + " getInitialPlacement did not find a solution. giving up.");
			throw new RuntimeManagementException("After " + MAX_TRIES + " getInitialPlacement did not find a solution. giving up.");	
		}
		if(resValue == null) 
			throw new NullPointerException();
		return resValue;
	}

	// WARNING: this is duplicated code from ExecuteInitialPlacement
        static VirtualMachine getVm(CDOView cdoCon, String vmUuid, CactosCdoSession cactosCdoSession) {
                LogicalDCModel logicalDCModel =
                                (LogicalDCModel) cactosCdoSession.
                                getRepository(cdoCon, cactosCdoSession.getLogicalModelPath());
                VirtualMachine vm = CdoHelper.getVirtualMachineById(logicalDCModel, vmUuid);
                logger.log(Level.INFO, "found vm: " + vm);
                return vm;
        }

}
