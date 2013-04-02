package com.matt.remotenotifier;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingsFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflator, ViewGroup container,
			Bundle savedInstance) {
		View view = inflator.inflate(R.layout.activity_set_prefs, container,
				false);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

}
