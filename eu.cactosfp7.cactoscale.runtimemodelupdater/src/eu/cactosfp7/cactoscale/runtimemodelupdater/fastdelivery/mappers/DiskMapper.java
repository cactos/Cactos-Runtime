package eu.cactosfp7.cactoscale.runtimemodelupdater.fastdelivery.mappers;

import java.util.Map;

import org.apache.log4j.Logger;

import eu.cactosfp7.cactoscale.runtimemodelupdater.fastdelivery.updater.PhysicalLoadUpdater;
import eu.cactosfp7.cdosession.CactosCdoSession;

public class DiskMapper implements MetricsMapper {

	public static final String DISK_UTILIZATION_KEY = "HDiskUtil";
	private PhysicalLoadUpdater physicalLoadUpdater;
	private final static Logger logger = Logger.getLogger(DiskMapper.class);

	public DiskMapper() {
		physicalLoadUpdater = new PhysicalLoadUpdater();
	}

	@Override
	public void map(Map<String, String> metric, CactosCdoSession cactosCdoSession) {
		physicalLoadUpdater.initializeCdoModel(cactosCdoSession);
		physicalLoadUpdater.setComputeNodeId(metric.get("csource"));

		int disk_index = 0;
		String disk_mount;
		while (true) {
			disk_mount = metric.get("Mountpoint." + disk_index);
			if (disk_mount.equals("/") || disk_mount.equals(null) || disk_mount.isEmpty()) {
				break;
			}
			disk_index++;
		}
		String readThrouputString = metric.get("kB_read/s." + disk_index);
		String writeThrouputString = metric.get("kB_wrtn/s." + disk_index);
		if (readThrouputString != null && writeThrouputString != null) {
			double readThroughput = Double.parseDouble(readThrouputString);
			double writeThroughput = Double.parseDouble(writeThrouputString);

			physicalLoadUpdater.setStorageThroughput(readThroughput, writeThroughput);

			physicalLoadUpdater.commit();
			logger.info("Disk utilizations committed successfully on " + metric.get("csource") + "!!!!!");
		}
	}
}
