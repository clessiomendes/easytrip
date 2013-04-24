package org.open.easytrip.control;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.open.easytrip.AppConstants;
import org.open.easytrip.AppUtils;
import org.open.easytrip.R;
import org.open.easytrip.bo.AlertBO;
import org.open.easytrip.bo.AlertBO.IGpsCallBack;
import org.open.easytrip.bo.BOFactory;
import org.open.easytrip.bo.IImportLocationsBO;
import org.open.easytrip.dao.DAOFactory;
import org.open.easytrip.dao.DatabaseStructureDAO;
import org.open.easytrip.entity.LocationBean;
import org.open.easytrip.entity.ParcelableLocationBean;
import org.open.easytrip.helper.AlarmControllerHelper;
import org.open.easytrip.helper.AppDatabaseHelper;
import org.open.easytrip.helper.IgnoreListHelper;
import org.open.easytrip.service.MainService;
import org.open.easytrip.service.MainService.LocalBinder;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.SoundPool;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class MainActivity extends AppActivity {
	
//============================  L O C A L     F I E L D S  ==========================================================================
	
	private static final int NEW_LOCATION_REQUEST = 1;
	private int showAliveDisplay= 0;
	/**
	 * Is bearing information provided by the GPS engine of the present device? Access through homonymous method. 
	 */
	private Boolean supportsBearing = null;
	/**
	 * Is speed information provided by the GPS engine of the present device? Access through homonymous method. 
	 */
	private Boolean supportsSpeed = null;

    //Beep every once in a while
	private final ToneGenerator beepTone = new ToneGenerator(AudioManager.STREAM_SYSTEM, 50);
	private long lastBeep = 0;
	private static final long BEEP_INTERVAL = 5000/*ms*/;
    
	/**
	 * Used to blink phone's LED
	 */
    private NotificationManager mNotificationManager;
	
    
    
    
    
//============================  N E S T E D      C L A S S E S  ==========================================================================
    
    private MainService mainService;
    /** Defines callbacks for service binding, passed to bindService() */
    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mainService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };
    
	private final LocationListener myLocationListener = new MyLocationListener();
	/**
	 * Listen to GPS location changes.
	 */
	private class MyLocationListener implements LocationListener {
		public void onLocationChanged(Location location) {
			showAlive(location);
			bos.getAlertBO().checkLocations(gpsCallback, AppUtils.convert(location), supportsSpeed(), supportsBearing());
		}
		
		public void onProviderDisabled(String provider){		}
		
		public void onProviderEnabled(String provider){		}
		
		public void onStatusChanged(String provider, int status, Bundle extras){		}
	};

	private IGpsCallBack gpsCallback;
	/**
	 * Alarm incoming location, vibrating and showing its distance.
	 * @param distance In meters
	 * @param currentSpeed In km/h
	 * @param locationBean
	 */
	public class AlertCallback implements AlertBO.IGpsCallBack, OnErrorListener {
		private static final float ALARM_VOLUME = 0.1f/*full volume = 1f*/;
		//	    final AlphaAnimation blinkingAnimator;
		private int stopSignal = 0;
		private final SoundPool sounds;
		private final int alarmIndex;
		private boolean visualAlarmActive;
		private boolean soundAlarmActive;
		
		public AlertCallback() {
//			blinkingAnimator = new AlphaAnimation(1.0f, 0.0f);
//			blinkingAnimator.setDuration(700/*ms*/); //You can manage the time of the blink with this parameter
//			blinkingAnimator.setStartOffset(0);
//			blinkingAnimator.setRepeatMode(Animation.REVERSE);
//			blinkingAnimator.setRepeatCount(Animation.INFINITE);
//			textView(R.id.txtMain).setAnimation(blinkingAnimator);

			sounds = new SoundPool(1, AudioManager.STREAM_ALARM, 0);
			alarmIndex = sounds.load(MainActivity.this, R.raw.alarm, 1);
			
//			mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.alarm);
//			mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
//			mediaPlayer.setLooping(true);
//			mediaPlayer.setOnErrorListener(this);
//			mediaPlayer.setVolume(0.3f, 0.3f);
//			mediaPlayer.start();
		}

		@Override
		public void startVisualAlarm(int distance, int currentSpeed, LocationBean locationBean) {
			//Progress bar must be updated always
			if (locationBean.getSearchRadius() != null)
				showProgress(distance, locationBean.getSearchRadius());
			
			//Alarm already being shown. No need to restart blinking.
//			if (visualAlarmActive)
//				return;
//			visualAlarmActive = true;

			//			TextView txtUserMessage = textView(R.id.txtUserMessage);
			//			txtUserMessage.setText(txtUserMessage.getText()+" - "+distanceMeters+getQuantityString(R.plurals.meters, distanceMeters));  
			
			TextView txtMain = textView(R.id.txtMain);
			txtMain.setText(locationBean.getSpeedLimit() != null ? ""+locationBean.getSpeedLimit(): "0");
			txtMain.setVisibility(View.VISIBLE);
			//		AlarmControllerHelper.getInstance().alarm(txtMain, withSound, withVibration, (Vibrator)getSystemService(Context.VIBRATOR_SERVICE));
		}

		@Override
		public void stopVisualAlarm() {
//			visualAlarmActive = false;
			textView(R.id.txtMain).setVisibility(View.GONE);
			textView(R.id.txtMain).postInvalidate();
			
			findViewById(R.id.vwCrossedDistance).setVisibility(View.GONE);
		}
		
		@Override
		public void startSoundAlarm() {
			if (soundAlarmActive)
				return;
			soundAlarmActive = true ;
//			((AudioManager)getSystemService(Context.AUDIO_SERVICE)).setStreamSolo(AudioManager.STREAM_NOTIFICATION, true);
			((AudioManager)getSystemService(Context.AUDIO_SERVICE)).setStreamMute(AudioManager.STREAM_MUSIC, true);
			stopSignal = sounds.play(alarmIndex, ALARM_VOLUME, ALARM_VOLUME, 1, -1/*loop forever*/, 1f);
		}

		@Override
		public void stopSoundAlarm() {
			soundAlarmActive = false;
			sounds.stop(stopSignal);
//			((AudioManager)getSystemService(Context.AUDIO_SERVICE)).setStreamSolo(AudioManager.STREAM_NOTIFICATION, false);
			((AudioManager)getSystemService(Context.AUDIO_SERVICE)).setStreamMute(AudioManager.STREAM_MUSIC, false);
		}

		@Override
		public boolean isSoundAlarmActive() {
			return soundAlarmActive;
		}

		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			// TODO Auto-generated method stub
			mp.reset();
			return false;
		}
	};
	
	private final SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferencesListener();
	/**
	 * Listen to preferences changes. Restart GPS location provider with the new interval parameter selected by the user.
	 */
	private class SharedPreferencesListener implements SharedPreferences.OnSharedPreferenceChangeListener {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences prefs,String key) {
			if (key.equals(getString(R.string.preferences_gps_interval))) {
				registerLocationUpdate();
			} 
			TextView myLocationText = textView(R.id.txtUserMessage);
			myLocationText.setText(getString(R.string.settings_updated));
		}
	};
	
	/**
	 * Inner class implementing the task of updating the locations database with the most recent data found on the external resources. 
	 */
	private class ReloadLocationsTask extends AsyncTask {
		final IImportLocationsBO.OnReloadProgress progressListener = new IImportLocationsBO.OnReloadProgress() {
			@Override
			public void reportProgress(int percentage) {
				String preFormated = ""+getText(R.string.reload_location_progress);
				publishProgress(String.format(preFormated, percentage));
			}
		};
		
	    // Do the long-running work in here
		@Override
	    protected Long doInBackground(Object... urls) {
			return (long)bos.getImportLocationsBO().reloadLocations(progressListener);
	    }

		// This is called each time you call publishProgress()
		@Override
		protected void onProgressUpdate(Object... values) {
			textView(R.id.txtUserMessage).setText(""+values[0]);
	    }

		@Override
		protected void onPostExecute(Object result) {
			//If some locations were updated, report the user.
			if (result != null && result instanceof Number && ((Number)result).intValue() > 0)
				toast(String.format(getString(R.string.finished_importing_alerts), result));
		}
		
	}


	
	
	
	
