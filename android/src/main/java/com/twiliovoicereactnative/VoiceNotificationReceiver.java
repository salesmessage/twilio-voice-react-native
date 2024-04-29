package com.twiliovoicereactnative;

import static com.twiliovoicereactnative.CommonConstants.VoiceEventMissedCallNotificationTapped;
import static com.twiliovoicereactnative.Constants.ACTION_PUSH_APP_TO_FOREGROUND_FOR_MISSED_CALL;
import static com.twiliovoicereactnative.Constants.JS_EVENT_KEY_CANCELLED_CALL_INVITE_INFO;
import static com.twiliovoicereactnative.Constants.VOICE_CHANNEL_HIGH_IMPORTANCE;
import static com.twiliovoicereactnative.Constants.VOICE_CHANNEL_HIGH_IMPORTANCE_WITH_VIBRATION;
import static com.twiliovoicereactnative.JSEventEmitter.constructJSMap;
import static com.twiliovoicereactnative.CommonConstants.ScopeVoice;
import static com.twiliovoicereactnative.CommonConstants.VoiceEventCallInvite;
import static com.twiliovoicereactnative.CommonConstants.VoiceEventCallInviteAccepted;
import static com.twiliovoicereactnative.CommonConstants.VoiceEventCallInviteCancelled;
import static com.twiliovoicereactnative.CommonConstants.VoiceEventCallInviteNotificationTapped;
import static com.twiliovoicereactnative.CommonConstants.VoiceEventCallInviteRejected;
import static com.twiliovoicereactnative.CommonConstants.VoiceEventType;
import static com.twiliovoicereactnative.CommonConstants.VoiceErrorKeyError;
import static com.twiliovoicereactnative.Constants.ACTION_ACCEPT_CALL;
import static com.twiliovoicereactnative.Constants.ACTION_CALL_DISCONNECT;
import static com.twiliovoicereactnative.Constants.ACTION_CANCEL_CALL;
import static com.twiliovoicereactnative.Constants.ACTION_CANCEL_NOTIFICATION;
import static com.twiliovoicereactnative.Constants.ACTION_FOREGROUND_AND_DEPRIORITIZE_INCOMING_CALL_NOTIFICATION;
import static com.twiliovoicereactnative.Constants.ACTION_INCOMING_CALL;
import static com.twiliovoicereactnative.Constants.ACTION_PUSH_APP_TO_FOREGROUND;
import static com.twiliovoicereactnative.Constants.ACTION_RAISE_OUTGOING_CALL_NOTIFICATION;
import static com.twiliovoicereactnative.Constants.ACTION_REJECT_CALL;
import static com.twiliovoicereactnative.Constants.JS_EVENT_KEY_CALL_INVITE_INFO;
import static com.twiliovoicereactnative.Constants.VOICE_CHANNEL_DEFAULT_IMPORTANCE;
import static com.twiliovoicereactnative.ReactNativeArgumentsSerializer.serializeCall;
import static com.twiliovoicereactnative.ReactNativeArgumentsSerializer.serializeCallException;
import static com.twiliovoicereactnative.ReactNativeArgumentsSerializer.serializeCallInvite;
import static com.twiliovoicereactnative.ReactNativeArgumentsSerializer.serializeCancelledCallInvite;
import static com.twiliovoicereactnative.VoiceApplicationProxy.getCallRecordDatabase;
import static com.twiliovoicereactnative.VoiceApplicationProxy.getJSEventEmitter;

import com.twilio.voice.CancelledCallInvite;
import com.twiliovoicereactnative.CallRecordDatabase.CallRecord;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import android.Manifest;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.facebook.react.bridge.WritableMap;
import com.twilio.voice.AcceptOptions;

import io.embrace.android.embracesdk.Embrace;

public class VoiceNotificationReceiver extends BroadcastReceiver {
  private static boolean isCallInProgress = false;

