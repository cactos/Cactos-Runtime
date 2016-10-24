package eu.cactosfp7.infrastructuremodels.builder.applicationmodelinstance.util;

import eu.cactosfp7.infrastructuremodels.logicaldc.core.Hypervisor;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine;

public class ModelQueries {
    public static VirtualMachine getVmByName(LogicalDCModel ldcModel, String name) {
        for(Hypervisor curHypervisor : ldcModel.getHypervisors()) {
            for(VirtualMachine curVm : curHypervisor.getVirtualMachines()) {
                if(curVm.getName().equals(name)) {
                    return curVm;
                }
            }
        }
        return null;
    }
}
