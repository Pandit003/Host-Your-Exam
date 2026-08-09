# Implementation Plan - Fix IllegalStateException in ProgressFragment

The `ProgressFragment` crashes with `java.lang.IllegalStateException: Fragment not attached to a context` when a Firestore snapshot listener callback triggers after the fragment has been detached from its activity. This happens because `requireContext()` is called within the callback.

## Proposed Changes

### [ProgressFragment](file:///D:/Host-Your-Exam/app/src/main/java/com/example/testmaster/fragments/ProgressFragment.kt)

#### [MODIFY] [ProgressFragment.kt](file:///D:/Host-Your-Exam/app/src/main/java/com/example/testmaster/fragments/ProgressFragment.kt)
- Add `private var registration: ListenerRegistration? = null` to track the Firestore listener.
- In `fetchProgressData()`, assign the result of `addSnapshotListener` to `registration`.
- Add an `if (!isAdded) return@addSnapshotListener` check at the beginning of the listener callback.
- Replace `requireContext()` with `context` and use safe calls (e.g., `context?.getSharedPreferences(...)`).
- Update `updateUI()` to safely access resources or use `ContextCompat` with a null-checked context.
- Override `onDestroyView()` to remove the listener using `registration?.remove()`.

## Verification Plan

### Automated Tests
- N/A (UI and Firebase related, hard to unit test without mocks)

### Manual Verification
- Navigate to the Progress screen.
- Rapidly navigate away from the Progress screen while data is loading.
- Verify that the app no longer crashes.
