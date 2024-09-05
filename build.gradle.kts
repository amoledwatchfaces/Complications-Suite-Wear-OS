plugins {
    id ("org.jetbrains.kotlin.android") version("2.0.0") apply false
    id ("org.jetbrains.kotlin.plugin.compose") version("2.0.0") apply false
    id ("com.google.dagger.hilt.android") version "2.51" apply false
    id ("com.google.devtools.ksp") version("2.0.0-1.0.21") apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

buildscript {
    dependencies {
        classpath ("com.android.tools.build:gradle:8.6.0")
        classpath ("org.jetbrains.kotlin:kotlin-serialization:2.0.0")
    }
    repositories {
        google()
    }
}