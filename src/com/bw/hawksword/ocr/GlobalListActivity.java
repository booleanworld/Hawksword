package com.bw.hawksword.ocr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.bw.hawksword.offlinedict.RealCode_Compress;
import com.bw.hawksword.parser.Token;
import com.bw.hawksword.parser.Tokenizer;
import com.bw.hawksword.wiktionary.LookupActivity;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class GlobalListActivity extends Activity {

	private TableLayout tbl_list;
	private LinearLayout linearLayout1;
	private FrameLayout frameLayout1;
	private Intent intent;
	private String action;
	private String type;
	private static RealCode_Compress r;
	private SharedPreferences prefs;
	private static ArrayList<String> preparedList;
	private static boolean flag;
	private ClipboardManager clipboard;
	private GoogleAnalyticsTracker tracker; //For Google Analytics

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.global_list);
		tbl_list = (TableLayout)findViewById(R.id.tableLayout1);
		linearLayout1 = (LinearLayout)findViewById(R.id.linearLayout1);
		frameLayout1 = (FrameLayout)findViewById(R.id.FrameLayout1);
		clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
		intent = getIntent();
		action = intent.getAction();
		type = intent.getType();
		tracker = GoogleAnalyticsTracker.getInstance(); //Initialise Google Analytics  
		tracker.startNewSession("UA-30209148-1", 10, this); //booleanworld
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if ("text/plain".equals(type)) {
				try {
					handleSendText(intent); // Handle text being sent
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // Handle text being sent
			}
		} 

		if(flag)
			generateList();
		else
			finish();
		//linearLayout1.getLayoutParams().height = (int)(getWindowManager().getDefaultDisplay().getHeight() * 90)/100;

		//For Voice Data
		//		Bundle b = getIntent().getExtras(); 
		//		if(b != null) {
		//			voiceText = b.getString("VoiceText");
		//		}
		//else if (captureText != null) {
		//			try {
		//				handleClipboardText(captureText);
		//			} catch (IOException e) {
		//				// TODO Auto-generated catch block
		//				e.printStackTrace();
		//			}
		//		} else if (voiceText != null) {
		//			try {
		//				handleClipboardText(voiceText);
		//			} catch (IOException e) {
		//				// TODO Auto-generated catch block
		//				e.printStackTrace();
		//			}
		//		} else if(clipboard.hasText()){ //Should be always at last
		//			try {
		//				handleClipboardText(clipboard.getText().toString());
		//			} catch (Exception e) {
		//			}
	}

	void handleSendText(Intent intent) throws IOException {
		String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
		if (sharedText != null) {
			isListProper(sharedText);
		}
	}

	//	void handleClipboardText(String clipedText) throws IOException {
	//		if (clipedText != null) {
	//			Tokenizer tokenizer = null;
	//			tokenizer = new Tokenizer("/mnt/sdcard/Android/data/com.bw.hawksword.ocr/files/mounted/tessdata/stop_words");
	//			HashMap<String, Token> tokens = tokenizer.tokenize(clipedText);
	//			if(!tokens.isEmpty()){
	//				generateList(tokens);
	//			}
	//		}
	//	}
	public static boolean isListProper(String rawText) {
		Tokenizer tokenizer = null;
		preparedList = new ArrayList<String>();
		r = new RealCode_Compress();
		flag = false;
		try {
			tokenizer = new Tokenizer("/mnt/sdcard/Android/data/com.bw.hawksword.ocr/files/mounted/tessdata/stop_words");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HashMap<String, Token> tokens = tokenizer.tokenize(rawText);
		if(!tokens.isEmpty()){
			for (String word : tokens.keySet()) {
				if(RealCode_Compress.spellSearch(word) || RealCode_Compress.spellSearch(word.toLowerCase())){
					flag = true;
					preparedList.add(word);
				}
			}
		}
		return flag;
	}
	private void generateList() {

		TextView t2;
		TableRow row;

		//Converting to dip unit
		int dip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,(float) 1, getResources().getDisplayMetrics());

		TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT);
		int leftMargin=1;
		int topMargin=0;
		int rightMargin=1;
		int bottomMargin=1;

		tableRowParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);


		//Generating List..
		for (String word : preparedList) {
			row = new TableRow(this);
			t2 = new TextView(this);
			t2.setTextColor(getResources().getColor(R.color.Black));    
			t2.setText(word);
			t2.setTypeface(null, 0);
			t2.setTextSize(18 * dip);
			t2.setGravity(17);
			t2.setWidth(150 * dip);
			//row.setPadding(1, 0, 1, 0);
			row.setMinimumHeight(75 * dip);
			row.setBackgroundColor(Color.WHITE);
			t2.setBackgroundDrawable(t2.getContext().getResources().getDrawable(R.drawable.grid_glow));
			row.addView(t2);

			TableRow.LayoutParams params =  (TableRow.LayoutParams)t2.getLayoutParams();
			params.width = TableRow.LayoutParams.MATCH_PARENT;
			params.height = TableRow.LayoutParams.MATCH_PARENT;
			t2.setLayoutParams(params);

			row.setClickable(true);
			row.setFocusable(true);
			row.setFocusableInTouchMode(true);
			tbl_list.addView(row, tableRowParams);
			t2.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					arg0.setSelected(true);
					String currentText = ((TextView)arg0).getText().toString();
					tracker.trackEvent( // Google Analytics 
							"Word Lookup",  // Category
							"From Camera",  // Action
							currentText, // Label
							1);  
					Intent  dict = new Intent(getBaseContext(),LookupActivity.class);
					String[] dictModes = getResources().getStringArray(R.array.capturemodes);
					final String dictMode = prefs.getString(PreferencesActivity.KEY_DICTIONARY_MODE, dictModes[0]);
					dict.putExtra("ST",currentText);
					dict.putExtra("Mode",dictMode);
					startActivity(dict);	
				}
			});
		}
		Animation slideUpList = AnimationUtils.loadAnimation(GlobalListActivity.this,R.anim.slide_up_list);
		Animation slideUpMenu = AnimationUtils.loadAnimation(GlobalListActivity.this,R.anim.slide_up_menu);
		linearLayout1.startAnimation(slideUpMenu);
		frameLayout1.startAnimation(slideUpList);
		frameLayout1.setVisibility(LinearLayout.VISIBLE);
	}
}
