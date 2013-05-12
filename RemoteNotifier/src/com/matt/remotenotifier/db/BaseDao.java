package com.matt.remotenotifier.db;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Base class for all other DAO classes
 * @author mattm
 *
 */
public class BaseDao {
	
	  protected SQLiteDatabase database;
	  protected DatabaseHelper dbHelper;
	  
	  protected void open() throws SQLException {
		  database = dbHelper.getWritableDatabase();
	  }

	  protected void close() {
		  dbHelper.close();
	  }
	  
}
