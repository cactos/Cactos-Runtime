package eu.cactosfp7.infrastructuremodels.builder.applicationmodelinstance.tests;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.Assert;
import org.junit.Test;

import eu.cactosfp7.infrastructuremodels.builder.applicationmodelinstance.impl.ApplicationModelInstanceLoaderAndBuilder;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.WhiteBoxApplicationTemplate;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.CoreFactory;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;
import eu.cactosfp7.infrastructuremodels.physicaldc.architecturetype.ArchitectureType;
import eu.cactosfp7.infrastructuremodels.physicaldc.architecturetype.ArchitectureTypeRepository;
import eu.cactosfp7.infrastructuremodels.physicaldc.architecturetype.ArchitecturetypeFactory;

public class ModelTemplateLoadingTest {
    @Test
    public void test() {
        ApplicationModelInstanceLoaderAndBuilder builder = new ApplicationModelInstanceLoaderAndBuilder();
        ResourceSet resourceSet = new ResourceSetImpl();
        File logicalFile = null;
        File architectureFile = null;
        try {
            logicalFile = File.createTempFile("testModel", ".logical");
            architectureFile = File.createTempFile("testModel", ".architecture");
        } catch (IOException e) {
            Assert.fail("Could not write to temporary files.\n" + e.toString());
        }
        Resource resourceLogical = resourceSet.createResource(URI.createFileURI(logicalFile.getAbsolutePath()));
        Resource resourceArchitecture = resourceSet.createResource(URI.createFileURI(architectureFile.getAbsolutePath()));
        LogicalDCModel ldcModel = CoreFactory.INSTANCE.createLogicalDCModel();
        resourceLogical.getContents().add(ldcModel);
        ArchitecturetypeFactory architectureFactory = ArchitecturetypeFactory.INSTANCE;
        ArchitectureTypeRepository architectureRepository = architectureFactory .createArchitectureTypeRepository();
        resourceLogical.getContents().add(architectureRepository);
        ArchitectureType x86Architecture = architectureFactory.createArchitectureType();
        x86Architecture.setArchitectureTypeRepository(architectureRepository);
        x86Architecture.setName("x86");
        resourceArchitecture.getContents().add(architectureRepository);
        for(WhiteBoxApplicationTemplate curTemplate : builder.getWhiteBoxApplicationTemplates()) {
            builder.loadExistingModelTemplateByName(ldcModel, curTemplate.getName(), x86Architecture);
        }
        EcoreUtil.resolveAll(ldcModel);
        try {
            ldcModel.eResource().save(Collections.EMPTY_MAP);
            architectureRepository.eResource().save(Collections.EMPTY_MAP);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
