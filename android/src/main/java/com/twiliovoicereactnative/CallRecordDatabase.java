package com.twiliovoicereactnative;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.twilio.voice.Call;
import com.twilio.voice.CallInvite;
import com.twilio.voice.CancelledCallInvite;

class CallRecordDatabase  {
  public static class CallRecord {
    private final UUID uuid;
    private String callSid = null;
    private Date timestamp = null;
    private int notificationId = -1;
    private Call voiceCall = null;
    private CallInvite callInvite = null;
    private CancelledCallInvite cancelledCallInvite = null;
    private Promise callAcceptedPromise = null;
    private Promise callRejectedPromise = null;
    public CallRecord(final UUID uuid) {
      this.uuid = uuid;
    }
    public CallRecord(final String callSid) {
      this.uuid = null;
      this.callSid = callSid;
    }
    public CallRecord(final UUID uuid, final CallInvite callInvite) {
      this.uuid = uuid;
      this.callSid = callInvite.getCallSid();
      this.callInvite = callInvite;
    }
    public CallRecord(final UUID uuid, final Call call) {
      this.uuid = uuid;
      this.callSid = call.getSid();
      this.voiceCall = call;
    }
    public final UUID getUuid() {
      return uuid;
    }
    public String getCallSid() {
      return callSid;
    }
    public int getNotificationId() {
      return notificationId;
    }
    public Date getTimestamp() {
      return timestamp;
    }
    public Call getVoiceCall() {
      return this.voiceCall;
    }
    public CallInvite getCallInvite() {
      return this.callInvite;
    }
    public CancelledCallInvite getCancelledCallInvite() {
      return this.cancelledCallInvite;
    }
    public Promise getCallAcceptedPromise() {
      return this.callAcceptedPromise;
    }
    public Promise getCallRejectedPromise() {
      return this.callRejectedPromise;
    }
    public void setNotificationId(int notificationId) {
      this.notificationId = notificationId;
    }
    public void setTimestamp(Date timestamp) {
      this.timestamp = timestamp;
    }
    public void setCall(@NonNull Call voiceCall) {
      this.callSid = voiceCall.getSid();
      this.voiceCall = voiceCall;
    }
    public void setCancelledCallInvite(@NonNull CancelledCallInvite cancelledCallInvite) {
      this.callSid = cancelledCallInvite.getCallSid();
      this.cancelledCallInvite = cancelledCallInvite;
    }
    public void setCallAcceptedPromise(@NonNull Promise callAcceptedPromise) {
      this.callAcceptedPromise = callAcceptedPromise;
    }
    public void setCallRejectedPromise(@NonNull Promise callRejectedPromise) {
      this.callRejectedPromise = callRejectedPromise;
    }
    @Override
    public boolean equals(Object obj) {
      return (obj instanceof CallRecord) && comparator(this, (CallRecord)obj);
    }
  }
  private List<CallRecord> callRecordList = new Vector<>();

  public boolean add(final CallRecord callRecord) {
    return callRecordList.add(callRecord);
  }
  public void clear() {
    callRecordList.clear();
  }

  public CallRecord get(final CallRecord record) {
    try {
      return callRecordList.get(callRecordList.indexOf(record));
    } catch (IndexOutOfBoundsException e) {
      return null;
    }
  }
  public CallRecord remove(final CallRecord record) {
    try {
      return callRecordList.remove(callRecordList.indexOf(record));
    } catch (IndexOutOfBoundsException e) {
      return null;
    }
  }
  public Collection<CallRecord> getCollection() {
    return callRecordList;
  }
  private static boolean comparator(@NonNull final CallRecord lhs, @NonNull final CallRecord rhs) {
    if (null != lhs.uuid && null != rhs.uuid) {
      return lhs.uuid.equals(rhs.uuid);
    } else if (null != lhs.callSid && null != rhs.callSid) {
      return lhs.callSid.equals(rhs.callSid);
    }
    return false;
  }
}

