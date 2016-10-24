package eu.cactosfp7.runtimemanagement.impl;

import java.util.Map;

import eu.cactosfp7.infrastructuremodels.logicaldc.core.Flavour;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VMImage;
import eu.cactosfp7.optimisationplan.OptimisationStep;
import eu.cactosfp7.runtimemanagement.IRuntimeManagement;

/**Manages CACTOS models and incoming and exiting Virtual Machines and Applications.
 * @author hgroenda
 *
 */
public class RuntimeManagement implements IRuntimeManagement {
	/** Factory to create optimisation plans. */
//	private static final OptimisationplanFactory opf = OptimisationplanFactory.eINSTANCE;
	
	/**Creates a StartVmAction step in an optimisation plan.
	 * @param flavour The Flavour to use for instantiation.
	 * @param vmImage The VM Image to use as root image.
	 * @param inputParameters Input parameters.
	 * @return Resulting optimisation step(s) to start the image.
	 */
	protected OptimisationStep createStartVMStep(Flavour flavour, VMImage vmImage, Map<String, String> inputParameters) {
		/* TODO Create Virtual Machine description in CACTOS model 
		 * 	create VM instance in list of unassigned VMs 
		 * 	infer behaviour for VM - if Black- or Grey-Box behaviour then create a new corresponding application instance in the logical DC model else do not create an application instance.
		 * 	get suggested placement
		 * 	--realized by VmPlacementAction: 
		 * 	---create virtual disk and overlay (local or remote differs) for suggested placement
		 * 	---assign according to suggested placement
		 */
//		OptimisationStep steps = opf.createSequentialSteps();
//		VmPlacementAction vmpa = opf.createVmPlacementAction();
//		vmpa.setVmImage(vmImage);
//		vmpa.setTargetHost(placementHost);
//		vmpa.setUnassignedVirtualMachine(vm);
//		steps.getSequentialSteps().add(vmpa);
//		steps.getSequentialSteps().add(createStartVMStep(vm));
//		return steps;
		return null;
	}
	
	@Override
	public String startVM(String flavourRef, String vmImageRef, Map<String, String> inputParameters) {
//		OptimisationPlan plan = opf.createOptimisationPlan();
//		// TODO Resolve information in method parameters
//		StartVmAction act = createStartVMStep(flavour, vmImage, inputParameters);
//		plan.setOptimisationStep(act);
//		/* TODO
//		 * store plan in model
//		 * explicit call to VMIController.execute(plan.getId())
//		 */
//		return act.getStartedVm().getId();
//		/* Code used before
//			Lifecycle init = new Instantiation(vcores, memory, diskspace, imageref, meta);
//			init.start();
//			return init.result();
//			return null;
//			return null;
//		*/
		return null;
	}

	@Override
	public String startApplication(String appRef, Map<String, String> inputParameters) {
//		OptimisationPlan plan = opf.createOptimisationPlan();
//		// TODO Resolve information in method parameters
//		ApplicationTemplate template;
//		return new ApplicationSwitch<String>() {
//			public String caseBlackBoxApplicationTemplate(BlackBoxApplicationTemplate object) {
//				// TODO create instance of application
//				// TODO create VM as described above in #startVM
//			};
//			public String caseBlackBoxApplicationTemplate(GreyBoxApplicationTemplate object) {
//				// TODO create instance of application
//				// TODO create VM as described above in #startVM 
//			};
//			public String caseBlackBoxApplicationTemplate(WhiteBoxApplicationTemplate object) {
//				// TODO create instance of application
//			/* TODO implement oriented at WhiteBoxDemoModel#instantiateApplicationTemplate
//			 * Note to create all StartVmAction and ConnectVmAction in the correct order to enable 
//			 * forwarding of user requests along the control-flow between the new instances.
//			 */
//			};
//		}.doSwitch(template);
		return null;
	}

	@Override
	public boolean stopVM(String vmRef, Map<String, String> inputParameters) {
		// TODO Auto-generated method stub with legacy code easing refactoring. Take care of changed meaning of parameters.
		/* Outdate code
		Lifecycle init = new Deletion(vmName, meta);
		init.start();
		return init.result();
		return false;
		 */
		return false;
	}

	@Override
	public boolean stopApplication(String appInstanceRef, Map<String, String> inputParameters) {
		// TODO stop all VMs of application instance and remove application instance from model
		return false;
	}
	
}
