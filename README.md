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
| **Collectibles** | Each signed photo is an ownable, tradeable item (Studio Pass CertCard with QR authentication is designed but not yet built) |
| **Marketplace** | Browse, list, and buy signed photos (no payment processing yet — see Known gaps) |
| **Feed** | Discover trending autographs and featured celebrities |
| **Social sharing** | Share signed photos directly to social media |
| **Listing writer** | "Suggest with Gemini" drafts a listing title and description from the signed photo (Firebase AI Logic) |

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

> **Production note:** Replace the test-mode rules with the ones in this repo:
> `firebase deploy --only firestore:rules,storage`.

#### Security rules tests

`rules-tests/` runs `firestore.rules` and `storage.rules` against the local
Firebase emulator (no real project is touched). Run it after any rules change:

```bash
cd rules-tests
npm install
npm test        # needs Java 21+ on PATH for the emulator
```

#### Gemini (Firebase AI Logic)

The Create Listing screen's "Suggest with Gemini" button calls Gemini through
**Firebase AI Logic** (`firebase-ai`, `GenerativeBackend.googleAI()`), in
`data/ai/GeminiListingAssistant.kt`. The app ships **no Gemini API key**: Firebase
holds it server-side and requests carry the app's App Check token.

One-time setup: Firebase Console → **AI Logic** → Get started → choose the
**Gemini Developer API**. Usage is billed to the Firebase project's Cloud billing
account. Set a spend cap in AI Studio → Spend for the project.

**Unlike Firestore and Storage, AI Logic enforces App Check** (Console → App Check →
APIs shows "Firebase AI Logic: Basic – Enforced"). Every install needs a valid App Check token:

- **Debug builds:** add each device's debug token under Console → App Check → Apps →
  `com.autografr.app` → ⋮ → Manage debug tokens. The token is printed in logcat
  ("Enter this debug secret into the allow list…") and changes if the app is
  uninstalled or its data cleared.
- **Release builds:** register Play Integrity for the app (needs the signing
  certificate's SHA-256) before testers can use the Gemini button.

If App Check fails, the button shows "Couldn't write a suggestion right now" and
logcat has the cause under the `GeminiListing` tag.

#### Account deletion

Play requires in-app account deletion. Profile → ⋮ → **Delete account** asks for the
password, writes `account_deletions/{uid}` (uid, role, server timestamp), deletes the
profile image, the `users/{uid}` doc and the Auth account. It does **not** delete
photos or requests. The privacy policy (`docs/privacy.html`) promises those are
removed within 30 days, so check `account_deletions` in the Firestore console and
clean up by hand: the user's unsold `signed_photos`, their `autograph_requests`, and
their Storage folders. Leave photos another user bought. Deletion requests that
arrive by email to apps@mirarver.com get the same treatment (also delete the Auth
user in the console). A Cloud Function can automate this once the project is on Blaze.

#### Reports and blocks

Play's user-generated content policy requires in-app reporting and blocking, and that reports get acted on.

- **Report**: the ⋮ menu on a listing, a celebrity profile or a request opens a reason list (spam, harassment,
  sexual content, violence, hate, impersonation, scam, other) plus optional details. Each report is a doc in
  `reports/`: `reporterId`, `targetType` (`PHOTO`/`USER`/`REQUEST`), `targetId`, `targetUserId`, `reason`,
  `details`, `createdAt`. The app can only create them; nobody can read them from the app.
- **Block**: the same menu blocks the other person. Blocks live in `users/{uid}/blocked/{blockedUid}` (private to
  the owner). Blocked users' photos, listings and profile are hidden, and their pending requests drop out of a
  celebrity's queue. `firestore.rules` also stops a fan a celebrity blocked from sending new requests. Users
  unblock from My Profile > Blocked users.
- **Reviewing reports** (until there's a Cloud Function that emails new ones): check Firebase Console → Firestore
  → `reports` at least weekly, newest first. Remove content that breaks the rules (delete the `signed_photos` doc
  and its Storage files), and for repeat or serious cases delete the user (Authentication → user → Delete, then
  their `users` doc). Delete the report once handled. Illegal content: remove it right away and report it as
  the law requires.

#### Celebrity verification

Only celebrities with `verificationStatus: "VERIFIED"` can save autographs or
receive requests — the rules enforce this so nobody can sell an "autograph" under
someone else's name. Users choose Fan or Celebrity at profile setup, but
verification can only be set server-side. Until there's a backend process for it,
set it by hand: Firebase Console → Firestore → `users/<uid>` → add
`verificationStatus` = `"VERIFIED"` (string). Rating and autograph/purchase
counters are server-only too and stay at zero until a backend maintains them.

#### Testing on a device with local emulators

A debug build can talk to the local Firebase emulators instead of the real
project, so you can test end-to-end without touching live data:

```bash
cd rules-tests && npm run emulators     # Auth :9099, Firestore :8181, Storage :9199 (Java 21+)
# in another terminal, with the phone connected over USB:
adb reverse tcp:9099 tcp:9099 && adb reverse tcp:8181 tcp:8181 && adb reverse tcp:9199 tcp:9199
./gradlew installDebug -PuseFirebaseEmulators=true
```

Without `-PuseFirebaseEmulators=true` (the default), debug builds use the real
project — reinstall a normal build before using the app for real. Emulator data
is in-memory and is lost when the emulators stop. To verify a test celebrity,
PATCH their `users/<uid>` doc through the Firestore emulator's REST API with
`Authorization: Bearer owner` (bypasses rules).
Stop the emulators (Ctrl-C) and `./gradlew --stop` when you're done — they hold
several GB between them.

#### App Check

Debug builds use the App Check **debug provider**: each install prints its own
token to logcat, and only tokens registered under Console → App Check → Manage
debug tokens are accepted. Release builds use **Play Integrity**, which only
passes for installs that came from Google Play (an internal-testing track works).

**Do not turn on App Check enforcement while the app is shared as a sideloaded
APK** (`app-debug.apk` or a sideloaded release build). Every such install will
be rejected by Firestore and Storage. Leave enforcement off (Console shows
"unenforced" metrics only) until testers install through a Play track.

## Releasing

The app and the security rules must ship together: builds from before
2026-09-25 write Storage paths the current rules reject, and current builds
need the current rules.

1. `cd rules-tests && npm test` — all rules tests pass.
2. `firebase deploy --only firestore:rules,storage`
3. Hand out the new APK at the same time.
4. Mark real celebrity accounts verified (see *Celebrity verification*).

## Known gaps

- **Payments:** purchases are recorded client-side with no payment gate. Before
  launch, move `purchaseListing` into a Cloud Function behind a payment provider
  and set `transactions` create to `false` in `firestore.rules`.
- **Server-side jobs:** nothing sets `verificationStatus` or updates rating /
  autograph / purchase counters yet.
- **Resale:** there's no way to list a photo you bought (listing is only offered
  right after signing).
- **Canvas:** the photo is stretched to fill the canvas; the eraser clears the
  photo underneath, not just strokes (erased areas export black).
- **Stale cache:** Marketplace/Trending and the request queue are served from
  Room and don't evict items that leave the Firestore query (e.g. sold elsewhere).
- **Download URLs:** stored `getDownloadUrl()` links carry a token, so anyone
  with a link can fetch the image without signing in.

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
