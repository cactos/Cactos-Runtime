package eu.cactosfp7.infrastructuremodels.builder.applicationmodelinstance.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.EcoreUtil.Copier;
import org.palladiosimulator.commons.eclipseutils.FileHelper;

import eu.cactosfp7.infrastructuremodels.builder.applicationmodelinstance.util.Constants;
import eu.cactosfp7.infrastructuremodels.builder.applicationmodelinstance.util.ModelQueries;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.ApplicationFactory;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.ComposedVM;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.ComposedVMImage;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.ServiceInterface;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.ServiceProvidedRole;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.ServiceRequiredRole;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.WhiteBoxApplicationInstance;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.WhiteBoxApplicationTemplate;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.WhiteBoxUserBehaviourInstance;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.WhiteBoxUserBehaviourTemplate;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.WhiteBoxVMBehaviour;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.WhiteBoxVMImageBehaviour;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.entity.ProvidedRole;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.entity.RequiredRole;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.entity.Role;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.entity.util.EntitySwitch;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.impl.ApplicationPackageImpl;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.util.ApplicationSwitch;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.util.NonIdCopier;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.Flavour;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VMImage;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualDisk;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.impl.CorePackageImpl;
import eu.cactosfp7.infrastructuremodels.physicaldc.architecturetype.ArchitectureType;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.ProcessingUnitSpecification;
import eu.cactosfp7.optimisationplan.OptimisationActionStep;
import eu.cactosfp7.optimisationplan.ScaleIn;
import eu.cactosfp7.optimisationplan.ScaleOut;

public class ApplicationModelInstanceLoaderAndBuilder {
    
    private ApplicationFactory applicationFactory = ApplicationFactory.INSTANCE;
    private ApplicationPackageImpl applicationPackage = ApplicationPackageImpl.eINSTANCE;
    private CorePackageImpl logicalCorePackage = CorePackageImpl.eINSTANCE;
    private eu.cactosfp7.infrastructuremodels.physicaldc.core.impl.CorePackageImpl physicalCorePackage 
                = eu.cactosfp7.infrastructuremodels.physicaldc.core.impl.CorePackageImpl.eINSTANCE;

    public WhiteBoxApplicationTemplate getApplicationTemplateByName(LogicalDCModel ldcModel, String applicationName) {
	Collection<WhiteBoxApplicationTemplate> templates = EcoreUtil.getObjectsByType(ldcModel.getApplicationTemplates(), 
        	applicationPackage.getWhiteBoxApplicationTemplate());
	for (WhiteBoxApplicationTemplate t : templates) {
	    if (t.getName().equals(applicationName)) {
		return t;
	    }
	}
	return null;
    }

    public void createModelInstance(LogicalDCModel ldcModel, String applicationName,
            Map<String, String> vmName2compName, String applicationInstanceId) {
        // Check if the template has already been loaded
        WhiteBoxApplicationTemplate template = this.getApplicationTemplateByName(ldcModel, applicationName);
        if (template == null) {
            // Get any of the VMs
            VirtualMachine curVm = ModelQueries.getVmByName(ldcModel, vmName2compName.keySet().iterator().next());
            // We pass any of the ArchitectureTypes as we assume the type to be homogeneous for the application
            template = this.loadExistingModelTemplateByName(ldcModel, applicationName, 
                    curVm.getHypervisor().getNode().getCpuSpecifications().get(0).getArchitectureType());
        }
        
        // Create instance
        WhiteBoxApplicationInstance whiteBoxApplicationInstance = applicationFactory.createWhiteBoxApplicationInstance();
        whiteBoxApplicationInstance.setLogicalDCModel(ldcModel);
        whiteBoxApplicationInstance.setApplicationTemplate(template);
        whiteBoxApplicationInstance.setId(applicationInstanceId);
        for(WhiteBoxUserBehaviourTemplate curUserBehaviourTemplate : template.getWhiteBoxUserBehaviourTemplate()) {
            WhiteBoxUserBehaviourInstance inst = applicationFactory.createWhiteBoxUserBehaviourInstance();
            
            EcoreUtil.Copier nonIdCopier = new NonIdCopier(false);
            inst.getUserBehaviours().addAll(nonIdCopier.copyAll(curUserBehaviourTemplate.getUserBehaviours()));
            inst.setName(curUserBehaviourTemplate.getName());
            nonIdCopier.copyReferences();
            whiteBoxApplicationInstance.getWhiteBoxUserBehaviourInstance().add(inst);     
        }
        
        /* in the following we assume that the VM has already been created and is available in ldcModel 
         * and we simply attach the behaviour model to it.
         */
        createWhiteBoxModelsForVms(ldcModel, vmName2compName, whiteBoxApplicationInstance);
    }

