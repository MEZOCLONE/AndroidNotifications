package com.matt.remotenotifier;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

class IncomingNotificationAdaptor extends BaseAdapter {
	private static String TAG = "IncomingNotifAdaptor";

	private ArrayList<String> mData = new ArrayList<String>();
	private ArrayList<String> sData = new ArrayList<String>();
	private ArrayList<Integer> iconData = new ArrayList<Integer>();
	private ArrayList<Integer> alphaData = new ArrayList<Integer>();
	private ArrayList<Long> timeData = new ArrayList<Long>();
	private LayoutInflater mInflater;
	private Context ctx;

	public IncomingNotificationAdaptor(Context context) {
		mInflater = LayoutInflater.from(context);
		ctx = context;
		mInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public void addItem(String main, String sub, int colourResourseId, int alpha, Long time){
		addMain(main);
		addSub(sub);
		addColour(colourResourseId);
		addAlpha(alpha);
		addTimeSince(time);
		notifyDataSetChanged();
	}
	
	/*
	 * Note: This will update the UI directly after the colour change
	 */
	public void editColour(int newColourResourseId, int notifId){
		iconData.set(notifId, newColourResourseId);
		notifyDataSetChanged();
	}

	private void addMain(String item) {
		mData.add(0, item);
	}
	
	private void addSub(String item) {
		sData.add(0, item);
	}
	
	private void addColour(int colour){
		iconData.add(0, colour);
	}
	
	private void addAlpha(int alpha){
		alphaData.add(0, alpha);
	}
	
	private void addTimeSince(Long timeAdded){
		timeData.add(0, timeAdded);
	}
	
	public CharSequence getTimeSince(Long timeData){
		Long currentTime = System.currentTimeMillis();
		CharSequence timeSinceStr = DateUtils.getRelativeTimeSpanString(timeData, currentTime, 0, DateUtils.FORMAT_ABBREV_ALL);
		if(timeSinceStr.toString().contains("sec")){
			return "just a moment ago";
		}else{
			return timeSinceStr;
		}
	}

	public void remove(int i) {
		mData.remove(i);
		sData.remove(i);
		iconData.remove(i);
		alphaData.remove(i);
		timeData.remove(i);
		notifyDataSetChanged();
	}
	
	public void clearAll(){
		mData.clear();
		sData.clear();
		iconData.clear();
		alphaData.clear();
		timeData.clear();
		notifyDataSetChanged();
	}

	public int getCount() {
		return mData.size();
	}

	public String getItem(int position) {
		return mData.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder view = null;
		int type = getItemViewType(position);
		//Log.d(TAG, "getView " + position + " " + convertView + " type = "
		//		+ type);
		if (convertView == null) {
			view = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_layout_incoming, parent, false);
			convertView.setMinimumHeight(50);
			view.textMain = (TextView) convertView.findViewById(R.id.mainLine);
			view.textSub = (TextView) convertView.findViewById(R.id.secondLine);
			view.timeSince = (TextView) convertView.findViewById(R.id.incomingNotifTitle);
			view.rowIcon = (ImageView) convertView.findViewById(R.id.icon);
			view.rLayout = (RelativeLayout) convertView.findViewById(R.id.relListItem);
			
			convertView.setTag(view);
		} else {
			view = (ViewHolder) convertView.getTag();
		}
		view.textMain.setText(mData.get(position));
		view.textMain.setTextColor(Color.argb(alphaData.get(position), 255, 255, 255));
		view.textSub.setText(sData.get(position));
		view.textSub.setTextColor(Color.argb(alphaData.get(position), 255, 255, 255));
		view.timeSince.setText(getTimeSince(timeData.get(position)));
		view.timeSince.setTextColor(Color.argb(alphaData.get(position), 255, 255, 255));
		view.rowIcon.setImageResource(iconData.get(position));
		view.rowIcon.setAlpha(alphaData.get(position));
		
		return convertView;
	}
}

class ViewHolder {
	public TextView textMain;
	public TextView textSub;
	public TextView timeSince;
	public ImageView rowIcon;
	public RelativeLayout rLayout;

	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
}