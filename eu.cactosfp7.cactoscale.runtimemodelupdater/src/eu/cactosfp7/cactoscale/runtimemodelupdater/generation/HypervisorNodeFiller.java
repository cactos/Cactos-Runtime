package eu.cactosfp7.cactoscale.runtimemodelupdater.generation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.eclipse.emf.cdo.transaction.CDOTransaction;

import eu.cactosfp7.cactoscale.runtimemodelupdater.ApplicationModelLoadBuilderClient;
import eu.cactosfp7.cactoscale.runtimemodelupdater.SettingsInitializer;
import eu.cactosfp7.cactoscale.runtimemodelupdater.modelbuilder.HypervisorNodePlaceholder;
import eu.cactosfp7.cactoscale.runtimemodelupdater.modelbuilder.VirtualMachinePlaceholder;
import eu.cactosfp7.cdosession.CactosCdoSession;
import eu.cactosfp7.infrastructuremodels.builder.applicationmodelload.ApplicationModelLoadBuilder.LoadBalancerMetric;

public class HypervisorNodeFiller {
	private final static Logger logger = Logger.getLogger(HypervisorNodeFiller.class);

	public static void fillHypervisorModelForNode(HypervisorNodePlaceholder nodePlaceholder,
		MultiResultReader readerForCNSnapshotTable,
		HbaseConnect hbaseConnection,
		CactosCdoSession cactosCdoSession,
		CDOTransaction cdoTransaction) throws IOException {
		String rowKey = nodePlaceholder.getNodeKey();
		List<String> vmNames = new ArrayList<String>();
		List<String> uuids = new ArrayList<String>();
		List<String> vmImageUUIDs = new ArrayList<String>();
		// List<String> states = new ArrayList<String>();
		int index = 0;
		while (true) {
			String vmName = readerForCNSnapshotTable.getResult(rowKey, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.VMS_FAMILY), "vm_name." + index, String.class, "");
			String uuid = readerForCNSnapshotTable.getResult(rowKey, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.VMS_FAMILY), "vm_uuid." + index, String.class, "");
			String vmImageUUID = readerForCNSnapshotTable.getResult(rowKey, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.VMS_FAMILY), "vm_image_uuid." + index, String.class, "");
			// String state = readerForCNSnapshotTable.getResult(rowKey,
			// SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.VMS_FAMILY),
			// "vm_state." + index, String.class, "");

			if (vmName == null || vmName.isEmpty())
				break;
			index++;
			vmNames.add(vmName);
			vmImageUUIDs.add(vmImageUUID);
			uuids.add(uuid);
			// states.add(state);
		}

		MultiResultReader readerForVMSnapshotTable = hbaseConnection.getReaderForTable(SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.VMSNAPSHOT_TABLE),
				uuids.toArray(new String[uuids.size()]));
		try {
			for (int i = 0; i < index; i++) {
				createVirtualMachinePlaceholder(nodePlaceholder, readerForVMSnapshotTable, vmNames.get(i), uuids.get(i), vmImageUUIDs.get(i), cactosCdoSession, cdoTransaction);
			}
		} finally {
			readerForVMSnapshotTable.closeTable();
		}
	}

	private static void createVirtualMachinePlaceholder(HypervisorNodePlaceholder nodePlaceholder,
		MultiResultReader readerForVMSnapshotTable,
		String vmName,
		String uuid,
		String vmImageUUID,
		CactosCdoSession cactosCdoSession,
		CDOTransaction cdoTransaction) {
		String state = readerForVMSnapshotTable.getResult(uuid, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.META_FAMILY), "vm_state", String.class, "");
		if (!state.isEmpty() || state != null) {
			VirtualMachinePlaceholder vm = nodePlaceholder.createVirtualMachinePlaceholder(uuid, vmImageUUID, nodePlaceholder.getNodeKey());
			String isDeleted = readerForVMSnapshotTable.getResult(uuid, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.META_FAMILY), "isDeleted", String.class, "false");
			String applicationType = readerForVMSnapshotTable.getResult(uuid, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.META_FAMILY), "applicationType", String.class, "");
			String applicationTypeInstance = readerForVMSnapshotTable.getResult(uuid, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.META_FAMILY), "applicationTypeInstance", String.class, "");
			String applicationComponent = readerForVMSnapshotTable.getResult(uuid, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.META_FAMILY), "applicationComponent", String.class, "");
			String applicationComponentInstance = readerForVMSnapshotTable.getResult(uuid, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.META_FAMILY), "applicationComponentInstance", String.class, "");
			vm.fillVirtualMachineInputParameters(vmName, isDeleted, applicationType, applicationTypeInstance, applicationComponent, applicationComponentInstance);
			vm.fillVirtualMachineState(state);
			int vCores = readerForVMSnapshotTable.getResult(uuid, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.HARDWARE_FAMILY), "CpuCS", int.class, 0);
			String cpuUtil = readerForVMSnapshotTable.getResult(uuid, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.HARDWARE_FAMILY), "CpuVM", String.class, "");
			if (cpuUtil != null && !cpuUtil.isEmpty()) {
				cpuUtil = refactorString(cpuUtil, 1);
				vm.fillVCpuUtil(Double.parseDouble(cpuUtil));
			}
			vm.fillNumberOfCoresForVCpu(vCores);

			String storageSize = readerForVMSnapshotTable.getResult(uuid, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.STORAGE_FAMILY), "disk-total", String.class, "");
			if (storageSize != null && !storageSize.isEmpty()) {
				storageSize = refactorString(storageSize, 2);
				vm.fillVMImageInstanceStorageSize(Integer.parseInt(storageSize));
			}
			String ram_total = readerForVMSnapshotTable.getResult(uuid, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.HARDWARE_FAMILY), "ram-total", String.class, "");
			if (ram_total != null && !ram_total.isEmpty()) {
				ram_total = refactorString(ram_total, 2);
				vm.fillProvisionedMemory(Integer.parseInt(ram_total));
				String ram_used = readerForVMSnapshotTable.getResult(uuid, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.HARDWARE_FAMILY), "ram-used", String.class, "");
				if (ram_used != null && !ram_used.isEmpty()) {
					ram_used = refactorString(ram_used, 2);
					vm.fillMemoryUtil(Double.parseDouble(ram_total), Double.parseDouble(ram_used));
				}
			}
			NavigableMap<byte[], byte[]> appMeasurements = readerForVMSnapshotTable.getResultMap(uuid, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.APP_FAMILY));
			if (!SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.ENABLE_FASTDELIVERY).equals("true")) {
				updateLoadBalancerMetric(uuid, appMeasurements, cactosCdoSession, cdoTransaction);
			}
			nodePlaceholder.addVm(vm);
		}
	}

	public static void updateLoadBalancerMetric(String uuid, Map<byte[], byte[]> appMeasurements, CactosCdoSession cactosCdoSession, CDOTransaction cdoTransaction) {
		Map<String, Map<LoadBalancerMetric, Double>> measurementsForLoadBalancer = new HashMap<String, Map<LoadBalancerMetric, Double>>();
		for (byte[] key : appMeasurements.keySet()) {
			String keyString = Bytes.toString(key);
			String[] keyStringParts = keyString.split("-");
			if (keyStringParts.length < 2) {
				continue;
			}
			try{
			String loadBalancerGroup = keyStringParts[1];
			if (!measurementsForLoadBalancer.containsKey(loadBalancerGroup)) {
				Map<LoadBalancerMetric, Double> measurements = createMeasurementsMapForLoadBalancerGroup();
				if (getMetricFromString(keyStringParts[2]) != null) {
					measurements.put(getMetricFromString(keyStringParts[2]), Double.parseDouble(Bytes.toString(appMeasurements.get(key))));
					measurementsForLoadBalancer.put(loadBalancerGroup, measurements);
				}
			} else {
				Map<LoadBalancerMetric, Double> measurements = measurementsForLoadBalancer.get(loadBalancerGroup);
				if (getMetricFromString(keyStringParts[2]) != null) {
					measurements.put(getMetricFromString(keyStringParts[2]), Double.parseDouble(Bytes.toString(appMeasurements.get(key))));
				}
			}
			}catch(Exception e){
				logger.debug("Cannot handle the application monitoring data for VM:" + uuid);
			}
		}
		logger.debug("Found load balancer data(" + measurementsForLoadBalancer.size() + ") for VM:" + uuid);

		if (ApplicationModelLoadBuilderClient.INSTANCE == null || ApplicationModelLoadBuilderClient.INSTANCE.getService() == null) {
			logger.warn("no ApplicationModelLoadBuilder service available!");
		} else {
			for (String loadbalancerGroup : measurementsForLoadBalancer.keySet()) {
				ApplicationModelLoadBuilderClient.INSTANCE.getService().updateLoadBalancerMetric(cactosCdoSession, cdoTransaction, loadbalancerGroup, uuid,
						measurementsForLoadBalancer.get(loadbalancerGroup));
			}
		}
	}

	private static Map<LoadBalancerMetric, Double> createMeasurementsMapForLoadBalancerGroup() {
		return new HashMap<LoadBalancerMetric, Double>();
	}

	private static LoadBalancerMetric getMetricFromString(String metric) {
		switch (metric) {
		case "SESSION_PER_SECOND":
			return LoadBalancerMetric.SESSION_PER_SECOND;
		case "TWO_XX_PER_SECOND":
			return LoadBalancerMetric.TWO_XX_PER_SECOND;
		}
		return null;
	}

	private static String refactorString(String string, int offset) {
		try {
			return string.substring(0, string.length() - offset);
		} catch (Exception e) {
			logger.warn("refactorString", e);
			return "";
		}
	}

}
