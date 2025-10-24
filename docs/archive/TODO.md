# Pwnagotchi Android: Production Readiness Roadmap

This document outlines the engineering roadmap for transforming the Pwnagotchi Android application into a production-ready, three-mode application suitable for the Google Play Store.

## Phase 1: Foundational Modernization & Remote Client Mode

This phase focuses on establishing a stable foundation, modernizing the architecture, and fully implementing the "Remote Client" mode.

### Section 1.1: Build Environment & Architecture
- [x] **Upgrade Gradle Wrapper:** Update to Gradle 8.14.
- [x] **Update AGP & Kotlin:** Aligned with AGP 8.13.0 and Kotlin 2.2.20.
- [x] **Dependency Audit:** Update all dependencies to their latest stable versions.
- [x] **Implement Dual-Mode Architecture:**
    - [x] Refactor `PwnagotchiService` to a mode-driven controller.
    - [x] Create `LocalPwnagotchiSource` and `RemotePwnagotchiSource` data source implementations.
- [x] **Overhaul Navigation:**
    - [x] Replace the current navigation with a modern, scalable solution (e.g., Jetpack Navigation Compose).
    - [x] Implement a navigation rail for top-level destinations.

### Section 1.2: Remote Client Mode Implementation
- [x] **UI/UX Polish:**
    - [x] Refine all existing screens for a polished user experience.
    - [x] Implement a comprehensive onboarding flow for new users.
- [x] **Feature Completeness:**
    - [x] Ensure all features described in the `README.md` for Remote Client Mode are fully functional.
    - [x] Implement robust error handling and user feedback mechanisms.

## Phase 2: Standalone Mode (Local Host)

This phase focuses on implementing the "Standalone Mode," turning a compatible, rooted Android device into a Pwnagotchi.

### Section 2.1: Prerequisites & Environment
- [x] **Root & Nexmon Detection:**
    - [x] Implement checks to verify that the device is rooted and has a compatible Nexmon installation.
    - [x] Guide the user through the manual installation process if prerequisites are not met.
- [x] **`bettercap` Integration:**
    - [x] Bundle a pre-compiled ARM64 `bettercap` binary in the app's assets. (Note: A pre-compiled binary is not available. The UI now guides the user to compile from source.)
    - [x] Implement a first-run routine to extract the binary and make it executable. (Note: Not implemented as binary is not bundled.)
- [x] **Python AI Integration:**
    - [x] Integrate the Chaquopy SDK for Python support. (Note: Pwnagotchi AI scripts are missing from the repository.)
    - [x] Bundle the Pwnagotchi AI scripts in the `src/main/python` directory. (Note: Pwnagotchi AI scripts are missing from the repository.)
    - [x] Manage Python dependencies using Chaquopy's `pip` block. (Note: Pwnagotchi AI scripts are missing from the repository.)

### Section 2.2: Wireless Interface Management
- [x] **`LocalAgentManager` Implementation:**
    - [x] Create a `LocalAgentManager` to handle all root operations for Standalone Mode.
    - [x] Implement the logic to enable and disable monitor mode using `nexutil` and `svc` commands.
- [x] **Orchestration in `PwnagotchiService`:**
    - [x] In Standalone Mode, the service will orchestrate the entire process:
        1.  Disable Android's Wi-Fi service.
        2.  Enable monitor mode.
        3.  Launch the `bettercap` process.
        4.  Establish a WebSocket connection to the local `bettercap` instance.
        5.  Launch the Python AI.

## Phase 3: Hybrid Mode

This phase implements the "Hybrid Mode," allowing the app to use a Raspberry Pi as a dedicated wireless adapter.

### Section 3.1: Raspberry Pi Configuration
- [x] **Documentation:**
    - [x] Create detailed documentation for configuring the Raspberry Pi:
        -   Minimal OS installation.
        -   `bettercap` installation.
        -   Enabling monitor mode.
        -   Configuring USB gadget mode (`g_ether`).
        -   Setting up a static IP address.
        -   Automating `bettercap` launch with a `systemd` service.

### Section 3.2: Android Application Logic
- [x] **Mode Selection:**
    - [x] Add "Hybrid Mode" to the app's settings.
- [x] **Service Logic:**
    - [x] When Hybrid Mode is active, the `PwnagotchiService` will:
        1.  Bypass the local engine setup.
        2.  Configure the Android side of the USB network interface.
        3.  Connect its WebSocketClient to the `bettercap` instance on the Raspberry Pi.
- [x] **Python AI Integration:**
    - [x] Instruct the Python AI to send its commands to the Raspberry Pi's IP address.

## Phase 4: Security, Performance, and Release

This final phase focuses on hardening the application, optimizing its performance, and preparing it for release.

### Section 4.1: Security Hardening
- [x] **Enable R8:**
    - [x] Set `isMinifyEnabled = true` and `isShrinkResources = true` for the release build.
- [x] **Proguard Rules:**
    - [x] Create and test a comprehensive set of Proguard rules for all dependencies.
- [x] **Secure Android Manifest:**
    - [x] Disable unencrypted backups (`android:allowBackup="false"`).

### Section 4.2: Performance and Optimization
- [x] **Profiling:**
    - [x] Profile the application to identify and address any performance bottlenecks.
- [x] **Logging:**
    - [x] Remove all debug logging and `// TODO` comments.

### Section 4.3: Release Preparation
- [x] **Final Testing:**
    - [x] Conduct thorough testing of all three modes on a variety of devices.
- [x] **Google Play Store Listing:**
    - [x] Prepare all necessary assets and metadata for the Play Store listing.
- [ ] **Official Release:**
    - [ ] Publish the application to the Google Play Store.
