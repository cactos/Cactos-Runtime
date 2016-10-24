package eu.cactosfp7.runtimemanagement.dataupdater;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import eu.cactosfp7.runtimemanagement.dataupdater.tasks.NodeListUpdater;
import eu.cactosfp7.runtimemanagement.dataupdater.tasks.StateChecker;
import eu.cactosfp7.runtimemanagement.dataupdater.tasks.VmCleanup;
import eu.cactosfp7.runtimemanagement.hbase.RestRequest;

public class PeriodicDataUpdater {

	private static Properties prop;
	
	public static void main(String[] args) throws IOException {
		loadConfig();

		StateChecker.start(prop);
		NodeListUpdater.start(prop);
		VmCleanup.start(prop);
	}

	private static void loadConfig() throws IOException {

		
		prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream("config.properties");
			// load a properties file
			prop.load(input);
			// get the property value and print it out
			RestRequest.host = prop.getProperty("hbase_restserver_ip");
			RestRequest.port = Integer.valueOf(prop.getProperty("hbase_restserver_port"));
		} catch (IOException ex) {
			ex.printStackTrace();
			throw ex;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	

}
