package eu.cactosfp7.vmi.controller.openstack.worker.planexecution;

import java.util.logging.Logger;

import eu.cactosfp7.optimisationplan.ExecutionStatus;
import eu.cactosfp7.optimisationplan.LogicalMemoryScalingAction;
import eu.cactosfp7.optimisationplan.LogicalStorageScalingAction;
import eu.cactosfp7.optimisationplan.ManagePhysicalNodeAction;
import eu.cactosfp7.optimisationplan.PhysicalFrequencyScalingAction;
import eu.cactosfp7.optimisationplan.StartVmAction;
import eu.cactosfp7.optimisationplan.StopVmAction;
import eu.cactosfp7.optimisationplan.SuspendVmAction;
import eu.cactosfp7.optimisationplan.VerticalScalingAction;
import eu.cactosfp7.optimisationplan.VmMigrationAction;
import eu.cactosfp7.optimisationplan.VmPlacementAction;

public class DummyOptimisationplanSwitch extends OptimisationplanSwitch {

	public DummyOptimisationplanSwitch() {
		super();
		// TODO Auto-generated constructor stub
	}

	private static final Logger logger = Logger.getLogger(DummyOptimisationplanSwitch.class.getName());

	@Override
	public ExecutionStatus caseStartVmAction(StartVmAction action) {
		logger.info("DummyOptimisationplanSwitch action StartVm " + action.getStartedVm());
		return ExecutionStatus.COMPLETED_SUCCESSFUL;
	}

	@Override
	public ExecutionStatus caseStopVmAction(StopVmAction action) {
		logger.info("DummyOptimisationplanSwitch action StopVm " + action.getStoppedVm());
		return ExecutionStatus.COMPLETED_SUCCESSFUL;
	}

	@Override
	public ExecutionStatus caseSuspendVmAction(SuspendVmAction action) {
		logger.info("DummyOptimisationplanSwitch action SuspendVm " + action.getSuspendedVm());
		return ExecutionStatus.COMPLETED_SUCCESSFUL;
	}

	@Override
	public ExecutionStatus caseVmMigrationAction(VmMigrationAction action) {
		logger.info("DummyOptimisationplanSwitch action VmMigration " + action.getMigratedVm() + " to "
				+ action.getTargetHost());
		return ExecutionStatus.COMPLETED_SUCCESSFUL;
	}

	@Override
	public ExecutionStatus caseManagePhysicalNodeAction(ManagePhysicalNodeAction action) {
		logger.info("DummyOptimisationplanSwitch action ManagePhysicalNode " + action.getManagedNode() + " state "
				+ action.getTargetState());
		return ExecutionStatus.COMPLETED_SUCCESSFUL;
	}

	@Override
	public ExecutionStatus caseLogicalMemoryScalingAction(LogicalMemoryScalingAction action) {
		logger.info("DummyOptimisationplanSwitch action LogicalMemoryScaling "
				+ action.getScaledVirtualMemory().getId() + " to " + action.getProposedSize());
		return ExecutionStatus.COMPLETED_SUCCESSFUL;
	}

	@Override
	public ExecutionStatus caseLogicalStorageScalingAction(LogicalStorageScalingAction action) {
		logger.info("DummyOptimisationplanSwitch action LogicalStorageScaling "
				+ action.getVMImageInstance().getVirtualMachine() + " to " + action.getProposedLocalSize());
		return ExecutionStatus.COMPLETED_SUCCESSFUL;
	}

	@Override
	public ExecutionStatus casePhysicalFrequencyScalingAction(PhysicalFrequencyScalingAction action) {
		// TODO Find a way to implement this
		return super.casePhysicalFrequencyScalingAction(action);
	}

	@Override
	public ExecutionStatus caseVerticalScalingAction(VerticalScalingAction action) {
		// christopher says: no idea how to implement this
		return super.caseVerticalScalingAction(action);
	}

	@Override
	public ExecutionStatus caseVmPlacementAction(VmPlacementAction action) {
		// This action should not be used any more!
		// Please use the synchronous communication between VMI, CactoOpt and
		// VMI Controller
		// instead of using the asynchronous way via OptimisationPlans.
		throw new RuntimeException("caseVmPlacementAction is not allowed and not supported any more");
	}
}
