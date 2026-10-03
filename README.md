# SteamEnhanced for Android

**SteamEnhanced** is a custom Android application that supercharges the Steam web experience on mobile devices. By combining Mozilla's powerful **GeckoView** engine with popular browser extensions (**Augmented Steam** and a native **HowLongToBeat (HLTB)** integration), it brings desktop-class Steam enhancements directly to Android.

---

## ✨ Features

- **GeckoView Engine**: Powered by Mozilla GeckoView for robust web extension support and smooth rendering.
- **Augmented Steam Integration**: Built-in support for the official Augmented Steam extension, providing enhanced pricing, ratings, reviews, and store modifications.
- **HowLongToBeat (HLTB) Integration**: Native messaging bridge that fetches main-story completion times and direct HLTB links for Steam games.
- **High Refresh Rate Support**: Automatically queries and applies the highest available display refresh rate for fluid scrolling and browsing.
- **Smart Back Navigation**: Seamless in-app browser history back navigation with hardware/gesture back button integration.
- **Arm64 Optimization**: Configured to target ARM64 architecture for lightweight builds and optimal performance.

---

## 🛠️ Tech Stack

- **Language**: Java
- **Browser Engine**: [Mozilla GeckoView](https://wiki.mozilla.org/Mobile/GeckoView)
- **Architecture**: Native Android Activities + WebExtensions (Native Messaging)
- **Build System**: Gradle

---

## 🚀 Getting Started

### Prerequisites
- Android Studio (Koala / Jellyfish or newer recommended)
- Android SDK (API Level 26+)
- JDK 17

### Building the App
1. Clone the repository:
   ```bash
   git clone https://github.com/Mystix03/SteamEnhanced.git
   ```
2. Open the project in Android Studio.
3. Sync Gradle and run the app on an ARM64 Android device or emulator.

---

## 📄 License

This project is for personal and educational use. Augmented Steam and HowLongToBeat assets belong to their respective creators and open-source contributors.
