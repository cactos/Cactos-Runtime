package eu.cactosfp7.vmi.controller.openstack.worker;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.cactosfp7.ossession.service.OsSession;
import eu.cactosfp7.ossessionclient.OsSessionClient;
import eu.cactosfp7.ossessionclient.VmiOpenstackConstants;
import eu.cactosfp7.runtimemanagement.util.HttpForwarder;

public final class ExecuteDeletion implements VMIWorker {

	private static final Logger logger = Logger.getLogger(ExecuteDeletion.class.getName());

	private final String vmUuid;
	private final Map<String, String> meta;

	public ExecuteDeletion(String vmUuid, Map<String, String> meta) {
		this.vmUuid = vmUuid;
		this.meta = meta;
	}

	@Override
	public boolean work() {
		logger.log(Level.WARNING, "will remove vm " + vmUuid);
		
		String tenantId = meta.get(VmiOpenstackConstants.TENANTID);
		String tenantName = meta.get(VmiOpenstackConstants.TENANTNAME);
		String authToken = meta.get(VmiOpenstackConstants.AUTH_TOKEN);
		
		Map<String,String> requestHeaders = new HashMap<String,String>();
		//requestHeaders.put("X-Auth-Project-Id", tenantName);
		requestHeaders.put("X-Auth-Token", authToken);
		requestHeaders.put("Accept", "application/json");
/*
Host: 134.60.64.160:9090
Connection: keep-alive
Accept-Encoding: gzip, deflate
Accept: application/json
User-Agent: python-novaclient
X-Auth-Token: e11cff50c9a44556a43b8f5a10035251
Content-Length: 0
 */
		
		
		OsSession openstackSession = OsSessionClient.INSTANCE.getService().getCactosOsSession();
		String osEndpointAsProxy = openstackSession.getEndpointAsProxy();
		String targetUrl = osEndpointAsProxy + "" + tenantId + "/servers/" + vmUuid;
		logger.info("Forward Boot Request Headers: " + requestHeaders + "");
		String result = HttpForwarder.simpleForward("DELETE", targetUrl, requestHeaders, null);
		logger.log(Level.INFO, "Delete Response: " + result);
		
		if(result != null && !result.isEmpty())
			return true;
		return false;
		/*
		 REQ: curl -i 'http://omistack-beta.e-technik.uni-ulm.de:8774/v2/b330159537df45c484986fb1ca4a88a4/servers/14f1cf6c-5321-4e01-8c9a-81e75cf1955a' 
		 -X DELETE -H "X-Auth-Project-Id: cactos-testing" -H "User-Agent: python-novaclient" -H "Accept: application/json" 
		 -H "X-Auth-Token: 106c20b1d87e48649b8b47f7635674ea"
		 */		
		
	}

}
