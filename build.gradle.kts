plugins {
    id ("org.jetbrains.kotlin.android") version("1.9.22") apply false
    id ("com.android.library") version "8.3.1" apply false
    id ("com.google.dagger.hilt.android") version "2.51" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

buildscript {
    dependencies {
        classpath ("com.android.tools.build:gradle:8.3.1")
        classpath ("org.jetbrains.kotlin:kotlin-serialization:1.9.22")
    }
    repositories {
        google()
    }
}