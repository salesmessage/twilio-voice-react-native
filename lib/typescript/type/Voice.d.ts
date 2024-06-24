import type { Constants } from '../constants';
import type { NativeAudioDevicesUpdatedEvent } from './AudioDevice';
import type { NativeCallInviteInfo, NativeMissedCallNotificationTappedEvent } from './CallInvite';
import type { NativeErrorEvent } from './Error';
export interface NativeRegisteredEvent {
    type: Constants.VoiceEventRegistered;
}
export interface NativeUnregisteredEvent {
    type: Constants.VoiceEventUnregistered;
}
export interface NativeCallInviteIncomingEvent {
    [Constants.VoiceEventType]: Constants.VoiceEventTypeValueIncomingCallInvite;
    callInvite: NativeCallInviteInfo;
}
export declare type NativeVoiceEvent = NativeAudioDevicesUpdatedEvent | NativeCallInviteIncomingEvent | NativeMissedCallNotificationTappedEvent | NativeErrorEvent | NativeRegisteredEvent | NativeUnregisteredEvent;
export declare type NativeVoiceEventType = Constants.VoiceEventAudioDevicesUpdated | Constants.VoiceEventMissedCallNotificationTapped | Constants.VoiceEventTypeValueIncomingCallInvite | Constants.VoiceEventError | Constants.VoiceEventRegistered | Constants.VoiceEventUnregistered;
