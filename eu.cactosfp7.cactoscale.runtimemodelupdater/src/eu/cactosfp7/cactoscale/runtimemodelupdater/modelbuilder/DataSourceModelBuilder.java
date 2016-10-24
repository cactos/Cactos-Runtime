package eu.cactosfp7.cactoscale.runtimemodelupdater.modelbuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import eu.cactosfp7.infrastructuremodels.load.logical.LogicalFactory;
import eu.cactosfp7.infrastructuremodels.load.logical.LogicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.physical.PhysicalFactory;
import eu.cactosfp7.infrastructuremodels.load.physical.PhysicalLoadModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.hypervisor.HypervisorFactory;
import eu.cactosfp7.infrastructuremodels.logicaldc.hypervisor.HypervisorRepository;
import eu.cactosfp7.infrastructuremodels.logicaldc.hypervisor.HypervisorType;
import eu.cactosfp7.infrastructuremodels.physicaldc.architecturetype.ArchitectureType;
import eu.cactosfp7.infrastructuremodels.physicaldc.architecturetype.ArchitectureTypeRepository;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.CoreFactory;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.PhysicalDCModel;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.Rack;

public final class DataSourceModelBuilder {

	protected final PhysicalDCModel physicalDCModel;
	protected final PhysicalLoadModel physicalLoadModel;
	protected final LogicalDCModel logicalDCModel;
	protected final LogicalLoadModel logicalLoadModel;
	protected final HypervisorRepository hypervisorRepository;
	protected final ArchitectureTypeRepository architectureTypeRepository;

	// private final List<ArchitectureTypePlaceholder>
	// architectureTypePlaceholders = new
	// ArrayList<ArchitectureTypePlaceholder>();
	// private final List<HypervisorTypePlaceholder> hypervisorPlaceHolders =
	// new ArrayList<HypervisorTypePlaceholder>();
	// private final List<RackPlaceholder> rackPlaceholders = new
	// ArrayList<RackPlaceholder>();

	private final List<PhysicalNodePlaceholder> physicalNodes = new ArrayList<PhysicalNodePlaceholder>();
	private final List<HypervisorNodePlaceholder> hypervisorNodes = new ArrayList<HypervisorNodePlaceholder>();

	private final HypervisorType hypervisorType;
	private final ArchitectureType architectureType;
	private final Rack rack;

	private RelationshipFactory<PhysicalNodePlaceholder> PHYSICAL = new RelationshipFactory<PhysicalNodePlaceholder>() {
		@Override
			PhysicalNodePlaceholder createNameRowRelationship(RowKeyName name) {
			// ArchitectureTypePlaceholder architectureTypePlaceholder =
			// findMatchingArchitectureType(name.getName());
			// RackPlaceholder rackPlaceholder =
			// findMatchingRack(name.getName());
			return new PhysicalNodePlaceholder(rack, name, architectureType, physicalDCModel, physicalLoadModel);
		}
	};

	private RelationshipFactory<HypervisorNodePlaceholder> VIRTUAL = new RelationshipFactory<HypervisorNodePlaceholder>() {
		@Override
			HypervisorNodePlaceholder createNameRowRelationship(RowKeyName name) {
			PhysicalNodePlaceholder node = findMatchingHypervisorNode(name.getName());
			// HypervisorTypePlaceholder hypervisorTypePlaceholder =
			// findMatchingHypervisorType(name.getName());
			return new HypervisorNodePlaceholder(node.getComputeNode(), name, hypervisorType, logicalDCModel, logicalLoadModel);
		}

	};

	public void setPhysicalNodes(List<RowKeyName> nodeNamesRows) {
		iterateRowNames(nodeNamesRows, physicalNodes, PHYSICAL);
	}

	public void setHypervisorNodes(List<RowKeyName> rowKeysHypervisors) {
		if (physicalNodes.size() != rowKeysHypervisors.size()) {
			throw new IllegalStateException("#hypervisor nodes(" + rowKeysHypervisors.size() + ") does not match #physical nodes(" + physicalNodes.size() + ")");
		}

		iterateRowNames(rowKeysHypervisors, hypervisorNodes, VIRTUAL);
	}

