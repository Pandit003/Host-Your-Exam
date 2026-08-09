# Implementation Plan - Save User Details to SharedPreferences in LoginActivity

The goal is to fetch user details (name, email, phone, dob, image URL) from Firestore and save them into `SharedPreferences` before navigating to the `MainActivity`. This ensures that user information is readily available throughout the app without frequent Firestore calls.

## User Review Required

> [!IMPORTANT]
> The details will be fetched from the `personalDetails` collection in Firestore using the user's UID. If the data is missing in Firestore, only the fields available will be saved.

## Proposed Changes

### [app](file:///D:/Host-Your-Exam/app)

#### [MODIFY] [LoginActivity.kt](file:///D:/Host-Your-Exam/app/src/main/java/com/example/testmaster/activities/LoginActivity.kt)

-   Implement `saveToSharedPrefs(details: personalDetail)` to store user info in `SharedPreferences`.
-   Implement `fetchUserDetailsAndNavigate(uid: String)` to retrieve data from Firestore, save it, and then navigate.
-   Update `onCreate` auto-login logic to fetch details.
-   Update `performEmailLogin` to fetch details after successful authentication.
-   Update `checkIfUserExistsInFirestore` (for Google Login) to save fetched or newly created details.

## Verification Plan

### Manual Verification
-   Log in with an existing email/password account.
-   Log in with a Google account.
-   Check if the app correctly navigates to `MainActivity`.
-   (Optional) Verify `SharedPreferences` content using Layout Inspector or by adding temporary logs in `MainActivity`.
