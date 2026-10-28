# 👁️ Blink Match - Android Memory Game

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Android SDK](https://img.shields.io/badge/SDK-26%2B-green.svg?logo=android)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-brightgreen.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-purple.svg)](LICENSE)

> **See it. Remember it. Match it.**  
> A fast-paced visual & spatial memory training Android game built entirely with modern **Kotlin** and **Jetpack Compose**.

---

## 📱 About Blink Match

**Blink Match** tests and trains your visual working memory and visual-spatial recall.

1. **Watch**: A pattern of distinct symbols appears on a grid.
2. **Memorize**: You get a short window to memorize their exact grid locations before they **BLINK!** and disappear.
3. **Recall**: Tap the grid cells to reproduce the exact pattern before time runs out!

As you advance through rounds, grid dimensions expand, symbol counts increase, and time limits tighten—offering an engaging cognitive challenge for players of all skill levels.

---

## ✨ Features

- **🎮 Dynamic Game Engine**:
  - **Adaptive Grid Sizes**: Expands from `3x3` up to `6x6` based on round progression and selected difficulty.
  - **Smart Pattern Generation**: Balances symbol placement across grid quadrants and prevents identical repetitive patterns.
  - **Precision Timers**: Dynamic display and recall phases calibrated down to milliseconds.

- **🎯 Progressive Difficulty Modes**:
  - **Easy**: Starts at `3x3` with 2–6 symbols; generous recall timers.
  - **Medium**: Starts at `4x4` with 3–9 symbols; balanced speed & complexity.
  - **Hard**: Scales up to `6x6` with 4–12 symbols; fast display times for high-level memory training.

- **🔥 Scoring & Streak System**:
  - Real-time score calculation based on symbols remembered, speed bonus, round multiplier, and consecutive perfect streaks.
  - Visual feedback banners for "PERFECT MATCH!" and "MEMORY STREAK!".

- **🔊 Audio & Haptic Feedback Engine**:
  - **Synthesized Audio Engine**: Built-in tone generation using Android's low-latency `AudioTrack` and PCM synthesis (no bloated WAV/MP3 assets required).
  - **Haptic Patterns**: Tailored vibration ticks for taps, success, error, and streak triggers.

- **🎨 Modern Material 3 UI & Customization**:
  - **Theme Customization**: Full support for System Default, Light Theme, and Dark Theme modes.
  - **Accessibility**: Reduced Motion option to minimize scaling animations for sensitive users.
  - **Statistics Tracking**: Local persistence for High Score, Max Round, Accuracy %, and Longest Streak.

---

## 🛠 Tech Stack & Architecture

- **Language**: 100% [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 UI
- **Architecture**: Unidirectional Data Flow (UDF) with `ViewModel`, `StateFlow`, and Clean Layering:
  - `data`: Models, Repository, and DataStore preferences
  - `game`: Pattern Generator, Round Config Provider, and Score Engine
  - `ui`: Modular Composables (Screens, Custom Canvas Symbol Views, Game Board)
  - `audio`: Low-level PCM tone synthesizer (`AudioTrack`)
  - `haptics`: Custom vibration effects manager
- **Navigation**: `androidx.navigation:navigation-compose`
- **State & Concurrency**: Kotlin Coroutines & Flow

---

## 📂 Project Structure

```text
BlinkMatch/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/blinkmatch/
│   │   │   │   ├── audio/              # Synthesized SoundManager
│   │   │   │   ├── data/               # Models & GamePreferencesRepository
│   │   │   │   ├── game/               # Core pattern & scoring logic
│   │   │   │   ├── haptics/            # Haptic feedback helper
│   │   │   │   ├── ui/                 # Compose UI screens & components
│   │   │   │   │   ├── components/     # GameBoard, SymbolCanvas, GridCellView
│   │   │   │   │   ├── navigation/     # NavHost & Screen definitions
│   │   │   │   │   ├── screens/        # Home, Game, Result, Settings, About
│   │   │   │   │   └── theme/          # Color palettes, Typography, Theme
│   │   │   │   ├── viewmodel/          # BlinkMatchViewModel & GameUiState
│   │   │   │   └── MainActivity.kt     # Main entry point with edge-to-edge
│   │   │   └── AndroidManifest.xml
│   │   └── test/                       # Unit tests (Game logic & scoring)
│   └── build.gradle.kts
├── gradle/libs.versions.toml           # Version Catalog
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Ladybug / Jellyfish or newer (2024.1+)
- **JDK**: Version 11 or later
- **Android SDK**: Minimum SDK 26 (Android 8.0), Target SDK 37

### Building from Source

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/Blink-Match.git
   cd Blink-Match
   ```

2. **Build the Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```

3. **Run Unit Tests**:
   ```bash
   ./gradlew test
   ```

4. **Install on Device / Emulator**:
   ```bash
   ./gradlew installDebug
   ```

---

## 🧪 Testing

The game logic is thoroughly covered by unit tests located in `app/src/test/java/com/example/blinkmatch/BlinkMatchGameTest.kt`:
- **Pattern Generator**: Validates non-duplicate cell positioning, grid boundaries, and symbol balancing.
- **Score Calculator**: Tests base scores, speed bonus accuracy, and streak scaling logic.
- **Round Configuration**: Validates dynamic size and recall/display duration formulas across difficulties.

To execute tests:
```bash
./gradlew testDebugUnitTest
```

---

## 📄 License

```text
MIT License

Copyright (state) 2026 Abdullah Ateeq

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction privileges...
```

---

<p align="center">
  Developed with ❤️ by <a href="https://github.com/abdullah-ateeq">Abdullah Ateeq</a>
</p>