	// public void setArchitectureTypes(List<RowKeyName>
	// rowKeyArchitectureTypes) {
	// iterateRowNames(rowKeyArchitectureTypes, architectureTypePlaceholders,
	// ARCHITECTURETYPE);
	// }
	//
	// public void setHypervisorTypes(List<RowKeyName> rowKeyHypervisors) {
	// iterateRowNames(rowKeyHypervisors, hypervisorPlaceHolders,
	// HYPERVISORTYPE);
	// }
	//
	// public void setRacks(List<RowKeyName> rowKeyRacks) {
	// iterateRowNames(rowKeyRacks, rackPlaceholders, RACK);
	// }

	public void setStorageForPhysicalNodes(List<RowKeyName> storageRowKeys) {
		for (PhysicalNodePlaceholder node : physicalNodes) {
			RowKeyName storageRowKeyName = getRowKeyNameFromList(storageRowKeys, node.getNodeKey());
			if (storageRowKeyName != null) {
				node.initializeStoragePlaceholder(storageRowKeyName);
			}
		}
	}

	public void setPowerForPhysicalNodes(List<RowKeyName> powerRowKeys) {
		for (PhysicalNodePlaceholder node : physicalNodes) {
			RowKeyName powerRowKeyName = getRowKeyNameFromList(powerRowKeys, node.getNodeKey());
			if (powerRowKeyName != null) {
				node.initializePowerPlaceholder(powerRowKeyName);
			}
		}
	}

	public DataSourceModelBuilder() {

		// create repos
		physicalDCModel = CoreFactory.INSTANCE.createPhysicalDCModel();
		physicalLoadModel = PhysicalFactory.INSTANCE.createPhysicalLoadModel();
		logicalDCModel = eu.cactosfp7.infrastructuremodels.logicaldc.core.CoreFactory.INSTANCE.createLogicalDCModel();
		logicalLoadModel = LogicalFactory.INSTANCE.createLogicalLoadModel();
		hypervisorRepository = eu.cactosfp7.infrastructuremodels.logicaldc.hypervisor.HypervisorFactory.INSTANCE.createHypervisorRepository();
		architectureTypeRepository = eu.cactosfp7.infrastructuremodels.physicaldc.architecturetype.ArchitecturetypeFactory.INSTANCE.createArchitectureTypeRepository();

		// create static models
		hypervisorType = buildHypervisorType();
		architectureType = buildArchitectureType();
		rack = buildRack();
	}

	private final <T extends NodePlaceholder> void iterateRowNames(List<RowKeyName> toAdd, List<T> container, RelationshipFactory<T> factory) {
		for (RowKeyName name : toAdd) {
			T t = factory.createNameRowRelationship(name);
			container.add(t);
		}
	}

	private final ArchitectureType buildArchitectureType() {
		ArchitectureType _architectureType = eu.cactosfp7.infrastructuremodels.physicaldc.architecturetype.ArchitecturetypeFactory.INSTANCE.createArchitectureType();
		_architectureType.setArchitectureTypeRepository(architectureTypeRepository);
		_architectureType.setName("x86_64");
		return _architectureType;
	}

	private final HypervisorType buildHypervisorType() {
		HypervisorType _hypervisorType = HypervisorFactory.INSTANCE.createHypervisorType();
		_hypervisorType.setName("kvm"); // manual
		_hypervisorType.setHypervisorRepository(hypervisorRepository);
		return _hypervisorType;
	}

	private final Rack buildRack() {
		Rack _rack = CoreFactory.INSTANCE.createRack();
		_rack.setName("rack01");
		_rack.setPhysicalDCModel(physicalDCModel);
		return _rack;
	}

	private PhysicalNodePlaceholder findMatchingHypervisorNode(String name) {
		for (PhysicalNodePlaceholder node : physicalNodes) {
			if (node.getNodeKey().equals(name))
				return node;
		}
		throw new IllegalStateException("hypervisor node: " + name + " is not available in physical nodes list.");
	}

	// private RackPlaceholder findMatchingRack(String name) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// private ArchitectureTypePlaceholder findMatchingArchitectureType(String
	// name) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	//
	// private HypervisorTypePlaceholder findMatchingHypervisorType(String name)
	// {
	// // TODO Auto-generated method stub
	// return null;
	// }

