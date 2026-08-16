plugins {
    id ("com.android.application") version ("9.2.1") apply false
    id ("org.jetbrains.kotlin.android") version ("2.4.0") apply false
    id ("org.jetbrains.kotlin.plugin.compose") version ("2.4.0") apply false
    id ("com.google.dagger.hilt.android") version ("2.60.1") apply false
    id ("com.google.devtools.ksp") version ("2.3.4") apply false
    id ("com.google.gms.google-services") version ("4.4.2") apply false
    //id ("com.google.firebase.crashlytics") version ("3.0.2") apply false
}

tasks.register("clean", Delete::class) {
    description = "Clean build directory"
    delete(rootProject.layout.buildDirectory)
}

buildscript {

    /** Set version for wear & mobile modules **/
    val versionCode by extra(10000407)
    val versionName by extra("4.0.7")

    dependencies {
        classpath ("com.android.tools.build:gradle:9.3.1")
        classpath ("org.jetbrains.kotlin:kotlin-serialization:2.4.0")
    }
    repositories {
        google()
    }
}