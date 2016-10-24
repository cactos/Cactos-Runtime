package eu.cactosfp7.vmi.controller.openstack.worker.planexecution;

import javax.measure.quantity.DataAmount;

import org.jscience.physics.amount.Amount;

import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMemory;
import eu.cactosfp7.optimisationplan.ExecutionStatus;

public class ScaleMemoryVm implements OptimisationActionStepExecution {

	Amount<DataAmount> proposedSize;
	VirtualMemory scaledVirtualMemory;

	public ScaleMemoryVm(Amount<DataAmount> proposedSize, VirtualMemory scaledVirtualMemory) {
		this.proposedSize = proposedSize;
		this.scaledVirtualMemory = scaledVirtualMemory;
	}

	@Override
	public ExecutionStatus execute() {

		throw new RuntimeException("Cannot ScaleMemory since method VirtualMemory.getVirtualMachine() is unknown.");
		
		// lookup variables
//		VirtualMachine vm = this.scaledVirtualMemory.getVirtualMachine();
//		String VMid = vm.getName();
//		OSClient os = OsSessionClient.INSTANCE.getService().getCactosOsSession().getOsClient();		
//		long memoryProposed = this.proposedSize.getExactValue();
//		
//		// lookup matching flavor
//		Flavor matchingFlavor = null;
//		List<? extends Flavor> flavors = os.compute().flavors().list();
//		for(Flavor flavor : flavors){
//			int flavorRam = flavor.getRam();
//			// current flavor is best matching, if
//			// 1. memoryProposed is smaller;
//			// 2. previous found is larger than current one
//			if(memoryProposed < flavorRam &&
//					matchingFlavor.getRam() > flavor.getRam()){
//				matchingFlavor = flavor;
//			}			
//		}
//		if(matchingFlavor == null){
//			// no matching flavor found
//			return ExecutionStatus.COMPLETED_FAILED;
//		}
//		
//		// change flavor of VM
//		ActionResponse response = os.compute().servers().resize(VMid, matchingFlavor.getId());
//		if(response.isSuccess()){
//			// confirm if resize was successful
//			response = os.compute().servers().confirmResize(VMid);
//		}
//		
//		// look for execution state of rest call
//		if (response.isSuccess()) {
//			return ExecutionStatus.COMPLETED_SUCCESSFUL;
//		} else {
//			return ExecutionStatus.COMPLETED_FAILED;
//		}
	}

}
