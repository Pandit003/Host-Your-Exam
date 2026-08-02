# Walkthrough - Updated Blue Box Outline Drawable

I have updated the `blue_box_outline.xml` drawable to use a sky blue solid background and a darker blue stroke as requested.

## Changes

### 1. New Colors Defined
Added `sky_blue` and `dark_blue` to the color resources to ensure consistency and support for light/dark modes.

**[values/colors.xml](file:///D:/Host-Your-Exam/app/src/main/res/values/colors.xml)**:
- `sky_blue`: `#E1F5FE` (Light Blue 50)
- `dark_blue`: `#0288D1` (Light Blue 700)

**[values-night/colors.xml](file:///D:/Host-Your-Exam/app/src/main/res/values-night/colors.xml)**:
- `sky_blue`: `#01579B` (Darker variant for accessibility)
- `dark_blue`: `#0288D1` (Consistent stroke)

### 2. Updated Drawable
Updated **[blue_box_outline.xml](file:///D:/Host-Your-Exam/app/src/main/res/drawable/blue_box_outline.xml)** to use these new colors while maintaining the `2dp` stroke and `5dp` corners.

```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <stroke android:color="@color/dark_blue"
        android:width="2dp"/>
    <corners android:radius="5dp"/>
    <solid android:color="@color/sky_blue"/>
</shape>
```

## Verification

This drawable is used as a background for question options in:
- `FragmentQuestion`
- `FragmentAnalysis`

The new colors provide a clear, distinct look for highlighted options.
