package com.matt.remotenotifier;

import java.io.NotActiveException;

import com.matt.remotenotifier.device.DeviceCoordinator;
import com.matt.remotenotifier.device.DeviceHolder;
import com.matt.remotenotifier.job.JobCoordinator;
import com.matt.remotenotifier.job.JobHolder;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * This is used as abstraction layer between the Coordinators and the list of devices. 
 * It will handle showing what job is up next, refreshing the list and those sorts of things. 
 */
public class ExpandListAdapter extends BaseExpandableListAdapter  {
	private static final String TAG = "ExpandListAdapter";
	private LayoutInflater mInflater;
	private Context ctx;
	private DeviceCoordinator deviceCoordinator;
	private JobCoordinator jobCoordinator;
	
	public ExpandListAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
		ctx = context;
		mInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);		
		getDeviceCoodinatorInstance();
		//JC currently not used in this class. Will need it soon when scheduled jobs become more flexible.
		getJobCoodinatorInstance();
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

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		DeviceHolder dh = deviceCoordinator.getDeviceHolder(groupPosition);
		return dh.getCommndName(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		viewHolderControl view = null;
		//Log.d(TAG, "getView " + childPosition + " " + convertView);
		if (convertView == null) {
			view = new viewHolderControl();
			convertView = mInflater.inflate(R.layout.list_layout_outgoing_child, parent, false);
			convertView.setMinimumHeight(50);
			view.textMain = (TextView) convertView.findViewById(R.id.textChildControl);
			view.textMain.setTextColor(Color.argb(255, 255, 255, 255));
			view.textScheduledTime = (TextView) convertView.findViewById(R.id.textSceduledTime);
			view.textScheduledTime.setTextColor(Color.argb(200, 255, 255, 255));
			view.jobProgressBar = (ProgressBar) convertView.findViewById(R.id.pbJobProgress);
			view.rLayout = (RelativeLayout) convertView.findViewById(R.id.relExpListItem);
			
			convertView.setTag(view);
		} else {
			view = (viewHolderControl) convertView.getTag();
		}
		view.textMain.setText(deviceCoordinator.getDeviceHolder(groupPosition).getCommndName(childPosition));
		view.jobProgressBar.setVisibility(View.GONE);
		JobHolder jh = jobCoordinator.getLatestJobHolder(deviceCoordinator.getDeviceHolder(groupPosition).getCommandHolder(childPosition));
		if(jh != null){
			view.textScheduledTime.setText("("+jh.getRunDateTime()+")");
		}else{
			
			view.textScheduledTime.setText("");
		}

		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return deviceCoordinator.getDeviceHolder(groupPosition).getCommandCount();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return deviceCoordinator.getDeviceHolder(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return deviceCoordinator.getDeviceCount();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		viewHolderControl view = null;
		//Log.d(TAG, "getView " + groupPosition + " " + convertView);
		if (convertView == null) {
			view = new viewHolderControl();
			convertView = mInflater.inflate(R.layout.list_layout_outgoing_group, parent, false);
			convertView.setMinimumHeight(50);
			view.textMain = (TextView) convertView.findViewById(R.id.textMainControl);
			view.textMain.setTextColor(Color.argb(255, 255, 255, 255));
			view.rLayout = (RelativeLayout) convertView.findViewById(R.id.relExpListItem);
			
			convertView.setTag(view);
		} else {
			view = (viewHolderControl) convertView.getTag();
		}
		//ListElement le = (ListElement) getGroup(groupPosition);
		view.textMain.setText(deviceCoordinator.getDeviceHolder(groupPosition).getDeviceName());

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
	   /* used to make the notifyDataSetChanged() method work */
	   super.registerDataSetObserver(observer);
	}
	
	class viewHolderControl {
		public TextView textMain;
		public TextView textScheduledTime;
		public ProgressBar jobProgressBar;
		public RelativeLayout rLayout;

		public boolean onTouch(View v, MotionEvent event) {
			return false;
		}
	}

	

}
