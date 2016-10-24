package eu.cactosfp7.cactoscale.runtimemodelupdater.generation;

import eu.cactosfp7.cactoscale.runtimemodelupdater.SettingsInitializer;
import eu.cactosfp7.cactoscale.runtimemodelupdater.modelbuilder.ArchitectureTypePlaceholder;
import eu.cactosfp7.cactoscale.runtimemodelupdater.modelbuilder.ResultAccessor;

public class ArchitectureTypeFiller {

	public static void fillArchitectureTypeForNode(ArchitectureTypePlaceholder architectureTypePlaceholder, ResultAccessor readerForCNSnapshot){
		String rowKey = architectureTypePlaceholder.getNodeKey();
		String architectureType = readerForCNSnapshot.getResult(rowKey, SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.HARDWARE_FAMILY), "cpu_arch", String.class, "");
		architectureTypePlaceholder.fillArchitectureType(architectureType);
	}
}
