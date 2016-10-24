package eu.cactosfp7.vmi.openstack;

import java.util.LinkedList;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import eu.cactosfp7.cloudiator.CamelEntryPoint;
import eu.cactosfp7.vmi.openstack.models.ServerProvider;
import eu.cactosfp7.vmi.openstack.services.Applications;
import eu.cactosfp7.vmi.openstack.services.Servers;

public class Activator implements BundleActivator {

	private BundleContext context;

	private static final Logger logger = Logger.getLogger(Activator.class
			.getName());
	
	private LinkedList<ServiceRegistration<?>> regs = new LinkedList<ServiceRegistration<?>>();

	@Override
	public synchronized void start(BundleContext _context) throws Exception {
		context = _context;
		// Register RESTful services
		registerRestServices();
		createCloudiatorCamelEntrypoint();
	}

	private void createCloudiatorCamelEntrypoint() {
		logger.info("Call createCloudiatorCamelEntrypoint()");
		new CamelEntryPoint().createCloudiatorCamelEntrypoint();
	}

	public void registerRestServices() {
		logger.info("Register REST services ...");		
		regs.add(context.registerService(ServerProvider.class.getName(), new ServerProvider(), null));
		regs.add(context.registerService(Servers.class.getName(), new Servers(), null));
		regs.add(context.registerService(Applications.class.getName(), new Applications(), null));
		logger.info("REST Services registration done.");
	}

	@Override
	public synchronized void stop(BundleContext _context) throws Exception {
		context = _context;
		while(!regs.isEmpty()){
			regs.poll().unregister();
		}
	}

}
