package com.matt.remotenotifier.db;

public class BaseLinkTable extends BaseTable {
	
	private static String linkSuffix = "_link";
	
	protected static String getLinkTabelName(String tbl1, String tbl2){
		String tblOne = tbl1.substring(4);
		String tblTwo = tbl2.substring(4);
		
		return TABLE_PREFIX+tblOne+"_"+tblTwo+linkSuffix;
	}
}
