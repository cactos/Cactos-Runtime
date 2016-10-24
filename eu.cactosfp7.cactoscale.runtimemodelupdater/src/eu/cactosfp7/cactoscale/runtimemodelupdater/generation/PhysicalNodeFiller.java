package eu.cactosfp7.cactoscale.runtimemodelupdater.generation;

import org.apache.log4j.Logger;

import eu.cactosfp7.cactoscale.runtimemodelupdater.SettingsInitializer;
import eu.cactosfp7.cactoscale.runtimemodelupdater.modelbuilder.PhysicalNodePlaceholder;
import eu.cactosfp7.cactoscale.runtimemodelupdater.modelbuilder.ResultAccessor;

public final class PhysicalNodeFiller {
	private final static Logger logger = Logger.getLogger(PhysicalNodeFiller.class);

	public static void fillPhysicalModelForNode(PhysicalNodePlaceholder nodePlaceholder, ResultAccessor readerForCNSnapshotTable) {
		String rowKey = nodePlaceholder.getNodeKey();

		String state = readerForCNSnapshotTable.getResult(rowKey, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.META_FAMILY), "state", String.class, "");
		if (state != null) {
			nodePlaceholder.fillNodeState(state);
		}

		int coreCount = readerForCNSnapshotTable.getResult(rowKey, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.HARDWARE_FAMILY), "cpu_cores", int.class, 0);
		double frequ = readerForCNSnapshotTable.getResult(rowKey, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.HARDWARE_FAMILY), "cpu_freq", double.class, 0.0);
		nodePlaceholder.fillCpuSpecification(frequ, coreCount);

		double cpu_usr = readerForCNSnapshotTable.getResult(rowKey, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.HARDWARE_UTIL_FAMILY), "cpu_usr", double.class, 0.0);
		nodePlaceholder.fillCpuUtilisation(cpu_usr);

		double mem_size = readerForCNSnapshotTable.getResult(rowKey, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.HARDWARE_FAMILY), "mem_size", double.class, 0.0);
		nodePlaceholder.fillMemorySpecification(mem_size);

		double mem_free = readerForCNSnapshotTable.getResult(rowKey, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.HARDWARE_UTIL_FAMILY), "mem_free", double.class, 0.0) / 1024;
		double mem_util = 0.0;
		if (mem_size > 0.0) {
			mem_util = (mem_size - mem_free) / mem_size;
		}
		nodePlaceholder.fillMemoryUtilisation(mem_util);

		String netw_speed = readerForCNSnapshotTable.getResult(rowKey, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.NETWORK_FAMILY), "netw_speed", String.class, "");
		try {
			netw_speed = refactorString(netw_speed, 4);
			nodePlaceholder.fillNetworkInterconnect(Double.parseDouble(netw_speed));
		} catch (Exception e) {
			logger.warn("The network is in unknown state.");
		}
		int net_through = readerForCNSnapshotTable.getResult(rowKey, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.NETWORK_UTIL_FAMILY), "net_through", int.class, 0);
		nodePlaceholder.fillInterconnectMeasurement(net_through);

		int filesystem_index = 0;
		String filesystem_mount;
		while (true) {
			filesystem_mount = readerForCNSnapshotTable.getResult(rowKey, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.FILESYSTEM_FAMILY), "mount." + filesystem_index, String.class,
					"");
			if (filesystem_mount.equals("/") || filesystem_mount.equals(null) || filesystem_mount.isEmpty()) {
				break;
			}
			filesystem_index++;
		}

		int storage_index = 0;
		boolean isStorageExists = true;
		if (filesystem_mount != null && !filesystem_mount.isEmpty()) {
			String readBandwidth = readerForCNSnapshotTable.getResult(rowKey, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.FILESYSTEM_FAMILY), "readbandmax." + filesystem_index,
					String.class, "");
			readBandwidth = refactorString(readBandwidth, 5);
			String writeBandwidth = readerForCNSnapshotTable.getResult(rowKey, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.FILESYSTEM_FAMILY), "writebandmax." + filesystem_index,
					String.class, "");
			writeBandwidth = refactorString(writeBandwidth, 5);
			if (nodePlaceholder.getStoragePlaceholder() != null)
				nodePlaceholder.getStoragePlaceholder().fillStorageSpecificationBandwidthAndDelay(Double.parseDouble(readBandwidth), Double.parseDouble(writeBandwidth));

			while (true) {
				String disk_mount = readerForCNSnapshotTable.getResult(rowKey, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.STORAGE_FAMILY), "disk_mount." + storage_index,
						String.class, "");
				if (disk_mount.isEmpty()) {
					isStorageExists = false;
					if (nodePlaceholder.getStoragePlaceholder() != null)
						nodePlaceholder.getStoragePlaceholder().fillStorageSpecificationDiskSize(Double.parseDouble("0.0"));
					break;
				}
				if (disk_mount.equals(filesystem_mount)) {
					break;
				}
				storage_index++;
			}
			if (isStorageExists) {

				String disk_size = readerForCNSnapshotTable.getResult(rowKey, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.STORAGE_FAMILY), "disk_size." + storage_index, String.class,
						"");
				disk_size = refactorString(disk_size, 1);
				if (nodePlaceholder.getStoragePlaceholder() != null)
					nodePlaceholder.getStoragePlaceholder().fillStorageSpecificationDiskSize(Double.parseDouble(disk_size));

				String disk_usage = readerForCNSnapshotTable.getResult(rowKey, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.FILESYSTEM_FAMILY), "used." + filesystem_index,
						String.class, "");
				Double disk_utilization = (Double.parseDouble(disk_usage) / 1024) / (Double.parseDouble(disk_size) * 1024);
				if (nodePlaceholder.getStoragePlaceholder() != null)
					nodePlaceholder.getStoragePlaceholder().fillStorageUtilisation(disk_utilization);

				String disk_name = readerForCNSnapshotTable.getResult(rowKey, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.STORAGE_FAMILY), "disk_name." + storage_index, String.class,
						"");
				int storage_util_index = 0;
				while (true) {
					String device_name = readerForCNSnapshotTable.getResult(rowKey, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.STORAGE_UTIL_FAMILY), "Device:." + storage_util_index,
							String.class, "");
					if (device_name.equals(disk_name)) {
						break;
					}
					storage_util_index++;
				}

				String readThroughput = readerForCNSnapshotTable.getResult(rowKey, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.STORAGE_UTIL_FAMILY), "kB_read/s." + storage_util_index,
						String.class, "");
				String writeThroughput = readerForCNSnapshotTable.getResult(rowKey, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.STORAGE_UTIL_FAMILY),
						"kB_wrtn/s." + storage_util_index, String.class, "");
				if (nodePlaceholder.getStoragePlaceholder() != null)
					nodePlaceholder.getStoragePlaceholder().fillStorageMeasurement(Double.parseDouble(readThroughput), Double.parseDouble(writeThroughput));
			}
		}
		/**
		 * FIXME: Add the PDU when data are sufficient.DON'T REMOVE!
		 */
		int powerIndex = 0;
		while (true) {
			double capacity = readerForCNSnapshotTable.getResult(rowKey, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.POWER_FAMILY), "capacity." + powerIndex, double.class, 0.0);
			String serialNumber = readerForCNSnapshotTable.getResult(rowKey, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.POWER_FAMILY), "serial." + powerIndex, String.class, "");
			if (serialNumber == null || serialNumber.isEmpty()) {
				break;
			}
			if (nodePlaceholder.getPowerPlaceholder() != null)
				nodePlaceholder.getPowerPlaceholder().addPowerDistributionUnit(serialNumber, capacity);
			powerIndex++;
		}
		String consumption = readerForCNSnapshotTable.getResult(rowKey, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.POWER_UTIL_FAMILY), "consumption", String.class, "");
		if (!consumption.equals("-"))
			if (nodePlaceholder.getPowerPlaceholder() != null)
				nodePlaceholder.getPowerPlaceholder().addPowerConsumingEntityMeasurement(Double.parseDouble(consumption));
	}

	private static String refactorString(String string, int offset) {
		return string.substring(0, string.length() - offset);
	}

}
