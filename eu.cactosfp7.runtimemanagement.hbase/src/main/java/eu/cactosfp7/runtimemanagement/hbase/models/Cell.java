package eu.cactosfp7.runtimemanagement.hbase.models;

import org.w3c.dom.Node;

public class Cell {

	private String column;
	private long timestamp;
	private String content;
	//private boolean persisted = false;
	
	public Cell(String column, long timestamp, String content) {
		super();
		this.column = column;
		this.timestamp = timestamp;
		this.content = content;
	}

	public String getColumn() {
		return column;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getContent() {
		return content;
	}

	@Override
	public String toString() {
		return "\tcolumn=" + column + ", timestamp=" + timestamp + ", content=" + content + "\n";
	}
	
	public String toXml(){
		return "<Cell column=\"" + Util.encr(column) + "\" timestamp=\"" + timestamp + "\">" + Util.encr(content) + "</Cell>";
	}

	public static Cell getFromDom(Node node){
        String attr_column = Util.decr(node.getAttributes().getNamedItem("column").getTextContent());
        long attr_timestamp = Long.valueOf(node.getAttributes().getNamedItem("timestamp").getTextContent());
        String content = Util.decr(node.getTextContent());
        Cell cell = new Cell(attr_column, attr_timestamp, content);
        //cell.persisted = true;
        return cell;
	}
	
}
