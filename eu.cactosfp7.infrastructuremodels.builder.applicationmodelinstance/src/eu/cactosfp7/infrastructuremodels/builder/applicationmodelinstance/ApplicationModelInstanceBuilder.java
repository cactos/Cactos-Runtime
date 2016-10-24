package eu.cactosfp7.infrastructuremodels.builder.applicationmodelinstance;

import java.util.Map;

import eu.cactosfp7.optimisationplan.OptimisationActionStep;

/**
 * Interface for building application models in CDO server.
 * 
 * @author chauser
 */
public interface ApplicationModelInstanceBuilder {

	public enum UpdateAction{
		ADD,
		REMOVE
	}
	
	/**
	 * Create application model instances and map them to the existing virtual machine models in the CDO server.
	 * @param applicationName The name of the deployed application which should be linked in the CDO server
	 * @param vmName2compName A list of mappings of virtual machine names to application component names
	 * @param applicationInstanceId A unique identifier for this application instance
	 */
	public void createModelInstance(String applicationName, Map<String, String> vmName2compName, String applicationInstanceId);
	
	/**
	 * Update an application model instance and add or remove the mapping of a virtual machine to a component in the CDO server.
	 * @param applicationInstanceId A unique identifier for this application instance
	 * @param action The requested action according to UpdateAction, if the component should be removed or added to a virtual machine
	 * @param vmName2compName A map of virtual machine names to application component names
	 */
	public void updateModelInstance(String applicationInstanceId, UpdateAction action, Map<String, String> vmName2compName);	
	
	/**
	 * Extract cloudiator applicationInstanceId from the models in the CDO server.
	 * @param OptimisationActionStep scale in or scale out action
	 * @return long applicationInstanceId
	 */
	public String scaleActionToAppInstanceId(OptimisationActionStep action);
	
	/**
	 * Extract Cloud VM UUID from the models in the CDO server (vm name)
	 * @param OptimisationActionStep scale in or scale out action
	 * @return String vmname
	 */	
	public String scaleActionToComponentName(OptimisationActionStep action);
	
}
