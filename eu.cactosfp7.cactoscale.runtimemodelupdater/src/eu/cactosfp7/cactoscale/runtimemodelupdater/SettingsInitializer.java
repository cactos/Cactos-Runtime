package eu.cactosfp7.cactoscale.runtimemodelupdater;

import java.util.Dictionary;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;

import eu.cactosfp7.cactoscale.runtimemodelupdater.fastdelivery.main.MetricSocketClient;
import eu.cactosfp7.cactoscale.runtimemodelupdater.generation.ModelsUpdater;

public class SettingsInitializer implements ManagedService {
	/** Logger for this class. */
	private static final Logger log = Logger.getLogger(SettingsInitializer.class);

	/** Properties. */
	private Properties properties = new Properties();

	private ModelsUpdater modelUpdateTask;
	private MetricSocketClient metricSocketClient;
	private ScheduledFuture<?> runtimeModelUpdater;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public static String CHUKWA_COLLECTOR_HOST = "chukwaCollectorHost";
	public static String CHUKWA_COLLECTOR_PORT = "chukwaCollectorPort";
	public static String ENABLE_SLOWDELIVERY = "enableSlowDelivery";
	public static String ENABLE_FASTDELIVERY = "enableFastDelivery";
	public static String ZOOKEEPER_IP = "zookeeperHost";
	public static String RUNTIME_MODEL_UPDATER_INITIAL_DELAY = "runtimeModelUpdaterInitialDelay";
	public static String RUNTIME_MODEL_UPDATER_INTERVAL = "runtimeModelUpdaterInterval";
	
	public static String CNSNAPSHOT_TABLE = "cnSnapshotTable";
	public static String VMSNAPSHOT_TABLE = "vmSnapshotTable";

	public static String HARDWARE_FAMILY = "hardwareFamily";
	public static String HARDWARE_UTIL_FAMILY = "hardwareUtilFamily";
	public static String STORAGE_FAMILY = "storageFamily";
	public static String STORAGE_UTIL_FAMILY = "storageUtilFamily";
	public static String FILESYSTEM_FAMILY = "filesystemFamily";
	public static String NETWORK_FAMILY = "networkFamily";
	public static String NETWORK_UTIL_FAMILY = "networkUtilFamily";
	public static String POWER_FAMILY = "powerFamily";
	public static String POWER_UTIL_FAMILY = "powerUtilFamily";
	public static String META_FAMILY = "metaFamily";
	public static String APP_FAMILY = "appFamily";
	public static String VMS_FAMILY = "vmsFamily";
	public static String SCAN_TIME_OFFSET = "hbaseScanOffset";
	public static String FILTERED_COMPUTE_NODES = "filteredComputeNodes";

	public static SettingsInitializer INSTANCE;

