package eu.cactosfp7.infrastructuremodels.builder.applicationmodelload.impl;

import java.util.Map;
import java.util.Map.Entry;

import javax.measure.unit.SI;

import org.apache.log4j.Logger;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.jscience.physics.amount.Amount;

import eu.cactosfp7.cdosession.CactosCdoSession;
import eu.cactosfp7.infrastructuremodels.builder.applicationmodelinstance.util.ModelQueries;
import eu.cactosfp7.infrastructuremodels.builder.applicationmodelload.ApplicationModelLoadBuilder;
import eu.cactosfp7.infrastructuremodels.load.logical.LogicalFactory;
import eu.cactosfp7.infrastructuremodels.load.logical.LogicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.logical.RequestArrivalRateMeasurement;
import eu.cactosfp7.infrastructuremodels.load.logical.ResponseArrivalRateMeasurement;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.ComposedVM;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.VMImageConnector;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.WhiteBoxApplicationInstance;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.WhiteBoxApplicationTemplate;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.WhiteBoxVMImageBehaviour;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.impl.ApplicationPackageImpl;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine;

public class ApplicationModelLoadBuilderUtil implements ApplicationModelLoadBuilder {

	private LogicalFactory measurementFactory;
	private ApplicationPackageImpl applicationPackage = ApplicationPackageImpl.eINSTANCE;
	private CactosCdoSession cactosCdoSession;
	private final static Logger logger = Logger.getLogger(ApplicationModelLoadBuilderUtil.class);

	public ApplicationModelLoadBuilderUtil() {
		measurementFactory = LogicalFactory.INSTANCE;
	}

	@Override
	public void updateLoadBalancerMetric(CactosCdoSession cactosCdoSession, CDOTransaction cdoTransaction, String loadBalancerGroupName, String vmName, Map<LoadBalancerMetric, Double> measurements) {
		this.cactosCdoSession = cactosCdoSession;
		for (Entry<LoadBalancerMetric, Double> curEntry : measurements.entrySet()) {
			LoadBalancerMetric curMetric = curEntry.getKey();
			LogicalDCModel ldcModel = this.getLogicalDCModel(cdoTransaction);
			if (ldcModel == null)
				return;
			LogicalLoadModel loadModel = this.getLogicalLoadModel(cdoTransaction);
			switch (curMetric) {
			// TODO Christopher check whether this matches your intended metric
			// settings.
			case TWO_XX_PER_SECOND:
				lookUpOrCreateArrivalMeasurement(loadModel, ldcModel, loadBalancerGroupName, vmName, curEntry.getValue());
				break;
			case SESSION_PER_SECOND:
				lookUpOrCreateResponseRateMeasurement(loadModel, ldcModel, loadBalancerGroupName, vmName, curEntry.getValue());
				break;
			default:
				throw new IllegalStateException("Can not handle metric of type " + curMetric.name());
			}
		}
	}

	private void lookUpOrCreateResponseRateMeasurement(LogicalLoadModel loadModel, LogicalDCModel ldcModel, String loadBalancerGroupName, String vmName, Double value) {
		ResponseArrivalRateMeasurement responseArrivalRateMeasurement = lookUpResponseRateMeasurement(loadModel, loadBalancerGroupName, vmName);
		if (responseArrivalRateMeasurement == null) {
			responseArrivalRateMeasurement = this.measurementFactory.createResponseArrivalRateMeasurement();
			VirtualMachine curVm = ModelQueries.getVmByName(ldcModel, vmName);
			if (curVm == null) {
				logger.debug("The VM doesn't exist in the models.");
				return;
			} else {
				if (curVm.getRuntimeApplicationModel() == null) {
					logger.debug("There are no WhiteBoxApplication models.");
					return;
				}
			}
			responseArrivalRateMeasurement.setObservedVmImageConnector(getVmImageConnector(curVm, loadBalancerGroupName, vmName));
			setApplicationInstance(ldcModel, vmName, responseArrivalRateMeasurement);
			responseArrivalRateMeasurement.setLogicalLoadModel(loadModel);
		}
		logger.debug("The responseArrivalRateMeasurement exists " + responseArrivalRateMeasurement + " and the value for it is " + value);
		if (value != null) {
			responseArrivalRateMeasurement.setArrivalRate(Amount.valueOf(value, SI.HERTZ));
		}

	}

	private ResponseArrivalRateMeasurement lookUpResponseRateMeasurement(LogicalLoadModel loadModel, String loadBalancerGroupName, String vmName) {
		for (ResponseArrivalRateMeasurement curMeasurement : loadModel.getResponseArrivalRateMeasurement()) {
			if (curMeasurement.getObservedVmImageConnector().getId().equals(loadBalancerGroupName)) {
				return curMeasurement;
			}
		}
		return null;
	}

