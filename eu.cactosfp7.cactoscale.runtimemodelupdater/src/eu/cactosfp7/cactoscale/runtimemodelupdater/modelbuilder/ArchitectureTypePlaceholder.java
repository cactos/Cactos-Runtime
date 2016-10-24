package eu.cactosfp7.cactoscale.runtimemodelupdater.modelbuilder;

import java.util.ArrayList;
import java.util.List;

import eu.cactosfp7.infrastructuremodels.physicaldc.architecturetype.ArchitectureType;
import eu.cactosfp7.infrastructuremodels.physicaldc.architecturetype.ArchitectureTypeRepository;

public class ArchitectureTypePlaceholder implements NodePlaceholder {

	private final ArchitectureTypeRepository architectureTypeRepository;
	private final ArchitectureType architectureType;
	private final List<String> computeNodeList;
	private String name = null;

	public ArchitectureTypePlaceholder(RowKeyName rowKey, ArchitectureTypeRepository _architectureTypeRepository) {
		if (rowKey != null)
			name = rowKey.getName();
		architectureTypeRepository = _architectureTypeRepository;
		architectureType = createArchitectureType();
		computeNodeList = new ArrayList<String>();
	}

	private ArchitectureType createArchitectureType() {
		ArchitectureType _architectureType = eu.cactosfp7.infrastructuremodels.physicaldc.architecturetype.ArchitecturetypeFactory.INSTANCE.createArchitectureType();
		_architectureType.setArchitectureTypeRepository(architectureTypeRepository);
		return _architectureType;
	}

	public void fillArchitectureType(String type) {
		architectureType.setName(type);
	}

	public void addComputenode(String computenode) {
		computeNodeList.add(computenode);
	}

	public List<String> getComputeNodes() {
		return computeNodeList;
	}

	public ArchitectureType getArchitectureType() {
		return architectureType;
	}

	@Override
	public String getNodeKey() {
		return name;
	}

}
