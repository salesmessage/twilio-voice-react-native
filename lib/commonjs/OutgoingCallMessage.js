"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.OutgoingCallMessage = void 0;
var _constants = require("./constants");
var _common = require("./common");
var _utility = require("./error/utility");
var _CallMessage = require("./CallMessage");
function _defineProperty(obj, key, value) { key = _toPropertyKey(key); if (key in obj) { Object.defineProperty(obj, key, { value: value, enumerable: true, configurable: true, writable: true }); } else { obj[key] = value; } return obj; }
function _toPropertyKey(t) { var i = _toPrimitive(t, "string"); return "symbol" == typeof i ? i : String(i); }
function _toPrimitive(t, r) { if ("object" != typeof t || !t) return t; var e = t[Symbol.toPrimitive]; if (void 0 !== e) { var i = e.call(t, r || "default"); if ("object" != typeof i) return i; throw new TypeError("@@toPrimitive must return a primitive value."); } return ("string" === r ? String : Number)(t); } /**
 * Copyright © 2022 Twilio, Inc. All rights reserved. Licensed under the Twilio
 * license.
 *
 * See LICENSE in the project root for license information.
 */
/**
 * Defines strict typings for all events emitted by {@link (OutgoingCallMessage:class)
 * | OutgoingCallMessage objects}.
 *
 * @remarks
 * Note that the `on` function is an alias for the `addListener` function.
 * They share identical functionality and either may be used interchangeably.
 *
 * - See also the {@link (CallMessage:class) | CallMessage class}.
 * - See also the {@link (OutgoingCallMessage:namespace) | OutgoingCallMessage namespace}.
 *
 * @public
 */

/**
 * Provides access to information about a outgoingCallMessage, including the call
 * message content, contentType, messageType, and voiceEventSid
 *
 * @remarks
 * Note that the outgoingCallMessage information is fetched as soon as possible from the
 * native layer, but there is no guarantee that all information is immediately
 * available. Methods such as `OutgoingCallMessage.getContent` or `OutgoingCallMessage.getSid`
 * may return `undefined`.
 *
 * As outgoingCallMessage events are received from the native layer, outgoingCallMessage information will
 * propagate from the native layer to the JS layer and become available.
 * Therefore, it is good practice to read information from the outgoingCallMessage after an
 * event occurs, or as events occur.
 *
 * - See the {@link (OutgoingCallMessage:namespace).Event} enum for events emitted by `OutgoingCallMessage`
 *   objects.
 * - See the {@link (OutgoingCallMessage:interface) | OutgoingCallMessage interface} for overloaded event listening
 *   metods.
 * - See the {@link (OutgoingCallMessage:namespace) | OutgoingCallMessage namespace} for types and enumerations
 *   used by this class.
 *
 * @public
 */
class OutgoingCallMessage extends _CallMessage.CallMessage {
  constructor({
    content,
    contentType,
    messageType,
    voiceEventSid
  }) {
    super({
      content,
      contentType,
      messageType,
      voiceEventSid
    });
    /**
     * Handlers for native OutgoingCallMessage events. Set upon construction so we can
     * dynamically bind events to handlers.
     *
     * @privateRemarks
     * This is done by the constructor so this mapping isn't made every time the
     * {@link (OutgoingCallMessage:class)._handleNativeEvent} function is invoked.
     */
    _defineProperty(this, "_nativeEventHandler", void 0);
    /**
     * This intermediate native callMessage event handler acts as a "gate".
     * @param nativeCallMessageEvent - A callMessage event directly from the native layer.
     */
    _defineProperty(this, "_handleNativeEvent", nativeCallMessageEvent => {
      const {
        type
      } = nativeCallMessageEvent;
      const handler = this._nativeEventHandler[type];
      if (typeof handler === 'undefined') {
        throw new Error(`Unknown callMessage event type received from the native layer: "${type}"`);
      }
      if (this.getSid() === nativeCallMessageEvent.voiceEventSid) {
        handler(nativeCallMessageEvent);
      }
    });
    /**
     * Handler for the {@link (OutgoingCallMessage:namespace).Event.Failure} event.
     * @param nativeCallMessageEvent - The native callMessage event.
     */
    _defineProperty(this, "_handleFailureEvent", nativeCallMessageEvent => {
      if (nativeCallMessageEvent.type !== _constants.Constants.CallEventMessageFailure) {
        throw new Error('Incorrect "outgoingCallMessage#Failure" handler called for type' + `"${nativeCallMessageEvent.type}`);
      }
      const {
        message,
        code
      } = nativeCallMessageEvent.error;
      const error = (0, _utility.constructTwilioError)(message, code);
      this.emit(OutgoingCallMessage.Event.Failure, error);
    });
    /**
     * Handler for the {@link (OutgoingCallMessage:namespace).Event.Sent} event.
     * @param nativeCallMessageEvent - The native callMessage event.
     */
    _defineProperty(this, "_handleSentEvent", nativeCallMessageEvent => {
      if (nativeCallMessageEvent.type !== _constants.Constants.CallEventMessageSent) {
        throw new Error('Incorrect "outgoingCallMessage#Sent" handler called for type' + `"${nativeCallMessageEvent.type}"`);
      }
      this.emit(OutgoingCallMessage.Event.Sent);
    });
    this._nativeEventHandler = {
      /**
       * Sending Call Message State
       */
      [_constants.Constants.CallEventMessageFailure]: this._handleFailureEvent,
      [_constants.Constants.CallEventMessageSent]: this._handleSentEvent
    };
    _common.NativeEventEmitter.addListener(_constants.Constants.ScopeCallMessage, this._handleNativeEvent);
  }
}

/**
 * Namespace for enumerations and types used by
 * {@link (OutgoingCallMessage:class) | OutgoingCallMessage objects}.
 *
 * @remarks
 *  - See also the {@link (OutgoingCallMessage:class) | OutgoingCallMessage class}.
 *  - See also the {@link (OutgoingCallMessage:interface) | OutgoingCallMessage interface}.
 *
 * @public
 */
exports.OutgoingCallMessage = OutgoingCallMessage;
(function (_OutgoingCallMessage2) {
  let Event = /*#__PURE__*/function (Event) {
    Event["Failure"] = "failure";
    Event["Sent"] = "sent";
    return Event;
  }({});
  _OutgoingCallMessage2.Event = Event;
})(OutgoingCallMessage || (exports.OutgoingCallMessage = OutgoingCallMessage = {}));
//# sourceMappingURL=OutgoingCallMessage.js.map