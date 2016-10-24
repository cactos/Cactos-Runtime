package eu.cactosfp7.runtimemanagement.dataupdater.tasks;

import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import eu.cactosfp7.runtimemanagement.hbase.RestRequest;
import eu.cactosfp7.runtimemanagement.hbase.models.Cell;
import eu.cactosfp7.runtimemanagement.hbase.models.Row;
import eu.cactosfp7.runtimemanagement.hbase.models.Table;

public class NodeListUpdater {

	public static void start(Properties prop) {
		int initialDelay = 10;
		int period = 10*60;
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				try {
					updateComputenodes();
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}

		}, initialDelay, period, TimeUnit.SECONDS);				
	}
	
	protected static void updateComputenodes(){
		StringBuffer buff = new StringBuffer();
		Table tab = RestRequest.getTable("CNSnapshot");
		for (Entry<String, Row> entry : tab.getRows().entrySet()) {
			if(buff.length() != 0)
				buff.append(",");
			buff.append(entry.getKey());
		}
		
		long currentTimestamp = new java.util.Date().getTime();

		System.out.println(currentTimestamp + ": " + buff.toString());
		
		Cell listCell = new Cell("cns:list", currentTimestamp, buff.toString());
		Row historyRow = new Row(new Table("CNListHistory"), String.valueOf(currentTimestamp));
		historyRow.putCell(listCell);
		historyRow.persist();
	}

}
