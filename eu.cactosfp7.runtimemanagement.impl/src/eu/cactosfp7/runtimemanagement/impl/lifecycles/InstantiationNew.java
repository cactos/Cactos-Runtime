package eu.cactosfp7.runtimemanagement.impl.lifecycles;

import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.cdo.common.branch.CDOBranch;
import org.eclipse.emf.cdo.common.id.CDOID;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.cdo.util.CommitException;
import org.eclipse.emf.cdo.view.CDOView;
import org.omg.CORBA.StringHolder;

import eu.cactosfp7.cdosession.CactosCdoSession;
import eu.cactosfp7.cdosession.settings.CactosUser;
import eu.cactosfp7.cdosession.util.CdoHelper;
import eu.cactosfp7.cdosessionclient.CdoSessionClient;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.Hypervisor;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine;
import eu.cactosfp7.infrastructuremodels.physicaldc.architecturetype.ArchitectureTypeRepository;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.ComputeNode;
import eu.cactosfp7.runtimemanagement.impl.VmiControllerClient;
import eu.cactosfp7.runtimemanagement.service.RuntimeManagementException;
import eu.cactosfp7.runtimemanagement.util.Lifecycle;
import eu.cactosfp7.runtimemanagement.util.PropagateToChukwa;

public class InstantiationNew implements Lifecycle {

	/** Logger for this class. */
	private static final Logger logger = Logger.getLogger(InstantiationNew.class.getCanonicalName());

	private final int vcores;
	private final int memory;
	private final int diskspace;
	private final String imageref;
	private final Map<String, String> meta;
	private Map<String, String> resultMap;
	private final CactosCdoSession cdoSession;
	private String result;

	// private final int nodeBootSleep = 10*1000; // milliseconds
	// private final int nodeBootTries = 5;

	public InstantiationNew(int _vcores, int _memory, int _diskspace, String _imageref, Map<String, String> _meta) {
		vcores = _vcores;
		memory = _memory;
		diskspace = _diskspace;
		imageref = _imageref;
		meta = _meta;
		cdoSession = CdoSessionClient.INSTANCE.getService().getCactosCdoSession(CactosUser.CACTOSCALE);
	}

	private CDOID createAndRegisterVm() {
		final CDOTransaction cdoCon = cdoSession.createTransaction();
		try {
			LogicalDCModel logicalDcModel = (LogicalDCModel) cdoSession.getRepository(cdoCon, cdoSession.getLogicalModelPath());
			logger.info("Read logicalDcModel from cdoSession: " + logicalDcModel);
			ArchitectureTypeRepository architectureTypeRepository = (ArchitectureTypeRepository) cdoSession.getRepository(cdoCon, cdoSession.getArchitectureTypePath());
			logger.info("Read architectureTypeRepository from cdoSession: " + architectureTypeRepository);

			VirtualMachine vm = CdoHelper.createVMModel(logicalDcModel, architectureTypeRepository, vcores, memory, diskspace, imageref, meta);
			createBehaviourModel(vm, architectureTypeRepository);
			logger.log(Level.INFO, "created vm with ID " + vm.cdoID() + ". committing in transaction " + cdoCon);
			cdoSession.commitAndCloseConnection(cdoCon);
			CDOID vmCdoId = vm.cdoID();
			logger.log(Level.INFO, "created vm with ID " + vmCdoId + ". committing in transaction " + cdoCon);			
			//logger.log(Level.INFO, "commit success");
			return vmCdoId;
		} catch (CommitException e) {
			cdoCon.rollback();
			logger.log(Level.SEVERE, "Could not commit VM information for submitted VM to Runtime Model Storage. The VM is not accepted for submission and will not be placed.", e);
		} finally {
			// just in case
			cdoSession.closeConnection(cdoCon);
		} 
		return null;
	}

