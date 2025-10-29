# Plan: Integrate .NET 9 Android Library with Kotlin Test App

## Overview
Create a new Kotlin Android app that consumes the C# services from our .NET Android library, similar to the original `KotlinAppWithXamarinDependency` project.

## Architecture
```
DotNetAndroidDependency/
├── DotNetAndroidLib/           # C# library (already created)
├── DotNetAndroidApp/           # .NET app to package assemblies (already created)
└── KotlinTestApp/              # NEW: Kotlin app consuming C# services
    ├── app/                    # Main Kotlin application
    │   ├── build.gradle        # References dotnet module
    │   └── src/main/
    │       ├── java/
    │       │   └── MainActivity.kt  # Uses C# services
    │       └── res/
    │           └── layout/activity_main.xml
    └── dotnet/                 # NEW: Module with .NET assemblies & runtime
        ├── build.gradle        # Extracts from .NET build, provides to Kotlin
        └── src/
            ├── main/
            │   ├── java/       # Generated ACW Java classes
            │   ├── jniLibs/    # .NET runtime (.so files)
            │   └── assets/     # .NET assemblies (DLLs)
```

## Implementation Steps

### 1. Create Kotlin Test App Project Structure
- Create new Android project: `KotlinTestApp`
- Set up multi-module Gradle project with `app` and `dotnet` modules
- Configure project-level build.gradle:
  - Kotlin 2.x
  - Android Gradle Plugin 8.x
  - compileSdk 34, minSdk 21

### 2. Create `dotnet` Gradle Module
This module packages .NET assemblies for consumption by Kotlin:

**Build Tasks:**
- `copyDotNetBuild`: Copy files from `DotNetAndroidApp/obj/Release/net9.0-android/android/`:
  - `src/com/roydammarell/dotnetandroid/*.java` → `src/main/java/`
  - `bin/mono.android.jar` → compile dependency

- `extractDotNetRuntime`: Extract from APK:
  - `lib/arm64-v8a/*.so` → `src/main/jniLibs/arm64-v8a/`
  - `lib/x86_64/*.so` → `src/main/jniLibs/x86_64/`

- `extractDotNetAssemblies`: Extract assembly blobs:
  - `libassemblies.*.blob.so` → `src/main/jniLibs/`

**build.gradle configuration:**
- Apply: `com.android.library`, `kotlin-android`
- Dependencies: `mono.android.jar` (from .NET build)
- sourceSets: Point to extracted Java/native code

### 3. Create Kotlin Interfaces Module (Optional)
Create simple Kotlin interfaces that C# classes will implement:
```kotlin
interface IHelloService {
    fun createHello(): String
}

interface IExceptionService {
    fun throwNullReferenceException()
}
```

### 4. Update C# Services to Implement Kotlin Interfaces
Modify `HelloAndroidService.cs` and `ExceptionAndroidService.cs`:
- Keep `[Register]` attributes for ACW generation
- Ensure method signatures match Kotlin interface expectations
- Methods are `public virtual` to allow override

### 5. Create Kotlin Factory/Wrapper
Create `ServiceFactory.kt` in app module:
```kotlin
object ServiceFactory {
    fun createHelloService(): IHelloService {
        return HelloAndroidService()
    }

    fun createExceptionService(): IExceptionService {
        return ExceptionAndroidService()
    }
}
```

### 6. Build Kotlin App with UI
**MainActivity.kt:**
- Display hello message from C# service
- Button to trigger C# exception (for testing stacktraces)

**activity_main.xml:**
- TextView for hello message
- Button to test exception handling

**app/build.gradle:**
- Dependency: `implementation project(':dotnet')`
- `androidResources.noCompress = ['dll', '.blob']` (for assemblies)

### 7. Configure Mono Runtime Initialization
Ensure .NET runtime initializes before C# classes are used:
- May need custom Application class
- Or lazy initialization in factory

### 8. Build & Test
- Build entire project: `./gradlew assembleDebug`
- Verify APK contains:
  - Mono runtime libraries (.so files)
  - .NET assemblies (blob files)
  - Generated ACW classes
  - Kotlin app code
- Test on device/emulator:
  - Hello message displays
  - Exception button triggers C# exception with full stacktrace

## Key Differences from Original Xamarin Project
1. **No MSBuild**: Gradle extracts from .NET build output instead of calling MSBuild
2. **Package names**: `com.roydammarell.dotnetandroid` vs `com.roydammarell.xamarindependency`
3. **.NET 9 runtime**: Modern runtime, smaller, faster than old Mono/Xamarin
4. **Simpler**: No bindings project needed, direct ACW generation

## Success Criteria
✅ Kotlin app builds successfully
✅ C# services instantiate without errors
✅ Hello message displays from C# code
✅ C# exception properly caught with full stacktrace
✅ APK size reasonable (<10MB for basic app)
