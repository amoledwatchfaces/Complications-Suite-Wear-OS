plugins {
    id ("com.android.application") version ("8.2.2") apply false
    id ("org.jetbrains.kotlin.android") version ("2.2.0") apply false
    id ("org.jetbrains.kotlin.plugin.compose") version ("2.2.0") apply false
    id ("com.google.dagger.hilt.android") version ("2.57.2") apply false
    id ("com.google.devtools.ksp") version ("2.3.2") apply false
    id ("com.google.gms.google-services") version ("4.4.2") apply false
    id ("com.google.firebase.crashlytics") version ("3.0.2") apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

buildscript {

    /** Set version for wear & mobile modules **/
    val versionCode by extra(10000394)
    val versionName by extra("3.9.4")

    dependencies {
        classpath ("com.android.tools.build:gradle:8.13.1")
        classpath ("org.jetbrains.kotlin:kotlin-serialization:2.2.0")
    }
    repositories {
        google()
    }
}