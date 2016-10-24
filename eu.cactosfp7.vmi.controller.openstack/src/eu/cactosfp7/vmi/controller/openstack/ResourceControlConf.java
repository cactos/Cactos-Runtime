package eu.cactosfp7.vmi.controller.openstack;

import java.util.Dictionary;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import java.util.List;

public class ResourceControlConf implements ManagedService {

	public static final String RESOURCE_CONTROL_PREFIX = "resource_control_prefix";
	public static final String RESOURCE_CONTROL_WHITELIST = "resource_control_whitelist";
	private static final String IPMIPROXY_PORT = "ipmiproxy_port";
	
	private volatile Dictionary<String, ?> properties;
	
	/**
	 * Method not called my ConfigAdminService but my VMIServiceImpl!
	 */
	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		this.properties = properties;
	}

	public String getPrefix(){
		return (String) properties.get(RESOURCE_CONTROL_PREFIX);
	}

	public boolean isOnWhitelist(String nodename){
		if(nodename == null || nodename.isEmpty())
			return false;
		
		String list = (String) properties.get(RESOURCE_CONTROL_WHITELIST);
		String[] aList = list == null ? new String[] {} : list.split(",");
		for(String cmp : aList) {
			if(cmp == null || cmp.isEmpty())
				continue;
			if(nodename.equals(cmp.trim()))
				return true;
		}
		return false;
	}
	
	public int getPort(){
		return Integer.valueOf((String)properties.get(IPMIPROXY_PORT));
	}
	
}
