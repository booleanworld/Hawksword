package com.bw.hawksword.ocr;

import android.app.Application;

public class HawkswordApplication extends Application {
	public WordData wordData;

	@Override
	public void onCreate() {
		super.onCreate();
		wordData = new WordData(this);
	}
	
	
}
