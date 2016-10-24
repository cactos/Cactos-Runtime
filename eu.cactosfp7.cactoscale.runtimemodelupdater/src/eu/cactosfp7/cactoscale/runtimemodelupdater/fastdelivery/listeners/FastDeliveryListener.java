package eu.cactosfp7.cactoscale.runtimemodelupdater.fastdelivery.listeners;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.cactosfp7.cactoscale.runtimemodelupdater.fastdelivery.mappers.DiskMapper;
import eu.cactosfp7.cactoscale.runtimemodelupdater.fastdelivery.mappers.HardwareMapper;
import eu.cactosfp7.cactoscale.runtimemodelupdater.fastdelivery.mappers.MetricsMapper;
import eu.cactosfp7.cactoscale.runtimemodelupdater.fastdelivery.mappers.PowerMapper;
import eu.cactosfp7.cactoscale.runtimemodelupdater.fastdelivery.mappers.VirtualMachineMapper;
import eu.cactosfp7.cactoscale.runtimemodelupdater.fastdelivery.mappers.VisorMapper;
import eu.cactosfp7.cdosession.CactosCdoSession;
import eu.cactosfp7.cdosession.settings.CactosUser;
import eu.cactosfp7.cdosessionclient.CdoSessionClient;

public class FastDeliveryListener implements MetricListener {
	private final Map<String, MetricsMapper> mappers;
	private final static Logger logger = Logger.getLogger(FastDeliveryListener.class);

	public FastDeliveryListener() {
		mappers = initMap();
	}

	private Map<String, MetricsMapper> initMap() {
		Map<String, MetricsMapper> mappers = new HashMap<String, MetricsMapper>();
		mappers.put(HardwareMapper.HARDWARE_UTILIZATION_KEY, new HardwareMapper());
		mappers.put(DiskMapper.DISK_UTILIZATION_KEY, new DiskMapper());
		mappers.put(PowerMapper.POWER_UTILIZATION_KEY, new PowerMapper());
		mappers.put(VirtualMachineMapper.VM_UTILIZATION_KEY, new VirtualMachineMapper());
		mappers.put(VisorMapper.VISOR_KEY, new VisorMapper());
		return mappers;
	}

	@Override
	public void handleMetric(Metric e) {
		Map<String, String> metricValues = e.getValues();

		String mapperKey = metricValues.get("dataType");
		if (mapperKey == null) {
			logger.info("dataType not found in metric values");
			return;
		}

		final CactosCdoSession cactosCdoSession = CdoSessionClient.INSTANCE.getService().getCactosCdoSession(CactosUser.CACTOSCALE);

		MetricsMapper mapper = mappers.get(mapperKey);
		if (mapper == null) {
//			logger.warn("did not find a configured mapper for key '" + mapperKey + "'");
			return;
		}

		mapper.map(metricValues, cactosCdoSession);
	}

}