	private void sendDataToCactoScale(CDOID vmCdoId) {
		final CDOView view = cdoSession.createView();
		try {
			VirtualMachine vm = (VirtualMachine) view.getObject(vmCdoId);
			if(vm == null) {
				logger.log(Level.SEVERE, "Virtual machine " + vmCdoId + " not found in cdo server");
			} else {
				String vmName = vm.getName();
				String applicationType = vm.getInputParameters().containsKey("applicationType") ? vm.getInputParameters().get("applicationType") : "-";
				String applicationTypeInstance = vm.getInputParameters().containsKey("applicationTypeInstance") ? vm.getInputParameters().get("applicationTypeInstance") : "-";
				String applicationComponent = vm.getInputParameters().containsKey("applicationComponent") ? vm.getInputParameters().get("applicationComponent") : "-";
				String applicationComponentInstance = vm.getInputParameters().containsKey("applicationComponentInstance") ? vm.getInputParameters().get("applicationComponentInstance") : "-";
				// store new vm in chukwa/hbase
				PropagateToChukwa.instantiation(vmName, applicationType, applicationTypeInstance, applicationComponent, applicationComponentInstance);
			} 
		} finally {
			cdoSession.closeConnection(view);
		}
	}

	private void deleteVmModel(CDOID vmId) throws RuntimeManagementException {
		logger.log(Level.INFO, "deleting vm with id " + vmId);
		logger.log(Level.INFO, "deleting vm: resultMap " + resultMap);
		logger.log(Level.INFO, "deleting vm: result " + result);

		final CDOTransaction cdoCon = cdoSession.createTransaction();
		try {
			VirtualMachine vm = (VirtualMachine) cdoCon.getObject(vmId);
			logger.info("attempt to remove vm: " + vmId);
			CdoHelper.deleteVirtualMachine(vm);
			cdoSession.commitAndCloseConnection(cdoCon);
		} catch (Exception e) {
			cdoCon.rollback();
			logger.log(Level.SEVERE, "Could not delete virtual machine: " + vmId, e);
			throw new RuntimeManagementException("Cannot delete virtual machine", new RuntimeException("Cannot instantiate new virtual machine.", e));
		} finally {
			// just in case
			cdoSession.closeConnection(cdoCon);
		}
	}

	@Override
	public synchronized String result() {
		if(result == null || result.isEmpty()) {
			logger.severe("returning null or empty result string.");
		}
		return result;
	}

	///////////////////////////////////////////////////////////////////////
	// HELPER METHODS:

	/**
	 * Infers the Behaviour for the VM using the available
	 * BehaviourInferenceAlgorithms.
	 * 
	 * @param vm
	 *            The VM.
	 * @param architectureTypeRepo
	 *            The architecture type repository used. See
	 *            {@link IBehaviourInferenceAlgorithm#inferBehaviour(VirtualMachine, ArchitectureTypeRepository)}
	 *            .
	 */
	private void createBehaviourModel(VirtualMachine vm, ArchitectureTypeRepository architectureTypeRepo) {
		// FIXME: add that back in.
		// if(BehaviourInferenceAlgorithmClient.INSTANCE == null)
		// logger.severe("Cannot add behaviour model,
		// BehaviourInferenceAlgorithmClient.INSTANCE is null.");
		// IBehaviourInferenceAlgorithm service =
		// BehaviourInferenceAlgorithmClient.INSTANCE.getService();
		// if(service == null)
		// logger.severe("Cannot add behaviour model since no
		// IBehaviourInferenceAlgorithm service is registered");
		// service.inferBehaviour(vm, architectureTypeRepo);
	}

	private void prepareBootstrap2(StringHolder vmIdH, StringHolder hypervisorH, CDOID vmCdoId, CDOID cmpId) throws CommitException {
		CommitException old = null;
		for(int i = 0; i < 10; i++) {
			logger.info("preparing bootstrap, iteration: " + i);
			CommitException e = doPrepareBootstrap2(vmIdH, hypervisorH, vmCdoId, cmpId); 
			if(e == null) return;
			if(old != null && e.getCause() == null) e.initCause(old) ;
			old = e;
			try { Thread.sleep(1000);}
			catch(InterruptedException ie){}
		}
		throw old;
	}