    /**
     * Instantiates the white-box behaviour models of a set of VMs.
     * @param ldcModel The Logical DC Model in which the models are added.
     * @param vmName2compName A map that maps a set of VM names to component names
     * @param whiteBoxApplicationInstance The application instance to which the component instances are added.
     */
    private void createWhiteBoxModelsForVms(LogicalDCModel ldcModel, Map<String, String> vmName2compName,
            WhiteBoxApplicationInstance whiteBoxApplicationInstance) {
        for(Entry<String, String> curEntry: vmName2compName.entrySet()) {
            for(ComposedVMImage curComposedVmImage 
                    : ((WhiteBoxApplicationTemplate)whiteBoxApplicationInstance.getApplicationTemplate()).getComposedVMImages()) {
            	String vmName = curEntry.getKey();
            	String compName = curEntry.getValue();
                if(compName.equals(curComposedVmImage.getName())) {
                    ComposedVM composedVM = applicationFactory.createComposedVM();
                    composedVM.setApplicationInstance(whiteBoxApplicationInstance);
                    composedVM.setComposedVMImage(curComposedVmImage);
                    VirtualMachine curVm = ModelQueries.getVmByName(ldcModel, vmName);
                    composedVM.setVirtualMachine(curVm);
                    WhiteBoxVMImageBehaviour vmImageBehaviour = curComposedVmImage.getVmImageBehaviour();
                    WhiteBoxVMBehaviour vmBehaviour = applicationFactory.createWhiteBoxVMBehaviour();
                    vmBehaviour.setName("Behaviour of " + curVm.getName());
                    vmBehaviour.setVirtualMachine(curVm);
                    vmBehaviour.setVmImageBehaviour(vmImageBehaviour);
                    EcoreUtil.Copier nonIdCopier = new NonIdCopier(false);
                    vmBehaviour.getServiceEffects().addAll(nonIdCopier.copyAll(vmImageBehaviour.getServiceEffects()));
                    nonIdCopier.copyReferences();
                    curVm.setRuntimeApplicationModel(vmBehaviour);
                }
            }
        }
    }

