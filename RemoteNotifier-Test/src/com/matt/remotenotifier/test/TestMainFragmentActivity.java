package com.matt.remotenotifier.test;

import java.io.NotActiveException;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

import com.matt.remotenotifier.MainFragmentActivity;
import com.matt.remotenotifier.device.DeviceCoordinator;

/**
 * Test case that tests the main startup and runtimes of RemoteNotifier
 * @author mattm
 *
 */
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
	
	/**
	 * Test that the {@link DeviceCoordinator} has started correctly with the application. (started with <i>getActivity()</i>)
	 * @throws NotActiveException
	 */
	public void testDeviceCoordinatorStartup() throws NotActiveException{
		DeviceCoordinator deviceCoordinator = DeviceCoordinator.getInstance();
		assertNotNull(deviceCoordinator);
	}
	
//	/**
//	 * Test that the {@link JobCoordinator} has started correctly with the application. (started with <i>getActivity()</i>)
//	 * @throws NotActiveException
//	 */
//	public void testJobCoordinatorStartup() throws NotActiveException{
//		JobCoordinator jobCoordinator = JobCoordinator.getInstance();
//		assertNotNull(jobCoordinator);
//	}
	
	
	// Quickly check the laws of physics before we pass...
	public void testSanity(){
		assertEquals(2, 1 + 1);
	}
	
}
