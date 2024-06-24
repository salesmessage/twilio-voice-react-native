"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.CallInvite = void 0;
var _eventemitter = require("eventemitter3");
var _Call = require("./Call");
var _common = require("./common");
var _InvalidStateError = require("./error/InvalidStateError");
var _TwilioError = require("./error/TwilioError");
var _UnsupportedPlatformError = require("./error/UnsupportedPlatformError");
var _utility = require("./error/utility");
var _CallMessage = require("./CallMessage");
var _OutgoingCallMessage = require("./OutgoingCallMessage");
var _constants = require("./constants");
function _defineProperty(obj, key, value) { key = _toPropertyKey(key); if (key in obj) { Object.defineProperty(obj, key, { value: value, enumerable: true, configurable: true, writable: true }); } else { obj[key] = value; } return obj; }
function _toPropertyKey(t) { var i = _toPrimitive(t, "string"); return "symbol" == typeof i ? i : String(i); }
function _toPrimitive(t, r) { if ("object" != typeof t || !t) return t; var e = t[Symbol.toPrimitive]; if (void 0 !== e) { var i = e.call(t, r || "default"); if ("object" != typeof i) return i; throw new TypeError("@@toPrimitive must return a primitive value."); } return ("string" === r ? String : Number)(t); } /**
 * Copyright © 2022 Twilio, Inc. All rights reserved. Licensed under the Twilio
 * license.
 *
 * See LICENSE in the project root for license information.
 */
/**
 * Defines strict typings for all events emitted by {@link (CallInvite:class)
 * | CallInvite objects}.
 *
 * @remarks
 * Note that the `on` function is an alias for the `addListener` function.
 * They share identical functionality and either may be used interchangeably.
 *
 * - See also the {@link (CallInvite:class) | CallInvite class}.
 * - See also the {@link (CallInvite:namespace) | CallInvite namespace}.
 *
 * @public
 */

/**
 * Provides access to information about a call invite, including the call
 * parameters, and exposes functionality to accept or decline a call.
 *
 * @remarks
 *
 * Note that when a `CallInvite` is acted upon (i.e. when
 * {@link (CallInvite:class).accept} or {@link (CallInvite:class).reject} is
 * invoked), then the `CallInvite` is "settled".
 *
 * The state of the `CallInvite` is changed from
 * {@link (CallInvite:namespace).State.Pending} to
 * {@link (CallInvite:namespace).State.Accepted} or
 * {@link (CallInvite:namespace).State.Rejected} and the `CallInvite` can no
 * longer be acted upon further.
 *
 * Further action after "settling" a `CallInvite` will throw an error.
 *
 *  - See the {@link (CallInvite:namespace) | CallInvite namespace} for
 *    enumerations and types used by this class.
 *
 * @public
 */
