package eu.cactosfp7.runtimemanagement.service;

import java.util.Map;

public interface RuntimeManagementServiceLegacy {


	/**
	 * @param tenant_id
	 * @param vcores
	 * @param memory
	 * @param diskspace
	 * @param imageref
	 * @param meta
	 * @return
	 */
	public String instatiate(int vcores, int memory,
			int diskspace, String imageref, Map<String, String> meta) throws RuntimeManagementException;

	/**
	 * Propagate delete command for a virtual machine to the Cloud middleware
	 * @param vmName
	 * @param meta
	 * @return
	 * @throws RuntimeManagementException 
	 */
	public String delete(String vmName, Map<String, String> meta) throws RuntimeManagementException;

	
}
