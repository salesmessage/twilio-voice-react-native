import type { Constants } from '../constants';
import type { NativeAudioDevicesUpdatedEvent } from './AudioDevice';
import type { NativeCallInviteEvent, NativeCallInviteAcceptedEvent, NativeCallInviteNotificationTappedEvent, NativeMissedCallNotificationTappedEvent, NativeCallInviteRejectedEvent, NativeCancelledCallInviteEvent } from './CallInvite';
import type { NativeErrorEvent } from './Error';
export interface NativeRegisteredEvent {
    type: Constants.VoiceEventRegistered;
}
export interface NativeUnregisteredEvent {
    type: Constants.VoiceEventUnregistered;
}
export declare type NativeVoiceEvent = NativeAudioDevicesUpdatedEvent | NativeCallInviteEvent | NativeCallInviteAcceptedEvent | NativeCallInviteNotificationTappedEvent | NativeMissedCallNotificationTappedEvent | NativeCallInviteRejectedEvent | NativeCancelledCallInviteEvent | NativeErrorEvent | NativeRegisteredEvent | NativeUnregisteredEvent;
export declare type NativeVoiceEventType = Constants.VoiceEventAudioDevicesUpdated | Constants.VoiceEventCallInvite | Constants.VoiceEventCallInviteAccepted | Constants.VoiceEventCallInviteNotificationTapped | Constants.VoiceEventMissedCallNotificationTapped | Constants.VoiceEventCallInviteRejected | Constants.VoiceEventCallInviteCancelled | Constants.VoiceEventError | Constants.VoiceEventRegistered | Constants.VoiceEventUnregistered;
