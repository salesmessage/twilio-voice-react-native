package com.twiliovoicereactnative;

import java.net.URLDecoder;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationChannelGroupCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.twilio.voice.CallInvite;

import static com.twiliovoicereactnative.Constants.VOICE_CHANNEL_DEFAULT_IMPORTANCE;
import static com.twiliovoicereactnative.Constants.VOICE_CHANNEL_HIGH_IMPORTANCE;
import static com.twiliovoicereactnative.Constants.VOICE_CHANNEL_HIGH_IMPORTANCE_WITH_VIBRATION;
import static com.twiliovoicereactnative.Constants.VOICE_CHANNEL_LOW_IMPORTANCE;
import static com.twiliovoicereactnative.VoiceNotificationReceiver.constructMessage;

import com.twilio.voice.CancelledCallInvite;
import com.twiliovoicereactnative.CallRecordDatabase.CallRecord;

class NotificationUtility {
  private static final SDKLog logger = new SDKLog(VoiceNotificationReceiver.class);
  private static final SecureRandom secureRandom = new SecureRandom();

  public static int createNotificationIdentifier() {
    return (secureRandom.nextInt() & 0x7FFFFFFF) + 1; // prevent 0 as an id
  }

  public static Notification createIncomingCallNotification(@NonNull Context context,
                                                            @NonNull final CallRecord callRecord,
                                                            @NonNull final String channelImportance,
                                                            boolean fullScreenIntent) {
    CallInvite callInvite = callRecord.getCallInvite();

    Intent foregroundIntent = constructMessage(
      context,
      Constants.ACTION_FOREGROUND_AND_DEPRIORITIZE_INCOMING_CALL_NOTIFICATION,
      Objects.requireNonNull(VoiceApplicationProxy.getMainActivityClass()),
      callRecord.getUuid());
    PendingIntent piForegroundIntent = constructPendingIntentForActivity(context, foregroundIntent);

    Intent rejectIntent = constructMessage(
      context,
      Constants.ACTION_REJECT_CALL,
      VoiceNotificationReceiver.class,
      callRecord.getUuid());
    PendingIntent piRejectIntent = constructPendingIntentForReceiver(context, rejectIntent);

    Intent acceptIntent = constructMessage(
      context,
      Constants.ACTION_ACCEPT_CALL,
      Objects.requireNonNull(VoiceApplicationProxy.getMainActivityClass()),
      callRecord.getUuid());
    PendingIntent piAcceptIntent = constructPendingIntentForActivity(context, acceptIntent);

//    RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.custom_notification_incoming);
//    remoteViews.setTextViewText(R.id.notif_title, title);
//    remoteViews.setTextViewText(R.id.notif_content, getContentBanner(context));
//    remoteViews.setOnClickPendingIntent(R.id.button_answer, piAcceptIntent);
//    remoteViews.setOnClickPendingIntent(R.id.button_decline, piRejectIntent);

    Bundle extras = new Bundle();
    if (callInvite != null) {
      extras.putString("CALL_SID", callInvite.getCallSid());
    }

    NotificationCompat.Action answerAction = new NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_call,
            getActionText(context, R.string.accept, R.color.green),
            piAcceptIntent
    ).build();

    NotificationCompat.Action rejectAction = new NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_delete,
            getActionText(context, R.string.reject, R.color.red),
            piRejectIntent
    ).build();

    NotificationCompat.Builder builder = constructNotificationBuilder(context, channelImportance);
      builder
              .setSmallIcon(R.drawable.ic_call_white_24dp)
              .setContentTitle("Incoming call")
              .setContentText(getContentCallBanner(callInvite))
              .setExtras(extras)
              .setAutoCancel(true)
              .addAction(rejectAction)
              .addAction(answerAction)
              .setFullScreenIntent(piForegroundIntent, true)
              .setPriority(NotificationCompat.PRIORITY_HIGH)
              .setCategory(Notification.CATEGORY_CALL)
              .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
              .setContentIntent(piForegroundIntent);
