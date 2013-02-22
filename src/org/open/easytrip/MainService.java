package org.open.easytrip;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.widget.Toast;

public class MainService extends Service {
	
	MediaPlayer player;
	private static final String TAG = "MyService";
	
	public MainService() {
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
		player.start();
		return Service.START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		Toast.makeText(this, "My Service Created", Toast.LENGTH_LONG).show();
		player = MediaPlayer.create(this, R.raw.music);
		player.setLooping(false); // Set looping
		
	}

	@Override
	public void onDestroy() {
		Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
		player.stop();
	}
	
}
