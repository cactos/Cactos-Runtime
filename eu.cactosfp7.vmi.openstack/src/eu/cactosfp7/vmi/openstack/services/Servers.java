package eu.cactosfp7.vmi.openstack.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.openstack4j.model.compute.Flavor;

import eu.cactosfp7.ossession.service.OsSessionService;
import eu.cactosfp7.ossessionclient.OsSessionClient;
import eu.cactosfp7.ossessionclient.VmiOpenstackConstants;
import eu.cactosfp7.runtimemanagement.service.RuntimeManagementException;
import eu.cactosfp7.runtimemanagement.service.RuntimeManagementServiceLegacy;
import eu.cactosfp7.vmi.openstack.RuntimeManagementClient;
import eu.cactosfp7.vmi.openstack.models.Server;

@Path("/{tenantid}/servers")
public class Servers {

	private static final Logger logger = Logger.getLogger(Servers.class
			.getName());

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response boot(@PathParam("tenantid") String tenantid, Server server) {
		
		logger.info("Boot request for new vm/server " + server + " for tenant " + tenantid);
		/*
		 * Example Json of Server {"server": { "name": "testvm-tc", "imageRef":
		 * "bff7c603-52ad-43e2-bb7d-573c757fefe9", "availability_zone":
		 * "default", "flavorRef": "1", "max_count": 1, "min_count": 1,
		 * "metadata": {"metafield": "metavalue"} } }
		 */
		try {
			OsSessionService sessionService = OsSessionClient.INSTANCE.getService();
			Flavor flavor = sessionService.getCactosOsSession().lookupFlavor(server.getFlavorRef());
			if (flavor == null) {
				String msg = "Flavor lookup failed for flavor reference '" + server.getFlavorRef() + "'.";
				logger.warning(msg);
				return Response.status(410).type(MediaType.TEXT_PLAIN).entity(msg).build();
			}
			logger.fine("OpenStack boot target located. TenantId:" + tenantid + ", FlavorRef: " + server.getFlavorRef() + ", Vcpus: " + flavor.getVcpus() + ", Ram: "
					+ flavor.getRam() + ", Disk: " + flavor.getDisk() + ", ImageRef: "
					+ server.getImageRef() + ", Metadata: " + server.getMetadata());
	
			Map<String, String> metadata = buildInputParameters(tenantid, server);
	
			RuntimeManagementServiceLegacy mgmtService = RuntimeManagementClient.INSTANCE
					.getService();
			try {
				String result = mgmtService.instatiate(flavor.getVcpus(), flavor.getRam(),
						flavor.getDisk(), server.getImageRef(), metadata);
				//String result = mgmtService.startVM(flavor.getId(), server.getImageRef(), metadata);		
				logger.info("RuntimeManagement initiate says: " + result + " for " + server);
				if(result != null && !result.isEmpty()) {
					return Response.ok().type(MediaType.TEXT_PLAIN).entity(result).build();
				} else {
					return Response.serverError().type(MediaType.TEXT_PLAIN).entity("result from RuntimeManagement is null or empty").build();
				}
			} catch(RuntimeManagementException ex) {
				return Response.serverError().type(MediaType.TEXT_PLAIN).entity(ex.getMessage()).build();
			}
		} catch(Error er) {
			logger.log(Level.SEVERE, "Error in instantiate");
			return Response.serverError().type(MediaType.TEXT_PLAIN).entity(er.getMessage()).build();
		} catch(Throwable t) {
			logger.log(Level.WARNING, "exception in instantiate: ", t);
			return Response.serverError().type(MediaType.TEXT_PLAIN).entity(t.getMessage()).build();	
		} finally {
			logger.log(Level.INFO, "Hijacking VM instantiate HTTP request done (" + server + ")");
		}
	}

