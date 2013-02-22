package org.open.easytrip.helper;

import org.open.easytrip.R;
import org.open.easytrip.dao.DAOFactory;
import org.open.easytrip.exception.AppRuntimeException;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.ToneGenerator;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

/**
 * Manages the alive and alarm notifications, their duration, interval and so on.
 */
public class AlarmControllerHelper {
	
	private static AlarmControllerHelper instance;

	private static AlphaAnimation blinkingAnimator;

	private final ToneGenerator beepTone = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
//	private final ToneGenerator alarmTone = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
	
	private long lastBeep = 0;
//	long lastAlarm = 0;
	
	/**
	 * Flag indicating there is an alarm happening.
	 */
	private boolean alarming;
	
	private static final long BEEP_INTERVAL = 5000/*ms*/;
//	private static final long ALARM_INTERVAL = 2000/*ms*/;
//	private static final int ALARM_DURATION = Math.round(ALARM_INTERVAL / 2);
	private static final int ALARM_DURATION = 1000/*ms*/;
	private static final int VIBRATE_DURATION = 500/*ms*/;
	
	private final SoundPool sounds;
	private final int alarmIndex;
//	final int aliveIndex;

	private int stopSignal = 0;
	
	/**
	 * Can not be explicitly created.
	 */
	private AlarmControllerHelper(Activity context) {
		sounds = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
		alarmIndex = sounds.load(context, R.raw.alarm, 1);
//		aliveIndex = sounds.load(context, R.raw.alive, 1);
	    // Set the hardware buttons to control the music
	    context.setVolumeControlStream(AudioManager.STREAM_MUSIC);
	    alarming = false;
	    blinkingAnimator = new AlphaAnimation(1.0f, 0.0f);
	    blinkingAnimator.setDuration(ALARM_DURATION); //You can manage the time of the blink with this parameter
	    blinkingAnimator.setStartOffset(0);
	    blinkingAnimator.setRepeatMode(Animation.REVERSE);
	    blinkingAnimator.setRepeatCount(Animation.INFINITE);
	    
	}
	
	/**
	 * Initializes the factory injecting the database connection.
	 */
	public static void init(Activity context) {
		if (instance != null)
			return;
		instance = new AlarmControllerHelper(context);
	}
	
	/**
	 * Singleton instance implementation. 
	 */
	public static AlarmControllerHelper getInstance() {
		if (instance == null) 
			throw new AppRuntimeException("ERROR. Init not called prior to AlarmControllerHelper.getInstance()");
		return instance;
	}

	
	public void playBeep() {
		if (System.currentTimeMillis() - lastBeep < BEEP_INTERVAL)
			return;

		stopAlarm(null);
		
		beepTone.startTone(ToneGenerator.TONE_PROP_BEEP, 500);
		lastBeep = System.currentTimeMillis();
	}

	/**
	 * Do all the sensitive alarms (visual, with sound and with vibration)
	 * @param visualAlarm A view to blink (always will)
	 * @param withSound Should the sound alarm be used
	 * @param withVibration Should the vibration alarm be used
	 * @param vibrator System service to vibrate
	 */
	public void alarm(View visualAlarm, boolean withSound, boolean withVibration, Vibrator vibrator) {
		if (alarming)
			return;
		
		alarming = true;
		
		startBlinking(visualAlarm);
//		visualAlarm.setVisibility(View.VISIBLE);
		
//		if (System.currentTimeMillis() - lastAlarm < ALARM_INTERVAL)
//			return; //not time yet for sound alarms

		if (withSound) {
//			beepTone.stopTone();
//			alarmTone.startTone(ToneGenerator.TONE_DTMF_A, ALARM_DURATION);
			beepTone.stopTone();
			stopSignal = sounds.play(alarmIndex, 1f/*full volume*/, 1f/*full volume*/, 1, -1/*loop forever*/, 1f);
		}
		
		if (withVibration) {
			vibrator.vibrate(VIBRATE_DURATION);
		}
//		lastAlarm = System.currentTimeMillis();
	}
	
	private void startBlinking(View visualAlarm) {
		visualAlarm.setVisibility(View.VISIBLE);
		blinkingAnimator.reset();
		visualAlarm.startAnimation(blinkingAnimator);	
	}		

	public void stopAlarm(View visualAlarm) {
//		alarmTone.stopTone();
//		visualAlarm.clearAnimation();
		alarming = false;
		sounds.stop(stopSignal);
		if (visualAlarm != null) {
			blinkingAnimator.cancel();
			visualAlarm.setVisibility(View.INVISIBLE);
		}
	}
	
}
