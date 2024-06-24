function _defineProperty(obj, key, value) { key = _toPropertyKey(key); if (key in obj) { Object.defineProperty(obj, key, { value: value, enumerable: true, configurable: true, writable: true }); } else { obj[key] = value; } return obj; }
function _toPropertyKey(t) { var i = _toPrimitive(t, "string"); return "symbol" == typeof i ? i : String(i); }
function _toPrimitive(t, r) { if ("object" != typeof t || !t) return t; var e = t[Symbol.toPrimitive]; if (void 0 !== e) { var i = e.call(t, r || "default"); if ("object" != typeof i) return i; throw new TypeError("@@toPrimitive must return a primitive value."); } return ("string" === r ? String : Number)(t); }
/**
 * Copyright © 2022 Twilio, Inc. All rights reserved. Licensed under the Twilio
 * license.
 *
 * See LICENSE in the project root for license information.
 */

import { EventEmitter } from 'eventemitter3';
import { AudioDevice } from './AudioDevice';
import { Call } from './Call';
import { CallInvite } from './CallInvite';
import { NativeEventEmitter, NativeModule, Platform } from './common';
import { Constants } from './constants';
import { InvalidArgumentError } from './error/InvalidArgumentError';
import { UnsupportedPlatformError } from './error/UnsupportedPlatformError';
import { constructTwilioError } from './error/utility';

/**
 * Defines strict typings for all events emitted by {@link (Voice:class)
 * | Voice objects}.
 *
 * @remarks
 * Note that the `on` function is an alias for the `addListener` function.
 * They share identical functionality and either may be used interchangeably.
 *
 * - See also the {@link (Voice:class) | Voice class}.
 * - See also the {@link (Voice:namespace) | Voice namespace}.
 *
 * @public
 */

/**
 * Main entry-point of the Voice SDK. Provides access to the entire feature-set
 * of the library.
 *
 * @example
 * Usage:
 * ```
 * const token = '...';
 *
 * const voice = new Voice();
 *
 * voice.on(Voice.Event.CallInvite, (callInvite: CallInvite) => {
 *   callInvite.accept();
 * });
 *
 * voice.register(token);
 * ```
 *
 * @remarks
 *  - See also the {@link (Voice:namespace).Event} enum for events emitted by
 *    `Voice` objects.
 *  - See also the {@link (Voice:interface) | Voice interface} for events
 *    emitted by this class and associated types.
 *  - See also the {@link (Voice:namespace) | Voice namespace} for types and
 *    enumerations used by this class.
 *
 * @public
 */
