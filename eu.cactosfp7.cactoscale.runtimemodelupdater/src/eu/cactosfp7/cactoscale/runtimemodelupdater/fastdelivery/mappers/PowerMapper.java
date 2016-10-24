package eu.cactosfp7.cactoscale.runtimemodelupdater.fastdelivery.mappers;

import java.util.Map;

import org.apache.log4j.Logger;

import eu.cactosfp7.cactoscale.runtimemodelupdater.fastdelivery.updater.PhysicalLoadUpdater;
import eu.cactosfp7.cdosession.CactosCdoSession;

public class PowerMapper implements MetricsMapper {

	public static final String POWER_UTILIZATION_KEY = "HPowerUtil";
	private PhysicalLoadUpdater physicalLoadUpdater;
	private final static Logger logger = Logger.getLogger(PowerMapper.class);

	public PowerMapper() {
		physicalLoadUpdater = new PhysicalLoadUpdater();
	}

	@Override
	public void map(Map<String, String> metric, CactosCdoSession cactosCdoSession) {
		physicalLoadUpdater.initializeCdoModel(cactosCdoSession);
		physicalLoadUpdater.setComputeNodeId(metric.get("csource"));

		physicalLoadUpdater.setPowerLoad(Double.parseDouble(metric.get("consumption")));

		physicalLoadUpdater.commit();
		logger.info("Power utilizations committed successfully on " + metric.get("csource") + "!!!!!");

	}

}