  private static final String EventTag = "[Android VoiceNotificationReceiver]";
  private static final SDKLog logger = new SDKLog(VoiceNotificationReceiver.class);
  private static final Map<String, Integer> missedCallsMap = new HashMap<String, Integer>();
  @Override
  public void onReceive(@NonNull Context context, @NonNull Intent intent) {
    String action = Objects.requireNonNull(intent.getAction());
    logger.log("action: " + action);

    Embrace.getInstance().logInfo(EventTag + " Action::" + action);

    switch (action) {
      case ACTION_INCOMING_CALL:
        handleIncomingCall(context, Objects.requireNonNull(getMessageUUID(intent)));
        break;
      case ACTION_ACCEPT_CALL:
        isCallInProgress = true;
        handleAccept(context, Objects.requireNonNull(getMessageUUID(intent)));
        break;
      case ACTION_REJECT_CALL:
        handleReject(context, Objects.requireNonNull(getMessageUUID(intent)));
        break;
      case ACTION_CANCEL_CALL:
        handleCancelCall(context, Objects.requireNonNull(getMessageUUID(intent)));
        break;
      case ACTION_CALL_DISCONNECT:
        isCallInProgress = false;
        handleDisconnect(Objects.requireNonNull(getMessageUUID(intent)));
        VoiceApplicationProxy.getAudioSwitchManager().getAudioSwitch().deactivate();
        break;
      case ACTION_RAISE_OUTGOING_CALL_NOTIFICATION:
        isCallInProgress = true;
        handleRaiseOutgoingCallNotification(context, Objects.requireNonNull(getMessageUUID(intent)));
        break;
      case ACTION_CANCEL_NOTIFICATION:
        isCallInProgress = false;
        handleCancelNotification(context, Objects.requireNonNull(getMessageUUID(intent)));
        VoiceApplicationProxy.getAudioSwitchManager().getAudioSwitch().deactivate();
        break;
      case ACTION_FOREGROUND_AND_DEPRIORITIZE_INCOMING_CALL_NOTIFICATION:
        handleForegroundAndDeprioritizeIncomingCallNotification(
          context,
          Objects.requireNonNull(getMessageUUID(intent)));
        break;
      case ACTION_PUSH_APP_TO_FOREGROUND_FOR_MISSED_CALL:
        handleMissedCallNotificationClick(context, intent);
        break;
      case ACTION_PUSH_APP_TO_FOREGROUND:
        logger.warning("BroadcastReceiver received foreground request, ignoring");
        break;
      case "android.intent.action.MAIN":
//        cancelAllNotifications(context);
        break;
      default:
        logger.log("Unknown notification, ignoring");
        break;
    }
  }

  public static Intent constructMessage(@NonNull Context context,
                                        @NonNull final String action,
                                        @NonNull final Class<?> target,
                                        @NonNull final UUID uuid) {
    Intent intent = new Intent(context.getApplicationContext(), target);
    intent.setAction(action);
    intent.putExtra(Constants.MSG_KEY_UUID, uuid);
    return intent;
  }

  public static void sendMessage(@NonNull Context context,
                                 @NonNull final String action,
                                 @NonNull final UUID uuid) {
    context.sendBroadcast(constructMessage(context, action, VoiceNotificationReceiver.class, uuid));
  }

  private static UUID getMessageUUID(@NonNull final Intent intent) {
    return (UUID)intent.getSerializableExtra(Constants.MSG_KEY_UUID);
  }

  private void handleIncomingCall(Context context, final UUID uuid) {
    try {
      logger.debug("Incoming_Call Message Received");
      // find call record
      CallRecord callRecord =
              Objects.requireNonNull(getCallRecordDatabase().get(new CallRecord(uuid)));

      Embrace.getInstance().logInfo(EventTag + " IncomingCall::CallRecordRetrievedFromDB");

      // put up notification
      callRecord.setNotificationId(NotificationUtility.createNotificationIdentifier());
      Notification notification = NotificationUtility.createIncomingCallNotification(
              context.getApplicationContext(),
              callRecord,
              VoiceApplicationProxy.getMediaPlayerManager().isRingerModeVibrate() ?
                      VOICE_CHANNEL_HIGH_IMPORTANCE_WITH_VIBRATION : VOICE_CHANNEL_HIGH_IMPORTANCE,
              true);
//    notification.flags = Notification.FLAG_INSISTENT;
      createOrReplaceNotification(context, callRecord.getNotificationId(), notification);

      Embrace.getInstance().logInfo(EventTag + " IncomingCall::NotificationCreated");

      // play ringer sound
//    VoiceApplicationProxy.getAudioSwitchManager().getAudioSwitch().activate();
      if (!isCallInProgress) {
        VoiceApplicationProxy.getMediaPlayerManager().play(true, isAppVisible());
      }

      Embrace.getInstance().logInfo(EventTag + " IncomingCall::SendingJSEvent");

      // trigger JS layer
      sendJSEvent(
              constructJSMap(
                      new Pair<>(VoiceEventType, VoiceEventCallInvite),
                      new Pair<>(JS_EVENT_KEY_CALL_INVITE_INFO, serializeCallInvite(callRecord))));

      Embrace.getInstance().logInfo(EventTag + " IncomingCall::JSEventSent");
    } catch (Exception e) {
      HashMap props = new HashMap<String, Integer>();
      props.put("errMessage", e.getMessage());

      Embrace.getInstance().logError(EventTag + " IncomingCall::Error::", props);
      e.printStackTrace();
    }
  }

