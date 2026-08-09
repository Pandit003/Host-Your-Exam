# Walkthrough - Saving User Details to SharedPreferences

I have updated `LoginActivity` to ensure user details are fetched from Firestore and saved into `SharedPreferences` before the app navigates to the `MainActivity`.

## Changes

### [LoginActivity.kt](file:///D:/Host-Your-Exam/app/src/main/java/com/example/testmaster/activities/LoginActivity.kt)

-   **Added `saveToSharedPrefs(details: personalDetail)`**: This helper method saves the name, email, phone number, date of birth, and image URL into a `SharedPreferences` file named `"UserDetails"`.
-   **Added `fetchUserDetailsAndNavigate(uid: String)`**: This method fetches the user's document from the `personalDetails` collection in Firestore, saves the data to `SharedPreferences`, and then triggers the navigation to `MainActivity`.
-   **Updated Auto-login Logic**: In `onCreate`, if a user is already logged in, the app now fetches their details before navigating.
-   **Updated Email Login**: `performEmailLogin` now calls `fetchUserDetailsAndNavigate` upon successful authentication and email verification.
-   **Updated Google Login**: `checkIfUserExistsInFirestore` now saves the user details (either fetched from Firestore or newly created) to `SharedPreferences` before navigating.

## Verification Results

### Automated Analysis
I ran `analyze_file` on `LoginActivity.kt`, and no new errors were introduced by the changes.

### Manual Verification Recommended
1.  Launch the app.
2.  Log in using either Email/Password or Google Login.
3.  In `MainActivity`, you can now access these details using:
    ```kotlin
    val sharedPref = getSharedPreferences("UserDetails", Context.MODE_PRIVATE)
    val name = sharedPref.getString("name", null)
    // and so on...
    ```
