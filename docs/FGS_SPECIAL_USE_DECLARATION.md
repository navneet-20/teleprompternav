# Teleprompter Pro — Special Use Foreground Service

## Type
specialUse

## Functionality
Teleprompter Pro provides a user-controlled floating teleprompter overlay. After the user explicitly starts the teleprompter, a foreground service keeps the requested script visible while the user reads or records content in another app.

## Why it must run immediately
If the foreground service is deferred or interrupted while the user is actively using the teleprompter, the floating overlay disappears and the core user-requested feature stops working.

## User initiation
The service starts only after an explicit user action in Teleprompter Pro and the required overlay permission is granted.

## User control
The user can stop the feature with the close button on the floating overlay.

## Duration
The service runs only while the user actively uses the floating teleprompter.

## Reviewer video flow
1. Open Teleprompter Pro.
2. Enter a short script.
3. Choose Manual WPM or Target Time.
4. Start the floating teleprompter.
5. Grant overlay permission if requested.
6. Show the floating overlay.
7. Open another app such as Camera.
8. Show the teleprompter above the other app.
9. Demonstrate play/pause, reset and speed controls.
10. Tap close.
11. Show that the overlay disappears.

The final Play Console declaration must accurately describe the submitted binary.
