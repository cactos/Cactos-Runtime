package eu.cactosfp7.runtimemanagement.impl;


import java.util.logging.Logger;

import eu.cactosfp7.cactoopt.behaviourinference.IBehaviourInferenceAlgorithm;
import eu.cactosfp7.cactoopt.behaviourinference.registry.BehaviourInferenceAlgorithmRegistry;

/**Client for accessing the Service for Behaviour Inference Algorithms.
 * Uses the {@link BehaviourInferenceAlgorithmRegistry}.
 * 
 * @author hgroenda
 *
 */
public final class BehaviourInferenceAlgorithmClient {
	/** Singleton for accessing the service. */
    public static BehaviourInferenceAlgorithmClient INSTANCE;
    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(BehaviourInferenceAlgorithmClient.class.getName());
    
    /** Registry for behaviour inference algorithms (resolved by DS). */
    private BehaviourInferenceAlgorithmRegistry registry;
    
    public BehaviourInferenceAlgorithmClient() {
        if (INSTANCE != null)
            throw new RuntimeException("Only DS is allowed to instantiate this class. Use the property INSTANCE to get access to the singleton.");
        INSTANCE = this;
	}

    /**Bind method for discovered service.
     * @param service The discovered service.
     */
    public synchronized void bindService(BehaviourInferenceAlgorithmRegistry registry) {
    	this.registry = registry;
        logger.info("BehaviourInferenceAlgorithmRegistry service connected.");
    }
    
    /**Unbind method for discovered service.
     * @param service The removed service.
     */
    public synchronized void unbindService(BehaviourInferenceAlgorithmRegistry registry) {
    	registry = null;
        logger.info("BehaviourInferenceAlgorithmRegistry service disconnected.");
    }
    
    /**Access to the connected service.
     * @return Behaviour Inference Algorithm interface. <code>null</code> if there is no service bound.
     */
    public IBehaviourInferenceAlgorithm getService(){
        if(registry == null) {
        	logger.warning("BehaviourInferenceAlgorithmRegistry service unbound.");
        	return null;
        }
        return registry;
    }
    
}
