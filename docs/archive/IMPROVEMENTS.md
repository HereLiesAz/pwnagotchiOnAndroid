# Pwnagotchi Android: Improvement Roadmap

This document outlines the next steps for improving the Pwnagotchi Android application, focusing on enhancing stability, user experience, and feature completeness.

- [x] **Step 1: Simplify Reconnection Logic**
    - [x] Refactor `PwnagotchiService` to simplify the reconnection mechanism.
    - [x] Remove the `needsReconnect` flag and rely on a more streamlined approach.

- [x] **Step 2: Streamline Settings**
    - [x] Remove the redundant `ip_address` from `SharedPreferences` and the `onSaveSettings` function.
    - [x] The app should only use the `host` for WebSocket connections.

- [x] **Step 3: Add API Key Input for oPwngrid**
    - [x] Add a field to the `SettingsScreen` for users to enter their oPwngrid API key.
    - [x] Store the API key securely in `SharedPreferences`.
    - [x] Update `OpwngridClient` to use the stored API key.

- [x] **Step 4: Implement `OpwngridViewModel`**
    - [x] Create a new `OpwngridViewModel` to manage the state of the oPwngrid feature.
    - [x] The ViewModel should handle fetching the leaderboard, managing loading and error states, and exposing the data to the UI.

- [x] **Step 5: Add Loading Indicator to oPwngrid**
    - [x] Update the `OpwngridScreen` to display a loading indicator while the leaderboard is being fetched.
    - [x] The screen should also handle and display error states.
