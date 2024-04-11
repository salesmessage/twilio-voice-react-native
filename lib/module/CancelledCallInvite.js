function _defineProperty(obj, key, value) { key = _toPropertyKey(key); if (key in obj) { Object.defineProperty(obj, key, { value: value, enumerable: true, configurable: true, writable: true }); } else { obj[key] = value; } return obj; }
function _toPropertyKey(t) { var i = _toPrimitive(t, "string"); return "symbol" == typeof i ? i : String(i); }
function _toPrimitive(t, r) { if ("object" != typeof t || !t) return t; var e = t[Symbol.toPrimitive]; if (void 0 !== e) { var i = e.call(t, r || "default"); if ("object" != typeof i) return i; throw new TypeError("@@toPrimitive must return a primitive value."); } return ("string" === r ? String : Number)(t); }
/**
 * Copyright © 2022 Twilio, Inc. All rights reserved. Licensed under the Twilio
 * license.
 *
 * See LICENSE in the project root for license information.
 */

/**
 * Provides access to information about a cancelled call invite, including the
 * call parameters.
 *
 * @public
 */
export class CancelledCallInvite {
  /**
   * These objects should not be instantiated by consumers of the SDK. All
   * instances of the
   * {@link (CancelledCallInvite:class) | CancelledCallInvite class} should be
   * emitted by the SDK.
   *
   * @param nativeCancelledCallInviteInfo - A dataobject containing the native
   * information of a `CancelledCallInvite`.
   *
   * @internal
   */
  constructor({
    callSid,
    from,
    to
  }) {
    /**
     * A string representing the SID of this call.
     */
    _defineProperty(this, "_callSid", void 0);
    /**
     * Call `from` parameter.
     */
    _defineProperty(this, "_from", void 0);
    /**
     * Call `to` parameter.
     */
    _defineProperty(this, "_to", void 0);
    this._callSid = callSid;
    this._from = from;
    this._to = to;
  }

  /**
   * Get the call SID associated with this `CancelledCallInvite`.
   * @returns - A string representing the call SID.
   */
  getCallSid() {
    return this._callSid;
  }

  /**
   * Get the `from` parameter of the call associated with this
   * `CancelledCallInvite`.
   * @returns - A `string` representing the `from` parameter.
   */
  getFrom() {
    return this._from;
  }

  /**
   * Get the `to` parameter of the call associated with this
   * `CancelledCallInvite`.
   * @returns - A `string` representing the `to` parameter.
   */
  getTo() {
    return this._to;
  }
}
//# sourceMappingURL=CancelledCallInvite.js.map