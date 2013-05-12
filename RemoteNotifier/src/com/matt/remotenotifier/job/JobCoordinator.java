package com.matt.remotenotifier.job;

import java.io.NotActiveException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.matt.pusher.ChannelEventCoordinator;
import com.matt.remotenotifier.AppPreferences;
import com.matt.remotenotifier.device.CommandHolder;
import com.matt.remotenotifier.device.DeviceCoordinator;
import com.matt.remotenotifier.device.DeviceHolder;
import com.matt.remotenotifier.device.DeviceType;

import android.content.Context;
import android.net.ParseException;
import android.util.Log;
import android.widget.Toast;

/*
 * Singleton instance - Use getInstance
 */
public class JobCoordinator {
	private static String TAG = JobCoordinator.class.getSimpleName();
	private static JobCoordinator instance;
	private JobDao jobDao;
	private DeviceCoordinator deviceCoordinator;
	private ChannelEventCoordinator cec;
	private ArrayList<JobHolder> jobList;
	private int jobCount;
	private Context ctx;
	
	public static int UNKNOWN_JOB_STATUS = 0;
	public static int SCHEDULED_JOB_STATUS = 1;
	public static int COMPLETE_JOB_STATUS = 2;
	public static int ERROR_JOB_STATUS = 3;
	
	protected JobCoordinator(Context ctx) {
		jobCount = 0;
		jobList = new ArrayList<JobHolder>();
		jobDao = new JobDao(ctx);
		
		getChannelEventCoordinatorInstance();
		getDeviceCoodinatorInstance();
		Log.i(TAG, "Job Coodinator Started Okay");
	}

	static public JobCoordinator getInstance() throws NotActiveException{
		if(instance != null){
			Log.d(TAG, "JobCoordinator already active. Returning instace");
			return instance;
		}else{
			throw new NotActiveException("Job Coordinator not yet active");
		}
	}
	
	static public JobCoordinator getInstance(Context ctx){
		if(instance != null){
			Log.d(TAG, "JobCoordinator already active. Returning instace");
			return instance;
		}else{
			return instance = new JobCoordinator(ctx);
		}
	}
	
	public void shutdown(AppPreferences appPrefs) throws NotActiveException {
		if(instance != null){
			Log.d(TAG, "JobCoordinator shutting down. Saving Job States");
			appPrefs.setJobStore(jobList);
			
			instance = null;			
		}else{
			Log.w(TAG, "Shutdown requested but not yet active. This can only be called when not active!");
			throw new NotActiveException("Job Coordinator not yet active");
		}
	}
	
	public ArrayList<JobHolder> getJobHolderList() throws NotActiveException{
		if(instance != null){
			return jobList;
		}else{
			throw new NotActiveException("Job Coordinator not yet active");
		}
	}
	
