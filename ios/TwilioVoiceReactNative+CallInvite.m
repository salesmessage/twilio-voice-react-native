//
//  TwilioVoiceReactNative+CallInvite.m
//  TwilioVoiceReactNative
//
//  Copyright © 2023 Twilio, Inc. All rights reserved.
//

@import TwilioVoice;
@import CallKit;

#import "TwilioVoiceReactNative.h"
#import "TwilioVoiceReactNativeConstants.h"

@interface TwilioVoiceReactNative (CallInvite) <TVONotificationDelegate>

@end

@implementation TwilioVoiceReactNative (CallInvite)

- (void)callInviteReceived:(TVOCallInvite *)callInvite {
    self.callInviteMap[callInvite.uuid.UUIDString] = callInvite;
    
    [self reportNewIncomingCall:callInvite];

    [self sendEventWithName:kTwilioVoiceReactNativeScopeVoice
                       body:@{
                         kTwilioVoiceReactNativeVoiceEventType: kTwilioVoiceReactNativeVoiceEventTypeValueIncomingCallInvite,
                         kTwilioVoiceReactNativeEventKeyCallInvite: [self callInviteInfo:callInvite]}];
}

- (void)cancelledCallInviteReceived:(TVOCancelledCallInvite *)cancelledCallInvite error:(NSError *)error {
    if (cancelledCallInvite == nil) {
        NSLog(@"[TwilioVoiceReactNative] cancelledCallInviteReceived: nil cancelledCallInvite");
        return;
    }

    NSString *cancelledCallSid = cancelledCallInvite.callSid;
    if (cancelledCallSid == nil || cancelledCallSid.length == 0) {
        NSLog(@"[TwilioVoiceReactNative] cancelledCallInviteReceived: missing callSid");
        return;
    }

    NSString *uuid;
    for (NSString *uuidKey in [self.callInviteMap allKeys]) {
        TVOCallInvite *callInvite = self.callInviteMap[uuidKey];
        if (callInvite == nil || callInvite.callSid == nil) {
            continue;
        }
        if ([callInvite.callSid isEqualToString:cancelledCallSid]) {
            uuid = uuidKey;
            break;
        }
    }

    if (!uuid) {
        NSLog(@"[TwilioVoiceReactNative] cancelledCallInviteReceived: no matching call invite for callSid %@", cancelledCallSid);
        return;
    }

    self.cancelledCallInviteMap[uuid] = cancelledCallInvite;

    NSMutableDictionary *eventBody = [@{
        kTwilioVoiceReactNativeVoiceEventType: kTwilioVoiceReactNativeCallInviteEventTypeValueCancelled,
        kTwilioVoiceReactNativeCallInviteEventKeyCallSid: cancelledCallSid,
        kTwilioVoiceReactNativeEventKeyCancelledCallInvite: [self cancelledCallInviteInfo:cancelledCallInvite],
    } mutableCopy];

    if (error != nil) {
        eventBody[kTwilioVoiceReactNativeVoiceErrorKeyError] = @{
            kTwilioVoiceReactNativeVoiceErrorKeyCode: @(error.code),
            kTwilioVoiceReactNativeVoiceErrorKeyMessage: [error localizedDescription] ?: @"",
        };
    }

    [self sendEventWithName:kTwilioVoiceReactNativeScopeCallInvite body:eventBody];

    // Ring-group / call-distribution calls ring every member simultaneously.
    // When one member answers, Twilio cancels the invite on all the others.
    // Report those cancellations to CallKit as "answered elsewhere" so iOS does
    // NOT log them as red "Missed" calls (or bump the missed-call badge) on the
    // non-answering members' devices. The backend flags such calls with the
    // `isGroupCall` custom parameter on the invite.
    TVOCallInvite *matchedInvite = self.callInviteMap[uuid];
    BOOL isGroupCall = [matchedInvite.customParameters[@"isGroupCall"] boolValue];

    // TEMP (SMR-6844): both sources logged so we can see the exact key/value the
    // backend sends and whether the cancelled invite carries the params too.
    NSLog(@"[TwilioVoiceReactNative] cancelled invite %@ isGroupCall=%d invite=%@ cancelled=%@",
          cancelledCallSid,
          isGroupCall,
          matchedInvite.customParameters,
          cancelledCallInvite.customParameters);

    [self.callInviteMap removeObjectForKey:uuid];

    // TEMP (SMR-6844): the "answered elsewhere" branch is disabled and the
    // default behavior restored while the ticket is investigated further.
    // Reporting every group-call cancel as "answered elsewhere" also hides the
    // calls nobody in the group answered, which should still be logged as missed
    // — and nothing here can tell the two apart: Twilio cancels the invite
    // identically in both cases (verified on device: 31008 / "Call Cancelled" /
    // "SIP/2.0 410 Gone" either way), and neither the RN SDK nor the native iOS
    // SDK exposes a cancellation reason or a call state. Only the backend knows,
    // via GET core/voice/call/{callSid}, so the call has to be held here until
    // the app reports that status back. Restore this branch together with that
    // hold, not on its own.
    // if (isGroupCall) {
    //     [self.callKitProvider reportCallWithUUID:[[NSUUID alloc] initWithUUIDString:uuid]
    //                                  endedAtDate:[NSDate date]
    //                                       reason:CXCallEndedReasonAnsweredElsewhere];
    // } else {
    // 1:1 direct call — keep the default behavior so a genuinely missed call
    // is still logged as a red "Missed" entry in native Recents.
    [self endCallWithUuid:[[NSUUID alloc] initWithUUIDString:uuid]];
    // }
}

@end
