//
//  TwilioVoiceReactNative+CallInvite.m
//  TwilioVoiceReactNative
//
//  Copyright © 2023 Twilio, Inc. All rights reserved.
//

@import TwilioVoice;

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

    [self.callInviteMap removeObjectForKey:uuid];

    [self endCallWithUuid:[[NSUUID alloc] initWithUUIDString:uuid]];
}

@end
