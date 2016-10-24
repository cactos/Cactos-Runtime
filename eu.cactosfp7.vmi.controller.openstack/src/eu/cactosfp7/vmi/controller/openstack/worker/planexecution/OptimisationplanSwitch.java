package eu.cactosfp7.vmi.controller.openstack.worker.planexecution;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.cdo.util.CommitException;

import eu.cactosfp7.cdosession.CactosCdoSession;
import eu.cactosfp7.cdosession.settings.CactosUser;
import eu.cactosfp7.cdosessionclient.CdoSessionClient;
import eu.cactosfp7.optimisationplan.ExecutionStatus;
import eu.cactosfp7.optimisationplan.LogicalMemoryScalingAction;
import eu.cactosfp7.optimisationplan.LogicalStorageScalingAction;
import eu.cactosfp7.optimisationplan.ManagePhysicalNodeAction;
import eu.cactosfp7.optimisationplan.OptimisationPlan;
import eu.cactosfp7.optimisationplan.OptimisationStep;
import eu.cactosfp7.optimisationplan.ParallelSteps;
import eu.cactosfp7.optimisationplan.PhysicalFrequencyScalingAction;
import eu.cactosfp7.optimisationplan.ResourceControlAction;
import eu.cactosfp7.optimisationplan.ScaleIn;
import eu.cactosfp7.optimisationplan.ScaleOut;
import eu.cactosfp7.optimisationplan.SequentialSteps;
import eu.cactosfp7.optimisationplan.StartVmAction;
import eu.cactosfp7.optimisationplan.StopVmAction;
import eu.cactosfp7.optimisationplan.SuspendVmAction;
import eu.cactosfp7.optimisationplan.VmMigrationAction;
import eu.cactosfp7.optimisationplan.VmPlacementAction;

public class OptimisationplanSwitch extends eu.cactosfp7.optimisationplan.util.OptimisationplanSwitch<ExecutionStatus> {

	private static final Logger logger = Logger.getLogger(OptimisationplanSwitch.class.getName());
	private final CactosCdoSession cactosCdoSession;

	public OptimisationplanSwitch() {
		cactosCdoSession = CdoSessionClient.INSTANCE.getService()
				.getCactosCdoSession(CactosUser.CACTOSCALE);
	}

	private ExecutionStatus changeState(OptimisationStep viewStep, ExecutionStatus status) {
		CDOTransaction transaction = cactosCdoSession.createTransaction();
		OptimisationStep step = (OptimisationStep) transaction.getObject(viewStep.cdoID());
		if (status.equals(ExecutionStatus.COMPLETED_FAILED) || status.equals(ExecutionStatus.COMPLETED_SUCCESSFUL)) {
			step.setExecutionStopped(new Date());
		}
		if (status.equals(ExecutionStatus.IN_EXECUTION)) {
			step.setExecutionStarted(new Date());
		}
		step.setExecutionStatus(status);
		try {
			cactosCdoSession.commitAndCloseConnection(transaction);
		} catch (CommitException e) {
			if(transaction != null)
				transaction.rollback();
			logger.log(Level.SEVERE, "Cannot update OptimisationStep's status", e);
		}finally{
			cactosCdoSession.closeConnection(transaction);
		}
		return status;
	}

	// Copy of changeState method, since Plan does not extend Step ...
	private ExecutionStatus changeStatePlan(OptimisationPlan viewStep, ExecutionStatus status) {
		CDOTransaction transaction = cactosCdoSession.createTransaction();
		OptimisationPlan step = (OptimisationPlan) transaction.getObject(viewStep.cdoID());
		if (status.equals(ExecutionStatus.COMPLETED_FAILED) || status.equals(ExecutionStatus.COMPLETED_SUCCESSFUL)) {
			step.setExecutionStopped(new Date());
		}
		if (status.equals(ExecutionStatus.IN_EXECUTION)) {
			step.setExecutionStarted(new Date());
		}
		step.setExecutionStatus(status);
		try {
			cactosCdoSession.commitAndCloseConnection(transaction);
		} catch (CommitException e) {
			if(transaction != null)
				transaction.rollback();
			logger.log(Level.SEVERE, "Cannot update OptimisationPlan's status", e);
		}finally{
			cactosCdoSession.closeConnection(transaction);
		}
		return status;
	}

