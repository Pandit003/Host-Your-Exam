# Implementation Plan - Notification Service

Implement a comprehensive notification service to handle announcements, subscriptions, and exam hosting notifications, including both in-app history and system (push) notifications.

## User Review Required

> [!IMPORTANT]
> This implementation assumes that the Firebase project is already set up and `google-services.json` is correctly placed. I will be adding the Firebase Messaging dependency.

## Proposed Changes

### 1. Build Configuration
- Add Firebase Messaging dependency to `app/build.gradle.kts`.

### 2. Models
- Create [Notification](file:///D:/Pandit Projects/Pandit.P/Pandit R&D/Host-Your-Exam/app/src/main/java/com/example/testmaster/model/Notification.kt) model.
- Update [personalDetail](file:///D:/Pandit Projects/Pandit.P/Pandit R&D/Host-Your-Exam/app/src/main/java/com/example/testmaster/model/personalDetail.kt) to include `fcmToken`.

### 3. Notification Logic & FCM
- Create [NotificationHelper](file:///D:/Pandit Projects/Pandit.P/Pandit R&D/Host-Your-Exam/app/src/main/java/com/example/testmaster/util/NotificationHelper.kt) to handle creating notification records in Firestore.
- Create [MyFirebaseMessagingService](file:///D:/Pandit Projects/Pandit.P/Pandit R&D/Host-Your-Exam/app/src/main/java/com/example/testmaster/service/MyFirebaseMessagingService.kt) to handle incoming push notifications and show system notifications.
- Update [MainActivity](file:///D:/Pandit Projects/Pandit.P/Pandit R&D/Host-Your-Exam/app/src/main/java/com/example/testmaster/activities/MainActivity.kt) to request notification permissions and update FCM tokens.

### 4. UI Components
- Update [activity_notification.xml](file:///D:/Pandit Projects/Pandit.P/Pandit R&D/Host-Your-Exam/app/src/main/res/layout/activity_notification.xml) to include a RecyclerView.
- Create [item_notification.xml](file:///D:/Pandit Projects/Pandit.P/Pandit R&D/Host-Your-Exam/app/src/main/res/layout/item_notification.xml).
- Create [NotificationAdapter](file:///D:/Pandit Projects/Pandit.P/Pandit R&D/Host-Your-Exam/app/src/main/java/com/example/testmaster/adapter/NotificationAdapter.kt).
- Update [NotificationActivity](file:///D:/Pandit Projects/Pandit.P/Pandit R&D/Host-Your-Exam/app/src/main/java/com/example/testmaster/activities/NotificationActivity.kt) to display the notification list.

### 5. Integration
- Update [UserSearchAdapter](file:///D:/Pandit Projects/Pandit.P/Pandit R&D/Host-Your-Exam/app/src/main/java/com/example/testmaster/adapter/UserSearchAdapter.kt) to trigger a notification when a user subscribes.
- Update [CreateAnnouncementActivity](file:///D:/Pandit Projects/Pandit.P/Pandit R&D/Host-Your-Exam/app/src/main/java/com/example/testmaster/activities/CreateAnnouncementActivity.kt) to notify subscribers.
- Update [CreateMcqTest](file:///D:/Pandit Projects/Pandit.P/Pandit R&D/Host-Your-Exam/app/src/main/java/com/example/testmaster/activities/CreateMcqTest.kt) to notify subscribers when an exam is hosted.

## Verification Plan

### Automated Tests
- Build the project to ensure dependencies are correctly added.

### Manual Verification
- **FCM Token**: Verify that `fcmToken` is updated in Firestore under `personalDetails`.
- **Subscription**: Subscribe to a user and check if that user receives a notification in their Notification list.
- **Announcement**: Post an announcement and check if subscribers receive a notification.
- **Exam Hosting**: Host an exam and check if subscribers receive a notification.
- **UI**: Verify the layout of the notification list in `NotificationActivity`.
