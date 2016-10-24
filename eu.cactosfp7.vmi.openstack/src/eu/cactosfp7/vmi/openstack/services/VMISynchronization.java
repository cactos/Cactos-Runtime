package eu.cactosfp7.vmi.openstack.services;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import eu.cactosfp7.runtimemanagement.IVMISychronization;

@Path("/CACTOS")
public class VMISynchronization implements IVMISychronization {

	@POST
	@Path("updateFlavours")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public String updateFlavours() {
		// TODO receive list of Flavours and update list in CACTOS
		// Infrastructure Model accordingly. UUIDs must remain the same for
		// application to work properly. Check if an OpenStack UUID is contained
		// and only update values. Add new entries in CACTOS if not found.
		// Untouched elements in the model can be removed at the end (just keep
		// them in a separate list and remove them from it if touched).
		return "{ \"success\" : \"true\"}";
	}

	@POST
	@Path("updateVMImages")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public String updateVMImages() {
		// TODO receive list of VMImages and update list in CACTOS
		// Infrastructure Model accordingly. UUIDs must remain the same for
		// volumes and disk usage to work properly. Check if an OpenStack UUID
		// is contained and only update values. Add new entries in CACTOS if not
		// found. Untouched elements in the model can be removed at the end
		// (just keep them in a separate list and remove them from it if
		// touched).
		return "{ \"success\" : \"true\"}";
	}

}
