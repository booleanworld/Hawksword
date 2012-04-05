/*
 * Copyright (C) 2010 ZXing authors
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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;

import com.bw.hawksword.ocr.R;


/**
 * Manages beeps and vibrations for {@link CaptureActivity}.
 * 
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing/
 */
public final class BeepManager {

  private static final String TAG = BeepManager.class.getSimpleName();
  
  private  SoundPool mSoundPool;
  private  HashMap mSoundPoolMap;
  private  AudioManager  mAudioManager;
  private  Context mContext;
  private AudioManager audioService;
  SharedPreferences prefs;
  
  public void initSounds(Context theContext,Activity activity) {
      mContext = theContext;
      mSoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
      mSoundPoolMap = new HashMap();
      mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
      audioService = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
      prefs = PreferenceManager.getDefaultSharedPreferences(activity);
  }

  public void addSound(int index, int SoundID) {
      mSoundPoolMap.put(index, mSoundPool.load(mContext, SoundID, 1));
  }

  public void playSound(int index) {
	  boolean shouldPlayBeep = prefs.getBoolean(PreferencesActivity.KEY_PLAY_BEEP, CaptureActivity.DEFAULT_TOGGLE_BEEP);
	    if ((shouldPlayBeep) && !(audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL)) {
	      // play beep
	    	float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
	        streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	        mSoundPool.play(index, streamVolume, streamVolume, 1, 0, 1f);
	      }      
  }

  public void playLoopedSound(int index) {
      float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
      streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
      mSoundPool.play(index, streamVolume, streamVolume, 1, -1, 1f);
  }

  
  
/*
  private static final float BEEP_VOLUME = 0.10f;

 
  private MediaPlayer mediaPlayer;
  private boolean playBeep;

  public BeepManager(Activity activity) {
    this.activity = activity;
    this.mediaPlayer = null;
    updatePrefs();
  }

  public void updatePrefs() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
    playBeep = shouldBeep(prefs, activity);
    if (playBeep && mediaPlayer == null) {
      // The volume on STREAM_SYSTEM is not adjustable, and users found it too loud,
      // so we now play on the music stream.
      activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
      mediaPlayer = buildMediaPlayer(activity);
    }
  }

  public void playBeepSoundAndVibrate() {
    if (playBeep && mediaPlayer != null) {
      mediaPlayer.start();
    }
  }

  private static boolean shouldBeep(SharedPreferences prefs, Context activity) {
    boolean shouldPlayBeep = prefs.getBoolean(PreferencesActivity.KEY_PLAY_BEEP, CaptureActivity.DEFAULT_TOGGLE_BEEP);
    if (shouldPlayBeep) {
      // See if sound settings overrides this
      AudioManager audioService = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
      if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
        shouldPlayBeep = false;
      }
    }
    return shouldPlayBeep;
  }

  private static MediaPlayer buildMediaPlayer(Context activity) {
    MediaPlayer mediaPlayer = new MediaPlayer();
    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    // When the beep has finished playing, rewind to queue up another one.
    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      public void onCompletion(MediaPlayer player) {
        player.seekTo(0);
      }
    });

    AssetFileDescriptor file = activity.getResources().openRawResourceFd(R.raw.beep);
    try {
      mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
      file.close();
      mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
      mediaPlayer.prepare();
    } catch (IOException ioe) {
      Log.w(TAG, ioe);
      mediaPlayer = null;
    }
    return mediaPlayer;
  }
*/
  
  
  
}
