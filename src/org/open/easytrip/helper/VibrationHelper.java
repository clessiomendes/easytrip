package org.open.easytrip.helper;

import android.content.Context;
import android.os.Vibrator;

/**
 * Manages the alive and alarm notifications, their duration, interval and so on. 
 */
public class VibrationHelper {
	
	private static VibrationHelper instance;
	long lastVibration = 0;
	final long VIBRATION_INTERVAL = 2000/*ms*/;
	final long VIBRATION_DURATION = 1000/*ms*/;
	
	/**
	 * Can not be explicitly created.
	 */
	private VibrationHelper() {
	}
	
	/**
	 * Singleton instance implementation. 
	 */
	public static VibrationHelper getInstance() {
		if (instance == null)
			instance = new VibrationHelper();
		return instance;
	}
	
	public void vibrate(Vibrator vibrator) {
		if (System.currentTimeMillis() - lastVibration < VIBRATION_INTERVAL)
			return;
		
		vibrator.vibrate(VIBRATION_DURATION);
		lastVibration = System.currentTimeMillis();
	}
	
}
