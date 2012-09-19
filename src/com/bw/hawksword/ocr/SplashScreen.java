package com.bw.hawksword.ocr;


import java.io.File;

import com.googlecode.tesseract.android.TessBaseAPI;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

public class SplashScreen extends Activity {

	protected int _splashTime = 2000; 
	private String TAG = "SplashScreen"; 
	private Thread splashTread;
	private ProgressBar progressBar;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		final SplashScreen sPlashScreen = this;
		// Start AsyncTask to install language data
		File storageDirectory = getStorageDirectory();
		MenuActivity.dataPath = getStorageDirectory().toString();
		progressBar = (ProgressBar)findViewById(R.id.progressBar1);
		new DataInitAsyncTask(this).execute(storageDirectory.toString());
		progressBar.setVisibility(View.VISIBLE);
		// thread for displaying the SplashScreen
		splashTread = new Thread() {
			@Override
			public void run() {
				try {	    
					synchronized(this){
						wait(_splashTime);
						while(!DataInitAsyncTask.status) {
							wait(2000);	
						}
					}
				} catch(InterruptedException e) {} 
				finally {
					finish();
					Intent i = new Intent();
					i.setClass(sPlashScreen, MenuActivity.class);
					startActivity(i);
				}
			}
		};
		splashTread.start();


	}
	/** Finds the proper location on the SD card where we can save files. */
	@SuppressLint("NewApi")
	private File getStorageDirectory() {
		//Log.d(TAG, "getStorageDirectory(): API level is " + Integer.valueOf(android.os.Build.VERSION.SDK_INT));

		String state = null;
		try {
			state = Environment.getExternalStorageState();
		} catch (RuntimeException e) {
			Log.e(TAG, "Is the SD card visible?", e);
			showErrorMessage("Error", "Required external storage (such as an SD card) is unavailable.");
		}

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

			// We can read and write the media
			//    	if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) > 7) {
			// For Android 2.2 and above

			try {
				return getExternalFilesDir(Environment.MEDIA_MOUNTED);
			} catch (NullPointerException e) {
				// We get an error here if the SD card is visible, but full
				Log.e(TAG, "External storage is unavailable");
				showErrorMessage("Error", "Required external storage (such as an SD card) is full or unavailable.");
			}


		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			Log.e(TAG, "External storage is read-only");
			showErrorMessage("Error", "Required external storage (such as an SD card) is unavailable for data storage.");
		} else {
			// Something else is wrong. It may be one of many other states, but all we need
			// to know is we can neither read nor write
			Log.e(TAG, "External storage is unavailable");
			showErrorMessage("Error", "Required external storage (such as an SD card) is unavailable.");
		}
		return null;
	}
	/**
	 * Displays an error message dialog box to the user on the UI thread.
	 * 
	 * @param title The title for the dialog box
	 * @param message The error message to be displayed
	 */
	void showErrorMessage(String title, String message) {
		new AlertDialog.Builder(this)
		.setTitle(title)
		.setMessage(message)
		.setOnCancelListener(new FinishListener(this))
		.setPositiveButton( "Done", new FinishListener(this))
		.show();
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			synchronized(splashTread){
				splashTread.notifyAll();
			}
		}
		return true;
	}


}
