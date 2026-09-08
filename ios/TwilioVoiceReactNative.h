//
//  TwilioVoiceReactNative.h
//  TwilioVoiceReactNative
//
//  Copyright © 2022 Twilio, Inc. All rights reserved.
//

#import <AVFoundation/AVFoundation.h>

#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@class CXCallController;
@class CXProvider;
@class TVOCall;
@class TVOCallInvite;
@class TVOCancelledCallInvite;
@class TVODefaultAudioDevice;
@class TVOPreflightTest;

FOUNDATION_EXPORT NSString * const kTwilioVoiceReactNativeEventKeyCall;
FOUNDATION_EXPORT NSString * const kTwilioVoiceReactNativeEventKeyCallInvite;
FOUNDATION_EXPORT NSString * const kTwilioVoiceReactNativeEventKeyCancelledCallInvite;

@interface TwilioVoiceReactNative : RCTEventEmitter <RCTBridgeModule>

@property (nonatomic, readonly, strong) NSMutableDictionary<NSString *, TVOCall *> *callMap;
@property (nonatomic, readonly, strong) NSMutableDictionary<NSString *, NSString *> *callConnectMap;
@property (nonatomic, readonly, strong) NSMutableDictionary<NSString *, TVOCallInvite *> *callInviteMap;
@property (nonatomic, readonly, strong) NSMutableDictionary<NSString *, TVOCancelledCallInvite *> *cancelledCallInviteMap;

@property (nonatomic, strong) CXProvider *callKitProvider;
@property (nonatomic, strong) CXCallController *callKitCallController;

@property (nonatomic, copy) NSString *accessToken;
@property (nonatomic, copy) NSDictionary *twimlParams;
@property (nonatomic, strong) void(^callKitCompletionCallback)(BOOL);
@property (nonatomic, strong) RCTPromiseResolveBlock callPromiseResolver;

@property (nonatomic, strong) TVOPreflightTest *preflightTest;
@property (nonatomic, copy) NSString *preflightTestUuid;
@property (nonatomic, strong) NSMutableArray *preflightTestEvents;

// Indicates if the disconnect is triggered from app UI, instead of the system Call UI
@property (nonatomic, assign) BOOL userInitiatedDisconnect;

@property (nonatomic, strong) AVAudioPlayer *ringbackPlayer;

+ (TVODefaultAudioDevice *)twilioAudioDevice;

- (NSString *)warningNameWithNumber:(NSNumber *)warning;
- (NSMutableArray *)callQualityWarningsArrayFromSet:(NSSet<NSNumber *> *)qualityWarnings;

@end

@interface TwilioVoiceReactNative (EventEmitter)

// Override so we can check the event observer before emitting events
- (void)sendEventWithName:(NSString *)eventName body:(id)body;

@end

@interface TwilioVoiceReactNative (CallKit)

- (void)initializeCallKit;
- (void)initializeCallKitWithConfiguration:(NSDictionary *)configuration;
- (void)makeCallWithAccessToken:(NSString *)accessToken
                         params:(NSDictionary *)params
                  contactHandle:(NSString *)contactHandle;
- (void)reportNewIncomingCall:(TVOCallInvite *)callInvite;
/* Fallback used when a VoIP push cannot be parsed into a Twilio Voice call
 * invite. PushKit requires that every VoIP push results in a CallKit report,
 * so we report a placeholder call and immediately end it rather than let the
 * OS terminate the app for an unreported push (see SMR-6413 crash investigation). */
- (void)reportAndEndUnrecognizedIncomingCall;
- (void)endCallWithUuid:(NSUUID *)uuid;
/* Initiate the answering from the app UI */
- (void)answerCallInvite:(NSUUID *)uuid
              completion:(void(^)(BOOL success))completionHandler;
- (void)updateCall:(NSString *)uuid callerHandle:(NSString *)handle;

/* Utility */
- (NSDictionary *)callInfo:(TVOCall *)call;
- (NSDictionary *)callInviteInfo:(TVOCallInvite *)callInvite;
- (NSDictionary *)cancelledCallInviteInfo:(TVOCancelledCallInvite *)cancelledCallInvite;

@end
