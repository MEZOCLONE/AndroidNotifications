package com.matt.remotenotifier.test;

import com.matt.remotenotifier.MainFragmentActivity;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

public class TestMainFragmentActivity extends ActivityInstrumentationTestCase2<MainFragmentActivity> {
	private Activity mActivity;
	
	public TestMainFragmentActivity() {
		super(MainFragmentActivity.class);
	}

	@Override
	protected void setUp() throws Exception{
		super.setUp();
		
		mActivity = getActivity();
	}
	
	@Override
	protected void tearDown() throws Exception{
		super.tearDown();
	}
	
	public void testSanity(){
		assertEquals(2, 1 + 1);
	}
	
	public void testSanity2(){
		assertEquals(4, 2 + 2);
	}

}
