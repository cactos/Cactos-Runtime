package eu.cactosfp7.runtimemanagement.hbase.cactos;

import java.util.Date;

import eu.cactosfp7.runtimemanagement.hbase.models.Table;

public class StorageSnapshot {

	private final Table cnTable;
	private final Table vmTable;
	private final Date timestamp; // TODO provide way "back in time"
	
	public StorageSnapshot(Table cnTable, Table vmTable){
		this.cnTable = cnTable;
		this.vmTable = vmTable;
		timestamp = new Date(); // current time
	}
	
	public Table getCnTable() {
		return cnTable;
	}
	
	public Table getVmTable() {
		return vmTable;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	
	
}
