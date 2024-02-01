package com.twiliovoicereactnative;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.twilio.audioswitch.AudioSwitch;

import android.media.RingtoneManager;
import android.media.Ringtone;

import java.io.IOException;

public class MediaPlayerManager {
    private boolean playing = false;
    private Ringtone ringtone = null;
    public final int DISCONNECT_WAV;
    public final int INCOMING_WAV;
    public final int OUTGOING_WAV;
    public final int RINGTONE_WAV;
    private static final String TAG = MediaPlayerManager.class.getSimpleName();
    private final SoundPool soundPool;
    private final AudioSwitch audioSwitch;
    private int activeStream;
    private static MediaPlayerManager instance;
    private AudioManager audioManager;
    private static Context _context = null;

    private MediaPlayerManager(Context context) {
        Uri ringtoneSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringtone = RingtoneManager.getRingtone(context, ringtoneSound);

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
      soundPool = (new SoundPool.Builder())
        .setMaxStreams(2)
        .setAudioAttributes(
          new AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
            .build())
        .build();
      audioSwitch = AudioSwitchManager.getInstance(context).getAudioSwitch();
      activeStream = 0;
      DISCONNECT_WAV = soundPool.load(context, R.raw.disconnect, 1);
      INCOMING_WAV = soundPool.load(context, R.raw.incoming, 1);
      OUTGOING_WAV = soundPool.load(context, R.raw.outgoing, 1);
      RINGTONE_WAV = soundPool.load(context, R.raw.ringtone, 1);

      AudioAttributes alarmAttribute = new AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ALARM)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build();
        if (ringtone != null) {
            ringtone.setAudioAttributes(alarmAttribute);
        }
    }

    public static MediaPlayerManager getInstance(Context context) {
        _context = context;
        if (instance == null) {
            instance = new MediaPlayerManager(context);
        }
        return instance;
    }

    public void play(final int soundId, Boolean enableSpeakerphone, Boolean isAppVisible) {
        ringtone.play();
        playing = true;

        if (enableSpeakerphone) {
            audioManager.setSpeakerphoneOn(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (isBluetoothHeadsetConnected()) {
                Log.i(TAG, "Switching to bluetooth device");
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                audioManager.startBluetoothSco();
                audioManager.setBluetoothScoOn(true);
            }
        }
    }

    public void stop() {
        try {
            ringtone.stop();
            playing = false;
            soundPool.stop(activeStream);
          activeStream = 0;
        } catch (Exception e) {
            Log.e(TAG, "Failed from stopRinging");
        }
    }

    public void release() {
      soundPool.release();
      instance = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private boolean isBluetoothHeadsetConnected() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean hasBluetoothPermission = ActivityCompat.checkSelfPermission(_context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;

        Log.i(TAG, "hasBluetoothPermission: " + hasBluetoothPermission);

        try {
            return bluetoothAdapter != null && bluetoothAdapter.isEnabled()
                    && bluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothAdapter.STATE_CONNECTED;
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }

        return false;
    }
}
