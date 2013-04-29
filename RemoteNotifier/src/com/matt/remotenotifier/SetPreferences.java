package com.matt.remotenotifier;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.matt.remotenotifier.R.id;

public class SetPreferences extends Activity {
	private static String TAG = "SetPreferences";

	// TODO Fix layout issues now basic functions are there (make it look
	// pretty?)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final AppPreferences appPrefs = new AppPreferences(getApplicationContext());

		setContentView(R.layout.activity_set_prefs);

		final EditText edtKey = (EditText) findViewById(id.edtKey);
		final EditText edtSecret = (EditText) findViewById(id.edtSecret);
		final CheckBox cbShowHeartbeats = (CheckBox) findViewById(id.cbShowHeartbeats);
		final Button btnSave = (Button) findViewById(id.btnSave);

		edtKey.setText(appPrefs.getKey());
		edtSecret.setText(appPrefs.getSecret());
		cbShowHeartbeats.setChecked(appPrefs.getHeartbeatShow()); 

		btnSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Currently, there is no input validation. This could go
				// wrong...
				appPrefs.setKey(edtKey.getText().toString());
				appPrefs.setSecret(edtSecret.getText().toString());
				appPrefs.setHeartbeatShow(cbShowHeartbeats.isChecked());
				setResult(RESULT_OK);
				finish();
			}
		});
	}
}
