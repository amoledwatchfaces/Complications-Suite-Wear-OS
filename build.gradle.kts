plugins {
    id("org.jetbrains.kotlin.android") version("1.9.0") apply false
    id("com.android.library") version "8.1.1" apply false
}

tasks.register("clean", Delete::class){
    delete(rootProject.buildDir)
}

buildscript {
    dependencies {
        classpath ("com.android.tools.build:gradle:8.1.1")
    }
    repositories {
        google()
    }
}