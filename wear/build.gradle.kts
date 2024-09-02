@file:Suppress("UnstableApiUsage")
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
}

android {
    compileSdk = 34

    defaultConfig {
        applicationId = "com.weartools.weekdayutccomp"
        minSdk = 27
        targetSdk = 34
        versionCode = 10000345
        versionName = "3.4.5"
        resourceConfigurations += listOf("en", "cs", "de", "el", "es", "it", "pt","pl", "ro", "sk", "zh", "ru")
    }
    bundle {
        language {
            enableSplit = false // TO HAVE ALL LANGUAGES AVAILABLE
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
    kotlinOptions {
        jvmTarget = "11"
    }

    namespace = "com.weartools.weekdayutccomp"
}

dependencies {
    val composeMaterial = "1.6.8"
    //val wearCompose = "1.3.1"
    val wearComposeBeta = "1.4.0-rc01"

    // NEW MOON / SUNRISE / SUNSET COMP
    //request permission
    implementation ("com.google.accompanist:accompanist-permissions:0.34.0")
    //location
    implementation ("com.google.android.gms:play-services-location:21.3.0")

    // Used for WorkManager
    implementation ("androidx.work:work-runtime-ktx:2.9.1")

    // MOON PHASE HELPER
    implementation ("org.shredzone.commons:commons-suncalc:3.7")

    // WEAR OS
    implementation ("com.google.android.gms:play-services-wearable:18.2.0")
    implementation ("androidx.wear.watchface:watchface-complications-data-source-ktx:1.2.1")
    implementation ("androidx.wear:wear:1.3.0")

    //COMPOSE
    implementation ("androidx.core:core-ktx:1.13.1")
    implementation ("androidx.compose.ui:ui:$composeMaterial")
    implementation ("androidx.wear.compose:compose-material:$wearComposeBeta")
    implementation ("androidx.wear.compose:compose-foundation:$wearComposeBeta")
    implementation ("androidx.compose.ui:ui-tooling-preview:$composeMaterial")
    implementation ("androidx.activity:activity-compose:1.9.1")
    implementation ("androidx.compose.material:material-icons-extended:$composeMaterial")

    androidTestImplementation ("androidx.compose.ui:ui-test-junit4:$composeMaterial")
    debugImplementation ("androidx.compose.ui:ui-tooling:$composeMaterial")
    debugImplementation ("androidx.compose.ui:ui-test-manifest:$composeMaterial")

    //DataStore
    implementation ("androidx.datastore:datastore-preferences:1.1.1")

    //Locale
    implementation ("androidx.appcompat:appcompat:1.7.0")

    // INPUT
    implementation ("androidx.wear:wear-input:1.2.0-alpha02")

    // SPLASH SCREEN
    implementation ("androidx.core:core-splashscreen:1.0.1")

    // HOROLOGIST
    implementation ("com.google.android.horologist:horologist-composables:0.2.8")
    implementation ("com.google.android.horologist:horologist-audio-ui:0.2.8")

    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    // PERSIAN DATE
    implementation("com.github.samanzamani:persiandate:1.7.1")

    // HILT
    implementation ("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation ("com.google.dagger:hilt-android:2.51")
    ksp ("com.google.dagger:hilt-compiler:2.51")

    // Google Places
    implementation ("com.google.android.libraries.places:places:3.5.0")

    // INPUT
    implementation("androidx.compose.material3:material3:1.2.1")

    // Ktor + OkHttp + Kotlinx.Serialization
    val ktorVersion = "2.3.11"
    implementation ("io.ktor:ktor-client-logging:$ktorVersion")
    implementation ("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation ("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation ("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
}