	@Override
	public ExecutionStatus caseOptimisationPlan(OptimisationPlan viewPlan) {
		try {
			ExecutionStatus stateStep = this.doSwitch(viewPlan.getOptimisationStep());
			return changeStatePlan(viewPlan, stateStep);
		} catch (Throwable t) {
			logger.log(Level.SEVERE, "Exception during execution of OptimisationPlan:", t);
			return changeStatePlan(viewPlan, ExecutionStatus.COMPLETED_FAILED);
		}
	}

	@Override
	public ExecutionStatus caseSequentialSteps(SequentialSteps viewObject) {
		changeState(viewObject, ExecutionStatus.IN_EXECUTION);
		// Iteratively execute all the nested steps
		for (OptimisationStep step : viewObject.getOptimisationSteps()) {
			// Execute a step, set its returned status as the global
			// status
			ExecutionStatus status = this.doSwitch(step);
			// stop the execution if one of the steps was not successful
			if (status.equals(ExecutionStatus.COMPLETED_FAILED)) {
				return changeState(viewObject, status);
			}
		}
		// If all steps were succesfull, the whole plan was successful
		return changeState(viewObject, ExecutionStatus.COMPLETED_SUCCESSFUL);
	}

	@Override
	public ExecutionStatus caseParallelSteps(ParallelSteps viewParallelSteps) {
		changeState(viewParallelSteps, ExecutionStatus.IN_EXECUTION);
		// Initialise thread pool and list of future results according to #
		// childs
		int childSteps = viewParallelSteps.getOptimisationSteps().size();
		ExecutorService pool = Executors.newFixedThreadPool(childSteps);
		List<Future<ExecutionStatus>> futures = new ArrayList<Future<ExecutionStatus>>(childSteps);

		// Define class for executing parallel steps
		class ParallelExecutionTask implements Callable<ExecutionStatus> {
			private OptimisationStep step;
			private OptimisationplanSwitch planSwitch;

			public ParallelExecutionTask(OptimisationStep step, OptimisationplanSwitch planSwitch) {
				this.step = step;
				this.planSwitch = planSwitch;
			}

			@Override
			public ExecutionStatus call() throws Exception {
				changeState(step, ExecutionStatus.IN_EXECUTION);
				ExecutionStatus status = planSwitch.doSwitch(step);
				return changeState(step, status);
			}
		}

		// loop through childs and submit their threads
		for (OptimisationStep step : viewParallelSteps.getOptimisationSteps()) {
			ParallelExecutionTask task = new ParallelExecutionTask(step, this);
			futures.add(pool.submit(task));
		}

		// Block for all childs to get finished
		boolean isResultFailed = false;
		for (Future<ExecutionStatus> future : futures) {
			try {
				ExecutionStatus result = future.get();
				// if only one failed, plan failed! But let the rest to finish!!
				if (result == ExecutionStatus.COMPLETED_FAILED) {
					isResultFailed = true;
				}
			} catch (InterruptedException | ExecutionException e) {
				logger.log(Level.SEVERE, "parallel step execution failed", e);
				return changeState(viewParallelSteps, ExecutionStatus.COMPLETED_FAILED);
			}
		}

		if (isResultFailed) {
			return changeState(viewParallelSteps, ExecutionStatus.COMPLETED_FAILED);
		}

		// shutdown thread pool correctly
		pool.shutdown();

		// If all went right, return success
		return changeState(viewParallelSteps, ExecutionStatus.COMPLETED_SUCCESSFUL);
	}

	@Override
	public ExecutionStatus caseStartVmAction(StartVmAction viewAction) {
		changeState(viewAction, ExecutionStatus.IN_EXECUTION);
		OptimisationActionStepExecution exec = new StartVm(viewAction.getStartedVm());
		ExecutionStatus status = exec.execute();
		return changeState(viewAction, status);
	}

