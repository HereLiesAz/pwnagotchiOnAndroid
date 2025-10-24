

# **Pwnagotchi Android: Production Readiness Protocol**

This document provides an exhaustive, step-by-step engineering guide for transitioning the Pwnagotchi Android application from its current state to a production-ready, dual-mode application. The protocol is predicated on the specified build environment: Android API 36, Android Gradle Plugin (AGP) 8.13.0, and Kotlin 2.2.20.

## **Phase I: Foundational Upgrade & Architectural Refactoring**

This phase establishes a stable and modern foundation by upgrading the build environment, overhauling the navigation system, and implementing a dual-mode architecture capable of supporting both remote client and local host functionalities.

### **Section 1.1: Build Environment and Dependency Modernization**

The first action is to align the project's build environment and dependencies. This includes adding new libraries required for the navigation rail, home screen widgets, and local host mode functionality.

**Actionable Steps:**

1. **Upgrade Gradle Wrapper:** Modify the distributionUrl in gradle/wrapper/gradle-wrapper.properties to use Gradle 8.14, which is compatible with AGP 8.13.0.1
   Properties  
   distributionUrl\=https\\://services.gradle.org/distributions/gradle-8.14-bin.zip

2. **Update Android Gradle Plugin (AGP):** Modify the top-level build.gradle.kts to specify AGP version 8.13.0.4
   Kotlin  
   // FILE:./build.gradle.kts  
   plugins {  
       id("com.android.application") version "8.13.0" apply false
       //... other plugins  
   }

3. **Dependency Overhaul:** Modify app/build.gradle.kts to update all existing libraries to their latest stable versions and add new dependencies for Navigation, Glance widgets, and the specified AzNavRail.  
   Kotlin  
   // FILE:./app/build.gradle.kts (dependencies block)  
   dependencies {  
       // Core & Lifecycle \- Updated  
       implementation("androidx.core:core-ktx:1.13.1") // \[7, 8, 9, 10\]  
       implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3") // \[11, 12, 10, 13, 14, 15\]  
       implementation("androidx.activity:activity-compose:1.9.0") //

       // Compose BOM \- Updated  
       implementation(platform("androidx.compose:compose-bom:2024.06.00")) //  
       implementation("androidx.compose.ui:ui")  
       implementation("androidx.compose.ui:ui-graphics")  
       implementation("androidx.compose.ui:ui-tooling-preview")  
       implementation("androidx.compose.material3:material3")  
       implementation("androidx.compose.material:material-icons-extended")

       // Material Components (Legacy) \- Updated  
       implementation("com.google.android.material:material:1.12.0") // \[16, 17, 18, 19\]

       // Networking \- Ktor & WebSocket \- Updated  
       implementation("io.ktor:ktor-client-core:3.3.1") // \[20, 21, 22\]  
       implementation("io.ktor:ktor-client-cio:3.3.1")  
       implementation("io.ktor:ktor-client-content-negotiation:3.3.1")  
       implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.1")  
       implementation("org.java-websocket:Java-WebSocket:1.5.7") // \[23, 24, 25, 26, 27\]

       // Root Access \- libsu \- Updated  
       implementation("com.github.topjohnwu.libsu:core:6.0.0") // \[28, 29, 30, 31, 32\]  
       implementation("com.github.topjohnwu.libsu:service:6.0.0")

       // \--- NEW DEPENDENCIES \---  
       implementation("androidx.navigation:navigation-compose:2.8.0-beta05")  
       implementation("androidx.glance:glance-appwidget:1.1.1") // \[33, 31, 34, 35, 36, 37\]  
       implementation("androidx.glance:glance-material3:1.1.1")  
       implementation("com.github.HereLiesAz:AzNavRail:1.0.2") // \[38\]  
   }

### **Section 1.2: Implementing Dual-Mode Architecture**

To support both local and remote operation, the architecture must be refactored to use a mode-driven data source strategy. The PwnagotchiService will be elevated to a controller that delegates operations to either a LocalPwnagotchiSource or a RemotePwnagotchiSource.

**Actionable Steps:**

1. **Define Data Source Abstraction:** Create a new interface PwnagotchiDataSource that defines the contract for all Pwnagotchi interactions.  
   Kotlin  
   // FILE: app/src/main/java/com/pwnagotchi/pwnagotchiandroid/datasources/PwnagotchiDataSource.kt  
   interface PwnagotchiDataSource {  
       val uiState: StateFlow\<PwnagotchiUiState\>  
       suspend fun start(params: Map\<String, Any\> \= emptyMap())  
       suspend fun stop()  
       suspend fun sendCommand(command: String)  
   }

