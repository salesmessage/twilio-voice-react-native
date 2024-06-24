function _defineProperty(obj, key, value) { key = _toPropertyKey(key); if (key in obj) { Object.defineProperty(obj, key, { value: value, enumerable: true, configurable: true, writable: true }); } else { obj[key] = value; } return obj; }
function _toPropertyKey(t) { var i = _toPrimitive(t, "string"); return "symbol" == typeof i ? i : String(i); }
function _toPrimitive(t, r) { if ("object" != typeof t || !t) return t; var e = t[Symbol.toPrimitive]; if (void 0 !== e) { var i = e.call(t, r || "default"); if ("object" != typeof i) return i; throw new TypeError("@@toPrimitive must return a primitive value."); } return ("string" === r ? String : Number)(t); }
import { TwilioError } from './TwilioError';

/**
 * Error describing that an SDK function is invoked with an invalid argument.
 *
 * @public
 */
export class InvalidArgumentError extends TwilioError {
  constructor(message) {
    super(message);
    _defineProperty(this, "description", 'Invalid argument error.');
    _defineProperty(this, "explanation", 'The SDK has encountered a situation where invalid arguments were passed.');
    Object.setPrototypeOf(this, InvalidArgumentError.prototype);
    this.name = InvalidArgumentError.name;
  }
}
//# sourceMappingURL=InvalidArgumentError.js.map