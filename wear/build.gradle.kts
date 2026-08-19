import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    id ("com.android.application")
    id ("kotlinx-serialization")
    id ("com.google.devtools.ksp")
    id ("com.google.dagger.hilt.android")
    id ("org.jetbrains.kotlin.plugin.compose")
    id ("org.jetbrains.kotlin.plugin.parcelize")
    id ("com.google.gms.google-services")
    //id ("com.google.firebase.crashlytics")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

android {
    namespace = "com.weartools.weekdayutccomp"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.weartools.weekdayutccomp"
        minSdk = 27
        targetSdk = 36
        versionCode = 10000408
        versionName = "4.0.8"

        versionNameSuffix = "-wear"
        versionCode = 20000 + (versionCode ?: 0)
    }
    bundle {
        language {
            enableSplit = false // TO HAVE ALL LANGUAGES AVAILABLE
        }
        androidResources { // <-- Add this block
            localeFilters.addAll(listOf("en", "cs", "de", "el", "es", "fr", "it", "pt", "pl", "ro", "sk","zh","ru","sv","tr"))
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
            //noinspection NotShrinkingResources
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

}

dependencies {
    val composeBom = platform ("androidx.compose:compose-bom:2025.08.00")

    // Wear OS
    implementation ("com.google.android.gms:play-services-wearable:20.0.1")
    implementation ("androidx.wear.watchface:watchface-complications-data-source-ktx:1.3.0")
    implementation ("androidx.wear:wear:1.4.0")

    // General compose dependencies
    implementation (composeBom)
    implementation ("androidx.activity:activity-compose:1.13.0")
    implementation ("androidx.compose.ui:ui:1.12.0")
    implementation ("androidx.compose.ui:ui-tooling-preview:1.12.0")
    implementation ("androidx.compose.material:material-icons-extended:1.7.8")

    // Compose for Wear OS dependencies
    implementation ("androidx.core:core-ktx:1.19.0")
    implementation ("androidx.wear.compose:compose-material3:1.6.2")
    implementation ("androidx.wear.compose:compose-navigation:1.6.2")

    // Mobile
    implementation("androidx.compose.material3:material3:1.4.0")

    // Foundation is additive, so you can use the mobile version in your Wear OS app.
    implementation ("androidx.wear.compose:compose-foundation:1.6.2")

    // Wear OS preview annotations
    implementation ("androidx.wear.compose:compose-ui-tooling:1.6.2")

    // Permissions
    implementation ("com.google.accompanist:accompanist-permissions:0.37.3")

    // Location
    implementation ("com.google.android.gms:play-services-location:21.4.0")

    // Used for WorkManager
    implementation ("androidx.work:work-runtime-ktx:2.11.2")

    // Moon Phase Helper
    implementation ("org.shredzone.commons:commons-suncalc:3.11")

    // Testing
    androidTestImplementation ("androidx.compose.ui:ui-test-junit4:1.12.0")
    debugImplementation ("androidx.compose.ui:ui-tooling:1.12.0")
    debugImplementation ("androidx.compose.ui:ui-test-manifest:1.12.0")

    // DataStore
    implementation ("androidx.datastore:datastore:1.2.1")

    // Locale
    implementation ("androidx.appcompat:appcompat:1.8.0")

    // Input
    implementation ("androidx.wear:wear-input:1.2.0")

    // Splash Screen
    implementation ("androidx.core:core-splashscreen:1.2.0")

    // Horologist
    implementation ("com.google.android.horologist:horologist-composables:0.7.15")
    implementation ("com.google.android.horologist:horologist-audio-ui:0.7.15")

    // Serialization
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")

    // Hilt
    implementation ("androidx.hilt:hilt-navigation-compose:1.4.0")
    implementation ("com.google.dagger:hilt-android:2.60.1")
    ksp ("com.google.dagger:hilt-compiler:2.60.1")

    // Google Places
    implementation ("com.google.android.libraries.places:places:5.3.0") // 4.2.0 and later causing some issues, use 4.1.0!

    // Firebase
    //implementation (platform("com.google.firebase:firebase-bom:34.13.0"))
    //implementation ("com.google.firebase:firebase-crashlytics")

    // Protolayout
    implementation("androidx.wear.protolayout:protolayout-expression:1.4.2")
    implementation("androidx.wear.protolayout:protolayout:1.4.2")
}


