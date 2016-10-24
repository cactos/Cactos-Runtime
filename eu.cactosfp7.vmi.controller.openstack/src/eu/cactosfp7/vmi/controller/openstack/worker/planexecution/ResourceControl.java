package eu.cactosfp7.vmi.controller.openstack.worker.planexecution;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Dictionary;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.measure.quantity.Dimensionless;
import javax.measure.unit.NonSI;

import org.jscience.physics.amount.Amount;
import org.osgi.service.cm.ConfigurationException;

import eu.cactosfp7.optimisationplan.ExecutionStatus;
import eu.cactosfp7.optimisationplan.ResourceControlAction;
import eu.cactosfp7.vmi.controller.openstack.ResourceControlConf;
import eu.cactosfp7.vmi.controller.openstack.VMIServiceImpl;

public class ResourceControl implements OptimisationActionStepExecution {

	private final ResourceControlAction action;
	
	private static final Logger logger = Logger.getLogger(ResourceControl.class.getName());
	
	public ResourceControl(ResourceControlAction rcAction) {
		action = rcAction;
	}
	
	private ExecutionStatus doCheckPreconditions(String hostname, String vmName, int value) {	
		if(value < 1) {
			logger.log(Level.SEVERE, "Cannot set value to percentage lower than 1%.");
			return ExecutionStatus.COMPLETED_FAILED;
		}
		
		if(vmName == null || vmName.isEmpty()) {
			logger.log(Level.SEVERE, "Cannot retrieve vmname. received 'null'.");
			return ExecutionStatus.COMPLETED_FAILED;
		}
		
		if(hostname == null || hostname.isEmpty()) {
			logger.log(Level.SEVERE, "Cannot retrieve name of computenode. received 'null'.");
			return ExecutionStatus.COMPLETED_FAILED;
		}
		
		if(! VMIServiceImpl.resourceControlConf.isOnWhitelist(hostname)){
			logger.info("computenode '" + hostname + "' is not on whitelist. considering this as successfull as it reflects configuration.");
			return ExecutionStatus.COMPLETED_SUCCESSFUL;
		}
		
		return null;
	}
	
