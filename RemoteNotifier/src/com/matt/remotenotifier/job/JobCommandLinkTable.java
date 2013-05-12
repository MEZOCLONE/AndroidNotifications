package com.matt.remotenotifier.job;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.matt.remotenotifier.db.BaseLinkTable;
import com.matt.remotenotifier.db.CommandTable;
import com.matt.remotenotifier.db.JobTable;

public class JobCommandLinkTable extends BaseLinkTable {
	private static final String TAG = JobCommandLinkTable.class.getSimpleName();

	public static final String TABLE_NAME = getLinkTabelName(JobTable.TABLE_NAME, CommandTable.TABLE_NAME);
	public static final String COLUMN_JOB_ID = "job_id";
	public static final String COLUMN_COMMAND_ID = "command_id";

	private static final String DATABASE_CREATE = "create table " 
			+ TABLE_NAME
			+ "(" 
			+ COLUMN_ID + " INTEGER primary key autoincrement, "
			+ COLUMN_JOB_ID + " INTEGER not null, " 
			+ COLUMN_COMMAND_ID + " INTEGER not null, "
			+ "FOREIGN KEY(" +COLUMN_JOB_ID+ ") REFERENCES "+JobTable.TABLE_NAME+" ("+JobTable.COLUMN_ID+"), "
			+ "FOREIGN KEY(" +COLUMN_COMMAND_ID+ ") REFERENCES "+CommandTable.TABLE_NAME+" ("+CommandTable.COLUMN_ID+")"
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
