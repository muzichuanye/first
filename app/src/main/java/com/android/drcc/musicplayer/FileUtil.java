package com.android.drcc.musicplayer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
//import android.app.ProgressDialog;
import android.os.Environment;




public class FileUtil {
	public static final List<File> mlist=new ArrayList<File>();

	public static final List<File> traverseFile(File dir,List<File> mlist){
		File[] files=dir.listFiles();
		if(files==null){
			return mlist;
		}
		for (File file:files) {
			mlist.add(file);
			if(file.isDirectory()){
				traverseFile(file, mlist);
			}
		}
				return mlist;
	}
	
	
	public static final void isNull(){
		if(mlist.size()==0){
			System.out.println();
			traverseFile(Environment.getExternalStorageDirectory(), mlist);
		}
	}

	
	public static final List<File> queryfilevideo(String types,String sdpath){
		List<File> list=new ArrayList<File>();
		if(sdpath != null){
			File f=new File(sdpath);
		
		if(mlist!=null){
			mlist.clear();
		}
		
		if(f.exists()){
			traverseFile(f, mlist);
			System.out.println("=============cunzai=");
		}else{
			System.out.println("=============bucunzai=");
		}
		System.out.println("=============mlist="+mlist.size());
		//初始化
		}
		
			list.clear();
		
		//遍历sd卡的所有文件
		for(File file:mlist){
			if(file.isFile()){
				String filename=file.getName();
				//拿到当前文件的后缀
				if(filename.lastIndexOf(".")==-1){
					continue;
				}
				String type=filename.substring(filename.lastIndexOf("."));

				//是不是需要匹配的后缀
				String[] queryType=types.split("-");
					for (String string : queryType) {
						//匹配其中一个类型
					//	System.out.println("=============mliststring="+string);
						if(type.contains(string)){
					//	System.out.println("=============mlist999");
							System.out.println("yyy-the copy file de name is " + file.getPath()+" /"+file.getName());

							list.add(file);	
						}
					}
			}
		}

		return list;
	}
	
	


}









