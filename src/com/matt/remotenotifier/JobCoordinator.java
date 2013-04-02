package com.matt.remotenotifier;

import java.io.NotActiveException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.ParseException;
import android.text.format.DateFormat;
import android.util.Log;

import com.matt.remotenotifier.DeviceCoordinator.CommandHolder;
import com.matt.remotenotifier.DeviceCoordinator.DeviceHolder;
import com.matt.remotenotifier.DeviceCoordinator.DeviceType;


/*
 * Singleton instance - Use getInstance
 */
public class JobCoordinator {
	private static String TAG = "JobCoordinator";
	private String registeredChannelName;
	private static JobCoordinator instance;
	private DeviceCoordinator deviceCoordinator;
	private Pusher mPusher;
	private ArrayList<JobHolder> jobList;
	private int jobCount;
	
	class JobHolder{

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((jobName == null) ? 0 : jobName.hashCode());
			result = prime * result
					+ (int) (jobCreateTime ^ (jobCreateTime >>> 32));
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			JobHolder other = (JobHolder) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (jobName == null) {
				if (other.jobName != null)
					return false;
			} else if (!jobName.equals(other.jobName))
				return false;
			if (jobCreateTime != other.jobCreateTime)
				return false;
			return true;
		}

		private int jobId;
		private int jobProgress;
		private int deviceId;
		private boolean jobComplete;
		private boolean jobRecieved;
		private int jobStatus;
		private long jobCreateTime;
		private String jobRetval;
		private String jobName;
		private String runDateTime;
		private CommandHolder jobCommandHolder;
		
		public JobHolder(String jobName, CommandHolder jobCommand, int deviceId){
			this.jobName = jobName;
			this.deviceId = deviceId;
			this.jobCommandHolder = jobCommand;
			jobCreateTime = System.currentTimeMillis();
			jobComplete = false;
			jobRecieved = false;
			jobProgress = 0;	
			jobStatus = 0;
		}

		private JobCoordinator getOuterType() {
			return JobCoordinator.this;
		}

		/**
		 * @return the jobProgress
		 */
		public int getJobProgress() {
			return jobProgress;
		}

		/**
		 * @return the jobComplete
		 */
		public boolean isJobComplete() {
			return jobComplete;
		}

		/**
		 * @param jobProgress the jobProgress to set
		 */
		public void setJobProgress(int jobProgress) {
			this.jobProgress = jobProgress;
		}

		/**
		 * @return the jobId
		 */
		public int getJobId() {
			return jobId;
		}

		/**
		 * @param jobId the jobId to set
		 * IMPORTANT: This must be called directly after the constructor
		 */
		public void setJobId(int jobId) {
			this.jobId = jobId;
		}
		
		public int getDeviceId(){
			return deviceId; 
		}
		
		public boolean isJobRecieved(){
			return jobRecieved;
		}
		
		/**
		 * @param set jobComplete to true
		 */
		public void setJobComplete() {
			jobComplete = true;
		}

		/**
		 * @param set jobRecieved to true
		 */
		public void setJobRecieved() {
			jobRecieved = true;
		}

		public CommandHolder getCommandHolder(){
			return jobCommandHolder;
		}

		public String getJobName() {
			return jobName;
		}

		/**
		 * @return the jobCreateTime
		 */
		public long getJobCreateTime() {
			return jobCreateTime;
		}

		/**
		 * @return the jobStatus
		 * 1: Job Received (Not Complete)
		 * 2: Job Complete
		 * 3: Job error
		 * 0: Unknown Job Status
		 */
		public int getJobStatus() {
			return jobStatus;
		}

		/**
		 * @param jobStatus the jobStatus to set
		 * 0: Job not yet started
		 * 1: Job currently running on device
		 * 2: Job completed okay
		 * 3: Job in error
		 */
		public void setJobStatus(int jobStatus) {
			this.jobStatus = jobStatus;
		}

		/**
		 * @return the jobRetval
		 */
		public String getJobRetval() {
			return jobRetval;
		}

		/**
		 * @param jobRetval the jobRetval to set
		 */
		public void setJobRetval(String jobRetval) {
			this.jobRetval = jobRetval;
		}

		/**
		 * @return the runDateTime
		 */
		public String getRunDateTime() {
			return runDateTime;
		}

