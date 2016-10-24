package eu.cactosfp7.vmi.openstack.services;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import eu.cactosfp7.cloudiator.MolproJob;
import eu.cactosfp7.cloudiator.DataPlayComponent;
import eu.cactosfp7.cloudiator.DataPlayApplicationEnactor;

@Path("/applications")
public class Applications {

	private static final Logger logger = Logger.getLogger(Applications.class.getName());
	private static final Gson gson = new Gson();
	/**
	 * TESTING ONLY no deeper sense behind this method
	 * 
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getApplications() {
		logger.info("Incoming get request to getApplications()");
		return "Molpro, Dataplay";
	}

	/**
	 * Start a new DataPlay Application
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Path("dataplay")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String createDataplayInstance(String dataplaydeployment) {
		logger.info("Incoming POST request to createDataplayInstance()");
		Type listType = new TypeToken<List<DataPlayComponent>>() {}.getType();
		List<DataPlayComponent> dataplayList = gson.fromJson(dataplaydeployment, listType);
		
		// check if we have all the information we need //
		Object retVal = DataPlayApplicationEnactor.isValidDataPlaySpecification(dataplayList);
		if(retVal != null && (retVal instanceof String)) {
			String error = (String) retVal;
			logger.log(Level.SEVERE, "DataPlaySpecification seems to be invalid!" + error);
			return "{\"success\": false, \"error\": \"" + error +"\"}";
		}
		
		logger.log(Level.INFO, "Parsed dataplay specification: " + (Map<String,DataPlayComponent>) retVal);
		
		// now that we have the information, deploy the application //
		long appInstance = 0;
		try {
			appInstance = DataPlayApplicationEnactor.deploy((Map<String,DataPlayComponent>) retVal);
		} catch(Exception e){
			logger.log(Level.SEVERE, "Exception while deploying Dataplay!", e);
			return "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}";
		}
		return "{\"success\": true, \"applicationInstance\": " + appInstance + " }"; 
	}		
	
	/**
	 * Start a new Molpro Job
	 * 
	 * @return
	 */
	@Path("molpro")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String createMolproJob(String molproJobString) {
		logger.info("Incoming POST request to createMolproJob()");
		//MolproJob molproJob = gson.fromJson(molproJobString, MolproJob.class);
		Type listType = new TypeToken<List<MolproJob>>() {}.getType();
		List<MolproJob> molproList = gson.fromJson(molproJobString, listType);
		if(molproList.size() != 1) {
			return "{\"success\": false, \"error\": \"Invalid JSON format\"}";
		}
		MolproJob molproJob = molproList.get(0);
		if(molproJob == null ||
		    !"MolproComponent".equals(molproJob.name) ||
		    1 != molproJob.instances) {
			return "{\"success\": false, \"error\": \"Invalid job description\"}";
		}
		// else: proceed as before. 		
		logger.info("Going to deploy MolproJob " + molproJob);
		long appInstance = 0;
		try{
			appInstance = molproJob.deploy();
		}catch(Exception e){
			return "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}";
		}
		return "{\"success\": true, \"applicationInstance\": " + appInstance + " }"; 
	}
	
}
