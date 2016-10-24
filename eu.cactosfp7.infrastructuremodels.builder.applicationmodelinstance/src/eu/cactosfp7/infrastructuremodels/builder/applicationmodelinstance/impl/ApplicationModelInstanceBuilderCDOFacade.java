package eu.cactosfp7.infrastructuremodels.builder.applicationmodelinstance.impl;

import java.util.Map;

import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.cdo.util.CommitException;

import eu.cactosfp7.cdosession.CactosCdoSession;
import eu.cactosfp7.cdosession.settings.CactosUser;
import eu.cactosfp7.cdosessionclient.CdoSessionClient;
import eu.cactosfp7.infrastructuremodels.builder.applicationmodelinstance.ApplicationModelInstanceBuilder;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;
import eu.cactosfp7.optimisationplan.OptimisationActionStep;
import eu.cactosfp7.optimisationplan.ScaleIn;
import eu.cactosfp7.optimisationplan.ScaleOut;

public class ApplicationModelInstanceBuilderCDOFacade implements ApplicationModelInstanceBuilder {

    private ApplicationModelInstanceLoaderAndBuilder builder;
    private CactosCdoSession cactosCdoSession;
    
    public ApplicationModelInstanceBuilderCDOFacade() {
        this.builder = new ApplicationModelInstanceLoaderAndBuilder();
    }
    
    @Override
    public void createModelInstance(String applicationName, Map<String, String> vmName2compName,
            String applicationInstanceId) {
    	CDOTransaction cdoTransaction = getCdoTransaction();
        LogicalDCModel ldcModel = getCurrentLdcModel(cdoTransaction);
        builder.createModelInstance(ldcModel, applicationName, vmName2compName, applicationInstanceId);
        try {
			cactosCdoSession.commitAndCloseConnection(cdoTransaction);
		} catch (CommitException e) {
			e.printStackTrace();
		}
    }

    /** 
     * Fetch the Logical DC Model from CDO.
     * @return The current Logical Data Centre Model.
     */
    private LogicalDCModel getCurrentLdcModel(CDOTransaction cdoTransaction) {
		// Read LogicalDCModel from CDO Server
		LogicalDCModel ldcModel = (LogicalDCModel) cactosCdoSession.getRepository(cdoTransaction,
				cactosCdoSession.getLogicalModelPath());

        return ldcModel;
    }

    /**
     * Open a new transaction to the CDO server. 
     * TAKE CARE TO COMMIT+CLOSE IT AFTER USE!
     * @return CDOTransaction
     */
    private CDOTransaction getCdoTransaction(){
    	cactosCdoSession = CdoSessionClient.INSTANCE.getService()
				.getCactosCdoSession(CactosUser.CACTOSCALE);
		return cactosCdoSession.createTransaction();
    }
    
    @Override
    public void updateModelInstance(String applicationInstanceId, UpdateAction action,
            Map<String, String> vmName2compName) {
    	CDOTransaction cdoTransaction = getCdoTransaction();
        LogicalDCModel ldcModel = getCurrentLdcModel(cdoTransaction);
        switch(action) {
        case ADD:
            builder.scaleOutModelInstance(ldcModel, applicationInstanceId, vmName2compName);
            break;
        case REMOVE:
            builder.scaleInModelInstance(ldcModel, applicationInstanceId, vmName2compName);
            break;
        default:
            throw new IllegalStateException("Unknown/unhandled type of update operation: " + action);         
        }
        try {
			cactosCdoSession.commitAndCloseConnection(cdoTransaction);
		} catch (CommitException e) {
			e.printStackTrace();
		}
    }

    @Override
    public String scaleActionToAppInstanceId(OptimisationActionStep action) {
    	if(action instanceof ScaleIn || action instanceof ScaleOut){
    		return this.builder.scaleActionToAppInstanceId(action);
    	}else{
    		throw new RuntimeException("Provided action is neither ScaleIn nor ScaleOut action!");
    	}
    }

    @Override
    public String scaleActionToComponentName(OptimisationActionStep action) {
    	if(action instanceof ScaleIn || action instanceof ScaleOut){
        	return this.builder.scaleActionToComponentName(action);
		}else{
			throw new RuntimeException("Provided action is neither ScaleIn nor ScaleOut action!");
		}
    }

}
