# Autografr

Android app where celebrities autograph photos from fans. Fans request autographs, celebrities sign them on a digital canvas, and signed photos can be shared or sold on a marketplace.

## Download

Grab the latest APK from [Releases](https://github.com/HighviewOne/Autografr/releases) and install on any Android 8.0+ device.

## Features

- **Fan & Celebrity roles** — register as either, with dedicated flows for each
- **Drawing canvas** — sign photos with 5 brush types (Pen, Marker, Calligraphy, Glow, Eraser), undo/redo, color & size controls
- **Camera & gallery** — capture photos or pick from your library
- **Autograph requests** — fans request autographs from celebrities, celebrities manage their queue
- **Marketplace** — browse and purchase signed photos
- **Social sharing** — share signed photos to social media
- **Feed** — discover trending autographs and featured celebrities

## Tech Stack

| Layer | Tech |
|-------|------|
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Backend | Firebase (Auth, Firestore, Storage) |
| Database | Room |
| Navigation | Compose Navigation + Kotlin Serialization |
| Images | Coil 3 |
| Camera | CameraX |

## Project Structure

```
app/src/main/java/com/autografr/app/
├── data/           # Repository implementations, Firebase, Room, mappers
├── di/             # Hilt modules
├── domain/         # Models, repository interfaces, Result type
├── navigation/     # Type-safe routes & NavHost
├── ui/             # Screens, components, theme
└── usecase/        # Business logic use cases
```

## Building

**Requirements:** JDK 17, Android SDK (compileSdk 35, minSdk 26)

```bash
# Set environment
export JAVA_HOME=/path/to/jdk-17
export ANDROID_HOME=/path/to/android-sdk

# Build debug APK
./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## Firebase Setup

The `google-services.json` file is not included in the repo for security. To set up Firebase:

1. Go to [Firebase Console](https://console.firebase.google.com) and create a new project
2. Add an Android app with package `com.autografr.app`
3. Download `google-services.json` and place it in the `app/` directory

### Enable Authentication
1. In the Firebase Console sidebar, go to **Build → Authentication**
2. Click **Get started**
3. Under "Sign-in method", click **Email/Password**
4. Toggle the first switch to **Enable** and click **Save**

### Enable Cloud Firestore
1. Go to **Build → Firestore Database**
2. Click **Create database**
3. Choose a location closest to your users (can't be changed later)
4. Select **Start in test mode** and click **Create**

### Enable Storage
1. Go to **Build → Storage**
2. Click **Get started**
3. Select **Start in test mode**
4. Click **Next**, pick the same location as Firestore, and click **Done**

### Rebuild
```bash
./gradlew assembleDebug
```

## License

All rights reserved.
