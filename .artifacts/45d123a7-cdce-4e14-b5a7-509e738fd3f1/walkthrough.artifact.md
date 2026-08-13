# Walkthrough - Enhanced User Search & Subscriber Counts

I have improved the user search functionality to be case-insensitive and support prefix matching. I've also added visible subscriber counts to the search results.

## Key Changes

### 1. Case-Insensitive Prefix Search
- **Data Model**: Added a `name_lowercase` field to the `personalDetail` model to store the lowercase version of the username.
- **Auto-Sync**: Updated `RegisterActivity` and `EditProfileActivity` to automatically save this field whenever a username is created or changed.
- **Logic**: Refactored the search query in `SearchExamId` to use the `name_lowercase` field. It now supports:
    - **Case-insensitivity**: Searching "PANDIT" or "pandit" yields the same result.
    - **Prefix matching**: Results appear as you type (e.g., typing "Pa" will show "Pandit").

### 2. Subscriber Counts in Search
- **UI Upgrade**: Added a "Subscribers Count" label to each user item in the search results (`item_user.xml`).
- **Real-time Stats**: Stored a dedicated `subscribersCount` field in the user profile for high-performance loading.
- **Automatic Updates**: Updated the subscription logic in `UserSearchAdapter` to automatically increment or decrement the target user's `subscribersCount` in Firestore whenever someone subscribes or unsubscribes.

### 3. Profile Data Integrity
- Modified `EditProfileActivity` to ensure that when a user updates their profile, their existing subscriber count is preserved and not reset.

## How to Test
1. **Search**: Go to the Search screen and start typing a username. Notice how results appear instantly and match regardless of capitalization.
2. **View Counts**: Each user in the results should now display their total subscriber count (e.g., "5 Subscribers").
3. **Subscribe/Unsubscribe**: Tap the Subscribe button on a user. The count in the database will update automatically.

> [!NOTE]
> Existing users in the database will need to update their profile once (or wait for a system-wide migration) to appear in the new prefix-based, case-insensitive search. New users will be searchable instantly.
