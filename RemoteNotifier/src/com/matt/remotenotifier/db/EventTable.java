package com.matt.remotenotifier.db;

import com.matt.remotenotifier.event.EventHolder;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * The table helper for the {@link EventHolder}
 * This helper class is responsible for creating and updating the table schema
 * @author mattm
 *
 */
public class EventTable extends BaseTable {
	
	private static final String TAG = EventTable.class.getSimpleName();
	
	public static final String TABLE_NAME = TABLE_PREFIX + "event";
	public static final String COLUMN_MAIN_TEXT = "main_text";
	public static final String COLUMN_SUB_TEXT = "sub_text";
	public static final String COLUMN_ICON = "icon_resid";
	public static final String COLUMN_ALPHA = "row_alpha";
	public static final String COLUMN_TIME = "time";
	

	 private static final String DATABASE_CREATE = "create table " 
		      + TABLE_NAME
		      + "(" 
		      + COLUMN_ID + " INTEGER primary key autoincrement, " 
		      + COLUMN_MAIN_TEXT + " TEXT not null, " 
		      + COLUMN_SUB_TEXT + " TEXT not null, " 
		      + COLUMN_ICON + " INTEGER not null, "
		      + COLUMN_ALPHA + " INTEGER not null, "
		      + COLUMN_TIME + " INTEGER not null"
		      + ");";
	 
	 public static void onCreate(SQLiteDatabase database) {
		 Log.i(TAG,"Creating db table [" +TABLE_NAME+ "]");
		 database.execSQL(DATABASE_CREATE);
	 }
	 
	 public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		 Log.i(TAG, "Upgrading database table [" +TABLE_NAME+ "] from version [" +oldVersion+ "] to [" +newVersion+ "]");
	    database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
	    onCreate(database);
	 }
	 
}
