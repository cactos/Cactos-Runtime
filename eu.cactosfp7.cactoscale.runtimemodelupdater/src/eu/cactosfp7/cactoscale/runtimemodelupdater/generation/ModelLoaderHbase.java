package eu.cactosfp7.cactoscale.runtimemodelupdater.generation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.cdo.util.CommitException;
import org.eclipse.emf.cdo.util.ConcurrentAccessException;

import eu.cactosfp7.cactoscale.runtimemodelupdater.SettingsInitializer;
import eu.cactosfp7.cactoscale.runtimemodelupdater.modelbuilder.DataSourceModelBuilder;
import eu.cactosfp7.cactoscale.runtimemodelupdater.modelbuilder.HypervisorNodePlaceholder;
import eu.cactosfp7.cactoscale.runtimemodelupdater.modelbuilder.PhysicalNodePlaceholder;
import eu.cactosfp7.cactoscale.runtimemodelupdater.modelbuilder.RowKeyName;
import eu.cactosfp7.cdosession.CactosCdoSession;

public class ModelLoaderHbase {

	private final static Logger logger = Logger.getLogger(ModelLoaderHbase.class.getName());

	// Connect to Hbase...
	public final HbaseConnect hbaseConnection = HbaseConnect.getInstance();
	private CactosCdoSession cactosCdoSession;
	private CDOTransaction cdoTransaction;

	public void setCactosCdoSession(CactosCdoSession cactosCdoSession, CDOTransaction cdoTransaction) {
		this.cactosCdoSession = cactosCdoSession;
		this.cdoTransaction = cdoTransaction;
	}

	public ArrayList<Object> loadModelInstances() throws IOException, ConcurrentAccessException, CommitException {
		return this.loadModelInstances(false);
	}

	public ArrayList<Object> loadModelInstances(Boolean updateWithNewData) throws ConcurrentAccessException, IOException {

		DataSourceModelBuilder builder = generateBuilder();

		fillPhysicalModel(builder);
		fillVirtualModel(builder);

		ArrayList<Object> result = builder.buildResultSet();

		logger.info("Successfully loaded data from HBase");
		return result;
	}

	private final DataSourceModelBuilder generateBuilder() throws IOException {
		RowKeyScanner cnScanner = hbaseConnection.getRowKeyScannerForCNTable();
		cnScanner.prepareRowScan("hardware_util", "csource");
		cnScanner.prepareRowScan("storage", "csource");
		cnScanner.prepareRowScan("power", "csource");
		cnScanner.prepareRowScan("vms", "csource");
		cnScanner.scanAllColumns();
		List<RowKeyName> physical = cnScanner.lookup("hardware_util", "csource");
		List<RowKeyName> hypervisors = cnScanner.lookup("vms", "csource");
		List<RowKeyName> storage = cnScanner.lookup("storage", "csource");
		List<RowKeyName> power = cnScanner.lookup("power", "csource");

		cnScanner.close();
		assert physical.size() == hypervisors.size() : "physical hosts and hypervisors do not match";

		// create builder with set of physicalNodes and hypervisorNodes from
		// hbase
		DataSourceModelBuilder builder = new DataSourceModelBuilder();
		// List<String> architectureTypes = findArchitectureTypesModel(builder);
		// builder.setArchitectureTypes(physical);
		// builder.setHypervisorTypes(hypervisors);
		// builder.setRacks(physical);
//		for (RowKeyName powerKey: power) {
//			Map<String, String> results = (Map<String, String>) hbaseConnection.getPowerResults("serial", "capacity", powerKey.getName());
//		}

		builder.setPhysicalNodes(physical);
//		builder.setPowerDistributionUnits(power);
		if (storage != null)
			builder.setStorageForPhysicalNodes(storage);
		if (power != null)
			builder.setPowerForPhysicalNodes(power);
		builder.setHypervisorNodes(hypervisors);

		return builder;
	}

	// private void findArchitectureTypesModel(DataSourceModelBuilder builder)
	// throws IOException {
	// String[] rowKeys = builder.collectAllPhysicalNodeKeys();
	// MultiResultReader readerForCNSnapshotTable =
	// hbaseConnection.getMultiSystemCpu(SettingsInitializer.CNSNAPSHOT_TABLE,
	// rowKeys);
	// if (readerForCNSnapshotTable == null) {
	// logger.error("connection and/or data access to HBase failed. Cannot build
	// models");
	// } else {
	// try {
	// Iterator<ArchitectureTypePlaceholder> it =
	// builder.getArchitectureTypes().iterator();
	// while (it.hasNext()) {
	// ArchitectureTypePlaceholder node = it.next();
	// ArchitectureTypeFiller.fillArchitectureTypeForNode(node,
	// readerForCNSnapshotTable);
	// }
	//
	// } finally {
	// readerForCNSnapshotTable.closeTable();
	// }
	// }
	// }

	private final void fillPhysicalModel(DataSourceModelBuilder builder) throws IOException {
		String[] rowKeys = builder.collectAllPhysicalNodeKeys();

		MultiResultReader readerForCNSnapshotTable = hbaseConnection.getReaderForTable(SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.CNSNAPSHOT_TABLE), rowKeys);
		if (readerForCNSnapshotTable == null) {
			logger.error("connection and/or data access to HBase failed. Cannot build models");
		} else {
			try {
				Iterator<PhysicalNodePlaceholder> it = builder.physicalNodesIterator();
				while (it.hasNext()) {
					PhysicalNodePlaceholder node = it.next();
					PhysicalNodeFiller.fillPhysicalModelForNode(node, readerForCNSnapshotTable);
				}
			} finally {
				readerForCNSnapshotTable.closeTable();
			}
		}
	}

	private void fillVirtualModel(DataSourceModelBuilder builder) throws IOException {
		String[] rowKeys = builder.collectAllHypervisorNodeRowKeys();
		MultiResultReader readerForCNSnapshotTable = hbaseConnection.getReaderForTable(SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.CNSNAPSHOT_TABLE), rowKeys);
		if (readerForCNSnapshotTable == null) {
			logger.error("connection and/or data access to HBase failed. Cannot build models");
			return;
		}

		try {
			Iterator<HypervisorNodePlaceholder> it = builder.hypervisorNodesIterator();
			while (it.hasNext()) {
				HypervisorNodePlaceholder node = it.next();
				HypervisorNodeFiller.fillHypervisorModelForNode(node, readerForCNSnapshotTable, hbaseConnection, cactosCdoSession, cdoTransaction);
			}
		} finally {
			readerForCNSnapshotTable.closeTable();
		}
	}

}
