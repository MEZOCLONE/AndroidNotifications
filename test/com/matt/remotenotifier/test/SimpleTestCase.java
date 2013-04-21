package com.matt.remotenotifier.test;

import com.matt.remotenotifier.IncomingFragment;
import com.matt.remotenotifier.MainFragmentActivity;
import com.matt.remotenotifier.R;

import android.test.ActivityInstrumentationTestCase2;

public class SimpleTestCase extends ActivityInstrumentationTestCase2<MainFragmentActivity> {
	
	private IncomingFragment inf;

	public SimpleTestCase() {
		super("com.matt.remotenotifier", MainFragmentActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		MainFragmentActivity mfa = getActivity();
		inf = (IncomingFragment) IncomingFragment.instantiate(mfa.getApplicationContext(), IncomingFragment.class.getName());
	}
	
	public void testAddNotification(){
		inf.addItem("Test Main Title", "Test SubTitle", R.color.haloDarkBlue, 255, System.currentTimeMillis());
		assertTrue(true);
	}

}
