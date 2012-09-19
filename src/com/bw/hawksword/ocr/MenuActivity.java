package com.bw.hawksword.ocr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.bw.hawksword.offlinedict.RealCode_Compress;
import com.bw.hawksword.parser.Token;
import com.bw.hawksword.wiktionary.LookupActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.text.ClipboardManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MenuActivity extends Activity {
	TableLayout tbl_list;
	FrameLayout menu_frame;
	WindowManager mWinMgr;
	ClipboardManager clip_board;
	private static final int REQUEST_CODE = 1234;
	private SharedPreferences prefs;
	RealCode_Compress r;
	
	// Context menu
	private static final int SETTINGS_ID = Menu.FIRST;
	private static final int HISTORY_ID = Menu.FIRST + 1;
	private static final int ABOUT_ID = Menu.FIRST + 2;
	private static final int HELP_ID = Menu.FIRST + 3;
	
	private static boolean isFirstLaunch;
	private AlertDialog.Builder FB;
	private AlertDialog.Builder TW;
	private AlertDialog.Builder RN;
	private static final String TAG = MenuActivity.class.getSimpleName();
	public static String dataPath;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		checkFirstLaunch();
		if (isFirstLaunch) {
			setDefaultPreferences();
		}
		setContentView(R.layout.menuactivity);
		final String action = getIntent().getAction();
		final Intent  list = new Intent(getBaseContext(),LookupActivity.class);
		clip_board = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		r = new RealCode_Compress();
		if (Intent.ACTION_SEARCH.equals(action)) {
			// Start query for incoming search request
			Intent  dict = new Intent(getBaseContext(),LookupActivity.class);
			dict.putExtra("ST",getIntent().getStringExtra(SearchManager.QUERY).trim());
			startActivity(dict);
			finish();
		}
		prepareView();
		FB = new AlertDialog.Builder(this);
		// set the message to display
		FB.setMessage("Would you like to \"Like\" our Facebook Page?");
		// set a positive/yes button and create a listener                    
		FB.setPositiveButton("Like", new DialogInterface.OnClickListener() {
			// do something when the button is clicked
			public void onClick(DialogInterface arg0, int arg1) {
				String url ="http://www.facebook.com/hawkswordbybooleanworld/";
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
			}
		});
		//For Tweeter
		// prepare the alert box                   
		TW = new AlertDialog.Builder(this);
		// set the message to display
		TW.setMessage("To stay updated, follow us on Tweeter");
		// set a positive/yes button and create a listener                    
		TW.setPositiveButton("Follow", new DialogInterface.OnClickListener() {
			// do something when the button is clicked
			public void onClick(DialogInterface arg0, int arg1) {
				String url ="http://www.twitter.com/hawksword_app/";
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
			}
		});
		//For Rating
		// prepare the alert box                   
		RN = new AlertDialog.Builder(this);
		// set the message to display
		RN.setMessage("Would you like to rate us?");
		// set a positive/yes button and create a listener                    
		RN.setPositiveButton("Rate", new DialogInterface.OnClickListener() {
			// do something when the button is clicked
			public void onClick(DialogInterface arg0, int arg1) {
				String url ="http://www.goo.gl/SL8yY";
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));

			}
		});

		createFileForCount();
		updateCount();

	}

	public void prepareView() {
		tbl_list = (TableLayout) findViewById(R.id.tableLayout1);
		menu_frame = (FrameLayout)findViewById(R.id.menuframe);

		mWinMgr = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		int displayWidth = ( mWinMgr.getDefaultDisplay().getWidth() / 4 ) * 2 + 1;
		int displayHeight = ( mWinMgr.getDefaultDisplay().getHeight() / 4 ) * 2 + 1;

		menu_frame.getLayoutParams().height = menu_frame.getLayoutParams().width =  Math.min(displayHeight,displayWidth); 

		//Row-1
		TableRow row1 = new TableRow(this);
		row1.setBackgroundColor(android.R.color.transparent);

		//Cell-1
		ImageView t1 = new ImageView(this);         
		t1.setImageResource(R.drawable.menu_button_1);
		t1.setClickable(true);
		t1.setBackgroundColor(Color.WHITE);
		t1.setScaleType(ImageView.ScaleType.CENTER);
		t1.setBackgroundDrawable(t1.getContext().getResources().getDrawable(R.drawable.grid_glow));
		row1.addView(t1); //Attach TextView to its parent (row)
		
		TableRow.LayoutParams params1_img =  (TableRow.LayoutParams)t1.getLayoutParams();
		params1_img.width = TableRow.LayoutParams.MATCH_PARENT;
		params1_img.height = TableRow.LayoutParams.MATCH_PARENT;
		params1_img.column= 0; //place at ith columns.
		params1_img.span = 1; //span these many columns, 
		params1_img.setMargins(0,0,1,1); //To "draw" margins
		t1.setLayoutParams(params1_img);
		
		t1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final Intent  camera = new Intent(getBaseContext(),CaptureActivity.class);
				startActivity(camera);
			}
		});

		//Cell-2    
		ImageView t2 = new ImageView(this);       
		t2.setImageResource(R.drawable.menu_button_2);
		t2.setBackgroundColor(R.drawable.menu_box);
		t2.setClickable(true);
		t2.setScaleType(ImageView.ScaleType.CENTER);
		t2.setBackgroundColor(Color.WHITE);
		t2.setBackgroundDrawable(t2.getContext().getResources().getDrawable(R.drawable.grid_glow));
		row1.addView(t2); //Attach TextView to its parent (row)
		TableRow.LayoutParams params2_img =  (TableRow.LayoutParams)t2.getLayoutParams();
		params2_img.width = TableRow.LayoutParams.MATCH_PARENT;
		params2_img.height = TableRow.LayoutParams.MATCH_PARENT;
		params2_img.setMargins(0,0,0,1); //To "draw" margins
		params2_img.column = 1; //place at ith columns.
		params2_img.span = 1; //span these many columns, 
		t2.setLayoutParams(params2_img);
		t2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startVoiceRecognitionActivity();
			}
		});
		row1.setMinimumHeight((menu_frame.getLayoutParams().width / 2) + 1);
		tbl_list.addView(row1,new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.MATCH_PARENT));

		//Row-2
		TableRow row2 = new TableRow(this);
		row2.setBackgroundColor(android.R.color.transparent);

		//Cell-3
		ImageView t3 = new ImageView(this);       
		t3.setImageResource(R.drawable.menu_button_3);
		t3.setClickable(true);
		t3.setScaleType(ImageView.ScaleType.CENTER);
		t3.setBackgroundColor(Color.WHITE);
		t3.setBackgroundDrawable(t3.getContext().getResources().getDrawable(R.drawable.grid_glow));
		row2.addView(t3); //Attach TextView to its parent (row)
		TableRow.LayoutParams params3_img =  (TableRow.LayoutParams)t3.getLayoutParams();
		params3_img.width = TableRow.LayoutParams.MATCH_PARENT;
		params3_img.height = TableRow.LayoutParams.MATCH_PARENT;
		params3_img.column = 0;
		params3_img.span = 1;
		params3_img.setMargins(0,0,1,0); //To "draw" margins
		t3.setLayoutParams(params3_img);
		t3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onSearchRequested();
			}
		});

		//Cell-4
		ImageView t4 = new ImageView(this);       	
		t4.setImageResource(R.drawable.menu_button_4);
		t4.setClickable(true);
		t4.setFocusable(true);
		t4.setScaleType(ImageView.ScaleType.CENTER);
		t4.setBackgroundColor(Color.WHITE);
		row2.addView(t4); //Attach TextView to its parent (row)
		t4.setBackgroundDrawable(t4.getContext().getResources().getDrawable(R.drawable.grid_glow));
		TableRow.LayoutParams params4_img =  (TableRow.LayoutParams)t4.getLayoutParams();
		params4_img.width = TableRow.LayoutParams.MATCH_PARENT;
		params4_img.height = TableRow.LayoutParams.MATCH_PARENT;
		params4_img.setMargins(0,0,0,0); //To "draw" margins
		params4_img.column = 1;
		params4_img.span = 1;
		t4.setLayoutParams(params4_img);
		t4.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(clip_board.hasText()) {
					final Intent  list = new Intent(getBaseContext(),GlobalListActivity.class);
					if(GlobalListActivity.isListProper(clip_board.getText().toString())) {
						startActivity(list);
					} else {
						Toast.makeText(MenuActivity.this, "There is no text in clipboard", Toast.LENGTH_LONG).show();
					}
				}
				else {
					Toast.makeText(MenuActivity.this, "There is nothing in clipboard", Toast.LENGTH_LONG).show();
				}
			}
		});
		row2.setMinimumHeight((menu_frame.getLayoutParams().width / 2));
		tbl_list.addView(row2,new TableLayout.LayoutParams(TableRow.LayoutParams.FILL_PARENT,TableRow.LayoutParams.FILL_PARENT));
	}
	/**
	 * Fire an intent to start the voice recognition activity.
	 */
	private void startVoiceRecognitionActivity()
	{
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hawksword");
		startActivityForResult(intent, REQUEST_CODE);
	}
	/**
	 * Handle the results from the voice recognition activity.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK)
		{
			// Populate the wordsList with the String values the recognition engine thought it heard
			ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			final Intent  list = new Intent(getBaseContext(),GlobalListActivity.class);
			if(GlobalListActivity.isListProper(matches.toString())) {
				startActivity(list);
			} else {
				Toast toast = Toast.makeText(this, "Result not found.", Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.BOTTOM, 0, 0);
				toast.show();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		//    MenuInflater inflater = getMenuInflater();
		//    inflater.inflate(R.menu.options_menu, menu);
		super.onCreateOptionsMenu(menu);
		menu.add(0, SETTINGS_ID, 0, "Settings").setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(1, HISTORY_ID, 1, "History").setIcon(android.R.drawable.ic_menu_recent_history);
		menu.add(2, ABOUT_ID, 2, "About").setIcon(android.R.drawable.ic_menu_info_details);
		menu.add(3, HELP_ID, 3, "Help").setIcon(android.R.drawable.ic_menu_help);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case SETTINGS_ID: {
			intent = new Intent().setClass(this, PreferencesActivity.class);
			startActivity(intent);
			break;
		}
		case ABOUT_ID: {
			intent = new Intent(this, HelpActivity.class);
			intent.putExtra(HelpActivity.REQUESTED_PAGE_KEY, HelpActivity.ABOUT_PAGE);
			startActivity(intent);
			break;
		}
		case HELP_ID: {
			intent = new Intent(this, HelpActivity.class);
			intent.putExtra(HelpActivity.REQUESTED_PAGE_KEY, HelpActivity.HELP_PAGE);
			startActivity(intent);
			break;
		}
		case HISTORY_ID: {
			intent = new Intent(this, WordhistoryActivity.class);
			startActivity(intent);
			break;
		}
		}
		return super.onOptionsItemSelected(item);
	}
	/**
	 * We want the help screen to be shown automatically the first time a new version of the app is
	 * run. The easiest way to do this is to check android:versionCode from the manifest, and compare
	 * it to a value stored as a preference.
	 */
	private boolean checkFirstLaunch() {
		try {
			Log.d("Hawksword","Checking for First Launch........");
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
			int currentVersion = info.versionCode;
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			int lastVersion = prefs.getInt(PreferencesActivity.KEY_HELP_VERSION_SHOWN, 0);
			if (lastVersion == 0) {
				isFirstLaunch = true;
				Log.d("Hawksword","New Version");
			} else {
				isFirstLaunch = false;
				Log.d("Hawksword","Old Version");
			}
			if (currentVersion > lastVersion) {

				// Record the last version for which we last displayed the What's New (Help) page
				prefs.edit().putInt(PreferencesActivity.KEY_HELP_VERSION_SHOWN, currentVersion).commit();
				Intent intent = new Intent(this, HelpActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				// Show the default page on a clean install, and the what's new page on an upgrade.
				String page = lastVersion == 0 ? HelpActivity.DEFAULT_PAGE : HelpActivity.WHATS_NEW_PAGE;
				intent.putExtra(HelpActivity.REQUESTED_PAGE_KEY, page);
				startActivity(intent);
				return true;
			}
		} catch (PackageManager.NameNotFoundException e) {
			Log.w(TAG, e);
		}
		return false;
	}
	
	/**
	 * Sets default values for preferences. To be called the first time this app is run.
	 */
	private void setDefaultPreferences() {
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		//Focus Mode
		prefs.edit().putString(PreferencesActivity.KEY_FOCUS_MODE, CaptureActivity.DEFAULT_FOCUS_MODE).commit();

		// Dictionary Mode
		prefs.edit().putString(PreferencesActivity.KEY_DICTIONARY_MODE, CaptureActivity.DEFAULT_DICTIONARY_MODE).commit();

		// Recognition language
		prefs.edit().putString(PreferencesActivity.KEY_SOURCE_LANGUAGE_PREFERENCE, CaptureActivity.DEFAULT_SOURCE_LANGUAGE_CODE).commit();

		// Translation
		prefs.edit().putBoolean(PreferencesActivity.KEY_TOGGLE_TRANSLATION, CaptureActivity.DEFAULT_TOGGLE_TRANSLATION).commit();

		// Translator
		prefs.edit().putString(PreferencesActivity.KEY_TRANSLATOR, CaptureActivity.DEFAULT_TRANSLATOR).commit();

		// OCR Engine
		prefs.edit().putString(PreferencesActivity.KEY_OCR_ENGINE_MODE, CaptureActivity.DEFAULT_OCR_ENGINE_MODE).commit();

		// Beep
		prefs.edit().putBoolean(PreferencesActivity.KEY_PLAY_BEEP, CaptureActivity.DEFAULT_TOGGLE_BEEP).commit();

		// Character blacklist
		prefs.edit().putString(PreferencesActivity.KEY_CHARACTER_BLACKLIST, 
				OcrCharacterHelper.getDefaultBlacklist(CaptureActivity.DEFAULT_SOURCE_LANGUAGE_CODE)).commit();

		// Character whitelist
		prefs.edit().putString(PreferencesActivity.KEY_CHARACTER_WHITELIST, 
				OcrCharacterHelper.getDefaultWhitelist(CaptureActivity.DEFAULT_SOURCE_LANGUAGE_CODE)).commit();

		// Page segmentation mode
		prefs.edit().putString(PreferencesActivity.KEY_PAGE_SEGMENTATION_MODE, CaptureActivity.DEFAULT_PAGE_SEGMENTATION_MODE).commit();

		// Reversed camera image
		prefs.edit().putBoolean(PreferencesActivity.KEY_REVERSE_IMAGE, CaptureActivity.DEFAULT_TOGGLE_REVERSED_IMAGE).commit();

		// Light
		prefs.edit().putBoolean(PreferencesActivity.KEY_TOGGLE_LIGHT, CaptureActivity.DEFAULT_TOGGLE_LIGHT).commit();
	}
	public void createFileForCount() {
		try {

			File file = new File(dataPath+ File.separator + "tessdata" + File.separator + "Count");

			if (!file.exists()) {
				file.createNewFile();
				FileOutputStream fos = new FileOutputStream(file);
				DataOutputStream dos = new DataOutputStream(fos);
				int a = 0;
				dos.write(a);
				dos.close();
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void updateCount() {
		try {
			int count = 0;
			FileInputStream fis = new FileInputStream(dataPath+ File.separator + "tessdata" + File.separator + "Count");
			DataInputStream dis = new DataInputStream(fis);
			try {
				count = dis.read();
				System.out.println(count);
			} catch (EOFException e) {
			}
			dis.close();
			if(count == 5) {
				// Rate Application
				RN.show();
			}
			if(count == 10) {
				// Facebook
				FB.show();
			}
			if(count == 15) {
				// Tweeter
				TW.show();
			}
			count++;
			FileOutputStream fos = new FileOutputStream(dataPath+ File.separator + "tessdata" + File.separator + "Count");
			DataOutputStream dos = new DataOutputStream(fos);
			dos.write(count);
			dos.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
}
}
