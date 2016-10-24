package eu.cactosfp7.vmi.controller;

import java.util.Map;

import org.eclipse.emf.cdo.util.CommitException;
import org.eclipse.emf.cdo.util.ConcurrentAccessException;

/**
 * Interface for the Virtualisation Middleware Integration.
 * 
 * @author taspolat
 * @author hgroenda
 * @author chauser
 */
public interface IVMIService {

	/**Execute the most recent optimisation plan.
	 * The plan must be in state READY.
	 * @throws ConcurrentAccessException In case of CDO repository access failures.
	 * @throws CommitException In case of CDO repository access failures.
	 * @return Whether the execution was successful.
	 */
	public boolean execute() throws ConcurrentAccessException, CommitException;
	
	/**Execute the optimisation plan with the given UUID in the repository.
	 * Does not execute any plan if there is no plan with the given UUID.
	 * Behaves as {@link #execute()} if UUID is <code>null</code>.
	 * @param uuid The UUID of the plan. Can be <code>null</code>.
	 * @throws ConcurrentAccessException In case of CDO repository access failures.
	 * @throws CommitException In case of CDO repository access failures.
	 * @return Whether the execution was successful.
	 */
	public boolean execute(String uuid) throws ConcurrentAccessException, CommitException;

	/**
	 * Executes the placement of an unassigned VM with UUID {@code vmUuid} on the compute node with the UUID {@code computeNodeUuid}.
	 * @param vmUuid The UUID of the VM that is to be placed.
	 * @param computeNodeUuid The compute node on which the placement is to be executed.
	 * @return Whether the placement was executed successfully.
	 */
	public boolean executePlacement(String vmUuid, String computeNodeUuid);
	
	/**
	 * Executes the deletion of a VM.
	 * @param vmName The vmName of the VM that is to be placed.
	 * @param meta 
	 * @return Whether the deletion was executed successfully.
	 */
	public boolean executeDeletion(String vmName, Map<String, String> meta);	
	
}
