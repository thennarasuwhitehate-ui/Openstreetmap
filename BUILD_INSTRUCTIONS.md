# NaviBharat - Complete Build & Deployment Guide

## System Requirements

- **Android Studio** 2023.1 or later
- **JDK** 11 or higher
- **Gradle** 8.0 or later (included with Android Studio)
- **Android SDK** API Level 34
- **Minimum Android Version**: API Level 24 (Android 7.0)
- **Target Android Version**: API Level 34 (Android 14)

## Pre-Build Setup

### 1. Download Android SDK Components

Open Android Studio and go to:
```
Settings → Appearance & Behavior → System Settings → Android SDK
```

Install these components:
- ✅ Android SDK Platform 34
- ✅ Android SDK Build-Tools 34.0.0 (or latest)
- ✅ Google Play Services
- ✅ Intel HAXM (for emulator support)
- ✅ Android Emulator

### 2. Configure API Keys

Create or update `local.properties` in the project root:

```properties
sdk.dir=/path/to/Android/sdk

# Google Maps API (get from https://console.cloud.google.com)
GOOGLE_MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY

# Google Places API (same project as above)
GOOGLE_PLACES_API_KEY=YOUR_GOOGLE_PLACES_API_KEY

# Google Gemini API (get from https://makersuite.google.com/app/apikey)
GEMINI_API_KEY=YOUR_GEMINI_API_KEY

# Supabase Configuration (get from https://supabase.com)
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your_supabase_anon_key
```

**Example (Windows):**
```properties
sdk.dir=C:\Users\YourUsername\AppData\Local\Android\sdk
```

**Example (macOS/Linux):**
```properties
sdk.dir=/Users/YourUsername/Library/Android/sdk
# or
sdk.dir=~/Android/sdk
```

### 3. Get API Keys

#### Google Maps & Places API
1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Create a new project
3. Enable these APIs:
   - Maps SDK for Android
   - Places API
   - Maps Directions API
4. Create Android API Key:
   - Go to Credentials
   - Create new API Key
   - Restrict to Android apps
   - Add your app's package name: `com.navibharat`
5. Copy the key to `local.properties`

