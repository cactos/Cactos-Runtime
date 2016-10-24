package eu.cactosfp7.cactoscale.runtimemodelupdater.modelbuilder;

import javax.measure.quantity.Dimensionless;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import eu.cactosfp7.cactoscale.runtimemodelupdater.SettingsInitializer;
import eu.cactosfp7.infrastructuremodels.load.logical.LogicalFactory;
import eu.cactosfp7.infrastructuremodels.load.logical.LogicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.logical.VirtualMemoryMeasurement;
import eu.cactosfp7.infrastructuremodels.load.logical.VirtualProcessingUnitsMeasurement;
import eu.cactosfp7.infrastructuremodels.load.physical.PhysicalFactory;
import eu.cactosfp7.infrastructuremodels.load.physical.Utilization;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.CoreFactory;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.Hypervisor;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VMImage;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VMImageInstance;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VM_State;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualDisk;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMemory;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualProcessingUnit;
import eu.cactosfp7.infrastructuremodels.physicaldc.architecturetype.ArchitectureType;

public class VirtualMachinePlaceholder implements NodePlaceholder {

	private final LogicalLoadModel logicalLoadModel;
	private final LogicalDCModel logicalDCModel;
	private final Hypervisor hypervisor;

	@SuppressWarnings("unused")
	private final VMImage vmImage;
	@SuppressWarnings("unused")
	private final VirtualDisk virtualDisk;
	private final VMImageInstance vmImageInstance;
	private final VirtualMachine vm;
	// private final PuAffinity puAffinitiy;
	private final VirtualMemory vMemory;
	private final VirtualMemoryMeasurement vMemoryMeasurement;
	private final Utilization vMemoryUtilization;

	private final VirtualProcessingUnit vCpu;
	private final VirtualProcessingUnitsMeasurement vCpuMeasurement;
	private final Utilization vCpuUtilization;

	private final String vmUUID;
	private final String vmImageUUID;

	public VirtualMachinePlaceholder(String _vmUUID, String _vmImageUUID, String _hypervisorName, LogicalLoadModel _logicalLoadModel, LogicalDCModel _logicalDCModel, Hypervisor _hypervisor) {
		vmUUID = _vmUUID;
		vmImageUUID = _vmImageUUID;
		logicalLoadModel = _logicalLoadModel;
		logicalDCModel = _logicalDCModel;
		hypervisor = _hypervisor;

		vmImage = createVMImage();

		virtualDisk = createVirtualDisk();

		vmImageInstance = createVmImageInstance();
		vm = createVirtualMachine();
		// puAffinitiy = createPuAffinity();

		vMemory = createVMemory();
		vMemoryMeasurement = createVirtualMemoryMeasurement();
		vMemoryUtilization = createMemoryUtilisation();

		vCpu = createVCpu();
		vCpuMeasurement = createVCpuMeasurement();
		vCpuUtilization = createVCpuUtilization();

	}

	private VMImage createVMImage() {
		VMImage _vmImage = CoreFactory.INSTANCE.createVMImage();
		_vmImage.setName(vmImageUUID);
		_vmImage.setLogicalDCModel(logicalDCModel);
		return _vmImage;
	}

	private VirtualDisk createVirtualDisk() {
		VirtualDisk virtualDisk = CoreFactory.INSTANCE.createVirtualDisk();
		virtualDisk.setLogicalDCModel(logicalDCModel);
		return virtualDisk;
	}

	private VMImageInstance createVmImageInstance() {
		VMImageInstance _vmImageInstance = CoreFactory.INSTANCE.createVMImageInstance();
		// vmImage.getVirtualMachine().add(_vmImageInstance);
		// _vmImageInstance.setExecutedVMImage(vmImage);
		// _vmImageInstance.setIsRunLocally(true);
		return _vmImageInstance;
	}

	private VirtualMachine createVirtualMachine() {
		VirtualMachine _vm = CoreFactory.INSTANCE.createVirtualMachine();
		_vm.setName(vmUUID);
		_vm.setVMImageInstance(vmImageInstance);
		_vm.setHypervisor(hypervisor);
		return _vm;
	}

	private VirtualMemory createVMemory() {
		VirtualMemory _vMemory = CoreFactory.INSTANCE.createVirtualMemory();
		_vMemory.setVirtualMemoryConsumingEntity(vm);
		return _vMemory;
	}