//============================  R E G U L A R       M E T H O D S  ==========================================================================
	
	private boolean supportsSpeed() {
		if (supportsSpeed == null)
			supportsSpeed = getLocationManager().getProvider(LocationManager.GPS_PROVIDER).supportsSpeed();
		return supportsSpeed;
	}
	
	private boolean supportsBearing() {
		if (supportsBearing == null)
			supportsBearing = getLocationManager().getProvider(LocationManager.GPS_PROVIDER).supportsBearing();
		return supportsBearing;
	}
	
	/**
	 * Cleaning up location listener and notifications.
	 */
	private void unregisterLocationUpdate() {
		getLocationManager().removeUpdates(myLocationListener);
		mNotificationManager.cancel(1);
	}

	/**
	 * Register the location update service (or mock it if mockGPS() is called instead)
	 */
	private void registerLocationUpdate() {
//		mockGPS();
		getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 0/*m*/, myLocationListener);
	}

	private void mockGPS() {
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	    scheduler.scheduleAtFixedRate(new Runnable() {
	        public void run() {
	        	runOnUiThread(new Runnable() {
	                public void run() {
	    	            myLocationListener.onLocationChanged(mockLocation());
	                }
	            });
	        }
	    }, 0, 3, TimeUnit.SECONDS);
	}

	private static double[][] path = {{-19.94378 , -43.98770}
	,{-19.94378 , -43.98770}
	,{-19.94378 , -43.98770}
	,{-19.94378 , -43.98770}
	,{-19.94509 ,-43.98709 } //19,94509, -43,98709
	,{-19.94657 ,-43.98761 } //19,94657, -43,98761
	,{-19.94657 ,-43.98761 } //19,94657, -43,98761
	,{-19.94657 ,-43.98761 } //19,94657, -43,98761
	,{-19.94378 , -43.98770}
//	,{-19.94657 ,-43.98761 } //19,94657, -43,98761
//	,{-19. ,-43. } 
	};
	private int pathIndex = 0;
	
	
	private Location mockLocation() {
		Location location = new Location("GPS mock");
    	location.setBearing(139);
    	location.setSpeed(90f / 3.6f);
    	location.setLatitude(path[pathIndex][0]);
    	location.setLongitude(path[pathIndex][1]);
    	location.setTime(System.currentTimeMillis());
        if (++pathIndex >= path.length) 
        	pathIndex = 0;
		return location;
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		//Just to signal it is not the first time the application opens the main activity
		return new Object();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initializeScreen();
		
		initializeListeners();
		
		initializeServices();
		
	    mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		final Object alreadyStarted = getLastNonConfigurationInstance();
		if (alreadyStarted == null) {
			/* OPTIMIZING. Sometimes, android will return a lastNonConfigurationInstance which
			 * assure the application has already run at least once.
			 * 
			 * Initializing non-visual components (like database connections, factories, etc.)
			 * should go here */

			initializeFactoriesAndHelpers();

			initializeDatabase();
			
		}
	}

	private void initializeServices() {
 		final Intent intent = new Intent(this, MainService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	private void initializeListeners() {
		//Listen for preference changes
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(prefListener);
		
		//Turn GPS on
//		registerLocationUpdate();
	}
	
/*
	private void initializeTimer() {
		ScheduledExecutorService scheduler =
	            Executors.newSingleThreadScheduledExecutor();
	    scheduler.scheduleAtFixedRate(new Runnable() {

	        public void run() {
	        	runOnUiThread(new Runnable() {
	                public void run() {
	                    Toast.makeText(getApplicationContext(), "It works", Toast.LENGTH_SHORT).show();
	                }
	            });
	        }
	    }, 10, 10, TimeUnit.SECONDS);
	}
*/
	/**
	 * Initializing services and BOs
	 */
	private void initializeFactoriesAndHelpers() {
		DAOFactory.init(this);
		BOFactory.init(); //BO initialization must precede getWritableDatabase()
		AlarmControllerHelper.init(this);
	}

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
		gpsCallback = new AlertCallback();
		
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

	/**
	 * Initialize the database, reloading locations in background, if necessary.
	 */
	private void initializeDatabase() {
//		AppDatabaseHelper.getInstance(this).getWritableDatabase();
//		DAOFactory.getInstance().getDatabaseStructureDAO().checkDatabase();
		new ReloadLocationsTask().execute();
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
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
//		if (! bos.getPreferencesBO().isRunInBackgroud())
//			unregisterLocationUpdate(); //Turn GPS off
	}

	private void showAlive(Location location) {
		//TODO Too much logic. Refactor to BOs?
		String message = (location.hasSpeed() ? AppUtils.ms2Kmh(location.getSpeed()) + getString(R.string.km_h) : "")  
				+  (location.hasBearing() ? Math.round(location.getBearing()) + " º " : "");  
		
		if (AppUtils.isDevelopmentTime())
			message += "("+android.text.format.DateFormat.getTimeFormat(this).format(new Date())+")";  
		
		TextView txtUserMessage = textView(R.id.txtUserMessage);
		txtUserMessage.setText(message);
		
		//Circling through |/-\ to simulate a rotating bar
		TextView txtAliveDisplay = textView(R.id.txtAliveDisplay);
		String aliveText = ""+AppConstants.showAliveDisplay[showAliveDisplay++ % AppConstants.showAliveDisplay.length];
		txtAliveDisplay.setText(aliveText);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			blinkLED();

		//Beep every once in a while
		if (bos.getPreferencesBO().isBeep() && System.currentTimeMillis() - lastBeep >= BEEP_INTERVAL && ( !gpsCallback.isSoundAlarmActive())) {
			beepTone.startTone(ToneGenerator.TONE_PROP_BEEP, 500);
			lastBeep = System.currentTimeMillis();
		}
	}

	/**
	 * Update a progress bar to reflect the distance to target relative to the current search radius
	 * @param searchRadius 
	 */
	private void showProgress(int distance, int searchRadius) {
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

	@TargetApi(11)
	private void blinkLED() {
	      mNotificationManager.cancel(1);

	      Notification mNotification = new NotificationCompat.Builder(this)
	      	.setLights(Color.WHITE, 500, 100000)
	      	.setAutoCancel(false)
	      	.build();

	      mNotificationManager.notify(1, mNotification);
	}
	
	/**
	 * Clicking on main alert text view, stop alarming the current location (supposes the user
	 * is already aware of it)
	 */
	public void txtMainClick(View view)  
	{
		bos.getAlertBO().forceStopAlarm(gpsCallback);
	}  

	/**
	 * OnClick event for btnUpdateSettings
	 */
	public void btnNewLocationClick(View view)  
	{
	      
		//Starts by loading the last known location from GPS
		Location l = getLocationManager().getLastKnownLocation(LocationManager.GPS_PROVIDER);
	
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

	private LocationManager getLocationManager() {
			return (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	}

}