	private Map<String, String> buildInputParameters(String tenantid, Server server) {
		Map<String,String> metadata = new HashMap<String, String>();

		// insert headers with prefix and body
		for (Map.Entry<String, List<String>> entry : server.getRequest_headers().entrySet()){
			StringBuilder value = new StringBuilder();
			for(String listEntry : entry.getValue()){
				if(value.length() != 0)
					value.append(",");
				value.append(listEntry);
			}
			String key = VmiOpenstackConstants.REQUEST_HEADERS + entry.getKey();
			metadata.put(key, value.toString());
		}
		metadata.put(VmiOpenstackConstants.REQUEST_JSON, server.getRequest_json());

		metadata.put(VmiOpenstackConstants.TENANTID, tenantid);
		//metadata.put(VmiOpenstack.FLAVORID, server.getFlavorRef());
		//metadata.put(VmiOpenstack.IMAGEID, server.getImageRef());

		// Add Metadata fields for CactoOpt
		if(server.getMetadata() != null)
			metadata.putAll(server.getMetadata());

		return metadata;
	}

	@DELETE
	@Path("{serverid}")
	public Response delete(@PathParam("tenantid") String tenantid, @PathParam("serverid") String serverid, @Context HttpHeaders headers) {
		/*
		 REQ: curl -i 'http://omistack-beta.e-technik.uni-ulm.de:8774/v2/b330159537df45c484986fb1ca4a88a4/servers/14f1cf6c-5321-4e01-8c9a-81e75cf1955a' 
		 -X DELETE -H "X-Auth-Project-Id: cactos-testing" -H "User-Agent: python-novaclient" -H "Accept: application/json" 
		 -H "X-Auth-Token: 106c20b1d87e48649b8b47f7635674ea"
		 */
		logger.log(Level.INFO, "Hijacking VM delete HTTP request. (" + serverid + ") Headers: " + headers);
		try {
			String project = headers.getHeaderString("X-Auth-Project-Id");
			String authToken = headers.getHeaderString("X-Auth-Token");
			Map<String,String> metadata = new HashMap<String, String>();
			metadata.put(VmiOpenstackConstants.TENANTNAME, project);
			metadata.put(VmiOpenstackConstants.TENANTID, tenantid);
			metadata.put(VmiOpenstackConstants.AUTH_TOKEN, authToken);
			RuntimeManagementServiceLegacy mgmtService = RuntimeManagementClient.INSTANCE.getService();
			mgmtService.delete(serverid, metadata);
			logger.log(Level.INFO, "Hijacking VM delete HTTP request done (" + serverid + ")");
			return Response.ok().type(MediaType.TEXT_PLAIN).build();
		} catch(RuntimeManagementException rte){
			logger.log(Level.WARNING, "exception in delete: ", rte);
			return Response.serverError().type(MediaType.TEXT_PLAIN).entity(rte.getMessage()).build();
		} catch(Error er) {
			logger.log(Level.SEVERE, "Error in delete");
			return Response.serverError().type(MediaType.TEXT_PLAIN).entity(er.getMessage()).build();
		} catch(Throwable t) {
			logger.log(Level.WARNING, "exception in delete: ", t);
			return Response.serverError().type(MediaType.TEXT_PLAIN).entity(t.getMessage()).build();	
		}
		//mgmtService.stopVM(serverid, metadata);
	}


	// /**
	// * TESTING ONLY
	// * no deeper sense behind this method
	// * @return
	// */
	// @GET
	// @Produces(MediaType.APPLICATION_JSON)
	// public Server getAnyServer() {
	// Server s = new Server();
	// s.setAvailability_zone("default");
	// s.setDelete_on_termination("false");
	// s.setFlavorRef("flavor4711");
	// s.setUuid("UUID6969");
	// s.setImageRef("image0815");
	// s.setName("SuperVM-690");
	// return s;
	// }


	/*
	 *	EXAMPLE REST RESPONSE
	 *  of nova boot request
	 *  
		{
			"server": {
					"security_groups": [{"name": "default"}], 
					"OS-DCF:diskConfig": "MANUAL", 
					"id": "1491fa92-de96-4b08-9192-1829e6043686", 
					"links": [{
						"href": "http://omistack-beta.e-technik.uni-ulm.de:8774/v2/ca70457511d7478eaa22894fbb45f04d/servers/1491fa92-de96-4b08-9192-1829e6043686",
						"rel": "self"
					},{
						"href": "http://omistack-beta.e-technik.uni-ulm.de:8774/ca70457511d7478eaa22894fbb45f04d/servers/1491fa92-de96-4b08-9192-1829e6043686", 
						"rel": "bookmark"}], 
					"adminPass": "Tj2iJu9jBE66"
			}
		}
	 * 
	 */

}
