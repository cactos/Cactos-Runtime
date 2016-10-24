package eu.cactosfp7.vmi.controller.openstack;

import java.util.logging.Logger;

import eu.cactosfp7.infrastructuremodels.builder.applicationmodelinstance.ApplicationModelInstanceBuilder;

public class ApplicationModelInstanceBuilderClient {
	public static ApplicationModelInstanceBuilderClient INSTANCE;
	
	public ApplicationModelInstanceBuilderClient(){
		if(INSTANCE != null)
			throw new RuntimeException("Instantiating new ApplicationModelInstanceBuilderClient is not allowed!");
		INSTANCE = this;
	}
	
	private static final Logger logger = Logger.getLogger(ApplicationModelInstanceBuilderClient.class.getName());
	private ApplicationModelInstanceBuilder service;
	
	/**Bind method for discovered service.
	 * @param service The discovered service.
	 */
	public synchronized void bindService(ApplicationModelInstanceBuilder _service) {
		INSTANCE = this;
		service = _service;
		logger.info("ApplicationModelInstanceBuilder connected.");
		
//		long appInstanceId = 32768;
//		new CamelEntryPoint().pushDeploymentToCactos(appInstanceId);
	}
	
	/**Unbind method for discovered service.
	 * @param service The removed service.
	 */
	public synchronized void unbindService(ApplicationModelInstanceBuilder _service) {
		service = null;
		logger.info("ApplicationModelInstanceBuilder disconnected.");
	} 
	
	public ApplicationModelInstanceBuilder getService(){
//		if(service == null)
//			throw new RuntimeException("ApplicationModelInstanceBuilder unbound.");
		return service;
	}
	
}
