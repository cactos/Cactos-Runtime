package eu.cactosfp7.cactoscale.runtimemodelupdater.modelbuilder;

import javax.measure.quantity.DataRate;
import javax.measure.quantity.Dimensionless;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import eu.cactosfp7.cactoscale.runtimemodelupdater.SettingsInitializer;
import eu.cactosfp7.infrastructuremodels.load.physical.InterconnectMeasurement;
import eu.cactosfp7.infrastructuremodels.load.physical.MemoryMeasurement;
import eu.cactosfp7.infrastructuremodels.load.physical.PhysicalFactory;
import eu.cactosfp7.infrastructuremodels.load.physical.PhysicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.physical.PuMeasurement;
import eu.cactosfp7.infrastructuremodels.load.physical.Utilization;
import eu.cactosfp7.infrastructuremodels.physicaldc.architecturetype.ArchitectureType;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.ComputeNode;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.CoreFactory;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.MemorySpecification;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.NetworkInterconnect;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.NodeState;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.PhysicalDCModel;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.ProcessingUnitSpecification;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.Rack;
import eu.cactosfp7.infrastructuremodels.physicaldc.util.Bandwidth;
import eu.cactosfp7.infrastructuremodels.physicaldc.util.UtilFactory;

public final class PhysicalNodePlaceholder implements NodePlaceholder {

	private final String name;

	private final ComputeNode computeNode;
	private final ProcessingUnitSpecification cpu;
	private final PuMeasurement cpuMeasurement;
	private final Utilization cpuUtilisation;

	private final MemorySpecification memory;
	private final MemoryMeasurement memoryMeasurement;
	private final Utilization memoryUtilisation;

	private final NetworkInterconnect networkInterconnect;
	private final InterconnectMeasurement interConnectMeasurement;

	private final Rack rack;
	private final ArchitectureType architectureType;

	private final PhysicalDCModel physicalDCModel;
	private final PhysicalLoadModel physicalLoadModel;

	private StoragePlaceholder storagePlaceholder;
	private PowerPlaceholder powerPlaceholder;

	public PhysicalNodePlaceholder(Rack _rack, RowKeyName _rowkeyname, ArchitectureType _architectureType, PhysicalDCModel _physicalDCModel, PhysicalLoadModel _physicalLoadModel) {
		name = _rowkeyname.getName();

		rack = _rack;
		architectureType = _architectureType;

		physicalDCModel = _physicalDCModel;
		physicalLoadModel = _physicalLoadModel;
		networkInterconnect = createNetworkInterconnect();
		computeNode = createComputeNode();

		cpu = createCpu();
		cpuMeasurement = createCpuMeasurement();
		cpuUtilisation = createCpuUtilization();

		memory = createMemory();
		memoryMeasurement = createMemoryMeasurement();
		memoryUtilisation = createMemoryUtilization();

		interConnectMeasurement = createInterConnectMeasurement();
	}

	private ComputeNode createComputeNode() {
		ComputeNode _computeNode = CoreFactory.INSTANCE.createComputeNode();
		_computeNode.setRack(rack);
		_computeNode.setName(name);
		_computeNode.getNetworkInterconnects().add(networkInterconnect);
		return _computeNode;
	}

	public ComputeNode getComputeNode() {
		return computeNode;
	}
	
	public void fillNodeState(String state) {
		NodeState nodeState = null;
		switch (state) {
		case "running":
			nodeState = NodeState.RUNNING;
			break;
		case "failure":// FCO SHUTDOWN
			nodeState = NodeState.FAILURE;
			break;
		case "off":
			nodeState = NodeState.OFF;
			break;
		default:
			nodeState = NodeState.UNKNOWN;
		}
		computeNode.setState(nodeState);
	}

	public ProcessingUnitSpecification createCpu() {
		ProcessingUnitSpecification _cpu = CoreFactory.INSTANCE.createProcessingUnitSpecification();
		_cpu.setNode(computeNode);
		_cpu.getPowerProvidingEntities().add(computeNode);
		_cpu.setName(name);
		_cpu.setSupportsTurboMode(true);
		_cpu.setArchitectureType(architectureType);
		return _cpu;
	}

