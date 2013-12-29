package org.open.easytrip.control;

import java.util.Date;

import org.open.easytrip.R;
import org.open.easytrip.entity.LocationBean;
import org.open.easytrip.entity.ParcelableLocationBean;
import org.open.easytrip.helper.IgnoreListHelper;
import org.open.easytrip.service.MainService;
import org.open.easytrip.service.MainService.LocalBinder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class MainActivity extends AppActivity implements MainService.ICallBackActivity {

	private static final int NEW_LOCATION_REQUEST = 1;
	private static final int TIME_FOR_LAST_LOCATION = 60 * 1000 /*milliseconds*/;
	
	/**
	 * Called by onCreate() to initialize screen stuff
	 */
	private void initializeScreen() {
		setContentView(R.layout.activity_main);
		
	    // Set the hardware buttons to control volume
		MainActivity.this.setVolumeControlStream(AudioManager.STREAM_ALARM);
		
		//Never turn the screen off
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		textView(R.id.txtMain).setVisibility(View.INVISIBLE);
		textView(R.id.txtMain).setBackgroundColor(Color.RED);
		
		//Callback instance must be created AFTER layout inflation
//		alertCallback = new AlertCallback();
		//FIXME alertCallback should not be initialized here, but during service creating. The problem is that accessing its views before
		//inflating the main activity will result on errors. Maybe that won't be a problem when screen manipulations are separated from the
		//other ones (like the sound alarm)
		
		switch (getResources().getConfiguration().orientation) {
		case Configuration.ORIENTATION_PORTRAIT:
			//Default behavior. Do nothing
			break;
		case Configuration.ORIENTATION_LANDSCAPE:
			//Reorder the layout
			((LinearLayout)findViewById(R.id.layoutAlarm)).setOrientation(LinearLayout.HORIZONTAL);
			textView(R.id.txtMain).setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT));
			break;
		};
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initializeScreen();
	}
	
	/**
	 * Clicking on main alert text view, stop alarming the current location (supposes the user
	 * is already aware of it)
	 */
	public void txtMainClick(View view)  
	{
		bos.getAlertBO().forceStopAlarm(mainService.getGpsCallback());
	}  

	public void startService(View view) {
		startService(new Intent(this, MainService.class));
		bindToService();
	}
	
	public void stopService(View view) {
		stopService(new Intent(this, MainService.class));
		unbindFromService();
	}
	
	
	/**
	 * OnClick event for btnUpdateSettings
	 */
	public void btnNewLocationClick(View view)  
	{
	      
		//Starts by loading the last known location from GPS
		Location l = getLocationManager().getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (null == l || (new Date().getTime() - l.getTime() > TIME_FOR_LAST_LOCATION) ) {
			toast("No GPS position availabe");
			return;
		}
	
		//Begin constructing the new location
		LocationBean locationBean = new LocationBean();
		locationBean.setLatitude(l.getLatitude());
		locationBean.setLongitude(l.getLongitude());
		locationBean.setDirection(l.hasBearing() ? Math.round(l.getBearing()) : null);
		
		//Call the save location activity passing the new location
		startActivityForResult(
				new Intent(this, SaveLocationActivity.class).
				putExtra("location", new ParcelableLocationBean(locationBean))
				, NEW_LOCATION_REQUEST);
	}  
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_CANCELED)
			return;
		
		switch (requestCode) {
		case NEW_LOCATION_REQUEST:
			//Won't alarm the just created location
			ParcelableLocationBean parcelableLocation = (ParcelableLocationBean)data.getParcelableExtra("location");
			LocationBean newLocation = parcelableLocation.getLocationBean();
			IgnoreListHelper.getInstance().put(newLocation.getId());
			break;
		}
	}	
	

	/**
	 * Update a progress bar to reflect the distance to target relative to the current search radius
	 * Pass -1 on either params to hide the progress bar
	 */
	public void showProgress(int distance, int searchRadius) {
		
		//Special case: hide the progress bar
		if (distance == -1 || searchRadius == -1) {
			findViewById(R.id.vwCrossedDistance).setVisibility(View.GONE);
			return;
		}
		
		LinearLayout.LayoutParams params;
		//Already crossed
		float remainingPercentage = 1f*distance/searchRadius;
		params = new LinearLayout.LayoutParams(30, 0, 1-remainingPercentage);
		findViewById(R.id.vwCrossedDistance).setVisibility(View.VISIBLE);
		findViewById(R.id.vwCrossedDistance).setLayoutParams(params);
		//Remaining
		params = new LinearLayout.LayoutParams(30, 0, remainingPercentage);
		findViewById(R.id.vwRemainingDistance).setLayoutParams(params);
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)	{
		switch (item.getItemId()) {
		case R.id.user_locations_title:
			startActivity(new Intent(this, UserLocationsActivity.class));
			return true;
		case R.id.settings_title:
			startActivity(new Intent(this, EasyTripPrefs.class));
			return true;
		case R.id.exit_title:
			finish();
			return true;
		}
		return false;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		startService(new Intent(this, MainService.class));
		bindToService();

//		if (mainService != null)
//			mainService.registerLocationUpdate();
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		unbindFromService();
		if (! bos.getPreferencesBO().isRunInBackgroud())
			stopService(new Intent(this, MainService.class));
			
	}

	private void bindToService() {
		if (! serviceConnected) {
        	serviceConnected = true;
			final Intent intent = new Intent(this, MainService.class);
			bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		}
	}
	
	private void unbindFromService() {
		if (serviceConnected) {
        	serviceConnected = false;
			unbindService(mConnection);
		}
	}

    private MainService mainService;
    private boolean serviceConnected = false;
    /** Defines callbacks for service binding, passed to bindService() */
    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mainService = binder.getService();
    		mainService.setCallBack(MainActivity.this);
//			mainService.registerLocationUpdate(); //Turn GPS on
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
    		if (mainService != null) {
//    			mainService.unregisterLocationUpdate(); //Turn GPS off
    			mainService.setCallBack(null);
    		}
        	mainService = null;
        }
    };

	@Override
	public void update(int resourceId, String message) {
		TextView txtMain = textView(resourceId);
		if (null != message) {
			txtMain.setText(message);
			txtMain.setVisibility(View.VISIBLE);
			//TODO And when should we turn visibility off?
		} else {
			textView(resourceId).setVisibility(View.GONE);
			textView(resourceId).postInvalidate();
		}
	}

	private LocationManager getLocationManager() {
		return (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	}

	/**
	 * Special update method to be used by other threads, not the main UI thread 
	 */
	@Override
	public void updateOnUIThread(final int resourceId, final String message) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				update(resourceId, message);
			}
		});
	}
/*
	@Override
	public void alertGPSOff(int delay) {
		TextView txtMain = textView(R.id.txtUserMessage);
		txtMain.setBackgroundColor(Color.YELLOW);
		txtMain.setText("X");
		txtMain.setVisibility(View.VISIBLE);
	}

	@Override
	public void alertIncommingLocation(String message) {
		TextView txtMain = textView(R.id.txtUserMessage);
		txtMain.setBackgroundColor(Color.RED);
		txtMain.setText(message);
		txtMain.setVisibility(View.VISIBLE);
	}
*/
	
}
