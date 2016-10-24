package eu.cactosfp7.runtimemanagement.impl;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.cactosfp7.runtimemanagement.impl.lifecycles.Deletion;
import eu.cactosfp7.runtimemanagement.impl.lifecycles.Instantiation;
import eu.cactosfp7.runtimemanagement.impl.lifecycles.InstantiationNew;
import eu.cactosfp7.runtimemanagement.service.RuntimeManagementException;
import eu.cactosfp7.runtimemanagement.service.RuntimeManagementServiceLegacy;
import eu.cactosfp7.runtimemanagement.util.Lifecycle;

public class RuntimeManagementServiceLegacyImpl implements RuntimeManagementServiceLegacy {

	private static final String ERROR_MESSAGE = "Error occurred.";
	private static final Logger logger = Logger.getLogger(RuntimeManagementServiceLegacy.class.getCanonicalName());
	
	@Override
	public String instatiate(int vcores, int memory,
			int diskspace, String imageref, Map<String, String> meta) throws RuntimeManagementException {
		try{
			Lifecycle init = new InstantiationNew(vcores, memory, diskspace, imageref, meta);
			init.start();
			return init.result();
		} catch(Error er) {
			logger.severe(ERROR_MESSAGE + "Message: " + er.getMessage());
			logger.severe(er.getStackTrace().toString());
			throw er;
		} catch(RuntimeManagementException ex) {
			logger.log(Level.WARNING, "RuntimeManagementException while creating a vm", ex);
			throw ex;
		} catch(Throwable t){ // everything that is not an Error
			logger.log(Level.SEVERE, "exception while creating a vm", t);
			throw new RuntimeManagementException("error occurred (create): " + t.getMessage(), t);
		}
	}

	@Override
	public String delete(String vmName, Map<String, String> meta) throws RuntimeManagementException {
		try {
			Lifecycle delete = new Deletion(vmName, meta);
			delete.start();
			return delete.result();
		} catch(Error er) {
			logger.severe(ERROR_MESSAGE + "Message: " + er.getMessage());
			logger.severe(er.getStackTrace().toString());
			throw er;
		} catch(RuntimeManagementException ex) {
			logger.log(Level.WARNING, "RuntimeManagementException while deleting a vm", ex);
			throw ex;
		} catch(Throwable t){ // everything that is not an Error
			logger.log(Level.SEVERE, "exception while deleting a vm", t);
			throw new RuntimeManagementException("error occurred (delete): " + t.getMessage(), t);
		}
	}
}
