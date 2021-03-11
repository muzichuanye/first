package com.android.drcc.musicplayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.storage.StorageManager;
import android.util.Log;

public class MusicService extends Service{
	
	private static final File MUSIC_PATH = Environment   
            .getExternalStorageDirectory(); //sd卡路径
	private static final String TAG = "MusicService";
	public static List<File> musicList; //音乐列表
	public MediaPlayer player;
	public boolean isplay;
	//MusicThread audioThread;
	HeadsetThread headsetThread;
	boolean isRun;
	boolean headsetReady;
	boolean musicReady;
	String dataSource;

	class HeadsetThread extends Thread{
		@Override
		public void run() {
//			super.run();
			while(true){

				if(player.isPlaying()) {
					headsetReady = false;
					player.stop();
				}
				try {
					sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	class MusicThread extends Thread{
		private String mPath;
		private String TAG = "MusicThread";
		private Process p2;

		MusicThread(String path){
			mPath = path;
		}
		public void kill(){
			if(p2!=null) {
				Log.e(TAG,"p2 destroy");
				p2.destroy();
			}
		}
		@Override
		public void run() {
			String[] cmd2 = new String[]{"/system/bin/tinyplay",PlayerActivity.sd_path+"/123.wav","-D","2"};
			Runtime runtime = Runtime.getRuntime();
			while(true) {
				while (musicReady) {
					try {
						Log.e(TAG, "cmd2");
						p2 = runtime.exec(cmd2);
						Log.e(TAG, "after cmd2");
						musicReady = true;

						BufferedReader errorBr2;
						InputStream errorIs2 = p2.getErrorStream();
						InputStreamReader errorisr2 = new InputStreamReader(errorIs2);
						errorBr2 = new BufferedReader(errorisr2);
						InputStream is2 = p2.getInputStream();
						BufferedReader inputBr2;
						InputStreamReader isr2 = new InputStreamReader(is2);
						inputBr2 = new BufferedReader(isr2);
						Log.e(TAG, "error2: " + errorBr2.readLine());
						Log.e(TAG, "out2: " + inputBr2.read());
					} catch (IOException e) {
						e.printStackTrace();
					}
					musicReady = false;
				}
				try {
					sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
		
	public MusicService() {

		isRun = false;
		headsetReady = false;
		musicReady = false;

		player = new MediaPlayer();
		
		System.out.println("yyy-the sd_path list is " + PlayerActivity.sd_path);
		musicList = FileUtil.queryfilevideo("mp3", PlayerActivity.sd_path);
		
		System.out.println("yyy-the music_path list is " + musicList);
		try {
			dataSource = musicList.get(0).getAbsolutePath();
			System.out.println("yyy-the datasource path is " + dataSource);
			player.setDataSource(dataSource);
			player.prepare();
//			player.setLooping(true);
			player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					headsetReady = false;
				}
			});
			/*audioThread = new MusicThread(dataSource);
			audioThread.start();*/
			/*headsetThread = new HeadsetThread();
			headsetThread.start();*/
		} catch (Exception e){
			e.printStackTrace();
		}
//		player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//			@Override
//			public void onCompletion(MediaPlayer mp) {
//				mp.release();
//			}
//		});
	}
	
//  通过 Binder 来保持 Activity 和 Service 的通信
    public MyBinder binder = new MyBinder();
    public class MyBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }
	
	public void pause(){
		Log.d(TAG, "3, isRun: " + isRun + ", player.isPlaying():" + player.isPlaying());
    	if(!isRun){
			isRun = true;
    		playOnce();
		} else {
    		try {
				if(player.isPlaying()){
					player.pause();
				} else {
					player.start();
				}
    		}catch (IllegalStateException e){
				Log.e(TAG, "IllegalStateException: " + e);
				isRun = false;
			}catch (Exception e){
				Log.e(TAG, "Exception: " + e);
			}
		}
	}


	private void playOnce(){
		try {
			AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
			Log.d(TAG, "cur index=" + mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
			Log.d(TAG, "max index=" + mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 11, AudioManager.FLAG_SHOW_UI);

			player.reset();
			player.setDataSource(dataSource);//重新设置要播放的音频
			player.prepare();//预加载音频
			player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					isRun = false;
				}
			});
			player.setLooping(false);
			player.start();//开始播放
//			hint.setText("正在播放音频.....");
		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.d(TAG, "playOnce");

	}
	
	@Override
	public IBinder onBind(Intent intent){
		return binder;
	}
	
	public static String getSdPath(Context mContext) {

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
						return null;
					}else if(description.endsWith("SD")||description.endsWith("SD 卡")){//sd卡可判断
						return path;
					}else{//其它sd卡不可通过SD、SD卡来判断识别
						return path;
					}
				} else {// 内置卡存储路径
					return null;
				}
			}
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

