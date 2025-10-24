# Code Review Fixes

This document details the changes made to address the comments from the code review.

## Comment 1: `MainActivity.kt` Side Effect Issue

-   **Issue:** Repeatedly calling `setService` in recompositions may cause unintended side effects.
-   **Fix:** The `PwnagotchiViewModel` was moved to be an Activity-level property, and `setService` is now called from within the `onServiceConnected` callback. This ensures that the service is set only once when it connects.

## Comment 2: `runBlocking` in `StatusWidget.kt`

-   **Issue:** Using `runBlocking` in a Composable for initial state can block the UI thread.
-   **Fix:** The `runBlocking` calls were removed, and the initial state for the `face` and `message` flows is now an empty string.

## Comment 3: Potential Crash in `LeaderboardWidget.kt`

-   **Issue:** Potential crash if `leaderboardJson` is not a valid JSON string.
-   **Fix:** The `Json.decodeFromString` call is now wrapped in a `try-catch` block. If JSON decoding fails, an empty list is used for the leaderboard, preventing the widget from crashing.

## Comment 4: Missing Error Handling in `HandshakeLogWidget.kt`

-   **Issue:** Missing error handling for JSON decoding of handshakes.
-   **Fix:** Similar to the `LeaderboardWidget`, the `Json.decodeFromString` call is wrapped in a `try-catch` block, and an empty list is used as a fallback.

## Comment 5: Theme Selection Not Persisted in `SettingsScreen.kt`

-   **Issue:** Theme selection is not persisted or loaded from preferences.
-   **Fix:** The `SettingsScreen` now loads the saved theme from `SharedPreferences` on composition and saves the selected theme to `SharedPreferences` when the user clicks the save button.

## Comment 6: `stringResource` Usage in `MainScreen.kt`

-   **Issue:** Using `stringResource` for screen titles requires that all titles are valid string resource IDs.
-   **Fix:** The `Screen` sealed class was updated to include a string resource ID (`@StringRes val title: Int`) and an `ImageVector` for the icon. The `NavigationRailItem` now correctly uses `stringResource(screen.title)` to display the screen title. The necessary string resources were also added to `strings.xml`.