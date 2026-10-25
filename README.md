# Toolkit for Pixel

Tools, tweaks and automations for Google Pixel phones. Pixel only: other phones are not supported.

Toolkit for Pixel started as a fork of [Essentials](https://github.com/sameerasw/essentials) (MIT); see [LICENSE](LICENSE). It is being reworked with its own design and a modular structure.

## Status

Private, in development. Phase 0 (fresh start: own package, Pixel-only code, upstream branding and services removed) is under way. No releases yet.

## Building

- Android Studio with JDK 21 or newer, Android SDK platform 37.
- `./gradlew :app:assembleDebug` builds the debug app (application ID `com.jawzf.pixeltoolkit`, the same as release).
- `./gradlew :app:testDebugUnitTest` runs the unit tests.

Every pull request to `main` runs the checks in [.github/workflows/ci.yml](.github/workflows/ci.yml): ktlint and detekt on changed files, Android lint, unit tests, debug and release builds, an APK size budget, a dependency check and a secret scan.

## Permissions

Some features need extra access:

- **Shizuku or root** for system-level commands.
- **Accessibility service** for features that react to the screen, buttons or gestures.
- **`WRITE_SECURE_SETTINGS`**, granted once over ADB. For a debug build connected to a computer: `./grant_perms.sh`.

## Docs

Technical notes live in [docs/](docs/): architecture, services and permissions, the Dynamic Island and its providers, and how to add a Quick Settings tile.
