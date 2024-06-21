package com.twiliovoicereactnative;

import java.net.URLDecoder;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Objects;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
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
import androidx.core.app.Person;

import com.twilio.voice.CallInvite;

import static com.twiliovoicereactnative.Constants.VOICE_CHANNEL_DEFAULT_IMPORTANCE;
import static com.twiliovoicereactnative.Constants.VOICE_CHANNEL_HIGH_IMPORTANCE;
import static com.twiliovoicereactnative.Constants.VOICE_CHANNEL_LOW_IMPORTANCE;
import static com.twiliovoicereactnative.VoiceService.constructMessage;

import com.twiliovoicereactnative.CallRecordDatabase.CallRecord;

class NotificationUtility {
  private static final SecureRandom secureRandom = new SecureRandom();
  private static class NotificationResource {
    private final Context ctx;
    private final String smallIconId;
    private final String contentTextId;
    public NotificationResource(Context context, String smallIconId, String contentTextId) {
      ctx = context;
      this.smallIconId = smallIconId;
      this.contentTextId = contentTextId;
    }
    int getSmallIconId() {
      return getDrawableResourceId(smallIconId);
    }
    public String getContent(CallInvite callInvite) {
      final String from = (null != callInvite) ? getDisplayName(callInvite) : "";
      return ctx.getString(getTextResourceId(contentTextId))
        .replaceAll("\\$\\{from\\}", from);
    }
    public String getContent(String to) {
      return ctx.getString(getTextResourceId(contentTextId))
        .replaceAll("\\$\\{to\\}", to);
    }

    @SuppressLint("DiscouragedApi")
    private int getDrawableResourceId(String id) {
      return ctx.getResources().getIdentifier(id, "drawable", ctx.getPackageName());
    }
    @SuppressLint("DiscouragedApi")
    private int getTextResourceId(String id) {
      return ctx.getResources().getIdentifier(id, "string", ctx.getPackageName());
    }
    private static String getDisplayName(@NonNull CallInvite callInvite) {
      final String title = callInvite.getFrom();
      if (title.startsWith("client:")) {
        return title.replaceFirst("client:", "");
      }
      return title;
    }
  }

  public static int createNotificationIdentifier() {
    return (secureRandom.nextInt() & 0x7FFFFFFF) + 1; // prevent 0 as an id
  }

  public static Notification createIncomingCallNotification(@NonNull Context context,
                                                            @NonNull final CallRecord callRecord,
                                                            @NonNull final String channelImportance) {
    final NotificationResource notificationResource = new NotificationResource(
      context,
      "incoming_call_small_icon",
      "incoming_call_caller_name_text");

    final Person incomingCaller = new Person.Builder()
      .setName(notificationResource.getContent(callRecord.getCallInvite()))
      .build();

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
      VoiceService.class,
      callRecord.getUuid());
    PendingIntent piRejectIntent = constructPendingIntentForService(context, rejectIntent);

    Intent acceptIntent = constructMessage(
      context,
      Constants.ACTION_ACCEPT_CALL,
      Objects.requireNonNull(VoiceApplicationProxy.getMainActivityClass()),
      callRecord.getUuid());
    PendingIntent piAcceptIntent = constructPendingIntentForActivity(context, acceptIntent);


