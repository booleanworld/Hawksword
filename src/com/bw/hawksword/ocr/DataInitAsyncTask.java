/*
 * Copyright 2011 Robert Theis
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.xeustechnologies.jtar.TarEntry;
import org.xeustechnologies.jtar.TarInputStream;



import com.googlecode.tesseract.android.TessBaseAPI;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * Installs the language data required for OCR, and initializes the OCR engine using a background 
 * thread.
 */
final class DataInitAsyncTask extends AsyncTask<String, String, Boolean> {
  private static final String TAG = DataInitAsyncTask.class.getSimpleName();
  public static boolean status;
  /** Suffixes of required data files for Cube. */
  private static final String[] CUBE_DATA_FILES = {
    "eng.traineddata",
    "stop_words",
    "primary-index",
    "secondary-index",
    "Types",
    "wiktionary"
  };

  private SplashScreen activity;
  private Context context;
  private ProgressBar progressBar;
  //private TessBaseAPI baseApi;
  //private ProgressDialog dialog;
  //private final String languageCode;
  //private String languageName;
  //private int ocrEngineMode;

  /**
   * AsyncTask to asynchronously download data and initialize Tesseract.
   * 
   * @param activity
   *          The calling activity
   * @param baseApi
   *          API to the OCR engine
   * @param dialog
   *          Dialog box with thermometer progress indicator
   * @param indeterminateDialog
   *          Dialog box with indeterminate progress indicator
   * @param languageCode
   *          ISO 639-2 OCR language code
   * @param languageName
   *          Name of the OCR language, for example, "English"
   * @param ocrEngineMode
   *          Whether to use Tesseract, Cube, or both
   */
  DataInitAsyncTask(SplashScreen activity) {
    this.activity = activity;
    this.context = activity.getBaseContext();
  }

  @Override
  protected void onPreExecute() {
    super.onPreExecute();
    status = false;
  }

  /**
   * In background thread, perform required setup, and request initialization of
   * the OCR engine.
   * 
   * @param params
   *          [0] Pathname for the directory for storing language data files to the SD card
   */
  protected Boolean doInBackground(String... params) {
    // Check whether we need Cube data or Tesseract data.
    // Example Cube data filename: "tesseract-ocr-3.01.eng.tar"
	  // http://ocr-dictionary.googlecode.com/files/dictionary_data.tar.gz
    // Example Tesseract data filename: "eng.traineddata"
    String destinationFilenameBase = "eng.traineddata";
    //boolean isCubeSupported = false;
    for (String s : CaptureActivity.CUBE_SUPPORTED_LANGUAGES) {
      if (s.equals("eng")) {
      //  isCubeSupported = true;
        destinationFilenameBase = "tessdata";
      }
    }
    // Check for, and create if necessary, folder to hold model data
    String destinationDirBase = params[0]; // The storage directory, minus the
                                           // "tessdata" subdirectory
    File tessdataDir = new File(destinationDirBase + File.separator + "tessdata");
    if (!tessdataDir.exists() && !tessdataDir.mkdirs()) {
      Log.e(TAG, "Couldn't make directory " + tessdataDir);
      return false;
    }
    // Create a reference to the file to save the download in
    File downloadFile = new File(tessdataDir, destinationFilenameBase);

    // Check if an incomplete download is present. If a *.download file is there, delete it and
    // any (possibly half-unzipped) Tesseract and Cube data files that may be there.
    File incomplete = new File(tessdataDir, destinationFilenameBase + ".download");
    if (incomplete.exists()) {
      incomplete.delete();
    }

    // Check whether all Cube data files have already been installed
    boolean isAllCubeDataInstalled = false;
    //if (isCubeSupported) {
      boolean isAFileMissing = false;
      File dataFile;
      for (String s : CUBE_DATA_FILES) {
        dataFile = new File(tessdataDir.toString() + File.separator + s);
        if (!dataFile.exists()) {
          isAFileMissing = true;
        }
      }
      isAllCubeDataInstalled = !isAFileMissing;
    //}

    // If language data files are not present, install them
    boolean installSuccess = false;
//    boolean download = false;
    //Download Data....
    //1) Check, If files are present or not.    
    if(!isAllCubeDataInstalled){
      Log.d(TAG, "Language data for eng not found in " + tessdataDir.toString());
      deleteCubeDataFiles(tessdataDir);
    
     // if (!installSuccess) {
    	  Log.d(TAG, "Checking for language data (" + destinationFilenameBase + ".zip) in application assets...");
    	        // Check for a file like "eng.traineddata.zip" or "tesseract-ocr-3.01.eng.tar.zip"
    	        try {
					installSuccess = installFromAssets(destinationFilenameBase + ".zip", tessdataDir, downloadFile);
					installSuccess = true;
					
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return false;
				}
    }
    installSuccess = true;
	    return installSuccess;
 }
    
