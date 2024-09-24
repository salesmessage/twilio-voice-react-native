package com.twiliovoicereactnative;

import static com.twiliovoicereactnative.CommonConstants.CallInviteEventKeyCallSid;
import static com.twiliovoicereactnative.CommonConstants.CallInviteEventKeyType;
import static com.twiliovoicereactnative.CommonConstants.CallInviteEventTypeValueAccepted;
import static com.twiliovoicereactnative.CommonConstants.CallInviteEventTypeValueCancelled;
import static com.twiliovoicereactnative.CommonConstants.CallInviteEventTypeValueNotificationTapped;
import static com.twiliovoicereactnative.CommonConstants.CallInviteEventTypeValueRejected;
import static com.twiliovoicereactnative.CommonConstants.ScopeCallInvite;
import static com.twiliovoicereactnative.CommonConstants.ScopeVoice;
import static com.twiliovoicereactnative.CommonConstants.VoiceErrorKeyError;
import static com.twiliovoicereactnative.CommonConstants.VoiceEventError;
import static com.twiliovoicereactnative.CommonConstants.VoiceEventType;
import static com.twiliovoicereactnative.CommonConstants.VoiceEventTypeValueIncomingCallInvite;
import static com.twiliovoicereactnative.CommonConstants.VoiceEventMissedCallNotificationTapped;
import static com.twiliovoicereactnative.Constants.ACTION_ACCEPT_CALL;
import static com.twiliovoicereactnative.Constants.ACTION_CALL_DISCONNECT;
import static com.twiliovoicereactnative.Constants.ACTION_CANCEL_CALL;
import static com.twiliovoicereactnative.Constants.ACTION_CANCEL_ACTIVE_CALL_NOTIFICATION;
import static com.twiliovoicereactnative.Constants.ACTION_FOREGROUND_AND_DEPRIORITIZE_INCOMING_CALL_NOTIFICATION;
import static com.twiliovoicereactnative.Constants.ACTION_INCOMING_CALL;
import static com.twiliovoicereactnative.Constants.ACTION_PUSH_APP_TO_FOREGROUND;
import static com.twiliovoicereactnative.Constants.ACTION_PUSH_APP_TO_FOREGROUND_FOR_MISSED_CALL;
import static com.twiliovoicereactnative.Constants.ACTION_RAISE_OUTGOING_CALL_NOTIFICATION;
import static com.twiliovoicereactnative.Constants.ACTION_REJECT_CALL;
import static com.twiliovoicereactnative.Constants.JS_EVENT_KEY_CALL_INVITE_INFO;
import static com.twiliovoicereactnative.Constants.JS_EVENT_KEY_CANCELLED_CALL_INVITE_INFO;
import static com.twiliovoicereactnative.Constants.VOICE_CHANNEL_DEFAULT_IMPORTANCE;
import static com.twiliovoicereactnative.Constants.VOICE_CHANNEL_HIGH_IMPORTANCE;
import static com.twiliovoicereactnative.JSEventEmitter.constructJSMap;
import static com.twiliovoicereactnative.ReactNativeArgumentsSerializer.serializeCall;
import static com.twiliovoicereactnative.ReactNativeArgumentsSerializer.serializeCallException;
import static com.twiliovoicereactnative.ReactNativeArgumentsSerializer.serializeCallInvite;
import static com.twiliovoicereactnative.ReactNativeArgumentsSerializer.serializeCancelledCallInvite;
import static com.twiliovoicereactnative.ReactNativeArgumentsSerializer.serializeError;
import static com.twiliovoicereactnative.VoiceApplicationProxy.getCallRecordDatabase;
import static com.twiliovoicereactnative.VoiceApplicationProxy.getJSEventEmitter;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.ServiceCompat;

import com.facebook.react.bridge.WritableMap;
import com.twilio.voice.AcceptOptions;
import com.twilio.voice.Call;
import com.twilio.voice.CancelledCallInvite;
import com.twilio.voice.ConnectOptions;
import com.twilio.voice.Voice;

import java.util.HashMap;
import java.util.Map;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import io.embrace.android.embracesdk.Embrace;

public class VoiceService extends Service {
  private static final SDKLog logger = new SDKLog(VoiceService.class);
  private static final Map<String, Integer> missedCallsMap = new HashMap<String, Integer>();

  private static boolean isRegisterExecuted = false;
  private static final String EventTag = "[Android VoiceService]";

