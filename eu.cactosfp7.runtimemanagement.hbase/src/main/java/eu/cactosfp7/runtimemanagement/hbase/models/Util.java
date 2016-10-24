package eu.cactosfp7.runtimemanagement.hbase.models;

import javax.xml.bind.DatatypeConverter;

public class Util {

	public static String decr(String s){
		return new String(DatatypeConverter.parseBase64Binary(s));
	}
	
	public static String encr(String s){
		return DatatypeConverter.printBase64Binary(s.getBytes());
	}	
	
}
