package eu.cactosfp7.vmi.controller.openstack;

import java.util.Dictionary;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

public class IpmiProxyConf implements ManagedService {

	private static final String IPMIPROXY_TOKEN = "ipmiproxy_token";
	private static final String IPMIPROXY_ADDRESS = "ipmiproxy_address";
	private static final String IPMIPROXY_PORT = "ipmiproxy_port";
	
	private Dictionary<String, ?> properties;
	
	/**
	 * Method not called my ConfigAdminService but my VMIServiceImpl!
	 */
	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		this.properties = properties;
	}

	public String getToken(){
		return (String) properties.get(IPMIPROXY_TOKEN);
	}

	public String getAddress(){
		return (String) properties.get(IPMIPROXY_ADDRESS);
	}
	
	public int getPort(){
		return Integer.valueOf((String)properties.get(IPMIPROXY_PORT));
	}
	
}
