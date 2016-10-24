package eu.cactosfp7.cactoscale.runtimemodelupdater.modelbuilder;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Power;

import org.jscience.physics.amount.Amount;

import eu.cactosfp7.cactoscale.runtimemodelupdater.SettingsInitializer;
import eu.cactosfp7.infrastructuremodels.load.physical.PhysicalFactory;
import eu.cactosfp7.infrastructuremodels.load.physical.PhysicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.physical.PowerConsumingEntityMeasurement;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.ComputeNode;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.CoreFactory;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.PhysicalDCModel;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.PowerDistributionUnit;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.Rack;

public final class PowerPlaceholder implements NodePlaceholder {

	private final String name;
	private final List<PowerDistributionUnit> powerDistributionUnits = new ArrayList<PowerDistributionUnit>();
	private final List<PowerConsumingEntityMeasurement> powerConsumingEntityMeasurements = new ArrayList<PowerConsumingEntityMeasurement>();
	private final PhysicalDCModel physicalDCModel;
	private final PhysicalLoadModel physicalLoadModel;
	private final ComputeNode computeNode;
	private final Rack rack;

	public PowerPlaceholder(RowKeyName _rowkeyname, ComputeNode _computenode, Rack _rack, PhysicalDCModel _physicalDCModel, PhysicalLoadModel _physicalLoadModel) {
		name = _rowkeyname.getName();
		computeNode = _computenode;
		rack = _rack;
		physicalDCModel = _physicalDCModel;
		physicalLoadModel = _physicalLoadModel;
	}

	/**
	 * FIXME: Add the PDU when data are sufficient.DON'T REMOVE!
	 */
	public void addPowerDistributionUnit(String serialNumber, double capacity) {
		PowerDistributionUnit _powerDistributionUnit = CoreFactory.INSTANCE.createPowerDistributionUnit();
		_powerDistributionUnit.setPhysicalDCModel(physicalDCModel);
		_powerDistributionUnit.setName(serialNumber);
		_powerDistributionUnit.getPowerConsumingEntities().add(computeNode);
		_powerDistributionUnit.setHostedIn(rack);
		_powerDistributionUnit.setSuppliablePeakPower(Amount.valueOf(capacity, Power.UNIT));
		powerDistributionUnits.add(_powerDistributionUnit);
	}

	public void addPowerConsumingEntityMeasurement(double consumption) {
		PowerConsumingEntityMeasurement _powerConsumingEntityMeasurement = PhysicalFactory.INSTANCE.createPowerConsumingEntityMeasurement();
		_powerConsumingEntityMeasurement.setPhysicalLoadModel(physicalLoadModel);
		_powerConsumingEntityMeasurement.setPowerConsumingEntity(computeNode);
		if (!SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.ENABLE_FASTDELIVERY).equals("true")) {
			_powerConsumingEntityMeasurement.setCurrentConsumption(Amount.valueOf(consumption, Power.UNIT));
		}
		powerConsumingEntityMeasurements.add(_powerConsumingEntityMeasurement);
	}

	@Override	
	public String getNodeKey() {
		return name;
	}

}