    /**
     * Get and load an existing White Box Application Template by name.
     * @param ldcModel The model in which the template is loaded.
     * @param applicationName The name of the application template.
     * @param cpuArchitecture The architecture needed to run the application.
     *  For now it is assumed that it is homogeneous.
     * @return The loaded template.
     */
    public WhiteBoxApplicationTemplate loadExistingModelTemplateByName(LogicalDCModel ldcModel,
            String applicationName, ArchitectureType cpuArchitecture) {
        List<WhiteBoxApplicationTemplate> templates = this.getWhiteBoxApplicationTemplates();
        WhiteBoxApplicationTemplate selectedTemplate = null;
        for(WhiteBoxApplicationTemplate curTemplate : templates) {
            if(curTemplate.getName().equals(applicationName)) {
                selectedTemplate = curTemplate;
            }
        }
        EcoreUtil.Copier copier = new Copier(true);
        WhiteBoxApplicationTemplate copiedTemplate = (WhiteBoxApplicationTemplate) copier.copy(selectedTemplate);

        for(ComposedVMImage curImage : selectedTemplate.getComposedVMImages()) {
            // Copy service interfaces
            EList<ProvidedRole> providedRoles = curImage.getProvidedRoles();
            Collection<ServiceInterface> missingInterfaces = new ArrayList<ServiceInterface>();
            missingInterfaces.addAll(getMissingInterfaces(ldcModel, providedRoles));
            EList<RequiredRole> requiredRoles = curImage.getRequiredRoles();
            Collection<ServiceInterface> newInterfaceCandidates = getMissingInterfaces(ldcModel, requiredRoles);
            newInterfaceCandidates.removeAll(missingInterfaces);
            missingInterfaces.addAll(newInterfaceCandidates);
            ldcModel.getServiceInterfaces().addAll(copier.copyAll(missingInterfaces));
            
            // Copy image representation
            if(!contains(ldcModel, curImage.getVmImageBehaviour().getVmImage())) {
                ldcModel.getVolumesAndImages().add((VirtualDisk) copier.copy(curImage.getVmImageBehaviour().getVmImage()));
            }
            
            // Copy flavour information
            if(!contains(ldcModel, curImage.getVmImageBehaviour().getDefaultFlavour())) {
                ldcModel.getFlavours().add((Flavour) copier.copy(curImage.getVmImageBehaviour().getDefaultFlavour()));
            }
        }
        
        ldcModel.getApplicationTemplates().add(copiedTemplate);
        copier.copyReferences();
        
        // replace architecture type with type used in existing Runtime Model
        for (ComposedVMImage composedVMImage : copiedTemplate.getComposedVMImages()) {
            for(ProcessingUnitSpecification curResource : EcoreUtil.<ProcessingUnitSpecification>getObjectsByType(composedVMImage.getVmImageBehaviour().getReferenceResourceSpecifications(), physicalCorePackage.getProcessingUnitSpecification())) {
                curResource.setArchitectureType(cpuArchitecture);                
            }
            if(!composedVMImage.getVmImageBehaviour().getDefaultFlavour().getId().equals(cpuArchitecture.getId())) {
                composedVMImage.getVmImageBehaviour().getDefaultFlavour().setArchitectureType(cpuArchitecture);                
            }
        }
        return copiedTemplate;
    }
    
