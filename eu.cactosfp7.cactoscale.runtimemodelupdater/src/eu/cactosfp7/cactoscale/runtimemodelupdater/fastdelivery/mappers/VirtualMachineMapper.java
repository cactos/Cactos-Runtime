package eu.cactosfp7.cactoscale.runtimemodelupdater.fastdelivery.mappers;

import java.util.Map;

import eu.cactosfp7.cactoscale.runtimemodelupdater.fastdelivery.updater.LogicalLoadUpdater;
import eu.cactosfp7.cdosession.CactosCdoSession;

public class VirtualMachineMapper implements MetricsMapper {

	public static final String VM_UTILIZATION_KEY = "KvmTop";
	private LogicalLoadUpdater logicalLoadUpdater;
	
	public VirtualMachineMapper() {
		logicalLoadUpdater = new LogicalLoadUpdater();
	}

	@Override
	public void map(Map<String, String> metric, CactosCdoSession cactosCdoSession) {
		logicalLoadUpdater.initializeCdoModel(cactosCdoSession);

	}
}
