package eu.cactosfp7.cactoscale.runtimemodelupdater.modelbuilder;

import eu.cactosfp7.infrastructuremodels.logicaldc.hypervisor.HypervisorFactory;
import eu.cactosfp7.infrastructuremodels.logicaldc.hypervisor.HypervisorRepository;
import eu.cactosfp7.infrastructuremodels.logicaldc.hypervisor.HypervisorType;

public class HypervisorTypePlaceholder implements NodePlaceholder {

	private final HypervisorType hypervisorType;
	private final HypervisorRepository hypervisorRepository;

	public HypervisorTypePlaceholder(HypervisorRepository _hypervisorRepository) {
		hypervisorRepository = _hypervisorRepository;
		hypervisorType = createHypervisorType();
	}

	private final HypervisorType createHypervisorType() {
		HypervisorType _hypervisorType = HypervisorFactory.INSTANCE.createHypervisorType();
		_hypervisorType.setHypervisorRepository(hypervisorRepository);
		return _hypervisorType;
	}
	
	public void fillHypervisorType(String type) {
		hypervisorType.setName(type);
	}

	public HypervisorType getHypervisorType() {
		return hypervisorType;
	}
	

	@Override
	public String getNodeKey() {
		// TODO Auto-generated method stub
		return null;
	}

}
