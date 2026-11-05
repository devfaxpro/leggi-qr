# Leggi QR

An offline Android QR and barcode scanner, creator and local collection. Light blue interface, black QR graphics, no account, ads or Internet permission.

[Italiano](README.md) · [Download APK and source](https://github.com/devfaxpro/leggi-qr/releases/tag/v2.1.1)

## Version 2.1.1

This release adds Italian explanations throughout the application code and test methods, documents the important processing steps, and licenses the original project under **GPL-3.0-only**. The existing functions, 40 languages, system-language default and globe selector are retained.

- [Signed APK](https://github.com/devfaxpro/leggi-qr/releases/download/v2.1.1/Leggi-QR-2.1.1.apk), Android 7.0 or newer.
- [Corresponding source archive](https://github.com/devfaxpro/leggi-qr/releases/download/v2.1.1/Leggi-QR-2.1.1-AndroidStudio.zip), including Android Studio sources, translation generators, tests and the desugar_jdk_libs source archive.
- [Checksums](https://github.com/devfaxpro/leggi-qr/releases/download/v2.1.1/SHA256-Leggi-QR-2.1.1.txt).

Features include camera scanning, batch scanning, image import, typed QR forms, barcode generation, history, favorites, search, PNG/SVG sharing and JSON export. Content is shown before external actions. The scanner does not store camera frames.

## Build

Open the repository in Android Studio with JDK 17 or 21 and Android SDK 36. Run `./gradlew testDebugUnitTest assembleDebug` (`gradlew.bat` on Windows). See [BUILDING.md](BUILDING.md) for release signing and translation regeneration. Signing keys are never included in the public source.

## Validation and scope

The project includes 36 JUnit/Robolectric tests and validates 145 translation keys in each of 40 locales. Release checks cover APK signing, alignment and AAB structure. Physical-device camera testing and native-speaker translation review have not been completed in this release session.

## License

Copyright © 2026 devfaxpro. Original application code is licensed under the [GNU General Public License version 3 only](LICENSE), without warranty. Third-party components keep their own licenses; see [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