	@Override
	public ExecutionStatus caseStopVmAction(StopVmAction viewAction) {
		changeState(viewAction, ExecutionStatus.IN_EXECUTION);
		OptimisationActionStepExecution exec = new StopVm(viewAction.getStoppedVm());
		ExecutionStatus status = exec.execute();
		return changeState(viewAction, status);
	}

	@Override
	public ExecutionStatus caseSuspendVmAction(SuspendVmAction viewAction) {
		changeState(viewAction, ExecutionStatus.IN_EXECUTION);
		OptimisationActionStepExecution exec = new SuspendVm(viewAction.getSuspendedVm());
		ExecutionStatus status = exec.execute();
		return changeState(viewAction, status);
	}

	@Override
	public ExecutionStatus caseVmMigrationAction(VmMigrationAction viewAction) {
		changeState(viewAction, ExecutionStatus.IN_EXECUTION);
		OptimisationActionStepExecution exec = new MigrateVm(viewAction.getMigratedVm(), viewAction.getSourceHost(), viewAction.getTargetHost());
		ExecutionStatus status = exec.execute();
		return changeState(viewAction, status);
	}

	@Override
	public ExecutionStatus caseManagePhysicalNodeAction(ManagePhysicalNodeAction viewAction) {
		changeState(viewAction, ExecutionStatus.IN_EXECUTION);
		OptimisationActionStepExecution exec = new ManagePhysicalNode(viewAction.getManagedNode(), viewAction.getTargetState());
		ExecutionStatus status = exec.execute();
		return changeState(viewAction, status);
	}

	@Override
	public ExecutionStatus caseLogicalMemoryScalingAction(LogicalMemoryScalingAction viewAction) {
		changeState(viewAction, ExecutionStatus.IN_EXECUTION);
		OptimisationActionStepExecution exec = new ScaleMemoryVm(viewAction.getProposedSize(), viewAction.getScaledVirtualMemory());
		ExecutionStatus status = exec.execute();
		return changeState(viewAction, status);
	}

	@Override
	public ExecutionStatus caseLogicalStorageScalingAction(LogicalStorageScalingAction viewAction) {
		changeState(viewAction, ExecutionStatus.IN_EXECUTION);
		OptimisationActionStepExecution exec = new ScaleDiskVm(viewAction.getProposedLocalSize(), viewAction.getVMImageInstance());
		ExecutionStatus status = exec.execute();
		return changeState(viewAction, status);
	}

	@Override
	public ExecutionStatus casePhysicalFrequencyScalingAction(PhysicalFrequencyScalingAction viewAction) {
		// TODO Find a way to implement this
		return super.casePhysicalFrequencyScalingAction(viewAction);
	}

	@Override
	public ExecutionStatus caseVmPlacementAction(VmPlacementAction viewAction) {
		/*
		 * TODO create virtual disk and overlay (local or remote differs) for
		 * suggested placement assign according to suggested placement
		 */
		return null;
	}

	@Override
	public ExecutionStatus caseScaleIn(ScaleIn viewAction) {
		changeState(viewAction, ExecutionStatus.IN_EXECUTION);
		OptimisationActionStepExecution exec = new ScaleInStep(viewAction);
		ExecutionStatus status = exec.execute();
		return changeState(viewAction, status);
	}

	@Override
	public ExecutionStatus caseScaleOut(ScaleOut viewAction) {
		changeState(viewAction, ExecutionStatus.IN_EXECUTION);
		OptimisationActionStepExecution exec = new ScaleOutStep(viewAction);
		ExecutionStatus status = exec.execute();
		return changeState(viewAction, status);
	}

	@Override
	public ExecutionStatus caseResourceControlAction(ResourceControlAction viewAction) {
		changeState(viewAction, ExecutionStatus.IN_EXECUTION);
		OptimisationActionStepExecution exec = new ResourceControl(viewAction);
		ExecutionStatus status = exec.execute();
		return changeState(viewAction, status);
	}
}
