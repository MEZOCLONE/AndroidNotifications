package com.matt.remotenotifier.test;

import com.matt.remotenotifier.MainFragmentActivity;

import android.test.ActivityInstrumentationTestCase2;

public class MainFragmentActivityTest extends ActivityInstrumentationTestCase2<MainFragmentActivity> {

	public MainFragmentActivityTest(Class<MainFragmentActivity> activityClass) {
		super(activityClass);
	}
	
	public void testSanity(){
		assertEquals(2, 1 + 1);
	}
	
	public void testSanity2(){
		assertEquals(4, 2 + 2);
	}

}
