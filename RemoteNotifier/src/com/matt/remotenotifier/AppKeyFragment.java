package com.matt.remotenotifier;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

public class AppKeyFragment extends DialogFragment {

	public AppKeyFragment(){
		// That no-args constructor everyone keeps talking about
	}
	
	public interface AppKeyDialogListener {
        void onFinishAppKeyDialog(String inputText);
    }
		
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		LinearLayout layout = new LinearLayout(getActivity());
		layout.setOrientation(LinearLayout.VERTICAL);
		final EditText et = new EditText(getActivity());
		layout.addView(et);
		
		AlertDialog alert = new AlertDialog.Builder(getActivity())
        .setTitle("Set Application Key")
        .setPositiveButton("Okay",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	if(et.getText().toString() != ""){
	                    AppKeyDialogListener activity = (AppKeyDialogListener) getActivity();
	                    activity.onFinishAppKeyDialog(et.getText().toString());
                	}
                }
            }
        )
        .setNegativeButton("Cancel",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    et.setText("");
                }
            }
        )
        .create();
		
		alert.setView(layout);
		//getDialog().getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		return alert;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){		
		return container;
		
	}
}
