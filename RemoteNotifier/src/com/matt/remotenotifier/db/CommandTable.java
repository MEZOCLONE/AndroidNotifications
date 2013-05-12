package com.matt.remotenotifier.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class CommandTable extends BaseTable {

	private static final String TAG = CommandTable.class.getSimpleName();

	public static final String TABLE_NAME = TABLE_PREFIX + "command";
	public static final String COLUMN_DEVICE_ID = "device_id";
	public static final String COLUMN_COMMAND_NAME = "name";
	public static final String COLUMN_COMMAND_STRING = "command_string";

	private static final String DATABASE_CREATE = "create table " 
			+ TABLE_NAME
			+ "(" 
			+ COLUMN_ID + " INTEGER primary key autoincrement, " 
			+ COLUMN_DEVICE_ID + " INTEGER not null, "
			+ COLUMN_COMMAND_NAME + " TEXT not null, " 
			+ COLUMN_COMMAND_STRING + " TEXT not null, "  
			+ "FOREIGN KEY(" +COLUMN_DEVICE_ID+ ") REFERENCES "+DeviceTable.TABLE_NAME+" ("+DeviceTable.COLUMN_ID+")"
			+ ");";

	public static void onCreate(SQLiteDatabase database) {
		Log.i(TAG, "Creating db table [" + TABLE_NAME + "]");
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.i(TAG, "Upgrading database table [" + TABLE_NAME + "] from version [" + oldVersion + "] to [" + newVersion + "]");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(database);
	}

}
