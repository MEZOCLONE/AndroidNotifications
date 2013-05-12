package com.matt.remotenotifier.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DeviceTable extends BaseTable {

	private static final String TAG = DeviceTable.class.getSimpleName();

	public static final String TABLE_NAME = TABLE_PREFIX + "device";
	public static final String COLUMN_DEVICE_NAME = "name";
	public static final String COLUMN_DEVICE_TYPE = "type";
	public static final String COLUMN_LAST_HEARTBEAT_TIME = "lastHeatbeatTime";
	public static final String COLUMN_HAS_HEARTBEAT = "hasHeartbeat";

	private static final String DATABASE_CREATE = "create table " 
			+ TABLE_NAME
			+ "(" 
			+ COLUMN_ID + " INTEGER primary key autoincrement, "
			+ COLUMN_DEVICE_NAME + " TEXT not null, " 
			+ COLUMN_DEVICE_TYPE + " TEXT not null, " 
			+ COLUMN_LAST_HEARTBEAT_TIME + " INTEGER, "
			+ COLUMN_HAS_HEARTBEAT + " INTEGER"
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
