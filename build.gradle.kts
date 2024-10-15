plugins {
    id ("org.jetbrains.kotlin.android") version ("2.0.0") apply false
    id ("org.jetbrains.kotlin.plugin.compose") version ("2.0.0") apply false
    id ("com.google.dagger.hilt.android") version ("2.51") apply false
    id ("com.google.devtools.ksp") version("2.0.0-1.0.21") apply false
    id ("com.google.gms.google-services") version ("4.4.2") apply false
    id ("com.google.firebase.crashlytics") version ("3.0.2") apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

buildscript {

    /** Set version for wear & mobile modules **/
    val versionCode by extra(10000367)
    val versionName by extra("3.6.7")

    dependencies {
        classpath ("com.android.tools.build:gradle:8.6.1")
        classpath ("org.jetbrains.kotlin:kotlin-serialization:2.0.0")
    }
    repositories {
        google()
    }
}