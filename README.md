GuardianLink is a Kotlin Multiplatform (KMP) project targeting Android, iOS, and Server, designed using a shared UI and shared business logic architecture to minimize duplication, reduce platform-specific bugs, and simplify long-term maintenance. The project is structured to support production-grade deployment to both the Google Play Store and Apple App Store while keeping development efficient and scalable.

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./composeApp/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./composeApp/src/jvmMain/kotlin)
    folder is the appropriate location.

* [/iosApp](./iosApp/iosApp) contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/server](./server/src/main/kotlin) is for the Ktor server application.

* [/shared](./shared/src) is for the code that will be shared between all targets in the project.
  The most important subfolder is [commonMain](./shared/src/commonMain/kotlin). If preferred, you
  can add code to the platform-specific folders here too.

### Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE’s toolbar or build it directly from the terminal:

- on macOS/Linux
  ```shell
  ./gradlew :composeApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:assembleDebug
  ```

### Build and Run Server

To build and run the development version of the server, use the run configuration from the run widget
in your IDE’s toolbar or run it directly from the terminal:

- on macOS/Linux
  ```shell
  ./gradlew :server:run
  ```
- on Windows
  ```shell
  .\gradlew.bat :server:run
  ```

### Build and Run iOS Application

To build and run the development version of the iOS app, use the run configuration from the run widget
in your IDE’s toolbar or open the [/iosApp](./iosApp) directory in Xcode and run it from there.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…# GuardianLink

The `composeApp` module contains the shared UI built using Compose Multiplatform. All common UI components, screens, navigation, and presentation logic live in this module, enabling a single UI codebase to run across Android and iOS. Platform-specific UI behavior, permissions, or integrations can still be handled using conditional logic or platform-specific source sets such as `androidMain` and `iosMain`, while the majority of UI remains in `commonMain`.

The `shared` module holds the core business logic used by all platforms. This includes authentication and OTP workflows, SOS triggering logic, encryption, data models, offline-first storage strategies, and domain-level state management. The `commonMain` source set acts as the single source of truth, while platform-specific implementations are provided through `androidMain`, `iosMain`, and `jvmMain` using the expect/actual mechanism where required.

The `iosApp` directory contains the iOS application entry point required by Xcode. Even when using Compose Multiplatform for UI, this module is necessary to bootstrap the app, configure signing, manage app lifecycle, and integrate with SwiftUI or UIKit when platform-native features are needed. The iOS app consumes the shared UI and logic exposed by the KMP modules.

The `server` module is a Ktor-based backend service used for server-side operations such as push notification coordination, authentication support, or telemetry if required. It shares data models and logic from the `shared` module, ensuring consistency between client and server behavior.

To build and run the Android application, developers can use the IDE run configuration or execute `./gradlew :composeApp:assembleDebug` on macOS or Linux, or `.\gradlew.bat :composeApp:assembleDebug` on Windows. The server can be started using `./gradlew :server:run` (or the Windows equivalent). The iOS application is built and run directly from Xcode by opening the `iosApp` directory and selecting a simulator or physical device.

This architecture provides an optimized balance between developer productivity and user experience by sharing as much code as possible while still allowing platform-specific optimizations where absolutely necessary, making it well-suited for a safety-critical, cross-platform application like GuardianLink.

