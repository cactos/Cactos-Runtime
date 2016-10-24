package eu.cactosfp7.runtimemanagement.hbase.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.cactosfp7.runtimemanagement.hbase.RestRequest;

public class Row {

	
	private Map<String, Cell> cells = new HashMap<String, Cell>();
	private final String rowKey;
	private final Table table;
	private boolean persisted = false;
	
	
	public Row(Table table, String rowKey) {
		super();
		this.table = table;
		this.rowKey = rowKey;
	}
	
	protected void addCell(Cell cell){
		cells.put(cell.getColumn(), cell);
	}

	public void putCell(Cell cell){
		if(persisted)
			RestRequest.putCell(table, this, cell);
		cells.put(cell.getColumn(), cell);
	}
	
	public Map<String, Cell> getCells() {
		return cells;
	}
	
	public Cell getCell(String column){
		return cells.get(column);
	}
	
	public Map<String, Cell> findCells(String columnRegexp){
		Map<String, Cell> matches = new HashMap<String, Cell>();
		for (Entry<String, Cell> entry : this.getCells().entrySet()) {
			if(entry.getKey().matches(columnRegexp)){
				matches.put(entry.getKey(), entry.getValue());
			}
		}
		return matches;
	}

	public String getRowKey() {
		return rowKey;
	}
	
	public void persist(){
		RestRequest.putRow(table, this);
	}	
	
	@Override
	public String toString() {
		return "Row " + rowKey + " with cells: \n" + cells + "\n";
	}

	public String toXml(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("<Row key=\"" + Util.encr(rowKey) + "\">");
		for (Entry<String, Cell> entry : this.getCells().entrySet()) {
			buffer.append(entry.getValue().toXml());
		}
		buffer.append("</Row>");
		return buffer.toString();
	}
	
	public static Row getFromDom(Table table, Node node){
		String rowKey = Util.decr(node.getAttributes().getNamedItem("key").getTextContent());
		Row row = new Row(table, rowKey);
		NodeList cells = node.getChildNodes();
        for (int i = 0; i < cells.getLength(); i++) {
                Cell cell = Cell.getFromDom(cells.item(i));
                row.addCell(cell);
        }
        row.persisted = true;
		return row;
	}
	
}
