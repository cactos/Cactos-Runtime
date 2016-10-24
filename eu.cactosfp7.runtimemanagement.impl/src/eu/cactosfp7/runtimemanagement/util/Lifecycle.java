package eu.cactosfp7.runtimemanagement.util;

import eu.cactosfp7.runtimemanagement.service.RuntimeManagementException;

public interface Lifecycle {

	public void start() throws RuntimeManagementException;

	public String result();
	
}