class CallInvite extends _eventemitter.EventEmitter {
  /**
   * These objects should not be instantiated by consumers of the SDK. All
   * instances of the `CallInvite` class should be emitted by the SDK.
   *
   * @param nativeCallInviteInfo - A dataobject containing the native
   * information of a call invite.
   * @param state - Mocking options for testing.
   *
   * @internal
   */
  constructor({
    uuid,
    callSid,
    customParameters,
    from,
    to
  }, state) {
    super();
    /**
     * The current state of the call invite.
     *
     * @remarks
     * See {@link (CallInvite:namespace).State}.
     */
    _defineProperty(this, "_state", void 0);
    /**
     * The `Uuid` of this call invite. Used to identify calls between the JS and
     * native layer so we can associate events and native functionality between
     * the layers.
     */
    _defineProperty(this, "_uuid", void 0);
    /**
     * A string representing the SID of this call.
     */
    _defineProperty(this, "_callSid", void 0);
    /**
     * Call custom parameters.
     */
    _defineProperty(this, "_customParameters", void 0);
    /**
     * Call `from` parameter.
     */
    _defineProperty(this, "_from", void 0);
    /**
     * Call `to` parameter.
     */
    _defineProperty(this, "_to", void 0);
    /**
     * This intermediate native call invite event handler acts as a "gate", only
     * executing the actual call invite event handler (such as `Accepted`) if
     * this call invite object matches the `Uuid` of the call invite that had an
     * event raised.
     * @param nativeCallInviteEvent - A call invite event directly from the native
     * layer.
     */
    _defineProperty(this, "_handleNativeCallInviteEvent", nativeCallInviteEvent => {
      if (typeof nativeCallInviteEvent !== 'object') {
        throw new _TwilioError.TwilioError(`Received a "${typeof nativeCallInviteEvent}" native call invite event.`);
      }
      if (nativeCallInviteEvent === null) {
        throw new _TwilioError.TwilioError('Received a null native call invite event.');
      }
      if (typeof nativeCallInviteEvent.callSid !== 'string') {
        throw new _TwilioError.TwilioError('Received a native call invite event without a call SID.');
      }
      if (nativeCallInviteEvent.callSid !== this._callSid) {
        return;
      }
      switch (nativeCallInviteEvent.type) {
        case _constants.Constants.CallInviteEventTypeValueAccepted:
          return this._handleCallInviteAccepted(nativeCallInviteEvent);
        case _constants.Constants.CallInviteEventTypeValueRejected:
          return this._handleCallInviteRejected();
        case _constants.Constants.CallInviteEventTypeValueCancelled:
          return this._handleCallInviteCancelled(nativeCallInviteEvent);
        case _constants.Constants.CallInviteEventTypeValueNotificationTapped:
          return this._handleCallInviteNotificationTapped();
        case _constants.Constants.CallEventMessageReceived:
          return this._handleMessageReceivedEvent(nativeCallInviteEvent);
        default:
          return this._handleUnexpectedCallInviteEventType(nativeCallInviteEvent);
      }
    });
    /**
     * Handler for the {@link (CallInvite:namespace).Event.MessageReceived} event.
     * @param nativeCallEvent - The native call event.
     */
    _defineProperty(this, "_handleMessageReceivedEvent", nativeCallInviteEvent => {
      const {
        callMessage: callMessageInfo
      } = nativeCallInviteEvent;
      const callMessage = new _CallMessage.CallMessage(callMessageInfo);
      this.emit(CallInvite.Event.MessageReceived, callMessage);
    });
    /**
     * Handle when this call invite is accepted.
     */
    _defineProperty(this, "_handleCallInviteAccepted", ({
      callInvite
    }) => {
      this._state = CallInvite.State.Accepted;
      const callInfo = {
        uuid: callInvite.uuid,
        customParameters: callInvite.customParameters,
        sid: callInvite.callSid,
        from: callInvite.from,
        to: callInvite.to
      };
      const call = new _Call.Call(callInfo);
      this.emit(CallInvite.Event.Accepted, call);
    });
    /**
     * Handle when this call invite is rejected.
     */
    _defineProperty(this, "_handleCallInviteRejected", () => {
      this._state = CallInvite.State.Rejected;
      this.emit(CallInvite.Event.Rejected);
    });
    /**
     * Handle when a call invite is cancelled.
     */
    _defineProperty(this, "_handleCallInviteCancelled", nativeCallInviteCancelledEvent => {
      this._state = CallInvite.State.Cancelled;
      const error = nativeCallInviteCancelledEvent.error ? (0, _utility.constructTwilioError)(nativeCallInviteCancelledEvent.error.message, nativeCallInviteCancelledEvent.error.code) : undefined;
      this.emit(CallInvite.Event.Cancelled, error);
    });
    /**
     * Handle when a call invite notification is tapped.
     */
    _defineProperty(this, "_handleCallInviteNotificationTapped", () => {
      this.emit(CallInvite.Event.NotificationTapped);
    });
    this._uuid = uuid;
    this._callSid = callSid;
    this._customParameters = {
      ...customParameters
    };
    this._from = from;
    this._to = to;
    this._state = state;
    _common.NativeEventEmitter.addListener(_constants.Constants.ScopeCallInvite, this._handleNativeCallInviteEvent);
  }

  /**
   * This helper function serves as both a runtime-check error log and a
   * compile-time type-guard. If the switch-case statement below is non-
   * exhaustive, then the type passed to this function will _not_ have type
   * `never`.
   */
  _handleUnexpectedCallInviteEventType(event) {
    throw new _TwilioError.TwilioError(`Unknown event type "${event === null || event === void 0 ? void 0 : event.type}" reached call invite.`);
  }
  /**
   * Accept a call invite. Sets the state of this call invite to
   * {@link (CallInvite:namespace).State.Accepted}.
   * @param options - Options to pass to the native layer when accepting the
   * call.
   * @returns
   *  - Resolves when a {@link (Call:class) | Call object} associated with this
   *    {@link (CallInvite:class)} has been created.
   */
  async accept(options = {}) {
    if (this._state !== CallInvite.State.Pending) {
      throw new _InvalidStateError.InvalidStateError(`Call in state "${this._state}", ` + `expected state "${CallInvite.State.Pending}".`);
    }
    const callInfo = await _common.NativeModule.callInvite_accept(this._uuid, options);
    const call = new _Call.Call(callInfo);
    return call;
  }

  /**
   * Reject a call invite. Sets the state of this call invite to
   * {@link (CallInvite:namespace).State.Rejected}.
   * @returns
   *  - Resolves when the {@link (CallInvite:class)} has been rejected.
   */
  async reject() {
    if (this._state !== CallInvite.State.Pending) {
      throw new _InvalidStateError.InvalidStateError(`Call in state "${this._state}", ` + `expected state "${CallInvite.State.Pending}".`);
    }
    await _common.NativeModule.callInvite_reject(this._uuid);
  }

