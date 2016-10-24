package eu.cactosfp7.runtimemanagement.hbase;

import eu.cactosfp7.runtimemanagement.hbase.models.Table;

public class ExampleQueries {

	public static void main(String... args){

		Table tab = RestRequest.getTable("VMSnapshot");
		System.out.println(tab);
		
	}
	
//	private static void printCellNames(){
//		Table snap = RestRequest.getTable("CNSnapshot");
//		//System.out.println(snap);
//		for (Entry<String, Row> entry : snap.getRows().entrySet()) {
//			Row row = entry.getValue();
//			System.out.println("\n" + row.getRowKey());
//			for (Entry<String, Cell> entryCell : row.getCells().entrySet()) {
//				Cell cell = entryCell.getValue();
//				System.out.println(cell.getColumn()  + " \t\t\t= " + cell.getContent());
//			}
//		}
//	}
	
//	private static void historyExperiments(){
//		Table tab;
//		
//		//tab = RestRequest.getRow("CNSnapshot", "*", "starttime=1454600060000&endtime=1454600075999&limit=1");
//		
//		tab = RestRequest.getRow("CNHistory", "*", "starttime=1454600060000&endtime=1454600065999&column=hardware_util:cpu_sys");
//		//tab = RestRequest.getRow("CNHistory", "*", "startrow=&endrow=&limit=1&column=hardware_util:cpu_sys");
//		
////		tab = tab.condense();
////		System.out.println(tab);
//		for (Entry<String, Row> entry : tab.getRows().entrySet()) {
//			System.out.println(entry.getKey() + "\t" + entry.getValue());
//			
//		}
//
//	}
	
}
