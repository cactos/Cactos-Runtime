package eu.cactosfp7.vmi.controller.openstack.worker.planexecution;

import eu.cactosfp7.cloudiator.ApplicationModelInstanceBuilderClient;
import eu.cactosfp7.cloudiator.ColosseumUser;
import eu.cactosfp7.optimisationplan.ExecutionStatus;
import eu.cactosfp7.optimisationplan.ScaleOut;

public class ScaleOutStep implements OptimisationActionStepExecution {

	private final ScaleOut action;

	public ScaleOutStep(ScaleOut action) {
		this.action = action;
	}

	@Override
	public ExecutionStatus execute() {
		if (ApplicationModelInstanceBuilderClient.INSTANCE == null
				|| ApplicationModelInstanceBuilderClient.INSTANCE.getService() == null) {
			return ExecutionStatus.COMPLETED_FAILED;
		}
		String appInstanceId = ApplicationModelInstanceBuilderClient.INSTANCE.getService().scaleActionToAppInstanceId(action);
		String componentName = ApplicationModelInstanceBuilderClient.INSTANCE.getService().scaleActionToComponentName(action);
		
		// Trigger scale-out with cloudiator //FIXME: DataPlay
		ColosseumUser colUser = new ColosseumUser(ColosseumUser.UserType.DATAPLAY);
		colUser.scaleOut(
				Long.valueOf(appInstanceId), 
				componentName);
		
		// when done, set state.
		return ExecutionStatus.COMPLETED_SUCCESSFUL;
	}

}
