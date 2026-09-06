# 🚀 How to Run the Echo Android App

## Prerequisites

Before running the app, make sure you have the following installed:

| Tool | Version | Download |
|------|---------|----------|
| Android Studio | Latest (Meerkat or newer) | [developer.android.com/studio](https://developer.android.com/studio) |
| JDK | 17+ (bundled with Android Studio) | Included |
| Android SDK | API 36 | Via Android Studio |

> **Note**: Android Studio is already installed on this machine at  
> `C:\Program Files\Android\Android Studio`

---

## Option 1 — Android Studio (Recommended)

### Step 1: Open the Project

1. Launch **Android Studio**
2. Click **File → Open**
3. Navigate to and select this folder:
   ```
   C:\Users\adith\Desktop\audioT\android app\Echo
   ```
4. Click **OK** and wait for **Gradle sync** to complete  
   *(First sync downloads ~200 MB of dependencies — takes 2–5 minutes)*

### Step 2: Set Up an Emulator

1. In the top toolbar, click **Device Manager** (phone icon) or go to  
   **Tools → Device Manager**
2. Click **Create Device**
3. Select a phone (e.g. **Pixel 9**) → Click **Next**
4. Select **API Level 36** → Download if needed → Click **Next**
5. Click **Finish**

### Step 3: Run the App

1. Select your emulator from the device dropdown in the toolbar
2. Press the **▶ Run** button (or `Shift + F10`)
3. The app will build, install, and launch automatically 🎉

---

## Option 2 — Physical Android Device

### Step 1: Enable Developer Options on Your Phone

1. Go to **Settings → About Phone**
2. Tap **Build Number** 7 times until you see *"You are now a developer"*
3. Go back to **Settings → Developer Options**
4. Enable **USB Debugging**

### Step 2: Connect and Run

1. Connect your phone via **USB cable**
2. Accept the *"Allow USB Debugging"* prompt on your phone
3. Open the project in **Android Studio**
4. Your device will appear in the device dropdown
5. Press **▶ Run**

---

## Option 3 — Command Line

### Step 1: Set Environment Variables

Open **PowerShell** and run:

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME = "C:\Users\adith\AppData\Local\Android\Sdk"
$env:PATH = $env:PATH + ";$env:JAVA_HOME\bin"
```

### Step 2: Navigate to Project

```powershell
cd "C:\Users\adith\Desktop\audioT\android app\Echo"
```

### Step 3: Build the APK

```powershell
.\gradlew assembleDebug
```

The APK will be generated at:
```
app\build\outputs\apk\debug\app-debug.apk
```

### Step 4: Install on Device / Emulator

```powershell
# Install directly (requires a running emulator or connected device)
.\gradlew installDebug

# OR install the APK manually via adb
adb install app\build\outputs\apk\debug\app-debug.apk
```

---

## Option 4 — Install APK Directly (Sideload)

Once you have the APK file (`app-debug.apk`):

1. Transfer the APK to your Android phone
2. On your phone: **Settings → Security → Install Unknown Apps** → enable for your file manager
3. Open the APK file and tap **Install**

---

## Project Structure

```
Echo/
├── app/src/main/java/com/example/echo/
│   ├── MainActivity.kt          # Entry point
│   ├── Navigation.kt            # Screen routing
│   ├── NavigationKeys.kt        # Route definitions
│   ├── EchoApplication.kt       # App + dependency container
│   ├── data/
│   │   ├── local/               # In-memory data store (mock data)
│   │   ├── remote/              # Mock AI transcription service
│   │   └── repository/          # Data access layer
│   ├── domain/model/            # Recording, Person, Interaction models
│   ├── theme/                   # Colors, typography, theme
│   └── ui/
│       ├── dashboard/           # Dashboard screen + ViewModel
│       ├── history/             # History screen + ViewModel
│       ├── recording/           # Recording detail screen
│       ├── person/              # Person detail screen
│       ├── profile/             # Profile screen
│       └── components/          # Shared UI components
└── gradle/
    └── libs.versions.toml       # Dependency versions
```

---

## App Features

| Feature | Status |
|---------|--------|
| Dashboard with upload zone | ✅ |
| File picker (MP3, WAV, M4A, etc.) | ✅ |
| Audio recording with timer | ✅ |
| Recent Summaries cards | ✅ |
| History with person cards | ✅ |
| Search / filter contacts | ✅ |
| Recording detail + transcript | ✅ |
| Person detail screen | ✅ |
| Profile screen | ✅ |
| Bottom navigation | ✅ |
| Mock AI processing (no backend needed) | ✅ |
| Dark mode support | ✅ |

---

## Minimum Requirements

| Requirement | Value |
|-------------|-------|
| Android Version | 7.0 (API 24) or higher |
| RAM | 2 GB+ |
| Storage | 50 MB free |
| Microphone | Required for recording feature |

---

## Troubleshooting

### ❌ "JAVA_HOME is not set"
→ Set it manually (see Option 3, Step 1) or open the project in Android Studio which handles this automatically.

### ❌ Gradle sync fails
→ Check your internet connection. The first sync downloads dependencies.  
→ Try: **File → Sync Project with Gradle Files**

### ❌ Device not detected
→ Make sure **USB Debugging** is enabled on your phone.  
→ Try a different USB cable or port.  
→ Run `adb devices` in terminal to verify connection.

### ❌ App crashes on launch
→ Check **Logcat** in Android Studio for the error.  
→ Make sure you're running Android 7.0+ (API 24+).

---

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM + StateFlow
- **Navigation**: Navigation3 (Compose)
- **Data**: In-memory store with `MutableStateFlow`
- **Audio**: Android `MediaRecorder` API
- **Min SDK**: API 24 (Android 7.0)
- **Target SDK**: API 36 (Android 16)