export class Voice extends EventEmitter {
  /**
   * Main entry-point of the Voice SDK. Provides access to the entire
   * feature-set of the library.
   */
  constructor() {
    super();
    /**
     * Handlers for native voice events. Set upon construction so we can
     * dynamically bind events to handlers.
     *
     * @privateRemarks
     * This is done by the constructor so this mapping isn't made every time the
     * {@link (Voice:class)._handleNativeEvent} function is invoked.
     */
    _defineProperty(this, "_nativeEventHandler", void 0);
    /**
     * Intermediary event handler for `Voice`-level events. Ensures that the type
     * of the incoming event is expected and invokes the proper event listener.
     * @param nativeVoiceEvent - A `Voice` event directly from the native layer.
     */
    _defineProperty(this, "_handleNativeEvent", nativeVoiceEvent => {
      const {
        type
      } = nativeVoiceEvent;
      const handler = this._nativeEventHandler[type];
      if (typeof handler === 'undefined') {
        throw new Error(`Unknown voice event type received from the native layer: "${type}".`);
      }
      handler(nativeVoiceEvent);
    });
    /**
     * Call invite handler. Creates a {@link (CallInvite:class)} from the info
     * raised by the native layer and emits it.
     * @param nativeVoiceEvent - A `Voice` event directly from the native layer.
     */
    _defineProperty(this, "_handleCallInvite", nativeVoiceEvent => {
      if (nativeVoiceEvent.type !== Constants.VoiceEventTypeValueIncomingCallInvite) {
        throw new Error('Incorrect "voice#callInvite" handler called for type ' + `"${nativeVoiceEvent.type}".`);
      }
      const {
        callInvite: callInviteInfo
      } = nativeVoiceEvent;
      const callInvite = new CallInvite(callInviteInfo, CallInvite.State.Pending);
      this.emit(Voice.Event.CallInvite, callInvite);
    });
    /**
     * Error event handler. Creates an error from the namespace
     * {@link TwilioErrors} from the info raised by the native layer and emits it.
     * @param nativeVoiceEvent - A `Voice` event directly from the native layer.
     */
    _defineProperty(this, "_handleError", nativeVoiceEvent => {
      if (nativeVoiceEvent.type !== Constants.VoiceEventError) {
        throw new Error('Incorrect "voice#error" handler called for type ' + `"${nativeVoiceEvent.type}".`);
      }
      const {
        error: {
          code,
          message
        }
      } = nativeVoiceEvent;
      const error = constructTwilioError(message, code);
      this.emit(Voice.Event.Error, error);
    });
    /**
     * Registered event handler. Emits a
     * {@link (Voice:namespace).Event.Registered} event.
     */
    _defineProperty(this, "_handleRegistered", nativeVoiceEvent => {
      if (nativeVoiceEvent.type !== Constants.VoiceEventRegistered) {
        throw new Error('Incorrect "voice#error" handler called for type ' + `"${nativeVoiceEvent.type}".`);
      }
      this.emit(Voice.Event.Registered);
    });
    /**
     * Unregistered event handler. Emits a
     * {@link (Voice:namespace).Event.Unregistered} event.
     */
    _defineProperty(this, "_handleUnregistered", nativeVoiceEvent => {
      if (nativeVoiceEvent.type !== Constants.VoiceEventUnregistered) {
        throw new Error('Incorrect "voice#error" handler called for type ' + `"${nativeVoiceEvent.type}".`);
      }
      this.emit(Voice.Event.Unregistered);
    });
    _defineProperty(this, "_handleMissedCallNotificationTapped", nativeVoiceEvent => {
      if (nativeVoiceEvent.type !== Constants.VoiceEventMissedCallNotificationTapped) {
        throw new Error('Incorrect "voice#callInviteNotificationTapped" handler called for ' + `type "${nativeVoiceEvent.type}".`);
      }

      // this.emit(Voice.Event.CallInviteNotificationTapped);

      const {
        callInvite: callInviteInfo
      } = nativeVoiceEvent;
      this.emit(Voice.Event.MissedCallNotificationTapped, callInviteInfo);
    });
    /**
     * Audio devices updated event handler. Generates a new list of
     * {@link (AudioDevice:class) | AudioDevice objects} and emits it.
     * @param nativeVoiceEvent - A `Voice` event directly from the native layer.
     */
    _defineProperty(this, "_handleAudioDevicesUpdated", nativeVoiceEvent => {
      if (nativeVoiceEvent.type !== Constants.VoiceEventAudioDevicesUpdated) {
        throw new Error('Incorrect "voice#audioDevicesUpdated" handler called for type ' + `"${nativeVoiceEvent.type}".`);
      }
      const {
        audioDevices: audioDeviceInfos,
        selectedDevice: selectedDeviceInfo
      } = nativeVoiceEvent;
      const audioDevices = audioDeviceInfos.map(audioDeviceInfo => new AudioDevice(audioDeviceInfo));
      const selectedDevice = typeof selectedDeviceInfo !== 'undefined' && selectedDeviceInfo !== null ? new AudioDevice(selectedDeviceInfo) : undefined;
      this.emit(Voice.Event.AudioDevicesUpdated, audioDevices, selectedDevice);
    });
    this._nativeEventHandler = {
      /**
       * Common
       */
      [Constants.VoiceEventError]: this._handleError,
      /**
       * Call Invite
       */
      [Constants.VoiceEventTypeValueIncomingCallInvite]: this._handleCallInvite,
      /**
       * Registration
       */
      [Constants.VoiceEventRegistered]: this._handleRegistered,
      [Constants.VoiceEventUnregistered]: this._handleUnregistered,
      [Constants.VoiceEventMissedCallNotificationTapped]: this._handleMissedCallNotificationTapped,
      /**
       * Audio Devices
       */
      [Constants.VoiceEventAudioDevicesUpdated]: this._handleAudioDevicesUpdated
    };
    NativeEventEmitter.addListener(Constants.ScopeVoice, this._handleNativeEvent);
  }

