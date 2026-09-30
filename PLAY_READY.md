# Play-ready Android build

This branch prepares the native app for Google Play without storing signing secrets in Git.

## Build baseline

- applicationId: `app.alfatiha.tafsir`
- minSdk: 24
- compileSdk: 36
- targetSdk: 36
- versionCode: 4
- versionName: 1.2.0
- Android Gradle Plugin: 8.10.1
- Gradle: 8.11.1
- JDK: 17

## Artifacts

CI builds:

- installable debug APK for phone QA
- release AAB for Google Play pipeline validation

The release AAB is unsigned unless all four signing environment variables are present:

- `ANDROID_UPLOAD_STORE_FILE`
- `ANDROID_UPLOAD_STORE_PASSWORD`
- `ANDROID_UPLOAD_KEY_ALIAS`
- `ANDROID_UPLOAD_KEY_PASSWORD`

Do not commit keystores or passwords.

## Android 16 compatibility

System Back is handled through `OnBackInvokedDispatcher` on API 33+ while the legacy callback remains for older Android versions.

The UI already consumes system bar insets at the root view so content remains clear of status and navigation bars under enforced edge-to-edge behavior.

## Before Play Console upload

1. Create or securely store a dedicated upload key.
2. Configure Play App Signing for the new app.
3. Build a signed release AAB using the upload key.
4. Upload the signed AAB to an internal testing track first.
5. Complete Play Console store listing, content rating, Data safety, target audience and privacy declarations as applicable.
6. Test the Play-generated build on at least one Android 16 device before production rollout.
