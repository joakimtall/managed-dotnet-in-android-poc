import org.gradle.api.GradleException
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

val dotnetLibPath = file("../../DotNetAndroidLib")
val dotnetPublishBaseDir = file("../../DotNetAndroidLib/bin/Release/net9.0")

val dotnetRoot: String = System.getenv("DOTNET_ROOT")?.takeIf { it.isNotBlank() } ?: run {
    val os = org.gradle.internal.os.OperatingSystem.current()
    when {
        os.isMacOsX -> "/usr/local/share/dotnet"
        os.isWindows -> System.getenv("ProgramFiles")?.let { "$it\\dotnet" }
            ?: throw GradleException("DOTNET_ROOT not set and ProgramFiles environment variable not available.")
        else -> System.getenv("HOME")?.let { "$it/.dotnet" }
            ?: throw GradleException("DOTNET_ROOT not set and HOME environment variable not available.")
    }
}

val dotnetCommand = "$dotnetRoot/dotnet"

val androidSdkRoot = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
    ?: throw GradleException("ANDROID_HOME or ANDROID_SDK_ROOT environment variable not set")

val ndkDir = file("$androidSdkRoot/ndk")
val ndkPath = if (ndkDir.exists()) {
    ndkDir.listFiles()?.maxByOrNull { it.name }
        ?: throw GradleException("No NDK versions found in $ndkDir. Install NDK through Android Studio SDK Manager.")
} else {
    throw GradleException("NDK directory not found at $ndkDir. Install NDK through Android Studio SDK Manager.")
}

val dotnetJniLibsDir = layout.buildDirectory.dir("generated/dotnet/jniLibs")

val abiTargets = mapOf(
    "arm64-v8a" to "linux-bionic-arm64"
)

android {
    namespace = "com.example.kotlindotnettestapp.dotnet"
    compileSdk = rootProject.extra["compileSdkVersion"] as Int

    defaultConfig {
        minSdk = rootProject.extra["minSdkVersion"] as Int
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters.addAll(abiTargets.keys)
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/java")
            jniLibs.srcDir(dotnetJniLibsDir)
        }
    }
}

val cleanDotNetLib = tasks.register<Exec>("cleanDotNetLib") {
    description = "Clean the .NET library before building"
    workingDir = dotnetLibPath
    commandLine(dotnetCommand, "clean", "DotNetAndroidLib.csproj", "-c", "Release")
    isIgnoreExitValue = true
}

val publishDotNetLibTasks = abiTargets.map { (abi, rid) ->
    tasks.register<Exec>("publishDotNetLib_${abi}") {
        description = "Publish .NET library with NativeAOT for $abi"
        dependsOn(cleanDotNetLib)
        workingDir = dotnetLibPath
        val ndkToolchain = "$ndkPath/toolchains/llvm/prebuilt/darwin-x86_64"
        environment("PATH", "$ndkToolchain/bin:${System.getenv("PATH")}")
        commandLine(
            dotnetCommand, "publish", "DotNetAndroidLib.csproj",
            "-c", "Release",
            "-r", rid,
            "/p:PublishAot=true",
            "/p:StripSymbols=false",
            "/p:CppCompilerAndLinker=clang"
        )
    }
}

val copyDotNetNativeLibsTasks = abiTargets.map { (abi, rid) ->
    tasks.register<Copy>("copyDotNetNativeLibs_${abi}") {
        description = "Copy NativeAOT .so files for $abi"
        dependsOn("publishDotNetLib_${abi}")
        from("${dotnetPublishBaseDir}/${rid}/publish") {
            include("*.so")
            rename { "lib$it" }
        }
        into(dotnetJniLibsDir.map { it.dir(abi) })
    }
}

val copyDotNetNativeLibs = tasks.register("copyDotNetNativeLibs") {
    description = "Copy all NativeAOT .so files to jniLibs"
    dependsOn(copyDotNetNativeLibsTasks)
}

val prepareDotNetDependencies = tasks.register("prepareDotNetDependencies") {
    description = "Prepare all .NET dependencies"
    dependsOn(copyDotNetNativeLibs)
}

tasks.named("preBuild") {
    dependsOn(prepareDotNetDependencies)
}

tasks.named("clean") {
    dependsOn(cleanDotNetLib)
}

tasks.matching { task ->
    task.name.startsWith("compile") && task.name.contains("Java")
}.configureEach {
    dependsOn(prepareDotNetDependencies)
}

dependencies {
    val kotlinVersion = rootProject.extra["kotlinVersion"] as String
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}
