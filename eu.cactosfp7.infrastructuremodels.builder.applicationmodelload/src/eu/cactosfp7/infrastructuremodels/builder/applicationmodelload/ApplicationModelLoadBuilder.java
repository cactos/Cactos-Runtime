package eu.cactosfp7.infrastructuremodels.builder.applicationmodelload;

import java.util.Map;

import org.eclipse.emf.cdo.transaction.CDOTransaction;

import eu.cactosfp7.cdosession.CactosCdoSession;

/**
 * Interface for building application load models in CDO server.
 * 
 * @author chauser
 */
public interface ApplicationModelLoadBuilder {
	
	public enum LoadBalancerMetric{
		TWO_XX_PER_SECOND, SESSION_PER_SECOND
	}
	
	/**
	 * Create or update application load model instance with the given measurements
	 * @param vmName The name of the virtual machine where the load balancer metrics are from
	 * @param measurements A list of measurements for the load balancer group, according to type LoadBalancerMetric
	 * @param loadBalancerGroupName A unique identifier for the load balancer group
	 */
	public void updateLoadBalancerMetric(CactosCdoSession cactosCdoSession, CDOTransaction cdoTransaction, String loadBalancerGroupName, String vmName, Map<LoadBalancerMetric, Double> measurements);
	
}
