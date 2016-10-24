package eu.cactosfp7.cactoscale.runtimemodelupdater.fastdelivery.mappers;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.cactosfp7.cactoscale.runtimemodelupdater.fastdelivery.updater.LogicalLoadUpdater;
import eu.cactosfp7.cdosession.CactosCdoSession;

public class VisorMapper implements MetricsMapper {

	public static final String VISOR_KEY = "Visor";
	private LogicalLoadUpdater logicalLoadUpdater;
	private final static Logger logger = Logger.getLogger(VisorMapper.class);

	public VisorMapper() {
		logicalLoadUpdater = new LogicalLoadUpdater();
	}

	@Override
	public void map(Map<String, String> metric, CactosCdoSession cactosCdoSession) {
		logicalLoadUpdater.initializeCdoModel(cactosCdoSession);
		Map<byte[], byte[]> appMeasurements = new HashMap<byte[], byte[]>();
		for (String key : metric.keySet()) {
			String value = metric.get(key);
			if (!value.isEmpty() && value != null) {
				appMeasurements.put(key.getBytes(), value.getBytes());
			}
		}
		logicalLoadUpdater.updateLoadBalancerMetric(metric.get("VMID"), appMeasurements);
		logger.info("Load Balancer metrics committed successfully on " + metric.get("VMID") + "!!!!!");
	}
}