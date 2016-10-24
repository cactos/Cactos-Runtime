package eu.cactosfp7.runtimemanagement.impl.lifecycles;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.measure.quantity.DataAmount;
import javax.measure.quantity.Frequency;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.eclipse.emf.cdo.common.id.CDOID;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.cdo.util.CommitException;
import org.eclipse.emf.cdo.util.ConcurrentAccessException;
import org.eclipse.emf.cdo.view.CDOView;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.jscience.physics.amount.Amount;

import eu.cactosfp7.cactoopt.placementservice.IPlacementService;
import eu.cactosfp7.cactoopt.placementservice.PlacementResult;
import eu.cactosfp7.cactoopt.placementservice.PlacementResult.Status;
import eu.cactosfp7.cdosession.CactosCdoSession;
import eu.cactosfp7.cdosession.settings.CactosUser;
import eu.cactosfp7.cdosession.util.CdoHelper;
import eu.cactosfp7.cdosessionclient.CdoSessionClient;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.CoreFactory;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VMImage;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VM_State;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine;
import eu.cactosfp7.infrastructuremodels.physicaldc.architecturetype.ArchitectureType;
import eu.cactosfp7.infrastructuremodels.physicaldc.architecturetype.ArchitectureTypeRepository;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.ComputeNode;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.PhysicalDCModel;
import eu.cactosfp7.infrastructuremodels.util.modelbuilder.logical.VirtualMachineFactory;
import eu.cactosfp7.optimisationplan.ExecutionStatus;
import eu.cactosfp7.optimisationplan.OptimisationPlan;
import eu.cactosfp7.optimisationplan.OptimisationPlanRepository;
import eu.cactosfp7.runtimemanagement.impl.PlacementClient;
import eu.cactosfp7.runtimemanagement.impl.VmiControllerClient;
import eu.cactosfp7.runtimemanagement.service.RuntimeManagementException;
import eu.cactosfp7.runtimemanagement.util.Lifecycle;
import eu.cactosfp7.runtimemanagement.util.PropagateToChukwa;

public class Instantiation implements Lifecycle {

	/** Logger for this class. */
	private static final Logger logger = Logger.getLogger(Instantiation.class.getCanonicalName());
	private static final CoreFactory LOGICAL_FACTORY = CoreFactory.INSTANCE;

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

	public Instantiation(int _vcores, int _memory, int _diskspace, String _imageref, Map<String, String> _meta) {
		vcores = _vcores;
		memory = _memory;
		diskspace = _diskspace;
		imageref = _imageref;
		meta = _meta;
		cdoSession = CdoSessionClient.INSTANCE.getService().getCactosCdoSession(CactosUser.CACTOSCALE);
	}

