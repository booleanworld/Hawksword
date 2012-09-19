package com.bw.hawksword.ocr;

import java.io.ByteArrayOutputStream;
import java.io.File;

import com.bw.hawksword.ocr.camera.CameraManager;
import com.bw.hawksword.ocr.language.LanguageCodeHelper;
import com.googlecode.tesseract.android.TessBaseAPI;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ScreenshotActivity extends Activity {

	private CropfinderView cropfinderView;
	private CameraManager screenshotManager;
	private FrameLayout shutter_box;
	private ImageView shutter;
	private Bitmap greyscaled;
	private boolean reverseImage;
	private TessBaseAPI baseApi;
	private String TAG = "ScreenshotActivity";
	private String sourceLanguageCodeOcr; // ISO 639-3 language code
	private String sourceLanguageReadable; // Language name, for example, "English"
	private String sourceLanguageCodeTranslation; // ISO 639-1 language code
	private boolean isEngineReady;
	private File path; //For Storage Directory.
	private int ocrEngineMode = TessBaseAPI.OEM_TESSERACT_ONLY;
	private ProgressDialog indeterminateDialog; // also for initOcr - init OCR engine
	private ProgressDialog dialog; // for initOcr - language download & unzip
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		setContentView(R.layout.capture_screenshot);
		cropfinderView = (CropfinderView) findViewById(R.id.viewfinder_view);
		shutter_box = (FrameLayout)findViewById(R.id.shutter_box);
		//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
		//reverseImage = prefs.getBoolean(PreferencesActivity.KEY_REVERSE_IMAGE, false);
		//File storageDirectory = getStorageDirectory();
		//prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// Retrieve from preferences, and set in this Activity, the language preferences
		//PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		//String languageCode =  prefs.getString(PreferencesActivity.KEY_SOURCE_LANGUAGE_PREFERENCE, CaptureActivity.DEFAULT_SOURCE_LANGUAGE_CODE);
		//sourceLanguageCodeOcr = languageCode;
		//sourceLanguageCodeTranslation = LanguageCodeHelper.mapLanguageCode(languageCode);
		//sourceLanguageReadable = LanguageCodeHelper.getOcrLanguageName(this, languageCode);
		
		
//		// Initialize the OCR engine
//		if (storageDirectory != null) {
//			initOcrEngine(storageDirectory, sourceLanguageCodeOcr, sourceLanguageReadable);
//		}
		//screenshotManager = new CameraManager(getApplication());
		//viewfinderView.setCameraManager(screenshotManager);
		cropfinderView.setOnTouchListener(new View.OnTouchListener() {
			int lastX = -1;
			int lastY = -1;

			public boolean onTouch(View v, MotionEvent event) {
				Rect rect = cropfinderView.getFramingRect();
				int cy = rect.bottom - rect.top;
				int cx = rect.right - rect.left;
				Display display = getWindowManager().getDefaultDisplay(); 
				int width = display.getWidth();  // deprecated
				int height = display.getHeight();  // deprecated

				if (( ( cy ) / 4 ) + rect.top < (int) event.getY() && ( rect.bottom - ( cy ) / 4 )  > (int) event.getY() && ( ( cy ) / 4 ) + rect.left < (int) event.getX() && ( rect.right - ( cx ) / 4 )  > (int) event.getX() ) {

					return true;
				}

				//       switch (event.getAction()) {
				//     case MotionEvent.ACTION_DOWN:
				//Toast.makeText(CaptureActivity.this, "Action Down", Toast.LENGTH_SHORT).show();
				//     lastX = -1;
				//   lastY = -1;
				// return true;
				//    case MotionEvent.ACTION_MOVE:
				int currentX = (int) event.getX();
				int currentY = (int) event.getY();
				int maxX = width;
				int maxY = ( height * 60 )/100;
				int centerX = maxX/2;
				int centerY = maxY/2;
				try {
					if(currentY > maxY)
					{	
						return true;
					}
					if(currentX <= centerX && currentY <= centerY) { // 2nd Quadrant 
						int deltaH = (rect.top - currentY);
						int deltaW = (rect.left - currentX);
						cropfinderView.adjustFramingRect( deltaW, deltaH);

					}
					else if (currentX <= centerX && currentY > centerY) { // 3rd Quadrant
						int deltaH = (currentY - rect.bottom);
						int deltaW = (rect.left - currentX);
						cropfinderView.adjustFramingRect( deltaW, deltaH);
					}
					else if (currentX > centerX && currentY <= centerY) { // 1st Quadrant
						int deltaH = (rect.top - currentY);
						int deltaW = (currentX - rect.right);
						cropfinderView.adjustFramingRect( deltaW, deltaH);
					}
					else if (currentX > centerX && currentY > centerY) { // 4th Quadrant
						int deltaH = (currentY - rect.bottom);
						int deltaW = (currentX - rect.right);
						cropfinderView.adjustFramingRect( deltaW, deltaH);
					}
				}
				catch(Exception e) {

				}
				v.invalidate();         
				lastX = currentX;
				lastY = currentY;
				return true;
			}

		});
		shutter = (ImageView)findViewById(R.id.shutter_button);
		shutter.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				View content = findViewById(R.id.preview_view);
				content.setDrawingCacheEnabled(true);
				Bitmap bitmap = content.getDrawingCache();
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
				byte[] byteArray = stream.toByteArray();

//				// Set up the indeterminate progress dialog box
//				ProgressDialog indeterminateDialog = new ProgressDialog(getApplication());
//				indeterminateDialog.setTitle("Please wait");    		
//				// String ocrEngineModeName = activity.getOcrEngineModeName();
//				//			    if (ocrEngineModeName.equals("Both")) {
//				//			      indeterminateDialog.setMessage("Performing OCR using Cube and Tesseract...");
//				//			    } else {
//				indeterminateDialog.setMessage("Performing OCR using tesserect...");
//				//			    }
//				indeterminateDialog.setCancelable(false);
//				indeterminateDialog.show();
//
//				// Asyncrhonously launch the OCR process
//				PlanarYUVLuminanceSource source = buildLuminanceSource(byteArray, bitmap.getWidth(), bitmap.getHeight());
//				new OcrRecognizeAsyncTask(ScreenshotActivity.this, baseApi, indeterminateDialog, source.renderCroppedGreyscaleBitmap()).execute();
			}
		});

		drawViewfinder();

	}
