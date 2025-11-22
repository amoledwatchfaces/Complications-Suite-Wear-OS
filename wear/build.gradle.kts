import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    id ("com.android.application")
    id ("org.jetbrains.kotlin.android")
    id ("kotlin-parcelize")
    id ("kotlinx-serialization")
    id ("com.google.devtools.ksp")
    id ("com.google.dagger.hilt.android")
    id ("org.jetbrains.kotlin.plugin.compose")
    id ("com.google.gms.google-services")
    id ("com.google.firebase.crashlytics")
}

android {
    compileSdk = 36

    defaultConfig {
        applicationId = "com.weartools.weekdayutccomp"
        minSdk = 27
        targetSdk = 36
        versionCode = rootProject.extra["versionCode"] as Int
        versionName = rootProject.extra["versionName"] as String

        versionNameSuffix = "-wear"
        versionCode = 20000 + (versionCode ?: 0)
    }
    bundle {
        language {
            enableSplit = false // TO HAVE ALL LANGUAGES AVAILABLE
        }
        androidResources { // <-- Add this block
            localeFilters.addAll(listOf("en", "cs", "de", "el", "es", "fr", "it", "pt", "pl", "ro", "sk","zh","ru","sv"))
        }
    }
    buildTypes {
        val apis = Properties().apply{
            load(FileInputStream(file("C:\\Users\\amoledwatchfaces\\workspace\\Projects\\apps\\(x) 4 - Complications Suite Wear OS APP\\APIs\\apis.properties")))
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField(type ="String", name = "PLACES_API_KEY", value = apis.getProperty("PLACES_API_KEY"))
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("debug")  {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile(name = "proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField(type ="String", name = "PLACES_API_KEY", value = apis.getProperty("PLACES_API_KEY"))
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = true
        }
    }
    testBuildType = "debug"

    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    namespace = "com.weartools.weekdayutccomp"
}

dependencies {
    // Permissions
    implementation ("com.google.accompanist:accompanist-permissions:0.37.3")
    // Location
    implementation ("com.google.android.gms:play-services-location:21.3.0")

    // Used for WorkManager
    implementation ("androidx.work:work-runtime-ktx:2.10.4")

    // Moon Phase Helper
    implementation ("org.shredzone.commons:commons-suncalc:3.11")

    // Wear OS
    implementation ("com.google.android.gms:play-services-wearable:19.0.0")
    implementation ("androidx.wear.watchface:watchface-complications-data-source-ktx:1.2.1")
    implementation ("androidx.wear:wear:1.3.0")

    // Compose
    implementation ("androidx.core:core-ktx:1.17.0")
    implementation ("androidx.compose.ui:ui:1.9.1")
    implementation ("androidx.wear.compose:compose-material:1.5.1")
    implementation ("androidx.wear.compose:compose-foundation:1.5.1")
    implementation ("androidx.compose.ui:ui-tooling-preview:1.9.1")
    implementation ("androidx.activity:activity-compose:1.11.0")
    implementation ("androidx.compose.material:material-icons-extended:1.7.8")

    androidTestImplementation ("androidx.compose.ui:ui-test-junit4:1.9.1")
    debugImplementation ("androidx.compose.ui:ui-tooling:1.9.1")
    debugImplementation ("androidx.compose.ui:ui-test-manifest:1.9.1")

    // DataStore
    implementation ("androidx.datastore:datastore:1.1.7")

    // Locale
    implementation ("androidx.appcompat:appcompat:1.7.1")

    // Input
    implementation ("androidx.wear:wear-input:1.2.0")

    // Splash Screen
    implementation ("androidx.core:core-splashscreen:1.0.1")

    // Horologist
    implementation ("com.google.android.horologist:horologist-composables:0.7.15")
    implementation ("com.google.android.horologist:horologist-audio-ui:0.7.15")

    // Serialization
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    // Persian Date
    implementation("com.github.samanzamani:persiandate:1.7.1")

    // Hilt
    implementation ("androidx.hilt:hilt-navigation-compose:1.3.0")
    implementation ("com.google.dagger:hilt-android:2.57.1")
    ksp ("com.google.dagger:hilt-compiler:2.57.1")

    // Google Places
    implementation ("com.google.android.libraries.places:places:4.4.1")

    // Material 3
    implementation("androidx.compose.material3:material3:1.3.2")

    // Firebase
    implementation (platform("com.google.firebase:firebase-bom:34.2.0"))
    implementation ("com.google.firebase:firebase-crashlytics")

    // Protolayout
    implementation("androidx.wear.protolayout:protolayout-expression:1.3.0")
    implementation("androidx.wear.protolayout:protolayout:1.3.0")
}


