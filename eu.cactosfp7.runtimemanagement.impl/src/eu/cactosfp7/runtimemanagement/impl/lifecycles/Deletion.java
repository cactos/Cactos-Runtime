package eu.cactosfp7.runtimemanagement.impl.lifecycles;

import java.util.Map;

import eu.cactosfp7.runtimemanagement.impl.VmiControllerClient;
import eu.cactosfp7.runtimemanagement.util.Lifecycle;
import eu.cactosfp7.runtimemanagement.util.PropagateToChukwa;

public class Deletion implements Lifecycle {
	
	/** Logger for this class. */
//	private static final Logger logger = Logger.getLogger(Deletion.class.getCanonicalName());
	private final String vmName;
	private String result;
	private final Map<String, String> meta;

	public Deletion(String vmName, Map<String, String> meta) {
		this.vmName = vmName;
		this.meta = meta;
	}

	@Override
	public void start() {
		// forward to openstack
		VmiControllerClient.INSTANCE.getService().executeDeletion(vmName, meta);	
		
		// store new vm in chukwa/hbase
		PropagateToChukwa.deletion(vmName);		
	}

	@Override
	public String result() {
		return result;
	}

}
