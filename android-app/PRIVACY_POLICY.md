# Day Line Privacy Policy

Last updated: 2026-02-15

## Overview
Day Line is a local-first app for task planning and activity tracking. This policy explains what data the app uses and how it is handled.

## Data We Process
- Task data you create (title, date/time, category, notes, reminders)
- Activity and health logs you enter in the app
- Walk-o-meter state (steps, intensity score, session summaries)
- Device location (only when permission is granted) for weather display

## Where Data Is Stored
- App data is stored locally on your device using Android DataStore.
- We do not require account sign-in and do not maintain a user cloud database for this app.

## Network Usage
- Weather data is fetched from Open-Meteo:
  - `https://api.open-meteo.com`
- Location coordinates are used in weather requests to retrieve local weather conditions.

## Permissions
Day Line may request:
- `POST_NOTIFICATIONS` for reminders and alarms
- `SCHEDULE_EXACT_ALARM` for exact alarm scheduling
- `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_MEDIA_PLAYBACK` for alarm playback
- `FOREGROUND_SERVICE_HEALTH` and `ACTIVITY_RECOGNITION` for walk tracking
- `ACCESS_COARSE_LOCATION` and `ACCESS_FINE_LOCATION` for weather
- `VIBRATE` for alarm vibration
- `INTERNET` for weather API access

## Data Sharing
- We do not sell your personal data.
- Weather requests are sent to Open-Meteo to provide weather information.

## Data Retention And Deletion
- Data remains on your device until you delete entries or uninstall the app.
- Uninstalling the app removes app-local data managed by the app sandbox.

## Children
Day Line is not specifically directed to children under 13.

## Security
We apply standard Android app controls and local storage boundaries. No method is perfectly secure, but we aim to minimize data exposure by keeping core data local.

## Contact
For privacy questions, contact:
-  `konoetouta69@gmail.com`
