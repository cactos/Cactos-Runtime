package eu.cactosfp7.cactoscale.runtimemodelupdater.modelbuilder;

import java.util.ArrayList;
import java.util.List;

import eu.cactosfp7.infrastructuremodels.load.logical.LogicalLoadModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.Hypervisor;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.hypervisor.HypervisorType;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.ComputeNode;

public final class HypervisorNodePlaceholder implements NodePlaceholder {

	// creation attributes
	private final String name;

	// repo attributes
	private final LogicalDCModel logicalDCModel;
	private final LogicalLoadModel logicalLoadModel;

	// model attributes
	private final ComputeNode node;
	private final HypervisorType hypervisorType;
	private final Hypervisor hypervisor;
	private final List<VirtualMachinePlaceholder> vms = new ArrayList<VirtualMachinePlaceholder>();

	public HypervisorNodePlaceholder(ComputeNode _node, RowKeyName _rowkeyname, HypervisorType _hypervisorType, LogicalDCModel _logicalDCModel, LogicalLoadModel _logicalLoadModel) {

		node = _node;
		name = _rowkeyname.getName();
		hypervisorType = _hypervisorType;
		logicalDCModel = _logicalDCModel;
		logicalLoadModel = _logicalLoadModel;

		hypervisor = createHypervisor();
	}

	private Hypervisor createHypervisor() {
		Hypervisor _hypervisor = eu.cactosfp7.infrastructuremodels.logicaldc.core.CoreFactory.INSTANCE.createHypervisor();
		// _hypervisor.setId(name);
		_hypervisor.setLogicalDCModel(logicalDCModel);
		_hypervisor.setNode(node);
		_hypervisor.setHypervisorType(hypervisorType);
		node.setHypervisor(_hypervisor);
		return _hypervisor;
	}

	void collectVms(List<VirtualMachinePlaceholder> allVms) {
		for (VirtualMachinePlaceholder vm : vms) {
			allVms.add(vm);
		}
	}

	public void addVm(VirtualMachinePlaceholder vm) {
		vms.add(vm);
	}

	@Override
	public String getNodeKey() {
		return name;
	}

	public LogicalLoadModel getLogicalLoadModel() {
		return logicalLoadModel;
	}

	public LogicalDCModel getLogicalDCModel() {
		return logicalDCModel;
	}

	public VirtualMachinePlaceholder createVirtualMachinePlaceholder(String vmUUID, String vmImageUUID, String nodeKey) {
		return new VirtualMachinePlaceholder(vmUUID, vmImageUUID, name, logicalLoadModel, logicalDCModel, hypervisor);
	}
}