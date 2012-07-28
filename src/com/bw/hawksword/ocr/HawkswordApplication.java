package com.bw.hawksword.ocr;

import android.app.Application;

public class HawkswordApplication extends Application {
	public DataAdaptor wordData;

	@Override
	public void onCreate() {
		super.onCreate();
		wordData = new DataAdaptor(this);
	}
}