	private VirtualMemoryMeasurement createVirtualMemoryMeasurement() {
		VirtualMemoryMeasurement _vMemoryMeasurement = LogicalFactory.INSTANCE.createVirtualMemoryMeasurement();
		_vMemoryMeasurement.setObservedVirtualMemory(vMemory);
		_vMemoryMeasurement.setLogicalLoadModel(logicalLoadModel);
		return _vMemoryMeasurement;
	}

	private Utilization createMemoryUtilisation() {
		Utilization _vMemoryUtilization = PhysicalFactory.INSTANCE.createUtilization();
		_vMemoryUtilization.setVirtualMemoryMeasurement(vMemoryMeasurement);
		return _vMemoryUtilization;
	}

	private VirtualProcessingUnit createVCpu() {
		VirtualProcessingUnit _vCpu = CoreFactory.INSTANCE.createVirtualProcessingUnit();
		ArchitectureType architectureType = hypervisor.getNode().getCpuSpecifications().get(0).getArchitectureType();
		_vCpu.setArchitectureType(architectureType);
		_vCpu.setName(architectureType.getName());
		_vCpu.setVirtualMachine(vm);
		return _vCpu;
	}

	private VirtualProcessingUnitsMeasurement createVCpuMeasurement() {
		VirtualProcessingUnitsMeasurement _vCpuMeasurement = LogicalFactory.INSTANCE.createVirtualProcessingUnitsMeasurement();
		_vCpuMeasurement.setObservedVirtualProcessingUnit(vCpu);
		_vCpuMeasurement.setLogicalLoadModel(logicalLoadModel);
		return _vCpuMeasurement;
	}

	private Utilization createVCpuUtilization() {
		Utilization _vCpuUtilization = PhysicalFactory.INSTANCE.createUtilization();
		_vCpuUtilization.setVirtualProcessingUnitMeasurement(vCpuMeasurement);
		return _vCpuUtilization;
	}

	public void fillVirtualMachineInputParameters(String vmName,
		String isDeleted,
		String applicationType,
		String applicationTypeInstance,
		String applicationComponent,
		String applicationComponentInstance) {
		vm.getInputParameters().put("vmName", vmName);
		vm.getInputParameters().put("isDeleted", isDeleted);
		vm.getInputParameters().put("applicationType", applicationType);
		vm.getInputParameters().put("applicationTypeInstance", applicationTypeInstance);
		vm.getInputParameters().put("applicationComponent", applicationComponent);
		vm.getInputParameters().put("applicationComponentInstance", applicationComponentInstance);
		
	}

	public void fillVirtualMachineState(String state) {
		VM_State vm_State = null;
		switch (state) {
		case "RUNNING":
		case "running":
			vm_State = VM_State.RUNNING;
			break;
		case "SHUT OFF":
		case "shut":
		case "STOPPED":// FCO SHUTDOWN
			vm_State = VM_State.SHUTDOWN;
			break;
		case "PAUSED":
		case "paused":
			vm_State = VM_State.PAUSED;
			break;
		default:
			vm_State = null;
		}
		vm.setState(vm_State);
	}

	public void fillVMImageInstanceStorageSize(double storageSize) {
		// vmImageInstance.setLocalStorageSize(Amount.valueOf(storageSize,
		// SI.MEGA(NonSI.BYTE)));
	}

	public void fillNumberOfCoresForVCpu(int vCores) {
		vCpu.setVirtualCores(vCores);
	}

	public void fillVCpuUtil(double util) {
		if (!SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.ENABLE_FASTDELIVERY).equals("true")) {
			vCpuUtilization.setValue(Amount.valueOf(util / 100, Dimensionless.UNIT));
		}
	}

	public void fillProvisionedMemory(double vmMemoryTotal) {
		vMemory.setProvisioned(Amount.valueOf(vmMemoryTotal, SI.MEGA(NonSI.BYTE)));
	}

	public void fillMemoryUtil(double total, double used) {
		if (!SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.ENABLE_FASTDELIVERY).equals("true")) {
			double vmMemoryUtilFinal = used / total;
			vMemoryUtilization.setValue(Amount.valueOf(vmMemoryUtilFinal, Dimensionless.UNIT));
		}
	}

	@Override
	public String getNodeKey() {
		return vmUUID;
	}
}