//	/**
//	 * Requests initialization of the OCR engine with the given parameters.
//	 * 
//	 * @param storageRoot Path to location of the tessdata directory to use
//	 * @param languageCode Three-letter ISO 639-3 language code for OCR 
//	 * @param languageName Name of the language for OCR, for example, "English"
//	 */
//	private void initOcrEngine(File storageRoot, String languageCode, String languageName) {    
//		isEngineReady = false;
//		path = storageRoot;
//		// Set up the dialog box for the thermometer-style download progress indicator
////		if (dialog != null) {
////			dialog.dismiss();
////		}
//		dialog = new ProgressDialog(this);
//
////		// If we have a language that only runs using Cube, then set the ocrEngineMode to Cube
////		if (ocrEngineMode != TessBaseAPI.OEM_CUBE_ONLY) {
////			for (String s : CUBE_REQUIRED_LANGUAGES) {
////				if (s.equals(languageCode)) {
////					ocrEngineMode = TessBaseAPI.OEM_CUBE_ONLY;
////					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
////					prefs.edit().putString(PreferencesActivity.KEY_OCR_ENGINE_MODE, getOcrEngineModeName()).commit();
////				}
////			}
////		}
////
////		// If our language doesn't support Cube, then set the ocrEngineMode to Tesseract
////		if (ocrEngineMode != TessBaseAPI.OEM_TESSERACT_ONLY) {
////			boolean cubeOk = false;
////			for (String s : CUBE_SUPPORTED_LANGUAGES) {
////				if (s.equals(languageCode)) {
////					cubeOk = true;
////				}
////			}
////			if (!cubeOk) {
//				ocrEngineMode = TessBaseAPI.OEM_TESSERACT_ONLY;
//				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//				prefs.edit().putString(PreferencesActivity.KEY_OCR_ENGINE_MODE, getOcrEngineModeName()).commit();
////			}
////		}
//
//		// Display the name of the OCR engine we're initializing in the indeterminate progress dialog box
//		indeterminateDialog = new ProgressDialog(this);
//		indeterminateDialog.setTitle("Please wait");
//		String ocrEngineModeName = getOcrEngineModeName();
//		if (ocrEngineModeName.equals("Both")) {
//			indeterminateDialog.setMessage("Initializing Cube and Tesseract OCR engines for " + languageName + "...");
//		} else {
//			indeterminateDialog.setMessage("Initializing " + ocrEngineModeName + " OCR engine for " + languageName + "...");
//		}
//		indeterminateDialog.setCancelable(false);
//		indeterminateDialog.show();
//
////		if (handler != null) {
////			handler.quitSynchronously();     
////		}
//
//		// Start AsyncTask to install language data and init OCR
//		baseApi = new TessBaseAPI();
//		new OcrInitAsyncTask(this, baseApi, dialog, indeterminateDialog, languageCode, languageName, ocrEngineMode)
//		.execute(storageRoot.toString());
//	}
//	/** Finds the proper location on the SD card where we can save files. */
//	@SuppressLint("NewApi")
//	private File getStorageDirectory() {
//		//Log.d(TAG, "getStorageDirectory(): API level is " + Integer.valueOf(android.os.Build.VERSION.SDK_INT));
//
//		String state = null;
//		try {
//			state = Environment.getExternalStorageState();
//		} catch (RuntimeException e) {
//			Log.e(TAG, "Is the SD card visible?", e);
//			showErrorMessage("Error", "Required external storage (such as an SD card) is unavailable.");
//		}
//
//		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
//
//			// We can read and write the media
//			//    	if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) > 7) {
//			// For Android 2.2 and above
//
//			try {
//				return getExternalFilesDir(Environment.MEDIA_MOUNTED);
//			} catch (NullPointerException e) {
//				// We get an error here if the SD card is visible, but full
//				Log.e(TAG, "External storage is unavailable");
//				showErrorMessage("Error", "Required external storage (such as an SD card) is full or unavailable.");
//			}
//
//
//		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
//			// We can only read the media
//			Log.e(TAG, "External storage is read-only");
//			showErrorMessage("Error", "Required external storage (such as an SD card) is unavailable for data storage.");
//		} else {
//			// Something else is wrong. It may be one of many other states, but all we need
//			// to know is we can neither read nor write
//			Log.e(TAG, "External storage is unavailable");
//			showErrorMessage("Error", "Required external storage (such as an SD card) is unavailable.");
//		}
//		return null;
//	}
//	/**
//	 * Displays an error message dialog box to the user on the UI thread.
//	 * 
//	 * @param title The title for the dialog box
//	 * @param message The error message to be displayed
//	 */
//	void showErrorMessage(String title, String message) {
//		new AlertDialog.Builder(this)
//		.setTitle(title)
//		.setMessage(message)
//		.setOnCancelListener(new FinishListener(this))
//		.setPositiveButton( "Done", new FinishListener(this))
//		.show();
//	}
//	/**
//	 * A factory method to build the appropriate LuminanceSource object based on the format
//	 * of the preview buffers, as described by Camera.Parameters.
//	 *
//	 * @param data A preview frame.
//	 * @param width The width of the image.
//	 * @param height The height of the image.
//	 * @return A PlanarYUVLuminanceSource instance.
//	 */
//	public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
//		Rect rect = cropfinderView.getFramingRectInPreview();
//		if (rect == null) {
//			return null;
//		}
//		// Go ahead and assume it's YUV rather than die.
//		return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top,rect.width(), rect.height(), reverseImage);
//	}
//	//	public Bitmap toGrayscale(Bitmap bmpOriginal)
//	//	{        
//	//	    int width, height;
//	//	    height = bmpOriginal.getHeight();
//	//	    width = bmpOriginal.getWidth();    
//	//
//	//	    Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
//	//	    Canvas c = new Canvas(bmpGrayscale);
//	//	    Paint paint = new Paint();
//	//	    ColorMatrix cm = new ColorMatrix();
//	//	    cm.setSaturation(0);
//	//	    ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
//	//	    paint.setColorFilter(f);
//	//	    c.drawBitmap(bmpOriginal, 0, 0, paint);
//	//	    return bmpGrayscale;
//	//	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	/** Request the viewfinder to be invalidated. */
	void drawViewfinder() {
		cropfinderView.drawViewfinder();
	}
//	/**
//	 * Returns a string that represents which OCR engine(s) are currently set to be run.
//	 * 
//	 * @return OCR engine mode
//	 */
//	String getOcrEngineModeName() {
//		String ocrEngineModeName = "";
//		String[] ocrEngineModes = getResources().getStringArray(R.array.ocrenginemodes);
//		if (ocrEngineMode == TessBaseAPI.OEM_TESSERACT_ONLY) {
//			ocrEngineModeName = ocrEngineModes[0];
//		} else if (ocrEngineMode == TessBaseAPI.OEM_CUBE_ONLY) {
//			ocrEngineModeName = ocrEngineModes[1];
//		} else if (ocrEngineMode == TessBaseAPI.OEM_TESSERACT_CUBE_COMBINED) {
//			ocrEngineModeName = ocrEngineModes[2];
//		}
//		return ocrEngineModeName;
//	}
}
