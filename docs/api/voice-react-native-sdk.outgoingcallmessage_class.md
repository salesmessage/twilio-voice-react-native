<!-- Do not edit this file. It is automatically generated by API Documenter. -->

[Home](./index.md) &gt; [@twilio/voice-react-native-sdk](./voice-react-native-sdk.md) &gt; [OutgoingCallMessage](./voice-react-native-sdk.outgoingcallmessage_class.md)

## OutgoingCallMessage class

Provides access to information about a outgoingCallMessage, including the call message content, contentType, messageType, and voiceEventSid

<b>Signature:</b>

```typescript
export declare class OutgoingCallMessage extends CallMessage 
```
<b>Extends:</b> [CallMessage](./voice-react-native-sdk.callmessage_class.md)

## Remarks

Note that the outgoingCallMessage information is fetched as soon as possible from the native layer, but there is no guarantee that all information is immediately available. Methods such as `OutgoingCallMessage.getContent` or `OutgoingCallMessage.getSid` may return `undefined`<!-- -->.

As outgoingCallMessage events are received from the native layer, outgoingCallMessage information will propagate from the native layer to the JS layer and become available. Therefore, it is good practice to read information from the outgoingCallMessage after an event occurs, or as events occur.

- See the [OutgoingCallMessage.Event](./voice-react-native-sdk.outgoingcallmessage_namespace.event_enum.md) enum for events emitted by `OutgoingCallMessage` objects. - See the [OutgoingCallMessage interface](./voice-react-native-sdk.outgoingcallmessage_interface.md) for overloaded event listening metods. - See the [OutgoingCallMessage namespace](./voice-react-native-sdk.outgoingcallmessage_namespace.md) for types and enumerations used by this class.

## Constructors

|  Constructor | Modifiers | Description |
|  --- | --- | --- |
|  [(constructor)({ content, contentType, messageType, voiceEventSid, })](./voice-react-native-sdk.outgoingcallmessage_class._constructor__constructor.md) |  | Constructs a new instance of the <code>OutgoingCallMessage</code> class |
