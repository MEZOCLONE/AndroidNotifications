package com.matt.pusher;

import java.io.NotActiveException;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.matt.remotenotifier.DeviceType;
import com.matt.remotenotifier.IncomingFragment;
import com.matt.remotenotifier.JobCoordinator;
import com.matt.remotenotifier.R;
import com.pusher.client.channel.PrivateChannelEventListener;
import com.pusher.client.channel.SubscriptionEventListener;

public class JobEventManager implements PrivateChannelEventListener {
	
	private static final String TAG = "JobEventManager";
	private IncomingFragment incomingFragment;
	private JobCoordinator jobCoordinator;

	public JobEventManager(IncomingFragment incomingFragment) {
		this.incomingFragment = incomingFragment;
	}
	
	private void getJobCoodinatorInstance(){
		if(jobCoordinator == null){
			try {
				jobCoordinator = JobCoordinator.getInstance();
			} catch (NotActiveException e) {
				Log.w(TAG, e.getMessage());
			}
		}else{
			Log.d(TAG, "Job Coodinator is already assigned");
		}
	}

	@Override
	public void onEvent(String channelName, String eventName, String data) {
		if(eventName.equalsIgnoreCase("device_ack_execute_job")){
			handleJobExecuteEvent(data);
		}
		
		if(eventName.equalsIgnoreCase("device_fail_exceute_job")){
			handleJobFailEvent(data);
		}

	}

	private void handleJobFailEvent(String data) {
		try{
			getJobCoodinatorInstance();
			JSONObject eventData = new JSONObject(data);
			String deviceName = eventData.getString("deviceName");
			int jobId = eventData.getInt("jobId");
			if(jobCoordinator.failJob(jobId)){
				Long now = System.currentTimeMillis();
				incomingFragment.addItem(jobCoordinator.getJobName(jobId)+" caused error on "+deviceName, "", R.color.haloDarkRed, 255, now);
			}						
		}catch(Exception e){
			Log.w(TAG, "Unable to parse device_ack_execute_job event", e);
		}		
	}

	private void handleJobExecuteEvent(String data) {
		try{
			getJobCoodinatorInstance();
			JSONObject eventData = new JSONObject(data);
			String deviceName = eventData.getString("deviceName");
			DeviceType deviceType = DeviceType.valueOf(eventData.getString("deviceType"));
			int jobId = eventData.getInt("jobId");
			String retval = null;
			try{
				retval = eventData.getString("retval");
			}catch(JSONException e){
				Log.i(TAG, "Job ["+jobId+"] did not return any assoicated data");
			}
			int jobCoodinatorStatus = jobCoordinator.handleJobAck(deviceName, deviceType, jobId, retval);
			String returnedparsedData;
			if (retval != null){
				returnedparsedData = jobCoordinator.getJobRetval(jobId);
			}else{
				returnedparsedData = "";
			}
			Long now = System.currentTimeMillis();
			switch (jobCoodinatorStatus){
				case 1: incomingFragment.addItem(jobCoordinator.getJobName(jobId)+" recieved by "+deviceName, "", R.color.haloLightPurple, 255, now);
						break;
				case 2: incomingFragment.addItem(jobCoordinator.getJobName(jobId)+" completed by "+deviceName, returnedparsedData, R.color.haloDarkPurple, 255, now);
						break;
				case 0: incomingFragment.addItem(jobCoordinator.getJobName(jobId)+" caused error on "+deviceName, "", R.color.haloDarkRed, 255, now);
						break;
			}
		}catch(Exception e){
			Log.w(TAG, "Unable to parse device_ack_execute_job event");
			Log.d(TAG, e.getMessage());
		}
		
	}

	@Override
	public void onSubscriptionSucceeded(String channelName) {
		Log.d(TAG, "Bound to Job Events");		
	}

	@Override
	public void onAuthenticationFailure(String arg0, Exception arg1) {
		Log.w(TAG, "Failed to bind to Job Events");	
	}

}
