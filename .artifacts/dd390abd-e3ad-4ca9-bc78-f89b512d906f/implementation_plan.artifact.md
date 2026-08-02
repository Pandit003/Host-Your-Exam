# Implementation Plan - Add Settings and Theme Switching

The goal is to enhance the user experience by adding a "Settings" option to the Navigation Drawer. This Settings screen will allow users to toggle between Light and Dark themes, manage notifications, and logout of the application.

## User Review Required

> [!IMPORTANT]
> The theme switching will be implemented using `AppCompatDelegate`. This will cause the current Activity (and any in the backstack) to recreate to apply the new theme.
>
> [!NOTE]
> The "Notification Management" will currently be a toggle that saves the preference to `SharedPreferences`. Actual integration with a notification service (like FCM) is out of scope unless already implemented elsewhere.

## Proposed Changes

### Navigation

#### [MODIFY] [drawer_menu.xml](file:///D:/Host-Your-Exam/app/src/main/res/menu/drawer_menu.xml)
- Uncomment and update the `setting` item.
- Remove the `logout` item from the main menu as it moves into Settings.

#### [MODIFY] [MainActivity.kt](file:///D:/Host-Your-Exam/app/src/main/java/com/example/testmaster/activities/MainActivity.kt)
- Handle the `setting` menu item click to navigate to `SettingsActivity`.
- Apply the saved theme preference in `onCreate` to ensure the app starts with the user's chosen theme.

### Settings Feature

#### [NEW] [SettingsActivity.kt](file:///D:/Host-Your-Exam/app/src/main/java/com/example/testmaster/activities/SettingsActivity.kt)
- Create a new Activity to host settings.
- Implement logic for:
    - **Theme Toggle**: Switch between Light and Dark modes.
    - **Notification Toggle**: Enable/Disable notifications (saves to `SharedPreferences`).
    - **Logout**: Signs out from Firebase and Google, then redirects to `LoginActivity`.

#### [NEW] [activity_settings.xml](file:///D:/Host-Your-Exam/app/src/main/res/layout/activity_settings.xml)
- Design an attractive UI for the settings screen.
- Use Material 3 components like `SwitchMaterial`, `MaterialButton`, and `CardView` for a modern look.

### Theme Support

#### [MODIFY] [themes.xml](file:///D:/Host-Your-Exam/app/src/main/res/values/themes.xml) and [themes.xml (night)](file:///D:/Host-Your-Exam/app/src/main/res/values-night/themes.xml)
- Ensure colors are properly defined for both Light and Dark modes if needed (they seem to be using `DayNight` parent already).

## Verification Plan

### Manual Verification
1.  Open the Navigation Drawer.
2.  Click on "Settings".
3.  Toggle the Theme switch and verify the app theme changes immediately.
4.  Toggle the Notification switch and verify it persists (re-open settings to check).
5.  Click the "Logout" button and verify it redirects to the Login screen.
6.  Restart the app and verify the theme preference is remembered.
