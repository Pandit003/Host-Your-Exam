# Walkthrough - Fixed Fragment Context Crash in ProgressFragment

I have fixed the `java.lang.IllegalStateException: Fragment not attached to a context` crash in `ProgressFragment.kt`.

## Changes Made

### [ProgressFragment](file:///D:/Host-Your-Exam/app/src/main/java/com/example/testmaster/fragments/ProgressFragment.kt)

- **Lifecycle Management**: Added a `ListenerRegistration` property to track the Firestore snapshot listener.
- **Cleanup**: Implemented `onDestroyView()` to properly remove the listener when the fragment's view is destroyed, preventing memory leaks and callbacks on a detached fragment.
- **Safety Checks**: Added `if (!isAdded) return@addSnapshotListener` at the beginning of the Firestore callback to ensure the fragment is still attached before performing any UI or context-related operations.
- **Safe Context Access**: Replaced `requireContext()` with `context` (nullable) and used safe calls for operations like accessing `SharedPreferences`.

## Verification Results

- The code now safely handles situations where the Firebase listener returns after the user has navigated away from the fragment.
- The `registration.remove()` call ensures that resources are freed and no more callbacks are received once the view is gone.