  public class VoiceServiceAPI extends Binder {
    public Call connect(@NonNull ConnectOptions cxnOptions,
        @NonNull Call.Listener listener) {
      logger.debug("connect");
      return Voice.connect(VoiceService.this, cxnOptions, listener);
    }

    public void onRegisterCall() {
      isRegisterExecuted = true;
    }
    public void disconnect(final CallRecordDatabase.CallRecord callRecord) {
      VoiceService.this.disconnect(callRecord);
    }
    public void incomingCall(final CallRecordDatabase.CallRecord callRecord) {
      VoiceService.this.incomingCall(callRecord);
    }
    public void acceptCall(final CallRecordDatabase.CallRecord callRecord) {
      VoiceService.this.acceptCall(callRecord);
    }
    public void rejectCall(final CallRecordDatabase.CallRecord callRecord) {
      VoiceService.this.rejectCall(callRecord);
    }
    public void cancelCall(final CallRecordDatabase.CallRecord callRecord) {
      VoiceService.this.cancelCall(callRecord);
    }
    public void raiseOutgoingCallNotification(final CallRecordDatabase.CallRecord callRecord) {
      VoiceService.this.raiseOutgoingCallNotification(callRecord);
    }
    public void cancelActiveCallNotification(final CallRecordDatabase.CallRecord callRecord) {
      VoiceService.this.cancelActiveCallNotification(callRecord);
    }
    public void foregroundAndDeprioritizeIncomingCallNotification(final CallRecordDatabase.CallRecord callRecord) {
      VoiceService.this.foregroundAndDeprioritizeIncomingCallNotification(callRecord);
    }
    public Context getServiceContext() {
      return VoiceService.this;
    }
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    String action = "";

    CallRecordDatabase.CallRecord callRecord = null;

    try {
      action = Objects.requireNonNull(intent.getAction());

      if (action == ACTION_PUSH_APP_TO_FOREGROUND_FOR_MISSED_CALL) {
        handleMissedCallNotificationClick(intent);
        return START_NOT_STICKY;
      }

      callRecord = getCallRecord(Objects.requireNonNull(getMessageUUID(intent)));
    } catch (Exception e) {
      e.printStackTrace();
      return START_NOT_STICKY;
    }

    if (callRecord == null) {
      return START_NOT_STICKY;
    }
    
    switch (Objects.requireNonNull(intent.getAction())) {
      case ACTION_INCOMING_CALL:
        incomingCall(getCallRecord(Objects.requireNonNull(getMessageUUID(intent))));
        break;
      case ACTION_ACCEPT_CALL:
        try {
          acceptCall(getCallRecord(Objects.requireNonNull(getMessageUUID(intent))));
        } catch (SecurityException e) {
          sendPermissionsError();
          logger.warning(e, "Cannot accept call, lacking necessary permissions");
        }
        break;
      case ACTION_REJECT_CALL:
        rejectCall(getCallRecord(Objects.requireNonNull(getMessageUUID(intent))));
        break;
      case ACTION_CANCEL_CALL:
        cancelCall(getCallRecord(Objects.requireNonNull(getMessageUUID(intent))));
        break;
      case ACTION_CALL_DISCONNECT:
        disconnect(getCallRecord(Objects.requireNonNull(getMessageUUID(intent))));
        break;
      case ACTION_RAISE_OUTGOING_CALL_NOTIFICATION:
        raiseOutgoingCallNotification(getCallRecord(Objects.requireNonNull(getMessageUUID(intent))));
        break;
      case ACTION_CANCEL_ACTIVE_CALL_NOTIFICATION:
        cancelActiveCallNotification(getCallRecord(Objects.requireNonNull(getMessageUUID(intent))));
        break;
      case ACTION_FOREGROUND_AND_DEPRIORITIZE_INCOMING_CALL_NOTIFICATION:
        foregroundAndDeprioritizeIncomingCallNotification(
          getCallRecord(Objects.requireNonNull(getMessageUUID(intent))));
        break;
      case ACTION_PUSH_APP_TO_FOREGROUND_FOR_MISSED_CALL:
        handleMissedCallNotificationClick(intent);
        break;
      case ACTION_PUSH_APP_TO_FOREGROUND:
        logger.warning("VoiceService received foreground request, ignoring");
        break;
      default:
        logger.log("Unknown notification, ignoring");
        break;
    }
    return START_NOT_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return new VoiceServiceAPI();
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
  private void disconnect(final CallRecordDatabase.CallRecord callRecord) {
    logger.debug("disconnect");
    if (null != callRecord) {
      Objects.requireNonNull(callRecord.getVoiceCall()).disconnect();
    } else {
      logger.warning("No call record found");
    }
  }
  private void incomingCall(final CallRecordDatabase.CallRecord callRecord) {
    logger.debug("incomingCall: " + callRecord.getUuid());

    // verify that mic permissions have been granted and if not, throw a error
    if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) &&
      ActivityCompat.checkSelfPermission(VoiceService.this,
        Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

      // report to js layer lack of permissions issue
      sendPermissionsError();

      // report an error to logger
      logger.warning("WARNING: Incoming call cannot be handled, microphone permission not granted");
      return;
    }

