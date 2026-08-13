# Implementation Plan - Subscriber Count Sync

Refactor how subscriber counts are managed to ensure `personalDetails` is the single source of truth during search and `MainActivity` handles synchronization for the current user.

## Proposed Changes

### [MainActivity] Count Sync
#### [MODIFY] [MainActivity.kt](file:///D:/Pandit.P/Pandit%20R&D/Host-Your-Exam/app/src/main/java/com/example/testmaster/activities/MainActivity.kt)
- Add a new private function `syncSubscriberCount(userId: String)`:
    - Query the `Subscribers` collection: `db.collection("Subscribers").document(userId).collection("UserSubscribers").get()`.
    - Get the `size` of the resulting documents.
    - Update the `personalDetails` document for `userId` with the actual count: `.update("subscribersCount", count)`.
- Call `syncSubscriberCount(userId)` inside `onCreate` (after `userId` is confirmed non-null).

### [UserSearchAdapter] Display Logic
#### [MODIFY] [UserSearchAdapter.kt](file:///D:/Pandit.P/Pandit%20R&D/Host-Your-Exam/app/src/main/java/com/example/testmaster/adapter/UserSearchAdapter.kt)
- Ensure the `tvSubscribersCount` is strictly using `user.subscribersCount` (it already does).
- **Optional/Clarification**: If "don't touch subscriber collection" includes checking subscription status, I will switch `checkSubscriptionStatus` to check the `Following` collection of the **current user** instead of the `Subscribers` collection of the **target user**.

## Verification Plan

### Manual Verification
- **MainActivity Sync**: Subscribe to yourself (or another test account) using a different device. Open `MainActivity` on the first device and check if the `subscribersCount` in the `personalDetails` collection (visible in Firebase Console) updates to match the actual number of documents in `Subscribers/{uid}/UserSubscribers`.
- **Search Display**: Search for a user and verify their subscriber count matches what is stored in their `personalDetails` document.
- **Toggle Subscription**: Verify that subscribing/unsubscribing still works and that the `subscribersCount` in `personalDetails` is still updated (keeping `FieldValue.increment` as it provides real-time feedback until the next sync).
