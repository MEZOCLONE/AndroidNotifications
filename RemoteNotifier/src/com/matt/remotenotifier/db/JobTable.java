package com.matt.remotenotifier.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.matt.remotenotifier.job.JobHolder;

/**
 * The table helper for the {@link JobHolder} objects
 * This helper class is responsible for creating and updating the table schema
 * @author mattm
 *
 */
public class JobTable extends BaseTable {
	
	private static final String TAG = EventTable.class.getSimpleName();
	
	public static final String TABLE_NAME = TABLE_PREFIX + "job";
	public static final String COLUMN_JOB_ID = "job_id";
	public static final String COLUMN_JOB_PROGRESS = "progress";
	public static final String COLUMN_DEVICE_ID = "device_id";
	public static final String COLUMN_JOB_COMPLETE = "complete";
	public static final String COLUMN_JOB_RECIEVED = "revieved";
	public static final String COLUMN_JOB_STATUS = "status";
	public static final String COLUMN_JOB_CREATED_TIME = "time_created";
	public static final String COLUMN_JOB_RETURN_VALUE = "return_value";
	public static final String COLUMN_JOB_NAME = "name";
	public static final String COLUMN_JOB_RUN_DATE_TIME = "run_date_time";
	public static final String COLUMN_JOB_COMMAND_ID = "command_id";

	 private static final String DATABASE_CREATE = "create table " 
		      + TABLE_NAME
		      + "(" 
		      + COLUMN_ID + " INTEGER primary key autoincrement, " 
		      + COLUMN_JOB_ID + " INTEGER, "
		      + COLUMN_JOB_PROGRESS + " INTEGER, " 
		      + COLUMN_DEVICE_ID + " INTEGER not null, " 
		      + COLUMN_JOB_COMPLETE + " INTEGER, " // boolean
		      + COLUMN_JOB_RECIEVED + " INTEGER, " // boolean
		      + COLUMN_JOB_STATUS + " INTEGER, " 
		      + COLUMN_JOB_CREATED_TIME + " INTEGER not null, "
		      + COLUMN_JOB_RETURN_VALUE + " TEXT, "
		      + COLUMN_JOB_NAME + " TEXT not null, "
		      + COLUMN_JOB_RUN_DATE_TIME + " TEXT, "
		      + COLUMN_JOB_COMMAND_ID + " INTEGER, "
		      + "FOREIGN KEY(" +COLUMN_JOB_COMMAND_ID+ ") REFERENCES "+CommandTable.TABLE_NAME+" ("+CommandTable.COLUMN_ID+")"
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
