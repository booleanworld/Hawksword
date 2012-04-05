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

import com.bw.hawksword.ocr.CaptureActivity;
import com.bw.hawksword.ocr.R;
import com.bw.hawksword.offlinedict.RealCode;
import com.bw.hawksword.offlinedict.RealCode_Compress;
import com.bw.hawksword.wiktionary.SimpleWikiHelper.ApiException;
import com.bw.hawksword.wiktionary.SimpleWikiHelper.ParseException;


import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Activity that lets users browse through Wiktionary content. This is just the
 * user interface, and all API communication and parsing is handled in
 * {@link ExtendedWikiHelper}.
 */
public class LookupActivity extends Activity implements AnimationListener {
    private static final String TAG = "LookupActivity";

    private View mTitleBar;
    private TextView mTitle;
    private ProgressBar mProgress;
    private WebView mWebView;

    private Animation mSlideIn;
    private Animation mSlideOut;
    private String query;
    private String mode;
   // private String path;
    private String[] list;

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

 	   Bundle b = getIntent().getExtras(); 
	   query = b.getString("ST"); 
	   mode = b.getString("Mode");
	   //path = b.getString("Path");
        
        // Make the view transparent to show background
        mWebView.setBackgroundColor(0);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Insert your code here
            	String[] arr = url.split("/");
            	query = arr[3];
            	onNewIntent(query);
				return true;
            }
        });
        // Prepare User-Agent string for wiki actions
        ExtendedWikiHelper.prepareUserAgent(this);

        // Handle incoming intents as possible searches or links
        if(mode.equals("Offline"))
        {
        	//RealCode r = new RealCode(path);
        	Log.d("This",query);
        	list = CaptureActivity.r.search(query);
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
            String lastEntry = mHistory.pop();
            startNavigating(lastEntry, false);

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
                    	if(list != null)
	                    	for(int i=0;i<list.length;i++)
	                    		parsedText += i+1 +". "+ list[i]+"<BR><HR>";
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
