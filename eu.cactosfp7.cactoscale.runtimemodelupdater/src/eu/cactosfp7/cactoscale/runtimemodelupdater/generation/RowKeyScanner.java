package eu.cactosfp7.cactoscale.runtimemodelupdater.generation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import eu.cactosfp7.cactoscale.runtimemodelupdater.SettingsInitializer;
import eu.cactosfp7.cactoscale.runtimemodelupdater.modelbuilder.RowKeyName;

final class RowKeyScanner {

	private final static Logger logger = Logger.getLogger(RowKeyScanner.class);

	private final Table table;
	private volatile boolean closed = false;

	private final Map<String, List<RowKeyName>> scansMap = new HashMap<String, List<RowKeyName>>();
	private final Scan scan;

	RowKeyScanner(Table _table) throws IOException {
		table = _table;
		scan = initScan();
	}

	private static final Scan initScan() throws IOException {
		final long maxTimestamp = System.currentTimeMillis();
		Scan s = new Scan();
		//Filter which row keys needed to be scanned
		FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ONE);
		String filteredComputenodesString = SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.FILTERED_COMPUTE_NODES);
		if (filteredComputenodesString != null) {
			String[] filteredComputenodes = filteredComputenodesString.split(";");
			for (int i = 0; i < filteredComputenodes.length; i++) {
				RowFilter filter = new RowFilter(CompareOp.EQUAL, new BinaryComparator(Bytes.toBytes(filteredComputenodes[i])));
				filterList.addFilter(filter);
			}
		}
		s.setFilter(filterList);
		//s.setTimeRange(maxTimestamp - Long.parseLong(SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.SCAN_TIME_OFFSET)), maxTimestamp);
		return s;
	}

	synchronized void prepareRowScan(String family, String column) {
		final String id = family + "?" + column;
		scansMap.put(id, new ArrayList<RowKeyName>());
		scan.addColumn(Bytes.toBytes(family), Bytes.toBytes(column));
	}

	synchronized void scanAllColumns() throws IOException {
		ResultScanner nodeScanner = null;
		try {
			nodeScanner = table.getScanner(scan);
			loopResults(nodeScanner);
		} catch (Throwable a) {
			logger.error("Exception while scanning columns: ", a);
		} finally {
			if (nodeScanner != null)
				nodeScanner.close();
		}
		return;
	}

	private final void loopResults(ResultScanner nodeScanner) throws IOException {
		for (Iterator<Result> results = nodeScanner.iterator(); results.hasNext();) {
			Result result = results.next();
			loopKeys(result);
		}
	}

	private final void loopKeys(Result rr) {
		Cell[] kvs = rr.rawCells();
		for (Cell kv : kvs) {
			handleKey(kv);
		}
	}

	private final void handleKey(Cell kv) {
		final List<RowKeyName> o = lookup(Bytes.toString(CellUtil.cloneFamily(kv)), Bytes.toString(CellUtil.cloneQualifier(kv)));
		o.add(new RowKeyName(Bytes.toString(CellUtil.cloneRow(kv))));
	}

	public List<RowKeyName> lookup(String family, String quantifier) {
		final String id = family + "?" + quantifier;
		List<RowKeyName> o = scansMap.get(id);
		if (o == null)
			throw new IllegalStateException("received result that was not asked for: " + id);
		// FIXME: return copy for outside world?
		return o;
	}

	synchronized void close() {
		if (closed)
			return;
		closeTable();
	}

	private void closeTable() {
		try {
			closed = true;
			table.close();
		} catch (Throwable e) {
			logger.error("Exception while closing table! ", e);
		}
	}
}

