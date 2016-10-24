/**
 * 
 */
package eu.cactosfp7.vmi.openstack.services;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import eu.cactosfp7.runtimemanagement.IRuntimeManagement;

@Path("/CACTOS/Management")
public class RuntimeManagement implements IRuntimeManagement {

	@POST
	@Path("vms")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public String startVM(String flavourRef, String vmImageRef, Map<String, String> inputParameters) {
		/*
		 * TODO Parse JSON Call RuntimeManagment return value
		 */
		return null;
	}

	@POST
	@Path("applications")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public String startApplication(String appRef, Map<String, String> inputParameters) {
		/*
		 * TODO Parse JSON Call RuntimeManagment return value
		 */
		return null;
	}

	@DELETE
	@Path("vms")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public boolean stopVM(String vmRef, Map<String, String> inputParameters) {
		/*
		 * TODO Parse JSON Call RuntimeManagment return value
		 */
		return false;
	}

	@DELETE
	@Path("applications")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public boolean stopApplication(String appInstanceRef, Map<String, String> inputParameters) {
		/*
		 * TODO Parse JSON Call RuntimeManagment return value
		 */
		return false;
	}

}
