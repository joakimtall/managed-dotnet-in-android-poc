# .NET 9 and Kotlin Android Integration Example

This project demonstrates how to embed a .NET 9 library within a native Kotlin Android application. It showcases the ability to call .NET code directly from Kotlin and handle exceptions that cross the boundary between the .NET managed runtime and the Android runtime.

It has been more or less stolen from https://github.com/royd/KotlinAppWithXamarinDependency, stripped to minimum, and updated for newer toolchain

This usecase is not officially supported, but we get around that by building an APK which is, and then stealing the necessary files from there to embed it into an existing standard Kotlin Android application.

<img width="400" alt="image" src="https://github.com/user-attachments/assets/20a024b9-d42b-4cc3-beb4-1f94e90f9393" />

## Key Features

*   **.NET 9 Library Integration**: A .NET 9 Android class library (`DotNetAndroidLib`) is consumed by a standard Kotlin-based Android app.
*   **Gradle-Powered Build Process**: The project uses Gradle to orchestrate the entire build. A dedicated Gradle module (`dotnet`) calls the .NET compiler, extracts the necessary artifacts, and packages them for the main Android application module.
*   **Seamless Interoperability**: Call C# methods from Kotlin as if they were native Java/Kotlin methods.
*   **3rd Party .NET Dependencies**: Demonstrated using Handlebars template to render text.

## Project Structure

The repository is organized into three main components:

| Directory | Description |
| :--- | :--- |
| `DotNetAndroidLib/` | A .NET 9 Android Class Library containing the C# business logic and HelloService implementation. |
| `DotNetAndroidApp/` | A minimal .NET 9 Android App project. This project is used by the Gradle build process to generate the necessary Android Callable Wrappers, Java bindings (`.jar`), and native shared libraries (`.so`) required for integration. |
| `KotlinTestApp/` | The main Android application project. |
| ┣ `app/` | The primary Android application module, written in Kotlin. It contains the UI and logic to interact with the .NET library. |
| ┗ `dotnet/` | A special-purpose Android library module. Its `build.gradle.kts` script is responsible for building the .NET projects and preparing their outputs to be consumed by the `app` module. |

## How it Works

The integration is achieved through a clever Gradle build script located in `KotlinTestApp/dotnet/build.gradle.kts`. Here is a summary of the process:

1.  **Build .NET**: Gradle invokes a `dotnet build` command on the `DotNetAndroidApp` project.
2.  **Extract Artifacts**: The script then locates and extracts key build artifacts, including:
    *   `mono.android.jar`: The core Mono runtime bindings for Android.
    *   `classes.zip`: The generated Java wrappers for the C# code (renamed to `mono-classes.jar`).
    *   Native Libraries (`.so`): The compiled native libraries for different Android architectures, which are extracted from the intermediate APK.
3.  **Provide Dependencies**: These extracted files are exposed as standard Android library dependencies.
4.  **Consume in App**: The main `app` module includes the `dotnet` module as a dependency (`implementation(project(":dotnet"))`), allowing it to access and instantiate the C# classes (`HelloAndroidService`) directly in Kotlin.

## Prerequisites

Before building this project, ensure you have the following installed:

*   **.NET 9 SDK** with the `net9.0-android` workload.
*   **Android SDK** (API Level 34 recommended).
*   **Java Development Kit (JDK)** compatible with your Android Gradle Plugin version.

## Building and Running

1.  Clone the repository.
2.  Ensure your `DOTNET_ROOT` environment variable is set correctly, or that the `dotnet` executable is available in your system's PATH.
3.  Open the `KotlinTestApp` directory in Android Studio.
4.  Allow Gradle to sync the project. This will automatically trigger the initial .NET build process via the `dotnet` module's build script.
5.  Run the `app` configuration on an Android emulator or a physical device.
