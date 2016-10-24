package eu.cactosfp7.cactoscale.runtimemodelupdater.fastdelivery.updater;

import java.util.ArrayList;
import java.util.Map;

import javax.measure.quantity.Dimensionless;

import org.apache.log4j.Logger;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.common.util.EList;
import org.jscience.physics.amount.Amount;

import eu.cactosfp7.cactoscale.runtimemodelupdater.generation.HypervisorNodeFiller;
import eu.cactosfp7.cactoscale.runtimemodelupdater.generation.ModelLoaderCdo;
import eu.cactosfp7.cdosession.CactosCdoSession;
import eu.cactosfp7.cdosession.util.CdoHelper;
import eu.cactosfp7.infrastructuremodels.load.logical.LogicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.logical.VirtualMemoryMeasurement;
import eu.cactosfp7.infrastructuremodels.load.logical.VirtualProcessingUnitsMeasurement;

public class LogicalLoadUpdater {

	private LogicalLoadModel cdoLogicalLoadModel;
	private ModelLoaderCdo cdoModelLoader;
	private CactosCdoSession cactosCdoSession;
	private CDOTransaction cdoTransaction;
	private final static Logger logger = Logger.getLogger(LogicalLoadUpdater.class);

	public void initializeCdoModel(CactosCdoSession cactosCdoSession) {
		this.cactosCdoSession = cactosCdoSession;
		this.cdoTransaction = cactosCdoSession.createTransaction();
		cdoModelLoader = new ModelLoaderCdo();
		cdoModelLoader.setCactosCdoSession(cactosCdoSession, cdoTransaction);
		ArrayList<Object> cdoModels = new ArrayList<Object>();

		cdoModels = cdoModelLoader.loadModelInstances();
		cdoLogicalLoadModel = CdoHelper.findLogicalLoadModel(cdoModels);
	}

	public void setVMemoryLoad(double load, String vmId) {
		EList<VirtualMemoryMeasurement> cdoVMemoryMeasurements = cdoLogicalLoadModel.getVirtualMemoryMeasurements();

		VirtualMemoryMeasurement vMemoryMeasurement;
		try {
			vMemoryMeasurement = CdoHelper.getModelByIdentifier(cdoVMemoryMeasurements, vmId);
			if (vMemoryMeasurement != null) {
				vMemoryMeasurement.getUtilization().setValue(Amount.valueOf(load, Dimensionless.UNIT));

			}
		} catch (Exception e) {
			logger.error("setVMemoryLoad", e);
		}
	}

	public void setVCpuLoad(String vmId, int cpuNum, double load) {
		EList<VirtualProcessingUnitsMeasurement> cdoVProcessingUnitMeasurements = cdoLogicalLoadModel.getVirtualProcessingUnitMeasurements();

		VirtualProcessingUnitsMeasurement vProcessingUnitMeasurement;
		try {
			vProcessingUnitMeasurement = CdoHelper.getModelByIdentifier(cdoVProcessingUnitMeasurements, vmId + "-" + cpuNum);
			if (vProcessingUnitMeasurement != null) {
				vProcessingUnitMeasurement.getUtilization().setValue(Amount.valueOf(load, Dimensionless.UNIT));
			}
		} catch (Exception e) {
			logger.error("setVCpuLoad", e);
		}

	}

	public void updateLoadBalancerMetric(String uuid, Map<byte[], byte[]> appMeasurements) {
		HypervisorNodeFiller.updateLoadBalancerMetric(uuid, appMeasurements, cactosCdoSession, cdoTransaction);
	}

	public void commit() {
		cdoModelLoader.commitAndCloseTransaction();
	}

}
