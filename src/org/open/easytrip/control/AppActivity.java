package org.open.easytrip.control;

import org.open.easytrip.R;
import org.open.easytrip.bo.BOFactory;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

public class AppActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
	    	setTheme(android.R.style.Theme_Holo);
	    else
	    	setTheme(android.R.style.Theme);
		
	}
	
	protected BOFactory bos = BOFactory.getInstance();
	
	/**
	 * shortcut to TextView components within this activity
	 */
	protected TextView textView(int id) {
		return (TextView)findViewById(id);
	}
	
	/**
	 * shortcut to quantity string resources
	 */
	protected String getQuantityString(int id, int quantity) {
		return getResources().getQuantityString(id, quantity);		
	}
	
	/**
	 * shortcut to test check attribute of CompoundButton (radios, checkboxes, etc.) components
	 * within this activity
	 */
	protected boolean isChecked(int viewId) {
		return ((CompoundButton)findViewById(viewId)).isChecked();
	}

	/**
	 * shortcut to Toast messages 
	 */
	protected void toast(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
	}
	/**
	 * shortcut to Toast messages 
	 */
	protected void toast(int resourceId) {
		Toast.makeText(getApplicationContext(), resourceId, Toast.LENGTH_LONG).show();
	}
	/**
	 * shortcut to Toast messages 
	 */
	protected void toast(CharSequence message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
	}

	/**
	 * Shows a confirmation message with ok and cancel buttons. The dialog is open asynchronous, as per android philosophy.
	 * @param title An optional title bar.
	 * @param message An optional, longer message to be presented to the user. 
	 * @param onOK An implementation of android.content.DialogInterface.OnClickListener with the code 
	 * to be executed when OK is clicked. Nothing will be executed if cancel is clicked.
	 */
	protected void confirmationDialog(String title, String message, android.content.DialogInterface.OnClickListener onOK) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setPositiveButton(R.string.ok, onOK);
				builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
				if (title != null)
					builder.setTitle(title);
				if (message != null)
					builder.setMessage(message);
			
				// Create the AlertDialog
				builder.create().show();
			}
	
}