    // put up notification
    callRecord.setNotificationId(NotificationUtility.createNotificationIdentifier());
    Notification notification = NotificationUtility.createIncomingCallNotification(
      VoiceService.this,
      callRecord,
      VOICE_CHANNEL_HIGH_IMPORTANCE);
    createOrReplaceNotification(callRecord.getNotificationId(), notification);

    Embrace.getInstance().logInfo(EventTag + " IncomingCall::NotificationCreated");



    // play ringer sound
//    VoiceApplicationProxy.getAudioSwitchManager().getAudioSwitch().activate();
    VoiceApplicationProxy.getMediaPlayerManager().play();

    Embrace.getInstance().logInfo(EventTag + " IncomingCall::SendingJSEvent");
    // trigger JS layer
    sendJSEvent(
      ScopeVoice,
      constructJSMap(
        new Pair<>(VoiceEventType, VoiceEventTypeValueIncomingCallInvite),
        new Pair<>(JS_EVENT_KEY_CALL_INVITE_INFO, serializeCallInvite(callRecord))));

    Embrace.getInstance().logInfo(EventTag + " IncomingCall::JSEventSent");
  }
  private void acceptCall(final CallRecordDatabase.CallRecord callRecord) {
    logger.debug("acceptCall: " + callRecord.getUuid());

    // verify that mic permissions have been granted and if not, throw a error
    if (ActivityCompat.checkSelfPermission(VoiceService.this,
      Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
      // cancel incoming call notification
      removeNotification(callRecord.getNotificationId());

      // stop ringer sound
      VoiceApplicationProxy.getMediaPlayerManager().stop();
      VoiceApplicationProxy.getAudioSwitchManager().getAudioSwitch().deactivate();

      // report an error to JS layer
      sendPermissionsError();

      // report an error to logger
      logger.warning("WARNING: Call not accepted, microphone permission not granted");
      return;
    }

    try {
      // cancel existing notification & put up in call
      Notification notification = NotificationUtility.createCallAnsweredNotificationWithLowImportance(
              VoiceService.this,
              callRecord);
      createOrReplaceForegroundNotification(callRecord.getNotificationId(), notification);

      Embrace.getInstance().logInfo(EventTag + " AcceptCall::NotificationCreated");

      // stop ringer sound
      VoiceApplicationProxy.getMediaPlayerManager().stop();
      VoiceApplicationProxy.getAudioSwitchManager().getAudioSwitch().activate();

      // accept call
      AcceptOptions acceptOptions = new AcceptOptions.Builder()
              .enableDscp(true)
              .callMessageListener(new CallMessageListenerProxy())
              .build();

      callRecord.setCall(
              callRecord.getCallInvite().accept(
                      VoiceService.this,
                      acceptOptions,
                      new CallListenerProxy(callRecord.getUuid(), VoiceService.this)));
      callRecord.setCallInviteUsedState();

      // handle if event spawned from JS
      if (null != callRecord.getCallAcceptedPromise()) {
        callRecord.getCallAcceptedPromise().resolve(serializeCall(callRecord));
      }

      VoiceApplicationProxy.getMediaPlayerManager().enableBluetooth();

      Embrace.getInstance().logInfo(EventTag + " AcceptCall::SendingJSEvent");
      // notify JS layer
      sendJSEvent(
              ScopeCallInvite,
              constructJSMap(
                      new Pair<>(CallInviteEventKeyType, CallInviteEventTypeValueAccepted),
                      new Pair<>(CallInviteEventKeyCallSid, callRecord.getCallSid()),
                      new Pair<>(JS_EVENT_KEY_CALL_INVITE_INFO, serializeCallInvite(callRecord))));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  private void rejectCall(final CallRecordDatabase.CallRecord callRecord) {
    logger.debug("rejectCall: " + callRecord.getUuid());

    try {

      // remove call record
      getCallRecordDatabase().remove(callRecord);

      Embrace.getInstance().logInfo(EventTag + " RejectedCall::CallRecordRemovedFromDB");

      // take down notification
      removeNotification(callRecord.getNotificationId());
      cancelNotification(callRecord);

      // stop ringer sound
      VoiceApplicationProxy.getMediaPlayerManager().stop();
      VoiceApplicationProxy.getAudioSwitchManager().getAudioSwitch().deactivate();

      // reject call
      callRecord.getCallInvite().reject(VoiceService.this);
      callRecord.setCallInviteUsedState();

      Embrace.getInstance().logInfo(EventTag + " RejectedCall::CallInviteRejected");

      // handle if event spawned from JS
      if (null != callRecord.getCallRejectedPromise()) {
        callRecord.getCallRejectedPromise().resolve(callRecord.getUuid().toString());
      }

      Embrace.getInstance().logInfo(EventTag + " RejectedCall::SendingJSEvent");
      // notify JS layer
      sendJSEvent(
              ScopeCallInvite,
              constructJSMap(
                      new Pair<>(CallInviteEventKeyType, CallInviteEventTypeValueRejected),
                      new Pair<>(CallInviteEventKeyCallSid, callRecord.getCallSid()),
                      new Pair<>(JS_EVENT_KEY_CALL_INVITE_INFO, serializeCallInvite(callRecord))));

      Embrace.getInstance().logInfo(EventTag + " RejectedCall::JSEventSent");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  private void cancelCall(final CallRecordDatabase.CallRecord callRecord) {
    logger.debug("CancelCall: " + callRecord.getUuid());

    try {
      // take down notification
      cancelNotification(callRecord);

      Embrace.getInstance().logInfo(EventTag + " CancelledCall::NotificationRemoved");
      
      // stop ringer sound
      VoiceApplicationProxy.getMediaPlayerManager().stop();
      VoiceApplicationProxy.getAudioSwitchManager().getAudioSwitch().deactivate();

      Embrace.getInstance().logInfo(EventTag + " CancelledCall::AudioSwitchDeactivated");

      CancelledCallInvite cancelledCallInvite = callRecord.getCancelledCallInvite();
      String caller = cancelledCallInvite.getFrom().replaceAll("\\+", "");
      logger.debug("from: " + caller);
      String callerShort = caller.substring(caller.length() - 9);

      int callerNumber = Integer.parseInt(callerShort);

      if (!missedCallsMap.containsKey(caller)) {
        missedCallsMap.put(caller, 0);
      }

      int missedCallsValue = missedCallsMap.get(caller);

      missedCallsMap.put(caller, ++missedCallsValue);

      Notification notification = NotificationUtility.createMissedCallNotificationWithLowImportance(
              VoiceService.this,
              callRecord, missedCallsValue, caller);
      createOrReplaceNotification2(getApplicationContext(), callerNumber, notification);

      Embrace.getInstance().logInfo(EventTag + " CancelledCall::MissedCallNotificationCreated");

      // notify JS layer
      sendJSEvent(
              ScopeCallInvite,
              constructJSMap(
                      new Pair<>(CallInviteEventKeyType, CallInviteEventTypeValueCancelled),
                      new Pair<>(CallInviteEventKeyCallSid, callRecord.getCallSid()),
                      new Pair<>(JS_EVENT_KEY_CANCELLED_CALL_INVITE_INFO, serializeCancelledCallInvite(callRecord)),
                      new Pair<>(VoiceErrorKeyError, serializeCallException(callRecord))));

      Embrace.getInstance().logInfo(EventTag + " CancelledCall::JSEventSent");
    } catch (Exception e) {
      Embrace.getInstance().logInfo(EventTag + " CancelledCall::Error::" + e.getMessage());
      e.printStackTrace();
    }
  }

  private void raiseOutgoingCallNotification(final CallRecordDatabase.CallRecord callRecord) {
    logger.debug("raiseOutgoingCallNotification: " + callRecord.getUuid());

    // put up outgoing call notification
    Notification notification =
      NotificationUtility.createOutgoingCallNotificationWithLowImportance(
        VoiceService.this,
        callRecord);
    createOrReplaceForegroundNotification(callRecord.getNotificationId(), notification);
  }
  private void foregroundAndDeprioritizeIncomingCallNotification(final CallRecordDatabase.CallRecord callRecord) {
    logger.debug("foregroundAndDeprioritizeIncomingCallNotification: " + callRecord.getUuid());

    // cancel existing notification & put up in call
    Notification notification = NotificationUtility.createIncomingCallNotification(
      VoiceService.this,
      callRecord,
      VOICE_CHANNEL_DEFAULT_IMPORTANCE);
    createOrReplaceNotification(callRecord.getNotificationId(), notification);

    // stop active sound (if any)
//    VoiceApplicationProxy.getMediaPlayerManager().stop();

    // notify JS layer
    sendJSEvent(
      ScopeCallInvite,
      constructJSMap(
        new Pair<>(CallInviteEventKeyType, CallInviteEventTypeValueNotificationTapped),
        new Pair<>(CallInviteEventKeyCallSid, callRecord.getCallSid())));
  }
  private void cancelActiveCallNotification(final CallRecordDatabase.CallRecord callRecord) {
    logger.debug("cancelNotification");
    // only take down notification & stop any active sounds if one is active
    if (null != callRecord) {
      VoiceApplicationProxy.getMediaPlayerManager().stop();
      removeForegroundNotification();
    }
  }

  private void cancelNotification(final CallRecordDatabase.CallRecord callRecord) {
    logger.debug("cancelNotification");
    // only take down notification & stop any active sounds if one is active
    if (null != callRecord) {
      VoiceApplicationProxy.getMediaPlayerManager().stop();
      removeNotification(callRecord.getNotificationId());
    }
  }

  private void createOrReplaceNotification(final int notificationId,
                                           final Notification notification) {
    NotificationManager mNotificationManager =
      (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    mNotificationManager.notify(notificationId, notification);
  }
  private void createOrReplaceForegroundNotification(final int notificationId,
                                                     final Notification notification) {
    if (ActivityCompat.checkSelfPermission(VoiceService.this, Manifest.permission.POST_NOTIFICATIONS)
      == PackageManager.PERMISSION_GRANTED) {
      foregroundNotification(notificationId, notification);
    } else {
      logger.warning("WARNING: Notification not posted, permission not granted");
    }
  }
  private void removeNotification(final int notificationId) {
    logger.debug("removeNotification");
    NotificationManager mNotificationManager =
      (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    mNotificationManager.cancel(notificationId);
  }

  private static void createOrReplaceNotification2(Context context,
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

  private void removeForegroundNotification() {
    logger.debug("removeForegroundNotification");
    ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE);
  }
  private void foregroundNotification(int id, Notification notification) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      try {
        startForeground(id, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE);
      } catch (Exception e) {
        sendPermissionsError();
        logger.warning(e, "Failed to place notification due to lack of permissions");
      }
    } else {
      startForeground(id, notification);
    }
  }
  private static UUID getMessageUUID(@NonNull final Intent intent) {
    return (UUID)intent.getSerializableExtra(Constants.MSG_KEY_UUID);
  }
  private static CallRecordDatabase.CallRecord getCallRecord(final UUID uuid) {
    return Objects.requireNonNull(getCallRecordDatabase().get(new CallRecordDatabase.CallRecord(uuid)));
  }
  private static void sendJSEvent(@NonNull String scope, @NonNull WritableMap event) {
    getJSEventEmitter().sendEvent(scope, event);
  }
  private static void sendPermissionsError() {
    final String errorMessage = "Missing permissions.";
    final int errorCode = 31401;
    getJSEventEmitter().sendEvent(ScopeVoice, constructJSMap(
      new Pair<>(VoiceEventType, VoiceEventError),
      new Pair<>(VoiceErrorKeyError, serializeError(errorCode, errorMessage))
    ));
  }

  private void postponeMissedCallNotificationCallback(WritableMap payload, Integer attempt) {
    if (attempt > 20) {
        return;
    }

    if (isRegisterExecuted) {
      sendJSEvent(
        ScopeVoice,
        constructJSMap(
          new Pair<>(VoiceEventType, VoiceEventMissedCallNotificationTapped),
          new Pair<>(JS_EVENT_KEY_CALL_INVITE_INFO, payload)));

      return;
    }

    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        postponeMissedCallNotificationCallback(payload, attempt + 1);
      }
    }, 1000L);
  }

  private void handleMissedCallNotificationClick(Intent intent) {
    logger.debug("Missed Call Notification Click Message Received");

    String INBOX_DATA = (String) intent.getSerializableExtra("INBOX_DATA");
    String CONTACT_DATA = (String) intent.getSerializableExtra("CONTACT_DATA");
    String CALLER = (String) intent.getSerializableExtra("CALLER");

    WritableMap payload = constructJSMap(
        new Pair<>("inbox", INBOX_DATA),
        new Pair<>("contact", CONTACT_DATA));

    postponeMissedCallNotificationCallback(payload, 0);

    missedCallsMap.put(CALLER, 0);
  }
}