#### Google Gemini API
1. Go to [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Create an API key
3. Copy to `local.properties`

#### Supabase (Optional - for backend)
1. Go to [Supabase](https://supabase.com)
2. Create new project
3. Copy URL and Anon Key to `local.properties`

## Building the App

### Method 1: Using Android Studio (Recommended)

1. **Open Project**
   - File → Open
   - Select the NaviBharat project root directory
   - Wait for Gradle sync to complete

2. **Build the App**
   - Menu: Build → Clean Project
   - Menu: Build → Build Bundle(s)/APK(s) → Build APK(s)
   - Wait for build to complete

3. **Run on Emulator**
   - Menu: Run → Run 'app'
   - Select an Android device/emulator
   - App will install and launch

### Method 2: Using Command Line

```bash
# Navigate to project root
cd /path/to/NaviBharat

# Clean previous builds
./gradlew clean

# Build debug APK
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk

# Build release APK
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk

# Build App Bundle (for Play Store)
./gradlew bundleRelease
# Output: app/build/outputs/bundle/release/app-release.aab

# Build and install on connected device
./gradlew installDebug

# Run unit tests
./gradlew test

# Run all checks
./gradlew check
```

### Method 3: Using Gradle Wrapper (if available)

```bash
# Make gradlew executable (Linux/macOS)
chmod +x gradlew

# Build
./gradlew clean build
./gradlew assembleDebug
```

## Troubleshooting Build Errors

### Error: "SDK location not found"
**Solution**: Ensure `local.properties` has correct `sdk.dir` path

```bash
# Find your SDK location
find ~ -name "platforms" -path "*/Android/sdk/*" 2>/dev/null
```

### Error: "Gradle sync failed"
1. Go to File → Settings → Build → Gradle
2. Select "Use default Gradle wrapper"
3. Click Sync Now

### Error: "API key invalid for specified Android application"
1. Verify API key is enabled for "Android" applications
2. Check package name matches in Google Cloud Console: `com.navibharat`
3. Get app's SHA1 fingerprint:
   ```bash
   ./gradlew signingReport
   ```
4. Add SHA1 to Google Cloud Console API restrictions

### Error: "Compilation failed; see the compiler error output"
1. Clean build: `./gradlew clean`
2. Invalidate caches: File → Invalidate Caches → Clear caches and restart
3. Rebuild: `./gradlew build`

### Error: "Out of memory during Gradle build"
Increase heap size in `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxPermSize=1024m
```

### Error: "Unresolved dependency"
1. Sync Gradle: File → Sync Now
2. Close Android Studio completely
3. Delete `.gradle` folder in project root
4. Reopen Android Studio
5. Wait for Gradle sync

## Testing the App

### On Android Emulator

1. Open Android Virtual Device (AVD) Manager
2. Create or select a device with:
   - Target API: 34 (or 24-34)
   - Recommended: 1GB+ RAM
3. Launch emulator
4. Run app: `./gradlew installDebug`

### On Physical Device

1. Enable Developer Mode:
   - Go to Settings → About Phone
   - Tap Build Number 7 times
2. Enable USB Debugging:
   - Settings → Developer Options → USB Debugging
3. Connect via USB
4. Run: `./gradlew installDebug`

### Manual Testing Checklist

- [ ] App launches without crashes
- [ ] Navigation tab opens map
- [ ] Search works with autocomplete
- [ ] Settings persist after restart
- [ ] Voice commands recognized (if network available)
- [ ] Permissions dialog appears
- [ ] Dark mode displays correctly

## Release Build & Play Store Deployment

### 1. Create Signing Key

```bash
keytool -genkey -v -keystore navibharat.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias navibharat-key
```

### 2. Sign Release APK

Create `keystore.properties`:
```properties
storeFile=navibharat.keystore
storePassword=YOUR_PASSWORD
keyAlias=navibharat-key
keyPassword=YOUR_PASSWORD
```

Update `app/build.gradle.kts`:
```kotlin
signingConfigs {
    release {
        val keystoreFile = rootProject.file("navibharat.keystore")
        if (keystoreFile.exists()) {
            storeFile = keystoreFile
            storePassword = "YOUR_PASSWORD"
            keyAlias = "navibharat-key"
            keyPassword = "YOUR_PASSWORD"
        }
    }
}

buildTypes {
    release {
        signingConfig = signingConfigs.release
        isMinifyEnabled = true
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
}
```

### 3. Build Release Bundle

```bash
./gradlew bundleRelease
# Output: app/build/outputs/bundle/release/app-release.aab
```

### 4. Upload to Play Store

1. Go to [Google Play Console](https://play.google.com/console)
2. Create app
3. Fill in app details:
   - Title: NaviBharat
   - Description (in 6 languages)
   - Screenshots (5+ per language)
   - Privacy policy URL
4. Go to Release → Production
5. Upload `app-release.aab`
6. Review and publish

## Project Structure

```
NaviBharat/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/navibharat/
│   │   │   │   ├── data/             (APIs, database, auth)
│   │   │   │   ├── ui/               (Activities, Fragments, ViewModels)
│   │   │   │   ├── navigation/       (Turn-by-turn engine)
│   │   │   │   ├── voice/            (Speech & AI)
│   │   │   │   ├── features/         (Tolls, fuel, services, alerts)
│   │   │   │   ├── maps/             (Map abstraction)
│   │   │   │   ├── di/               (Dependency injection)
│   │   │   │   ├── analytics/        (Firebase integration)
│   │   │   │   └── utils/            (Constants, helpers)
│   │   │   └── res/
│   │   │       ├── layout/           (UI layouts)
│   │   │       ├── values/           (Strings, colors, styles)
│   │   │       └── drawable/         (Icons)
│   │   ├── test/                      (Unit tests)
│   │   └── androidTest/               (Instrumented tests)
│   └── build.gradle.kts
├── maps_abstraction/                  (Map module)
├── voice_engine/                      (Voice module)
├── build.gradle.kts                   (Project-level)
├── settings.gradle.kts                (Module config)
├── gradle.properties                  (Gradle settings)
├── local.properties                   (API keys)
└── README.md
```

## Gradle Tasks Reference

```bash
# Build tasks
./gradlew clean                 # Clean build directory
./gradlew build                 # Build all
./gradlew assemble              # Build all APKs
./gradlew assembleDebug         # Build debug APK
./gradlew assembleRelease       # Build release APK
./gradlew bundleRelease         # Build App Bundle for Play Store

# Test tasks
./gradlew test                  # Run unit tests
./gradlew connectedAndroidTest  # Run instrumented tests
./gradlew check                 # Run all checks including tests

# Install tasks
./gradlew installDebug          # Install debug APK on device
./gradlew installRelease        # Install release APK on device

# Utility tasks
./gradlew dependencies          # Show dependency tree
./gradlew signingReport         # Show signing certificate info
./gradlew androidDependencies   # Show Android dependencies
./gradlew lint                  # Run Android lint checks
```

## Development Environment Setup

### Android Studio Plugins
Recommended plugins:
- ✅ Kotlin (built-in)
- ✅ Android Lint (built-in)
- ✅ Gradle (built-in)
- ✅ Material Theme UI
- ✅ SonarLint (for code quality)
- ✅ Logcat (built-in)

### Code Style
Project uses:
- Kotlin coding standards
- AndroidX libraries
- Material Design 3
- Follow existing patterns in code

## Continuous Integration

### GitHub Actions Example

Create `.github/workflows/build.yml`:
```yaml
name: Build and Test

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK
        uses: actions/setup-java@v3
        with:
          java-version: '11'
      - name: Build with Gradle
        run: ./gradlew build
      - name: Run tests
        run: ./gradlew test
      - name: Upload artifacts
        uses: actions/upload-artifact@v3
        with:
          name: apk
          path: app/build/outputs/apk/
```

## Performance Optimization

### Gradle Build Optimization
```properties
# gradle.properties
# Parallel build
org.gradle.parallel=true

# Daemon
org.gradle.daemon=true

# Build cache
org.gradle.caching=true

# Offline mode (after initial build)
# org.gradle.offline=true
```

### Minification & Optimization

The release build automatically:
- Enables ProGuard/R8 code obfuscation
- Shrinks unused resources
- Optimizes layout inflation
- Minifies code

## Support & Resources

- **Official Docs**: https://developer.android.com
- **Kotlin**: https://kotlinlang.org
- **AndroidX**: https://developer.android.com/jetpack/androidx
- **Gradle Docs**: https://gradle.org/docs
- **Play Store**: https://play.google.com/console

## Quick Reference

| Task | Command |
|------|---------|
| Build Debug APK | `./gradlew assembleDebug` |
| Build Release APK | `./gradlew assembleRelease` |
| Install on Device | `./gradlew installDebug` |
| Run Tests | `./gradlew test` |
| Clean Build | `./gradlew clean build` |
| Find Errors | `./gradlew lint` |
| Get Dependencies | `./gradlew dependencies` |
| Build Bundle (Play Store) | `./gradlew bundleRelease` |

---

**Version**: 1.0.0
**Last Updated**: 2024-12-23
**Status**: Production Ready ✅
