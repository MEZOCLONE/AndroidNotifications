package com.matt.remotenotifier.event;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.matt.remotenotifier.R;

public class EventFragment extends ListFragment {
	
	private static String TAG = EventFragment.class.getName();
	private EventAdaptor mAdaptor;
	public TextView connectionMessage;

	@Override
	public View onCreateView(LayoutInflater inflator, ViewGroup container,
			Bundle savedInstance) {
		View view = inflator.inflate(R.layout.main_incoming_fragment, container, false);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		final Vibrator vibe = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
			
		mAdaptor = new EventAdaptor(getActivity().getApplicationContext());
		setListAdapter(mAdaptor);

		ListView listView = getListView();
		listView.setTextFilterEnabled(true);
		// Restore the event list from last time.
		restoreEventList();
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// When clicked, show a toast with the TextView text
				Toast.makeText(getActivity().getApplicationContext(), "Clicked on item: " + position, Toast.LENGTH_SHORT).show();
				mAdaptor.notifyDataSetChanged();
			}
		});

		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				vibe.vibrate(30);
				AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
				adb.setTitle("Remove Notification");
				adb.setMessage("Remove this notification?");
				final int positionToRemove = arg2;
				adb.setNegativeButton("Nah, leave it", null);
				adb.setPositiveButton("Sure",
						new AlertDialog.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								mAdaptor.removeEvent(positionToRemove, true);
							}
						});
				adb.show();
				return false;
			}
		});
		
		Log.d(TAG, "Created Incoming Fragment");
	}
	
	public void showConnectionMessages(){
		try{
			ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.progressBar);
			progressBar.setVisibility(View.VISIBLE);
		}catch(Exception e){
			Log.e(TAG, "Error showing progress bar on connect", e);
		}
	}
	
	public void hideConnectionMessages(){
		try{
			ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.progressBar);
			progressBar.setVisibility(View.GONE);
		}catch(Exception e){
			Log.e(TAG, "Error hiding progress bar on connect");
		}
	}
	
	public void addItem(String main, String sub, int colourResourseId, int alpha, Long time){
		mAdaptor.addEvent(main, sub, colourResourseId, alpha, time);
	}
	
	public void removeAll(){
		mAdaptor.removeAll(true);
	}
	
	public void notifyDataSetChanged(){
		mAdaptor.notifyDataSetChanged();
	}
	
	public void restoreEventList(){
		Log.d(TAG, "Attempting to restore EventList from db");
		mAdaptor.restoreEventList();
	}
	
	public FragmentActivity getFragmentActivity(){
		return getActivity();
	}
}
