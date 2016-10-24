package eu.cactosfp7.runtimemanagement.hbase.cactos;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import eu.cactosfp7.runtimemanagement.hbase.RestRequest;
import eu.cactosfp7.runtimemanagement.hbase.cactos.models.ComputeNode;
import eu.cactosfp7.runtimemanagement.hbase.cactos.models.VirtualMachine;
import eu.cactosfp7.runtimemanagement.hbase.models.Row;
import eu.cactosfp7.runtimemanagement.hbase.models.Table;

public class StorageRequest {

	private static final long MAX_DISTANCE = 121 * 1000; // 121s
	
	public static Map<String, ComputeNode> getComputenodes(StorageSnapshot snapshot) {
		Map<String, ComputeNode> nodes = new HashMap<String, ComputeNode>();
		Table snap = snapshot.getCnTable();
		for (Entry<String, Row> entry : snap.getRows().entrySet()) {
			Row row = entry.getValue();
			ComputeNode node = new ComputeNode(row);
			nodes.put(entry.getKey(), node);
		}
		return nodes;
	}
	
	public static Map<String, VirtualMachine> getVirtualMachines(StorageSnapshot snapshot) {
		Map<String, VirtualMachine> nodes = new HashMap<String, VirtualMachine>();
		Table snap = snapshot.getVmTable();
		for (Entry<String, Row> entry : snap.getRows().entrySet()) {
			Row row = entry.getValue();
			VirtualMachine node = new VirtualMachine(row);
			nodes.put(entry.getKey(), node);
		}
		return nodes;
	}

	public static StorageSnapshot loadSnapshot() {
		Table snapCn = RestRequest.getTable("CNSnapshot");
		Table snapVm = RestRequest.getTable("VMSnapshot");
		return new StorageSnapshot(snapCn, snapVm);
	}
	

	public static StorageSnapshot loadSnapshot(Date date) {
		return loadAggregatedSnapshot(date, 0, GroupFunction.NEWEST);
	}
	
	public static StorageSnapshot loadAggregatedSnapshot(Date start, long secondsBack, GroupFunction function) {
		long ts_start = start.getTime();
		//long ts_end = ts_start - secondsBack - MAX_DISTANCE;
		long ts_end = ts_start + MAX_DISTANCE;
		
		Table historyCn = RestRequest.getTable("CNHistory", "*", "starttime="+ ts_start +"&endtime=" + ts_end);
		Table snapCn = historyCn.condense(function);
		Table historyVm = RestRequest.getTable("VMHistory", "*", "starttime="+ ts_start +"&endtime=" + ts_end);
		Table snapVm = historyVm.condense(function);
		
		return new StorageSnapshot(snapCn, snapVm);
	}	
	
	public enum GroupFunction {
		AGGR_MIN,
		AGGR_MAX,
		AGGR_AVG,
		NEWEST
	}
	

}
