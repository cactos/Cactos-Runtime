package eu.cactosfp7.cactoscale.runtimemodelupdater;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		SettingsInitializer.INSTANCE.stopRuntimeModelUpdater();
	}

}
