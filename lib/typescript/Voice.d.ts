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
import { CancelledCallInvite } from './CancelledCallInvite';
import type { TwilioError } from './error/TwilioError';
import type { CallKit } from './type/CallKit';
import type { Uuid } from './type/common';
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
export declare interface Voice {
    /**
     * ------------
     * Emit Typings
     * ------------
     */
    /** @internal */
    emit(voiceEvent: Voice.Event.AudioDevicesUpdated, audioDevices: AudioDevice[], selectedDevice?: AudioDevice): boolean;
    /** @internal */
    emit(voiceEvent: Voice.Event.CallInvite, callInvite: CallInvite): boolean;
    /** @internal */
    emit(voiceEvent: Voice.Event.CallInviteAccepted, callInvite: CallInvite, call: Call): boolean;
    /** @internal */
    emit(voiceEvent: Voice.Event.CallInviteNotificationTapped): boolean;
    /** @internal */
    emit(voiceEvent: Voice.Event.CallInviteRejected, callInvite: CallInvite): boolean;
    /** @internal */
    emit(voiceEvent: Voice.Event.CancelledCallInvite, cancelledCallInvite: CancelledCallInvite, error?: TwilioError): boolean;
    /** @internal */
    emit(voiceEvent: Voice.Event.Error, error: TwilioError): boolean;
    /** @internal */
    emit(voiceEvent: Voice.Event.Registered): boolean;
    /** @internal */
    emit(voiceEvent: Voice.Event.Unregistered): boolean;
    /** @internal */
    emit(voiceEvent: Voice.Event, ...args: any[]): boolean;
    /**
     * ----------------
     * Listener Typings
     * ----------------
     */
    /**
     * Audio devices updated event. Raised when the list of audio devices changes.
     *
     * @example
     * ```typescript
     * voice.addListener(Voice.Event.AudioDevicesUpdated, () => {
     *   // the list of available audio devices has changed and/or the selected
     *   // audio device has been changed
     * });
     * ```
     *
     * @param audioDevicesUpdatedEvent - The raised event string.
     * @param listener - A listener function that will be invoked when the event
     * is raised.
     * @returns - The call object.
     */
    addListener(audioDevicesUpdatedEvent: Voice.Event.AudioDevicesUpdated, listener: Voice.Listener.AudioDevicesUpdated): this;
    /** {@inheritDoc (Voice:interface).(addListener:1)} */
    on(audioDevicesUpdatedEvent: Voice.Event.AudioDevicesUpdated, listener: Voice.Listener.AudioDevicesUpdated): this;
    /**
     * Call invite event. Raised when an incoming call invite is received.
     *
     * @example
     * ```typescript
     * voice.addListener(Voice.Event.CallInvite, (callInvite: CallInvite) => {
     *   // handle the incoming call invite
     * });
     * ```
     *
     * @param callInviteEvent - The raised event string.
     * @param listener - A listener function that will be invoked when the event
     * is raised.
     * @returns - The call object.
     */
    addListener(callInviteEvent: Voice.Event.CallInvite, listener: Voice.Listener.CallInvite): this;
    /** {@inheritDoc (Voice:interface).(addListener:2)} */
    on(callInviteEvent: Voice.Event.CallInvite, listener: Voice.Listener.CallInvite): this;
    /**
     * Call invite accepted event. Raised when a pending incoming call invite has
     * been accepted.
     *
     * @remarks
     * This event is raised when call invites are accepted outside of the SDK,
     * i.e. through the native iOS or Android UI.
     *
     * @example
     * ```typescript
     * voice.addListener(Voice.Event.CallInviteAccepted, (callInvite: CallInvite, call: Call) => {
     *   // handle the incoming call invite and the call associated with it
     * });
     * ```
     *
     * @param callInviteAcceptedEvent - The raised event string.
     * @param listener - A listener function that will be invoked when the event
     * is raised.
     * @returns - The call object.
     */
    addListener(callInviteAcceptedEvent: Voice.Event.CallInviteAccepted, listener: Voice.Listener.CallInviteAccepted): this;
    /** {@inheritDoc (Voice:interface).(addListener:3)} */
    on(callInviteAcceptedEvent: Voice.Event.CallInviteAccepted, listener: Voice.Listener.CallInviteAccepted): this;
    /**
     * Call invite notification tapped event. Raised when the body of a push
     * notification of a pending call invite has been tapped.
     *
     * @remarks
     * Unsupported platforms:
     * - iOS
     *
     * This event is raised by the native layer, i.e. through the native Android
     * UI.
     *
     * This event is never raised when the application is running on the iOS
     * platform due to CallKit. Attaching a listener while running on an iOS
     * device is a no-operation, the listener will never be invoked, but no
     * error will occur.
     *
     * @example
     * ```typescript
     * voice.addEventListener(Voice.Event.CallInviteNotificationTapped, () => {
     *   // handle the tapped event
     *   // for example, your app could navigate to a call invite screen
     * });
     * ```
     */
    addListener(callInviteNotificationTappedEvent: Voice.Event.CallInviteNotificationTapped, listener: Voice.Listener.CallInviteNotificationTapped): this;
    /** {@inheritDoc (Voice:interface).(addListener:4)} */
    on(callInviteNotificationTappedEvent: Voice.Event.CallInviteNotificationTapped, listener: Voice.Listener.CallInviteNotificationTapped): this;
    addListener(missedCallNotificationTappedEvent: Voice.Event.CallInviteNotificationTapped, listener: Voice.Listener.MissedCallNotificationTapped): this;
    /** {@inheritDoc (Voice:interface).(addListener:4)} */
    on(callInviteNotificationTappedEvent: Voice.Event.CallInviteNotificationTapped, listener: Voice.Listener.CallInviteNotificationTapped): this;
    /**
     * Call invite rejected event. Raised when a pending incoming call invite has
     * been rejected.
     *
     * @remarks
     * This event is raised when call invites are rejected outside of the SDK,
     * i.e. through the native iOS or Android UI.
     *
     * @example
     * ```typescript
     * voice.addListener(Voice.Event.CallInviteRejected, (callInvite: CallInvite) => {
     *   // handle the rejection of the incoming call invite
     * });
     * ```
     *
     * @param callInviteRejectedEvent - The raised event string.
     * @param listener - A listener function that will be invoked when the event
     * is raised.
     * @returns - The call object.
     */
    addListener(callInviteRejectedEvent: Voice.Event.CallInviteRejected, listener: Voice.Listener.CallInviteRejected): this;
    /** {@inheritDoc (Voice:interface).(addListener:5)} */
    on(callInviteRejectedEvent: Voice.Event.CallInviteRejected, listener: Voice.Listener.CallInviteRejected): this;
    /**
     * Cancelled call invite event. Raised when a pending incoming call invite has
     * been cancelled and is no longer valid.
     *
     * @example
     * ```typescript
     * voice.addListener(Voice.Event.CancelledCallInvite, (cancelledCallInvite: CancelledCallInvite) => {
     *   // handle the cancellation of the incoming call invite
     * });
     * ```
     *
     * @param cancelledCallInviteEvent - The raised event string.
     * @param listener - A listener function that will be invoked when the event
     * is raised.
     * @returns - The call object.
     */
    addListener(cancelledCallInviteEvent: Voice.Event.CancelledCallInvite, listener: Voice.Listener.CancelledCallInvite): this;
    /** {@inheritDoc (Voice:interface).(addListener:6)} */
    on(cancelledCallInviteEvent: Voice.Event.CancelledCallInvite, listener: Voice.Listener.CancelledCallInvite): this;
    /**
     * Error event. Raised when the SDK encounters an error.
     *
     * @example
     * ```typescript
     * voice.addListener(Voice.Event.Error, (error: TwilioError.GenericError) => {
     *   // handle a generic Voice SDK error
     * });
     * ```
     *
     * @param errorEvent - The raised event string.
     * @param listener - A listener function that will be invoked when the event
     * is raised.
     * @returns - The call object.
     */
    addListener(errorEvent: Voice.Event.Error, listener: Voice.Listener.Error): this;
    /** {@inheritDoc (Voice:interface).(addListener:7)} */
    on(errorEvent: Voice.Event.Error, listener: Voice.Listener.Error): this;
    /**
     * Registered event. Raised when the SDK is registered for incoming calls.
     *
     * @example
     * ```typescript
     * voice.addListener(Voice.Event.Registered, () => {
     *   // handle successful registration for incoming calls
     * });
     * ```
     *
     * @param registeredEvent - The raised event string.
     * @param listener - A listener function that will be invoked when the event
     * is raised.
     * @returns - The call object.
     */
    addListener(registeredEvent: Voice.Event.Registered, listener: Voice.Listener.Registered): this;
    /** {@inheritDoc (Voice:interface).(addListener:8)} */
    on(registeredEvent: Voice.Event.Registered, listener: Voice.Listener.Registered): this;
    /**
     * Unregistered event. Raised when the SDK is unregistered for incoming calls.
     *
     * @example
     * ```typescript
     * voice.addListener(Voice.Event.Unregistered, () => {
     *   // handle successful unregistration for incoming calls
     * });
     * ```
     *
     * @param unregisteredEvent - The raised event string.
     * @param listener - A listener function that will be invoked when the event
     * is raised.
     * @returns - The call object.
     */
    addListener(unregisteredEvent: Voice.Event.Unregistered, listener: Voice.Listener.Unregistered): this;
    /** {@inheritDoc (Voice:interface).(addListener:9)} */
    on(unregisteredEvent: Voice.Event.Unregistered, listener: Voice.Listener.Unregistered): this;
    /**
     * Generic event listener typings.
     * @param voiceEvent - The raised event string.
     * @param listener - A listener function that will be invoked when the event
     * is raised.
     * @returns - The call object.
     */
    addListener(voiceEvent: Voice.Event, listener: Voice.Listener.Generic): this;
    /** {@inheritDoc (Voice:interface).(addListener:10)} */
    on(voiceEvent: Voice.Event, listener: Voice.Listener.Generic): this;
}
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
export declare class Voice extends EventEmitter {
    /**
     * Handlers for native voice events. Set upon construction so we can
     * dynamically bind events to handlers.
     *
     * @privateRemarks
     * This is done by the constructor so this mapping isn't made every time the
     * {@link (Voice:class)._handleNativeEvent} function is invoked.
     */
    private _nativeEventHandler;
    /**
     * Main entry-point of the Voice SDK. Provides access to the entire
     * feature-set of the library.
     */
    constructor();
    /**
     * Connect for devices on Android platforms.
     */
    private _connect_android;
    /**
     * Connect for devices on iOS platforms.
     */
    private _connect_ios;
    /**
     * Intermediary event handler for `Voice`-level events. Ensures that the type
     * of the incoming event is expected and invokes the proper event listener.
     * @param nativeVoiceEvent - A `Voice` event directly from the native layer.
     */
    private _handleNativeEvent;
    /**
     * Call invite handler. Creates a {@link (CallInvite:class)} from the info
     * raised by the native layer and emits it.
     * @param nativeVoiceEvent - A `Voice` event directly from the native layer.
     */
    private _handleCallInvite;
    /**
     * Call invite accepted handler. Creates a {@link (CallInvite:class)} and a
     * {@link (Call:class)} from the info raised by the native layer and emits it.
     * @param nativeVoiceEvent - A `Voice` event directly from the native layer.
     */
    private _handleCallInviteAccepted;
    /**
     * Call invite rejected handler. Creates a {@link (CallInvite:class)} from the
     * info raised by the native layer and emits it.
     * @param nativeVoiceEvent - A `Voice` event directly from the native layer.
     */
    private _handleCallInviteRejected;
    /**
     * Call invite notification tapped handler. This event can be used to
     * navigate to a call invite screen.
     * @param nativeVoiceEvent - A `Voice` event directly from the native layer.
     */
    private _handleCallInviteNotificationTapped;
    private _handleMissedCallNotificationTapped;
    /**
     * Call invite cancelled handler. Creates a
     * {@link (CancelledCallInvite:class)} from the info raised by the native
     * layer and emits it.
     * @param nativeVoiceEvent - A `Voice` event directly from the native layer.
     */
    private _handleCancelledCallInvite;
    /**
     * Error event handler. Creates an error from the namespace
     * {@link TwilioErrors} from the info raised by the native layer and emits it.
     * @param nativeVoiceEvent - A `Voice` event directly from the native layer.
     */
    private _handleError;
    /**
     * Registered event handler. Emits a
     * {@link (Voice:namespace).Event.Registered} event.
     */
    private _handleRegistered;
    /**
     * Unregistered event handler. Emits a
     * {@link (Voice:namespace).Event.Unregistered} event.
     */
    private _handleUnregistered;
    /**
     * Audio devices updated event handler. Generates a new list of
     * {@link (AudioDevice:class) | AudioDevice objects} and emits it.
     * @param nativeVoiceEvent - A `Voice` event directly from the native layer.
     */
    private _handleAudioDevicesUpdated;
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
    connect(token: string, { contactHandle, params, }?: Voice.ConnectOptions): Promise<Call>;
    /**
     * Get the version of the native SDK. Note that this is not the version of the
     * React Native SDK, this is the version of the mobile SDK that the RN SDK is
     * utilizing.
     * @returns
     * A `Promise` that
     *  - Resolves with a string representing the version of the native SDK.
     */
    getVersion(): Promise<string>;
    canUseFullScreenIntent(): Promise<boolean>;
    /**
     * Get the Device token from the native layer.
     * @returns a Promise that resolves with a string representing the Device
     * token.
     */
    getDeviceToken(): Promise<string>;
    /**
     * Get a list of existing calls, ongoing and pending. This will not return any
     * call that has finished.
     * @returns
     * A `Promise` that
     *  - Resolves with a mapping of `Uuid`s to {@link (Call:class)}s.
     */
    getCalls(): Promise<ReadonlyMap<Uuid, Call>>;
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
    getCallInvites(): Promise<ReadonlyMap<Uuid, CallInvite>>;
    /**
     * Register this device for incoming calls.
     * @param token - A Twilio Access Token.
     * @returns
     * A `Promise` that
     *  - Resolves when the device has been registered.
     */
    register(token: string): Promise<void>;
    /**
     * Unregister this device for incoming calls.
     * @param token - A Twilio Access Token.
     * @returns
     * A `Promise` that
     *  - Resolves when the device has been unregistered.
     */
    unregister(token: string): Promise<void>;
    /**
     * Get audio device information from the native layer.
     * @returns
     * A `Promise` that
     *  - Resolves with a list of the native device's audio devices and the
     *    currently selected device.
     */
    getAudioDevices(): Promise<{
        audioDevices: AudioDevice[];
        selectedDevice?: AudioDevice;
    }>;
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
    showAvRoutePickerView(): Promise<void>;
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
    initializePushRegistry(): Promise<void>;
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
    setCallKitConfiguration(configuration: CallKit.ConfigurationOptions): Promise<void>;
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
export declare namespace Voice {
    /**
     * Options to pass to the {@link (Voice:class).connect} method.
     */
    type ConnectOptions = {
        /**
         * Custom parameters to send to the TwiML Application.
         */
        params?: Record<string, string>;
        /**
         * A CallKit display name that will show in the call history as the contact
         * handle.
         *
         * @remarks
         * Unsupported platforms:
         * - Android
         */
        contactHandle?: string;
    };
    /**
     * Enumeration of all event strings emitted by {@link (Voice:class)} objects.
     */
    enum Event {
        /**
         * Raised when there is a change in available audio devices.
         *
         * @remarks
         *
         * See {@link (Voice:interface).(addListener:1)
         * | Voice.addListener(AudioDevicesUpdated)}.
         */
        'AudioDevicesUpdated' = "audioDevicesUpdated",
        /**
         * Raised when there is an incoming call invite.
         *
         * @remarks
         *
         * See {@link (Voice:interface).(addListener:2)
         * | Voice.addListener(CallInvite)}.
         */
        'CallInvite' = "callInvite",
        /**
         * Raised when an incoming call invite has been accepted.
         *
         * This event can be raised either through the SDK or outside of the SDK
         * (i.e. through native UI/UX such as push notifications).
         *
         * @remarks
         *
         * See {@link (Voice:interface).(addListener:3)
         * | Voice.addListener(CallInviteAccepted)}.
         */
        'CallInviteAccepted' = "callInviteAccepted",
        /**
         * Raised when the notification for an incoming call invite has been tapped.
         *
         * This event is raised only from the native layer, through the push
         * notification.
         *
         * @remarks
         *
         * See {@link (Voice:interface).(addListener:4)}
         */
        'CallInviteNotificationTapped' = "callInviteNotificationTapped",
        'MissedCallNotificationTapped' = "missedCallNotificationTapped",
        /**
         * Raised when an incoming call invite has been rejected.
         *
         * This event can be raised either through the SDK or outside of the SDK
         * (i.e. through native UI/UX such as push notifications).
         *
         * @remarks
         *
         * See {@link (Voice:interface).(addListener:5)
         * | Voice.addListener(CallInviteRejected)}.
         */
        'CallInviteRejected' = "callInviteRejected",
        /**
         * Raised when an incoming call invite has been cancelled, thus invalidating
         * the associated call invite.
         *
         * @remarks
         *
         * See {@link (Voice:interface).(addListener:6)
         * | Voice.addListener(CancelledCallInvite)}.
         */
        'CancelledCallInvite' = "cancelledCallInvite",
        /**
         * Raised when the SDK encounters an error.
         *
         * @remarks
         *
         * See {@link (Voice:interface).(addListener:7)
         * | Voice.addListener(Error)}.
         */
        'Error' = "error",
        /**
         * Raised when the SDK is registered for incoming calls.
         *
         * @remarks
         *
         * See {@link (Voice:interface).(addListener:8)
         * | Voice.addListener(Registered)}.
         */
        'Registered' = "registered",
        /**
         * Raised when the SDK is unregistered for incoming calls.
         *
         * @remarks
         *
         * See {@link (Voice:interface).(addListener:9)
         * | Voice.addListener(Unregistered)}.
         */
        'Unregistered' = "unregistered"
    }
    /**
     * Listener types for all events emitted by a {@link (Voice:class)
     * | Voice object.}
     */
    namespace Listener {
        /**
         * Audio devices updated event listener. This should be the function
         * signature of an event listener bound to the
         * {@link (Voice:namespace).Event.AudioDevicesUpdated} event.
         *
         * @remarks
         *
         * See {@link (Voice:interface).(addListener:1)}.
         */
        type AudioDevicesUpdated = (audioDevices: AudioDevice[], selectedDevice?: AudioDevice) => void;
        /**
         * Call invite event listener. This should be the function signature of an
         * event listener bound to the
         * {@link (Voice:namespace).Event.CallInvite} event.
         *
         * @remarks
         *
         * See {@link (Voice:interface).(addListener:2)}.
         */
        type CallInvite = (callInvite: CallInvite) => void;
        /**
         * Call invite accepted event listener. This should be the function
         * signature of an event listener bound to the
         * {@link (Voice:namespace).Event.CallInviteAccepted} event.
         *
         * @remarks
         *
         * See {@link (Voice:interface).(addListener:3)}.
         */
        type CallInviteAccepted = (callInvite: CallInvite, call: Call) => void;
        /**
         * Call invite notification tapped event listener. This should be the
         * function signature of an event listener bound to the
         * {@link (Voice:namespace).Event.CallInviteNotificationTapped} event.
         *
         * @remarks
         *
         * See {@link (Voice:interface).(addListener:4)}.
         */
        type CallInviteNotificationTapped = () => void;
        type MissedCallNotificationTapped = () => void;
        /**
         * Call invite rejected event listener. This should be the function
         * signature of an event listener bound to the
         * {@link (Voice:namespace).Event.CallInviteRejected} event.
         *
         * @remarks
         *
         * See {@link (Voice:interface).(addListener:5)}.
         */
        type CallInviteRejected = (callInvite: CallInvite) => void;
        /**
         * Call invite cancelled event listener. This should be the function
         * signature of an event listener bound to the
         * {@link (Voice:namespace).Event.CancelledCallInvite} event.
         *
         * @remarks
         *
         * See {@link (Voice:interface).(addListener:6)}.
         *
         * See {@link TwilioErrors} for all error classes.
         */
        type CancelledCallInvite = (cancelledCallInvite: CancelledCallInvite, error?: TwilioError) => void;
        /**
         * Error event listener. This should be the function signature of an event
         * listener bound to the
         * {@link (Voice:namespace).Event.Error} event.
         *
         * @remarks
         *
         * See {@link (Voice:interface).(addListener:7)}.
         *
         * See {@link TwilioErrors} for all error classes.
         */
        type Error = (error: TwilioError) => void;
        /**
         * Registered event listener. This should be the function signature of an
         * event listener bound to the
         * {@link (Voice:namespace).Event.Registered} event.
         *
         * @remarks
         *
         * See {@link (Voice:interface).(addListener:8)}.
         */
        type Registered = () => void;
        /**
         * Unregistered event listener. This should be the function signature of an
         * event listener bound to the
         * {@link (Voice:namespace).Event.Unregistered} event.
         *
         * @remarks
         *
         * See {@link (Voice:interface).(addListener:9)}.
         */
        type Unregistered = () => void;
        /**
         * Generic event listener. This should be the function signature of any
         * event listener bound to any voice event.
         *
         * @remarks
         *
         * See {@link (Voice:interface).(addListener:10)}.
         */
        type Generic = (...args: any[]) => void;
    }
}
