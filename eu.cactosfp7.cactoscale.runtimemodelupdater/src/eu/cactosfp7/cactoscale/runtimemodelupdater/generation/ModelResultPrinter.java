package eu.cactosfp7.cactoscale.runtimemodelupdater.generation;

import org.eclipse.emf.common.util.EList;

import eu.cactosfp7.infrastructuremodels.load.logical.LogicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.logical.VirtualMemoryMeasurement;
import eu.cactosfp7.infrastructuremodels.load.logical.VirtualProcessingUnitsMeasurement;
import eu.cactosfp7.infrastructuremodels.load.physical.InterconnectMeasurement;
import eu.cactosfp7.infrastructuremodels.load.physical.MemoryMeasurement;
import eu.cactosfp7.infrastructuremodels.load.physical.PhysicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.physical.PuMeasurement;
import eu.cactosfp7.infrastructuremodels.load.physical.StorageMeasurement;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.Hypervisor;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMemory;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.AbstractNode;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.ComputeNode;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.PhysicalDCModel;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.Rack;

public class ModelResultPrinter {
	public static void printAssignedVms(ComputeNode pm) {
		EList<VirtualMachine> vms = pm.getHypervisor().getVirtualMachines();

		System.out
				.println("---------------------------------------------------------------------------------------------");

		if (vms.size() == 0) {
			System.out.println("No VMs assigned to " + pm.toString());
			return;
		}

		System.out.println("List of VMs assigned to " + pm.toString() + ":");
		for (VirtualMachine vm : vms) {
			System.out.println("\t" + vm.toString());
		}
	}

	public static void printLogicalDCModel(LogicalDCModel ldcm) {
		System.out
				.println("---------------------------------------------------------------------------------------------");
		System.out.println("Logical DC Model");

		for (Hypervisor h : ldcm.getHypervisors()) {
			System.out.println("\t" + h.toString() + " on "
					+ h.getNode().toString());
			for (VirtualMachine vm : h.getVirtualMachines()) {
				System.out.println("\t\t" + vm.toString());
				for (VirtualMemory vmemory : vm.getVirtualMemoryUnits()) {
					System.out.println("\t\t\t" + vmemory.toString());
				}
			}
		}
	}

	public static void printPhysicalDCModel(PhysicalDCModel pdcm) {
		System.out
				.println("---------------------------------------------------------------------------------------------");
		System.out.println("Physical DC Model:");

		for (Rack r : pdcm.getRacks()) {
			System.out.println("\t" + r.toString());
			for (AbstractNode pm : r.getNodes()) {
				System.out.println("\t\t" + pm.toString());
				System.out.println("\t\t\t memory:"
						+ pm.getMemorySpecifications());
				System.out.println("\t\t\t cpu:" + pm.getCpuSpecifications());
				System.out.println("\t\t\t storage:"
						+ pm.getStorageSpecifications());

			}
		}
	}

	public static void printPhysicalLoadModel(PhysicalLoadModel plm) {
		System.out
				.println("---------------------------------------------------------------------------------------------");
		System.out.println("Physical Load Model:");

		for (MemoryMeasurement m : plm.getMemoryMeasurements()) {
			System.out.println("\t" + m.toString());
			System.out.println("\t\t"
					+ m.getObservedMemory().getNode().toString());
			System.out.println("\t\t" + m.getObservedMemory().toString());
			System.out.println("\t\t" + m.getUtilization().toString());
		}

		for (PuMeasurement c : plm.getCpuMeasurement()) {
			System.out.println("\t" + c.toString());
			System.out.println("\t\t" + c.getObservedPu().getNode().toString());
			System.out.println("\t\t" + c.getObservedPu().toString());
			System.out.println("\t\t" + c.getUtilization().toString());
		}

		for (StorageMeasurement c : plm.getStorageMeasurement()) {
			System.out.println("\t" + c.toString());
			System.out.println("\t\t"
					+ c.getObservedStorage().getNode().toString());
			System.out.println("\t\t" + c.getObservedStorage().toString());
			System.out.println("\t\t" + c.getStorageUtilization().toString());
		}

		for (InterconnectMeasurement c : plm.getInterconnectMeasurement()) {
			System.out.println("\t" + c.toString());
			// System.out.println("\t\t" +
			// c.getObservedInterconnect().toString());
			System.out.println("\t\t" + c.getMeasuredThroughput().toString());

		}
	}

	public static void printLogicalLoadModel(LogicalLoadModel llm) {
		System.out
				.println("---------------------------------------------------------------------------------------------");
		System.out.println("Logical Load Model:");

		for (VirtualMemoryMeasurement m : llm.getVirtualMemoryMeasurements()) {
			System.out.println("\t" + m.toString());
			// System.out.println("\t\t" +
			// m.getObservedVirtualMemory().getVirtualMachine().toString());
			System.out
					.println("\t\t" + m.getObservedVirtualMemory().toString());
			System.out.println("\t\t" + m.getUtilization().toString());
		}
		for (VirtualProcessingUnitsMeasurement c : llm
				.getVirtualProcessingUnitMeasurements()) {
			System.out.println("\t" + c.toString());
			// System.out.println("\t\t" +
			// m.getObservedVirtualMemory().getVirtualMachine().toString());
			System.out.println("\t\t" + c.getObservedVirtualProcessingUnit().toString());
			System.out.println("\t\t" + c.getUtilization().toString());
		}
	}
}
