<!-- Do not edit this file. It is automatically generated by API Documenter. -->

[Home](./index.md) &gt; [@twilio/voice-react-native-sdk](./voice-react-native-sdk.md) &gt; [Voice](./voice-react-native-sdk.voice_namespace.md) &gt; [Event](./voice-react-native-sdk.voice_namespace.event_enum.md)

## Voice.Event enum

Enumeration of all event strings emitted by [Voice](./voice-react-native-sdk.voice_class.md) objects.

<b>Signature:</b>

```typescript
enum Event 
```

## Enumeration Members

|  Member | Value | Description |
|  --- | --- | --- |
|  AudioDevicesUpdated | <code>&quot;audioDevicesUpdated&quot;</code> | Raised when there is a change in available audio devices. |
|  CallInvite | <code>&quot;callInvite&quot;</code> | Raised when there is an incoming call invite. |
|  CallInviteAccepted | <code>&quot;callInviteAccepted&quot;</code> | Raised when an incoming call invite has been accepted.<!-- -->This event can be raised either through the SDK or outside of the SDK (i.e. through native UI/UX such as push notifications). |
|  CallInviteNotificationTapped | <code>&quot;callInviteNotificationTapped&quot;</code> | Raised when the notification for an incoming call invite has been tapped.<!-- -->This event is raised only from the native layer, through the push notification. |
|  CallInviteRejected | <code>&quot;callInviteRejected&quot;</code> | Raised when an incoming call invite has been rejected.<!-- -->This event can be raised either through the SDK or outside of the SDK (i.e. through native UI/UX such as push notifications). |
|  CancelledCallInvite | <code>&quot;cancelledCallInvite&quot;</code> | Raised when an incoming call invite has been cancelled, thus invalidating the associated call invite. |
|  Error | <code>&quot;error&quot;</code> | Raised when the SDK encounters an error. |
|  Registered | <code>&quot;registered&quot;</code> | Raised when the SDK is registered for incoming calls. |
|  Unregistered | <code>&quot;unregistered&quot;</code> | Raised when the SDK is unregistered for incoming calls. |
