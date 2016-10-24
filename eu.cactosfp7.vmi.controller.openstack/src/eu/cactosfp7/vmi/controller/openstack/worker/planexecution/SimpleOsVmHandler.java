package eu.cactosfp7.vmi.controller.openstack.worker.planexecution;

import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Action;
import org.openstack4j.model.compute.ActionResponse;

import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine;
import eu.cactosfp7.optimisationplan.ExecutionStatus;
import eu.cactosfp7.ossessionclient.OsSessionClient;

public class SimpleOsVmHandler {

	VirtualMachine vm;
	
	protected SimpleOsVmHandler(VirtualMachine vm) {
		this.vm = vm;
	}

	protected ExecutionStatus execute(Action openStackAction) {
		// lookup variables
		String vmOsId = vm.getName();
		OSClient os = OsSessionClient.INSTANCE.getService().getCactosOsSession().getOsClient();
		
		// make openstack rest call
		ActionResponse response = os.compute().servers().action(vmOsId, openStackAction);
		
		// look for execution state of rest call
		if (response.isSuccess()) {
			return ExecutionStatus.COMPLETED_SUCCESSFUL;
		} else {
			return ExecutionStatus.COMPLETED_FAILED;
		}
	}

}
