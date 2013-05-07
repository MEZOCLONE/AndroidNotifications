package com.matt.remotenotifier;

import java.io.NotActiveException;
import java.util.ArrayList;
import java.util.List;

import com.matt.remotenotifier.device.DeviceCoordinator;
import com.matt.remotenotifier.device.DeviceHolder;
import com.matt.remotenotifier.job.JobCoordinator;
import com.matt.remotenotifier.job.TimePickerFragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;


// Look, I'm all grown up and have extended my own class! :)
public class CommandFragment extends ExpandableListFragment {	
	private static String TAG = CommandFragment.class.getName();
	public ExpandListAdapter mAdaptor;
	private DeviceCoordinator deviceCoordinator;
	private JobCoordinator jobCoordinator;
	
	@Override
	public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstance) {
		Log.d(TAG, "Created Outgoing Fragment");
		View view = inflator.inflate(R.layout.main_outgoing_fragment,container, false);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	
		mAdaptor = new ExpandListAdapter(getActivity().getApplicationContext());
		setListAdapter(mAdaptor);
		setListShown(true);
		
		//Get ALL the instances!
		getDeviceCoodinatorInstance();
		getJobCoodinatorInstance();
		registerForContextMenu(getExpandableListView());
		
		getExpandableListView().setOnChildClickListener(new OnChildClickListener() {
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, final int childPosition, long id) {
				final DeviceHolder dh = deviceCoordinator.getDeviceHolder(groupPosition);
				Log.d(TAG, "User clicked Command ["+dh.getCommndName(childPosition)+"]");
				
				// this all needs to be placed in a method or class that can be implemented using a listener for the result. 
				// Currently this code is replicated in the TimePickerFragment
				if(dh.getCommandHolder(childPosition).getArgsCount() > 0){
					AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
					Log.d(TAG, "Getting users input as required by command");
					alert.setMessage(dh.getCommndName(childPosition)+ " needs your input");
					
					// Currently I am only supporting Strings. The 'type' on the argument array will in future be used to 
					// select the correct Android Widget
					EditText et;
					TextView tv;
					LinearLayout layout = new LinearLayout(getActivity());
					layout.setOrientation(LinearLayout.VERTICAL);
					final List<EditText> allEt = new ArrayList<EditText>();
					
					for(int i = 0; i < dh.getCommandHolder(childPosition).getArgsCount(); i++){
						et = new EditText(getActivity());
						tv = new TextView(getActivity());
						char[] c = dh.getCommandHolder(childPosition).getArgumentName(i).toCharArray();
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
						for(int i = 0; i < dh.getCommandHolder(childPosition).getArgsCount(); i++){
							String argValue = allEt.get(i).getText().toString();
							dh.getCommandHolder(childPosition).setArgumentValue(i, argValue);
						}
						int jobId = jobCoordinator.createJob(dh.getCommndName(childPosition), dh.getCommandHolder(childPosition), deviceCoordinator.getDeviceIndex(dh));
						excuteJobFromFragment(jobId);
					}
					});

					alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					  public void onClick(DialogInterface dialog, int whichButton) {
					    Log.i(TAG, "User cancelled the job creation");
					  }
					});
					
					try{
						alert.show();
					}catch(Exception e){
						Log.e(TAG, "Error creating alert", e);
					}
				}else{
					int jobId = jobCoordinator.createJob(dh.getCommndName(childPosition), dh.getCommandHolder(childPosition), deviceCoordinator.getDeviceIndex(dh));
					excuteJobFromFragment(jobId);
				}
				return false;
			}
		});
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
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo){
		super.onCreateContextMenu(menu, v, menuInfo);
		
		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		final int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		final int child = ExpandableListView.getPackedPositionChild(info.packedPosition);
		
		if(type == ExpandableListView.PACKED_POSITION_TYPE_GROUP){
			menu.setHeaderTitle("Device Options");
			android.view.MenuItem timeSinceHeartbeat = menu.add(getTimeSinceLastHeatbeat(group));
			
			timeSinceHeartbeat.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					requestHeartbeatClick(group, child, item);
					return true;
				}
			});
		}
		
		if(type == ExpandableListView.PACKED_POSITION_TYPE_CHILD){
			menu.setHeaderTitle("Command Options");
			android.view.MenuItem executeAtTime = menu.add("Run this command at a certain time");
			android.view.MenuItem cancelAllAssoicatedJobs = menu.add("Cancel all jobs for this command");
			
			executeAtTime.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					executeAtTimeClick(group, child, item);
					return true;
				}
			});
			
			cancelAllAssoicatedJobs.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					cancelAllAssoicatedJobs(group, child, item);
					return true;
				}
			});
		}
	}
	
	private void cancelAllAssoicatedJobs(int group, int child, MenuItem item) {
		Log.i(TAG, "User requested all jobs cancel for CommandHolder ["+deviceCoordinator.getDeviceHolder(group).getCommandHolder(child).getName()+"] on " +
				"device ["+deviceCoordinator.getDeviceHolder(group).getDeviceName()+"]");
		jobCoordinator.cancelJobs(deviceCoordinator.getDeviceHolder(group).getCommandHolder(child));	
	}

	private void executeAtTimeClick(int group, int child, MenuItem item){
		DialogFragment dialogTimePickerFragment = new TimePickerFragment();
		// Create a bundle for the fragment to hold the right stuff. The fragment can use an instance of the jobCoordinator
		// to create and execute the job.
		Bundle timePickerFragmentBundle = new Bundle(2);
	    timePickerFragmentBundle.putInt("group", group);
	    timePickerFragmentBundle.putInt("child", child);
	    dialogTimePickerFragment.setArguments(timePickerFragmentBundle);
		dialogTimePickerFragment.show(getFragmentManager(), "timePickerFragment");
	}
	
	private void requestHeartbeatClick(int group, int child, MenuItem item){
		deviceCoordinator.requestHeartbeatFromDevice(deviceCoordinator.getDeviceHolder(group).getDeviceName());
	}
	
	private void excuteJobFromFragment(int jobId){
		jobCoordinator.executeJob(jobId);
	}
	
	public String getTimeSinceLastHeatbeat(int index){
		String TIME_PREFIX = "Heartbeat was ";
		getDeviceCoodinatorInstance(); //Just a check - At this point there shouldn't be an exception thrown, so no need to handle it I think
		DeviceHolder dh = deviceCoordinator.getDeviceHolder(index);
		Long currentTime = System.currentTimeMillis();
		Long dhLast = dh.getLastHeatbeatTime();
		
		if(!dh.hasHeartbeat()){
			return "Heatbeat not recieved";
		}else{
			CharSequence timeSinceStr = DateUtils.getRelativeTimeSpanString(dhLast, currentTime, 0, DateUtils.FORMAT_ABBREV_ALL);
			if(timeSinceStr.toString().contains("sec")){
				return TIME_PREFIX + "a moment ago";
			}else{
				return TIME_PREFIX + timeSinceStr.toString();
			}
		}
	}
}
