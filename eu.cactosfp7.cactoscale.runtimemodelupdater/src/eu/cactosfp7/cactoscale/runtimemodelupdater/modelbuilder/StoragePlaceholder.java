package eu.cactosfp7.cactoscale.runtimemodelupdater.modelbuilder;

import javax.measure.quantity.DataRate;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Duration;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import eu.cactosfp7.infrastructuremodels.load.physical.PhysicalFactory;
import eu.cactosfp7.infrastructuremodels.load.physical.PhysicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.physical.StorageMeasurement;
import eu.cactosfp7.infrastructuremodels.load.physical.Utilization;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.ComputeNode;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.CoreFactory;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.StorageSpecification;
import eu.cactosfp7.infrastructuremodels.physicaldc.util.Bandwidth;
import eu.cactosfp7.infrastructuremodels.physicaldc.util.UtilFactory;

public final class StoragePlaceholder implements NodePlaceholder {

	private final String name;
	private final StorageSpecification storage;
	private final StorageMeasurement storageMeasurement;
	private final Utilization storageUtilisation;
	private final ComputeNode computeNode;
	private final PhysicalLoadModel physicalLoadModel;

	public StoragePlaceholder(RowKeyName _rowkeyname, ComputeNode _computenode, PhysicalLoadModel _physicalLoadModel) {
		name = _rowkeyname.getName();

		computeNode = _computenode;
		physicalLoadModel = _physicalLoadModel;
		storage = createStorage();

		storageMeasurement = createStorageMeasurement();
		storageUtilisation = createStorageUtilization();
	}

	private StorageSpecification createStorage() {
		StorageSpecification _storage = CoreFactory.INSTANCE.createStorageSpecification();
		_storage.setNode(computeNode);
		_storage.getPowerProvidingEntities().add(computeNode);
		_storage.setName(name);
		Bandwidth readBandwidth = UtilFactory.INSTANCE.createBandwidth();
		_storage.setReadBandwidth(readBandwidth);
		Bandwidth writeBandwidth = UtilFactory.INSTANCE.createBandwidth();
		_storage.setWriteBandwidth(writeBandwidth);
		return _storage;
	}

	private StorageMeasurement createStorageMeasurement() {
		StorageMeasurement _storageMeasurement = PhysicalFactory.INSTANCE.createStorageMeasurement();
		_storageMeasurement.setPhysicalLoadModel(physicalLoadModel);
		_storageMeasurement.setObservedStorage(storage);
		return _storageMeasurement;
	}

	private Utilization createStorageUtilization() {
		Utilization _storageUtilization = PhysicalFactory.INSTANCE.createUtilization();
		_storageUtilization.setStorageMeasurement(storageMeasurement);
		return _storageUtilization;
	}

	public void fillStorageSpecificationDiskSize(double size) {
		storage.setSize(Amount.valueOf(size, SI.GIGA(NonSI.BYTE)));
	}

	public void fillStorageSpecificationBandwidthAndDelay(double _readBandwidth, double _writeBandwidth) {
		// Megabytes
		storage.getReadBandwidth().setValue(Amount.valueOf(_readBandwidth * 1024 * 1024 * 8, DataRate.UNIT));
		storage.setReadDelay(Amount.valueOf(1 / _readBandwidth, Duration.UNIT));

		storage.getWriteBandwidth().setValue(Amount.valueOf(_writeBandwidth * 1024 * 1024 * 8, DataRate.UNIT));
		storage.setWriteDelay(Amount.valueOf(1 / _writeBandwidth, Duration.UNIT));

	}

	public void fillStorageMeasurement(double readThroughput, double writeThroughput) {
		// Kilobytes
		storageMeasurement.setReadThroughput(Amount.valueOf(readThroughput * 1024 * 8, DataRate.UNIT));
		storageMeasurement.setWriteThroughput(Amount.valueOf(writeThroughput * 1024 * 8, DataRate.UNIT));
	}

	public void fillStorageUtilisation(double disk_util) {
		// FIXME: When in models we will store the used memory, fast delivery
		// will update the util also.
		storageUtilisation.setValue(Amount.valueOf(disk_util, Dimensionless.UNIT));
	}

	@Override
	public String getNodeKey() {
		return name;
	}
}