	@Override
	public void start() throws RuntimeManagementException {
		// Opening transaction and read repositories
		final CDOTransaction cdoCon = cdoSession.createTransaction();
		String path = cdoSession.getLogicalModelPath();
		logger.info("logicalModelpath is " + path);
		LogicalDCModel logicalDcModel = (LogicalDCModel) cdoSession.getRepository(cdoCon, path);
		logger.info("Read logicalDcModel from cdoSession: " + logicalDcModel);
		ArchitectureTypeRepository architectureTypeRepository = (ArchitectureTypeRepository) cdoSession.getRepository(cdoCon, cdoSession.getArchitectureTypePath());
		logger.info("Read architectureTypeRepository from cdoSession: " + architectureTypeRepository);

		VirtualMachine vm = createVMModel(logicalDcModel, architectureTypeRepository, vcores, memory, diskspace, imageref, meta);
		createBehaviourModel(vm, architectureTypeRepository);
		try {
			cdoCon.commit();
		} catch (CommitException e) {
			logger.log(Level.SEVERE, "Could not commit VM information for submitted VM to Runtime Model Storage. The VM is not accepted for submission and will not be placed.", e);
			throw new RuntimeManagementException(e);
		}
		ComputeNode computenode = null;
		CDOID vmCdoId = null;
		CDOID nodeId = null;
		try{
			computenode = getInitialPlacement(cdoCon, vm);
			vmCdoId = vm.cdoID();
			nodeId = computenode.cdoID();
		}catch(Exception e){
			deleteVmModel(vm, cdoCon);
			throw e;
		}
		CDOTransaction cdoTxSecond = null;
		try {
			cdoSession.commitAndCloseConnection(cdoCon);
			// This happens in another transaction that is opened later -> we
			// need to work in a new transaction afterwards.
			cdoTxSecond = cdoSession.createTransaction();
			vm = (VirtualMachine) cdoTxSecond.getObject(vmCdoId);
			computenode = (ComputeNode) cdoTxSecond.getObject(nodeId);
			boolean bootDone = bootVM(vm, computenode);
			resultMap = vm.getInstantiationProperties().map();
			result = resultMap.get("output");
			if (!bootDone)
				deleteVmModel(vm, cdoTxSecond);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Cannot boot virtual machine!", e);
			logger.warning("Due to errors, will remove virtual machine model from CDOServer now.");
			deleteVmModel(vm, cdoTxSecond);
			throw new RuntimeManagementException("Cannot instantiate new virtual machine.", e);
		}
		String vmName = vm.getName();
		String applicationType = vm.getInputParameters().containsKey("applicationType") ? vm.getInputParameters().get("applicationType") : "-";
		String applicationTypeInstance = vm.getInputParameters().containsKey("applicationTypeInstance") ? vm.getInputParameters().get("applicationTypeInstance") : "-";
		String applicationComponent = vm.getInputParameters().containsKey("applicationComponent") ? vm.getInputParameters().get("applicationComponent") : "-";
		String applicationComponentInstance = vm.getInputParameters().containsKey("applicationComponentInstance") ? vm.getInputParameters().get("applicationComponentInstance") : "-";
		// store new vm in chukwa/hbase
		
		try {
			cdoSession.commitAndCloseConnection(cdoTxSecond);
		} catch (ConcurrentAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CommitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PropagateToChukwa.instantiation(vmName, applicationType, applicationTypeInstance, applicationComponent, applicationComponentInstance);
	}

	private void deleteVmModel(VirtualMachine vm, CDOTransaction cdoCon) throws RuntimeManagementException{
		try {
			// remove newly created virtual machine
			// vm.getVMImageInstance().getVolumes().clear();
			logger.info("remove vmImageInstance RootDisk");
			EcoreUtil.delete(vm.getVMImageInstance().getRootDisk());
			logger.info("remove vmImageInstance");
			EcoreUtil.delete(vm.getVMImageInstance());
			logger.info("remove vm");
			EcoreUtil.delete(vm);
			cdoCon.commit();
		} catch (CommitException e) {
			logger.log(Level.SEVERE, "Could not delete virtual machine!", e);
			throw new RuntimeManagementException("Cannot delete virtual machine", new RuntimeException("Cannot instantiate new virtual machine.", e));
		}

	}

	@Override
	public String result() {
		return result;
	}

	///////////////////////////////////////////////////////////////////////
	// HELPER METHODS:

	private VirtualMachine createVMModel(LogicalDCModel logicalDcModel,
		ArchitectureTypeRepository architectureTypeRepository,
		int vcores,
		int memory,
		int diskspace,
		String imageref,
		Map<String, String> inputParameters) {
		logger.info("Going to create VirtualMachine model ...");
		// convert vm parameters
		ArchitectureType architectureTypeX86 = CdoHelper.getArchitectureTypeByName(architectureTypeRepository, "x86");
		Amount<Frequency> amountFrequency = Amount.valueOf(0, Frequency.UNIT);
		Amount<DataAmount> amountMemory = Amount.valueOf(memory, SI.MEGA(NonSI.BYTE));
		Amount<DataAmount> amountDiskspace = Amount.valueOf(diskspace, SI.GIGA(NonSI.BYTE));
//		VMImage instantiatedImage = CdoHelper.getVMImageByName(logicalDcModel, imageref);
//		if (instantiatedImage == null) {
//			// if no image found, create a new one
//			instantiatedImage = createVmImage(logicalDcModel, imageref);
//		}
		
		// create new VMImage for each new VM to set root disk size
		String imageName = imageref + "-" + diskspace + "-"+(System.currentTimeMillis() / 1000L);
		VMImage instantiatedImage = createVmImage(logicalDcModel, imageName);

		// create virtual machine and store it in logigaldcmodel
		VirtualMachineFactory vmf = new VirtualMachineFactory();
		VirtualMachine vm = vmf.createUnassignedVirtualMachine(logicalDcModel, vcores, amountFrequency, architectureTypeX86, amountMemory, amountDiskspace, instantiatedImage);
		vm.setState(VM_State.NEW);
		vm.getVMImageInstance().getRootDisk().setCapacity(amountDiskspace);
		vm.getInputParameters().putAll(inputParameters);

		logger.info("VirtualMachine model created: " + vm);
		return vm;
	}

	// TODO use the Builder from model updater instead the following method
	private VMImage createVmImage(LogicalDCModel logicalDCModel, String imageref) {
		VMImage _vmImage = LOGICAL_FACTORY.createVMImage();
		_vmImage.setLogicalDCModel(logicalDCModel);
		_vmImage.setName(imageref);
		// _vmImage.setId(imageref); DON'T SET ID, use auto-generated IDs
		return _vmImage;
	}

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

	private ComputeNode getInitialPlacement(CDOView view, VirtualMachine vm) throws RuntimeManagementException{
		IPlacementService service = PlacementClient.INSTANCE.getService();

		PlacementResult placementResult = service.determinePlacement(vm.getId());
		if (Status.SUCCESSFUL.equals(placementResult.getStatus())) {
			// do the placement
			String nodeId = placementResult.getUuid();
			PhysicalDCModel physicalDCModel = (PhysicalDCModel) cdoSession.getRepository(view, cdoSession.getPhysicalModelPath());
			logger.log(Level.INFO, "PlacementService decided to place VM on host " + nodeId);
			ComputeNode cn = CdoHelper.getComputeNodeById(physicalDCModel, nodeId);
			return cn;
		} else if (Status.FAILED_CONCURRENT_OPTIMISATION.equals(placementResult.getStatus())) {
			// wait for all plans to be finished and retry
			OptimisationPlanRepository planRepository = (OptimisationPlanRepository) cdoSession.getRepository(view, cdoSession.getOptimisationPlanPath());
			// if no planRepo found, quit
			if (planRepository == null) {
				logger.severe("OptimisationPlanRepository not found on CDO server.");
				cdoSession.closeConnection(view);
				return null;
			}
			int maxTries = 10;
			int tries = 0;
			while (tries <= maxTries) {
				boolean planInExecution = false;
				// Look for Plans with state IN_EXECUTION
				for (OptimisationPlan plan : planRepository.getOptimisationPlans()) {
					if (plan.getExecutionStatus() == ExecutionStatus.IN_EXECUTION) {
						// if any plan is in execution, quit
						planInExecution = true;
					}
				}
				if (!planInExecution) {
					// RETRY
					return getInitialPlacement(view, vm);
				}
				// Sleep a few seconds
				try {
					Thread.sleep(10 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				tries++;
			}
		} else if (Status.FAILED_IMPOSSIBLE.equals(placementResult.getStatus())) {
			// give up and send try again later to the client
			logger.log(Level.WARNING, "PlacementService cannot suggest a host to place VM (FAILED_IMPOSSIBLE)!");
			throw new RuntimeManagementException("PlacementService cannot suggest a host to place VM (FAILED_IMPOSSIBLE)!");
		}
		logger.log(Level.WARNING, "PlacementService not found, returned nothing or invalid state");
		throw new RuntimeManagementException("PlacementService not found, returned nothing or invalid state");
		//return null;
	}

	private boolean bootVM(VirtualMachine vm, ComputeNode computenode) {
		// The VMIController should boot the VM in the Cloud testbed. If
		// successful,
		// the VM model should be moved to the Hypervisor by the VMIController
		String nodeId = computenode == null ? null : computenode.getId();
		return VmiControllerClient.INSTANCE.getService().executePlacement(vm.getId(), nodeId);
	}

}
