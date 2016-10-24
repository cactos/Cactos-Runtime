package eu.cactosfp7.cactoscale.runtimemodelupdater.generation;

import java.io.IOException;
import java.util.Map;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import eu.cactosfp7.cactoscale.runtimemodelupdater.modelbuilder.ResultAccessor;


public class MultiResultReader implements ResultAccessor {

	private final Table table;
	private final Map<String, Result> resultMap;
	
	MultiResultReader(Table _table, Map<String, Result> _resultMap) {
		table = _table;
		resultMap = _resultMap;
	}
	
	void closeTable() throws IOException {
		table.close();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getResult(String rowKey, String familyName, String qualifierName, Class<T> clazz, T defVal) {
		Result r = resultMap.get(rowKey);
		if(r == null) return defVal;
		byte[] bValue = r.getValue(Bytes.toBytes(familyName), Bytes.toBytes(qualifierName));
		if(bValue == null) return defVal;
		
		String value = Bytes.toString(bValue);
		Object o = null;
		if(clazz == double.class) o = Double.parseDouble(value);
		if(clazz == long.class)   o = Long.parseLong(value);
		if(clazz == int.class)    o = Integer.parseInt(value);
		if(clazz == String.class) o = value;
		
		if(o == null) throw new IllegalArgumentException();
		return (T) o;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getResultMap(String rowKey, String familyName) {
		Result r = resultMap.get(rowKey);
		if(r == null) return null;
		 return (T) r.getFamilyMap(Bytes.toBytes(familyName));

	}
}
