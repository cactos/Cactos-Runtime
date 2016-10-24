package eu.cactosfp7.runtimemanagement.impl;

import java.util.logging.Logger;

import eu.cactosfp7.cactoopt.placementservice.IPlacementService;
import eu.cactosfp7.cactoopt.placementservice.registry.PlacementServiceRegistry;

public final class PlacementClient {
    
    public static PlacementClient INSTANCE;
    
    public PlacementClient(){
        if(INSTANCE != null)
            throw new RuntimeException("Instantiating new VmiControllerClient is not allowed!");
        INSTANCE = this;
    }
    
    private static final Logger logger = Logger.getLogger(PlacementClient.class.getName());
    private IPlacementService placementService;
    
    /**Bind method for discovered service.
     * @param service The discovered service.
     */
    public synchronized void bindService(PlacementServiceRegistry placementService) {
        INSTANCE = this;
        this.placementService = placementService;
        logger.info("IPlacementService connected.");
    }
    
    /**Unbind method for discovered service.
     * @param service The removed service.
     */
    public synchronized void unbindService(PlacementServiceRegistry placementService) {
        this.placementService = placementService;
        logger.info("IPlacementService disconnected.");
    }
    
    public IPlacementService getService(){
        if(placementService == null)
            throw new RuntimeException("IVMIService unbound.");     
        return placementService;
    }
    
}
