package eu.cactosfp7.cactoscale.runtimemodelupdater;

import java.util.logging.Logger;

import eu.cactosfp7.infrastructuremodels.builder.applicationmodelload.ApplicationModelLoadBuilder;

public class ApplicationModelLoadBuilderClient {
	public static ApplicationModelLoadBuilderClient INSTANCE;
	
	public ApplicationModelLoadBuilderClient(){
		if(INSTANCE != null)
			throw new RuntimeException("Instantiating newAApplicationModelLoadBuilderClient is not allowed!");
		INSTANCE = this;
	}
	
	private static final Logger logger = Logger.getLogger(ApplicationModelLoadBuilderClient.class.getName());
	private ApplicationModelLoadBuilder service;
	
	/**Bind method for discovered service.
	 * @param service The discovered service.
	 */
	public synchronized void bindService(ApplicationModelLoadBuilder _service) {
		INSTANCE = this;
		service = _service;
		logger.info("ApplicationModelLoadBuilder connected.");
	}
	
	/**Unbind method for discovered service.
	 * @param service The removed service.
	 */
	public synchronized void unbindService(ApplicationModelLoadBuilder _service) {
		service = null;
		logger.info("ApplicationModelLoadBuilder disconnected.");
	} 
	
	public ApplicationModelLoadBuilder getService(){
//		if(service == null)
//			throw new RuntimeException("ApplicationModelLoadBuilder unbound.");
		return service;
	}
	
}