  /**
  * Install a file from application assets to device external storage.
  *
  * @param sourceFilename
  * File in assets to install
  * @param modelRoot
  * Directory on SD card to install the file to
  * @param destinationFile
  * File name for destination, excluding path
  * @return True if installZipFromAssets returns true
  * @throws IOException
  */
    private boolean installFromAssets(String sourceFilename, File modelRoot,
        File destinationFile) throws IOException {
      String extension = sourceFilename.substring(sourceFilename.lastIndexOf('.'),
          sourceFilename.length());
      try {
        if (extension.equals(".zip")) {
          return installZipFromAssets(sourceFilename, modelRoot, destinationFile);
        } else {
          throw new IllegalArgumentException("Extension " + extension
              + " is unsupported.");
        }
      } catch (FileNotFoundException e) {
        Log.d(TAG, "Language not packaged in application assets.");
      }
      return false;
    }
    /**
    * Unzip the given Zip file, located in application assets, into the given
    * destination file.
    *
    * @param sourceFilename
    * Name of the file in assets
    * @param destinationDir
    * Directory to save the destination file in
    * @param destinationFile
    * File to unzip into, excluding path
    * @return
    * @throws IOException
    * @throws FileNotFoundException
    */
      private boolean installZipFromAssets(String sourceFilename,
          File destinationDir, File destinationFile) throws IOException,
          FileNotFoundException {
        // Attempt to open the zip archive
        publishProgress("Uncompressing data for English...", "0");
        ZipInputStream inputStream = new ZipInputStream(context.getAssets().open(sourceFilename));

        long totalSize = 44479408, totalUncompression = 0;

        // Loop through all the files and folders in the zip archive (but there should just be one)
        for (ZipEntry entry = inputStream.getNextEntry(); entry != null; entry = inputStream
            .getNextEntry()) {
          destinationFile = new File(destinationDir, entry.getName());

          if (entry.isDirectory()) {
            destinationFile.mkdirs();
          } else {
            FileOutputStream outputStream = new FileOutputStream(destinationFile);
            final int BUFFER = 8192;

            // Buffer the output to the file
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream, BUFFER);

            // Write the contents
            int count = 0;
            Integer percentComplete = 0;
            Integer percentCompleteLast = 0;
            byte[] data = new byte[BUFFER];
            while ((count = inputStream.read(data, 0, BUFFER)) != -1) {
              bufferedOutputStream.write(data, 0, count);
              //unzippedSize += count;
              totalUncompression += count;
              percentComplete = (int) ((totalUncompression * 100 / totalSize));
              if(totalSize/100 < totalUncompression) {
              }
//              if (percentComplete > percentCompleteLast) {
//                publishProgress("Uncompressing data for English...",
//                    percentComplete.toString(), "0");
//                percentCompleteLast = percentComplete;
//              }
            }
            bufferedOutputStream.close();
          }
          inputStream.closeEntry();
        }
        inputStream.close();
        return true;
      }
  /**
   * Delete any existing data files for Cube that are present in the given directory. Files may be 
   * partially uncompressed files left over from a failed install, or pre-v3.01 traineddata files.
   * 
   * @param tessdataDir
   *          Directory to delete the files from
   */
  private void deleteCubeDataFiles(File tessdataDir) {
    File badFile;
    for (String s : CUBE_DATA_FILES) {
      badFile = new File(tessdataDir.toString() + File.separator + "eng" + s);
      if (badFile.exists()) {
        Log.d(TAG, "Deleting existing file " + badFile.toString());
        badFile.delete();
      }
      badFile = new File(tessdataDir.toString() + File.separator + "tesseract-ocr-3.01.eng.tar");
      if (badFile.exists()) {
        Log.d(TAG, "Deleting existing file " + badFile.toString());
        badFile.delete();
      }
    }
  }

  /**
   * Update the dialog box with the latest incremental progress.
   * 
   * @param message
   *          [0] Text to be displayed
   * @param message
   *          [1] Numeric value for the progress
   */
  @Override
  protected void onProgressUpdate(String... message) {
    super.onProgressUpdate(message);
    int percentComplete = 0;

    percentComplete = Integer.parseInt(message[1]);
    //dialog.setMessage(message[0]);
    //dialog.setProgress(percentComplete);
    //dialog.show();
  }

  @Override
  protected void onPostExecute(Boolean result) {
    super.onPostExecute(result);
    CaptureActivity.fileCheck = true;
    if (result) {
    	status = result;
    } else {
    	Toast.makeText(context, "Network is unreachable - cannot download language data. "
          + "Please enable network access and restart this app.", Toast.LENGTH_LONG).show();
    }
  }
}