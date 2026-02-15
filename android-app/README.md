# Day Line

[![Download APK](https://img.shields.io/badge/Download-APK-2ea44f)](https://github.com/TOUTAKUN04/day_line/releases/download/v1.1.2/day_line_v1.1.2.apk)

## Release
Day Line v1.1.2 - Calendar Week Intensity Update

## About
Day Line now combines daily task planning and movement tracking, with smarter alarms, upgraded widgets, and refreshed branding.

## What's New
- Weekly Intensity Score now tracks a fixed Monday-Sunday week
- Weekly line resets at local Monday 12:00 AM
- Added Walk-o-meter home screen widget
- Updated package/branding to `com.toutakun04.dayline`
- Refreshed launcher/adaptive icons (Android 12+ support)
- Improved reminder and alarm behavior
- Added stronger license and security policy

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
See `CHANGELOG.md`. [Click here.](https://github.com/TOUTAKUN04/day_line/blob/main/android-app/CHANGELOG.md)
