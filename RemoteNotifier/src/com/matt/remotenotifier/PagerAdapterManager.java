package com.matt.remotenotifier;

import java.io.NotActiveException;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;

public class PagerAdapterManager extends FragmentPagerAdapter {

	private List<Fragment> fragments;
	private static PagerAdapterManager instance;
	
	protected PagerAdapterManager(FragmentManager fm, List<Fragment> fragments) {
		super(fm);
		this.fragments = fragments;
	}
	
	public static PagerAdapterManager getInstance(FragmentManager fm, List<Fragment> fragments){
		if(instance != null){
			return instance;
		}else{
			return instance = new PagerAdapterManager(fm, fragments);
		}
	}
	
	public static PagerAdapterManager getInstance() throws NotActiveException{
		if(instance != null){
			return instance;
		}else{
			throw new NotActiveException("PageAdapterManager is not active");
		}
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
