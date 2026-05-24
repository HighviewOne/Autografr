<p align="center">
  <img src="https://highviewone.github.io/Autografr/banner.svg" alt="Autografr" width="100%" />
</p>

<p align="center">
  <a href="https://developer.android.com/about/versions/oreo"><img src="https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white" alt="Android 8.0+"/></a>
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.1.0-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin 2.1.0"/></a>
  <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/Jetpack_Compose-BOM_2024.x-4285F4?logo=jetpackcompose&logoColor=white" alt="Jetpack Compose"/></a>
  <a href="https://firebase.google.com"><img src="https://img.shields.io/badge/Firebase-Auth_%7C_Firestore_%7C_Storage-FFCA28?logo=firebase&logoColor=black" alt="Firebase"/></a>
  <a href="https://highviewone.github.io/Autografr/"><img src="https://img.shields.io/badge/Site-Live-C9A24C?logo=github&logoColor=white" alt="Marketing site"/></a>
  <img src="https://img.shields.io/badge/License-Proprietary-0E0D0B" alt="License: Proprietary"/>
</p>

<p align="center">
  <strong>Real autographs. Authenticated by presence.</strong>
</p>

<p align="center">
  Autografr connects fans with celebrities through authenticated digital autographs.<br/>
  Fans request a signing, celebrities draw directly on the photo with professional brushes,<br/>
  and the signed card becomes a tradeable collectible.
</p>

---

## Features

| | |
|---|---|
| **Fan & Celebrity roles** | Dedicated onboarding and home flows per role |
| **Autograph requests** | Fans request signings; celebrities manage a live queue |
| **Drawing canvas** | 5 brush types (Pen, Marker, Calligraphy, Glow, Eraser), undo/redo, color & size controls |
| **Camera & gallery** | Capture a new photo or pick from your library |
| **CertCard collectibles** | Each signed photo becomes a Studio Pass card with QR authentication |
| **Marketplace** | Browse, buy, and sell signed cards |
| **Feed** | Discover trending autographs and featured celebrities |
| **Social sharing** | Share signed photos directly to social media |

## Tech Stack

| Layer | Tech |
|-------|------|
| Language | Kotlin 2.1.0 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Backend | Firebase (Auth, Firestore, Storage) |
| Local DB | Room 3 (offline-first) |
| Navigation | Compose Navigation + Kotlin Serialization |
| Images | Coil 3 |
| Camera | CameraX |

## Project Structure

```
app/src/main/java/com/autografr/app/
├── data/           # Repository implementations, Firebase data sources, Room, mappers
├── di/             # Hilt modules (Firebase, DB, repos, dispatchers)
├── domain/         # Models, repository interfaces, sealed Result<T>
├── navigation/     # Type-safe @Serializable routes + NavHost
├── ui/
│   ├── component/  # Shared composables (Studio Pass design system)
│   ├── screen/     # All app screens grouped by feature
│   └── theme/      # Color, Type, Theme (Studio Pass tokens)
└── usecase/        # Business logic (auth, photo, request, marketplace, profile)
```

## Getting Started

**Requirements:** JDK 17 · Android SDK · compileSdk 35 · minSdk 26 (Android 8.0)

```bash
export JAVA_HOME=/path/to/jdk-17
export ANDROID_HOME=/path/to/android-sdk

./gradlew assembleDebug
# APK → app/build/outputs/apk/debug/app-debug.apk
```

### Firebase Setup

`google-services.json` is not included. To wire up a backend:

1. Create a project at [Firebase Console](https://console.firebase.google.com)
2. Add an Android app with package name `com.autografr.app`
3. Download `google-services.json` → place in `app/`

Then enable three services:

| Service | Console path | Mode |
|---------|-------------|------|
| Authentication | Build → Authentication → Sign-in method → Email/Password | Enable |
| Cloud Firestore | Build → Firestore Database → Create database | Start in test mode |
| Storage | Build → Storage → Get started | Start in test mode |

```bash
./gradlew assembleDebug   # rebuild after adding google-services.json
```

> **Production note:** Replace the test-mode Firestore/Storage rules with proper security rules before shipping.

## Design System — Studio Pass

Autografr uses the **Studio Pass** design language: a collectible-card aesthetic inspired by vinyl sleeves and autograph books.

| Token | Value |
|-------|-------|
| Ink | `#0E0D0B` |
| Paper | `#F2EDE2` |
| Foil | `#C9A24C` |
| SignatureRed | `#B33A2A` |
| Stamp | `#1A4B3A` |
| Muted | `#7A736A` |

Typography: **Fraunces** (display) · **Inter** (UI) · **Caveat** (script accents) · **JetBrains Mono** (metadata)  
Fonts are loaded at runtime via the Google Fonts downloadable provider — no bundled TTF files needed.

## Contributing

1. Fork the repo and create a feature branch
2. Open a PR — use the template and fill in the checklist
3. All UI changes should follow the Studio Pass design system

See [`.github/ISSUE_TEMPLATE/`](.github/ISSUE_TEMPLATE/) for bug report and feature request forms.

## License

All rights reserved. © 2026 Highview.