	public PuMeasurement createCpuMeasurement() {
		PuMeasurement _cpuMeasurement = PhysicalFactory.INSTANCE.createPuMeasurement();
		_cpuMeasurement.setPhysicalLoadModel(physicalLoadModel);
		_cpuMeasurement.setObservedPu(cpu);
		return _cpuMeasurement;

	}

	public Utilization createCpuUtilization() {
		Utilization _cpuUtilization = PhysicalFactory.INSTANCE.createUtilization();
		_cpuUtilization.setPuMeasurement(cpuMeasurement);
		return _cpuUtilization;
	}

	private MemorySpecification createMemory() {
		MemorySpecification _memory = CoreFactory.INSTANCE.createMemorySpecification();
		_memory.getPowerProvidingEntities().add(computeNode);
		_memory.setNode(computeNode);
		_memory.setName(name);
		return _memory;
	}

	private MemoryMeasurement createMemoryMeasurement() {
		MemoryMeasurement _memoryMeasurement = PhysicalFactory.INSTANCE.createMemoryMeasurement();
		_memoryMeasurement.setPhysicalLoadModel(physicalLoadModel);
		_memoryMeasurement.setObservedMemory(memory);
		return _memoryMeasurement;
	}

	private Utilization createMemoryUtilization() {
		Utilization _memoryUtilization = PhysicalFactory.INSTANCE.createUtilization();
		_memoryUtilization.setMemoryMeasurement(memoryMeasurement);
		return _memoryUtilization;
	}

	private NetworkInterconnect createNetworkInterconnect() {
		NetworkInterconnect _networkInterconnect = CoreFactory.INSTANCE.createNetworkInterconnect();
		_networkInterconnect.setName(name);
		_networkInterconnect.setPhysicalDCModel(physicalDCModel);
		return _networkInterconnect;
	}

	private InterconnectMeasurement createInterConnectMeasurement() {
		InterconnectMeasurement _interConnectMeasurement = PhysicalFactory.INSTANCE.createInterconnectMeasurement();
		_interConnectMeasurement.setPhysicalLoadModel(physicalLoadModel);
		_interConnectMeasurement.setObservedInterconnect(networkInterconnect);
		return _interConnectMeasurement;
	}

	public void fillCpuSpecification(double frequ, int cores) {
		cpu.setFrequency(Amount.valueOf(frequ, SI.MEGA(SI.HERTZ)));
		cpu.setNumberOfCores(cores);
	}

	public void fillCpuUtilisation(double cpu_usr) {
		if (!SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.ENABLE_FASTDELIVERY).equals("true")) {
			cpuUtilisation.setValue(Amount.valueOf(cpu_usr / 100, Dimensionless.UNIT));
		}
	}

	public void fillMemorySpecification(double mem_size) {
		memory.setSize(Amount.valueOf(mem_size, SI.MEGA(NonSI.BYTE)));
	}

	public void fillMemoryUtilisation(double mem_util) {
		// FIXME: When in models we will store the used memory, fast delivery
		// will update the util also.
		memoryUtilisation.setValue(Amount.valueOf(mem_util, Dimensionless.UNIT));
	}

	public void fillNetworkInterconnect(double bandwidthValue) {
		Bandwidth bandwidth = UtilFactory.INSTANCE.createBandwidth();
		bandwidth.setValue(Amount.valueOf(bandwidthValue * 1024 * 1024 * 8, DataRate.UNIT));
		networkInterconnect.setBandwidth(bandwidth);
	}

	public void fillInterconnectMeasurement(double net_through) {
		if (!SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.ENABLE_FASTDELIVERY).equals("true")) {
			interConnectMeasurement.setMeasuredThroughput(Amount.valueOf(net_through * 8, DataRate.UNIT));
		}
	}

	public void initializeStoragePlaceholder(RowKeyName storageRowKeyName) {
		storagePlaceholder = new StoragePlaceholder(storageRowKeyName, computeNode, physicalLoadModel);
	}

	public void initializePowerPlaceholder(RowKeyName powerRowKeyName) {
		powerPlaceholder = new PowerPlaceholder(powerRowKeyName, computeNode, rack, physicalDCModel, physicalLoadModel);
	}

	public StoragePlaceholder getStoragePlaceholder() {
		return storagePlaceholder;
	}

	public PowerPlaceholder getPowerPlaceholder() {
		return powerPlaceholder;
	}

	@Override
	public String getNodeKey() {
		return name;
	}


}