	private URI validateAndBuildPrefixURI() {
		// lookup config variables
		String prefix = VMIServiceImpl.resourceControlConf.getPrefix();
		if(prefix == null || prefix.isEmpty()) {
			logger.log(Level.SEVERE, "configuration value for prefix is not set");
			return null;
		}
		
		URI uri = null;
		try {
			uri = URI.create(prefix);
		} catch(IllegalArgumentException e){
			logger.log(Level.SEVERE, "configuration value for prefix is not a valid URI: " + prefix);
		}
		if(! ("http".equalsIgnoreCase(uri.getScheme()) ||
				"https".equalsIgnoreCase(uri.getScheme()))) {
			logger.log(Level.SEVERE, "configuration value for prefix missing: no schema (http or https)");
			return null;
		}
		
		if(uri.getHost() == null || uri.getHost().isEmpty()) {
			logger.log(Level.SEVERE, "configuration value for host missing.");
			return null;
		}
		
		if(uri.getQuery() != null && !uri.getQuery().isEmpty()) {
			logger.log(Level.SEVERE, "configuration value for query set. this is not supported.");
			return null;
		}
		
		if(uri.getFragment() != null && !uri.getFragment().isEmpty()) {
			logger.log(Level.SEVERE, "configuration value for fragment set. this is not supported.");
			return null;
		}
		
		if(uri.getPath() == null || uri.getPath().isEmpty() || !uri.getPath().endsWith("/") || !uri.getPath().startsWith("/")) {
			logger.log(Level.SEVERE, "configuration value for path is not correct: path has to be available and start and end with '/'");
			return null;
		}
		
		try {
			if(uri.getPort() == -1) {
				if("http".equals(uri.getScheme())) {
					uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), 80, uri.getPath(), uri.getQuery(), uri.getFragment());
				} else if("https".equals(uri.getScheme())) {
					uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), 443, uri.getPath(), uri.getQuery(), uri.getFragment());
				}
			}
			return uri;
		} catch(URISyntaxException e) {
			logger.log(Level.SEVERE, "uri syntax is broken: " + uri);
			return null;
		}
	}
	
	private boolean run(String command){
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			logger.log(Level.INFO, "calling external process: '" + command + "'.");
			Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
			InputStream in = p.getInputStream();
			int j = -2;
			while(true) {
				j = in.read();
				if(j == -1) break;
				bout.write(j);
			}
			int ret = p.waitFor();
			// TODO: this is not entirely safe //
			String output = new String(bout.toByteArray());

			if(ret != 0) {
				logger.log(Level.SEVERE, "running request failed: " + ret + "; " + output);
				return false;
			}
			if(output == null && output.isEmpty() || output.trim().isEmpty()) {
				logger.log(Level.SEVERE, "could not parse output of command. " + command);
				return false;
			}
			
			String finalResult = "";
			if(output.indexOf(System.getProperty("line.separator")) > -1) {
				String[] splitLines = output.split(System.getProperty("line.separator"));
				if(splitLines == null || splitLines.length == 0) {
					logger.log(Level.SEVERE, "splitting lines failed: " + output);
					return false;
				}
				// jump directly to last one //
				finalResult = splitLines[splitLines.length - 1];
			} else {
				finalResult = output;
			}

			String[] chunks = finalResult.split("\\s");
			if(chunks == null || chunks.length < 3 || chunks[1] == null) {
				logger.log(Level.SEVERE, "splitting single line failed: '" + finalResult + "'");
				return false;
			}
			try {
				int retVal = Integer.parseInt(chunks[1]);
				if(retVal > 299 || retVal < 200) {
					logger.log(Level.SEVERE, "received unexpected return value: " + retVal);
					return false;
				}
				// the only success path //
				logger.log(Level.INFO, "return value: " + retVal);
				return true;
			} catch(NumberFormatException nfe) {
				logger.log(Level.SEVERE, "not a real number: " + chunks[1]);
				return false;
			}
		} catch(IOException ioe) {
			logger.log(Level.SEVERE, "could not run request. assuming failure.", ioe);
			return false;
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "could not run request. assuming failure.", e);
			return false;
		}
	}
	
	private ExecutionStatus invokeExternalService(URI prefix, String hostname, String vmName, int percentage) {
		String command = buildCurlCommand(prefix, hostname, vmName, percentage);
		boolean b = run(command);
		if(b) return ExecutionStatus.COMPLETED_SUCCESSFUL; 
		return ExecutionStatus.COMPLETED_FAILED;
	}
	
	private String buildCurlCommand(URI prefix, String hostname, String vmName, int percentage) {
		// TODO: this is extremely ugly and should be replaced by something more reasonable //
		String uri = buildTargetURI(prefix, hostname, vmName);
		String userInfo = prefix.getRawUserInfo();
		System.err.println("userInfo: " + userInfo);
		String loginString = "";
		if(userInfo != null && !userInfo.isEmpty()) {
			loginString = "--digest -u " + userInfo + " ";
		}
		String header = "-H \"Content-Type: application/json\" ";
		String command = "-X PUT ";
		String body = "-d '{\"status\" : \"enabled\", \"boundary\" : \"" + percentage + "\"}'";
		return "curl -S -s -i " + loginString + header + command + body + " " + uri + " | grep HTTP";
	}
	
	// https://omistack.e-technik.uni-ulm.de/vscale/computenode03/vm/instance-000011f1
	private String buildTargetURI(URI prefix, String computenode, String vmname) {
		StringBuilder builder = new StringBuilder();
		builder.append(prefix.getScheme()).append("://").append(prefix.getHost())
		.append(":").append(prefix.getPort()).append(prefix.getPath())
		.append(computenode).append("/vm/").append(vmname);
		return builder.toString();
	}
	
	public static void main(String[] args) throws ConfigurationException {
		Properties p = new Properties();
		//https://cactoOpta:nhmnjJqi@omistack.e-technik.uni-ulm.de/vscale/
		p.setProperty(ResourceControlConf.RESOURCE_CONTROL_PREFIX, "<add prefix here>");
		p.setProperty(ResourceControlConf.RESOURCE_CONTROL_WHITELIST, "<add list here>");
		Object o = VMIServiceImpl.resourceControlConf;
		if(o == null) throw new NullPointerException();
				
		VMIServiceImpl.resourceControlConf.updated((Dictionary<String,?>)(Object) p);
		// test the code //
		ResourceControl rc = new ResourceControl(null);
		
		// prefix not set: check
		// prefix with no schema: check
		// http port set to 80: check
		// https port set to 443: check
		// host required: check
		// query not accepted: check
		// fragment not accepted: check
		URI uri = rc.validateAndBuildPrefixURI();
		if(uri == null) {
			System.err.println("uri was null.");
			return;
		}
		System.err.println(uri);

		// percentage values < 1% are not accepted: check
		// null or empty string for vmname not allowed: check
		// null or empty string for hostname not allowed: check
		// check that hostname has to be in whitelist: check
		ExecutionStatus result = rc.doCheckPreconditions("computenode03", "123", 100);
		System.err.println(result);		
		if(result != null) return;
		
		// error when not reachable: check
		// error when not authorized: check
		String s = rc.buildCurlCommand(uri, "computenode03a", "instance-000011f1", 100);
		boolean b = rc.run(s);
		System.err.println(b);
	}
	
	@Override
	public ExecutionStatus execute() {
		
		URI uri = validateAndBuildPrefixURI();
		if(uri == null) {
			return ExecutionStatus.COMPLETED_FAILED;
		}
		String vmName = action.getAffectedVm().getInputParameters().get("vmName");
		String hostname = action.getControlledHypervisor().getNode().getName();
		int value = convertUnits();
		
		try {
			ExecutionStatus result = doCheckPreconditions(hostname, vmName, value);
			if(result != null) 
				return result;
			return invokeExternalService(uri, hostname, vmName, value);
		} finally {
			// anything to do here? //
		}
	}

	private int convertUnits() {
		Amount<Dimensionless> share = action.getResourceShare();
		// get the associated double value as a percent value
		double d = share.doubleValue(NonSI.PERCENT); 
		// TODO: add checks for values larger than Integer.MAX_INT
		return ((int) Math.round(d));
	}
}
