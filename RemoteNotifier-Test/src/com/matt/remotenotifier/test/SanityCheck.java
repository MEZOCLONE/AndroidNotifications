package com.matt.remotenotifier.test;

import com.matt.remotenotifier.MainFragmentActivity;

import android.test.ActivityInstrumentationTestCase2;

public class SanityCheck extends ActivityInstrumentationTestCase2<MainFragmentActivity> {

	public SanityCheck(Class<MainFragmentActivity> activityClass) {
		super(activityClass);
	}
	
	public void testSanity(){
		assertEquals(2, 1 + 1);
	}

}
