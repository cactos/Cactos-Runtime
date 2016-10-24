package eu.cactosfp7.cactoscale.runtimemodelupdater.generation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import eu.cactosfp7.cactoscale.runtimemodelupdater.SettingsInitializer;

public final class HbaseConnect {

	private final static Logger logger = Logger.getLogger(HbaseConnect.class.getName());

	private final static Configuration config = HBaseConfiguration.create();
	private final static HbaseConnect cactosConnect;

	public final static HbaseConnect getInstance() {
		return cactosConnect;
	}

	static {
		config.set("hbase.zookeeper.quorum", SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.ZOOKEEPER_IP));
		config.set("hbase.zookeeper.property.clientPort", "2181");
		logger.info("Create connection to hbase...");
		cactosConnect = new HbaseConnect();
		logger.info("connection to hbase created.");
	}

	private final Connection connection;

	private HbaseConnect() {
		Connection _connection;
		try {
			_connection = ConnectionFactory.createConnection(config);
		} catch (IOException ex) {
			logger.error("cannot create HBase connection: ", ex);
			_connection = null;
		}
		connection = _connection;
	}

	private final Table createTable(String tableName) {
		Table table = null;
		try {
			table = connection.getTable(TableName.valueOf(tableName));
		} catch (IOException e) {
			logger.error("cannot create connection to HBase table: ", e);
		}
		return table;
	}

	public MultiResultReader getReaderForTable(String tableString, String[] rowKeys) throws IOException {
		Table table = createTable(tableString);
		try {
			Map<String, Result> rm = multiGetResult(table, rowKeys);

			return new MultiResultReader(table, rm);
		} catch (Exception ex) {
			table.close();
			return null;
		}
	}

	private Map<String, Result> multiGetResult(Table table, String[] rowKeys) throws IOException {
		List<Get> gets = new ArrayList<Get>(rowKeys.length);
		final long maxTimestamp = System.currentTimeMillis();
		for (String rowKey : rowKeys) {
			Get g = new Get(Bytes.toBytes(rowKey));
			//g.setTimeRange(maxTimestamp - Long.parseLong(SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.SCAN_TIME_OFFSET)), maxTimestamp);
			gets.add(g);
		}
		Result[] rr = table.get(gets);

		Map<String, Result> resultMap = new HashMap<String, Result>();
		for (int i = 0; i < rr.length; i++) {
			Result r = rr[i];
			String key = rowKeys[i];
			resultMap.put(key, r);
		}

		return resultMap;
	}

	public RowKeyScanner getRowKeyScannerForCNTable() throws IOException {
		return new RowKeyScanner(createTable(SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.CNSNAPSHOT_TABLE)));
	}

	public RowKeyScanner getRowKeyScannerForVMTable() throws IOException {
		return new RowKeyScanner(createTable(SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.VMSNAPSHOT_TABLE)));
	}

}
