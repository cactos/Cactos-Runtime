package eu.cactosfp7.vmi.openstack;

import java.util.logging.Logger;

import eu.cactosfp7.runtimemanagement.service.RuntimeManagementServiceLegacy;

public final class RuntimeManagementClient {
	public static RuntimeManagementClient INSTANCE;
	
	public RuntimeManagementClient(){
		if(INSTANCE != null)
			throw new RuntimeException("Instantiating new RuntimeManagementClient is not allowed!");
		INSTANCE = this;
	}
	
	private static final Logger logger = Logger.getLogger(RuntimeManagementClient.class.getName());
	private RuntimeManagementServiceLegacy service;
	
	/**Bind method for discovered service.
	 * @param service The discovered service.
	 */
	public synchronized void bindService(RuntimeManagementServiceLegacy _service) {
		INSTANCE = this;
		service = _service;
		logger.info("RuntimeManagementServiceLegacy connected.");
	}
	
	/**Unbind method for discovered service.
	 * @param service The removed service.
	 */
	public synchronized void unbindService(RuntimeManagementServiceLegacy _service) {
		service = null;
		logger.info("RuntimeManagementServiceLegacy disconnected.");
	} 
	
	public RuntimeManagementServiceLegacy getService(){
		if(service == null)
			throw new RuntimeException("RuntimeManagementServiceLegacy unbound.");
		return service;
	}
	
}