  /**
   * Connect for devices on Android platforms.
   */
  async _connect_android(token, params) {
    const callInfo = await NativeModule.voice_connect_android(token, params);
    return new Call(callInfo);
  }

  /**
   * Connect for devices on iOS platforms.
   */
  async _connect_ios(token, params, contactHandle) {
    const parsedContactHandle = contactHandle === '' ? 'Default Contact' : contactHandle;
    const callInfo = await NativeModule.voice_connect_ios(token, params, parsedContactHandle);
    return new Call(callInfo);
  }
  /**
   * Create an outgoing call.
   *
   * @remarks
   * Note that the resolution of the returned `Promise` does not imply any call
   * event occurring, such as answered or rejected.
   * The `contactHandle` parameter is only required for iOS apps. Currently the
   * parameter does have any effect on Android apps and can be ignored.
   * `Default Contact` will appear in the iOS call history if the value is empty
   * or not provided.
   *
   * @param token - A Twilio Access Token, usually minted by an
   * authentication-gated endpoint using a Twilio helper library.
   * @param options - Connect options.
   *  See {@link (Voice:namespace).ConnectOptions}.
   *
   * @returns
   * A `Promise` that
   *  - Resolves with a call when the call is created.
   *  - Rejects:
   *    * When a call is not able to be created on the native layer.
   *    * With an {@link TwilioErrors.InvalidArgumentError} when invalid
   *      arguments are passed.
   */
  async connect(token, {
    contactHandle = 'Default Contact',
    params = {}
  } = {}) {
    if (typeof token !== 'string') {
      throw new InvalidArgumentError('Argument "token" must be of type "string".');
    }
    if (typeof contactHandle !== 'string') {
      throw new InvalidArgumentError('Optional argument "contactHandle" must be undefined or of type' + ' "string".');
    }
    if (typeof params !== 'object') {
      throw new InvalidArgumentError('Optional argument "params" must be undefined or of type "object".');
    }
    for (const [key, value] of Object.entries(params)) {
      if (typeof value !== 'string') {
        throw new InvalidArgumentError(`Voice.ConnectOptions.params["${key}"] must be of type string`);
      }
    }
    switch (Platform.OS) {
      case 'ios':
        return this._connect_ios(token, params, contactHandle);
      case 'android':
        return this._connect_android(token, params);
      default:
        throw new UnsupportedPlatformError(`Unsupported platform "${Platform.OS}". Expected "android" or "ios".`);
    }
  }

  /**
   * Get the version of the native SDK. Note that this is not the version of the
   * React Native SDK, this is the version of the mobile SDK that the RN SDK is
   * utilizing.
   * @returns
   * A `Promise` that
   *  - Resolves with a string representing the version of the native SDK.
   */
  getVersion() {
    return NativeModule.voice_getVersion();
  }
  canUseFullScreenIntent() {
    return NativeModule.voice_canUseFullScreenIntent();
  }

  /**
   * Get the Device token from the native layer.
   * @returns a Promise that resolves with a string representing the Device
   * token.
   */
  getDeviceToken() {
    return NativeModule.voice_getDeviceToken();
  }

  /**
   * Get a list of existing calls, ongoing and pending. This will not return any
   * call that has finished.
   * @returns
   * A `Promise` that
   *  - Resolves with a mapping of `Uuid`s to {@link (Call:class)}s.
   */
  async getCalls() {
    const callInfos = await NativeModule.voice_getCalls();
    const callsMap = new Map(callInfos.map(callInfo => [callInfo.uuid, new Call(callInfo)]));
    return callsMap;
  }

  /**
   * Get a list of pending call invites.
   *
   * @remarks
   * This list will not contain any call invites that have been "settled"
   * (answered or rejected).
   *
   * @returns
   * A `Promise` that
   *  - Resolves with a mapping of `Uuid`s to {@link (CallInvite:class)}s.
   */
  async getCallInvites() {
    const callInviteInfos = await NativeModule.voice_getCallInvites();
    const callInvitesMap = new Map(callInviteInfos.map(callInviteInfo => [callInviteInfo.uuid, new CallInvite(callInviteInfo, CallInvite.State.Pending)]));
    return callInvitesMap;
  }

