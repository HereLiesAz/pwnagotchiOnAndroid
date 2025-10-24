# Pwnagotchi Android: Widget Improvement Roadmap

This document outlines the steps to refactor the existing home screen widget to use Jetpack Glance and integrate it with a real-time data source.

- [x] **Step 1: Add Jetpack Glance Dependencies**
    - [x] Add the necessary Jetpack Glance dependencies to the `app/build.gradle.kts` file.

- [x] **Step 2: Implement Preferences DataStore**
    - [x] Create a `WidgetStateRepository` to act as the single source of truth for widget data.
    - [x] Implement a Preferences DataStore to persist the widget state (e.g., status, face).

- [x] **Step 3: Update `PwnagotchiService`**
    - [x] Modify `PwnagotchiService` to write UI state updates to the new `WidgetStateRepository` and DataStore.

- [x] **Step 4: Refactor the Widget with Jetpack Glance**
    - [x] Create a new `PwnagotchiGlanceWidget` that uses the Glance API to define the UI.
    - [x] Create a `PwnagotchiGlanceWidgetReceiver` to host the Glance widget.
    - [x] The widget will read its state from the `WidgetStateRepository`.

- [x] **Step 5: Clean Up Old Widget Implementation**
    - [x] Delete the old `pwnagotchi_widget.xml` layout file.
    - [x] Remove the now-unused `PwnagotchiWidget` class.
    - [x] Update `AndroidManifest.xml` to point to the new `PwnagotchiGlanceWidgetReceiver`.
