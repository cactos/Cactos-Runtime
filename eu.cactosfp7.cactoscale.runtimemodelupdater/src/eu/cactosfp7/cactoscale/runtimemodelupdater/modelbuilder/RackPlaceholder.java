package eu.cactosfp7.cactoscale.runtimemodelupdater.modelbuilder;

import eu.cactosfp7.infrastructuremodels.physicaldc.core.CoreFactory;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.PhysicalDCModel;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.Rack;

public class RackPlaceholder implements NodePlaceholder {

	private final PhysicalDCModel physicalDCModel;
	private final Rack rack;

	public RackPlaceholder(PhysicalDCModel _physicalDCModel) {
		physicalDCModel = _physicalDCModel;
		rack = createRack();
	}

	private Rack createRack() {
		Rack _rack = CoreFactory.INSTANCE.createRack();
		_rack.setPhysicalDCModel(physicalDCModel);
		return _rack;
	}
	
	public void fillRack(String rackName) {
		rack.setName(rackName);
	}

	public Rack getRack() {
		return rack;
	}

	@Override
	public String getNodeKey() {
		// TODO Auto-generated method stub
		return null;
	}

}
