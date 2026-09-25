# Taskr

A modern Android task management application built with Kotlin, designed to help you organize, track, and complete your daily tasks efficiently.

## Features

- ✅ **Create & Manage Tasks** — Add, edit, and delete tasks with ease
- 📅 **Calendar View** — Visualize tasks across a calendar interface
- ⏰ **Task Reminders** — Set notifications for task deadlines
- ✔️ **Task Completion Tracking** — Mark tasks complete and view your progress
- 💾 **Local Data Persistence** — All data stored locally on your device using Room Database
- ⚙️ **Customizable Settings** — Personalize your app experience
- 📱 **Material Design 3** — Modern, intuitive UI following Android design guidelines

## Tech Stack

- **Language:** Kotlin
- **Platform:** Android (API 23 – 34, Android 6.0+)
- **Architecture:** MVVM with Repository Pattern
- **Database:** Room Database
- **Dependency Injection:** Hilt
- **Navigation:** Android Navigation Component
- **Async Processing:** Kotlin Coroutines
- **Preferences:** DataStore
- **UI:** Material Design 3, AndroidX

### Key Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| AndroidX Core | 1.12.0 | Core Android utilities |
| AndroidX AppCompat | 1.6.1 | Backward compatibility |
| Material Design | 1.11.0 | Material Design 3 components |
| Room | 2.6.1 | Local database & persistence |
| Hilt | 2.50 | Dependency injection |
| Navigation | 2.7.7 | Fragment-based navigation |
| Lifecycle | 2.7.0 | ViewModels, LiveData |
| Coroutines | 1.7.3 | Async/await operations |
| DataStore | 1.0.0 | Preferences management |

## Project Structure

```
app/
├── src/main/
│   ├── java/com/rizowan/taskr/
│   │   ├── TaskrApplication.kt           # App entry point, Hilt initialization
│   │   │
│   │   ├── data/                         # Data layer (Repository pattern)
│   │   │   ├── local/                    # Room Database entities & DAOs
│   │   │   ├── preferences/              # DataStore preferences management
│   │   │   └── repository/               # Repository implementations
│   │   │
│   │   ├── di/                           # Hilt dependency injection modules
│   │   │
│   ���   ├── notification/                 # Notification scheduling & handling
│   │   │
│   │   ├── ui/                           # UI layer (Fragments & Activities)
│   │   │   ├── MainActivity.kt           # Main container activity
│   │   │   ├── splash/                   # Splash screen
│   │   │   ├── tasks/                    # Task list (home screen)
│   │   │   ├── addedittask/              # Create/edit task screens
│   │   │   ├── calendar/                 # Calendar view of tasks
│   │   │   ├── completed/                # Completed tasks history
│   │   │   ├── settings/                 # Settings & preferences UI
│   │   │   └── about/                    # About screen
│   │   │
│   │   └── util/                         # Utility helpers & extensions
│   │
│   ├── res/                              # Resources
│   │   ├── layout/                       # XML layouts
│   │   ├── drawable/                     # Images & vector graphics
│   │   ├── values/                       # Strings, colors, styles, dimensions
│   │   └── ...
│   │
│   └── AndroidManifest.xml
│
├── schemas/                              # Room database schema versions
├── build.gradle.kts                      # App-level build configuration
└── proguard-rules.pro                    # ProGuard minification rules
```

## Architecture

Taskr follows **MVVM (Model-View-ViewModel)** architecture combined with the **Repository Pattern** for clean separation of concerns:

```
UI Layer (Fragments/Activities)
    ↓
ViewModels + LiveData
    ↓
Repositories
    ↓
Data Sources (Room DB, DataStore)
```

- **UI Layer:** Fragments display data and respond to user interactions
- **ViewModel Layer:** Manages UI state, handles lifecycle-aware data
- **Repository Layer:** Abstracts data sources and provides unified API
- **Data Layer:** Room Database for tasks, DataStore for preferences

## Getting Started

### Prerequisites

