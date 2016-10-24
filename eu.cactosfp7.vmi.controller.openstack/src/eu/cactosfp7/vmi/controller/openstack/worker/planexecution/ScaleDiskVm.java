package eu.cactosfp7.vmi.controller.openstack.worker.planexecution;

import java.util.List;

import javax.measure.quantity.DataAmount;

import org.jscience.physics.amount.Amount;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.ActionResponse;
import org.openstack4j.model.compute.Flavor;

import eu.cactosfp7.infrastructuremodels.logicaldc.core.VMImageInstance;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine;
import eu.cactosfp7.optimisationplan.ExecutionStatus;
import eu.cactosfp7.ossessionclient.OsSessionClient;

public class ScaleDiskVm implements OptimisationActionStepExecution {

	Amount<DataAmount> proposedLocalSize;
	VMImageInstance vmImageInstance;
	
	public ScaleDiskVm(Amount<DataAmount> proposedLocalSize, VMImageInstance vmImageInstance) {
		this.proposedLocalSize = proposedLocalSize;
		this.vmImageInstance = vmImageInstance;
	}

	@Override
	public ExecutionStatus execute() {
		// lookup variables
		VirtualMachine vm = this.vmImageInstance.getVirtualMachine();
		String VMid = vm.getName();
		OSClient os = OsSessionClient.INSTANCE.getService().getCactosOsSession().getOsClient();		
		long disksizeProposed = this.proposedLocalSize.getExactValue();
		
		// lookup matching flavor
		Flavor matchingFlavor = null;
		List<? extends Flavor> flavors = os.compute().flavors().list();
		for(Flavor flavor : flavors){
			int flavorDisk = flavor.getDisk();
			// current flavor is best matching, if
			// 1. memoryProposed is smaller;
			// 2. previous found is larger than current one
			if(disksizeProposed < flavorDisk &&
					matchingFlavor.getDisk() > flavor.getDisk()){
				matchingFlavor = flavor;
			}			
		}
		if(matchingFlavor == null){
			// no matching flavor found
			return ExecutionStatus.COMPLETED_FAILED;
		}
		
		// change flavor of VM
		ActionResponse response = os.compute().servers().resize(VMid, matchingFlavor.getId());
		if(response.isSuccess()){
			// confirm if resize was successful
			response = os.compute().servers().confirmResize(VMid);
		}
		
		// look for execution state of rest call
		if (response.isSuccess()) {
			return ExecutionStatus.COMPLETED_SUCCESSFUL;
		} else {
			return ExecutionStatus.COMPLETED_FAILED;
		}
	}

}
