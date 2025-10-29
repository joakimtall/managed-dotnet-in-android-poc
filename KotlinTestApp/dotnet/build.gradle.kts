import org.gradle.api.GradleException
import org.gradle.api.file.RelativePath
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

val dotnetAppPath = file("../../DotNetAndroidApp")
val dotnetBuildPath = file("../../DotNetAndroidApp/obj/Release/net9.0-android")
val dotnetApkPath = file("../../DotNetAndroidApp/bin/Release/net9.0-android/com.companyname.DotNetAndroidApp-Signed.apk")

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

val javaRuntimeJarCandidates = fileTree("$dotnetRoot/packs") {
    include("**/java_runtime_net6.jar")
}.files

if (javaRuntimeJarCandidates.isEmpty()) {
    throw GradleException("Unable to locate java_runtime_net6.jar under $dotnetRoot/packs. Ensure .NET Android workload is installed.")
}

val javaRuntimeJar = javaRuntimeJarCandidates.maxByOrNull { it.lastModified() }
    ?: throw GradleException("Unable to evaluate java_runtime_net6.jar candidates under $dotnetRoot/packs.")

val cryptoRuntimeJarCandidates = fileTree("$dotnetRoot/packs") {
    include("Microsoft.NETCore.App.Runtime.Mono.android-*/**/libSystem.Security.Cryptography.Native.Android.jar")
}.files

if (cryptoRuntimeJarCandidates.isEmpty()) {
    throw GradleException("Unable to locate libSystem.Security.Cryptography.Native.Android.jar under $dotnetRoot/packs. Ensure .NET Android workload is installed.")
}

val cryptoRuntimeJar = cryptoRuntimeJarCandidates.maxByOrNull { it.lastModified() }
    ?: throw GradleException("Unable to evaluate crypto runtime jar candidates under $dotnetRoot/packs.")

val dotnetLibsDir = layout.buildDirectory.dir("generated/dotnet/libs")
val dotnetJniLibsDir = layout.buildDirectory.dir("generated/dotnet/jniLibs")
val monoAndroidJarFile = dotnetLibsDir.map { it.file("mono.android.jar") }
val monoClassesJarFile = dotnetLibsDir.map { it.file("mono-classes.jar") }
val javaRuntimeJarFile = dotnetLibsDir.map { it.file("java_runtime_net6.jar") }
val cryptoRuntimeJarFile = dotnetLibsDir.map { it.file("libSystem.Security.Cryptography.Native.Android.jar") }

android {
    namespace = "com.example.kotlindotnettestapp.dotnet"
    compileSdk = rootProject.extra["compileSdkVersion"] as Int

    defaultConfig {
        minSdk = rootProject.extra["minSdkVersion"] as Int
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

val cleanDotNetApp = tasks.register<Exec>("cleanDotNetApp") {
    description = "Clean the .NET Android app before building"
    workingDir = dotnetAppPath
    commandLine("dotnet", "clean", "DotNetAndroidApp.csproj", "-c", "Release")
}

val buildDotNetApp = tasks.register<Exec>("buildDotNetApp") {
    description = "Build the .NET Android app to generate bindings and native artifacts"
    dependsOn(cleanDotNetApp)
    workingDir = dotnetAppPath
    commandLine("dotnet", "build", "DotNetAndroidApp.csproj", "-c", "Release")
}

val copyMonoAndroidJar = tasks.register<Copy>("copyMonoAndroidJar") {
    description = "Copy mono.android.jar from .NET build"
    dependsOn(buildDotNetApp)
    from("${dotnetBuildPath}/android/bin/mono.android.jar")
    into(dotnetLibsDir)
}

val copyDotNetClassesJar = tasks.register<Copy>("copyDotNetClassesJar") {
    description = "Copy compiled Java classes from .NET build"
    dependsOn(buildDotNetApp)
    from("${dotnetBuildPath}/android/bin/classes.zip")
    into(dotnetLibsDir)
    rename { "mono-classes.jar" }
}

val copyJavaRuntimeJar = tasks.register<Copy>("copyJavaRuntimeJar") {
    description = "Copy Java runtime stub classes required by Mono runtime"
    from(javaRuntimeJar)
    into(dotnetLibsDir)
    rename { "java_runtime_net6.jar" }
}

val copyCryptoRuntimeJar = tasks.register<Copy>("copyCryptoRuntimeJar") {
    description = "Copy crypto helper classes required by Mono runtime"
    from(cryptoRuntimeJar)
    into(dotnetLibsDir)
    rename { "libSystem.Security.Cryptography.Native.Android.jar" }
}

val extractDotNetNativeLibs = tasks.register<Copy>("extractDotNetNativeLibs") {
    description = "Extract native libraries from .NET APK"
    dependsOn(copyMonoAndroidJar)

    from(zipTree(dotnetApkPath)) {
        include("lib/**/*.so")
        eachFile {
            val segments = relativePath.segments
            if (segments.isNotEmpty()) {
                relativePath = RelativePath(true, *segments.drop(1).toTypedArray())
            }
        }
    }
    into(dotnetJniLibsDir)
    includeEmptyDirs = false
}

val prepareDotNetDependencies = tasks.register("prepareDotNetDependencies") {
    description = "Prepare all .NET dependencies"
    dependsOn(
        copyMonoAndroidJar,
        copyDotNetClassesJar,
        copyJavaRuntimeJar,
        copyCryptoRuntimeJar,
        extractDotNetNativeLibs
    )
}

tasks.named("preBuild") {
    dependsOn(prepareDotNetDependencies)
}

tasks.matching { task ->
    task.name.startsWith("compile") && task.name.contains("Java")
}.configureEach {
    dependsOn(prepareDotNetDependencies)
}

dependencies {
    val kotlinVersion = rootProject.extra["kotlinVersion"] as String

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    api(files(monoAndroidJarFile).builtBy(copyMonoAndroidJar))
    api(files(monoClassesJarFile).builtBy(copyDotNetClassesJar))
    api(files(javaRuntimeJarFile).builtBy(copyJavaRuntimeJar))
    api(files(cryptoRuntimeJarFile).builtBy(copyCryptoRuntimeJar))
}
