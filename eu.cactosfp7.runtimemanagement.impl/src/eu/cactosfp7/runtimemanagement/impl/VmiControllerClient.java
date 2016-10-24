package eu.cactosfp7.runtimemanagement.impl;

import java.util.logging.Logger;

import eu.cactosfp7.vmi.controller.IVMIService;

public final class VmiControllerClient {
	
	public static VmiControllerClient INSTANCE;
	
	public VmiControllerClient(){
		if(INSTANCE != null)
			throw new RuntimeException("Instantiating new VmiControllerClient is not allowed!");
		INSTANCE = this;
	}
	
	private static final Logger logger = Logger.getLogger(VmiControllerClient.class.getName());
	private IVMIService service;
	
	/**Bind method for discovered service.
	 * @param service The discovered service.
	 */
	public synchronized void bindService(IVMIService _service) {
		INSTANCE = this;
		service = _service;
		logger.info("IVMIService connected.");
	}
	
	/**Unbind method for discovered service.
	 * @param service The removed service.
	 */
	public synchronized void unbindService(IVMIService _service) {
		service = null;
		logger.info("IVMIService disconnected.");
	}
	
	public IVMIService getService(){
		if(service == null)
			throw new RuntimeException("IVMIService unbound.");		
		return service;
	}
	
}
