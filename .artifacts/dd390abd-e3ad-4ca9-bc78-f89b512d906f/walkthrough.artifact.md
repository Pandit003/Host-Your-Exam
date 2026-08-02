# Walkthrough - Settings and Theme Management

I have successfully added a comprehensive Settings feature to the app, including theme switching, notification management, and a centralized logout option.

## Key Features Added

### 1. New Settings Screen
A modern and attractive Settings screen has been implemented using Material 3 design principles. It features grouped settings in elevated cards for better readability.

- **Appearance**: Toggle between Light and Dark modes.
- **Notifications**: Manage app-wide notification preferences.
- **Account**: Unified logout button.

### 2. Theme Switching & Persistence
The app now supports dynamic theme switching. The user's preference is saved in `SharedPreferences` and applied automatically during app startup (via `SplashScreen` and `LoginActivity`) to ensure a seamless experience.

### 3. Updated Navigation
The "Logout" option has been moved from the main navigation drawer into the Settings screen to keep the primary menu clean and organized.

## Changes Made

#### [MODIFY] [drawer_menu.xml](file:///D:/Host-Your-Exam/app/src/main/res/menu/drawer_menu.xml)
- Added "Setting" item.
- Removed "Logout" item.

#### [MODIFY] [MainActivity.kt](file:///D:/Host-Your-Exam/app/src/main/java/com/example/testmaster/activities/MainActivity.kt)
- Implemented theme loading logic.
- Added navigation handler for the Settings screen.

#### [NEW] [SettingsActivity.kt](file:///D:/Host-Your-Exam/app/src/main/java/com/example/testmaster/activities/SettingsActivity.kt) & [activity_settings.xml](file:///D:/Host-Your-Exam/app/src/main/res/layout/activity_settings.xml)
- Created the core settings logic and UI.

#### [MODIFY] [SplashScreen.kt](file:///D:/Host-Your-Exam/app/src/main/java/com/example/testmaster/activities/SplashScreen.kt) & [LoginActivity.kt](file:///D:/Host-Your-Exam/app/src/main/java/com/example/testmaster/activities/LoginActivity.kt)
- Added theme persistence checks to ensure the user's choice is respected from the first screen.

## Verification Results
- **Build**: Successfully compiled using `:app:assembleDebug`.
- **UI**: Verified layout structure for `activity_settings.xml` adheres to the requested "good and attractive UI" using Material Cards and modern spacing.
- **Persistence**: Logic verified to save and load `DarkMode` and `Notifications` keys from `AppSettings` SharedPreferences.
