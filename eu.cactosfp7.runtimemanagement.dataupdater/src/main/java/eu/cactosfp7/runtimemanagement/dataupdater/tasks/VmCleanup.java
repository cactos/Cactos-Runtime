package eu.cactosfp7.runtimemanagement.dataupdater.tasks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Properties;

import org.openstack4j.api.OSClient.OSClientV2;
import org.openstack4j.api.client.IOSClientBuilder.V2;
import org.openstack4j.model.compute.Server;
import org.openstack4j.openstack.OSFactory;

import eu.cactosfp7.runtimemanagement.hbase.RestRequest;
import eu.cactosfp7.runtimemanagement.hbase.models.Row;
import eu.cactosfp7.runtimemanagement.hbase.models.Table;

public class VmCleanup {

	private static Properties prop;
	
	public static void start(Properties prop){
		VmCleanup.prop = prop;
		
		int initialDelay = 10;
		int period = 10*60;
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				try {
					cleanup();
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}

		}, initialDelay, period, TimeUnit.SECONDS);			

	}

	protected static void cleanup() {
		Map<String, Server> osVms = getOsVms();
		Table snap = RestRequest.getTable("VMSnapshot");
		for (Entry<String, Row> entry : snap.getRows().entrySet()) {
			if(osVms.containsKey(entry.getKey())){
				// VM from HBase is also in OS.
				// nothing to do
			}else{
				// VM from HBase is not in OS.
				deleteVmFromHBase(snap, entry.getValue());
			}
		}
	}

	private static void deleteVmFromHBase(Table table, Row row) {
		System.out.println("Delete VM row from hbase: " + row);
		RestRequest.deleteRow(table, row);
	}

	private static Map<String, Server> getOsVms() {
		OSClientV2 osClient = getOsClient();
		List<? extends Server> osVmsList = osClient.compute().servers().listAll(false); // get short list (false) for all tenants
		Map<String, Server> osVms = new HashMap<String, Server>();
		for(Server s : osVmsList){
			osVms.put(s.getId(), s);
		}
		osVmsList.clear();
		return osVms;
	}

	private static OSClientV2 getOsClient() {
		OSClientV2 osClient;
		V2 v2 = OSFactory.builderV2();
		v2.endpoint(prop.getProperty("openstack_endpoint"));
		v2.credentials(prop.getProperty("openstack_user"), prop.getProperty("openstack_password"));
		v2.tenantName(prop.getProperty("openstack_tenant"));
		try {
			osClient = v2.authenticate(); 
		} catch (Exception e){
			throw e;
		}
		return osClient;
	}
	
}
