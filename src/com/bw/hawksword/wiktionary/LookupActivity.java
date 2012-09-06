/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bw.hawksword.wiktionary;


import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.Stack;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bw.hawksword.ocr.CaptureActivity;
import com.bw.hawksword.ocr.HawkswordApplication;
import com.bw.hawksword.ocr.HelpActivity;
import com.bw.hawksword.ocr.PreferencesActivity;
import com.bw.hawksword.ocr.R;
import com.bw.hawksword.ocr.DataAdaptor;
import com.bw.hawksword.ocr.WordhistoryActivity;
import com.bw.hawksword.wiktionary.SimpleWikiHelper.ApiException;
import com.bw.hawksword.wiktionary.SimpleWikiHelper.ParseException;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

/**
 * Activity that lets users browse through Wiktionary content. This is just the
 * user interface, and all API communication and parsing is handled in
 * {@link ExtendedWikiHelper}.
 */
public class LookupActivity extends Activity implements AnimationListener, OnInitListener {
	private static final String TAG = "LookupActivity";

	private View mTitleBar;
	private TextView mTitle;
	private ProgressBar mProgress;
	private WebView mWebView;

	// Context menu
	private static final int SETTINGS_ID = Menu.FIRST;
	private static final int HISTORY_ID = Menu.FIRST + 1;
	private static final int ABOUT_ID = Menu.FIRST + 2;
	private static final int HELP_ID = Menu.FIRST + 3;


	private Animation mSlideIn;
	private Animation mSlideOut;
	private String query;
	private String mode;
	private String path;
	private String[] list;
	private DataAdaptor wordData;
	private static String DictMeaning;

	//Buttons
	private ImageView btn_fav;
	private ImageView btn_tts;
	private ImageView btn_share;

	//TTS
	private TextToSpeech mTts;
	private LookupActivity  this_obj = this;
	private String[] ttsList = {"com.svox.pico",
	"com.google.android.tts"};
	private boolean ttsStatus = false;

	//Preset Messages
	private Random rNumber = new Random();
	private String[] msg = new String[7];
	//			"Found the meaning of \"" + query + "\" via #Hawksword Android app http://goo.gl/SL8yY",
	//			"Discovered what \"" + query + "\" means via #Hawksword Android app http://goo.gl/SL8yY",
	//			"Just used #Hawksword and learnt \"" + query + "\" http://goo.gl/SL8yY",
	//			"Word quiz for you. What does \"" + query + "\" mean ? By #Hawksword Android app http://goo.gl/SL8yY",
	//			"Can you guess the meaning of \"" + query + "\"? I just got it via #Hawksword Android app http://goo.gl/SL8yY",
	//			"It's awesome to use #Hawksword. Just learnt the meaning of \"" + query + "\" . Android app at http://goo.gl/SL8yY",
	//			"Did you tried #Hawksword ? I just tried for the word \"" + query + "\" . Android app http://goo.gl/SL8yY" };

	/**
	 * History stack of previous words browsed in this session. This is
	 * referenced when the user taps the "back" key, to possibly intercept and
	 * show the last-visited entry, instead of closing the activity.
	 */
	private Stack<String> mHistory = new Stack<String>();

	private String mEntryTitle;

	/**
	 * Keep track of last time user tapped "back" hard key. When pressed more
	 * than once within {@link #BACK_THRESHOLD}, we treat let the back key fall
	 * through and close the app.
	 */
	private long mLastPress = -1;

	private static final long BACK_THRESHOLD = DateUtils.SECOND_IN_MILLIS / 2;
	public void updateMsg(String query){

		msg[0]	=	"Found the meaning of \"" + query + "\" via #Hawksword Android app http://goo.gl/SL8yY";
		msg[1]	=	"Discovered what \"" + query + "\" means via #Hawksword Android app http://goo.gl/SL8yY";
		msg[2]	=	"Just used #Hawksword and learnt \"" + query + "\" http://goo.gl/SL8yY";
		msg[3]	=	"Word quiz for you. What does \"" + query + "\" mean ? By #Hawksword Android app http://goo.gl/SL8yY";
		msg[4]	=	"Can you guess the meaning of \"" + query + "\"? I just got it via #Hawksword Android app http://goo.gl/SL8yY";
		msg[5]	=	"It's awesome to use #Hawksword. Just learnt the meaning of \"" + query + "\" . Android app at http://goo.gl/SL8yY";
		msg[6]	=	"Did you tried #Hawksword ? I just tried for the word \"" + query + "\" . Android app http://goo.gl/SL8yY";
	}
	
