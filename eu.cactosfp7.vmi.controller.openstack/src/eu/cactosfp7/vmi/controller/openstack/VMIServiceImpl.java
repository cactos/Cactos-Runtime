package eu.cactosfp7.vmi.controller.openstack;

import java.util.Dictionary;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.cdo.util.CommitException;
import org.eclipse.emf.cdo.util.ConcurrentAccessException;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import eu.cactosfp7.vmi.controller.IVMIService;
import eu.cactosfp7.vmi.controller.openstack.worker.ExecuteDeletion;
import eu.cactosfp7.vmi.controller.openstack.worker.ExecuteInitialPlacement;
import eu.cactosfp7.vmi.controller.openstack.worker.ExecuteOptimisationPlan;
import eu.cactosfp7.vmi.controller.openstack.worker.IterateOptimisationPlans;

/**
 * OSGi service implementation for {@link IVMIService}.
 * 
 */
public class VMIServiceImpl implements IVMIService, ManagedService {

	//private static final Logger log = Logger.getLogger(VMIServiceImpl.class.getName());
	// TODO: making these classes static is really an ugly hack
	public static final IpmiProxyConf ipmiProxyConf = new IpmiProxyConf();
	public static final ResourceControlConf resourceControlConf = new ResourceControlConf();

	private static final String ERROR_MESSAGE = "Error occurred.";
	private static final Logger logger = Logger.getLogger(VMIServiceImpl.class.getCanonicalName());
	
	@Override
	public boolean execute(String uuid) throws ConcurrentAccessException, CommitException {
		ExecuteOptimisationPlan action = new ExecuteOptimisationPlan(uuid);
		return action.work();
	}

	
	public boolean execute() throws ConcurrentAccessException, CommitException{
		IterateOptimisationPlans action = new IterateOptimisationPlans();
		return action.work();
	}
	
	@Override
	public boolean executePlacement(String vmId, String computeNodeUuid) {
		try{
			ExecuteInitialPlacement action = new ExecuteInitialPlacement(vmId, computeNodeUuid);
			return action.work();
		} catch(Error er) {
			logger.severe(ERROR_MESSAGE);
			return false;
		} catch(Throwable t){ // everything that is not an Error
			logger.log(Level.SEVERE, "exception while creating a vm", t);
			return false;
		}		

	}

	@Override
	public boolean executeDeletion(String vmId, Map<String, String> meta) {
		ExecuteDeletion action = new ExecuteDeletion(vmId, meta);
		return action.work();
	}

	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		ipmiProxyConf.updated(properties);
		resourceControlConf.updated(properties);
	}
	
}
