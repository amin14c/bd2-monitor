# MoodCare

A Kotlin-based Android application for daily mental health tracking, featuring mood logging, sleep monitoring, and medication reminders.

## Features

- **Mood Tracking** – Daily mood ratings (1-10) with notes and weekly/monthly statistics
- **Sleep Monitor** – Log sleep duration and quality with bedtime reminders
- **Medication Reminders** – Scheduled alerts with adherence tracking

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose, Material Design 3
- **Database:** Room
- **Background Tasks:** WorkManager, AlarmManager
- **Charts:** MPAndroidChart

## Requirements

- Android 8.0 (API 26) or higher
- Android Studio Hedgehog+

## Installation

```bash
git clone https://github.com/username/moodcare.git
cd moodcare
./gradlew assembleDebug