    // Google Analytics Tracker
    private GoogleAnalyticsTracker tracker;
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lookup);

		// Load animations used to show/hide progress bar
		mSlideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in);
		mSlideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out);

		// Listen for the "in" animation so we make the progress bar visible
		// only after the sliding has finished.
		mSlideIn.setAnimationListener(this);
		mTitleBar = findViewById(R.id.title_bar);
		mTitle = (TextView) findViewById(R.id.title);
		mProgress = (ProgressBar) findViewById(R.id.progress);
		mWebView = (WebView) findViewById(R.id.webview);
        tracker = GoogleAnalyticsTracker.getInstance(); //Initialise Google Analytics  
        tracker.startNewSession("UA-34513206-1", 10, this); //booleanworld

		Bundle b = getIntent().getExtras(); 
		query = b.getString("ST"); 
		mode = b.getString("Mode");
		path = b.getString("Path");

		wordData = ((HawkswordApplication)getApplication()).wordData;

		// Make the view transparent to show background
		mWebView.setBackgroundColor(0);
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// Insert your code here
				String[] arr = url.split("/");
				query = arr[3];
				tracker.trackEvent( // Google Analytics 
	    	            "Word Lookup",  // Category
	    	            "From Dictionary Forwarding",  // Action
	    	            query, // Label
	    	            1);       // Value
				onNewIntent(query);
				return true;
			}
		});
		//Favourite words
		btn_fav = (ImageView)findViewById(R.id.star);
		btn_fav.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!wordData.lookUpHistory(query,"1")){
					wordData.insertFavourite(query, new Date(), 1);
					tracker.trackEvent( // Google Analytics 
		    	            "Favorite Word",  // Category
		    	            "Adding",  // Action
		    	            query, // Label
		    	            1);       // Value
					btn_fav.setImageResource(R.drawable.favorite);
					Toast.makeText(this_obj,"Word is added to Favourite List", Toast.LENGTH_SHORT).show();
				}
				else{
					Toast.makeText(this_obj,"Word is already in Favourite List", Toast.LENGTH_SHORT).show();
				}
			}

		});
		//TTS Button Listener
		btn_tts = (ImageView)findViewById(R.id.tts);
		btn_tts.setOnClickListener(new ImageButton.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				tracker.trackEvent( // Google Analytics 
	    	            "Text To Speech",  // Category
	    	            "Adding",  // Action
	    	            query, // Label
	    	            1);       // Value
				mTts = new TextToSpeech(this_obj,this_obj);
			}

		});
		//Sharing Button
		btn_share = (ImageView)findViewById(R.id.btn_share);
		btn_share.setOnClickListener(new ImageButton.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent sharingIntent = new Intent(Intent.ACTION_SEND);
				sharingIntent.setType("text/plain");
				updateMsg(query);
				sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, msg[Math.abs(rNumber.nextInt() % 7)] );
				sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Hawksword Dictionary Word");
				tracker.trackEvent( // Google Analytics 
	    	            "Sharing",  // Category
	    	            "Sharing",  // Action
	    	            query, // Label
	    	            1);       // Value
				startActivity(Intent.createChooser(sharingIntent, "Share with"));

			}

		});
		/* Checking for word that is already in History list, If it is then display message 
		 */
		//		if(wordData.lookUpHistory(query,"0")){
		//			Toast.makeText(this_obj,"You have already searched this word before", Toast.LENGTH_SHORT).show();
		//		}
		// Prepare User-Agent string for wiki actions
		ExtendedWikiHelper.prepareUserAgent(this);

		// Handle incoming intents as possible searches or links
		if(mode.equals("Offline"))
		{
			Log.d("This",query);
			onNewIntent(query);
		}
		else if(mode.equals("Online")){
			onNewIntent(query);
		}
		else
		{
			//Fail....
		}
		// onSearchRequested();
	}
	@Override
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
	// TTS Method
	public void onInit(int arg0) {
		// TODO Auto-generated method stub
		for(int i=0;i<ttsList.length;i++) {
			if(mTts.getDefaultEngine().equalsIgnoreCase(ttsList[i])){
				ttsStatus = true;
				break;
			}
		}

		if(arg0 == TextToSpeech.SUCCESS && ttsStatus == true ){ 
			mTts.setPitch(1); //change it as per your need
			mTts.setSpeechRate(1);
			mTts.setLanguage(Locale.ENGLISH);
			ttsStatus = false;
			mTts.speak(query,TextToSpeech.QUEUE_FLUSH,null);
		}
		else {
			Toast.makeText(this, "Please enable Google TTS from Setting->Voice input", Toast.LENGTH_LONG).show();
		}

	}
	
	@Override
	protected void onDestroy() {
		//Close the Text to Speech Library
	    if(mTts != null) {

	        mTts.stop();
	        mTts.shutdown();
	        Log.d(TAG, "TTS Destroyed");
	    }
	    super.onDestroy();

	    }//end onDestroy()

	/**
	 * Intercept the back-key to try walking backwards along our word history
	 * stack. If we don't have any remaining history, the key behaves normally
	 * and closes this activity.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Handle back key as long we have a history stack
		if (keyCode == KeyEvent.KEYCODE_BACK && !mHistory.empty()) {

			// Compare against last pressed time, and if user hit multiple times
			// in quick succession, we should consider bailing out early.
			long currentPress = SystemClock.uptimeMillis();
			if (currentPress - mLastPress < BACK_THRESHOLD) {
				return super.onKeyDown(keyCode, event);
			}
			mLastPress = currentPress;

			// Pop last entry off stack and start loading
			query = mHistory.pop();
			startNavigating(query, false);

			return true;
		}

		// Otherwise fall through to parent
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * Start navigating to the given word, pushing any current word onto the
	 * history stack if requested. The navigation happens on a background thread
	 * and updates the GUI when finished.
	 *
	 * @param word The dictionary word to navigate to.
	 * @param pushHistory If true, push the current word onto history stack.
	 */
	private void startNavigating(String word, boolean pushHistory) {
		// Push any current word onto the history stack
		if (!TextUtils.isEmpty(mEntryTitle) && pushHistory) {
			mHistory.add(mEntryTitle);
		}
		/* Checking for word that is already in Favourite list, If it is then change 
		 * the button image
		 */
		if(wordData.lookUpHistory(query,"1")){
			btn_fav.setImageResource(R.drawable.favorite);
		}
		else{
			btn_fav.setImageResource(R.drawable.notfavorite);
		}
		if(!wordData.lookUpHistory(word,"0")){
			wordData = ((HawkswordApplication)getApplication()).wordData;
			wordData.insertHistory(word, new Date(), 0);
		}

		// Start lookup for new word in background
		new LookupTask().execute(word);
	}

	/**
	 * {@inheritDoc}
	 */
	/*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.lookup, menu);
        return true;
    }
	 */
	/**
	 * {@inheritDoc}
	 */
	/*  @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.lookup_search: {
                onSearchRequested();
                return true;
            }
            case R.id.lookup_random: {
                startNavigating(null, true);
                return true;
            }
            case R.id.lookup_about: {
                showAbout();
                return true;
            }
        }
        return false;
    }
	 */
	/**
	 * Show an about dialog that cites data sources.
	 */
	/* protected void showAbout() {
        // Inflate the about message contents
        View messageView = getLayoutInflater().inflate(R.layout.about, null, false);

        // When linking text, force to always use default color. This works
        // around a pressed color state bug.
        TextView textView = (TextView) messageView.findViewById(R.id.about_credits);
        int defaultColor = textView.getTextColors().getDefaultColor();
        textView.setTextColor(defaultColor);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.app_icon);
        builder.setTitle(R.string.app_name);
        builder.setView(messageView);
        builder.create();
        builder.show();
    }
	 */

	/**
	 * Because we're singleTop, we handle our own new intents. These usually
	 * come from the {@link SearchManager} when a search is requested, or from
	 * internal links the user clicks on.
	 */
	public void onNewIntent(String query) {
		startNavigating(query, true);
	}

	/**
	 * Set the title for the current entry.
	 */
	protected void setEntryTitle(String entryText) {
		mEntryTitle = entryText;
		mTitle.setText(mEntryTitle);
	}

	/**
	 * Set the content for the current entry. This will update our
	 * {@link WebView} to show the requested content.
	 */
	protected void setEntryContent(String entryContent) {
		mWebView.loadDataWithBaseURL(ExtendedWikiHelper.WIKI_AUTHORITY, entryContent,
				ExtendedWikiHelper.MIME_TYPE, ExtendedWikiHelper.ENCODING, null);
	}
	/* 
	 * HTML tag parser
	 */
	static String removeHTMLTags(String input) {
		StringBuffer in = new StringBuffer(input);
		int i=0;
		int tagStrart =0, tagEnd=0;
		boolean tag = false;
		/* this loop will run over whole input string */
		for (i=0; i<in.length(); i++) {
			if (!(tag)) {
				if (in.charAt(i) == '<') {
					tagStrart = i;
					tag = true;
				}
			} else {
				if (in.charAt(i) == '>') {
					tagEnd = i + 1;
					if(in.substring(tagStrart, tagEnd).contentEquals("<BR>"))
						in.replace(tagStrart, tagEnd, "\n");
					else
						in.replace(tagStrart, tagEnd, "");
					i = i - (tagEnd - tagStrart);	//adjust 'i' based on the replacement
					tag = false;
				}
			} 
		}
		return in.toString();
	}
	/**
	 * Background task to handle Wiktionary lookups. This correctly shows and
	 * hides the loading animation from the GUI thread before starting a
	 * background query to the Wiktionary API. When finished, it transitions
	 * back to the GUI thread where it updates with the newly-found entry.
	 */
	private class LookupTask extends AsyncTask<String, String, String> {
		/**
		 * Before jumping into background thread, start sliding in the
		 * {@link ProgressBar}. We'll only show it once the animation finishes.
		 */
		@Override
		protected void onPreExecute() {
			mTitleBar.startAnimation(mSlideIn);
		}

		/**
		 * Perform the background query using {@link ExtendedWikiHelper}, which
		 * may return an error message as the result.
		 */
		@Override
		protected String doInBackground(String... args) {
			String query = args[0];
			String parsedText = null;

			try {
				// If query word is null, assume request for random word
				/*    if (query == null) {
                    query = ExtendedWikiHelper.getRandomWord();
                }
				 */
				if (query != null) {
					// Push our requested word to the title bar
					publishProgress(query);

					if(mode.equals("Online")){
						String wikiText = ExtendedWikiHelper.getPageContent(query, true);
						parsedText = ExtendedWikiHelper.formatWikiText(wikiText);
					}
					else if(mode.equals("Offline")){
						parsedText = "";
						parsedText = CaptureActivity.r.search(query);
					}

				}
			} catch (ApiException e) {
				Log.e(TAG, "Problem making wiktionary request", e);
			} catch (ParseException e) {
				Log.e(TAG, "Problem making wiktionary request", e);
			}

			if (parsedText == null || parsedText == "") {
				parsedText = "Dictionary could not find this word. Please Try Again.";
			}
			LookupActivity.DictMeaning = parsedText;
			return parsedText;
		}

		/**
		 * Our progress update pushes a title bar update.
		 */
		@Override
		protected void onProgressUpdate(String... args) {
			String searchWord = args[0];
			setEntryTitle(searchWord);
		}

		/**
		 * When finished, push the newly-found entry content into our
		 * {@link WebView} and hide the {@link ProgressBar}.
		 */
		@Override
		protected void onPostExecute(String parsedText) {
			mTitleBar.startAnimation(mSlideOut);
			mProgress.setVisibility(View.INVISIBLE);

			setEntryContent(parsedText);
		}
	}
	/**
	 * Make the {@link ProgressBar} visible when our in-animation finishes.
	 */
	public void onAnimationEnd(Animation animation) {
		mProgress.setVisibility(View.VISIBLE);
	}

	public void onAnimationRepeat(Animation animation) {
		// Not interested if the animation repeats
	}

	public void onAnimationStart(Animation animation) {
		// Not interested when the animation starts
	}
}
