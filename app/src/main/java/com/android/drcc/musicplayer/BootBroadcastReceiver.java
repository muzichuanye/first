package com.android.drcc.musicplayer;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class BootBroadcastReceiver extends BroadcastReceiver {

	
	private String mountedaction = "android.intent.action.MEDIA_MOUNTED";
	public static String path;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if(action.equals(mountedaction)){
        	path = intent.getData().getPath();
        	System.out.println("yyy-the Broadcast path is " + path);
        	
        	System.out.println("yyy-the PlayerActivity.sd_path is " + PlayerActivity.sd_path);
        	Intent intent1 = new Intent(context, PlayerActivity.class);  
    		intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	    if(FileUtil.queryfilevideo("mp3",path).size() != 0){
    	    	System.out.println("yyy-the initial mlist size is "+ FileUtil.mlist.size());
    	    	 	context.startActivity(intent1);
    	    }
        }		
	}  
}