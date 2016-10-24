package eu.cactosfp7.runtimemanagement;

import java.util.Map;

/**Entry point for managing Virtual Machines and Applications using CACTOS.
 * The semantic and content is already based on the CACTOS Infrastructure model. 
 * This class allows (de)coupling Virtualization Middleware, e.g. Open Stack or FCO, and connecting them to CACTOS. 
 * 
 * @author hgroenda
 *
 */
public interface IRuntimeManagement {

	/**Starts a new virtual machine using a Flavour.
	 * @param flavourRef UUID of the CACTOS Flavour to boot.
	 * @param vmImageRef UUID of the CACTOS VMImage to boot.
	 * @param inputParameters Input parameters, e.g. tenant id.
	 * @return UUID of the instantiated Virtual machine. <code>null</code> if not successful.
	 */
	public String startVM(String flavourRef, String vmImageRef, Map<String, String> inputParameters);
	
	/**Starts a new application.
	 * @param appRef UUID of the application.
	 * @param inputParameters Input parameters, e.g. tenant id.
	 * @return UUID of the Application Instance. <code>null</code> if not successful.
	 */
	public String startApplication(String appRef, Map<String, String> inputParameters);

	/**
	 * Stops a running virtual machine.
	 * @param vmRef UUID of the CACTOS Virtual Machine to remove.
	 * @param inputParameters Input parameters, e.g. tenant id.
	 * @return Success state. <code>true</code> if successful, <code>false</code> otherwise. 
	 */
	public boolean stopVM(String vmRef, Map<String, String> inputParameters);

	/**Stops a running application.
	 * @param appInstanceRef UUID of the application instance.
	 * @param inputParameters Input parameters, e.g. tenant id.
	 * @return Success state. <code>true</code> if successful, <code>false</code> otherwise. 
	 */
	public boolean stopApplication(String appInstanceRef, Map<String, String> inputParameters);
	
}
