# Implementation Plan - Search Optimization & Bug Fix

Optimize the search functionality with debouncing and fix the issue where results are not appearing for valid IDs.

## User Review Required

> [!IMPORTANT]
> The search will now wait for 500ms after the last character is typed before querying the database. This reduces unnecessary network calls and improves performance.
> I will also expand the search to support prefix matching for Exam IDs and add a fallback to search by UID directly if the query looks like a Firestore ID.

## Proposed Changes

### [Component: Search Optimization]

#### [MODIFY] [SearchExamId.kt](file:///D:/Pandit.P/Pandit%20R&D/Host-Your-Exam/app/src/main/java/com/example/testmaster/activities/SearchExamId.kt)
- Add a `Handler` and `Runnable` to implement 500ms debouncing in the `TextWatcher`.
- Update `searchExams` logic:
    - **Exam Search**: Change from `whereEqualTo("exam_id", query)` to prefix-based search using `whereGreaterThanOrEqualTo("exam_id", query)` and `whereLessThanOrEqualTo("exam_id", query + "\uf8ff")`. This will show exams as you type the ID.
    - **UID Search**: Add a check to see if the query matches a UID format (approx 28 characters). If so, try to fetch that specific user document directly.
    - **Synchronization**: Use a counter or flags to ensure that `updateAdapters()` is called only when all relevant queries (Exams and Users) have completed for the *latest* query string.

### [Component: UI Stability]

#### [MODIFY] [SearchExamId.kt](file:///D:/Pandit.P/Pandit%20R&D/Host-Your-Exam/app/src/main/java/com/example/testmaster/activities/SearchExamId.kt)
- Improve `updateAdapters()` to avoid unnecessary RecyclerView state resets if possible, although with `ConcatAdapter` and simple lists, recreation is often necessary unless using `ListAdapter` or `DiffUtil`. I will keep it simple but ensure it's called reliably.

## Verification Plan

### Manual Verification
1. **Debouncing**: Type quickly in the search bar and verify that the database is not queried until 500ms after you stop.
2. **Exam Prefix Search**: Type the first 3 digits of a known Exam ID and verify the exam appears in the list.
3. **User Search**: Type a known username (case-insensitive) and verify it appears.
4. **Valid ID Fix**: Paste a full Exam ID or Username and verify it consistently returns the correct result.
