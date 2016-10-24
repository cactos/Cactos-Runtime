package eu.cactosfp7.runtimemanagement.hbase.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.cactosfp7.runtimemanagement.hbase.RestRequest;
import eu.cactosfp7.runtimemanagement.hbase.cactos.StorageRequest.GroupFunction;

public class Table {

	private Map<String, Row> rows = new HashMap<String, Row>();
	private String name;
	private boolean persisted = false;
	
	public Table(String name) {
		super();
		this.name = name;
	}
	
	protected void addRow(Row row){
		rows.put(row.getRowKey(),row);
	}

	public void putRow(Row row){
		if(persisted)
			RestRequest.putRow(this, row);
		rows.put(row.getRowKey(), row);
	}

	public void persist(){
		RestRequest.putTable(this);
	}

	public Map<String, Row> getRows() {
		return rows;
	}

	public Row getRow(String rowkey){
		return rows.get(rowkey);
	}

	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return "Table [name=" + name + "] with rows: \n" + rows;
	}
	
	public String toXml(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("<CellSet>");
		for (Entry<String, Row> entry : this.getRows().entrySet()) {
			buffer.append(entry.getValue().toXml());
		}
		buffer.append("</CellSet>");
		return buffer.toString();
	}

	public static Table getFromDom(String name, Node tableNode){
		Table table = new Table(name);
		NodeList rows = tableNode.getChildNodes();
        for (int i = 0; i < rows.getLength(); i++) {
        	Row row = Row.getFromDom(table, rows.item(i));
        	table.addRow(row);
        }
        table.persisted = true;
        return table;
	}

	/**
	 * Condense a history table to a snapshot table
	 * @return
	 */
	public Table condense(GroupFunction function) {
		Table snap = new Table(getName() + "_condensed");
		for (Entry<String, Row> entry : getRows().entrySet()) {
			// remove "-timestamp" from rowkey
			String rowKey = entry.getKey();
			String nodeName = rowKey.substring(0, rowKey.lastIndexOf("-"));
			
			Row rowSnap = snap.getRow(nodeName);
			Row rowHist = entry.getValue();
			if(rowSnap == null){
				rowSnap = new Row(snap, nodeName);
			}
			for (Entry<String, Cell> entryCells : rowHist.getCells().entrySet()) {
				String cellKey = entryCells.getKey();
				Cell cellHist = entryCells.getValue();
				Cell cellSnap = rowSnap.getCell(cellKey);
				if(cellSnap == null){
					rowSnap.addCell(cellHist);
				}else{
					if(function.equals(GroupFunction.NEWEST)){
						// compare timestamps
						if(cellSnap.getTimestamp() < cellHist.getTimestamp()){
							// update cell with newer one
							rowSnap.addCell(cellHist);
						}
					}else if(function.equals(GroupFunction.AGGR_MAX)){
						// compare values TODO!
//						if(cellSnap.getContent() < cellHist.getContent()){
//							// update cell with larger one
//							rowSnap.addCell(cellHist);
//						}						
					}else if(function.equals(GroupFunction.AGGR_MIN)){
						// compare values TODO!
//						if(cellSnap.getContent() > cellHist.getContent()){
//							// update cell with smaller one
//							rowSnap.addCell(cellHist);
//						}						
					}else if(function.equals(GroupFunction.AGGR_AVG)){
						// TODO
					}
				}
			}
			snap.addRow(rowSnap);
		}
		return snap;
	}
}
