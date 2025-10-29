import org.gradle.api.tasks.Delete

plugins {
    id("com.android.application") version "8.7.3" apply false
    id("com.android.library") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.20" apply false
}

val kotlinVersion = "2.0.20"

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

extra.apply {
    set("kotlinVersion", kotlinVersion)
    set("compileSdkVersion", 34)
    set("minSdkVersion", 21)
    set("targetSdkVersion", 34)
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
