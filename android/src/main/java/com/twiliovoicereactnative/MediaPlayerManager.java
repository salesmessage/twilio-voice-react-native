package com.twiliovoicereactnative;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.SoundPool;

import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;


import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.util.HashMap;
import java.util.Map;

class MediaPlayerManager {
  public enum SoundTable {
    INCOMING,
    OUTGOING,
    DISCONNECT,
    RINGTONE
  }
  private final SoundPool soundPool;
  private final Map<SoundTable, Integer> soundMap;
  private int activeStream;

  private AudioManager audioManager;
  private Vibrator vibe = null;
  private Ringtone ringtone = null;
  private boolean playing = false;
  private static final SDKLog logger = new SDKLog(MediaPlayerManager.class);
  private static Context _context = null;

  MediaPlayerManager(Context context) {
    _context = context;
    Uri ringtoneSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
    audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    ringtone = RingtoneManager.getRingtone(context, ringtoneSound);

    soundPool = (new SoundPool.Builder())
      .setMaxStreams(2)
      .setAudioAttributes(
        new AudioAttributes.Builder()
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
          .build())
      .build();
    activeStream = 0;
    soundMap = new HashMap<>();
    soundMap.put(SoundTable.INCOMING, soundPool.load(context, R.raw.incoming, 1));
    soundMap.put(SoundTable.OUTGOING, soundPool.load(context, R.raw.outgoing, 1));
    soundMap.put(SoundTable.DISCONNECT, soundPool.load(context, R.raw.disconnect, 1));
    soundMap.put(SoundTable.RINGTONE, soundPool.load(context, R.raw.ringtone, 1));
  }

  public boolean isRingerModeVibrate() {
    return audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE;
  }

  public void play() {
    try {
      if (isRingerModeVibrate()) {
        long[] pattern = {0, 300, 1000};
        vibe.vibrate(pattern, 0);
        return;
      }
    } catch (Exception e) {

      e.printStackTrace();
      return;
    }

    try {
      if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL && !playing && ringtone != null) {
        logger.debug("play started...");
        ringtone.play();
        playing = true;
        audioManager.setSpeakerphoneOn(true);

        enableBluetooth();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  public void stop() {
    logger.debug("play stopped");
    try {
      if (ringtone.isPlaying() && ringtone != null) {
        ringtone.stop();
        playing = false;
      }

      vibe.cancel();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void finalize() throws Throwable {
    soundPool.release();
    super.finalize();
  }

  @RequiresApi(api = Build.VERSION_CODES.S)
  private boolean isBluetoothHeadsetConnected() {
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    boolean hasBluetoothPermission = ActivityCompat.checkSelfPermission(_context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;

    try {
      return bluetoothAdapter != null && bluetoothAdapter.isEnabled()
              && bluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothAdapter.STATE_CONNECTED;
    } catch (NullPointerException ex) {
      ex.printStackTrace();
    }

    return false;
  }

  public void enableBluetooth() {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (isBluetoothHeadsetConnected()) {
          logger.debug("Switching to bluetooth device");
          audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
          audioManager.startBluetoothSco();
          audioManager.setBluetoothScoOn(true);
        }
      }
    } catch (SecurityException e) {
      logger.error("Could not enable bluetooth");
    }
  }
}
