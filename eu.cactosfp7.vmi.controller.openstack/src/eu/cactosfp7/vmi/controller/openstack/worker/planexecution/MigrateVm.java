package eu.cactosfp7.vmi.controller.openstack.worker.planexecution;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.measure.quantity.DataAmount;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.eclipse.emf.cdo.common.branch.CDOBranchPoint;
import org.eclipse.emf.cdo.common.id.CDOID;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.cdo.util.CommitException;
import org.eclipse.emf.cdo.view.CDOView;
import org.jscience.physics.amount.Amount;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.compute.ServerService;
import org.openstack4j.model.compute.ActionResponse;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.actions.LiveMigrateOptions;

import eu.cactosfp7.cdosession.CactosCdoSession;
import eu.cactosfp7.cdosession.settings.CactosUser;
import eu.cactosfp7.cdosessionclient.CdoSessionClient;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.Hypervisor;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VM_State;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.ComputeNode;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.StorageSpecification;
import eu.cactosfp7.optimisationplan.ExecutionStatus;
import eu.cactosfp7.ossessionclient.OsSessionClient;

public class MigrateVm implements OptimisationActionStepExecution {

    private static final Logger logger = Logger.getLogger(MigrateVm.class.getName());

    //private static final int RETRIES = 40;
    private static final int MAX_TRIES = 45;
    private static final int WAIT_TIMEOUT = 20000;
    private final static int CORRECTION = 3;
    private static final long VM_MIN_AGE = 60*(5 + CORRECTION); // seconds

	private final CactosCdoSession cactosCdoSession;
    
	VirtualMachine vm;
//	Hypervisor source;
	Hypervisor target;

	
	public MigrateVm(VirtualMachine vm, Hypervisor source, Hypervisor target) {
		this.vm = vm;
//		this.source = source;
		this.target = target;
		cactosCdoSession = CdoSessionClient.INSTANCE.getService()
				.getCactosCdoSession(CactosUser.CACTOSCALE);
	}

	@Override
	public ExecutionStatus execute() {
		String VMid = vm.getName();
		CDOID vmCdoID = vm.cdoID();
		OSClient os = OsSessionClient.INSTANCE.getService().getCactosOsSession().getOsClient();
		String targetHostname = target.getNode().getName();

		if(VMid == null) {
			logger.log(Level.SEVERE, "about to migrate VM with Id null.");
			return ExecutionStatus.COMPLETED_FAILED;
		}
		if(!validateVmAge(VMid, os)){
			logger.log(Level.WARNING, "VM not old enough. Will reject migration of VM with COMPLETED_FAILED.");
			return ExecutionStatus.COMPLETED_FAILED;
		}

		// Set options for migration
		LiveMigrateOptions options = LiveMigrateOptions.create();
		options.host(targetHostname);
		options.diskOverCommit(true);
		options.blockMigration(isBlockMigration());

		ServerService servers = os.compute().servers();
		// Set VM State to IN_OPTIMISATION before triggering operation
		VM_State previousState = vm.getState();
		if(!changeVmState(vm.cdoID(), VM_State.IN_OPTIMISATION)){
			logger.log(Level.SEVERE, "Changing state of VM failed. MigrateVm step failed.");
			return ExecutionStatus.COMPLETED_FAILED;
		}

		// execute request
		ActionResponse response = servers.liveMigrate(VMid,options);
		String currentHostname = null;
		if (response.isSuccess()) {
			logger.log(Level.INFO, "Starting migration of " + VMid + " to target "  + targetHostname);
			// lookup migration state periodically
			int i = 0;
			while(i < MAX_TRIES){
				logger.log(Level.INFO, "current host name is: " + currentHostname);
				currentHostname = getCurrentServerName(os, VMid);
				if(currentHostname != null)
					break;
				i++;
			}
			if(i == MAX_TRIES) {
				logger.log(Level.SEVERE, "Migration lookup gives up after " + i + " checks; this should never occur.");
				return ExecutionStatus.COMPLETED_FAILED;
			}

			logger.log(Level.INFO, "current host name is: " + currentHostname);
			if(targetHostname.equals(currentHostname)){
				// migration executed successfully, wait for status to be
				// set to RUNNING
				logger.log(Level.INFO, "VM was migrated to host " + targetHostname + " on OpenStack level.");
				while(!waitForStateChange(vmCdoID)) ;
				logger.log(Level.INFO, "VM was migrated to host " + targetHostname + " successfully.");
				return ExecutionStatus.COMPLETED_SUCCESSFUL;	
			}
			logger.log(Level.SEVERE, "Migration to host " + targetHostname + " failed.");
			return ExecutionStatus.COMPLETED_FAILED;
		} else {
			logger.log(Level.INFO, "MigrationAction failed: " + response.getFault());
			
			if(!changeVmState(vm.cdoID(), previousState)){
				logger.log(Level.SEVERE, "Changing state of VM  back to previousState failed. MigrateVm step failed.");
//				recoverResetState(transaction.getSession(), vmCdoID, previousState, RETRIES);
			}
			return ExecutionStatus.COMPLETED_FAILED;
		}
	}