	abstract class RelationshipFactory<T extends NodePlaceholder> {

		abstract T createNameRowRelationship(RowKeyName name);
	}

	public String[] collectAllPhysicalNodeKeys() {
		List<String> keys = collectNodeRowKeysFromList(physicalNodes);
		return keys.toArray(new String[keys.size()]);
	}

	public String[] collectAllPhysicalNodeKeysForIndex() {
		List<String> keys = collectNodeRowKeysFromListForIndex(physicalNodes);
		return keys.toArray(new String[keys.size()]);
	}

	public String[] collectAllStorageKeys() {
		List<String> keys = collectStorageNodeRowKeysFromList(physicalNodes);
		return keys.toArray(new String[keys.size()]);
	}

	public String[] collectAllStorageKeysFromIndex() {
		List<String> keys = collectStorageNodeRowKeysFromListForIndex(physicalNodes);
		return keys.toArray(new String[keys.size()]);
	}

	public String[] collectAllPowerKeys() {
		List<String> keys = collectPowerNodeRowKeysFromList(physicalNodes);
		return keys.toArray(new String[keys.size()]);
	}

	public String[] collectAllHypervisorNodeRowKeys() {
		List<String> keys = collectNodeRowKeysFromList(hypervisorNodes);
		return keys.toArray(new String[keys.size()]);
	}

	private List<String> collectNodeRowKeysFromList(List<? extends NodePlaceholder> list) {
		final int a = list.size();
		List<String> keys = new ArrayList<String>(a);
		for (NodePlaceholder node : list) {
			keys.add(node.getNodeKey());
		}
		return keys;
	}

	private List<String> collectNodeRowKeysFromListForIndex(List<? extends PhysicalNodePlaceholder> list) {
		final int a = list.size();
		List<String> keys = new ArrayList<String>(a);
		for (PhysicalNodePlaceholder node : list) {
			keys.add(node.getNodeKey());
		}
		return keys;
	}

	private List<String> collectStorageNodeRowKeysFromList(List<? extends PhysicalNodePlaceholder> list) {
		final int a = list.size();
		List<String> keys = new ArrayList<String>(a);
		for (PhysicalNodePlaceholder node : list) {
			if (node.getStoragePlaceholder() != null)
				keys.add(node.getStoragePlaceholder().getNodeKey());
		}
		return keys;
	}

	private List<String> collectStorageNodeRowKeysFromListForIndex(List<? extends PhysicalNodePlaceholder> list) {
		final int a = list.size();
		List<String> keys = new ArrayList<String>(a);
		for (PhysicalNodePlaceholder node : list) {
			if (node.getStoragePlaceholder() != null)
				keys.add(node.getStoragePlaceholder().getNodeKey());
		}
		return keys;
	}

	private List<String> collectPowerNodeRowKeysFromList(List<? extends PhysicalNodePlaceholder> list) {
		final int a = list.size();
		List<String> keys = new ArrayList<String>(a);
		for (PhysicalNodePlaceholder node : list) {
			if (node.getPowerPlaceholder() != null)
				keys.add(node.getPowerPlaceholder().getNodeKey());
		}
		return keys;
	}

	public Iterator<PhysicalNodePlaceholder> physicalNodesIterator() {
		return physicalNodes.iterator();
	}

	public Iterator<HypervisorNodePlaceholder> hypervisorNodesIterator() {
		return hypervisorNodes.iterator();
	}

	public void collectAllVms(List<VirtualMachinePlaceholder> allVms) {
		for (HypervisorNodePlaceholder hv : hypervisorNodes) {
			hv.collectVms(allVms);
		}
	}

	public ArrayList<Object> buildResultSet() {
		ArrayList<Object> result = new ArrayList<Object>();
		result.add(physicalDCModel);
		result.add(physicalLoadModel);
		result.add(logicalDCModel);
		result.add(logicalLoadModel);
		result.add(hypervisorRepository);
		result.add(architectureTypeRepository);
		return result;
	}

	private RowKeyName getRowKeyNameFromList(List<RowKeyName> list, String name) {
		for (RowKeyName keyName : list) {
			if (keyName.getName().equals(name)) {
				return keyName;
			}
		}
		return null;
	}

}