	private CommitException doPrepareBootstrap2(StringHolder vmIdH, StringHolder hypervisorH, CDOID vmCdoId, CDOID cmpId) throws CommitException {
		String nodeId = null;
		String vmId = null;
		// final CDOView view = cdoSession.createView();
		final CDOTransaction cdoCon = cdoSession.createTransaction();
		try {
			long id = cdoCon.getSession().refresh();
			logger.info("session refresh returned: " + id);
			cdoCon.setBranchPoint(cdoCon.getSession().getBranchManager().getMainBranch(),
					CDOBranch.UNSPECIFIED_DATE);
			VirtualMachine vm = (VirtualMachine) cdoCon.getObject(vmCdoId);
			Hypervisor hypervisor = (Hypervisor) cdoCon.getObject(cmpId);
			if(hypervisor != null) {
				logger.info("setting computenode to " + hypervisor.getNode().getId());
				vm.setHypervisor(hypervisor);
				//I think it should work
				logger.info("setting storage location to the VMImage of the VM");
				vm.getVMImageInstance().getRootDisk().setStorageLocation(hypervisor.getNode().getStorageSpecifications().get(0));				
				nodeId = hypervisor.getNode().getId();
			} else {
				logger.info("hypervisor cannot be set");
			}
			vmId = vm.getId();			
			cdoSession.commitAndCloseConnection(cdoCon);
		} catch(CommitException e) {
			logger.log(Level.WARNING, "cannot commit hypervisor state", e);
			logger.log(Level.WARNING, "state before rollback");
			printFailedTransaction(cdoCon);
			cdoCon.rollback();
			logger.log(Level.WARNING, "state after rollback");
			printFailedTransaction(cdoCon);
			return e;
		} finally {
			cdoSession.closeConnection(cdoCon);
		}
		vmIdH.value = vmId;
		hypervisorH.value = nodeId;
		return null;
	}

	private void prepareBootstrap(StringHolder vmIdH, StringHolder computenodeH, CDOID vmCdoId, CDOID cmpId) throws CommitException {
		CommitException old = null;
		for(int i = 0; i < 10; i++) {
			logger.info("preparing bootstrap, iteration: " + i);
			CommitException e = doPrepareBootstrap(vmIdH, computenodeH, vmCdoId, cmpId);
			if(e == null) return;
			if(old != null && e.getCause() == null) e.initCause(old) ;
			old = e;
			try { Thread.sleep(1000);}
			catch(InterruptedException ie){}
		}
		throw old;
	}

	private CommitException doPrepareBootstrap(StringHolder vmIdH, StringHolder computenodeH, CDOID vmCdoId, CDOID cmpId) throws CommitException {
		String nodeId = null;
		String vmId = null;
		// final CDOView view = cdoSession.createView();
		final CDOTransaction cdoCon = cdoSession.createTransaction();
		try {
			long id = cdoCon.getSession().refresh();
			logger.info("session refresh returned: " + id);
			VirtualMachine vm = (VirtualMachine) cdoCon.getObject(vmCdoId);
			cdoCon.setBranchPoint(cdoCon.getSession().getBranchManager().getMainBranch(),
					CDOBranch.UNSPECIFIED_DATE);
			ComputeNode computenode = (ComputeNode) cdoCon.getObject(cmpId);
			if(computenode != null) {
				logger.info("setting hypervisor to " + computenode.getId());
				vm.setHypervisor(computenode.getHypervisor());
				nodeId = computenode.getId();
			} else {
				logger.info("hypervisor cannot be set");
			}
			vmId = vm.getId();			
			cdoSession.commitAndCloseConnection(cdoCon);
		} catch(CommitException e) {
			logger.log(Level.WARNING, "cannot commit hypervisor state", e);
			logger.log(Level.WARNING, "state before rollback");
			printFailedTransaction(cdoCon);
			cdoCon.rollback();
			logger.log(Level.WARNING, "state after rollback");
			printFailedTransaction(cdoCon);
			return e;
		} finally {
			cdoSession.closeConnection(cdoCon);
		}
		vmIdH.value = vmId;
		computenodeH.value = nodeId;
		return null;
	}

	private void printFailedTransaction(CDOTransaction cdoCon) {
		logger.info("failed transaction status: " + cdoCon.getViewID());
		logger.info("empty state: " + cdoCon.isEmpty());
		logger.info("dirty state: " + cdoCon.isDirty());
		logger.info("conflict state: " + cdoCon.hasConflict());
		logger.info("last commit: " + cdoCon.getLastCommitTime());
		logger.info("session id: " + cdoCon.getSessionID());
		logger.info("branch time stamp: " + cdoCon.getTimeStamp());
		logger.info("last update time: " + cdoCon.getLastUpdateTime());
		logger.info("branch: " + cdoCon.getBranch());
		logger.info("change data set: " + cdoCon.getChangeSetData());
		logger.info("commitables: " + cdoCon.getCommittables());
		logger.info("conflicts: " + cdoCon.getConflicts());
		logger.info("dirty: " + cdoCon.getDirtyObjects());
		logger.info("detached: " + cdoCon.getDetachedObjects());
		logger.info("new objects: " + cdoCon.getNewObjects());
		logger.info("view set: " + cdoCon.getViewSet());
		logger.info("branch and version: " + cdoCon.getBranch() + ":" + cdoCon.getTimeStamp());

	}

