package com.matt.remotenotifier;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;

public class PagerAdapterManager extends FragmentPagerAdapter {

	private List<Fragment> fragments;
	
	public PagerAdapterManager(FragmentManager fm, List<Fragment> fragments) {
		super(fm);
		this.fragments = fragments;
	}
	
	@Override
	public Fragment getItem(int position) {
		return this.fragments.get(position);
	}

	@Override
	public int getCount() {
		return this.fragments.size();
	}
	
	@SuppressWarnings("deprecation")
	public void onDestroyItem(View container, int position, Object object){
		super.destroyItem(container, position, object);
		fragments.remove(position);
		
	}
}