	public void restoreJobHolderList(ArrayList<JobHolder> jobStoreList) throws NotActiveException{
		if(instance != null){
			if(jobStoreList != null){
				jobList = jobStoreList;
				jobCount = jobStoreList.size();
				Log.i(TAG, "JobList has been restored - Contains ["+jobCount+"] Jobs");
				failPastJobs();
			//	deviceCoordinator.updateControl();
			}else{
				Log.w(TAG, "JobStore list was null at time of restore. Not restoring");
			}
		}else{
			throw new NotActiveException("Job Coordinator not yet active");
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
	
	private void getChannelEventCoordinatorInstance(){
		if(cec == null){
			try {
				cec = ChannelEventCoordinator.getInstance();
			} catch (NotActiveException e) {
				Log.w(TAG, e.getMessage());
			}
		}else{
			Log.d(TAG, "ChannelEventCoordinator is already assigned");
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

	public int getJobCount() {
		return jobCount;
	}
	
	public JobHolder getLatestJobHolder(CommandHolder ch){
		ArrayList<JobHolder> sJh = new ArrayList<JobHolder>();
		for(JobHolder job : jobList){
			CommandHolder c = job.getCommandHolder();
			if(ch.equals(c)){
				
				// Previous sessions jobs...
				for(int i=0; i < c.getAssociatedJobCount(); i++){
					if ((getJobHolder(c.getAssociatedJobId(i)).isJobComplete() == false) &&  (getJobHolder(c.getAssociatedJobId(i)).getJobStatus() != 3)){
						sJh.add(getJobHolder(c.getAssociatedJobId(i)));
					}
				}
				
				// This sessions jobs...
				for(int i=0; i < ch.getAssociatedJobCount(); i++){
					if ((getJobHolder(ch.getAssociatedJobId(i)).isJobComplete() == false) &&  (getJobHolder(ch.getAssociatedJobId(i)).getJobStatus() != 3)){
						sJh.add(getJobHolder(ch.getAssociatedJobId(i)));
					}
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
			}
		}
		if(sJh.size() == 0) {
			//Log.d(TAG, "All jobs for this commandHolder are complete");
			return null;
		}else{
			return sJh.get(0);
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
				//jobDao.createJob(jobName, commandHolder, deviceId);
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
		getChannelEventCoordinatorInstance();
		final Thread executeJobThread = new Thread(new Runnable() {
			@Override
			public void run() {
				JobHolder jh = jobList.get(jobId);
				if(jh != null){
					CommandHolder ch = jh.getCommandHolder();
					DeviceHolder dh = deviceCoordinator.getDeviceHolder(jh.getDeviceId());
					try {
						Log.i(TAG, "Excuting Job with Id ["+jobId+"]");
						JSONObject jObject = new JSONObject("{deviceName: "+dh.getDeviceName()+", deviceType: "+dh.getDeviceType().toString()+", deviceTag: "+ChannelEventCoordinator.DEVICE_TAG+", command: "+ch.getCommand()+", jobId: "+jobId+"}");
						
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
						try{
							if(!jh.getRunDateTime().isEmpty()){
								jObject.put("dateTime", jh.getRunDateTime());
								cec.trigger(0, ChannelEventCoordinator.EVENT_EXECUTE_TIMED_JOB, jObject.toString());
							}else{
								cec.trigger(0, ChannelEventCoordinator.EVENT_EXECUTE_JOB, jObject.toString());
							}
						}catch(Exception e){
							Log.e(TAG, e.getMessage());
						}
						
						synchronized(this){
							wait(60000);
						}
											
						if(!jh.isJobRecieved()){
							Log.w(TAG, "Job ["+jobId+"] on device ["+dh.getDeviceName()+"] took too long to respond with Ack. Failing Job");
							jh.setJobStatus(ERROR_JOB_STATUS);
							Toast.makeText(ctx, "Error with job ["+jh.getJobName()+"]", Toast.LENGTH_SHORT).show();
						}else{
							jh.setJobStatus(SCHEDULED_JOB_STATUS);
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
			if(jh.getJobStatus() != ERROR_JOB_STATUS){
				if(!jh.isJobRecieved()){
					Log.i(TAG, "Job with jobId ["+jobId+"] has been received & and scheduled");
					jh.setJobRecieved();
					jh.setJobStatus(SCHEDULED_JOB_STATUS);
					return SCHEDULED_JOB_STATUS;
				}else{
					Log.i(TAG, "Job with jobId ["+jobId+"] is complete");
					jh.setJobComplete();
					jh.setJobStatus(COMPLETE_JOB_STATUS);
					deviceCoordinator.updateControl();
					if(jobRetval != null){
						Log.i(TAG, "Job returned data. Adding this to jobHolder");
						jh.setJobRetval(jobRetval);
					}
					return COMPLETE_JOB_STATUS;
				}			
			}else{
				Log.w(TAG, "Previously errored job ["+jobId+"] has suddenly responded. Jobs status is now unknown!");
				return UNKNOWN_JOB_STATUS;
			}
		}	
		Log.wtf(TAG, "Unknown status for job ["+jobId+"]");
		return UNKNOWN_JOB_STATUS;
	}
	
	private void failPastJobs(){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for(JobHolder job : jobList){
	        try {
				Date d = df.parse(job.getRunDateTime());
				Date now = new Date();
				if((d.before(now) && (job.isJobRecieved()) && (!job.isJobComplete()))){
					failJob(job.getJobId());
				}
			} catch (Exception e) {
				Log.i(TAG, "This is not a timed job. Skipping and not failing.");
			}	        
        }
	}

	public boolean failJob(int jobId) {
		JobHolder jh = jobList.get(jobId);
		if(jh != null){
			if((jh.getJobStatus() != ERROR_JOB_STATUS) && (!jh.isJobComplete())){
				Log.i(TAG, "Job ["+jobId+"] was reported failed. Setting failed");
				jh.setJobStatus(ERROR_JOB_STATUS);
				return true;
			}else{
				Log.w(TAG, "Job ["+ jobId +"] has already failed");
				return false;
			}
		}
		Log.w("TAG", "Failed to get jobHandler for job Id ["+jobId+"]");
		return false;
	}

	public void cancelJobs(CommandHolder ch) {
		for(JobHolder job : jobList){
			CommandHolder c = job.getCommandHolder();
			if(ch.equals(c)){
				if(c.getAssociatedJobCount() > 0){
					for (int jobId : c.getAssoicatedJobList()){
						if(failJob(jobId)){
							cancelJob(jobId);
						}
					}
					deviceCoordinator.updateControl();
				}
			}
		}
	}
	
	public void cancelJob(final int jobId){
		getDeviceCoodinatorInstance();
		Log.i(TAG, "Cancel job requested for Job ["+jobId+"]");
		Thread cancelJobThread = new Thread(new Runnable() {
			@Override
			public void run() {
				JobHolder jh = jobList.get(jobId);
				if(jh != null){
					try{
						DeviceHolder dh = deviceCoordinator.getDeviceHolder(getJobHolder(jobId).getDeviceId());
						String jobCancelEvent = "{deviceName: "+dh.getDeviceName()+", deviceType: "+dh.getDeviceType().toString()+", deviceTag: "+ChannelEventCoordinator.DEVICE_TAG+", jobId: "+jobId+"}";
						
						cec.trigger(0, ChannelEventCoordinator.EVENT_CANCEL_JOB, jobCancelEvent);
					}catch(Exception e){
						Log.w(TAG, "Error creating JSON Object for failing job");
					}
				}
			}
		});
		cancelJobThread.start();
	}
}