	private void lookUpOrCreateArrivalMeasurement(LogicalLoadModel loadModel, LogicalDCModel ldcModel, String loadBalancerGroupName, String vmName, Double value) {
		RequestArrivalRateMeasurement arrivalRateMeasurement = lookUpArrivalRateMeasurement(loadModel, loadBalancerGroupName, vmName);
		if (arrivalRateMeasurement == null) {
			arrivalRateMeasurement = this.measurementFactory.createRequestArrivalRateMeasurement();
			VirtualMachine curVm = ModelQueries.getVmByName(ldcModel, vmName);
			if (curVm == null) {
				logger.debug("The VM doesn't exist in the models.");
				return;
			} else {
				if (curVm.getRuntimeApplicationModel() == null) {
					logger.debug("There are no WhiteBoxApplication models.");
					return;
				}
			}
			arrivalRateMeasurement.setObservedVmImageConnector(getVmImageConnector(curVm, loadBalancerGroupName, vmName));
			setApplicationInstance(ldcModel, vmName, arrivalRateMeasurement);
			arrivalRateMeasurement.setLogicalLoadModel(loadModel);
		}
		logger.debug("The requestArrivalRateMeasurement exists " + arrivalRateMeasurement + " and the value for it is " + value);
		if (value != null) {
			arrivalRateMeasurement.setArrivalRate(Amount.valueOf(value, SI.HERTZ));
		}

	}

	private void setApplicationInstance(LogicalDCModel ldcModel, String vmName, RequestArrivalRateMeasurement arrivalRateMeasurement) {
		for (WhiteBoxApplicationInstance curInstance : EcoreUtil.<WhiteBoxApplicationInstance> getObjectsByType(ldcModel.getApplicationInstances(),
				applicationPackage.getWhiteBoxApplicationInstance())) {
			for (ComposedVM curComposedVm : curInstance.getComposedVMs()) {
				if (curComposedVm.getVirtualMachine() != null && curComposedVm.getVirtualMachine().getName().equals(vmName)) {
					arrivalRateMeasurement.setObservedWhiteBoxApplicationInstance(curInstance);
					return;
				}
			}
		}
		;
	}

	private void setApplicationInstance(LogicalDCModel ldcModel, String vmName, ResponseArrivalRateMeasurement measurement) {
		for (WhiteBoxApplicationInstance curInstance : EcoreUtil.<WhiteBoxApplicationInstance> getObjectsByType(ldcModel.getApplicationInstances(),
				applicationPackage.getWhiteBoxApplicationInstance())) {
			for (ComposedVM curComposedVm : curInstance.getComposedVMs()) {
				if (curComposedVm.getVirtualMachine() != null && curComposedVm.getVirtualMachine().getName().equals(vmName)) {
					measurement.setObservedWhiteBoxApplicationInstance(curInstance);
					return;
				}
			}
		}
		;
	}

	/**
	 * Fetch the Logical DC Model from CDO.
	 * 
	 * @return The current Logical Data Centre Model.
	 */
	private LogicalDCModel getLogicalDCModel(CDOTransaction cdoTransaction) {
		// Read LogicalDCModel from CDO Server
		LogicalDCModel ldcModel = (LogicalDCModel) cactosCdoSession.getRepository(cdoTransaction, cactosCdoSession.getLogicalModelPath());
		return ldcModel;
	}

	/**
	 * Fetch the Logical Load Model from CDO.
	 * 
	 * @return The current Logical Load Model.
	 */
	private LogicalLoadModel getLogicalLoadModel(CDOTransaction cdoTransaction) {
		// Read LogicalDCModel from CDO Server
		LogicalLoadModel llModel = (LogicalLoadModel) cactosCdoSession.getRepository(cdoTransaction, cactosCdoSession.getLogicalLoadPath());
		return llModel;
	}

	private VMImageConnector getVmImageConnector(VirtualMachine curVm, String loadBalancerGroupName, String vmName) {
		WhiteBoxApplicationTemplate curTemplate = ((WhiteBoxVMImageBehaviour) curVm.getRuntimeApplicationModel().getVmImageBehaviour()).getComposedVMImage().getApplicationTemplate();
		VMImageConnector curConnector = lookUpConnectorById(curTemplate, loadBalancerGroupName);
		return curConnector;
	}

	private VMImageConnector lookUpConnectorById(WhiteBoxApplicationTemplate curTemplate, String loadBalancerGroupName) {
		for (VMImageConnector curConnector : curTemplate.getVmImageConnectors()) {
			if (curConnector.getId().equals(loadBalancerGroupName)) {
				return curConnector;
			}
		}
		throw new IllegalStateException("Connector for " + loadBalancerGroupName + " was not contained in template " + curTemplate.getName());
	}

	private RequestArrivalRateMeasurement lookUpArrivalRateMeasurement(LogicalLoadModel loadModel, String loadBalancerGroupName, String vmName) {
		for (RequestArrivalRateMeasurement curMeasurement : loadModel.getRequestArrivalRateMeasurement()) {
			if (curMeasurement.getObservedVmImageConnector().getId().equals(loadBalancerGroupName)) {
				return curMeasurement;
			}
		}
		return null;
	}

}
