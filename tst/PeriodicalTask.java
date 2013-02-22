package org.open.easytrip.control;

import org.open.easytrip.R;
import org.open.easytrip.entity.LocationBean;
import org.open.easytrip.helper.AlarmControllerHelper;
import org.open.easytrip.helper.VibrationHelper;

import android.content.Context;
import android.location.LocationManager;
import android.os.Vibrator;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class PeriodicalTask implements Runnable {

	private final MainActivity mainActivity;
	private int lastAlarmStart = 0;
	
	public class AlertTask implements Runnable {
		private static final int ALARM_INTERVAL = 5 * MainActivity.ONE_TIME_SLICE;

		public void run() {
			//There is an alarm registered.
			final TextView txtMain = mainActivity.textView(R.id.txtMain);
        	if (mainActivity.currentAlerm != null) {
        		Toast.makeText(mainActivity, "ALARM!", Toast.LENGTH_SHORT).show();
        		final LocationBean lastLocationAlarmed = mainActivity.lastLocationAlarmed;
				txtMain.setText(lastLocationAlarmed.getSpeedLimit() != null ? ""+lastLocationAlarmed.getSpeedLimit(): "0"); 
        		//Blink txtMain
				if (txtMain.getVisibility() == View.VISIBLE)
					txtMain.setVisibility(View.INVISIBLE);
				else
					txtMain.setVisibility(View.VISIBLE);
				
				boolean supportsSpeed = mainActivity.getLocationManager().getProvider(LocationManager.GPS_PROVIDER).supportsSpeed();
				if (mainActivity.bos.getAlertBO().playSoundAlarm(supportsSpeed, currentSpeed, locationBean)) {
					
					AudioControllerHelper.getInstance().playAlarm();
					//Also vibrate
					if (mainActivity.bos.getPreferencesBO().isVibrationAlarm())
						VibrationHelper.getInstance().vibrate((Vibrator) mainActivity.getSystemService(Context.VIBRATOR_SERVICE));
				}

				showProgress(distanceMeters);
				
        	} else {
        		txtMain.setVisibility(View.INVISIBLE);
        	}
        }
	}

	
	public PeriodicalTask(MainActivity _mActivity) {
		mainActivity = _mActivity;
	}
	
	private final Runnable timerAction = new AlertTask();
	
	public void run() {
		mainActivity.runOnUiThread(timerAction);
	}
}

