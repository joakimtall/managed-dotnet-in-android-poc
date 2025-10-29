# .NET 9 NativeAOT and Kotlin Android Integration Example

This project demonstrates how to embed a .NET 9 library compiled with NativeAOT within a native Kotlin Android application. It showcases the ability to call C# methods directly from Kotlin through JNA (Java Native Access).

<img width="400" src="https://github.com/user-attachments/assets/ce468629-6931-4e6f-8d0a-4448c4cd6e3b" />

## Key Features

*   **NativeAOT Compilation**: The .NET library is compiled to native code using NativeAOT, eliminating the need for the Mono runtime and providing native performance.
*   **JNA Integration**: Uses Java Native Access (JNA) to call native C# methods directly from Kotlin.
*   **Gradle-Powered Build Process**: Gradle orchestrates the entire build, including NativeAOT compilation and native library packaging.
*   **3rd Party .NET Dependencies**: Demonstrated using Handlebars.NET template library, which is compiled into the native binary.

## Project Structure

The repository is organized as an Android project with integrated .NET components:

| Directory | Description |
| :--- | :--- |
| `app/` | The primary Android application module, written in Kotlin. It contains the UI and logic to interact with the .NET library via JNA. |
| `dotnet/` | Android library module that orchestrates the NativeAOT compilation of the .NET library and packages the resulting native `.so` files as JNI libraries. |
| `DotNetAndroidLib/` | A .NET 9 class library containing the C# business logic. Compiled to native code using NativeAOT with `PublishAot=true`. |

## How it Works

The integration is achieved through NativeAOT compilation and JNA. Here is a summary of the process:

1.  **NativeAOT Compilation**: Gradle invokes `dotnet publish` with `/p:PublishAot=true` on `DotNetAndroidLib`, targeting `linux-bionic-arm64` (Android ARM64).
2.  **Native Library Generation**: The .NET compiler produces native `.so` files containing the compiled C# code and all dependencies (including Handlebars.NET).
3.  **JNI Packaging**: The build script copies the native libraries to the Android `jniLibs` directory structure, renaming them with the `lib` prefix required by Android.
4.  **JNA Binding**: The Kotlin code uses JNA to load and call functions from the native library, with Kotlin wrappers (`DotNetWrapper.kt`) providing a clean API.
5.  **Zero Runtime Overhead**: No Mono runtime is required - the C# code runs as native ARM64 code directly on the Android device.

## Prerequisites

Before building this project, ensure you have the following installed:

*   **.NET 9 SDK** with NativeAOT support.
*   **Android SDK** (API Level 34 recommended) with NDK installed.
*   **Java Development Kit (JDK)** compatible with your Android Gradle Plugin version.
*   The build script expects the NDK toolchain at `$ANDROID_HOME/ndk/` (installed via Android Studio SDK Manager).

## Building and Running

1.  Clone the repository.
2.  Ensure your `DOTNET_ROOT` environment variable is set correctly, or that the `dotnet` executable is available in your system's PATH.
3.  Open the project root directory in Android Studio.
4.  Allow Gradle to sync the project. This will automatically trigger the initial .NET build process via the `dotnet` module's build script.
5.  Run the `app` configuration on an Android emulator or a physical device.