	private void postprocessBootstrap(CDOID vmCdoId) {
		final CDOView view = cdoSession.createView();
		try {
			VirtualMachine vm = (VirtualMachine) view.getObject(vmCdoId);
			resultMap = new HashMap<String,String>(vm.getInstantiationProperties().map());
			result = resultMap.get("output");
			logger.log(Level.WARNING, "reading RESULT: " + result);
		} finally {
			cdoSession.closeConnection(view);
		}
	}

	private void bootVM2(CDOID vmCdoId, CDOID hvId) throws RuntimeManagementException {
		// set to true to cater for exceptions happening before booting
		boolean bootingFailed = true;

		StringHolder vmIdH = new StringHolder();
		StringHolder computenodeH = new StringHolder();

		try {
			prepareBootstrap2(vmIdH, computenodeH, vmCdoId, hvId);
			bootingFailed = !doBootVM(vmIdH.value, computenodeH.value);
			if(!bootingFailed) {
				postprocessBootstrap(vmCdoId);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Cannot boot virtual machine!", e);
			logger.warning("Due to errors, will remove virtual machine model from CDOServer now.");
			throw new RuntimeManagementException("Cannot instantiate new virtual machine.", e);
		} finally { 
			logger.log(Level.INFO, "return status of boot vm is: " + !bootingFailed);
			if (bootingFailed) {
				logger.log(Level.INFO, "deleting vm now");
				deleteVmModel(vmCdoId);
				throw new RuntimeManagementException("Booting VM failed. VM Models deleted. Skip the rest.");
			}
		}
	}

	private void bootVM(CDOID vmCdoId, CDOID cmpId) throws RuntimeManagementException {
		// set to true to cater for exceptions happening before booting
		boolean bootingFailed = true;

		StringHolder vmIdH = new StringHolder();
		StringHolder computenodeH = new StringHolder();

		try {
			prepareBootstrap(vmIdH, computenodeH, vmCdoId, cmpId);
			bootingFailed = !doBootVM(vmIdH.value, computenodeH.value);
			if(!bootingFailed) {
				postprocessBootstrap(vmCdoId);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Cannot boot virtual machine!", e);
			logger.warning("Due to errors, will remove virtual machine model from CDOServer now.");
			throw new RuntimeManagementException("Cannot instantiate new virtual machine.", e);
		} finally { 
			logger.log(Level.INFO, "return status of boot vm is: " + !bootingFailed);
			if (bootingFailed) {
				logger.log(Level.INFO, "deleting vm now");
				deleteVmModel(vmCdoId);
			}
		}
	}

	/**
	 * 
	 * @param vm
	 * @param computenode
	 * @return success
	 */
	private boolean doBootVM(String vmId, String nodeId) {
		// The VMIController should boot the VM in the Cloud testbed. If
		// successful,
		// the VM model should be moved to the Hypervisor by the VMIController
		logger.log(Level.INFO, "Bootstrapping vm " + vmId + " on node " + nodeId);
		return VmiControllerClient.INSTANCE.getService().executePlacement(vmId, nodeId);
	}

	@Override
	public synchronized void start() throws RuntimeManagementException {

		CDOID vmCdoId = createAndRegisterVm();
		if(vmCdoId == null) {
			throw new RuntimeManagementException("could not create and/or register vm");
		}

		CDOID nodeId = null;
		try {
			nodeId = PlacementHelper.getInitialPlacement(cdoSession, vmCdoId);
		} catch(Exception e) {
			deleteVmModel(vmCdoId);
			throw new RuntimeManagementException(e);
		}

		if(nodeId == null) {
			logger.log(Level.WARNING, "about to start vm on an arbitrary node.");
		}
		
		// bootVM(vmCdoId, nodeId);}
		bootVM2(vmCdoId, nodeId);
		sendDataToCactoScale(vmCdoId);	
	}
}
