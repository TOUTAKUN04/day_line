# Day Line

[![Download APK](https://img.shields.io/badge/Download-APK-2ea44f)](https://github.com/TOUTAKUN04/day_line/releases/download/v1.0.1x/day_line_v1.0.1x.apk)

Day Line is a lightweight daily task planner with reminders, start-time alarms, and a home screen widget.

## Features
- Create tasks with date/time, categories, tags, and notes
- Recurring tasks (daily, weekly, monthly)
- Reminders and start-time alarms with sound + vibration
- Weather summary based on device location
- Home screen widget for upcoming tasks

## Requirements
- JDK 17
- Android SDK 34
- Android Studio (recommended)

## Build And Run
```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

Release build:
```bash
./gradlew :app:assembleRelease
```

## Permissions
- `POST_NOTIFICATIONS` for reminders/alarms
- `SCHEDULE_EXACT_ALARM` for exact task alarms
- `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_MEDIA_PLAYBACK` for alarm sound
- `VIBRATE` for alarm vibration
- `ACCESS_COARSE_LOCATION` / `ACCESS_FINE_LOCATION` for weather

## Notes
- On Android 12+ the user may need to allow exact alarms in system settings.
- Alarm sound uses the system alarm tone. If exact alarms are blocked, the app falls back to a loud alarm notification.

## Data And Privacy
- Task data is stored locally in DataStore.
- Weather uses the Open-Meteo API (no API key required).

## Changelog
See `CHANGELOG.md`. [Click here to view the changelog](https://github.com/TOUTAKUN04/day_line/blob/main/android-app/CHANGELOG.md)

