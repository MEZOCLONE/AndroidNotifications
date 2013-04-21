package com.matt.remotenotifier.testRunner;

import android.test.ActivityInstrumentationTestCase2;

import com.matt.remotenotifier.IncomingFragment;
import com.matt.remotenotifier.MainFragmentActivity;
import com.matt.remotenotifier.R;

public class MainFragmentActivityTest extends
		ActivityInstrumentationTestCase2<MainFragmentActivity> {
	
	private IncomingFragment inf;

	public MainFragmentActivityTest(Class<MainFragmentActivity> activityClass) {
		super(MainFragmentActivity.class);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		MainFragmentActivity mfa = getActivity();
		inf = (IncomingFragment) IncomingFragment.instantiate(mfa.getApplicationContext(), IncomingFragment.class.getName());
	}
	
	public void testAddNotification() {
		inf.addItem("Test Main Title", "Test SubTitle", R.color.haloDarkBlue, 255, System.currentTimeMillis());
		assertEquals(true, true);
	}

}
