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

public class StateChecker {

	private static final int CN_TIMEOUT = 45*1000; // milliseconds!
	private static final int VM_TIMEOUT = 45*1000; // milliseconds!

	public static void start(Properties prop){
		int initialDelay = 2;
		int period = 10;
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				try {
					validateComputenodes();
					validateVirtualmachines();
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}

		}, initialDelay, period, TimeUnit.SECONDS);		
	}
	
	protected static void validateComputenodes(){
		Table snap = RestRequest.getTable("CNSnapshot");
		//System.out.println(snap);
		for (Entry<String, Row> entry : snap.getRows().entrySet()) {
			Row row = entry.getValue();
			boolean isValid = validateRow(row, CN_TIMEOUT);
			System.out.println(row.getRowKey() + " isValid " + isValid);
			Cell currentStateCell = row.getCell("meta:state");
			if(currentStateCell != null && (currentStateCell.getContent().equals("off") /* || currentStateCell.getContent().equals("maintenance") */)){
					// don't touch host if it is "off" or "maintenance"
					continue;
			}
			
			long currentTimestamp = new java.util.Date().getTime();
			Cell newStateCell;
			if(currentStateCell == null){
				// unknown host, set to "maintenance"
				newStateCell = new Cell("meta:state", currentTimestamp, "maintenance");
			}else{
				newStateCell = new Cell("meta:state", currentTimestamp, isValid ? "running" : "failure");
				if(currentStateCell != null && currentStateCell.getContent().equals(newStateCell.getContent())){
					// no change in state
					continue;
				}				
			}

			System.out.println("Change state to " + newStateCell + " (from "
					+ (currentStateCell == null ? "null" : currentStateCell.getContent()) + ")");
			
			// persist state in snapshot table 
			row.putCell(newStateCell);
			
			// persist state in history table
			Row historyRow = new Row(new Table("CNHistory"), row.getRowKey() + "-" + currentTimestamp);
			historyRow.putCell(newStateCell);
			historyRow.persist();
		}
	}

	protected static void validateVirtualmachines(){
		Table snap = RestRequest.getTable("VMSnapshot");
		for (Entry<String, Row> entry : snap.getRows().entrySet()) {
			Row row = entry.getValue();
			boolean isValid = validateRow(row, VM_TIMEOUT);
			System.out.println(row.getRowKey() + " isValid " + isValid);
			Cell currentStateCell = row.getCell("meta:vm_state");

			// update only when running but invalid
			if(currentStateCell == null || (currentStateCell.getContent().equals("running") && !isValid)){
				
				// not valid, set to failure
				long currentTimestamp = new java.util.Date().getTime();			
				Cell newStateCell = new Cell("meta:vm_state", currentTimestamp, "failure");
				System.out.println("Change state to " + newStateCell.getContent() + " (from " + currentStateCell.getContent() + ")");
				
				// persist state in snapshot table 
				row.putCell(newStateCell);
				
				// persist state in history table
				Row historyRow = new Row(new Table("VMHistory"), row.getRowKey() + "-" + currentTimestamp);
				historyRow.putCell(newStateCell);
				historyRow.persist();
				
			}
		}
	}
	
	private static boolean validateRow(Row row, int timeout){
		long latestTimestamp = 0;
		long currentTimestamp = new java.util.Date().getTime();
		for (Entry<String, Cell> entry : row.getCells().entrySet()) {
			Cell cell = entry.getValue();

			// check if timestamp is valid
			if (Math.abs(currentTimestamp - cell.getTimestamp()) > 3600*1000) {
				// invalid, skip row
				System.out.println("skip row " + Math.abs(currentTimestamp - cell.getTimestamp()));
				continue;
			}

			// check if timestamp is newer than latest timestamp
			if (latestTimestamp < cell.getTimestamp()) {
				latestTimestamp = cell.getTimestamp();
			}
		}
		// define node state
		boolean nodeRunning = (Math.abs(latestTimestamp-currentTimestamp) <= timeout);
		return nodeRunning;
	}	

	
}