		/**
		 * @param runDateTime the runDateTime to set
		 */
		public void setRunDateTime(String runDateTime) {
			this.runDateTime = runDateTime;
		}
	}
	
	protected JobCoordinator(Pusher pusher, String registeredChannelName) {
		jobCount = 0;
		mPusher = pusher;
		jobList = new ArrayList<JobHolder>();
		this.registeredChannelName = registeredChannelName;
		getDeviceCoodinatorInstance();
		Log.d(TAG, "Job Coodinator Started Okay");
	}

	static public JobCoordinator getInstance() throws NotActiveException{
		if(instance != null){
			return instance;
		}else{
			throw new NotActiveException("Job Coordinator not yet active");
		}
	}
	
	static public JobCoordinator getInstance(Pusher pusher, String registeredChannelName){
		if(instance != null){
			return instance;
		}else{
			return instance = new JobCoordinator(pusher, registeredChannelName);
		}
	}
	
	private void getDeviceCoodinatorInstance(){
		if(deviceCoordinator == null){
			try {
				deviceCoordinator = DeviceCoordinator.getInstance();
			} catch (NotActiveException e) {
				Log.w(TAG, e.getMessage());
			}
		}else{
			Log.d(TAG, "Device Coodinator is already assigned");
		}
	}
	
	public String getJobName(int jobId) throws Exception {
		JobHolder jh = jobList.get(jobId);
		if(jh != null){
			return jh.getJobName();
		}else{
			throw new Exception("JobHolder not found for jobId ["+jobId+"]");
		}
		
	}
	
	public JobHolder getJobHolder(int jobId){
		return jobList.get(jobId);
	}
	
	public String getJobRetval(int jobId) throws Exception{
		JobHolder jh = jobList.get(jobId);
		if(jh != null){
			return jh.getJobRetval();
		}else{
			throw new Exception("JobHolder not found for jobId ["+jobId+"]");
		}
	}

	/**
	 * @return the jobCount
	 */
	public int getJobCount() {
		return jobCount;
	}
	
	public JobHolder getLatestJobHolder(CommandHolder ch){
		if(ch.getAssociatedJobCount() > 0){
			ArrayList<JobHolder> sJh = new ArrayList<JobHolder>();
			for(int i=0; i < ch.getAssociatedJobCount(); i++){
				if ((getJobHolder(ch.getAssociatedJobId(i)).isJobComplete() == false) &&  (getJobHolder(ch.getAssociatedJobId(i)).getJobStatus() != 3)){
					sJh.add(getJobHolder(ch.getAssociatedJobId(i)));
				}
			}
			
			if(sJh.size() == 0) {
				Log.d(TAG, "All jobs for this commandHolder are complete");
				return null;
			}
			
			Collections.sort(sJh, new Comparator<JobHolder>() {
	            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	            @Override
	            public int compare(JobHolder jh1, JobHolder jh2) {
	                try {
	                    Date d1 = df.parse(jh1.getRunDateTime());
	                    Date d2 = df.parse(jh2.getRunDateTime());
	                    return d1.compareTo(d2);
	                } catch (ParseException pe) {
	                    Log.e(TAG, "Error comparing JobHolders");
	                    return 0;
	                } catch (java.text.ParseException e) {
	                	Log.e(TAG, "Error comparing JobHolders");
		                return 0;
					}
	            }
	        });
			
			return sJh.get(0);
			
		}else{
			//Log.d(TAG, "CommandHolder has no assoicated jobs. Returning Null");
			return null;
		}
	}
	
	/**
	 * Creates a job with the given params. Returns the JobId or -1 if fails
	 * @param jobName
	 * @param commandHolder
	 * @param deviceId
	 * @return the jobId
	 */
	public int createJob(String jobName, CommandHolder commandHolder, int deviceId){
		int jobId;
		JobHolder jh = new JobHolder(jobName, commandHolder, deviceId);
		if(!jobList.contains(jh)){
			jobList.add(jh);
			jobId = jobList.indexOf(jh);
			jh.setJobId(jobId);
			Log.i(TAG, "Created job with Id ["+jh.getJobId()+"]");
			return jobId;
		}
		Log.w(TAG, "Job ["+jobName+"] from device Id ["+deviceId+"] already exists");
		return -1;
	}
	
	public int createTimedJob(String jobName, CommandHolder commandHolder, int deviceId, String runDateTime){
		int jobId;
		JobHolder jh = new JobHolder(jobName, commandHolder, deviceId);
		if(!jobList.contains(jh)){
			jobList.add(jh);
			jobId = jobList.indexOf(jh);
			jh.setJobId(jobId);
			jh.setRunDateTime(runDateTime);
			Log.i(TAG, "Created job with Id ["+jh.getJobId()+"]");
			return jobId;
		}
		Log.w(TAG, "Job ["+jobName+"] from device Id ["+deviceId+"] already exists");
		return -1;
	}
	
	/**
	 * Executes the given jobId (creates a new thread)
	 * @param jobId
	 */
	public void executeJob(final int jobId){
		getDeviceCoodinatorInstance();
		Thread executeJobThread = new Thread(new Runnable() {
			@Override
			public void run() {
				JobHolder jh = jobList.get(jobId);
				if(jh != null){
					CommandHolder ch = jh.getCommandHolder();
					DeviceHolder dh = deviceCoordinator.getDeviceHolder(jh.getDeviceId());
					try {
						Log.i(TAG, "Excuting Job with Id ["+jobId+"]");
						JSONObject jObject = new JSONObject("{deviceName: "+dh.getDeviceName()+", deviceType: "+dh.getDeviceType().toString()+", command: "+ch.getCommand()+", jobId: "+jobId+"}");
						
						if(ch.getArgsCount() > 0){
							Log.d(TAG, "Adding argument values to JSONObject");
							JSONObject aObj; 
							JSONArray argArray = new JSONArray();
							for (int i=0; i < ch.getArgsCount(); i++){
								aObj = new JSONObject("{name: "+ch.getArgumentName(i)+", type: "+ch.getArgumentType(i)+", value: "+ch.getArgumentValue(i)+"}");
								argArray.put(aObj);
							}
							jObject.put("args", argArray);
						}
						
						if(jh.getRunDateTime() != null){
							jObject.put("dateTime", jh.getRunDateTime());
							mPusher.sendEvent("client-execute_timed_job", jObject, registeredChannelName);
						}else{
							mPusher.sendEvent("client-execute_job", jObject, registeredChannelName);
						}
						
						synchronized(this){
							wait(60000);
						}
						
						if(!jh.isJobRecieved()){
							Log.w(TAG, "Job ["+jobId+"] on device ["+dh.getDeviceName()+"] took too long to respond with Ack. Failing Job");
							jh.setJobStatus(3);
							//Toast toast = Toast.makeText(mainActivityContext, "Error with job ["+jh.getJobName()+"]", Toast.LENGTH_SHORT);
						}else{
							jh.setJobStatus(1);
						}
					} catch (JSONException e) {
						Log.e(TAG, e.getMessage());
					} catch (InterruptedException e) {
						Log.e(TAG, "Job execute interrupted");
						Log.d(TAG, e.getMessage());
					}	
				}else{
					Log.e(TAG, "Job with Id ["+jobId+"] does not exist");
				}
			}
		});
		executeJobThread.start();
	}
	
	/**
	 * Handles the incoming Acks for Jobs
	 * @param deviceName
	 * @param deviceType
	 * @param jobId
	 * @return job status
	 * 1: Job Received (Not Complete)
	 * 2: Job Complete
	 * 3: Job error
	 * 0: Unknown Job Status
	 */
	public int handleJobAck(String deviceName, DeviceType deviceType, int jobId, String jobRetval){
		JobHolder jh = jobList.get(jobId);
		if(jh != null){
			if(jh.getJobStatus() != 3){
				if(!jh.isJobRecieved()){
					Log.i(TAG, "Job with jobId ["+jobId+"] has been received & and scheduled");
					jh.setJobRecieved();
					jh.setJobStatus(1);
					return 1;
				}else{
					Log.i(TAG, "Job with jobId ["+jobId+"] is complete");
					jh.setJobComplete();
					jh.setJobStatus(2);
					deviceCoordinator.updateControl();
					if(jobRetval != null){
						Log.i(TAG, "Job returned data. Adding this to jobHolder");
						jh.setJobRetval(jobRetval);
					}
					return 2;
				}			
			}else{
				Log.w(TAG, "Previously errored job ["+jobId+"] has suddenly responded. Jobs status is now unknown!");
				return 0;
			}
		}	
		Log.wtf(TAG, "Unknown status for job ["+jobId+"]");
		return 0;
	}

	public boolean failJob(int jobId) {
		JobHolder jh = jobList.get(jobId);
		if(jh != null){
			if(jh.getJobStatus() != 3){
				Log.i(TAG, "Job ["+jobId+"] was reported failed. Setting failed");
				jh.setJobStatus(3);
				return true;
			}else{
				Log.w(TAG, "Job ["+ jobId +"] has already failed");
				return false;
			}
		}
		Log.w("TAG", "Failed to get jobHandler for job Id ["+jobId+"]");
		return false;
	}
}
