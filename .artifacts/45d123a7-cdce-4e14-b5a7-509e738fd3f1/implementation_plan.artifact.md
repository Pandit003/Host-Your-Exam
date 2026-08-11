# Implementation Plan - Enhanced Announcement System with App-Wide Updates

Upgrade the announcement system to support multiple post types, a modern UI, and integrated app-wide (SYSTEM) announcements.

## Proposed Changes

### [Component: Data Models]

#### [MODIFY] [Announcement.kt](file:///D:/Pandit.P/Pandit%20R&D/Host-Your-Exam/app/src/main/java/com/example/testmaster/model/Announcement.kt)
- Add `val type: String? = "EXAM"` (Values: "EXAM", "MESSAGE") to distinguish post types.

### [Component: Posting UI & Logic]

#### [MODIFY] [activity_post_announcement.xml](file:///D:/Pandit.P/Pandit%20R&D/Host-Your-Exam/app/src/main/res/layout/activity_post_announcement.xml)
- Implement a `TabLayout` or `SegmentedButton` to choose between "Announce Exam" and "Message".
- Group exam fields (Date, Duration, etc.) into a `LinearLayout` that toggles visibility based on the selection.
- Modernize the layout with better grouping and icons for an "attractive" look.

#### [MODIFY] [PostAnnouncementActivity.kt](file:///D:/Pandit.P/Pandit%20R&D/Host-Your-Exam/app/src/main/java/com/example/testmaster/activities/PostAnnouncementActivity.kt)
- Add logic to show/hide exam fields based on type selection.
- Update `postAnnouncement` to save the `type` and handle conditional validation.

### [Component: Home Screen & App Announcements]

#### [MODIFY] [HomeFragment.kt](file:///D:/Pandit.P/Pandit%20R&D/Host-Your-Exam/app/src/main/java/com/example/testmaster/fragments/HomeFragment.kt)
- Update `fetchAnnouncements` to:
    1. Fetch followed user UIDs.
    2. Add a special "SYSTEM" UID to the query list.
    3. Fetch announcements where `announcerUid` is in the expanded list.
- Add a `seedAppAnnouncements()` function called once to ensure two default "App" announcements exist in Firestore.

### [Component: Details View]

#### [MODIFY] [AnnouncementDetailsActivity.kt](file:///D:/Pandit.P/Pandit%20R&D/Host-Your-Exam/app/src/main/java/com/example/testmaster/activities/AnnouncementDetailsActivity.kt)
- Conditionally hide the exam specification grid if the announcement `type` is "MESSAGE".

## Verification Plan

### Manual Verification
1. **Posting**:
   - Post an "Exam" and verify all fields are saved.
   - Post a "Message" and verify only title/desc are required.
2. **App Announcements**:
   - Verify that "SYSTEM" announcements appear even if no one is followed.
3. **UI/UX**:
   - Verify the new "attractive" layout and type-specific details view.
