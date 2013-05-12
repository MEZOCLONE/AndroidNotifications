package com.matt.remotenotifier.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ArgumentTable extends BaseTable {
	private static final String TAG = ArgumentTable.class.getSimpleName();

	public static final String TABLE_NAME = TABLE_PREFIX + "argument";
	public static final String COLUMN_COMMAND_ID = "command_id";
	public static final String COLUMN_TYPE = "type";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_VALUE = "value";

	private static final String DATABASE_CREATE = "create table " 
			+ TABLE_NAME
			+ "(" 
			+ COLUMN_ID + " INTEGER primary key autoincrement, "
			+ COLUMN_COMMAND_ID + " INTEGER not null, "
			+ COLUMN_NAME + " INTEGER not null, "
			+ COLUMN_TYPE + " INTEGER not null, " 
			+ COLUMN_VALUE + " VARCHAR, "
			+ "FOREIGN KEY(" +COLUMN_COMMAND_ID+ ") REFERENCES "+CommandTable.TABLE_NAME+" ("+CommandTable.COLUMN_ID+") ON DELETE CASCADE"
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
