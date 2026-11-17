package com.twiliovoicereactnative;

import android.media.AudioManager;

import androidx.annotation.Nullable;

/**
 * SMR-7651: hands the audio focus changes of the call to the app. During a call the focus is held by the
 * AudioSwitch, and Android reports a focus change only to the holder of the request: an app that requested focus
 * itself would take it from the call. The app sets a listener here to see a GSM call or another app taking the
 * audio, and the focus coming back.
 */
public final class AudioFocusChangeRelay {
  @Nullable
  private static volatile AudioManager.OnAudioFocusChangeListener listener;

  private AudioFocusChangeRelay() {}

  /** Null removes the listener. */
  public static void setListener(@Nullable AudioManager.OnAudioFocusChangeListener newListener) {
    listener = newListener;
  }

  static void onAudioFocusChange(int focusChange) {
    final AudioManager.OnAudioFocusChangeListener current = listener;
    if (current != null) {
      current.onAudioFocusChange(focusChange);
    }
  }
}