    NotificationCompat.Action answerAction = new NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_call,
            getActionText(context, R.string.answer, R.color.colorGreen),
            piAcceptIntent
    ).build();

    NotificationCompat.Action rejectAction = new NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_delete,
            getActionText(context, R.string.decline, R.color.colorRed),
            piRejectIntent
    ).build();

    NotificationCompat.Builder builder = constructNotificationBuilder(context, channelImportance);
    builder
            .setSmallIcon(R.drawable.ic_call_white_24dp)
            .setContentTitle("Incoming call")
            .setContentText(getContentCallBanner(callInvite))
            .setAutoCancel(true)
            .addAction(rejectAction)
            .addAction(answerAction)
            .setFullScreenIntent(piForegroundIntent, true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(Notification.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(piForegroundIntent);
    return builder.build();

//      .setSmallIcon(R.drawable.ic_call_white_24dp)
//      .setCategory(Notification.CATEGORY_CALL)
//      .setAutoCancel(true)
//      .setContentIntent(piForegroundIntent)
//      .setFullScreenIntent(piForegroundIntent, true)
//      .addPerson(incomingCaller)
//      .setStyle(NotificationCompat.CallStyle.forIncomingCall(
//        incomingCaller, piRejectIntent, piAcceptIntent))
//      .build();
  }

  public static Notification createCallAnsweredNotificationWithLowImportance(@NonNull Context context,
                                                                             @NonNull final CallRecord callRecord) {
    final NotificationResource notificationResource = new NotificationResource(
      context,
      "answered_call_small_icon",
      "answered_call_caller_name_text");

    final Person activeCaller = new Person.Builder()
      .setName(notificationResource.getContent(callRecord.getCallInvite()))
      .build();

    Intent foregroundIntent = constructMessage(
      context,
      Constants.ACTION_PUSH_APP_TO_FOREGROUND,
      Objects.requireNonNull(VoiceApplicationProxy.getMainActivityClass()),
      callRecord.getUuid());
    PendingIntent piForegroundIntent = constructPendingIntentForActivity(context, foregroundIntent);

    Intent endCallIntent = constructMessage(
      context,
      Constants.ACTION_CALL_DISCONNECT,
      VoiceService.class,
      callRecord.getUuid());
    PendingIntent piEndCallIntent = constructPendingIntentForService(context, endCallIntent);

    return constructNotificationBuilder(context, Constants.VOICE_CHANNEL_LOW_IMPORTANCE)
      .setSmallIcon(notificationResource.getSmallIconId())
      .setCategory(Notification.CATEGORY_CALL)
      .setAutoCancel(false)
      .setContentIntent(piForegroundIntent)
      .setFullScreenIntent(piForegroundIntent, true)
      .setOngoing(true)
      .addPerson(activeCaller)
      .setStyle(NotificationCompat.CallStyle.forOngoingCall(activeCaller, piEndCallIntent))
      .build();
  }

  public static Notification createOutgoingCallNotificationWithLowImportance(@NonNull Context context,
                                                                             @NonNull final CallRecord callRecord) {
    final NotificationResource notificationResource = new NotificationResource(
      context,
      "outgoing_call_small_icon",
      "outgoing_call_caller_name_text");

    final Person activeCaller = new Person.Builder()
      .setName(notificationResource.getContent(callRecord.getCallRecipient()))
      .build();

    Intent foregroundIntent = constructMessage(
      context,
      Constants.ACTION_PUSH_APP_TO_FOREGROUND,
      Objects.requireNonNull(VoiceApplicationProxy.getMainActivityClass()),
      callRecord.getUuid());
    PendingIntent piForegroundIntent = constructPendingIntentForActivity(context, foregroundIntent);

    Intent endCallIntent = constructMessage(
      context,
      Constants.ACTION_CALL_DISCONNECT,
      VoiceService.class,
      callRecord.getUuid());
    PendingIntent piEndCallIntent = constructPendingIntentForService(context, endCallIntent);

    return constructNotificationBuilder(context, Constants.VOICE_CHANNEL_LOW_IMPORTANCE)
      .setSmallIcon(notificationResource.getSmallIconId())
      .setCategory(Notification.CATEGORY_CALL)
      .setAutoCancel(false)
      .setContentIntent(piForegroundIntent)
      .setFullScreenIntent(piForegroundIntent, true)
      .setOngoing(true)
      .addPerson(activeCaller)
      .setStyle(NotificationCompat.CallStyle.forOngoingCall(activeCaller, piEndCallIntent))
      .build();
  }

  public static void createNotificationChannels(@NonNull Context context) {
    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
    notificationManager.createNotificationChannelGroup(
      new NotificationChannelGroupCompat.Builder(Constants.VOICE_CHANNEL_GROUP)
        .setName("Twilio Voice").build());

    for (String channelId:
      new String[]{
        VOICE_CHANNEL_HIGH_IMPORTANCE,
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
    NotificationChannelCompat.Builder voiceChannelBuilder =
      new NotificationChannelCompat.Builder(voiceChannelId, notificationImportance)
        .setName("Primary Voice Channel")
        .setLightColor(Color.GREEN)
        .setGroup(Constants.VOICE_CHANNEL_GROUP);
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

  private static PendingIntent constructPendingIntentForActivity(@NonNull Context context,
                                                                 @NonNull final Intent intent) {
    return PendingIntent.getActivity(
      context.getApplicationContext(),
      0,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
  }

  private static PendingIntent constructPendingIntentForService(@NonNull Context context,
                                                                @NonNull final Intent intent) {
    return PendingIntent.getService(
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

  //

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

  private static String getContentCallBanner(CallInvite callInvite) {
    return getDisplayName(callInvite) + " is calling";
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

}
