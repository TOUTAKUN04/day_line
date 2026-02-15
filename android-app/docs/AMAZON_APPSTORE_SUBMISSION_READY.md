# Amazon Appstore Submission Readiness

## Important Platform Note
As announced by Amazon on February 20, 2025, new app submissions targeting Android mobile devices are no longer accepted in Amazon Appstore. Submission remains available for Amazon Fire tablet and Fire TV device families.

Reference:
- https://developer.amazon.com/apps-and-games/blogs/2025/02/amazon-appstore-on-android-devices-discontinuation

## Current App Identity
- Package: `com.toutakun04.dayline`
- Version name: `v1.1.2`
- Version code: `4`
- Min SDK: `24`
- Target SDK: `34`

## Build Artifacts
- Release APK: `android-app/day_line_v1.1.2.apk`
- Release AAB: `android-app/build-app/outputs/bundle/release/app-release.aab`

Build commands:
```powershell
./gradlew :app:assembleRelease
./gradlew :app:bundleRelease
```

## Release Signing Setup
1. Generate a private release keystore:
   - Run `android-app/scripts/generate-release-keystore.ps1`
2. This script creates:
   - `android-app/keystore/day_line-release.jks`
   - `android-app/keystore.properties`
3. `app/build.gradle.kts` now uses release signing automatically when `keystore.properties` exists.
4. Keep both files private. They are gitignored.

If you prefer manual setup, copy `android-app/keystore.properties.example` to `android-app/keystore.properties` and fill values.

## Store Console Checklist (Manual)
- App title and short/long description
- Icon and screenshot assets
- Content rating questionnaire
- Privacy and data usage questionnaire
- Category and distribution settings (Fire device targets)
- Test submission and pre-submission checks in Amazon console

## Data/Privacy Notes For Questionnaire
Use this as a starting point and verify before submission:
- User account required: No
- In-app purchases: No
- Ads SDK: No
- Analytics SDK: No
- Data stored on device: Tasks, activity logs, walk meter state (DataStore/local)
- Network calls: Weather request to Open-Meteo using location coordinates
- Sensitive permissions used:
  - `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`
  - `ACTIVITY_RECOGNITION`
  - `SCHEDULE_EXACT_ALARM`
  - `FOREGROUND_SERVICE_HEALTH`

## Remaining Manual Work
- Upload signed artifact in Amazon Developer Console
- Complete privacy and content forms in console
- Upload final listing graphics and localized descriptions
