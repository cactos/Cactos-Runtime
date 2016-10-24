package eu.cactosfp7.vmi.controller.openstack.worker;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.cdo.common.id.CDOID;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.cdo.util.CommitException;
import org.eclipse.emf.cdo.view.CDOView;

import eu.cactosfp7.cdosession.CactosCdoSession;
import eu.cactosfp7.cdosession.settings.CactosUser;
import eu.cactosfp7.cdosessionclient.CdoSessionClient;
import eu.cactosfp7.optimisationplan.ExecutionStatus;
import eu.cactosfp7.optimisationplan.OptimisationPlan;
import eu.cactosfp7.optimisationplan.OptimisationPlanRepository;
import eu.cactosfp7.vmi.controller.openstack.worker.planexecution.OptimisationplanSwitch;

public final class ExecuteOptimisationPlan implements VMIWorker {

	private static final Logger log = Logger.getLogger(ExecuteOptimisationPlan.class.getName());
	private final String planId;
	private final CactosCdoSession cactosCdoSession;

	public ExecuteOptimisationPlan(String _uuid) {
		planId = _uuid;
		cactosCdoSession = CdoSessionClient.INSTANCE.getService()
				.getCactosCdoSession(CactosUser.CACTOSCALE);
	}
	
	@Override
	public boolean work() {
		if(planId == null) {
			return true;
		}
		try {
			CDOID planCdoId = doFindPlan();
			/* b turns false whenever any of the steps fails */
			return (planCdoId != null) && doStartExecution(planCdoId) && doSwitch(planCdoId);
		} catch(Exception e){ 
			log.log(Level.SEVERE, "Exception occuder while executing optimisation plan.", e);
			return false;
		}
	}
	
	private boolean doStartExecution(CDOID planCdoId) {
		// Mark OptimisationPlan as IN_EXECUTION
		CDOTransaction transaction = cactosCdoSession.createTransaction();
		try{
			log.info("Start executing optimisation plan " + planCdoId);
			OptimisationPlan plan = (OptimisationPlan) transaction.getObject(planCdoId);
			plan.setExecutionStatus(ExecutionStatus.IN_EXECUTION);
			plan.setExecutionStarted(new Date());
			cactosCdoSession.commitAndCloseConnection(transaction);
			return true;
		}catch(CommitException e){
			if(transaction != null)
				transaction.rollback();
			log.log(Level.SEVERE, "Failed to update executionState or starting date on optimisation plan.", e);
			return false;			
		}finally{
			cactosCdoSession.closeConnection(transaction);
		}
	}
	
	private boolean doSwitch(CDOID planCdoId) {
		// Start Execution of OptimisationPlan
		OptimisationplanSwitch oSwitch = new OptimisationplanSwitch();
		CDOView view = null;
		try {
			view = cactosCdoSession.createView();
			OptimisationPlan plan = (OptimisationPlan) view.getObject(planCdoId);
			oSwitch.doSwitch(plan);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Failed to execute optimisation plan " + planCdoId, e);
			return false;
		}finally{
			cactosCdoSession.closeConnection(view);
		}
		return true;
	}
	
	private CDOID doFindPlan() {
		CDOView view = cactosCdoSession.createView();
		try{
			// Read OptimisationPlan from CDO Server
			OptimisationPlanRepository planRepository = (OptimisationPlanRepository) cactosCdoSession.getRepository(view,
											cactosCdoSession.getOptimisationPlanPath());
			OptimisationPlan plan = findOptimisationPlan(planId, planRepository);
			if (plan == null) {
				log.info("Optimisation plan " + planId + " not found in planRepository.");
			}
			return plan.cdoID();
		}finally{
			cactosCdoSession.closeConnection(view);
		}
	}

	private OptimisationPlan findOptimisationPlan(final String uuid, OptimisationPlanRepository planRepository) {
		if (planRepository == null || planRepository.getOptimisationPlans().size() == 0) {
			log.info("plan repository not set or empty");
			return null;
		}
		for (OptimisationPlan plan : planRepository.getOptimisationPlans()) {
			if (plan.getId().equals(uuid)) {
				return plan;
			}
		}
		log.info("plan '" + uuid + "' was not found in plan repository");
		return null;
	}

}
