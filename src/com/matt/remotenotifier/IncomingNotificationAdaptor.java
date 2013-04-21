package com.matt.remotenotifier;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

class IncomingNotificationAdaptor extends BaseAdapter {

	//private static String TAG = "IncomingNotifAdaptor";
	private ArrayList<EventHolder> eventList = new ArrayList<EventHolder>();
	private LayoutInflater mInflater;
	private Context ctx;

	public IncomingNotificationAdaptor(Context context) {
		mInflater = LayoutInflater.from(context);
		ctx = context;
		mInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	protected void addItem(String main, String sub, int colourResourseId, int alpha, Long time){
		EventHolder eh = new EventHolder(main, sub, colourResourseId, alpha, time);
		eventList.add(0, eh);
		notifyDataSetChanged();
	}
	
	public void editColour(int newColourResourseId, int notifId){
		eventList.get(notifId).setIconResource(newColourResourseId);
		notifyDataSetChanged();
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
		eventList.remove(i);
		notifyDataSetChanged();
	}
	
	public void clearAll(){
		eventList.clear();
		notifyDataSetChanged();
	}

	public int getCount() {
		return eventList.size();
	}

	public EventHolder getItem(int position) {
		return eventList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}
	
	protected ArrayList<EventHolder> getEventList(){
		return eventList;
	}
	
	protected void restoreEventList(ArrayList<EventHolder> eventList){
		this.eventList = eventList;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder view = null;
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
		view.textMain.setText(eventList.get(position).getTextMain());
		view.textMain.setTextColor(Color.argb(eventList.get(position).getAlphaData(), 255, 255, 255));
		view.textSub.setText(eventList.get(position).getTextSub());
		view.textSub.setTextColor(Color.argb(eventList.get(position).getAlphaData(), 255, 255, 255));
		view.timeSince.setText(getTimeSince(eventList.get(position).getTimeData()));
		view.timeSince.setTextColor(Color.argb(eventList.get(position).getAlphaData(), 255, 255, 255));
		view.rowIcon.setImageResource(eventList.get(position).getIconResource());
		view.rowIcon.setAlpha(eventList.get(position).getAlphaData());
		
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