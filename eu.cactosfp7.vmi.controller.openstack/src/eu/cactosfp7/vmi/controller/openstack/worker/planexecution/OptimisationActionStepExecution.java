package eu.cactosfp7.vmi.controller.openstack.worker.planexecution;

import eu.cactosfp7.optimisationplan.ExecutionStatus;

public interface OptimisationActionStepExecution {
	
	public ExecutionStatus execute();
	
}
