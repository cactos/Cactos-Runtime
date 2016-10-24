package eu.cactosfp7.vmi.controller.openstack.worker.planexecution;

import org.openstack4j.model.compute.Action;

import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine;
import eu.cactosfp7.optimisationplan.ExecutionStatus;

public class SuspendVm extends SimpleOsVmHandler implements OptimisationActionStepExecution {
	
	public SuspendVm(VirtualMachine vm) {
		super(vm);
	}

	@Override
	public ExecutionStatus execute() {
		return super.execute(Action.SUSPEND);
	}

}
