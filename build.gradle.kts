plugins {
    id ("org.jetbrains.kotlin.android") version ("2.0.20") apply false
    id ("org.jetbrains.kotlin.plugin.compose") version ("2.0.20") apply false
    id ("com.google.dagger.hilt.android") version ("2.52") apply false
    id ("com.google.devtools.ksp") version("2.0.21-1.0.25") apply false
    id ("com.google.gms.google-services") version ("4.4.2") apply false
    id ("com.google.firebase.crashlytics") version ("3.0.2") apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

buildscript {

    /** Set version for wear & mobile modules **/
    val versionCode by extra(10000369)
    val versionName by extra("3.6.9")

    dependencies {
        classpath ("com.android.tools.build:gradle:8.6.1")
        classpath ("org.jetbrains.kotlin:kotlin-serialization:2.0.20")
    }
    repositories {
        google()
    }
}