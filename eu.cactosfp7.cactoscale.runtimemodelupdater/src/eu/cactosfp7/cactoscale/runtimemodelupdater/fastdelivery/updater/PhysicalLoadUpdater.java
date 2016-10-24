package eu.cactosfp7.cactoscale.runtimemodelupdater.fastdelivery.updater;

import java.util.ArrayList;

import javax.measure.quantity.DataRate;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Power;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.jscience.physics.amount.Amount;

import eu.cactosfp7.cactoscale.runtimemodelupdater.generation.ModelLoaderCdo;
import eu.cactosfp7.cdosession.CactosCdoSession;
import eu.cactosfp7.cdosession.util.CdoHelper;
import eu.cactosfp7.infrastructuremodels.load.physical.InterconnectMeasurement;
import eu.cactosfp7.infrastructuremodels.load.physical.MemoryMeasurement;
import eu.cactosfp7.infrastructuremodels.load.physical.PhysicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.physical.PowerConsumingEntityMeasurement;
import eu.cactosfp7.infrastructuremodels.load.physical.PuMeasurement;
import eu.cactosfp7.infrastructuremodels.load.physical.StorageMeasurement;

public class PhysicalLoadUpdater {
	private String computeNodeId;
	private PhysicalLoadModel cdoPhysicalLoadModel;
	private ModelLoaderCdo cdoModelLoader;
	private final static Logger logger = Logger.getLogger(PhysicalLoadUpdater.class);

	public void initializeCdoModel(CactosCdoSession cactosCdoSession) {
		cdoModelLoader = new ModelLoaderCdo();
		// pass the cdo session initialised once when the fastdeliverylistener
		// is invoked
		cdoModelLoader.setCactosCdoSession(cactosCdoSession, cactosCdoSession.createTransaction());
		ArrayList<Object> cdoModels = new ArrayList<Object>();
		cdoModels = cdoModelLoader.loadModelInstances();
		cdoPhysicalLoadModel = CdoHelper.findPhysicalLoadModel(cdoModels);
		CdoHelper.findPhysicalDCModel(cdoModels);
	}

	public void setComputeNodeId(String computeNodeId) {
		this.computeNodeId = computeNodeId;
	}

	public void setMemoryLoad(double load) {
		EList<MemoryMeasurement> cdoMemoryMeasurements = cdoPhysicalLoadModel.getMemoryMeasurements();

		MemoryMeasurement memoryMeasurement;
		try {
			memoryMeasurement = CdoHelper.getModelByIdentifier(cdoMemoryMeasurements, computeNodeId);
			if (memoryMeasurement != null) {
				memoryMeasurement.getUtilization().setValue(Amount.valueOf(load, Dimensionless.UNIT));
			}
		} catch (Exception e) {
			logger.error("setMemoryLoad", e);
		}
	}

	public void setPuLoad(double load) {
		EList<PuMeasurement> cdoPuMeasurements = cdoPhysicalLoadModel.getCpuMeasurement();

		PuMeasurement puMeasurement;
		try {
			puMeasurement = CdoHelper.getModelByIdentifier(cdoPuMeasurements, computeNodeId);
			if (puMeasurement != null) {
				puMeasurement.getUtilization().setValue(Amount.valueOf(load / 100, Dimensionless.UNIT));
			}
		} catch (Exception e) {
			logger.error("setPuLoad", e);
		}

	}

	public void setInterconnectLoad(double load) {
		EList<InterconnectMeasurement> cdoInterconnectMeasurements = cdoPhysicalLoadModel.getInterconnectMeasurement();

		InterconnectMeasurement interconnectMeasurement;
		try {
			interconnectMeasurement = CdoHelper.getModelByIdentifier(cdoInterconnectMeasurements, computeNodeId);
			if (interconnectMeasurement != null) {
				interconnectMeasurement.setMeasuredThroughput(Amount.valueOf(load * 8, DataRate.UNIT));
			}
		} catch (Exception e) {
			logger.error("setInterconnectLoad", e);
		}

	}

	public void setStorageThroughput(double readThroughput, double writeThroughput) {
		EList<StorageMeasurement> cdoStorageMeasurements = cdoPhysicalLoadModel.getStorageMeasurement();

		StorageMeasurement storageMeasurement;
		try {
			storageMeasurement = CdoHelper.getModelByIdentifier(cdoStorageMeasurements, computeNodeId);
			if (storageMeasurement != null) {
				storageMeasurement.setReadThroughput(Amount.valueOf(readThroughput * 1024 * 8, DataRate.UNIT));
				storageMeasurement.setWriteThroughput(Amount.valueOf(writeThroughput * 1024 * 8, DataRate.UNIT));
			}
		} catch (Exception e) {
			logger.error("setStorageThroughput", e);
		}
	}

	public void setPowerLoad(double load) {
		EList<PowerConsumingEntityMeasurement> cdoPowerMeasurements = cdoPhysicalLoadModel.getPowerConsumingEntityMeasurements();

		PowerConsumingEntityMeasurement powerMeasurement;
		try {
			powerMeasurement = CdoHelper.getModelByIdentifier(cdoPowerMeasurements, computeNodeId);
			if (powerMeasurement != null) {
				powerMeasurement.setCurrentConsumption(Amount.valueOf(load, Power.UNIT));
			}
		} catch (Exception e) {
			logger.error("setPowerLoad", e);
		}
	}

	public void commit() {
		cdoModelLoader.commitAndCloseTransaction();
	}

}
