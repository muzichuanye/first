package com.android.drcc.musicplayer;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class PlayerActivity extends Activity {

	private Button btnPlayOrPause;
	//private SimpleDateFormat time = new SimpleDateFormat("mm:ss");
	private MusicService musicService;
	private boolean tag1 = false;
    private boolean tag2 = false;
    static String sd_path,usb_path,ext_path;
	
	private void bindServiceConnection() {
		Intent intent = new Intent (PlayerActivity.this, MusicService.class);
		startService(intent);
		bindService(intent, serviceConnection, this.BIND_AUTO_CREATE);
	}
	
	private ServiceConnection serviceConnection = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name, IBinder service){
			musicService = ((MusicService.MyBinder) (service)).getService();
			 
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			musicService = null;
		}
		
	};
	
	public Handler handler = new Handler();
	public Runnable runnable = new Runnable() {
		@Override
		public void run(){
            //musicTime.setText(time.format(musicService.player.getCurrentPosition()));
            //musicTotal.setText(time.format(musicService.player.getDuration()));
            handler.postDelayed(runnable, 200);
		}
	};
   
	private void findViewById() {
        btnPlayOrPause = (Button) findViewById(R.id.BtnPlayorPause);
        ImageView imageView = (ImageView) findViewById(R.id.Image);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String[] PathList = getAllSdPaths(this);
		sd_path = PathList[1];
		System.out.println("yyy-the sdpath is hhh" + sd_path);
		setContentView(R.layout.activity_player);
		findViewById();
		bindServiceConnection();
		Player();
		Log.d("lkj","onCreate 0");
	}
	
	@Override
	public void onResume(){
		super.onResume();
		String[] PathList = getAllSdPaths(this);
		sd_path = PathList[1];
		System.out.println("yyy-the sd_path is hhh onResume" + sd_path);
		bindServiceConnection();
		Player();
	}
	
	private void Player(){
		ImageView imageView = (ImageView) findViewById(R.id.Image);
		 final ObjectAnimator animator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360.0f);
	     animator.setDuration(10000);
	     animator.setInterpolator(new LinearInterpolator());
	     animator.setRepeatCount(-1);
		btnPlayOrPause.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if (musicService.isplay != true) {
					btnPlayOrPause.setText("PAUSE");
					musicService.pause();
					musicService.isplay = true;

					if(tag1 == false){
						animator.start();
						tag1 = true;
					} else {
						animator.resume();
					}
				} else{
					btnPlayOrPause.setText("PLAY");
					musicService.pause();
					musicService.isplay = false;
				}
				if(tag2 == false) {
					handler.post(runnable);
					tag2 = true;
				}
			}

		});
	}

	 @Override
	    public boolean onKeyDown(int keyCode, KeyEvent event) {
		 AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		 switch(keyCode){
		 	case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
		 		System.out.println("yyy-the keyCode is " + keyCode);
		 		musicService.pause();
		 		break;
		 		
		 	case KeyEvent.KEYCODE_VOLUME_UP:
		 		System.out.println("yyy-the keyCode_up is " + keyCode);
		 		mAudioManager.adjustStreamVolume (AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,AudioManager.FX_FOCUS_NAVIGATION_UP);
		 		break;
		 		
		 	case KeyEvent.KEYCODE_VOLUME_DOWN:
		 		System.out.println("yyy-the keyCode_down is " + keyCode);
		 		mAudioManager.adjustStreamVolume (AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,AudioManager.FX_FOCUS_NAVIGATION_UP);
		 		break;
		 }
      
//	        return super.onKeyDown(keyCode, event);
		 return true;
	    }
	 
	 public static String[] getAllSdPaths(Context mContext) {

		 StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
		 //List<StorageVolume> volumes = mStorageManager.getStorageVolumes();
		 Class<?> storageVolumeClazz = null;
		try {


			storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
			Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
			Method getPath = storageVolumeClazz.getMethod("getPath");
			Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
			Method getDescription = storageVolumeClazz.getMethod("getDescription", Context.class);
			 Object result = getVolumeList.invoke(mStorageManager);
			
			final int length = Array.getLength(result);
			for (int i = 0; i <length; i++) {
				Object storageVolumeElement = Array.get(result, i);
				
				
				String path = (String) getPath.invoke(storageVolumeElement);

				 String description = (String) getDescription.invoke(storageVolumeElement, mContext);
				
				boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
				if (true == removable) {// 可拆卸设备
					if (description.contains("USB") || description.contains("U 盘")||description.contains("USB 存储器") ) {// usb外置卡
					//if(uchar == 'u' || uchar == 'U'){
						usb_path = path;
					}else if(description.endsWith("SD")||description.endsWith("SD 卡")){//sd卡可判断
						sd_path=path;
					}else{//其它sd卡不可通过SD、SD卡来判断识别
						sd_path=path;
					}
				} else {// 内置卡存储路径
					ext_path = path;
				}
			}
			String[] pathLists = new String[] { ext_path, sd_path, usb_path };
			return pathLists;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
}
