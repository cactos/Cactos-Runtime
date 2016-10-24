package eu.cactosfp7.runtimemanagement.hbase.cactos.models;

import eu.cactosfp7.runtimemanagement.hbase.models.Cell;
import eu.cactosfp7.runtimemanagement.hbase.models.Row;

public class Model {

	
	/*
	 * Raw data from hbase
	 */
	protected Row row;

	
	public Model(Row row){
		this.row = row;
	}
	
	protected String getContent(Fields key){
		Cell cell = row.getCell(key.toString());
		if(cell == null)
			return "";
		return cell.getContent();
	}	
	
	public interface Fields {
		
	}
	
}