    private boolean contains(LogicalDCModel ldcModel, Flavour defaultFlavour) {
        for(Flavour flavour : ldcModel.getFlavours()) {
            if(flavour.getId().equals(defaultFlavour.getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean contains(LogicalDCModel ldcModel, VirtualDisk image) {
        for(VirtualDisk curImage : ldcModel.getVolumesAndImages()) {
            if(curImage instanceof VMImage && curImage.getId().equals(image.getId())) {
                return true;
            }
        }
        return false;
    }

    private <T extends Role> Collection<ServiceInterface> getMissingInterfaces(final LogicalDCModel ldcModel, List<T> roles) {
        final Map<String, ServiceInterface> missingInterfaces = new HashMap<String, ServiceInterface>();
        for(final Role role : roles) {
            new ApplicationSwitch<Void>() {
                public Void caseServiceProvidedRole(ServiceProvidedRole provRole) {
                    ServiceInterface curInterface = provRole.getServiceInterface();
                    if(!contains(ldcModel, curInterface) && !missingInterfaces.containsKey(curInterface.getId())) {
                        missingInterfaces.put(curInterface.getId(), curInterface);
                    }
                    return null;
                };
                public Void caseServiceRequiredRole(ServiceRequiredRole reqRole) {
                    ServiceInterface curInterface = reqRole.getServiceInterface();
                    if(!contains(ldcModel, curInterface) && !missingInterfaces.containsKey(curInterface.getId())) {
                        missingInterfaces.put(curInterface.getId(), curInterface);
                    }
                    return null;                    
                };
            }.doSwitch(role);
        }
        return missingInterfaces.values();
    }
    

    private boolean contains(LogicalDCModel ldcModel, ServiceInterface curInterface) {
        for(ServiceInterface itInterface : ldcModel.getServiceInterfaces()) {
            if(itInterface.getId().equals(curInterface.getId())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Gets the set of available White-Box Application Templates. Must be stored in Constants.PLUGIN_TEMPLATE_PATH.
     * @return The set of available White-Box Application Templates.
     */
    public List<WhiteBoxApplicationTemplate> getWhiteBoxApplicationTemplates() {
        ResourceSet resourceSet = new ResourceSetImpl();

        final URI[] uris = FileHelper.getURIs(Constants.PLUGIN_TEMPLATE_PATH, Constants.LDC_FILE_EXTENSION);
        List<WhiteBoxApplicationTemplate> availableTemplates = new ArrayList<WhiteBoxApplicationTemplate>();
        for (URI uri : uris) {
            Resource res = resourceSet.getResource(uri, true);
            Collection<LogicalDCModel> models = EcoreUtil.getObjectsByType(res.getContents(), logicalCorePackage.getLogicalDCModel());
            for (LogicalDCModel model : models) {
        	availableTemplates.addAll(EcoreUtil.<WhiteBoxApplicationTemplate>getObjectsByType(model.getApplicationTemplates(), 
        		applicationPackage.getWhiteBoxApplicationTemplate()));
            }
        }
        EcoreUtil.resolveAll(resourceSet);
        return availableTemplates;
    }

    public void scaleOutModelInstance(LogicalDCModel ldcModel, String applicationInstanceId,
            Map<String, String> vmName2compName) {
        WhiteBoxApplicationInstance modelInstance = this.getModelInstanceById(ldcModel, applicationInstanceId);
        createWhiteBoxModelsForVms(ldcModel, vmName2compName, modelInstance);
        
    }

    private WhiteBoxApplicationInstance getModelInstanceById(LogicalDCModel ldcModel, String applicationInstanceId) {
        Collection<WhiteBoxApplicationInstance> objectsByType = EcoreUtil.<WhiteBoxApplicationInstance>getObjectsByType(ldcModel.getApplicationInstances(), applicationPackage.getWhiteBoxApplicationInstance()); 
        for(WhiteBoxApplicationInstance curInstance : objectsByType) {
            if(curInstance.getId().equals(applicationInstanceId)) {
                return curInstance;
            }
        }
        return null;
    }

    public void scaleInModelInstance(LogicalDCModel ldcModel, String applicationInstanceId,
            Map<String, String> vmName2compName) {
        WhiteBoxApplicationInstance modelInstance = this.getModelInstanceById(ldcModel, applicationInstanceId);
        
        List<ComposedVM> vmsToDelete = new LinkedList<>();
        
        for(ComposedVM curVM : modelInstance.getComposedVMs()) {
            String compName = vmName2compName.get(curVM.getVirtualMachine().getName());
            // If the running VM component is to be removed, remove it.
            if(compName != null) {
                vmsToDelete.add(curVM);
                // Don't remove the VM here. VM is removed via hijacking the delete command to the Cloud
                //EcoreUtil.remove(curVM.getVirtualMachine());
            }
        }
        
        for(ComposedVM vmToDelete : vmsToDelete ) {
        	EcoreUtil.remove(vmToDelete);
        }
    }
    

    public String scaleActionToAppInstanceId(OptimisationActionStep action) {
    	if(action instanceof ScaleIn)
    		return ((ScaleIn)action).getLoadBalancerInstance().getApplicationInstance().getId();
    	if(action instanceof ScaleOut)
    		return ((ScaleOut)action).getLoadBalancerInstance().getApplicationInstance().getId();
    	return null;
    }    
    
    public String scaleActionToComponentName(OptimisationActionStep action) {
    	if(action instanceof ScaleIn)
    		return ((ScaleOut)action).getScalingConnector().getServiceProvidedRole().getInterfaceProvidingEntity().getName();
    	if(action instanceof ScaleOut)
    		return ((ScaleOut)action).getScalingConnector().getServiceProvidedRole().getInterfaceProvidingEntity().getName();
    	return null;    	
        
    }
    
}