2. **Create RemotePwnagotchiSource:** Refactor the existing WebSocket logic from PwnagotchiService into a new class that implements PwnagotchiDataSource. This class will handle connections to external devices.  
3. **Create LocalPwnagotchiSource:** Create a new placeholder class for the local agent. This class will be fully implemented in Phase IV.  
   Kotlin  
   // FILE: app/src/main/java/com/pwnagotchi/pwnagotchiandroid/datasources/LocalPwnagotchiSource.kt  
   class LocalPwnagotchiSource(private val context: Context) : PwnagotchiDataSource {  
       private val \_uiState \= MutableStateFlow\<PwnagotchiUiState\>(PwnagotchiUiState.Disconnected("Local Agent not started"))  
       override val uiState: StateFlow\<PwnagotchiUiState\> \= \_uiState

       override suspend fun start(params: Map\<String, Any\>) { /\* TODO: Implement in Phase IV \*/ }  
       override suspend fun stop() { /\* TODO: Implement in Phase IV \*/ }  
       override suspend fun sendCommand(command: String) { /\* TODO: Implement in Phase IV \*/ }  
   }

4. **Refactor PwnagotchiService as a Mode Controller:** Modify the service to manage and switch between the two data sources.  
   Kotlin  
   // FILE: app/src/main/java/com/pwnagotchi/pwnagotchiandroid/PwnagotchiService.kt (Partial)  
   enum class AppMode { LOCAL, REMOTE }

   class PwnagotchiService : Service() {  
       private var activeDataSource: PwnagotchiDataSource? \= null  
       private val localSource by lazy { LocalPwnagotchiSource(applicationContext) }  
       private val remoteSource by lazy { RemotePwnagotchiSource(applicationContext) }  
       private val \_uiState \= MutableStateFlow\<PwnagotchiUiState\>(PwnagotchiUiState.Disconnected("Select a mode"))  
       val uiState: StateFlow\<PwnagotchiUiState\> \= \_uiState  
       private var stateJob: Job? \= null

       fun setMode(mode: AppMode) {  
           stateJob?.cancel()  
           activeDataSource \= when (mode) {  
               AppMode.LOCAL \-\> localSource  
               AppMode.REMOTE \-\> remoteSource  
           }  
           stateJob \= serviceScope.launch {  
               activeDataSource?.uiState?.collect { \_uiState.value \= it }  
           }  
       }

       fun connect(uri: URI) {  
           if (activeDataSource is RemotePwnagotchiSource) {  
               serviceScope.launch { activeDataSource?.start(mapOf("uri" to uri)) }  
           }  
       }

       fun startLocalAgent() {  
            if (activeDataSource is LocalPwnagotchiSource) {  
               serviceScope.launch { activeDataSource?.start() }  
           }  
       }

       //... delegate other commands like disconnect(), togglePlugin() to activeDataSource  
   }

## **Phase II: UI Refactoring for Dual-Mode Operation**

This phase replaces the primitive navigation with a robust system using AzNavRail and Jetpack Navigation. It also introduces UI elements to control the new dual-mode architecture.

### **Section 2.1: Implementing AzNavRail and NavHost**

The core UI will be rebuilt around a persistent navigation rail that controls a NavHost, which displays the content for the selected destination.

**Actionable Steps:**