  private void handleAccept(Context context, final UUID uuid) {
    logger.debug("Accept_Call Message Received");
    // find call record

    CallRecord callRecord = null;
    try {
      callRecord = Objects.requireNonNull(getCallRecordDatabase().get(new CallRecord(uuid)));
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (callRecord == null) {
      VoiceApplicationProxy.getMediaPlayerManager().stop();
      Embrace.getInstance().logInfo(EventTag + " AcceptCall::CallRecordNotFoundInDB");
      return;
    }

    Embrace.getInstance().logInfo(EventTag + " AcceptCall::CallRecordRetrievedFromDB");

    VoiceApplicationProxy.getAudioSwitchManager().getAudioSwitch().activate();

    // cancel existing notification & put up in call
    Notification notification = NotificationUtility.createCallAnsweredNotificationWithLowImportance(
      context.getApplicationContext(),
      callRecord);
    createOrReplaceNotification(context, callRecord.getNotificationId(), notification);

    Embrace.getInstance().logInfo(EventTag + " AcceptCall::NotificationCreated");

    // stop ringer sound
    VoiceApplicationProxy.getMediaPlayerManager().stop();

    // accept call
    AcceptOptions acceptOptions = new AcceptOptions.Builder()
      .enableDscp(true)
      .callMessageListener(new CallMessageListenerProxy())
      .build();
    callRecord.setCall(callRecord.getCallInvite().accept(
      context.getApplicationContext(),
      acceptOptions,
      new CallListenerProxy(uuid, context)));
    callRecord.setCallInviteUsedState();

    // handle if event spawned from JS
    if (null != callRecord.getCallAcceptedPromise()) {
      callRecord.getCallAcceptedPromise().resolve(serializeCall(callRecord));
    }

    Embrace.getInstance().logInfo(EventTag + " AcceptCall::SendingJSEvent");
    // notify JS layer
    sendJSEvent(
      constructJSMap(
        new Pair<>(VoiceEventType, VoiceEventCallInviteAccepted),
        new Pair<>(JS_EVENT_KEY_CALL_INVITE_INFO, serializeCallInvite(callRecord))));

    Embrace.getInstance().logInfo(EventTag + " AcceptCall::JSEventSent");
  }

  private void handleReject(Context context, final UUID uuid) {
    logger.debug("Reject_Call Message Received");
    // find call record

    CallRecord callRecord = null;
    try {
      callRecord = Objects.requireNonNull(getCallRecordDatabase().remove(new CallRecord(uuid)));
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (callRecord == null) {
      VoiceApplicationProxy.getMediaPlayerManager().stop();
      Embrace.getInstance().logInfo(EventTag + " RejectedCall::CallRecordNotFoundInDB");
      return;
    }

    Embrace.getInstance().logInfo(EventTag + " RejectedCall::CallRecordRetrievedFromDB");

    // take down notification
    cancelNotification(context, callRecord.getNotificationId());

    Embrace.getInstance().logInfo(EventTag + " RejectedCall::NotificationCancelled");

    // stop ringer sound
    VoiceApplicationProxy.getMediaPlayerManager().stop();

//  VoiceApplicationProxy.getAudioSwitchManager().getAudioSwitch().deactivate();

    // reject call
    callRecord.getCallInvite().reject(context.getApplicationContext());
    callRecord.setCallInviteUsedState();

    // handle if event spawned from JS
    if (null != callRecord.getCallRejectedPromise()) {
      callRecord.getCallRejectedPromise().resolve(callRecord.getUuid().toString());
    }

    Embrace.getInstance().logInfo(EventTag + " RejectedCall::SendingJSEvent");
    // notify JS layer
    sendJSEvent(
      constructJSMap(
        new Pair<>(VoiceEventType, VoiceEventCallInviteRejected),
        new Pair<>(JS_EVENT_KEY_CALL_INVITE_INFO, serializeCallInvite(callRecord))));

    Embrace.getInstance().logInfo(EventTag + " RejectedCall::JSEventSent");
  }

  private void handleCancelCall(Context context, final UUID uuid) {
    CallRecord callRecord;

    try {
      logger.debug("Cancel_Call Message Received");
      // find call record
      callRecord =
              Objects.requireNonNull(getCallRecordDatabase().remove(new CallRecord(uuid)));

      Embrace.getInstance().logInfo(EventTag + " CancelledCall::CallRecordRetrievedFromDB");

      // take down notification
      cancelNotification(context, callRecord.getNotificationId());

      // stop ringer sound
      VoiceApplicationProxy.getMediaPlayerManager().stop();

//    VoiceApplicationProxy.getAudioSwitchManager().getAudioSwitch().deactivate();

      Embrace.getInstance().logInfo(EventTag + " CancelledCall::SendingJSEvent");
      // notify JS layer
      sendJSEvent(
              constructJSMap(
                      new Pair<>(VoiceEventType, VoiceEventCallInviteCancelled),
                      new Pair<>(JS_EVENT_KEY_CANCELLED_CALL_INVITE_INFO, serializeCancelledCallInvite(callRecord)),
                      new Pair<>(VoiceErrorKeyError, serializeCallException(callRecord))));

      Embrace.getInstance().logInfo(EventTag + " CancelledCall::JSEventSent");

    } catch (Exception e) {
      HashMap props = new HashMap<String, Integer>();
      props.put("errMessage", e.getMessage());

      Embrace.getInstance().logError(EventTag + " CancelledCall::Error", props);
      e.printStackTrace();
      return;
    }


    try {
      // put up notification
      callRecord.setNotificationId(NotificationUtility.createNotificationIdentifier());
      CancelledCallInvite cancelledCallInvite = callRecord.getCancelledCallInvite();
      String caller = cancelledCallInvite.getFrom().replaceAll("\\+", "");
      logger.debug("from: " + caller);
      String callerShort = caller.substring(caller.length() - 9);

      int callerNumber = Integer.parseInt(callerShort);

      logger.debug("from: " + callerNumber);

      if (!missedCallsMap.containsKey(caller)) {
        missedCallsMap.put(caller, 0);
      }

      int missedCallsValue = missedCallsMap.get(caller);

      missedCallsMap.put(caller, ++missedCallsValue);

      cancelNotification(context, callerNumber);

      Notification notification = NotificationUtility.createMissedCallNotificationWithLowImportance(
              context.getApplicationContext(),
              callRecord, missedCallsValue);
      createOrReplaceNotification(context, callerNumber, notification);

      Embrace.getInstance().logInfo(EventTag + " CancelledCall::MissedCallNotificationCreated");
    } catch (Exception e) {
      HashMap props = new HashMap<String, Integer>();
      props.put("errMessage", e.getMessage());

      Embrace.getInstance().logError(EventTag + " CancelledCall::MissedCallNotificationError", props);
      e.printStackTrace();
    }
  }

  private void handleDisconnect(final UUID uuid) {
    logger.debug("Disconnect Message Received");

    // find call record
    final CallRecord callRecord = getCallRecordDatabase().get(new CallRecord(uuid));

    // handle disconnect
    if (null != callRecord) {
      Objects.requireNonNull(callRecord.getVoiceCall()).disconnect();
    } else {
      logger.warning("No call found");
    }
  }

  private void handleRaiseOutgoingCallNotification(Context context,
                                                   final UUID uuid) {
    logger.debug("Raise Outgoing Call Notification Message Received");

    // find call record
    final CallRecord callRecord =
      Objects.requireNonNull(getCallRecordDatabase().get(new CallRecord(uuid)));

    Embrace.getInstance().logInfo(EventTag + " OutgoingCall::CallRecordRetrievedFromDB");

    // put up outgoing call notification
    Notification notification = NotificationUtility.createOutgoingCallNotificationWithLowImportance(
      context.getApplicationContext(),
      callRecord);
    createOrReplaceNotification(context, callRecord.getNotificationId(), notification);

    Embrace.getInstance().logInfo(EventTag + " OutgoingCall::NotificationCreated");
  }

  private void handleCancelNotification(Context context, final UUID uuid) {
    logger.debug("Cancel Notification Message Received");
    // only take down notification & stop any active sounds if one is active
    final CallRecord callRecord = getCallRecordDatabase().remove(new CallRecord(uuid));
    if (null != callRecord) {
      VoiceApplicationProxy.getMediaPlayerManager().stop();
      cancelNotification(context, callRecord.getNotificationId());
    }
  }

  private void handleForegroundAndDeprioritizeIncomingCallNotification(Context context,
                                                                       final UUID uuid) {
    logger.debug("Foreground & Deprioritize Incoming Call Notification Message Received");

    // cancel existing notification & put up in call
    CallRecord callRecord = null;
    try {
      callRecord = Objects.requireNonNull(getCallRecordDatabase().get(new CallRecord(uuid)));
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (callRecord == null) {
      Embrace.getInstance().logInfo(EventTag + " handleForegroundAndDeprioritizeIncomingCallNotification::CallRecordNotFoundInDB");
      return;
    }
    
    Notification notification = NotificationUtility.createIncomingCallNotification(
      context.getApplicationContext(),
      callRecord,
      VOICE_CHANNEL_DEFAULT_IMPORTANCE,
      false);
    createOrReplaceNotification(context, callRecord.getNotificationId(), notification);

    // stop active sound (if any)
//    VoiceApplicationProxy.getMediaPlayerManager().stop();

    // notify JS layer
    sendJSEvent(
      constructJSMap(
        new Pair<>(VoiceEventType, VoiceEventCallInviteNotificationTapped),
        new Pair<>(JS_EVENT_KEY_CALL_INVITE_INFO, serializeCallInvite(callRecord))
      ));
  }

  private void handleMissedCallNotificationClick(Context context, Intent intent) {
    logger.debug("Missed Call Notification Click Message Received");

    String INBOX_DATA = (String) intent.getSerializableExtra("INBOX_DATA");
    String CONTACT_DATA = (String) intent.getSerializableExtra("CONTACT_DATA");

    WritableMap payload = constructJSMap(
            new Pair<>("inbox", INBOX_DATA),
            new Pair<>("contact", CONTACT_DATA)
    );

    // notify JS layer
    sendJSEvent(
            constructJSMap(
                    new Pair<>(VoiceEventType, VoiceEventMissedCallNotificationTapped),
                    new Pair<>(JS_EVENT_KEY_CALL_INVITE_INFO, payload)
            ));
  }

  private static void sendJSEvent(@NonNull WritableMap event) {
    getJSEventEmitter().sendEvent(ScopeVoice, event);
  }

  private static void createOrReplaceNotification(Context context,
                                                  final int notificationId,
                                                  final Notification notification) {
    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
      == PackageManager.PERMISSION_GRANTED) {
      notificationManager.notify(notificationId, notification);
    } else {
      logger.warning("WARNING: Notification not posted, permission not granted");
    }
  }

  private static void cancelNotification(Context context, final int notificationId) {
    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
    notificationManager.cancel(notificationId);
  }

  private boolean isAppVisible() {
    return ProcessLifecycleOwner
            .get()
            .getLifecycle()
            .getCurrentState()
            .isAtLeast(Lifecycle.State.STARTED);
  }

  public static void cancelAllNotifications(Context context) {
    logger.debug("cancelAllNotifications start");
    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

    notificationManager.cancelAll();

    missedCallsMap.clear();
  }
}
