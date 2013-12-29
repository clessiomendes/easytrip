package org.open.easytrip.bo;

import org.open.easytrip.AppUtils;
import org.open.easytrip.entity.GpsMovement;
import org.open.easytrip.entity.LocationBean;
import org.open.easytrip.helper.IgnoreListHelper;

public class AlertBO extends AppBO {
	
	/**
	 * Callback interface to report the user of the alarm events. 
	 */
	public interface IGpsCallBack {
		public void startVisualAlarm(int distance, GpsMovement gpsMovement, LocationBean locationBean);
		public void startSoundAlarm();
		public void stopSoundAlarm();
		public void stopVisualAlarm();
		boolean isSoundAlarmActive();
	}

	/**
	 * Sound alarm will be played if speed limit is exceeded using this margin of safety 
	 */
	private static final int SPEED_SAFE_MARGIN = 0/*km/h*/;
	/**
	 * Sometimes the GPS show slow speeds when the agent is in fact stopped. We give it a margin of acceptance thow.
	 */
	private static final float CONSIDER_STOPPED = 5;

	/**
	 * Decides whether to play a sound alarm or not based on location attributes and current speed 
	 * @param speedSupported
	 * @param gpsMovement
	 * @param locationBean
	 * @return
	 */
	private boolean playSoundAlarm(boolean speedSupported, GpsMovement gpsMovement, LocationBean locationBean) {
		//Check user preferences
		if (! bos.getPreferencesBO().isSoundAlarm())
			return false;
		
		return commonAlarmDecision(speedSupported, gpsMovement, locationBean);
	}
	
	/**
	 * Decides whether to play a vibration alarm or not based on location attributes and current speed 
	 * @param speedSupported
	 * @param gpsMovement
	 * @param locationBean
	 * @return
	 */
	private boolean playVibrationAlarm(boolean speedSupported, GpsMovement gpsMovement, LocationBean locationBean) {
		//Check user preferences
		if (! bos.getPreferencesBO().isVibrationAlarm())
			return false;
		
		return commonAlarmDecision(speedSupported, gpsMovement, locationBean);
	}

	/**
	 * Refactored from both playSoundAlarm and playVibrationAlarm
	 */
	private boolean commonAlarmDecision(boolean speedSupported, GpsMovement gpsMovement, LocationBean locationBean) {
		//We decide based on current speed. If speed is not provided by GPS, it is safer to always alarm.
		if (! speedSupported)
			return true;
		
		if (locationBean.getType().speedControl) { //for speed alerts
			//Compare current speed to the limit
			return (gpsMovement.getSpeedKh() > locationBean.getSpeedLimit() - SPEED_SAFE_MARGIN );
		} else { //for non speed related alerts
			//don't alarm if we are stopped
			return gpsMovement.getSpeedKh() > CONSIDER_STOPPED; 
		}
	}

	/**
	 * Alarm incoming location, vibrating and showing its distance.
	 * @return true if some location is found. False if none;
	 */
	public boolean checkLocations(IGpsCallBack callback, GpsMovement gpsMovement, boolean supportsSpeed, boolean supportsBearing) {
		LocationBean closest = bos.getRetrieveLocationsBO().findClosestLocation(
				gpsMovement.getLatitude(), gpsMovement.getLongitude(), IgnoreListHelper.getInstance(), 
				supportsSpeed ? (int)gpsMovement.getSpeedMs() : null, supportsBearing ? (int)gpsMovement.getBearing() : null);
		
		if (closest != null) {
			//There is a location in sight
			final double distanceInDegrees = AppUtils.distance(gpsMovement.getLatitude(), gpsMovement.getLongitude(), closest.getLatitude(), closest.getLongitude());
			final double distanceInMeters = AppUtils.degrees2meters(distanceInDegrees);
//			final boolean withSound = bos.getAlertBO().playSoundAlarm(supportsSpeed, currentSpeed, closest);
//			final boolean withVibration = bos.getAlertBO().playVibrationAlarm(supportsSpeed, currentSpeed, closest); 

			//visual alarm
			callback.startVisualAlarm((int)distanceInMeters, gpsMovement, closest/*, withSound, withVibration*/);

			if (playSoundAlarm(supportsSpeed, gpsMovement, closest)  ) 
				callback.startSoundAlarm();
			else
				callback.stopSoundAlarm();

			//TODO vibrate if requested
//			final boolean withVibration = bos.getAlertBO().playVibrationAlarm(supportsSpeed, currentSpeed, closest); 
			
			daos.getMemoryStorageDAO().setLastLocationAlarmed(closest);
			
			return true;
			
		} else {
			//No locations in sight
			callback.stopSoundAlarm();
			callback.stopVisualAlarm();
			return false;
		}
	}

	public void forceStopAlarm(IGpsCallBack callback) {
		LocationBean lastLocationAlarmed = daos.getMemoryStorageDAO().getLastLocationAlarmed();
		//This location will be ignored in the near future searches
		if (lastLocationAlarmed != null)
			IgnoreListHelper.getInstance().put(lastLocationAlarmed.getId()); //TODO Ignorelist should go to MemoryStorageDAO.
		
		callback.stopVisualAlarm();
		callback.stopSoundAlarm();
	}
	
}