	public SettingsInitializer() throws Exception {
		if (INSTANCE != null)
			throw new RuntimeException("Instantiating new SettingsInitializer is not allowed!");
		INSTANCE = this;
	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	public void startFastDelivery() {
		metricSocketClient = new MetricSocketClient();
		metricSocketClient.start();
	}

	public void stopFastDelivery() {
		metricSocketClient.stopClient();
	}

	public void startSlowDelivery() {
		this.modelUpdateTask = new ModelsUpdater();
		runtimeModelUpdater = scheduler.scheduleAtFixedRate(modelUpdateTask, Integer.parseInt(getProperty(RUNTIME_MODEL_UPDATER_INITIAL_DELAY)),
				Integer.parseInt(getProperty(RUNTIME_MODEL_UPDATER_INTERVAL)), TimeUnit.SECONDS);
	}

	public void stopSlowDelivery() {
		runtimeModelUpdater.cancel(false); // stop without interrupt
	}

	public void stopRuntimeModelUpdater() {
		scheduler.schedule(new Runnable() {

			@Override
			public void run() {
				runtimeModelUpdater.cancel(true);
			}
		}, 0, TimeUnit.SECONDS);
	}

	@Override
	public void updated(Dictionary<String, ?> cactosProps) throws ConfigurationException {

		if (cactosProps == null || cactosProps.isEmpty()) {
			log.info("Configuration file is empty or not available!");
		} else {

			String stateFastDelivery = (String) this.properties.get(ENABLE_FASTDELIVERY);
			stateFastDelivery = stateFastDelivery == null ? "false" : stateFastDelivery;
			String stateSlowDelivery = (String) this.properties.get(ENABLE_SLOWDELIVERY);
			stateSlowDelivery = stateSlowDelivery == null ? "false" : stateSlowDelivery;

			this.properties.put(CHUKWA_COLLECTOR_HOST, cactosProps.get(CHUKWA_COLLECTOR_HOST));
			this.properties.put(CHUKWA_COLLECTOR_PORT, cactosProps.get(CHUKWA_COLLECTOR_PORT));
			this.properties.put(ENABLE_SLOWDELIVERY, cactosProps.get(ENABLE_SLOWDELIVERY));
			this.properties.put(ENABLE_FASTDELIVERY, cactosProps.get(ENABLE_FASTDELIVERY));
			this.properties.put(ZOOKEEPER_IP, cactosProps.get(ZOOKEEPER_IP));
			this.properties.put(RUNTIME_MODEL_UPDATER_INTERVAL, cactosProps.get(RUNTIME_MODEL_UPDATER_INTERVAL));
			this.properties.put(RUNTIME_MODEL_UPDATER_INITIAL_DELAY, cactosProps.get(RUNTIME_MODEL_UPDATER_INITIAL_DELAY));
			
			this.properties.put(CNSNAPSHOT_TABLE, cactosProps.get(CNSNAPSHOT_TABLE));
			this.properties.put(VMSNAPSHOT_TABLE, cactosProps.get(VMSNAPSHOT_TABLE));
			this.properties.put(HARDWARE_FAMILY, cactosProps.get(HARDWARE_FAMILY));
			this.properties.put(HARDWARE_UTIL_FAMILY, cactosProps.get(HARDWARE_UTIL_FAMILY));
			this.properties.put(STORAGE_FAMILY, cactosProps.get(STORAGE_FAMILY));
			this.properties.put(STORAGE_UTIL_FAMILY, cactosProps.get(STORAGE_UTIL_FAMILY));
			this.properties.put(FILESYSTEM_FAMILY, cactosProps.get(FILESYSTEM_FAMILY));
			this.properties.put(NETWORK_FAMILY, cactosProps.get(NETWORK_FAMILY));
			this.properties.put(NETWORK_UTIL_FAMILY, cactosProps.get(NETWORK_UTIL_FAMILY));
			this.properties.put(POWER_FAMILY, cactosProps.get(POWER_FAMILY));
			this.properties.put(POWER_UTIL_FAMILY, cactosProps.get(POWER_UTIL_FAMILY));
			this.properties.put(APP_FAMILY, cactosProps.get(APP_FAMILY));
			this.properties.put(META_FAMILY, cactosProps.get(META_FAMILY));
			this.properties.put(VMS_FAMILY, cactosProps.get(VMS_FAMILY));
			this.properties.put(SCAN_TIME_OFFSET, cactosProps.get(SCAN_TIME_OFFSET));
			this.properties.put(FILTERED_COMPUTE_NODES, cactosProps.get(FILTERED_COMPUTE_NODES));
			

			if (cactosProps.get(ENABLE_FASTDELIVERY).equals("true") && (!stateFastDelivery.equals("true"))) {
				startFastDelivery();
			}
			if (!cactosProps.get(ENABLE_FASTDELIVERY).equals("true") && stateFastDelivery.equals("true")) {
				stopFastDelivery();
			}
			if (cactosProps.get(ENABLE_SLOWDELIVERY).equals("true") && !stateSlowDelivery.equals("true")) {
				startSlowDelivery();
			}
			if (!cactosProps.get(ENABLE_SLOWDELIVERY).equals("true") && stateSlowDelivery.equals("true")) {
				stopSlowDelivery();
			}

			log.info("New configuration is set!");
		}

	}

	/**
	 * Declarative Service method called by activating
	 * 
	 * @param context
	 * @throws Exception
	 */
	public void activate(ComponentContext context) throws Exception {
		log.info("Activating: " + this.getClass().getName());
	}

	/**
	 * Declarative Service method called by deactivating
	 * 
	 * @param context
	 * @throws Exception
	 */
	public void deactivate(ComponentContext context) throws Exception {
		log.info("Deactivating: " + this.getClass().getName());
	}
}
