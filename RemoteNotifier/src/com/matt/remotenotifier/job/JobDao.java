package com.matt.remotenotifier.job;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.matt.remotenotifier.db.BaseDao;
import com.matt.remotenotifier.db.DatabaseHelper;
import com.matt.remotenotifier.db.JobTable;
import com.matt.remotenotifier.device.CommandHolder;

class JobDao extends BaseDao {

	private String[] allColumns = { JobTable.COLUMN_ID, JobTable.COLUMN_JOB_ID, JobTable.COLUMN_JOB_PROGRESS, 
			JobTable.COLUMN_DEVICE_ID, JobTable.COLUMN_JOB_COMPLETE, JobTable.COLUMN_JOB_RECIEVED, JobTable.COLUMN_JOB_STATUS,
			JobTable.COLUMN_JOB_CREATED_TIME, JobTable.COLUMN_JOB_RETURN_VALUE, JobTable.COLUMN_JOB_NAME, 
			JobTable.COLUMN_JOB_RUN_DATE_TIME, JobTable.COLUMN_JOB_COMMAND_ID };

	public JobDao(Context context) {
		dbHelper = new DatabaseHelper(context);
		open();
	}
	
	public JobHolder createJob(String jobName, CommandHolder commandHolder, int deviceId){
		Long time = System.currentTimeMillis();
		ContentValues values = new ContentValues();
		values.put(JobTable.COLUMN_JOB_NAME, jobName);
		values.put(JobTable.COLUMN_DEVICE_ID, deviceId);
		values.put(JobTable.COLUMN_JOB_CREATED_TIME, time);
		
		long insertId = database.insert(JobTable.TABLE_NAME, null, values);
		Cursor cursor = database.query(JobTable.TABLE_NAME, allColumns, JobTable.COLUMN_ID + " = " + insertId, null, null, null, null);
		
		cursor.moveToFirst();
		JobHolder job = cursorToJob(cursor);
		job.setColumnId(insertId);
		job.setJobCreatedTime(time);
		
		return job;
	}
	
	public JobHolder createTimedJob(String jobName, CommandHolder commandHolder, int deviceId, String runDateTime){
		JobHolder job = createJob(jobName, commandHolder, deviceId);
		
		ContentValues values = new ContentValues();
		values.put(JobTable.COLUMN_JOB_RUN_DATE_TIME, runDateTime);
		
		database.update(JobTable.TABLE_NAME, values, JobTable.COLUMN_ID + " = " + job.getColumnId(), null);
		job.setRunDateTime(runDateTime);
		
		return job;		
	}
	
	public JobHolder setJobStatus(int jobId, int jobStatus){
		Cursor cursor = database.query(JobTable.TABLE_NAME, allColumns, JobTable.COLUMN_JOB_ID + " = " + jobId, null, null, null, null);
		JobHolder job = cursorToJob(cursor);
		
		ContentValues values = new ContentValues();
		values.put(JobTable.COLUMN_JOB_STATUS, jobStatus);
		
		database.update(JobTable.TABLE_NAME, values, JobTable.COLUMN_ID + " = " + job.getColumnId(), null);
		job.setJobStatus(jobStatus);
		
		return job;
	}

	private JobHolder cursorToJob(Cursor cursor) {
		JobHolder job = new JobHolder(cursor.getString(9), null, cursor.getInt(3));
		job.setColumnId(cursor.getInt(0));
		return job;
	}
}
