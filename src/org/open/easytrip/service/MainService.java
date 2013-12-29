package org.open.easytrip.service;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.open.easytrip.AppConstants;
import org.open.easytrip.AppUtils;
import org.open.easytrip.R;
import org.open.easytrip.bo.AlertBO;
import org.open.easytrip.bo.AlertBO.IGpsCallBack;
import org.open.easytrip.bo.BOFactory;
import org.open.easytrip.bo.IImportLocationsBO;
import org.open.easytrip.dao.DAOFactory;
import org.open.easytrip.entity.GpsMovement;
import org.open.easytrip.entity.LocationBean;
import org.open.easytrip.helper.AlarmControllerHelper;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
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
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class MainService extends AppService {
	
	private boolean mockGPS = true;

	private Timer statusTimer;
	private Date lastGPSUpdate = new Date(); /*Let's pretend GPS is updated at start*/
	protected long MAXIMUM_GPS_DELAY_EXPECTED = 5000/*miliseconds*/;
	
	
	/**
	 * Interface to be implemented by activities that bind to this service
	 */
	public interface ICallBackActivity {
		void update(int resourceId, String message);
		/**
		 * Special update method to be used by other threads, not the main UI thread 
		 */
		void updateOnUIThread(int resourceId, String message);
		void showProgress(int distance, int searchRadius);
//		void alertGPSOff(int delay);
//		void alertIncommingLocation(String message);
	}

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
	
	protected BOFactory bos = BOFactory.getInstance();
    
    
    
    
//============================  N E S T E D      C L A S S E S  ==========================================================================

	private static final String TAG = "EasyTripService";
	
	private final IBinder mBinder = new LocalBinder();
	
	/**
    * Class used for the client Binder.  Because we know this service always
    * runs in the same process as its clients, we don't need to deal with IPC.
    */
    public class LocalBinder extends Binder {
        public MainService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MainService.this;
        }
    }
    
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

	private GpsCallback gpsCallback;
	/**
	 * Alarm incoming location, vibrating and showing its distance.
	 * @param distance In meters
	 * @param currentSpeed In km/h
	 * @param locationBean
	 */
	public class GpsCallback implements AlertBO.IGpsCallBack, OnErrorListener {
		private static final float ALARM_VOLUME = 0.1f/*full volume = 1f*/;
		//	    final AlphaAnimation blinkingAnimator;
		private int stopSignal = 0;
		private final SoundPool sounds;
		private final int alarmIndex;
		private boolean visualAlarmActive;
		private boolean soundAlarmActive;
		
		public GpsCallback() {
//			blinkingAnimator = new AlphaAnimation(1.0f, 0.0f);
//			blinkingAnimator.setDuration(700/*ms*/); //You can manage the time of the blink with this parameter
//			blinkingAnimator.setStartOffset(0);
//			blinkingAnimator.setRepeatMode(Animation.REVERSE);
//			blinkingAnimator.setRepeatCount(Animation.INFINITE);
//			textView(R.id.txtMain).setAnimation(blinkingAnimator);

			sounds = new SoundPool(1, AudioManager.STREAM_ALARM, 0);
			alarmIndex = sounds.load(MainService.this, R.raw.alarm, 1);
			
//			mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.alarm);
//			mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
//			mediaPlayer.setLooping(true);
//			mediaPlayer.setOnErrorListener(this);
//			mediaPlayer.setVolume(0.3f, 0.3f);
//			mediaPlayer.start();
		}

		@Override
		public void startVisualAlarm(int distance, GpsMovement gpsMovement, LocationBean location) {
			//Progress bar must be updated always
			if (location.getSearchRadius() != null)
				showCrossedDistanceInActivity(distance, location.getSearchRadius());
			
			//Alarm already being shown. No need to restart blinking.
//			if (visualAlarmActive)
//				return;
//			visualAlarmActive = true;

			//			TextView txtUserMessage = textView(R.id.txtUserMessage);
			//			txtUserMessage.setText(txtUserMessage.getText()+" - "+distanceMeters+getQuantityString(R.plurals.meters, distanceMeters));  
			
			updateActivity(R.id.txtMain, location.getSpeedLimit() != null ? ""+location.getSpeedLimit(): "0");
			String messageShouldIgnore = "";
			messageShouldIgnore += location.getOutOfDirection() ? "Is out of direction (me:"+gpsMovement.getBearing()+", location:"+location.getDirection()+"). " : "";
			messageShouldIgnore += location.getOutOfRange()!=null ? "Is out of range ("+Math.toDegrees(location.getOutOfRange())+"º). " : "";
			updateActivity(R.id.txtMessageLocationDetails, messageShouldIgnore);
			
			//		AlarmControllerHelper.getInstance().alarm(txtMain, withSound, withVibration, (Vibrator)getSystemService(Context.VIBRATOR_SERVICE));
		}

		@Override
		public void stopVisualAlarm() {
//			visualAlarmActive = false;
			updateActivity(R.id.txtMain, null);
			updateActivity(R.id.txtMessageLocationDetails, null);
			hideCrossedDistanceInActivity();
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
			updateActivity(R.id.txtUserMessage, getString(R.string.settings_updated));
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
			updateActivity(R.id.txtUserMessage, ""+values[0]);
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
		if (mockGPS)
			unmockGPS();
		else {
			getLocationManager().removeUpdates(myLocationListener);
			mNotificationManager.cancel(1);
		}
		if (null != gpsCallback) {
			gpsCallback.stopSoundAlarm();
			gpsCallback.stopVisualAlarm();
		}
	}

	/**
	 * Register the location update service (or mock it if mockGPS() is called instead)
	 */
	private void registerLocationUpdate() {
		if (mockGPS)
			mockGPS();
		else
			getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 0/*m*/, myLocationListener);
	}

	
	private static Timer timer =  new Timer();
	private boolean runTimer = false;
    private final Handler actionHandler = new Handler()
    {   @Override
        public void handleMessage(Message msg) {
        	myLocationListener.onLocationChanged(mockLocation());
        }
    };  	
	private void mockGPS() {
/*		
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
*/
		unmockGPS();
		runTimer = true;
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				if (runTimer) {
					actionHandler.sendEmptyMessage(0);
				}
	        }			
		}, 0, 2000);
		
	}
	private void unmockGPS() {
		runTimer = false;
//		if (null != gpsCallback) {
//			gpsCallback.stopSoundAlarm();
//			gpsCallback.stopVisualAlarm();
//		}
//		if (null != timer)
//			timer.cancel();
//		timer = null;
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
	private ICallBackActivity callBackActivity;
	
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

	private void initializeListeners() {
		gpsCallback = new GpsCallback();
		//Listen for preference changes
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(prefListener);
		//Used to keep the service on foreground
	    mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
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
	 * Initialize the database, reloading locations in background, if necessary.
	 */
	private void initializeDatabase() {
//		AppDatabaseHelper.getInstance(this).getWritableDatabase();
//		DAOFactory.getInstance().getDatabaseStructureDAO().checkDatabase();
		new ReloadLocationsTask().execute();
	}

	private void showAlive(Location location) {
		//TODO Too much logic. Refactor to BOs?
		String message = (location.hasSpeed() ? AppUtils.ms2Kmh(location.getSpeed()) + getString(R.string.km_h) : "")  
				+  (location.hasBearing() ? Math.round(location.getBearing()) + " º " : "");  
		
		if (AppUtils.isDevelopmentTime())
			message += "("+android.text.format.DateFormat.getTimeFormat(this).format(new Date())+")";  
		
		updateActivity(R.id.txtUserMessage, message);
		
		//Circling through |/-\ to simulate a rotating bar
		String aliveText = ""+AppConstants.showAliveDisplay[showAliveDisplay++ % AppConstants.showAliveDisplay.length];
		updateActivity(R.id.txtAliveDisplay, aliveText);
		
		lastGPSUpdate = new Date();
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			blinkLED();

		//Beep every once in a while
		if (bos.getPreferencesBO().isBeep() && System.currentTimeMillis() - lastBeep >= BEEP_INTERVAL && ( !gpsCallback.isSoundAlarmActive())) {
			beepTone.startTone(ToneGenerator.TONE_PROP_BEEP, 500);
			lastBeep = System.currentTimeMillis();
		}
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

	private LocationManager getLocationManager() {
			return (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}
	
	@Override
	public void onCreate() {
		Toast.makeText(this, "My  Service Created", Toast.LENGTH_LONG).show();
		
		initializeListeners();
		
	    initializeFactoriesAndHelpers();

	    initializeDatabase();

		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		registerLocationUpdate(); //Turn GPS on
	    enableStatusTimer(true);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
		unregisterLocationUpdate(); //Turn GPS off
		enableStatusTimer(false);
	}

	public void setCallBack(ICallBackActivity callBackActivity) {
		this.callBackActivity = callBackActivity;
	}

	public IGpsCallBack getGpsCallback() {
		return gpsCallback;
	}


	/**
	 * Updates the screen with some message
	 * FIXME Encapsulation violation. Create separate methods for each type of message instead of passing the resource id.
	 * @param resourceId
	 * @param message
	 */
	private void updateActivity(int resourceId, String message) {
		if (callBackActivity == null)
			return;
		callBackActivity.update(resourceId, message);
	}
/*	
	private void alertGPSOff(int delay) {
		if (callBackActivity == null)
			return;
		callBackActivity.alertGPSOff(delay);
	}

	private void alertIncommingLocation(String message) {
		if (callBackActivity == null)
			return;
		callBackActivity.alertIncommingLocation(message);
	}
*/
	private void hideCrossedDistanceInActivity() {
		if (callBackActivity == null)
			return;
		callBackActivity.showProgress(-1, -1);
	}

	private void showCrossedDistanceInActivity(int distance, Integer searchRadius) {
		if (callBackActivity == null)
			return;
		callBackActivity.showProgress(distance, searchRadius);
	}

	/**
	 * Turn on (or off) the status timer responsible for reporting eventual GPS signal failures
	 * @param on
	 */
	private void enableStatusTimer(boolean on) {
		if (on) {
		//Timer to update the GPS status
		statusTimer = new Timer();
		statusTimer.schedule(new TimerTask() {			
			@Override
			public void run() {
				verifyGPSStatus();
			}
			
		}, 0, 1000);
		} else {
			statusTimer.cancel();
			statusTimer.purge();
		}
	}
	
	
	/**
	 * Verifies the GPS status. If not update for a while, alerts the user.
	 */
	private void verifyGPSStatus() {
		if (callBackActivity != null) {
			Date currentTime = new Date();
			final long gpsDelay = currentTime.getTime() - lastGPSUpdate.getTime();
			if (gpsDelay > MAXIMUM_GPS_DELAY_EXPECTED  ) {
				callBackActivity.updateOnUIThread(R.id.txtCurrentStatus, "GPS not responding for "+gpsDelay+" miliseconds");
//				callBackActivity.updateOnUIThread(R.id.txtCurrentStatus, "GPS not responding for "+gpsDelay+" miliseconds");
			} else {
				callBackActivity.updateOnUIThread(R.id.txtCurrentStatus, "GPS responding fine");
			}
		}
	}
	
	
}