	private boolean changeVmState(CDOID cdoID, VM_State state) {
		CDOTransaction transaction = cactosCdoSession.createTransaction();
		try {
			VirtualMachine vm = (VirtualMachine) transaction.getObject(cdoID);
			vm.setState(state);
			cactosCdoSession.commitAndCloseConnection(transaction);
			return true;
		}catch(CommitException e){
			if(transaction != null)
				transaction.rollback();
			logger.log(Level.SEVERE, "could not set state of vm " + cdoID + " to " + state, e);
			return false;
		}finally{
			cactosCdoSession.closeConnection(transaction);
		}
	}

	private boolean validateVmAge(String vMid, OSClient os) {
	        Server server = os.compute().servers().get(vMid);
	        if(server == null){
	        	logger.log(Level.WARNING, "Cannot migrate vm " + vMid + ", vm not found in openstack.");
	        	return false;
	        }
        	Date creationDate = server.getCreated();
	        Date now = new Date();
	        long vmAge = (now.getTime()-creationDate.getTime())/1000;
	        return (vmAge > VM_MIN_AGE); // valid if vm is older than VM_MIN_AGE (5 minutes).
	}

	/**
	 * @return the host name of the current host. null otherwise.
	 */
	private String getCurrentServerName(OSClient os, String VMid) {
		logger.log(Level.FINE, "waiting for status change.");
		Server server = os.compute().servers().get(VMid);
		Server.Status status = server.getStatus();
		String serverName = server.getHost();
		logger.log(Level.INFO, "status is: " + status + " and server name is: " + serverName);
		if(!Server.Status.MIGRATING.equals(status)){
			logger.log(Level.INFO, "status is " + status + ": terminate wait cycle");
			return serverName;
		} else{
			logger.log(Level.INFO, "status is " + status + ": now sleeping");
			try {
				Thread.sleep(WAIT_TIMEOUT);
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, "Cannot wait before next state lookup", e);
			}
			return null;
		}
	}

	// HELPER METHODS BELOW
	private boolean waitForStateChange(final CDOID vmCdoID) {
		logger.log(Level.INFO, "Waiting for VM_State change in CDO models");
		CDOView view = cactosCdoSession.createView();
		logger.log(Level.INFO, "current branch point: " + view.getBranch() + "@" + view.getTimeStamp());
		boolean success = view.setTimeStamp(CDOBranchPoint.UNSPECIFIED_DATE);
		logger.log(Level.INFO, "setting timestamp status: " + success);
		VirtualMachine vm = (VirtualMachine) view.getObject(vmCdoID);
		VM_State state = vm.getState();
		if (!VM_State.IN_OPTIMISATION.equals(state))
			return true; // not in state IN_OPTIMISATION any more -> done

		logger.log(Level.INFO, "vm state is: " + vm.getState());
		try {
			Thread.sleep(WAIT_TIMEOUT);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "Cannot wait before next state lookup", e);
		} finally {
			cactosCdoSession.closeConnection(view);
		}
		return false;
	}

//	// Recover from failed commit of VM state information.
//	private void recoverResetState(final CDOSession session, final CDOID vmCdoID, VM_State oldState, final int retries) {
//        for(int idx = 0; idx < retries; idx++) {
//            CDOTransaction newTx = session.openTransaction();
//            VirtualMachine vm = (VirtualMachine) newTx.getObject(vmCdoID);
//            vm.setState(oldState);
//            try {
//                newTx.commit();
//            } catch(CommitException e) {
//                try {
//                Thread.sleep(500);
//                } catch (InterruptedException interrupted){
//                    Thread.currentThread().interrupt();
//                }
//            }
//        }
//    }

    private boolean isBlockMigration() {
		// if one of both has local disk, use block migration
		// if both are diskless, no block migration
		if(hasStorage(vm.getHypervisor().getNode()) || hasStorage(target.getNode()))
			return true;
		return false;
	}
	
	private boolean hasStorage(ComputeNode node){
		if(node.getStorageSpecifications().isEmpty())
			return false;
		for(StorageSpecification spec : node.getStorageSpecifications()){
			if(spec.getSize() == null)
				continue;
			Amount<DataAmount> zero = Amount.valueOf(0, SI.GIGA(NonSI.BYTE));
			if(spec.getSize().isGreaterThan(zero)){
				// found one storage greater 0 Byte. 
				// meaning: ComputeNode hasStorage is true
				return true;
			}				
		}
		return false;
	}

}
