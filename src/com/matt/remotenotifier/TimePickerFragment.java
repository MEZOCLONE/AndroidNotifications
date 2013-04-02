package com.matt.remotenotifier;

import java.io.NotActiveException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.matt.remotenotifier.DeviceCoordinator.DeviceHolder;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
	private static String TAG = "TimePickerFragment";
	private int group, child;
	private DeviceCoordinator deviceCoordinator;
	private JobCoordinator jobCoordinator;
	private boolean firstTime;
	private Calendar c;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		
		this.group = getArguments().getInt("group");
		this.child = getArguments().getInt("child");
		
		// Due to a bug an Android onTimeSet is fired twice. Use this boolean to put a stop to that! 
		// When the bug is fixed, remove this.
		// https://code.google.com/p/android/issues/detail?id=34860
		firstTime = true;
		
		getDeviceCoodinatorInstance();
		getJobCoodinatorInstance();
		
		final TimePickerDialog tpd =  new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
		tpd.setCancelable(true);
		tpd.setCanceledOnTouchOutside(false);
		
		tpd.setButton(DialogInterface.BUTTON_NEGATIVE, getActivity().getString(android.R.string.cancel), 
				new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which){
					
				}
		});
		
		tpd.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				Log.d(TAG, "Dialog dismissed... Probably");				
			}
		});
		
		tpd.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				Log.d(TAG, "Dialog cancelled... Probably");					
			}
		});
		return tpd;
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
	
	/**
	 * Returns a string with the current date in ready to add to a JobHolder format!
	 * @param hour
	 * @param min
	 * @param sec
	 * 
	 * @return yyyy-MM-dd HH:mm:ss formatted string
	 */
	@SuppressLint("SimpleDateFormat")
	private String createDateTimeString(int hour, int min){
		SimpleDateFormat sfd = 	new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), hour, min, 0);
		Date dateTimeStr = c.getTime();

		return sfd.format(dateTimeStr);
	}

	public void onTimeSet(TimePicker view, final int hourOfDay, final int minute) {
		if(firstTime){
			firstTime = false;
			Log.i(TAG, "User invoked timed job creation");
			if (!(hourOfDay < c.get(Calendar.HOUR_OF_DAY)) && !(minute <= c.get(Calendar.MINUTE))) { 
				getDeviceCoodinatorInstance();
				getJobCoodinatorInstance();
				
				final DeviceHolder dh = deviceCoordinator.getDeviceHolder(group);
				Log.d(TAG, "User clicked Command ["+dh.getCommndName(child)+"]");
				
				if(dh.getCommandHolder(child).getArgsCount() > 0){
					AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
					Log.d(TAG, "Getting users input as required by command");
					alert.setMessage(dh.getCommndName(child)+ " needs your input");
					
					// Currently I am only supporting Strings. The 'type' on the argument array will in future be used to 
					// select the correct Android Widget
					EditText et;
					TextView tv;
					LinearLayout layout = new LinearLayout(getActivity());
					layout.setOrientation(LinearLayout.VERTICAL);
					final List<EditText> allEt = new ArrayList<EditText>();
					
					for(int i = 0; i < dh.getCommandHolder(child).getArgsCount(); i++){
						et = new EditText(getActivity());
						tv = new TextView(getActivity());
						char[] c = dh.getCommandHolder(child).getArgumentName(i).toCharArray();
						c[0] = Character.toUpperCase(c[0]);
						String tl = new String(c);
						tv.setText(tl.replaceAll("_", " "));
						allEt.add(et);
						layout.addView(tv);	
						layout.addView(et);
					}
					alert.setView(layout);
					
					alert.setPositiveButton("Send", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						for(int i = 0; i < dh.getCommandHolder(child).getArgsCount(); i++){
							String argValue = allEt.get(i).getText().toString();
							dh.getCommandHolder(child).setArgumentValue(i, argValue);
						}
						Log.i(TAG, "Creating timed job at ["+createDateTimeString(hourOfDay, minute)+"]");
						int jobId = jobCoordinator.createTimedJob(dh.getCommndName(child), dh.getCommandHolder(child), deviceCoordinator.getDeviceIndex(dh), createDateTimeString(hourOfDay, minute));
						jobCoordinator.executeJob(jobId);
						dh.getCommandHolder(child).addJobId(jobId);
						//Now tell the deviceCoordinator that it should tell the list to refresh
						deviceCoordinator.updateControl();
					}
					});
	
					alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					  public void onClick(DialogInterface dialog, int whichButton) {
					    Log.i(TAG, "User cancelled the job timed creation");
					  }
					});
					
					try{
						alert.show();
					}catch(Exception e){
						Log.e(TAG, "Error creating alert", e);
					}
				}else{
					Log.i(TAG, "Creating timed job at ["+createDateTimeString(hourOfDay, minute)+"]");
					int jobId = jobCoordinator.createTimedJob(dh.getCommndName(child), dh.getCommandHolder(child), deviceCoordinator.getDeviceIndex(dh), createDateTimeString(hourOfDay, minute));
					jobCoordinator.executeJob(jobId);
					Log.d(TAG, "Adding jobId to commandHolder and notifiying deviceCoordinator to refresh list");
					dh.getCommandHolder(child).addJobId(jobId);
					//Now tell the deviceCoordinator that it should tell the list to refresh
					deviceCoordinator.updateControl();
				}	
			}else{
				Log.i(TAG, "User is trying to schedule a job in the past. Numpty.");
				Toast.makeText(getActivity(), "You can't change the past...", Toast.LENGTH_SHORT).show();
			}
		}
	}
}