package eu.cactosfp7.vmi.controller.openstack.worker;

import java.util.logging.Logger;
import org.eclipse.emf.cdo.view.CDOView;
import eu.cactosfp7.cdosession.CactosCdoSession;
import eu.cactosfp7.cdosession.settings.CactosUser;
import eu.cactosfp7.cdosessionclient.CdoSessionClient;
import eu.cactosfp7.optimisationplan.ExecutionStatus;
import eu.cactosfp7.optimisationplan.OptimisationPlan;
import eu.cactosfp7.optimisationplan.OptimisationPlanRepository;

public final class IterateOptimisationPlans implements VMIWorker {

	private static final Logger logger = Logger.getLogger(IterateOptimisationPlans.class.getName());
	
	public IterateOptimisationPlans() {
		// nothing to do
	}

	private String findOldestPlan(OptimisationPlanRepository planRepository) {
		OptimisationPlan oldestPlan = null;
		for (OptimisationPlan plan : planRepository.getOptimisationPlans()) {
			if(plan.getExecutionStatus() != ExecutionStatus.READY){
				continue;
			}
			if(oldestPlan == null || oldestPlan.getCreationDate().after(plan.getCreationDate())){
				// current plan is newer!
				oldestPlan = plan;
			}
		}
		
		if(oldestPlan == null){
			logger.info("no (oldest) plan found. nothing to do.");
			return null;
		}
		
		return oldestPlan.getId();
	}
	
	private boolean planInExecution(OptimisationPlanRepository planRepository) {
		for (OptimisationPlan plan : planRepository.getOptimisationPlans()) {
			if(plan.getExecutionStatus() == ExecutionStatus.IN_EXECUTION){
				// if any plan is in execution, quit
				logger.warning("Plan " + plan.getId() + " is currently in state " + ExecutionStatus.IN_EXECUTION + ". Not starting a new one.");
				return true;
			}
		}
		return false;
	}
	
	private OptimisationPlanRepository getPlanRepository(CactosCdoSession cactosCdoSession, CDOView cdoView) {
		OptimisationPlanRepository planRepository = (OptimisationPlanRepository) cactosCdoSession.getRepository(cdoView,
				cactosCdoSession.getOptimisationPlanPath());
		return planRepository;
	}
	
	@Override
	public boolean work() {
		// Read optimisationPlanRepository
		
		CactosCdoSession cactosCdoSession = CdoSessionClient.INSTANCE.getService()
														.getCactosCdoSession(CactosUser.CACTOOPT);
		CDOView cdoView = cactosCdoSession.createView();
		String planId = null;
		try {
			OptimisationPlanRepository planRepository = getPlanRepository(cactosCdoSession, cdoView);

			if (planRepository == null) { // quit, if no planRepo found
				logger.severe("OptimisationPlanRepository not found on CDO server.");
				return false;
			}
		
			// Look for Plans with state IN_EXECUTION
			if(planInExecution(planRepository)) {
				return false;
			}
		
			// Find the oldest plan
			planId = findOldestPlan(planRepository);
		} finally {
			cactosCdoSession.closeConnection(cdoView);
		}
		
		// Execute oldest Plan, if there is one
		ExecuteOptimisationPlan action = new ExecuteOptimisationPlan(planId);
		return action.work();
	}

}