  /**
   * Check if a `CallInvite` is valid.
   *
   * @returns
   *  - TODO
   *
   * @alpha
   */
  isValid() {
    return _common.NativeModule.callInvite_isValid(this._uuid);
  }

  /**
   * Get the call SID associated with this `CallInvite` class.
   * @returns - A string representing the call SID.
   */
  getCallSid() {
    return this._callSid;
  }

  /**
   * Get the custom parameters of the call associated with this `CallInvite`
   * class.
   * @returns - A `Record` of custom parameters.
   */
  getCustomParameters() {
    return this._customParameters;
  }

  /**
   * Get the `from` parameter of the call associated with this `CallInvite`
   * class.
   * @returns - A `string` representing the `from` parameter.
   */
  getFrom() {
    return this._from;
  }

  /**
   * Get the `state` of the `CallInvite`.
   * @returns - The `state` of this `CallInvite`.
   */
  getState() {
    return this._state;
  }

  /**
   * Get the `to` parameter of the call associated with this `CallInvite`
   * class.
   * @returns - A `string` representing the `to` parameter.
   */
  getTo() {
    return this._to;
  }

  /**
   * CallMessage API is in beta.
   *
   * Send {@link (CallMessage:class)}.
   *
   * @example
   * To send a user-defined-message
   * ```typescript
   * const message = new CallMessage({
   *    content: { key1: 'This is a messsage from the parent call' },
   *    contentType: CallMessage.ContentType.ApplicationJson,
   *    messageType: CallMessage.MessageType.UserDefinedMessage
   * })
   * const outgoingCallMessage: OutgoingCallMessage = await call.sendMessage(message)
   *
   * outgoingCallMessage.addListener(OutgoingCallMessage.Event.Failure, (error) => {
   *    // outgoingCallMessage failed, handle error
   * });
   *
   * outgoingCallMessage.addListener(OutgoingCallMessage.Event.Sent, () => {
   *    // outgoingCallMessage sent
   * })
   * ```
   *
   * @param content - The message content
   * @param contentType - The MIME type for the message. See {@link (CallMessage:namespace).ContentType}.
   * @param messageType - The message type. See {@link (CallMessage:namespace).MessageType}.
   *
   * @returns
   *  A `Promise` that
   *    - Resolves with the OutgoingCallMessage object.
   *    - Rejects when the message is unable to be sent.
   */
  async sendMessage(message) {
    const content = message.getContent();
    const contentType = message.getContentType();
    const messageType = message.getMessageType();
    const voiceEventSid = await _common.NativeModule.call_sendMessage(this._uuid, JSON.stringify(content), contentType, messageType);
    const outgoingCallMessage = new _OutgoingCallMessage.OutgoingCallMessage({
      content,
      contentType,
      messageType,
      voiceEventSid
    });
    return outgoingCallMessage;
  }

  /**
   * Update the caller name displayed in the iOS system incoming call screen.
   *
   * @param newHandle - The new value of the caller's name.
   *
   * @remarks
   * Unsupported platforms:
   * - Android
   *
   * This API is specific to iOS and unavailable in Android. Invoke this method
   * after the incoming call has been reported to CallKit and before the call
   * has been accepted. For example, perform an async request to your app server
   * to fetch the full name of the caller and use this method to replace the
   * default caller name in `from`.
   *
   * @returns
   *  - Resolves when the caller name has been updated.
   */
  async updateCallerHandle(newHandle) {
    switch (_common.Platform.OS) {
      case 'ios':
        return _common.NativeModule.callInvite_updateCallerHandle(this._uuid, newHandle);
      default:
        throw new _UnsupportedPlatformError.UnsupportedPlatformError(`Unsupported platform "${_common.Platform.OS}". This method is only supported on iOS.`);
    }
  }
}

/**
 * Provides enumerations and types used by a {@link (CallInvite:class)
 * | CallInvite object}.
 *
 * @remarks
 *  - See also the {@link (CallInvite:class) | CallInvite class}.
 *
 * @public
 */
exports.CallInvite = CallInvite;
(function (_CallInvite2) {
  /**
   * Options to pass to the native layer when accepting the call.
   */
  /**
   * An enumeration of {@link (CallInvite:class)} states.
   */
  let State = /*#__PURE__*/function (State) {
    State["Pending"] = "pending";
    State["Accepted"] = "accepted";
    State["Rejected"] = "rejected";
    State["Cancelled"] = "cancelled";
    return State;
  }({});
  _CallInvite2.State = State;
  let Event = /*#__PURE__*/function (Event) {
    Event["Accepted"] = "accepted";
    Event["Rejected"] = "rejected";
    Event["Cancelled"] = "cancelled";
    Event["NotificationTapped"] = "notificationTapped";
    Event["MessageReceived"] = "messageReceived";
    return Event;
  }({});
  _CallInvite2.Event = Event;
})(CallInvite || (exports.CallInvite = CallInvite = {}));
//# sourceMappingURL=CallInvite.js.map