- Android Studio (latest stable version)
- Android SDK API 34
- JDK 17 or higher
- Gradle 8.2.2+

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/rizowan120/Taskr.git
   cd Taskr
   ```

2. **Build the project:**
   ```bash
   ./gradlew build
   ```

3. **Run on emulator or device:**
   ```bash
   ./gradlew installDebug
   ```

   Or open the project in Android Studio and click **Run** → **Run 'app'**

### Build Variants

- **Debug:** Full logging, no minification, fast builds
- **Release:** Minified, optimized for distribution (requires signing configuration)

## Configuration

### Signing Configuration (Release Builds)

For release builds, set the following environment variables:

```bash
export KEYSTORE_PATH=/path/to/your/release.keystore
export KEYSTORE_PASSWORD="your_keystore_password"
export KEY_ALIAS="your_key_alias"
export KEY_PASSWORD="your_key_password"
```

Then build:
```bash
./gradlew assembleRelease
```

### Gradle Properties

Edit `gradle.properties` to modify:
- JVM memory allocation: `org.gradle.jvmargs=-Xmx2048m`
- Kotlin code style: `kotlin.code.style=official`

## Features Deep Dive

### Task Management
- Create tasks with title, description, priority, and due date
- Edit existing tasks
- Delete tasks with confirmation
- Mark tasks as complete

### Calendar Integration
- View all tasks on a calendar
- Filter tasks by date
- Quick task creation from calendar date

### Reminders & Notifications
- Set reminders for individual tasks
- Automatic notification scheduling via WorkManager
- High-priority notifications with vibration and badge

### Settings
- Enable/disable notifications
- Customize reminder timing
- App theme preferences (if applicable)

### Data Persistence
- All data stored locally in Room Database
- No cloud sync or internet required
- Schema versioning for database migrations

## Development Guidelines

### Code Style
- Follows Kotlin official conventions (`kotlin.code.style=official`)
- AndroidX and Jetpack recommended patterns
- Proper null-safety usage

### Dependencies Management
- Uses Hilt for dependency injection
- Coroutines for asynchronous operations
- ViewModels with LiveData for state management

### Building & Testing

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run instrumented tests (on device/emulator)
./gradlew connectedAndroidTest

# Check lint
./gradlew lint

# Build release APK (with signing config)
./gradlew assembleRelease
```

## File Sizes & Performance

- **Compilation SDK:** 34
- **Min SDK:** 23 (Android 6.0)
- **Target SDK:** 34 (Android 14)
- **ProGuard:** Enabled for release builds to reduce APK size

## API Levels Supported

| API Level | Version | Support |
|-----------|---------|---------|
| 23 | Android 6.0 | Minimum |
| 34 | Android 14 | Target |

## Notification Channels

The app creates notification channels for:
- **taskr_reminders** — High-priority task reminder notifications

Notifications are configured to show badges and enable vibration on Android 8.0+ (API 26+).

## Troubleshooting

### Build Issues

**Gradle sync fails:**
- Ensure JDK 17+ is installed
- Check `$JAVA_HOME` environment variable
- Run `./gradlew clean` and rebuild

**KSP (Kotlin Symbol Processing) errors:**
- Invalidate caches: Android Studio → File → Invalidate Caches
- Clean and rebuild the project

### Runtime Issues

**Database migration errors:**
- Clear app data: Settings → Apps → Taskr → Storage → Clear
- Reinstall the app

**Notifications not showing:**
- Check notification permissions in app settings
- Verify notification channel is created (Android 8.0+)
- Ensure reminders are enabled in app preferences

## Future Enhancements

- [ ] Cloud sync (Firebase)
- [ ] Task categories/tags
- [ ] Recurring tasks
- [ ] Dark theme
- [ ] Multi-language support
- [ ] Task templates

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is currently unlicensed. See the repository for more information.

## Author

**rizowan120** — [@rizowan120](https://github.com/rizowan120)

## Support

For issues, questions, or suggestions, please open a GitHub issue: [Create Issue](https://github.com/rizowan120/Taskr/issues)

---

**Last Updated:** September 2026  
**Version:** 1.1.8
