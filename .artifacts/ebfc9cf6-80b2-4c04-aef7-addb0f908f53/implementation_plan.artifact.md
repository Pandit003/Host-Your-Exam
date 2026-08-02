# Implementation Plan - Modernize "About" Page

Update the `activity_about.xml` layout to match the professional **Emerald & Slate** design system. The new design will use Material Cards to group information and clean typography for better readability.

## Proposed Changes

### [MODIFY] [activity_about.xml](file:///D:/Host-Your-Exam/app/src/main/res/layout/activity_about.xml)

1.  **Toolbar Section**:
    *   Change background to `@color/primary` (White).
    *   Change text color to `@color/onPrimary` (Dark Slate).
    *   Add a back navigation icon tinted to `@color/onPrimary`.

2.  **Content Structure**:
    *   Change root background to `@color/background`.
    *   Group each section (Overview, Key Features, etc.) into a `MaterialCardView`.
    *   Use `app:cardCornerRadius="20dp"` and `app:cardElevation="0dp"` with a stroke (`app:strokeColor="@color/outline"`) for a clean, modern look.

3.  **Typography & Icons**:
    *   Section Titles: `@color/onSurface`, `textStyle="bold"`, `textSize="16sp"`.
    *   Body Text: `@color/onSurfaceVariant`, `textSize="14sp"`, `lineSpacingExtra="4sp"`.
    *   Bullets: Use a standard bullet icon (e.g., `baseline_circle_24`) tinted to `@color/primary` (Emerald theme) or `@color/onSurfaceVariant`.

4.  **Spacing**:
    *   Use standard `16dp` padding inside cards and `16dp` margins between cards.

## Verification Plan

### Manual Verification
- Deploy the app and navigate to the "About" page.
- Verify that the Toolbar is consistent with other screens (White background, Dark text).
- Verify that sections are neatly grouped in rounded cards.
- Check that the text is legible and well-spaced.
