# Synapes Selen VOIP Android App V.2.0.0

## How This App Works Internally:
1. 

   a) It reads config from SharedPreferences:
      - On startup, the app retrieves stored account information and settings from encrypted SharedPreferences.

   b) Register to SIP server:
      - The SipService uses the stored account info to initialize the PJSIP stack.
      - It attempts to register the account with the SIP server.

   c) Listen to broadcast events such as:
      - Incoming calls
      - Registration status changes
      - Call state changes
      - Network connectivity changes

   d) Broadcast events:
      1. REGISTRATION: Indicates the status of SIP registration (success/failure).
      2. INCOMING_CALL: Notifies of an incoming call, triggering the CallActivity.
      3. CALL_STATE: Updates on the current state of a call (ringing, connected, disconnected).
      4. CALL_MEDIA_STATE: Indicates changes in call media (audio/video started or stopped).
      5. OUTGOING_CALL: Confirms that an outgoing call has been initiated.
      6. STACK_STATUS: Informs about the overall status of the SIP stack (started/stopped).

   e) The MainActivity and CallActivity respond to these broadcasts to update the UI and manage call flow.

   f) The SipService runs in the background, managing SIP operations even when the app is not in the foreground.

   g) For making calls:
      - The app uses the SipAccount class to create and manage calls.
      - It handles call setup, media negotiation, and teardown using PJSIP library functions.

   h) For incoming calls:
      - The service receives the incoming call notification from PJSIP.
      - It broadcasts this event, which is picked up by the app to display the incoming call screen.

   i) The app uses various managers (ConnectivityManager, TelephonyManager, WifiManager) to monitor and adapt to network conditions.

   j) It implements encryption for storing sensitive data like SIP credentials.

   k) The app supports both audio and video calls, managing the necessary UI components and media streams for each.



1. Development Setup:

   1. Clone the repository and open the project in Android Studio.
   2. Ensure all dependencies in `build.gradle.kts` are resolved.
   3. Set up the PJSIP library, ensuring it's properly linked in the project.
   4. Review the `AndroidManifest.xml` to understand required permissions and registered components.

2. Key Components for Developers:

   a) SIP Account Configuration:
      - Implement UI for SIP account setup in MainActivity or a dedicated SettingsActivity.
      - Use SharedPrefsProvider to securely store account details.

   b) SIP Service Integration:
      - Understand SipService.kt, which manages the SIP stack.
      - Implement service binding in MainActivity to interact with SipService.

   c) Call Handling:
      - Implement call initiation logic in MainActivity.
      - Develop CallActivity to manage active calls.

3. Internal Workflow:

   a) Configuration Management:
      - SharedPrefsProvider reads/writes encrypted config data.
      - Implement methods to retrieve SIP account details securely.

   b) SIP Registration:
      - SipService initializes PJSIP stack on startup.
      - Implement registration logic using account details from SharedPreferences.

   c) Event Handling:
      - Set up BroadcastReceivers in MainActivity and CallActivity.
      - Handle these key events:
        1. REGISTRATION
        2. INCOMING_CALL
        3. CALL_STATE
        4. CALL_MEDIA_STATE
        5. OUTGOING_CALL
        6. STACK_STATUS

   d) Broadcasting:
      - Use BroadcastEmitter in SipService to send events.
      - Implement handlers in activities to respond to these events.

   e) Call Management:
      - Utilize SipAccount and SipCall classes for call operations.
      - Implement call setup, media handling, and teardown using PJSIP.

   f) Background Service:
      - Ensure SipService runs as a foreground service for persistent operation.

   g) Network Monitoring:
      - Implement network state changes handling in MainActivity.
      - Update SIP stack configuration based on network conditions.

   h) Security:
      - Use the Crypto class for handling sensitive data encryption.
      - Implement secure storage of SIP credentials.

   i) Audio/Video Handling:
      - Set up audio/video streams in CallActivity.
      - Implement codec management in SipServiceUtils.

   j) UI Updates:
      - Create responsive UI updates based on call states and events.

Key Development Focus Areas:
1. Robust error handling and logging throughout the app.
2. Efficient management of SIP stack lifecycle.
3. Proper handling of Android lifecycle events in relation to SIP operations.
4. Optimization of network usage and adapting to varying network conditions.
5. Ensuring smooth audio/video experience during calls.
6. Implementing proper cleanup of resources, especially in CallActivity.

Remember to thoroughly test SIP operations, handle edge cases, and ensure the app behaves correctly under various network conditions and device states.