# Day Line

[![Download APK](https://img.shields.io/badge/Download-APK-2ea44f)](https://github.com/TOUTAKUN04/day_line/releases/download/v1.1.0/day_line_v1.1.0.apk)

Day Line is a lightweight daily planner with reminders, start-time alarms, weather, and home screen widgets.

## What's New In v1.1.0
- Package and application ID renamed to `com.toutakun04.dayline`
- New Walk-o-meter home screen widget with goal and score progress
- Updated adaptive launcher icon resources and Android 12+ theme assets
- Refined task widget visuals and app branding

## Features
- Create tasks with date/time, categories, tags, and notes
- Recurring tasks (daily, weekly, monthly)
- Reminders and start-time alarms with sound + vibration
- Weather summary based on device location
- Home screen widgets for upcoming tasks and Walk-o-meter progress

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
- `FOREGROUND_SERVICE_HEALTH` + `ACTIVITY_RECOGNITION` for Walk-o-meter tracking
- `VIBRATE` for alarm vibration
- `ACCESS_COARSE_LOCATION` / `ACCESS_FINE_LOCATION` for weather

## Notes
- On Android 12+ the user may need to allow exact alarms in system settings.
- Alarm sound uses the system alarm tone. If exact alarms are blocked, the app falls back to a loud alarm notification.

## Data And Privacy
- Task data is stored locally in DataStore.
- Weather uses the Open-Meteo API (no API key required).

## Changelog
See `CHANGELOG.md`.
