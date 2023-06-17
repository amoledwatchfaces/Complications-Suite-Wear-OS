// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    extra.apply {
        set("compose_version", "1.4.2")
        set("compose_wear_version", "1.1.2")
    }
}

plugins {
    val kotlinVersion = "1.8.20"
    id("com.android.application") version("8.0.2") apply false
    id("org.jetbrains.kotlin.android") version(kotlinVersion) apply false
}

tasks.register("clean", Delete::class){
    delete(rootProject.buildDir)
}
