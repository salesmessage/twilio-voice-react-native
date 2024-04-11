function _defineProperty(obj, key, value) { key = _toPropertyKey(key); if (key in obj) { Object.defineProperty(obj, key, { value: value, enumerable: true, configurable: true, writable: true }); } else { obj[key] = value; } return obj; }
function _toPropertyKey(t) { var i = _toPrimitive(t, "string"); return "symbol" == typeof i ? i : String(i); }
function _toPrimitive(t, r) { if ("object" != typeof t || !t) return t; var e = t[Symbol.toPrimitive]; if (void 0 !== e) { var i = e.call(t, r || "default"); if ("object" != typeof i) return i; throw new TypeError("@@toPrimitive must return a primitive value."); } return ("string" === r ? String : Number)(t); }
/**
 * Copyright © 2022 Twilio, Inc. All rights reserved. Licensed under the Twilio
 * license.
 *
 * See LICENSE in the project root for license information.
 */

import { Call } from './Call';
import { NativeEventEmitter, NativeModule } from './common';
import { InvalidStateError } from './error/InvalidStateError';
import { CallMessage } from './CallMessage';
import { OutgoingCallMessage } from './OutgoingCallMessage';
import { Constants } from './constants';
import { EventEmitter } from 'eventemitter3';

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
export class CallInvite extends EventEmitter {
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
     * Handlers for native callInvite events. Set upon construction so we can
     * dynamically bind events to handlers.
     *
     * @privateRemarks
     * This is done by the constructor so this mapping isn't made every time the
     * {@link (CallInvite:class)._handleNativeEvent} function is invoked.
     */
    _defineProperty(this, "_nativeEventHandler", void 0);
    /**
     * This intermediate native callInvite event handler acts as a "gate".
     * @param nativeCallInviteEvent - A callInvite event directly from the native layer.
     */
    _defineProperty(this, "_handleNativeEvent", nativeCallInviteEvent => {
      const {
        type
      } = nativeCallInviteEvent;
      const handler = this._nativeEventHandler[type];
      if (typeof handler === 'undefined') {
        throw new Error(`Unknown callInvite event type received from the native layer: "${type}".`);
      }
      handler(nativeCallInviteEvent);
    });
    /**
     * Handler for the {@link (CallInvite:namespace).Event.MessageReceived} event.
     * @param nativeCallEvent - The native call event.
     */
    _defineProperty(this, "_handleMessageReceivedEvent", nativeCallInviteEvent => {
      if (nativeCallInviteEvent.type !== Constants.CallEventMessageReceived) {
        throw new Error('Incorrect "callInvite#Received" handler called for type' + `"${nativeCallInviteEvent.type}`);
      }
      const {
        callMessage: callMessageInfo
      } = nativeCallInviteEvent;
      const callMessage = new CallMessage(callMessageInfo);
      this.emit(CallInvite.Event.MessageReceived, callMessage);
    });
    this._uuid = uuid;
    this._callSid = callSid;
    this._customParameters = {
      ...customParameters
    };
    this._from = from;
    this._to = to;
    this._state = state;
    this._nativeEventHandler = {
      /**
       * Call Message
       */
      [Constants.CallEventMessageReceived]: this._handleMessageReceivedEvent
    };
    NativeEventEmitter.addListener(Constants.ScopeCallInvite, this._handleNativeEvent);
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
      throw new InvalidStateError(`Call in state "${this._state}", ` + `expected state "${CallInvite.State.Pending}".`);
    }
    const callInfo = await NativeModule.callInvite_accept(this._uuid, options);
    const call = new Call(callInfo);
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
      throw new InvalidStateError(`Call in state "${this._state}", ` + `expected state "${CallInvite.State.Pending}".`);
    }
    await NativeModule.callInvite_reject(this._uuid);
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
    return NativeModule.callInvite_isValid(this._uuid);
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
    const voiceEventSid = await NativeModule.call_sendMessage(this._uuid, JSON.stringify(content), contentType, messageType);
    const outgoingCallMessage = new OutgoingCallMessage({
      content,
      contentType,
      messageType,
      voiceEventSid
    });
    return outgoingCallMessage;
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
    return State;
  }({});
  _CallInvite2.State = State;
  let Event = /*#__PURE__*/function (Event) {
    Event["MessageReceived"] = "messageReceived";
    return Event;
  }({});
  _CallInvite2.Event = Event;
})(CallInvite || (CallInvite = {}));
//# sourceMappingURL=CallInvite.js.map