//              .setSmallIcon(smallIconResId)
//        .setLargeIcon(icon)
//        .setContentTitle(title)
//        .setContentText(getContentBanner(context))
//        .setCategory(Notification.CATEGORY_CALL)
//        .setAutoCancel(true)
//        .setCustomContentView(remoteViews)
//        .setCustomBigContentView(remoteViews)
//        .setContentIntent(piForegroundIntent);
    if (fullScreenIntent) {
      builder.setFullScreenIntent(piForegroundIntent, true);
    }
    return builder.build();
  }

  public static Notification createCallAnsweredNotificationWithLowImportance(@NonNull Context context,
                                                                             @NonNull final CallRecord callRecord) {
    final int smallIconResId = getSmallIconResource(context);
    final String title = getDisplayName(callRecord.getCallInvite());

    CallInvite callInvite = callRecord.getCallInvite();

    Intent foregroundIntent = constructMessage(
      context,
      Constants.ACTION_PUSH_APP_TO_FOREGROUND,
      Objects.requireNonNull(VoiceApplicationProxy.getMainActivityClass()),
      callRecord.getUuid());
    PendingIntent pendingIntent = constructPendingIntentForActivity(context, foregroundIntent);

    Intent endCallIntent = constructMessage(
      context,
      Constants.ACTION_CALL_DISCONNECT,
      VoiceNotificationReceiver.class,
      callRecord.getUuid());
    PendingIntent piEndCallIntent = constructPendingIntentForReceiver(context, endCallIntent);

//    RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.custom_call_in_progress);
//    remoteViews.setTextViewText(R.id.make_call_text, getContentBanner(context));
//    remoteViews.setOnClickPendingIntent(R.id.end_call, piEndCallIntent);

    NotificationCompat.Action rejectAction = new NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_delete,
            getActionText(context, R.string.reject, R.color.red),
            piEndCallIntent
    ).build();

    Bundle extras = new Bundle();
    extras.putString("CALL_SID", callInvite.getCallSid());

    return constructNotificationBuilder(context, Constants.VOICE_CHANNEL_LOW_IMPORTANCE)
