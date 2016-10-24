package eu.cactosfp7.runtimemanagement.hbase;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.cactosfp7.runtimemanagement.hbase.models.Cell;
import eu.cactosfp7.runtimemanagement.hbase.models.Row;
import eu.cactosfp7.runtimemanagement.hbase.models.Table;
import eu.cactosfp7.runtimemanagement.hbase.models.Util;

public class RestRequest {

	public static String host = "134.60.64.143";
	public static int port = 8081;
	
	private final static DocumentBuilderFactory factory;
	private static DocumentBuilder builder;
	static {
		factory = DocumentBuilderFactory.newInstance();
		
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			builder = null;
		}
	}
	
	public static Table getTable(String name) {
		return getTable(name, "*");
	}

	public static Table getTable(String tableName, String rowkey) {
		return getTable(tableName, rowkey, "");
	}
	
	public static Table getTable(String tableName, String rowkey, String scannerProperties) {
		HttpClient httpClient = createHttpClient();
		try {
			String query = "http://" + host + ":" + port + "/" + tableName + "/" + rowkey + "?" + scannerProperties;
			//System.out.println(query);
			GetMethod getMethod = new GetMethod(query);
			getMethod.addRequestHeader("accept", "text/xml");
			try {
//				long startTime = System.currentTimeMillis();
				httpClient.executeMethod(getMethod);
//				long diff = System.currentTimeMillis() - startTime;
//				System.out.println("Query " + query + " took " + diff + "ms");
			} catch (HttpException e) {
				throw new RuntimeException("Cannot get table: http get request execution failed.", e);
			}

			if (getMethod.getStatusCode() != 200) {
				throw new RuntimeException("Status Code not 200 OK: " + getMethod.getStatusCode());
			}
			
//			long startTime = System.currentTimeMillis();
			InputStream responseInputStream = getMethod.getResponseBodyAsStream();
			Document doc;
			try {
				doc = builder.parse(responseInputStream);
			} catch (SAXException e) {
				throw new RuntimeException("Cannot get table: parsing error for xml content.", e);
			}
//			long diff = System.currentTimeMillis() - startTime;
//			System.out.println("DocumentBuilder took " + diff + "ms");
			
			Node tableNode = doc.getDocumentElement();
			Table table = Table.getFromDom(tableName, tableNode);
			return table;
		} catch (IOException e) {
			throw new RuntimeException("Cannot get table: some I/O issue happened.", e);
		}
	}

	private static HttpClient createHttpClient() {
		HostConfiguration hostConfiguration = new HostConfiguration();
		hostConfiguration.setHost(host, port);
		HttpClient httpClient = new HttpClient();
		httpClient.setHostConfiguration(hostConfiguration);
		return httpClient;
	}

	public static void putCell(Table table, Row row, Cell cell) {
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>" + "<CellSet>" + "<Row key=\""
				+ Util.encr(row.getRowKey()) + "\">" + cell.toXml() + "</Row>" + "</CellSet>";
		String url = "http://" + host + ":" + port + "/" + table.getName() + "/" + row.getRowKey() + "/" + cell.getColumn();
		putRequest(url, content);
	}

	public static void putRow(Table table, Row row) {
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>" + "<CellSet>" + 
				row.toXml() + "</CellSet>";
		String url = "http://" + host + ":" + port + "/" + table.getName() + "/" + row.getRowKey();		
		putRequest(url, content);		
	}

	public static void putTable(Table table) {
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>" + table.toXml();
		String url = "http://" + host + ":" + port + "/" + table.getName();
		putRequest(url, content);
	}

	private static void putRequest(String url, String content){
		HttpClient httpClient = createHttpClient();
		try {
			PutMethod putMethod = new PutMethod(url);
			putMethod.setRequestEntity(new StringRequestEntity(content, "text/xml", "UTF-8"));
			try {
				httpClient.executeMethod(putMethod);
			} catch (HttpException e) {
				throw new RuntimeException("Cannot put cell: http put request execution failed.", e);
			}

			if (putMethod.getStatusCode() != 200) {
				throw new RuntimeException("Status Code not 200 OK: " + putMethod.getStatusCode());
			}
		} catch (IOException e) {
			throw new RuntimeException("Cannot put cell: some I/O issue happened.", e);
		}		
	}

	public static void deleteRow(Table table, Row row) {
		String url = "http://" + host + ":" + port + "/" + table.getName() + "/" + row.getRowKey();
		deleteRequest(url);
	}
	
	private static void deleteRequest(String url){
		HttpClient httpClient = createHttpClient();
		try {
			DeleteMethod deleteMethod = new DeleteMethod(url);
			try {
				httpClient.executeMethod(deleteMethod);
			} catch (HttpException e) {
				throw new RuntimeException("Cannot put cell: http put request execution failed.", e);
			}

			if (deleteMethod.getStatusCode() != 200) {
				throw new RuntimeException("Status Code not 200 OK: " + deleteMethod.getStatusCode());
			}
		} catch (IOException e) {
			throw new RuntimeException("Cannot put cell: some I/O issue happened.", e);
		}		
	}	
	
}