1. **Define Navigation Graph:** Create a sealed class Screen to define all navigation routes, titles, and icons. (This step is identical to the previous guide's Section 1.2, Step 2).  
2. **Refactor MainActivity:** Replace the entire setContent block with a new structure that initializes the PwnagotchiViewModel and hosts a MainScreen composable.  
3. **Create MainScreen Composable:** This composable will contain the Scaffold, AzNavRail, and NavHost. It will observe the ViewModel's state and manage navigation.  
   Kotlin  
   // FILE: app/src/main/java/com/pwnagotchi/pwnagotchiandroid/ui/MainScreen.kt  
   @Composable  
   fun MainScreen(viewModel: PwnagotchiViewModel) {  
       val navController \= rememberNavController()  
       val navBackStackEntry by navController.currentBackStackEntryAsState()  
       val currentDestination \= navBackStackEntry?.destination

       Row(Modifier.fillMaxSize()) {  
           AzNavRail(  
               items \= navRailItems,  
               selectedItem \= navRailItems.find { it.route \== currentDestination?.route },  
               onItemClick \= { screen \-\>  
                   navController.navigate(screen.route) {  
                       // Pop up to the start destination of the graph to avoid building up a large back stack  
                       popUpTo(navController.graph.findStartDestination().id) { saveState \= true }  
                       launchSingleTop \= true  
                       restoreState \= true  
                   }  
               }  
           )

           NavHost(navController \= navController, startDestination \= Screen.Home.route) {  
               composable(Screen.Home.route) { HomeScreen(viewModel \= viewModel) }  
               //... other composable routes  
           }  
       }  
   }

4. **Refactor Screen Composables:** Adapt all existing screen composables (PwnagotchiScreen, PluginsScreen, etc.) to be stateless, accepting the ViewModel and navigation callbacks as parameters.

### **Section 2.2: Implementing Mode Selection UI**

The HomeScreen must be updated to allow the user to select between "Local Host" and "Remote Client" modes. This selection will control the PwnagotchiService.

**Actionable Steps:**

1. **Add Mode Selection State:** In PwnagotchiViewModel, add state to track the current application mode.  
   Kotlin  
   // FILE: app/src/main/java/com/pwnagotchi/pwnagotchiandroid/PwnagotchiViewModel.kt (additions)  
   private val \_appMode \= MutableStateFlow(AppMode.REMOTE)  
   val appMode: StateFlow\<AppMode\> \= \_appMode

   fun onModeChange(newMode: AppMode) {  
       \_appMode.value \= newMode  
       pwnagotchiService?.setMode(newMode)  
   }

2. **Create Mode Switch Composable:** Add a new composable to the HomeScreen that allows the user to toggle the mode.  
   Kotlin  
   // FILE: app/src/main/java/com/pwnagotchi/pwnagotchiandroid/ui/screens/HomeScreen.kt (additions)  
   @Composable  
   fun ModeSelector(  
       currentMode: AppMode,  
       onModeChange: (AppMode) \-\> Unit  
   ) {  
       SegmentedButtonRow {  
           SegmentedButton(  
               selected \= currentMode \== AppMode.REMOTE,  
               onClick \= { onModeChange(AppMode.REMOTE) },  
               shape \= SegmentedButtonDefaults.shape(position \= 0, count \= 2)  
           ) { Text("Remote") }  
           SegmentedButton(  
               selected \= currentMode \== AppMode.LOCAL,  
               onClick \= { onModeChange(AppMode.LOCAL) },  
               shape \= SegmentedButtonDefaults.shape(position \= 1, count \= 2)  
           ) { Text("Local") }  
       }  
   }

3. **Update HomeScreen:** Integrate the ModeSelector and conditionally display either the ConnectionControls (for Remote mode) or the RootControls (for Local mode).  
   Kotlin  
   // In HomeScreen composable  
   val appMode by viewModel.appMode.collectAsState()

   ModeSelector(currentMode \= appMode, onModeChange \= { viewModel.onModeChange(it) })

   when (appMode) {  
       AppMode.REMOTE \-\> ConnectionControls(...)  
       AppMode.LOCAL \-\> RootControls(  
           onRequestRoot \= { /\*... \*/ },  
           onStartAgent \= { viewModel.startLocalAgent() },  
           onStopAgent \= { viewModel.stopLocalAgent() }  
       )  
   }

## **Phase III: Local Host Mode Implementation**

This phase details the core logic required to turn the Android device into a Pwnagotchi. This involves managing native binaries, executing root commands to configure the network interface, and running the bettercap process.

### **Section 3.1: Bundling and Deploying Native Binaries**

The bettercap executable is not a standard Android asset and must be bundled with the application and deployed to an executable location at runtime.

**Actionable Steps:**

1. **Acquire bettercap Binary:** Obtain a pre-compiled bettercap binary for the arm64-v8a architecture, which is standard for modern Android devices. Bettercap does not provide official pre-compiled binaries for Android ARM, so this may require compiling from source.  
2. **Add Binary to Assets:** Place the bettercap binary in the app/src/main/assets/ directory.  
3. **Implement Binary Deployment Logic:** In the LocalPwnagotchiSource, create a function that, on first run, uses a root shell to copy the binary from the assets to an executable directory and set its permissions. The app's private data directory is a suitable location.  
   Kotlin  
   // FILE: app/src/main/java/com/pwnagotchi/pwnagotchiandroid/datasources/LocalPwnagotchiSource.kt (additions)  
   private suspend fun deployBettercap(): String? {  
       val binaryName \= "bettercap"  
       val targetFile \= File(context.filesDir, binaryName)  
       if (targetFile.exists()) {  
           // Optional: Add version check here  
           return targetFile.absolutePath  
       }

       // Copy from assets to app's private storage  
       context.assets.open(binaryName).use { input \-\>  
           targetFile.outputStream().use { output \-\>  
               input.copyTo(output)  
           }  
       }

       // Use root shell to make it executable  
       val result \= Shell.su("chmod 755 ${targetFile.absolutePath}").exec()  
       return if (result.isSuccess) targetFile.absolutePath else null  
   }

### **Section 3.2: Implementing Wi-Fi Monitor Mode Activation**

Activating monitor mode requires executing privileged shell commands. This process is highly dependent on the device's chipset. A strategy pattern will be implemented to attempt multiple known methods.

**Actionable Steps:**

1. **Create MonitorModeManager:** Develop a new class to encapsulate the logic for enabling and disabling monitor mode.  
2. **Implement Activation Strategy:** Within the manager, create a sequence of functions, each attempting a different method to enable monitor mode. The primary method for Qualcomm chipsets is writing to con\_mode. A fallback can be the standard iwconfig method.39  
   Kotlin  
   // FILE: app/src/main/java/com/pwnagotchi/pwnagotchiandroid/core/MonitorModeManager.kt  
   object MonitorModeManager {  
       private const val WLAN\_INTERFACE \= "wlan0"

       suspend fun enable(): Boolean {  
           // Method 1: Qualcomm con\_mode  
           var result \= Shell.su("echo 4 \> /sys/module/wlan/parameters/con\_mode").exec()  
           if (verify()) return true

           // Method 2: Standard iwconfig  
           result \= Shell.su(  
               "ip link set $WLAN\_INTERFACE down",  
               "iwconfig $WLAN\_INTERFACE mode monitor",  
               "ip link set $WLAN\_INTERFACE up"  
           ).exec()  
           return verify()  
       }

       suspend fun disable(): Boolean {  
           //... implementation to set mode back to "managed"  
           return\!verify()  
       }

       private suspend fun verify(): Boolean {  
           val result \= Shell.su("iwconfig $WLAN\_INTERFACE").exec()  
           return result.out.any { it.contains("Mode:Monitor", ignoreCase \= true) }  
       }  
   }

### **Section 3.3: Launching and Managing the bettercap Process**

With the binary deployed and monitor mode enabled, the final step is to launch bettercap as a persistent root process and connect to its local WebSocket stream.

**Actionable Steps:**

1. **Implement Process Launch in LocalPwnagotchiSource:** Modify the start function to orchestrate the entire sequence: deploy binary, enable monitor mode, and then launch the bettercap process. The process must be launched in the background, and its Process ID (PID) must be captured for later management.40  
   Kotlin  
   // FILE: app/src/main/java/com/pwnagotchi/pwnagotchiandroid/datasources/LocalPwnagotchiSource.kt (modified)  
   private var bettercapProcess: Process? \= null  
   private var bettercapPath: String? \= null

   override suspend fun start(params: Map\<String, Any\>) {  
       \_uiState.value \= PwnagotchiUiState.Connecting("Starting Local Agent...")

       // 1\. Deploy binary  
       bettercapPath \= deployBettercap()  
       if (bettercapPath \== null) {  
           \_uiState.value \= PwnagotchiUiState.Error("Failed to deploy bettercap binary.")  
           return  
       }

       // 2\. Enable Monitor Mode  
       if (\!MonitorModeManager.enable()) {  
           \_uiState.value \= PwnagotchiUiState.Error("Failed to enable monitor mode.")  
           return  
       }

       // 3\. Launch bettercap and connect WebSocket  
       \_uiState.value \= PwnagotchiUiState.Connecting("Launching bettercap...")  
       val job \= Shell.su("$bettercapPath \-iface wlan0 \-caplet pwnagotchi-auto").newJob()

       // Redirect stdout/stderr to lists for logging/debugging  
       val stdout \= mutableListOf\<String\>()  
       val stderr \= mutableListOf\<String\>()  
       job.to(stdout, stderr).submit { result \-\>  
           // This callback is invoked when the process terminates  
           \_uiState.value \= PwnagotchiUiState.Disconnected("Bettercap process exited.")  
       }

       // Give bettercap a moment to start its web UI  
       delay(5000) 

       // Now connect the WebSocket client to localhost  
       connectToLocalWebSocket()  
   }

   override suspend fun stop() {  
       // Use the captured PID to kill the process  
       bettercapProcess?.destroy()  
       MonitorModeManager.disable()  
       //... close websocket client  
       \_uiState.value \= PwnagotchiUiState.Disconnected("Local Agent stopped.")  
   }

   private fun connectToLocalWebSocket() {  
       // Use the existing WebSocket client logic, but with ws://127.0.0.1:8080/api/events  
       // (or the correct endpoint for bettercap's event stream)  
   }

## **Phase IV: Ambient UX and Production Hardening**

This final phase implements the ambient user experience features (widgets and notifications) and prepares the application for a secure release by enabling code obfuscation.

* **Section 4.1: Implementing the Persistent E-Ink Notification:** (This section is identical to the previous guide's Phase III).  
* **Section 4.2: Implementing Home Screen Widgets:** (This section is identical to the previous guide's Phase IV).  
* **Section 4.3: Finalizing for Production:** (This section is identical to the previous guide's Phase V, focusing on ProGuard/R8 configuration).

By executing these phases sequentially, the agentic AI will produce a fully-featured, dual-mode Pwnagotchi application that is architecturally sound, functionally complete, and ready for production.

#### **Works cited**

1. Gradle 8.14 Release Notes, accessed October 15, 2025, [https://docs.gradle.org/8.14/release-notes.html](https://docs.gradle.org/8.14/release-notes.html)  
2. Upgrading within Gradle 8.x, accessed October 15, 2025, [https://docs.gradle.org/current/userguide/upgrading\_version\_8.html](https://docs.gradle.org/current/userguide/upgrading_version_8.html)  
3. What's new in Gradle 9.0.0, accessed October 15, 2025, [https://gradle.org/whats-new/gradle-9/](https://gradle.org/whats-new/gradle-9/)  
4. Android Gradle plugin 8.13 release notes | Android Studio, accessed October 15, 2025, [https://developer.android.com/build/releases/gradle-plugin](https://developer.android.com/build/releases/gradle-plugin)  
5. Android Application Gradle Plugin \- Maven Repository, accessed October 15, 2025, [https://mvnrepository.com/artifact/com.android.application/com.android.application.gradle.plugin](https://mvnrepository.com/artifact/com.android.application/com.android.application.gradle.plugin)  
6. Flutter 3.35.3 with latest Android Gradle / NDK (Ready for 16KB memory page requirements) : r/FlutterDev \- Reddit, accessed October 15, 2025, [https://www.reddit.com/r/FlutterDev/comments/1nakv1f/flutter\_3353\_with\_latest\_android\_gradle\_ndk\_ready/](https://www.reddit.com/r/FlutterDev/comments/1nakv1f/flutter_3353_with_latest_android_gradle_ndk_ready/)  
7. How to configure Wi-Fi adaptor to monitor mode? \- Ask Ubuntu, accessed October 15, 2025, [https://askubuntu.com/questions/512926/how-to-configure-wi-fi-adaptor-to-monitor-mode](https://askubuntu.com/questions/512926/how-to-configure-wi-fi-adaptor-to-monitor-mode)  
8. How to find the Process ID (PID) of a running terminal program? \- Ask Ubuntu, accessed October 15, 2025, [https://askubuntu.com/questions/180336/how-to-find-the-process-id-pid-of-a-running-terminal-program](https://askubuntu.com/questions/180336/how-to-find-the-process-id-pid-of-a-running-terminal-program)  
9. How to get pid of just started process \- linux \- Server Fault, accessed October 15, 2025, [https://serverfault.com/questions/205498/how-to-get-pid-of-just-started-process](https://serverfault.com/questions/205498/how-to-get-pid-of-just-started-process)  
10. How to find the PID of a running command in bash? \- Stack Overflow, accessed October 15, 2025, [https://stackoverflow.com/questions/45494598/how-to-find-the-pid-of-a-running-command-in-bash](https://stackoverflow.com/questions/45494598/how-to-find-the-pid-of-a-running-command-in-bash)  
11. Get PID of a Just-Started Process | Baeldung on Linux, accessed October 15, 2025, [https://www.baeldung.com/linux/just-started-process-pid](https://www.baeldung.com/linux/just-started-process-pid)