  /**
   * Register this device for incoming calls.
   * @param token - A Twilio Access Token.
   * @returns
   * A `Promise` that
   *  - Resolves when the device has been registered.
   */
  register(token) {
    return NativeModule.voice_register(token);
  }

  /**
   * Unregister this device for incoming calls.
   * @param token - A Twilio Access Token.
   * @returns
   * A `Promise` that
   *  - Resolves when the device has been unregistered.
   */
  unregister(token) {
    return NativeModule.voice_unregister(token);
  }

  /**
   * Get audio device information from the native layer.
   * @returns
   * A `Promise` that
   *  - Resolves with a list of the native device's audio devices and the
   *    currently selected device.
   */
  async getAudioDevices() {
    const {
      audioDevices: audioDeviceInfos,
      selectedDevice: selectedDeviceInfo
    } = await NativeModule.voice_getAudioDevices();
    const audioDevices = audioDeviceInfos.map(audioDeviceInfo => new AudioDevice(audioDeviceInfo));
    const selectedDevice = typeof selectedDeviceInfo !== 'undefined' ? new AudioDevice(selectedDeviceInfo) : undefined;
    return selectedDevice ? {
      audioDevices,
      selectedDevice
    } : {
      audioDevices
    };
  }

  /**
   * Show the native AV route picker.
   *
   * @remarks
   * Unsupported platforms:
   * - Android
   *
   * This API is specific to iOS and unavailable in Android. If this API is
   * invoked on Android, there will be no operation and the returned `Promise`
   * will immediately resolve with `null`.
   *
   * @returns
   * A `Promise` that
   *  - Resolves when the AV Route Picker View is shown.
   */
  showAvRoutePickerView() {
    return NativeModule.voice_showNativeAvRoutePicker();
  }

  /**
   * Initialize a Push Registry instance inside the SDK for handling
   * PushKit device token updates and receiving push notifications.
   *
   * @remarks
   * Unsupported platforms:
   * - Android
   *
   * This API is specific to iOS and unavailable in Android.
   * Use this method if the application does not have an iOS PushKit
   * module and wishes to delegate the event handling to the SDK.
   * Call this method upon launching the app to guarantee that incoming
   * call push notifications will be surfaced to the users, especially when
   * the app is not running in the foreground.
   *
   * @return
   * A `Promise` that
   *  - Resolves when the initialization is done.
   */
  async initializePushRegistry() {
    switch (Platform.OS) {
      case 'ios':
        return NativeModule.voice_initializePushRegistry();
      default:
        throw new UnsupportedPlatformError(`Unsupported platform "${Platform.OS}". This method is only supported on iOS.`);
    }
  }

  /**
   * Custom iOS CallKit configuration.
   *
   * @param configuration - iOS CallKit configuration options.
   *
   * @remarks
   * Unsupported platforms:
   * - Android
   *
   * See {@link CallKit} for more information.
   *
   * @returns
   * A `Promise` that
   *  - Resolves when the configuration has been applied.
   *  - Rejects if the configuration is unable to be applied.
   */
  async setCallKitConfiguration(configuration) {
    switch (Platform.OS) {
      case 'ios':
        return NativeModule.voice_setCallKitConfiguration(configuration);
      default:
        throw new UnsupportedPlatformError(`Unsupported platform "${Platform.OS}". This method is only supported on iOS.`);
    }
  }
}

/**
 * Provides enumerations and types used by {@link (Voice:class)
 * | Voice objects}.
 *
 * @remarks
 * - See also the {@link (Voice:class) | Voice class}.
 * - See also the {@link (Voice:interface) | Voice interface}.
 *
 * @public
 */
(function (_Voice2) {
  /**
   * Options to pass to the {@link (Voice:class).connect} method.
   */
  /**
   * Enumeration of all event strings emitted by {@link (Voice:class)} objects.
   */
  let Event = /*#__PURE__*/function (Event) {
    Event["AudioDevicesUpdated"] = "audioDevicesUpdated";
    Event["CallInvite"] = "callInvite";
    Event["Error"] = "error";
    Event["Registered"] = "registered";
    Event["Unregistered"] = "unregistered";
    Event["MissedCallNotificationTapped"] = "missedCallNotificationTapped";
    return Event;
  }({});
  _Voice2.Event = Event;
})(Voice || (Voice = {}));
//# sourceMappingURL=Voice.js.map