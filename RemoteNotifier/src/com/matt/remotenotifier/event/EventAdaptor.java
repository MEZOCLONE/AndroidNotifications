package com.matt.remotenotifier.event;

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

import com.matt.remotenotifier.R;

/**
 * Adaptor class for the list {@link EventFragment}.
 * This manages a list of {@link EventHolder} objects and can accept new objects and events though the fragment.
 * @author mattm
 *
 */
class EventAdaptor extends BaseAdapter {

	private ArrayList<EventHolder> eventList = new ArrayList<EventHolder>();
	private LayoutInflater mInflater;
	private Context ctx;
	private EventDao eventDao;

	public EventAdaptor(Context context) {
		ctx = context;
		mInflater = LayoutInflater.from(ctx);
		mInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		eventDao = new EventDao(ctx);
		eventDao.open();
		
		eventList = eventDao.getAllEvents();
	}
	
	protected void addEvent(String mainText, String subText, int iconId, int rowAlpha, Long time){
		EventHolder eh = eventDao.createEvent(mainText, subText, iconId, rowAlpha, time);
		eventList.add(0, eh);
		notifyDataSetChanged();
	}	

	/**
	 * Remove the event from the list in the fragment
	 * @param eventPosition
	 */
	public void removeEvent(int eventPosition, boolean deleteEvent) {
		if(deleteEvent){
			int eventId = eventList.get(eventPosition).getId();
			deleteEvent(eventId);
		}
		
		eventList.remove(eventPosition);
		notifyDataSetChanged();
	}
	
	/**
	 * Provides a way of deleting the event data
	 * @param eventId
	 */
	private void deleteEvent(int eventId){
		eventDao.deleteEvent(eventId);
	}
	
	public void removeAll(boolean deleteEvent){
		if(deleteEvent){
			deleteAll();
		}
		eventList.clear();
		notifyDataSetChanged();
	}
	
	private void deleteAll(){
		eventDao.deleteAllEvents();
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
	
	public CharSequence getTimeSince(Long timeData){
		Long currentTime = System.currentTimeMillis();
		CharSequence timeSinceStr = DateUtils.getRelativeTimeSpanString(timeData, currentTime, 0, DateUtils.FORMAT_ABBREV_ALL);
		if(timeSinceStr.toString().contains("sec")){
			return "just a moment ago";
		}else{
			return timeSinceStr;
		}
	}
	
	protected ArrayList<EventHolder> getEventList(){
		return eventList;
	}
	
	protected void restoreEventList(){
		removeAll(false);
		this.eventList = eventDao.getAllEvents();
		notifyDataSetChanged();
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