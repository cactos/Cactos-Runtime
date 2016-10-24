package eu.cactosfp7.runtimemanagement.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropagateToChukwa {

	private static final Logger logger = Logger.getLogger(PropagateToChukwa.class.getCanonicalName());

	public static void deletion(String vmName) {
		byte[] requestBody = deletionBuildBody(vmName);
		Map<String, String> requestHeaders = new HashMap<String, String>();
		/*
		 * POST http://134.60.30.116:8080/chukwa HTTP/1.1 User-Agent: Jakarta
		 * Commons-HttpClient/3.1 Content-Length: 2164127 Content-Type:
		 * application/octet-stream Host: 134.60.30.116:8080
		 */
		requestHeaders.put("Content-Length", String.valueOf(requestBody.length));
		requestHeaders.put("Content-Type", "application/octet-stream");
		requestHeaders.put("Host", "134.60.64.143:8080");
		invokeForwarder(requestHeaders, requestBody);
	}

	private static void invokeForwarder(Map<String, String> requestHeaders, byte[] requestBody) {
		String chukwaUrl = "http://134.60.64.143:8080/chukwa";

		try {
			logger.log(Level.INFO, "propagadeToChukwa will send request to " + chukwaUrl);
			String result = HttpForwarder.simpleForward("POST", chukwaUrl, requestHeaders, requestBody);
			logger.log(Level.INFO, "Chukwa answer: " + result);
		} catch(RuntimeException ex){
			logger.log(Level.SEVERE, "problem when writing data to chukwa: ", ex);
		} catch(Error er){
			logger.log(Level.SEVERE, "problem when writing data to chukwa: ", er);
		} finally {
			logger.log(Level.INFO, "chukwa propagation done");
		}

	}

	private static byte[] deletionBuildBody(String vmUuid) {
		int PROTOCOL_VERSION = 1;
		long seqID = 1;
		String source = "VMIControllerOpenStack";
		String tags = "";
		String streamName = "Results from VMI Controller OpenStack about new VM";
		String dataType = "DeleteVM";
		String debuggingInfo = "";
		String data = "VMID" + "\t" + "isDeleted" + "\n" + vmUuid + "\t" + true + "\n";

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream w = new DataOutputStream(baos);
		// header fields for chukwa
		try {
			w.writeInt(1); // events
			w.writeInt(PROTOCOL_VERSION);
			w.writeLong(seqID);
			w.writeUTF(source); // computenode18
			w.writeUTF(tags);
			w.writeUTF(streamName);
			w.writeUTF(dataType);
			w.writeUTF(debuggingInfo);
			w.writeInt(1); // # of records
			// actual monitoring data
			w.writeInt(data.getBytes().length); // size of data
			w.writeUTF(data);
			w.flush();
			w.close();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Cannot create ChukwaRequestBody.", e);
			return null;
		}
		byte[] result = baos.toByteArray();
		try {
			baos.close();
		} catch (IOException e) {
		}
		return result;
	}

	public static void instantiation(String vmUuid, String applicationType, String applicationTypeInstance, String applicationComponent, String applicationComponentInstance) {
		String chukwaUrl = "http://134.60.64.143:8080/chukwa";
		logger.log(Level.INFO, "propagadeToChukwa preparing data for sending to " + chukwaUrl);
		byte[] requestBody = instantiationBuildBody(vmUuid, applicationType, applicationTypeInstance, applicationComponent, applicationComponentInstance);
		Map<String, String> requestHeaders = new HashMap<String, String>();
		/*
		 * POST http://134.60.30.116:8080/chukwa HTTP/1.1 User-Agent: Jakarta
		 * Commons-HttpClient/3.1 Content-Length: 2164127 Content-Type:
		 * application/octet-stream Host: 134.60.30.116:8080
		 */
		requestHeaders.put("Content-Length", String.valueOf(requestBody.length));
		requestHeaders.put("Content-Type", "application/octet-stream");
		requestHeaders.put("Host", "134.60.64.143:8080");
		invokeForwarder(requestHeaders, requestBody);
	}

	private static byte[] instantiationBuildBody(String vmUuid, String applicationType, String applicationTypeInstance, String applicationComponent, String applicationComponentInstance) {
		int PROTOCOL_VERSION = 1;
		long seqID = 1;
		String source = "VMIControllerOpenStack";
		String tags = "";
		String streamName = "Results from VMI Controller OpenStack about new VM";
		String dataType = "VmMetadata";
		String debuggingInfo = "";
		String data = "VMID" + "\t" + "applicationType" + "\t" + "applicationTypeInstance" + "\t" + "applicationComponent" + "\t" + "applicationComponentInstance" + "\n" + vmUuid + "\t"
				+ applicationType + "\t" + applicationTypeInstance + "\t" + applicationComponent + "\t" + applicationComponentInstance + "\n";

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream w = new DataOutputStream(baos);
		// header fields for chukwa
		try {
			w.writeInt(1); // events
			w.writeInt(PROTOCOL_VERSION);
			w.writeLong(seqID);
			w.writeUTF(source); // computenode18
			w.writeUTF(tags);
			w.writeUTF(streamName);
			w.writeUTF(dataType);
			w.writeUTF(debuggingInfo);
			w.writeInt(1); // # of records
			// actual monitoring data
			w.writeInt(data.getBytes().length); // size of data
			w.writeUTF(data);
			w.flush();
			w.close();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Cannot create ChukwaRequestBody.", e);
			return null;
		}
		byte[] result = baos.toByteArray();
		try {
			baos.close();
		} catch (IOException e) {
		}
		logger.log(Level.INFO, "returning ChukwasRequestBody: " + result);
		return result;
	}
	
	public static void writeState(String node,String state) {
		String chukwaUrl = "http://134.60.64.143:8080/chukwa";
		byte[] requestBody = writeStateBuildBody(node,state);
		Map<String, String> requestHeaders = new HashMap<String, String>();
		/*
		 * POST http://134.60.30.116:8080/chukwa HTTP/1.1 User-Agent: Jakarta
		 * Commons-HttpClient/3.1 Content-Length: 2164127 Content-Type:
		 * application/octet-stream Host: 134.60.30.116:8080
		 */
		requestHeaders.put("Content-Length", String.valueOf(requestBody.length));
		requestHeaders.put("Content-Type", "application/octet-stream");
		requestHeaders.put("Host", "134.60.64.143:8080");
		invokeForwarder(requestHeaders, requestBody);
	}

	private static byte[] writeStateBuildBody(String node,String state) {
		int PROTOCOL_VERSION = 1;
		long seqID = 1;
		String source = "VMIControllerOpenStack";
		String tags = "";
		String streamName = "Results from VMI Controller OpenStack about new VM";
		String dataType = "WriteState";
		String debuggingInfo = "";
		String data = "NodeName" + "\t" + "State" + "\n" + node + "\t" + state + "\n";

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream w = new DataOutputStream(baos);
		// header fields for chukwa
		try {
			w.writeInt(1); // events
			w.writeInt(PROTOCOL_VERSION);
			w.writeLong(seqID);
			w.writeUTF(source); // computenode18
			w.writeUTF(tags);
			w.writeUTF(streamName);
			w.writeUTF(dataType);
			w.writeUTF(debuggingInfo);
			w.writeInt(1); // # of records
			// actual monitoring data
			w.writeInt(data.getBytes().length); // size of data
			w.writeUTF(data);
			w.flush();
			w.close();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Cannot create ChukwaRequestBody.", e);
			return null;
		}
		byte[] result = baos.toByteArray();
		try {
			baos.close();
		} catch (IOException e) {
		}
		return result;
	}

}
