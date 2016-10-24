package eu.cactosfp7.cactoscale.runtimemodelupdater.fastdelivery.mappers;

import java.util.Map;

import org.apache.log4j.Logger;

import eu.cactosfp7.cactoscale.runtimemodelupdater.fastdelivery.updater.PhysicalLoadUpdater;
import eu.cactosfp7.cdosession.CactosCdoSession;

public class HardwareMapper implements MetricsMapper {

	public static final String HARDWARE_UTILIZATION_KEY = "HUtil";
	private PhysicalLoadUpdater physicalLoadUpdater;
	private final static Logger logger = Logger.getLogger(HardwareMapper.class);

	public HardwareMapper() {
		physicalLoadUpdater = new PhysicalLoadUpdater();
	}

	@Override
	public void map(Map<String, String> metric, CactosCdoSession cactosCdoSession) {
		physicalLoadUpdater.initializeCdoModel(cactosCdoSession);
		physicalLoadUpdater.setComputeNodeId(metric.get("csource"));

		physicalLoadUpdater.setPuLoad(Double.parseDouble(metric.get("cpu_usr")));

		//TODO: Mem size is in another adaptor. Wait for change of models
//		double mem_size = Double.parseDouble(metric.get("mem_size"));
//		double mem_free = Double.parseDouble(metric.get("mem_free"));
//		double mem_util = (mem_size - mem_free) / mem_size;
//		physicalLoadUpdater.setMemoryLoad(mem_util);

		physicalLoadUpdater.setInterconnectLoad(Double.parseDouble(metric.get("net_through")));

		physicalLoadUpdater.commit();
		logger.info("Cpu and network utilizations committed successfully on " + metric.get("csource") + "!!!!!");
	}

}
