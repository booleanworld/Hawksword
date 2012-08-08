/*
 * Copyright (C) 2008 ZXing authors
 * Copyright 2011 Robert Theis
 * Copyright (c) 2012 booleanworld <booleanworld@gmail.com>
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

package com.bw.hawksword.ocr;


import com.bw.hawksword.ocr.BeepManager;
import com.bw.hawksword.ocr.HelpActivity;
import com.bw.hawksword.ocr.OcrResult;
import com.bw.hawksword.ocr.PreferencesActivity;
import com.bw.hawksword.ocr.camera.CameraManager;
import com.bw.hawksword.ocr.camera.ShutterButton;
import com.bw.hawksword.ocr.language.LanguageCodeHelper;
import com.bw.hawksword.offlinedict.RealCode_Compress;
import com.bw.hawksword.parser.Token;
import com.bw.hawksword.parser.Tokenizer;
import com.bw.hawksword.wiktionary.LookupActivity;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.googlecode.tesseract.android.TessBaseAPI;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;

import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;

/**
 * This activity opens the camera and does the actual scanning on a background thread. It draws a
 * viewfinder to help the user place the barcode correctly, shows feedback as the image processing
 * is happening, and then overlays the results when a scan is successful.
 * 
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing/
 */
public final class CaptureActivity extends Activity implements SurfaceHolder.Callback, 
  ShutterButton.OnShutterButtonListener {

  private static final String TAG = CaptureActivity.class.getSimpleName();
  
  // Note: These constants will be overridden by any default values defined in preferences.xml.
  
  
  public static final String DEFAULT_FOCUS_MODE = "Macro";
  public static final String DEFAULT_DICTIONARY_MODE = "Offline";
  /** ISO 639-3 language code indicating the default recognition language. */
  public static final String DEFAULT_SOURCE_LANGUAGE_CODE = "eng";
  
  /** The default online machine translation service to use. */
  public static final String DEFAULT_TRANSLATOR = "Google Translator";
  
  /** The default OCR engine to use. */
  public static final String DEFAULT_OCR_ENGINE_MODE = "Tesseract";
  
  /** The default page segmentation mode to use. */
  public static final String DEFAULT_PAGE_SEGMENTATION_MODE = "Auto";
  
  /** Whether to beep by default when the shutter button is pressed. */
  public static final boolean DEFAULT_TOGGLE_BEEP = true;
  
  /** Whether to initially show a looping, real-time OCR display. */
  public static final boolean DEFAULT_TOGGLE_CONTINUOUS = false;
  
  /** Whether to initially reverse the image returned by the camera. */
  public static final boolean DEFAULT_TOGGLE_REVERSED_IMAGE = false;
  
  /** Whether to enable the use of online translation services be default. */
  public static final boolean DEFAULT_TOGGLE_TRANSLATION = true;
  
  /** Whether the light should be initially activated by default. */
  public static final boolean DEFAULT_TOGGLE_LIGHT = false;
  
  /** Flag to enable display of the on-screen shutter button. */
  private static final boolean DISPLAY_SHUTTER_BUTTON = true;
  
  /** Languages for which Cube data is available. */
  static final String[] CUBE_SUPPORTED_LANGUAGES = { 
    "ara", // Arabic
    "eng", // English
    "hin" // Hindi
  };

  /** Languages that require Cube, and cannot run using Tesseract. */
  private static final String[] CUBE_REQUIRED_LANGUAGES = { 
    "ara" // Arabic
  };
  
  /** Resource to use for data file downloads. */
  static final String DOWNLOAD_BASE = "http://ocr-dictionary.googlecode.com/files/";
  
  /** Download filename for orientation and script detection (OSD) data. */
  static final String OSD_FILENAME = "tesseract-ocr-3.01.osd.tar";
  
  /** Destination filename for orientation and script detection (OSD) data. */
  static final String OSD_FILENAME_BASE = "osd.traineddata";
  
  /** Minimum mean confidence score necessary to not reject single-shot OCR result. Currently unused. */
  static final int MINIMUM_MEAN_CONFIDENCE = 0; // 0 means don't reject any scored results
  
  /** Length of time before the next autofocus request, if the last one was successful. Used in CaptureActivityHandler. */
  static final long AUTOFOCUS_SUCCESS_INTERVAL_MS = 3000L;
  
  /** Length of time before the next autofocus request, if the last request failed. Used in CaptureActivityHandler. */
  static final long AUTOFOCUS_FAILURE_INTERVAL_MS = 1000L;
  
  // Context menu
  private static final int SETTINGS_ID = Menu.FIRST;
  private static final int ABOUT_ID = Menu.FIRST + 1;
  private static final int HELP_ID = Menu.FIRST+2;
  private static final int HISTORY_ID = Menu.FIRST+3;
  
  
  // Options menu, for copy to clipboard
  private static final int OPTIONS_COPY_RECOGNIZED_TEXT_ID = Menu.FIRST;
  private static final int OPTIONS_COPY_TRANSLATED_TEXT_ID = Menu.FIRST + 1;

  private CameraManager cameraManager;
  private CaptureActivityHandler handler;
  private ViewfinderView viewfinderView;
  private SurfaceView surfaceView;
  private SurfaceHolder surfaceHolder;
  private OcrResult lastResult;
  private boolean hasSurface;
  public static BeepManager beepManager;
  private TessBaseAPI baseApi; // Java interface for the Tesseract OCR engine
  private String sourceLanguageCodeOcr; // ISO 639-3 language code
  private String sourceLanguageReadable; // Language name, for example, "English"
  private String sourceLanguageCodeTranslation; // ISO 639-1 language code
  private String targetLanguageCodeTranslation; // ISO 639-1 language code
  private String targetLanguageReadable; // Language name, for example, "English"
  private int pageSegmentationMode = TessBaseAPI.PSM_AUTO;
  private int ocrEngineMode = TessBaseAPI.OEM_TESSERACT_ONLY;
  private String characterBlacklist;
  private String characterWhitelist;
  private boolean isTranslationActive; // Whether we want to show translations
  private boolean isContinuousModeActive; // Whether we are doing OCR in continuous mode
  private SharedPreferences prefs;
  private OnSharedPreferenceChangeListener listener;
  private ProgressDialog dialog; // for initOcr - language download & unzip
  private ProgressDialog indeterminateDialog; // also for initOcr - init OCR engine
  private boolean isEngineReady;
  private boolean isPaused;
  private static boolean isFirstLaunch; // True if this is the first time the app is being run
  private GoogleAnalyticsTracker tracker; //For Google Analytics
  private File path; //For Storage Directory.
  public static RealCode_Compress r;
  private boolean flag = true;
  private boolean cameralock = false;
  public static boolean fileCheck = false; 
  private Camera camera;
  private TableLayout tbl_list;
  private Button btn_scan;
  private ImageButton btn_rescan;
  private ProgressBar scan_process;
  private ImageButton btn_close;
  public static boolean btn_lock = false;
  public static boolean mode_chg = false;
  
  Handler getHandler() {
    return handler;
  }

  CameraManager getCameraManager() {
    return cameraManager;
  }
  
@Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    Log.d(TAG, "onCreate()");
    checkFirstLaunch();
    if (isFirstLaunch) {
      setDefaultPreferences();
    }

    Window window = getWindow();
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    setContentView(R.layout.capture);
    viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
    tbl_list = (TableLayout)findViewById(R.id.tableLayout1);
    handler = null;
    lastResult = null;
    hasSurface = false;
    beepManager = new BeepManager();
    
    tracker = GoogleAnalyticsTracker.getInstance(); //Initialise Google Analytics  
    tracker.startNewSession("UA-30209148-1", 10, this); //booleanworld
    /*
     * Only for Local Testing.
    //tracker.setDebug(true);
    //tracker.setDryRun(true);
   */

    cameraManager = new CameraManager(getApplication());
    viewfinderView.setCameraManager(cameraManager);
    scan_process = (ProgressBar)findViewById(R.id.scan_process);
    btn_scan = (Button)findViewById(R.id.scan);
    btn_scan.setOnClickListener(new Button.OnClickListener(){

		public void onClick(View v) {
			btn_scan.setVisibility(8);
			scan_process.setVisibility(0);

		      if (handler != null) {
		    	  tracker.trackEvent( // Google Analytics 
		    	            "Clicks",  // Category
		    	            "Shutter Button",  // Action
		    	            "clicked", // Label
		    	            1);       // Value
		        handler.shutterButtonClick();
		        
		        //Make one Thread that will process the the OCR Decode and Parsing the Words from the the Raw String.
		        //After Generating the list by this Thread, Make a Grid in Overlay.
		        //Grid List is Clickable
		        //By clicking on any row from the list , the Dictionary page should be opened.
		        
		     } else {
		        // Null handler. Why?
		        showErrorMessage("Null handler error", "Please report this error along with what type of device you are using.");
		      }
		}
    	
    });
    btn_rescan = (ImageButton)findViewById(R.id.imageButton2);
    btn_rescan.setOnClickListener(new Button.OnClickListener(){

		public void onClick(View v) {
			// TODO Auto-generated method stub
			btn_scan.setVisibility(0);
			scan_process.setVisibility(8);
			btn_lock = false;
			resetStatusView();
			clearList();
			handler.requestAutofocus(R.id.auto_focus);
		}
    	
    });
    btn_close = (ImageButton)findViewById(R.id.imageButton3);
    btn_close.setOnClickListener(new Button.OnClickListener(){

		public void onClick(View v) {
			// TODO Auto-generated method stub
			finish();
			System.exit(0);
			
		}
    	
    });
    // Set listener to change the size of the viewfinder rectangle.
    viewfinderView.setOnTouchListener(new View.OnTouchListener() {
      int lastX = -1;
      int lastY = -1;


      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
          lastX = -1;
          lastY = -1;
          return true;
        case MotionEvent.ACTION_MOVE:
          int currentX = (int) event.getX();
          int currentY = (int) event.getY();

          try {
            Rect rect = cameraManager.getFramingRect();

            final int BUFFER = 50;
            final int BIG_BUFFER = 60;
            if (lastX >= 0) {
              // Adjust the size of the viewfinder rectangle. Check if the touch event occurs in the corner areas first, because the regions overlap.
              if (((currentX >= rect.left - BIG_BUFFER && currentX <= rect.left + BIG_BUFFER) || (lastX >= rect.left - BIG_BUFFER && lastX <= rect.left + BIG_BUFFER))
                  && ((currentY <= rect.top + BIG_BUFFER && currentY >= rect.top - BIG_BUFFER) || (lastY <= rect.top + BIG_BUFFER && lastY >= rect.top - BIG_BUFFER))) {
                // Top left corner: adjust both top and left sides
                cameraManager.adjustFramingRect( 2 * (lastX - currentX), 2 * (lastY - currentY));
                viewfinderView.removeResultText();
              } else if (((currentX >= rect.right - BIG_BUFFER && currentX <= rect.right + BIG_BUFFER) || (lastX >= rect.right - BIG_BUFFER && lastX <= rect.right + BIG_BUFFER)) 
                  && ((currentY <= rect.top + BIG_BUFFER && currentY >= rect.top - BIG_BUFFER) || (lastY <= rect.top + BIG_BUFFER && lastY >= rect.top - BIG_BUFFER))) {
                // Top right corner: adjust both top and right sides
                cameraManager.adjustFramingRect( 2 * (currentX - lastX), 2 * (lastY - currentY));
                viewfinderView.removeResultText();
              } else if (((currentX >= rect.left - BIG_BUFFER && currentX <= rect.left + BIG_BUFFER) || (lastX >= rect.left - BIG_BUFFER && lastX <= rect.left + BIG_BUFFER))
                  && ((currentY <= rect.bottom + BIG_BUFFER && currentY >= rect.bottom - BIG_BUFFER) || (lastY <= rect.bottom + BIG_BUFFER && lastY >= rect.bottom - BIG_BUFFER))) {
                // Bottom left corner: adjust both bottom and left sides
                cameraManager.adjustFramingRect(2 * (lastX - currentX), 2 * (currentY - lastY));
                viewfinderView.removeResultText();
              } else if (((currentX >= rect.right - BIG_BUFFER && currentX <= rect.right + BIG_BUFFER) || (lastX >= rect.right - BIG_BUFFER && lastX <= rect.right + BIG_BUFFER)) 
                  && ((currentY <= rect.bottom + BIG_BUFFER && currentY >= rect.bottom - BIG_BUFFER) || (lastY <= rect.bottom + BIG_BUFFER && lastY >= rect.bottom - BIG_BUFFER))) {
                // Bottom right corner: adjust both bottom and right sides
                cameraManager.adjustFramingRect(2 * (currentX - lastX), 2 * (currentY - lastY));
                viewfinderView.removeResultText();
              } else if (((currentX >= rect.left - BUFFER && currentX <= rect.left + BUFFER) || (lastX >= rect.left - BUFFER && lastX <= rect.left + BUFFER))
                  && ((currentY <= rect.bottom && currentY >= rect.top) || (lastY <= rect.bottom && lastY >= rect.top))) {
                // Adjusting left side: event falls within BUFFER pixels of left side, and between top and bottom side limits
                cameraManager.adjustFramingRect(2 * (lastX - currentX), 0);
                viewfinderView.removeResultText();
              } else if (((currentX >= rect.right - BUFFER && currentX <= rect.right + BUFFER) || (lastX >= rect.right - BUFFER && lastX <= rect.right + BUFFER))
                  && ((currentY <= rect.bottom && currentY >= rect.top) || (lastY <= rect.bottom && lastY >= rect.top))) {
                // Adjusting right side: event falls within BUFFER pixels of right side, and between top and bottom side limits
                cameraManager.adjustFramingRect(2 * (currentX - lastX), 0);
                viewfinderView.removeResultText();
              } else if (((currentY <= rect.top + BUFFER && currentY >= rect.top - BUFFER) || (lastY <= rect.top + BUFFER && lastY >= rect.top - BUFFER))
                  && ((currentX <= rect.right && currentX >= rect.left) || (lastX <= rect.right && lastX >= rect.left))) {
                // Adjusting top side: event falls within BUFFER pixels of top side, and between left and right side limits
                cameraManager.adjustFramingRect(0, 2 * (lastY - currentY));
                viewfinderView.removeResultText();
              } else if (((currentY <= rect.bottom + BUFFER && currentY >= rect.bottom - BUFFER) || (lastY <= rect.bottom + BUFFER && lastY >= rect.bottom - BUFFER))
                  && ((currentX <= rect.right && currentX >= rect.left) || (lastX <= rect.right && lastX >= rect.left))) {
                // Adjusting bottom side: event falls within BUFFER pixels of bottom side, and between left and right side limits
                cameraManager.adjustFramingRect(0, 2 * (currentY - lastY));
                viewfinderView.removeResultText();
              }     
            }
          } catch (NullPointerException e) {
            Log.e(TAG, "Framing rect not available", e);
          }
          v.invalidate();         
          lastX = currentX;
          lastY = currentY;
          return true;
        case MotionEvent.ACTION_UP:
          lastX = -1;
          lastY = -1;
          return true;
        }
        return false;
      }
    });
    isEngineReady = false;
  }

  @Override
  protected void onResume() {
    super.onResume();  
    
    Log.d(TAG,"onResume()");
    resetStatusView();
    String previousSourceLanguageCodeOcr = sourceLanguageCodeOcr;
    int previousOcrEngineMode = ocrEngineMode;
    
    retrievePreferences();
    
    // Set up the camera preview surface.
    surfaceView = (SurfaceView) findViewById(R.id.preview_view);
    surfaceHolder = surfaceView.getHolder();
    if (!hasSurface) {
      surfaceHolder.addCallback(this);
      surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    
    // Comment out the following block to test non-OCR functions without an SD card
    
    // Do OCR engine initialization, if necessary
    boolean doNewInit = (baseApi == null) || !sourceLanguageCodeOcr.equals(previousSourceLanguageCodeOcr) || ocrEngineMode != previousOcrEngineMode;
    File storageDirectory = getStorageDirectory();
    if (doNewInit) {      
      // Initialize the OCR engine
      if (storageDirectory != null) {
        initOcrEngine(storageDirectory, sourceLanguageCodeOcr, sourceLanguageReadable);
      }
    } else {
      // We already have the engine initialized, so just start the camera.
      resumeOCR();
    }
    
  }
  /** 
   * Method to start or restart recognition after the OCR engine has been initialized,
   * or after the app regains focus. Sets state related settings and OCR engine parameters,
   * and requests camera initialization.
   */
  void resumeOCR() {
	  
    Log.d(TAG, "resumeOCR()");
    
    // This method is called when Tesseract has already been successfully initialized, so set 
    // isEngineReady = true here.
    isEngineReady = true;
    
    isPaused = false;

    if (handler != null) {
      handler.resetState();
    }
    if (baseApi != null) {
      baseApi.setPageSegMode(pageSegmentationMode);
      baseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, characterBlacklist);
      baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, characterWhitelist);
    }
    if(flag == true && fileCheck == true){
    	  r = new RealCode_Compress(path.toString()+ File.separator + "tessdata");
    	  flag = false;
    }
    if (hasSurface && !cameralock) { 
	      initCamera(surfaceHolder,90);
    }
  }
  public void surfaceCreated(SurfaceHolder holder) {
    Log.d(TAG, "surfaceCreated()");
    
    if (holder == null) {
      Log.e(TAG, "surfaceCreated gave us a null surface");
    }
    
    // Only initialize the camera if the OCR engine is ready to go.
    if (!hasSurface && isEngineReady && !cameralock) {
      Log.d(TAG, "surfaceCreated(): calling initCamera()...");
      initCamera(holder,90);
    }
    hasSurface = true;

  }
  
  /** Initializes the camera and starts the handler to begin previewing. */
  private void initCamera(SurfaceHolder surfaceHolder,int angle) {
    Log.d(TAG, "initCamera()");
    cameralock = true;
    String[] focusModes = getResources().getStringArray(R.array.focusmodes);
    String focusMode = prefs.getString(PreferencesActivity.KEY_FOCUS_MODE, focusModes[0]);
    try {

      // Open and initialize the camera
      cameraManager.openDriver(surfaceHolder,angle,focusMode);
      
      // Creating the handler starts the preview, which can also throw a RuntimeException.
      handler = new CaptureActivityHandler(this, cameraManager, baseApi, isContinuousModeActive);
      
    } catch (IOException ioe) {
      showErrorMessage("Error", "Could not initialize camera. Please try restarting device??");
    } catch (RuntimeException e) {
      // Barcode Scanner has seen crashes in the wild of this variety:
      // java.?lang.?RuntimeException: Fail to connect to camera service
      showErrorMessage("Error", "Could not initialize camera. Please try restarting device.");
    }   
  }
  
  @Override
  protected void onPause() {
    if (handler != null) {
      handler.quitSynchronously();
    }
    
    // Stop using the camera, to avoid conflicting with other camera-based apps
    cameraManager.closeDriver();
    cameralock = false;
    if (!hasSurface) {
      SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
      SurfaceHolder surfaceHolder = surfaceView.getHolder();
      surfaceHolder.removeCallback(this);
    }
    super.onPause();
  }

  void stopHandler() {
    if (handler != null) {
      handler.stop();
    }
  }

  @Override
  protected void onDestroy() {
    if (baseApi != null) {
      baseApi.end();
    }
    super.onDestroy();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      if (lastResult == null) {
        setResult(RESULT_CANCELED);
        finish();
        return true;
      } else {
        // Go back to previewing in regular OCR mode.
        resetStatusView();
        if (handler != null) {
          handler.sendEmptyMessage(R.id.restart_preview);
        }
        return true;
      }
    } else if (keyCode == KeyEvent.KEYCODE_CAMERA) {
        handler.hardwareShutterButtonClick();
      return true;
    } else if (keyCode == KeyEvent.KEYCODE_FOCUS) {      
      // Only perform autofocus if user is not holding down the button.
      if (event.getRepeatCount() == 0) {
     handler.requestDelayedAutofocus(500L, R.id.user_requested_auto_focus);
      }
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    //    MenuInflater inflater = getMenuInflater();
    //    inflater.inflate(R.menu.options_menu, menu);
    super.onCreateOptionsMenu(menu);
    menu.add(0, SETTINGS_ID, 0, "Settings").setIcon(android.R.drawable.ic_menu_preferences);
    menu.add(1, ABOUT_ID, 1, "About").setIcon(android.R.drawable.ic_menu_info_details);
    menu.add(2, HELP_ID, 2, "Help").setIcon(android.R.drawable.ic_menu_help);
    menu.add(3, HISTORY_ID, 3, "History").setIcon(android.R.drawable.ic_menu_recent_history);
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
    case HELP_ID:{
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

  public void surfaceDestroyed(SurfaceHolder holder) {
    hasSurface = false;
  }

  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	  Log.d(TAG, "surfaceChanged()");  
  }

  /** Sets the necessary language code values for the given OCR language. */
  private boolean setSourceLanguage(String languageCode) {
    sourceLanguageCodeOcr = languageCode;
    sourceLanguageCodeTranslation = LanguageCodeHelper.mapLanguageCode(languageCode);
    sourceLanguageReadable = LanguageCodeHelper.getOcrLanguageName(this, languageCode);
    return true;
  }

  /** Finds the proper location on the SD card where we can save files. */
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
   * Requests initialization of the OCR engine with the given parameters.
   * 
   * @param storageRoot Path to location of the tessdata directory to use
   * @param languageCode Three-letter ISO 639-3 language code for OCR 
   * @param languageName Name of the language for OCR, for example, "English"
   */
  private void initOcrEngine(File storageRoot, String languageCode, String languageName) {    
    isEngineReady = false;
    path = storageRoot;
    // Set up the dialog box for the thermometer-style download progress indicator
    if (dialog != null) {
      dialog.dismiss();
    }
    dialog = new ProgressDialog(this);
    
    // If we have a language that only runs using Cube, then set the ocrEngineMode to Cube
    if (ocrEngineMode != TessBaseAPI.OEM_CUBE_ONLY) {
      for (String s : CUBE_REQUIRED_LANGUAGES) {
        if (s.equals(languageCode)) {
          ocrEngineMode = TessBaseAPI.OEM_CUBE_ONLY;
          SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
          prefs.edit().putString(PreferencesActivity.KEY_OCR_ENGINE_MODE, getOcrEngineModeName()).commit();
        }
      }
    }

    // If our language doesn't support Cube, then set the ocrEngineMode to Tesseract
    if (ocrEngineMode != TessBaseAPI.OEM_TESSERACT_ONLY) {
      boolean cubeOk = false;
      for (String s : CUBE_SUPPORTED_LANGUAGES) {
        if (s.equals(languageCode)) {
          cubeOk = true;
        }
      }
      if (!cubeOk) {
        ocrEngineMode = TessBaseAPI.OEM_TESSERACT_ONLY;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putString(PreferencesActivity.KEY_OCR_ENGINE_MODE, getOcrEngineModeName()).commit();
      }
    }
    
    // Display the name of the OCR engine we're initializing in the indeterminate progress dialog box
    indeterminateDialog = new ProgressDialog(this);
    indeterminateDialog.setTitle("Please wait");
    String ocrEngineModeName = getOcrEngineModeName();
    if (ocrEngineModeName.equals("Both")) {
      indeterminateDialog.setMessage("Initializing Cube and Tesseract OCR engines for " + languageName + "...");
    } else {
      indeterminateDialog.setMessage("Initializing " + ocrEngineModeName + " OCR engine for " + languageName + "...");
    }
    indeterminateDialog.setCancelable(false);
    indeterminateDialog.show();
    
    if (handler != null) {
      handler.quitSynchronously();     
    }
    
    // Start AsyncTask to install language data and init OCR
    baseApi = new TessBaseAPI();
    new OcrInitAsyncTask(this, baseApi, dialog, indeterminateDialog, languageCode, languageName, ocrEngineMode)
      .execute(storageRoot.toString());
  }
  
  /**
   * Displays information relating to the result of OCR, and requests a translation if necessary.
   * 
   * @param ocrResult Object representing successful OCR results
   * @return True if a non-null result was received for OCR
   * @throws IOException 
   */
  boolean handleOcrDecode(OcrResult ocrResult) throws IOException {
    lastResult = ocrResult;

    try {
      ocrResult.getText();
      Log.i(TAG, "SUCCESS");
    } catch (NullPointerException e) {
      Toast toast = Toast.makeText(this, "Recognition failed. Please try again.", Toast.LENGTH_SHORT);
      toast.setGravity(Gravity.TOP, 0, 0);
      toast.show();  
      return false;
    }    
    Tokenizer tokenizer = null;
    tokenizer = new Tokenizer("/mnt/sdcard/Android/data/com.bw.hawksword.ocr/files/mounted/tessdata/stop_words");
	ArrayList<Token> tokens = tokenizer.tokenize(ocrResult.getText());
	if(!tokens.isEmpty()){
		generateList(tokens);
		btn_lock = true;
		scan_process.setVisibility(View.GONE);
		return true;
    /*if(cl.initparser()){
    	String word = cl.getBestWord();
    	if(word != null && word!="" && word != " "){
	        viewfinderView.setVisibility(View.GONE);
	        
	        //Google Analytic
	        
	        String[] dictModes = getResources().getStringArray(R.array.capturemodes);
	        String dictMode = prefs.getString(PreferencesActivity.KEY_DICTIONARY_MODE, dictModes[0]);
	        
	        if(dictMode.equals("Online"))
	        {
		        //Google Analytics
		        tracker.trackPageView("/Online"); 
		        //Call Dictionary----- Online
			    Intent  dict = new Intent(this,LookupActivity.class);
			    dict.putExtra("ST",word.toLowerCase());
			    dict.putExtra("Mode","Online");
			    startActivity(dict);	    
	        }
	        else if(dictMode.equals("Offline"))
	        {
		        //Google Analytics
		        tracker.trackPageView("/Offline"); 
		        //Call Dictionary ------- Offline
		        Intent  dict = new Intent(this,LookupActivity.class);
			    dict.putExtra("ST",word.toLowerCase());
			    dict.putExtra("Path",path.toString()+ File.separator + "tessdata");
			    dict.putExtra("Mode","Offline");
			    startActivity(dict);
	        }
		    return true;
        
    }else{
    	Toast toast = Toast.makeText(this, "Recognition failed. Please try again.", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
        return false;
    }
}else{
	Toast toast = Toast.makeText(this, "Recognition failed. Please try again.", Toast.LENGTH_SHORT);
    toast.setGravity(Gravity.TOP, 0, 0);
    toast.show();
    return false;
}*/
	  }
	  else{
		  Toast toast = Toast.makeText(this, "Recognition failed. Please try again.", Toast.LENGTH_SHORT);
		  toast.setGravity(Gravity.TOP, 0, 0);
		  toast.show();
		  return false;
	  }
  }
  /**
   * Resets view elements.
   */
  private void resetStatusView() {
	  if(!btn_lock)
	  {
		  viewfinderView.setVisibility(View.VISIBLE);
		  btn_scan.setVisibility(View.VISIBLE);
		  scan_process.setVisibility(View.GONE);
		  lastResult = null;
		  viewfinderView.removeResultText();
	  }
	  if(mode_chg)
	  {
		  clearList();
		  mode_chg=false;
	  }
  }
  /**
   * Generate the Grid List on main screen after getting result
   * @tokens:String array of parsed result.
   * @author:maunik318@gmail.com
   */
  private void generateList(ArrayList<Token> tokens)
  {

	  	TextView t2;
	  	TableRow row;
	  	
	  	//Retriving OCR Mode Online/Offline
	  	
        String[] dictModes = getResources().getStringArray(R.array.capturemodes);
        final String dictMode = prefs.getString(PreferencesActivity.KEY_DICTIONARY_MODE, dictModes[0]);
	  	
	  	
	  	//Converting to dip unit
	  	int dip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,(float) 1, getResources().getDisplayMetrics());
	  	
	  	//Generating List..
	  	for (int i = 0; i < tokens.size(); i++) {
	  		if(r.spellSearch(tokens.get(i).getValue())){
			  	row = new TableRow(this);
		  	    t2 = new TextView(this);
		        t2.setTextColor(getResources().getColor(R.color.Black));    	    		
	  	    	t2.setText(tokens.get(i).getValue());
		  	    t2.setTypeface(null, 1);
		  	    t2.setTextSize(15);
		  	    t2.setWidth(150 * dip);
		  	    row.addView(t2);
		  	    row.setClickable(true);
		  	    tbl_list.addView(row, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		  	    t2.setOnClickListener(new OnClickListener() {
		  	    	@Override
		  	    	public void onClick(View arg0) {
		  	    		String currentText = ((TextView)arg0).getText().toString();
		  	    		//Toast.makeText(CaptureActivity.this,currenttext,Toast.LENGTH_SHORT).show();
					    Intent  dict = new Intent(getBaseContext(),LookupActivity.class);
					    dict.putExtra("ST",currentText);
					    dict.putExtra("Mode",dictMode);
					    startActivity(dict);	
		  	    	}
		  	    });
		  	}
	  	}
  }
  /**
   * To clear generated List in TableView.
   * @auther:maunik318@gmail.com
   */
  private void clearList()
  {
	  tbl_list.removeAllViews();
  }
  /** Displays a pop-up message showing the name of the current OCR source language. */
  void showLanguageName() {   
    Toast toast = Toast.makeText(this, "OCR: " + sourceLanguageReadable, Toast.LENGTH_LONG);
    toast.setGravity(Gravity.TOP, 0, 0);
    toast.show();
  }

  
  void setButtonVisibility(boolean visible) {
  }
  
  /**
   * Enables/disables the shutter button to prevent double-clicks on the button.
   * 
   * @param clickable True if the button should accept a click
   */
  void setShutterButtonClickable(boolean clickable) {
 //   shutterButton.setClickable(clickable);
    
  }

  /** Request the viewfinder to be invalidated. */
  void drawViewfinder() {
    viewfinderView.drawViewfinder();
  }
  

  public void onShutterButtonClick(ShutterButton b) {

      if (handler != null) {
    	  tracker.trackEvent( // Google Analytics 
    	            "Clicks",  // Category
    	            "Shutter Button",  // Action
    	            "clicked", // Label
    	            1);       // Value
        handler.shutterButtonClick();
     } else {
        // Null handler. Why?
        showErrorMessage("Null handler error", "Please report this error along with what type of device you are using.");
      }
   // }
  }

  public void onShutterButtonFocus(ShutterButton b, boolean pressed) {
    requestDelayedAutofocus();
  }
  
  /**
   * Requests autofocus after a 350 ms delay. This delay prevents requesting focus when the user 
   * just wants to click the shutter button without focusing. Quick button press/release will 
   * trigger onShutterButtonClick() before the focus kicks in.
   */
  private void requestDelayedAutofocus() {
    // Wait 350 ms before focusing to avoid interfering with quick button presses when
    // the user just wants to take a picture without focusing.
    if (handler != null) {
      handler.requestDelayedAutofocus(500L, R.id.user_requested_auto_focus);
    }
  }
  
  static boolean getFirstLaunch() {
    return isFirstLaunch;
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
   * Returns a string that represents which OCR engine(s) are currently set to be run.
   * 
   * @return OCR engine mode
   */
  String getOcrEngineModeName() {
    String ocrEngineModeName = "";
    String[] ocrEngineModes = getResources().getStringArray(R.array.ocrenginemodes);
    if (ocrEngineMode == TessBaseAPI.OEM_TESSERACT_ONLY) {
      ocrEngineModeName = ocrEngineModes[0];
    } else if (ocrEngineMode == TessBaseAPI.OEM_CUBE_ONLY) {
      ocrEngineModeName = ocrEngineModes[1];
    } else if (ocrEngineMode == TessBaseAPI.OEM_TESSERACT_CUBE_COMBINED) {
      ocrEngineModeName = ocrEngineModes[2];
    }
    return ocrEngineModeName;
  }
  
  /**
   * Gets values from shared preferences and sets the corresponding data members in this activity.
   */
  private void retrievePreferences() {
      prefs = PreferenceManager.getDefaultSharedPreferences(this);
      
      // Retrieve from preferences, and set in this Activity, the language preferences
      PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
      setSourceLanguage(prefs.getString(PreferencesActivity.KEY_SOURCE_LANGUAGE_PREFERENCE, CaptureActivity.DEFAULT_SOURCE_LANGUAGE_CODE));
      isTranslationActive = prefs.getBoolean(PreferencesActivity.KEY_TOGGLE_TRANSLATION, false);
      
      // Retrieve from preferences, and set in this Activity, the page segmentation mode preference
      String[] pageSegmentationModes = getResources().getStringArray(R.array.pagesegmentationmodes);
      String pageSegmentationModeName = prefs.getString(PreferencesActivity.KEY_PAGE_SEGMENTATION_MODE, pageSegmentationModes[0]);
      if (pageSegmentationModeName.equals(pageSegmentationModes[0])) {
        pageSegmentationMode = TessBaseAPI.PSM_AUTO_OSD;
      } else if (pageSegmentationModeName.equals(pageSegmentationModes[1])) {
        pageSegmentationMode = TessBaseAPI.PSM_AUTO;
      } else if (pageSegmentationModeName.equals(pageSegmentationModes[2])) {
        pageSegmentationMode = TessBaseAPI.PSM_SINGLE_BLOCK;
      } else if (pageSegmentationModeName.equals(pageSegmentationModes[3])) {
        pageSegmentationMode = TessBaseAPI.PSM_SINGLE_CHAR;
      } else if (pageSegmentationModeName.equals(pageSegmentationModes[4])) {
        pageSegmentationMode = TessBaseAPI.PSM_SINGLE_COLUMN;
      } else if (pageSegmentationModeName.equals(pageSegmentationModes[5])) {
        pageSegmentationMode = TessBaseAPI.PSM_SINGLE_LINE;
      } else if (pageSegmentationModeName.equals(pageSegmentationModes[6])) {
        pageSegmentationMode = TessBaseAPI.PSM_SINGLE_WORD;
      } else if (pageSegmentationModeName.equals(pageSegmentationModes[7])) {
        pageSegmentationMode = TessBaseAPI.PSM_SINGLE_BLOCK_VERT_TEXT;
      }
      
      // Retrieve from preferences, and set in this Activity, the OCR engine mode
      String[] ocrEngineModes = getResources().getStringArray(R.array.ocrenginemodes);
      String ocrEngineModeName = prefs.getString(PreferencesActivity.KEY_OCR_ENGINE_MODE, ocrEngineModes[0]);
      if (ocrEngineModeName.equals(ocrEngineModes[0])) {
        ocrEngineMode = TessBaseAPI.OEM_TESSERACT_ONLY;
      } else if (ocrEngineModeName.equals(ocrEngineModes[1])) {
        ocrEngineMode = TessBaseAPI.OEM_CUBE_ONLY;
      } else if (ocrEngineModeName.equals(ocrEngineModes[2])) {
        ocrEngineMode = TessBaseAPI.OEM_TESSERACT_CUBE_COMBINED;
      }
      
      // Retrieve from preferences, and set in this Activity, the character blacklist and whitelist
      characterBlacklist = OcrCharacterHelper.getBlacklist(prefs, sourceLanguageCodeOcr);
      characterWhitelist = OcrCharacterHelper.getWhitelist(prefs, sourceLanguageCodeOcr);
      
      prefs.registerOnSharedPreferenceChangeListener(listener);
      
      beepManager.initSounds(getBaseContext(),this);
      beepManager.addSound(1, R.raw.beep);


      
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
}