//      .setSmallIcon(smallIconResId)
//      .setContentTitle(title)
//      .setContentText(getContentBanner(context))
//      .setCategory(Notification.CATEGORY_CALL)
//      .setAutoCancel(false)
//      .setCustomContentView(remoteViews)
//      .setCustomBigContentView(remoteViews)
//      .setContentIntent(pendingIntent)
//      .setFullScreenIntent(pendingIntent, true)
            .setSmallIcon(R.drawable.ic_call_white_24dp)
            .setContentTitle("Call in progress")
            .setContentText("Show call details in the app")
            .setExtras(extras)
            .setAutoCancel(true)
            .addAction(rejectAction)
            .setFullScreenIntent(pendingIntent, true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(Notification.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
      .build();
  }

  public static Notification createOutgoingCallNotificationWithLowImportance(@NonNull Context context,
                                                                             @NonNull final CallRecord callRecord) {
    final int smallIconResId = getSmallIconResource(context);

    Intent foregroundIntent = constructMessage(
      context,
      Constants.ACTION_PUSH_APP_TO_FOREGROUND,
      Objects.requireNonNull(VoiceApplicationProxy.getMainActivityClass()),
      callRecord.getUuid());
    PendingIntent piForegroundIntent = constructPendingIntentForActivity(context, foregroundIntent);

    Intent endCallIntent = constructMessage(
      context,
      Constants.ACTION_CALL_DISCONNECT,
      VoiceNotificationReceiver.class,
      callRecord.getUuid());
    PendingIntent piEndCallIntent = constructPendingIntentForReceiver(context, endCallIntent);

//    RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.custom_call_in_progress);
//    remoteViews.setTextViewText(R.id.make_call_text, getContentBanner(context));
//    remoteViews.setOnClickPendingIntent(R.id.end_call, piEndCallIntent);

    NotificationCompat.Action rejectAction = new NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_delete,
            getActionText(context, R.string.reject, R.color.red),
            piEndCallIntent
    ).build();

    return constructNotificationBuilder(context, Constants.VOICE_CHANNEL_LOW_IMPORTANCE)
//      .setSmallIcon(smallIconResId)
//      .setContentText(getContentBanner(context))
//      .setCategory(Notification.CATEGORY_CALL)
//      .setCustomContentView(remoteViews)
//      .setCustomBigContentView(remoteViews)
//      .setContentIntent(piForegroundIntent)
//      .setFullScreenIntent(piForegroundIntent, true)
            .setAutoCancel(false)
            .setSmallIcon(R.drawable.ic_call_white_24dp)
            .setContentTitle("Call in progress")
            .setContentText("Show call details in the app")
            .addAction(rejectAction)
            .setFullScreenIntent(piForegroundIntent, true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(Notification.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(piForegroundIntent)
      .build();
  }

  public static Notification createMissedCallNotificationWithLowImportance(@NonNull Context context,
                                                                             @NonNull final CallRecord callRecord, int missedCallsValue) {

    CancelledCallInvite cancelledCallInvite = Objects.requireNonNull(callRecord.getCancelledCallInvite());

//    Intent foregroundIntent = constructMessage(
//            context,
//            Constants.ACTION_PUSH_APP_TO_FOREGROUND_FOR_MISSED_CALL,
//            Objects.requireNonNull(VoiceApplicationProxy.getMainActivityClass()),
//            callRecord.getUuid());

    String inbox_data = "{}";
    String contact_data = "{}";

    try {
      inbox_data = cancelledCallInvite.getCustomParameters().get("inbox").toString();
      contact_data = cancelledCallInvite.getCustomParameters().get("contact").toString();
    } catch (Exception e) {
      e.printStackTrace();
    }

    Intent foregroundIntent = new Intent(context.getApplicationContext(),
            Objects.requireNonNull(VoiceApplicationProxy.getMainActivityClass()));

    Random generator = new Random();

    foregroundIntent.setAction(Constants.ACTION_PUSH_APP_TO_FOREGROUND_FOR_MISSED_CALL);
    foregroundIntent.putExtra(Constants.MSG_KEY_UUID, callRecord.getUuid());
    foregroundIntent.putExtra("INBOX_DATA", inbox_data);
    foregroundIntent.putExtra("CONTACT_DATA", contact_data);

//    PendingIntent pendingIntent = constructPendingIntentForActivity(context, foregroundIntent);

    PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            generator.nextInt(),
            foregroundIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

    Bundle extras = new Bundle();
    extras.putString("CALL_SID", cancelledCallInvite.getCallSid());

    String callerInfo = cancelledCallInvite.getFrom();
    Map<String, String> customParameters = cancelledCallInvite.getCustomParameters();
    if (customParameters.get("CallerName") != null) {
      callerInfo = URLDecoder.decode(customParameters.get("CallerName").replaceAll("\\+", "%20"));
    }

    return constructNotificationBuilder(context, Constants.VOICE_CHANNEL_LOW_IMPORTANCE)
            .setSmallIcon(R.drawable.ic_call_missed_white_24dp)
            .setContentTitle("Missed call")
            .setContentText("Show call details in the app")
            .setExtras(extras)
            .setAutoCancel(true)
            .setFullScreenIntent(pendingIntent, true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(Notification.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setContentTitle(missedCallsValue == 1 ? "Missed call" : missedCallsValue + " Missed calls")
            .setContentText("from: " + callerInfo)
            .build();
  }

  //
  public static void createNotificationChannels(@NonNull Context context) {
    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
    notificationManager.createNotificationChannelGroup(
      new NotificationChannelGroupCompat.Builder(Constants.VOICE_CHANNEL_GROUP)
        .setName("Twilio Voice").build());

    for (String channelId:
      new String[]{
        VOICE_CHANNEL_HIGH_IMPORTANCE,
        VOICE_CHANNEL_HIGH_IMPORTANCE_WITH_VIBRATION,
        VOICE_CHANNEL_DEFAULT_IMPORTANCE,
        VOICE_CHANNEL_LOW_IMPORTANCE}) {
      notificationManager.createNotificationChannel(
        createNotificationChannel(context, channelId));
    }
  }

  public static void destroyNotificationChannels(@NonNull Context context) {
    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
    notificationManager.deleteNotificationChannelGroup(Constants.VOICE_CHANNEL_GROUP);
  }

  private static NotificationChannelCompat createNotificationChannel(@NonNull Context context,
                                                                     @NonNull final String voiceChannelId) {
    final int notificationImportance = getChannelImportance(voiceChannelId);
    long[] pattern = new long[]{
            0, 300, 1000, 50,
            0, 300, 1000, 50,
            0, 300, 1000, 50,
            0, 300, 1000, 50,
            0, 300, 1000, 50,
            0, 300, 1000, 50,
            0, 300, 1000, 50,
            0, 300, 1000, 50,
            0, 300, 1000, 50,
            0, 300, 1000, 50,
            0, 300, 1000, 50,
            0, 300, 1000, 50,
            0, 300, 1000, 50,
            0, 300, 1000, 50,
            0, 300, 1000, 50,
            0, 300, 1000, 50,
            0, 300, 1000, 50,
            0, 300, 1000, 50};
    NotificationChannelCompat.Builder voiceChannelBuilder =
      new NotificationChannelCompat.Builder(voiceChannelId, notificationImportance)
        .setName("Primary Voice Channel")
        .setLightColor(Color.GREEN)
        .setGroup(Constants.VOICE_CHANNEL_GROUP);

    if (voiceChannelId == VOICE_CHANNEL_HIGH_IMPORTANCE_WITH_VIBRATION) {
      voiceChannelBuilder.setVibrationPattern(pattern);
    }

    // low-importance notifications don't have sound
    if (!Constants.VOICE_CHANNEL_LOW_IMPORTANCE.equals(voiceChannelId)) {
      // set audio attributes for channel
      Uri soundUri = provideResourceSilent_wav(context);
      AudioAttributes audioAttributes = new AudioAttributes.Builder()
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
        .build();
      voiceChannelBuilder.setSound(soundUri, audioAttributes);
    }
    return voiceChannelBuilder.build();
  }

  private static int getChannelImportance(@NonNull final String voiceChannel) {
    final Map<String, Integer> importanceMapping = Map.of(
            Constants.VOICE_CHANNEL_HIGH_IMPORTANCE_WITH_VIBRATION, NotificationManagerCompat.IMPORTANCE_HIGH,
      Constants.VOICE_CHANNEL_HIGH_IMPORTANCE, NotificationManagerCompat.IMPORTANCE_HIGH,
      Constants.VOICE_CHANNEL_DEFAULT_IMPORTANCE, NotificationManagerCompat.IMPORTANCE_DEFAULT,
      Constants.VOICE_CHANNEL_LOW_IMPORTANCE, NotificationManagerCompat.IMPORTANCE_LOW);
    return Objects.requireNonNull(importanceMapping.get(voiceChannel));
  }

  private static String getChannel(@NonNull Context context, @NonNull final String voiceChannelId) {
    // construct channel if it has not been created
    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

    if (null == notificationManager.getNotificationChannel(voiceChannelId)) {
      createNotificationChannels(context);
    }
    return voiceChannelId;
  }


  private static String getDisplayName(CallInvite callInvite) {
    try {
      String title = callInvite.getFrom();
      Map<String, String> customParameters = callInvite.getCustomParameters();
      // If "displayName" is passed as a custom parameter in the TwiML application,
      // it will be used as the caller name.
      if (customParameters.get(Constants.DISPLAY_NAME) != null) {
        title = URLDecoder.decode(customParameters.get(Constants.DISPLAY_NAME).replaceAll("\\+", "%20"));
      }

      if (customParameters.get("CallerName") != null) {
        title = URLDecoder.decode(customParameters.get("CallerName").replaceAll("\\+", "%20"));
      }

      return title;
    } catch (Exception e) {
      e.printStackTrace();
      return "Unknown caller";
    }
  }

  private static String getContentBanner(@NonNull Context context) {
    return context.getString(R.string.app_name) + Constants.NOTIFICATION_BANNER;
  }

  private static PendingIntent constructPendingIntentForActivity(@NonNull Context context,
                                                                 @NonNull final Intent intent) {
    return PendingIntent.getActivity(
      context.getApplicationContext(),
      0,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
  }

  private static PendingIntent constructPendingIntentForReceiver(@NonNull Context context,
                                                                 @NonNull final Intent intent) {
    return PendingIntent.getBroadcast(
      context.getApplicationContext(),
      0,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
  }

  private static NotificationCompat.Builder constructNotificationBuilder(
    @NonNull Context context,
    @NonNull final String channelImportance) {
    return new NotificationCompat.Builder(context,
        getChannel(context.getApplicationContext(),
          channelImportance));
  }

  @SuppressLint("DiscouragedApi")
  private static int getSmallIconResource(@NonNull Context context) {
    return context.getResources().getIdentifier(
      "ic_notification",
      "drawable",
      context.getPackageName());
  }

  private static Bitmap constructBitmap(@NonNull Context context, final int resId) {
    return BitmapFactory.decodeResource(context.getResources(), resId);
  }

  static Uri provideResourceSilent_wav(@NonNull Context context) {
    return provideResource(context, R.raw.silent);
  }

  private static Uri provideResource(@NonNull Context context, int id) {
    return (new Uri.Builder()
      .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
      .authority(context.getResources().getResourcePackageName(id))
      .appendPath(String.valueOf(id))
    ).build();
  }


  private static String getContentCallBanner(CallInvite callInvite) {
    return getDisplayName(callInvite) + " is calling";
  }

  private static Spannable getActionText(Context context, @StringRes int stringRes, @ColorRes int colorRes) {
    Spannable spannable = new SpannableString(context.getText(stringRes));
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
      spannable.setSpan(
              new ForegroundColorSpan(context.getColor(colorRes)),
              0,
              spannable.length(),
              0
      );
    }
    return spannable;
  }
}
