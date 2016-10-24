/**
 * 
 */
package eu.cactosfp7.runtimemanagement;

/**
 * Marking support that CACTOS Infrastructure models can be updated on demand if
 * settings in the Virtualization Middleware Infrastructure (VMI) change. This
 * interface is only required for the CACTOS Runtime Toolkit and not the CACTOS
 * Prediction Toolkit. The Prediction Toolkit runs specified data centre
 * scenarios and does not need Flavour and VMImage updates during a simulation.
 * 
 * A service providing this class should be provided to administrators, e.g. a
 * REST service that listens on /CACTOS/{methodName} for POST requests and
 * update the models according to the VM.
 * 
 * @author hgroenda
 *
 */
public interface IVMISychronization {

	/**
	 * @return JSON message setting success to true or false.
	 */
	public String updateFlavours();

	/**
	 * @return JSON message setting success to true or false.
	 */
	public String updateVMImages();
}
