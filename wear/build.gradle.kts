@file:Suppress("UnstableApiUsage")

plugins {
    id ("com.android.application")
    id ("org.jetbrains.kotlin.android")
    id ("kotlin-parcelize")
    id ("kotlinx-serialization")
    id ("kotlin-kapt")
    id ("com.google.dagger.hilt.android")
}

android {
    compileSdk = 34

    defaultConfig {
        applicationId = "com.weartools.weekdayutccomp"
        minSdk = 27
        //noinspection OldTargetApi
        targetSdk = 33
        versionCode = 10000297
        versionName = "2.9.7"
        resourceConfigurations += listOf("en", "cs", "de", "el", "es", "it", "pt", "ro", "sk", "zh", "ru")
    }
    bundle {
        language {
            enableSplit = false // TO HAVE ALL LANGUAGES AVAILABLE
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    namespace = "com.weartools.weekdayutccomp"
}

dependencies {
    val composeUiVersion = "1.6.2"
    val composeWearVersion = "1.3.0"

    // NEW MOON / SUNRISE / SUNSET COMP
    //request permission
    implementation ("com.google.accompanist:accompanist-permissions:0.28.0")
    //location
    implementation ("com.google.android.gms:play-services-location:21.1.0")

    // Used for WorkManager
    implementation ("androidx.work:work-runtime:2.9.0")
    implementation ("androidx.work:work-runtime-ktx:2.9.0")

    // MOON PHASE HELPER
    implementation ("org.shredzone.commons:commons-suncalc:3.7")

    // WEAR OS
    implementation ("com.google.android.gms:play-services-wearable:18.1.0")
    implementation ("androidx.wear.watchface:watchface-complications-data-source-ktx:1.2.1")
    implementation ("androidx.wear:wear:1.3.0")

    //COMPOSE
    implementation ("androidx.core:core-ktx:1.12.0")
    implementation ("androidx.compose.ui:ui:$composeUiVersion")
    implementation ("androidx.wear.compose:compose-material:$composeWearVersion")
    implementation ("androidx.wear.compose:compose-foundation:$composeWearVersion")
    implementation ("androidx.compose.ui:ui-tooling-preview:$composeUiVersion")
    implementation ("androidx.activity:activity-compose:1.8.2")
    implementation ("androidx.compose.material:material-icons-extended:$composeUiVersion")

    androidTestImplementation ("androidx.compose.ui:ui-test-junit4:$composeUiVersion")
    debugImplementation ("androidx.compose.ui:ui-tooling:$composeUiVersion")
    debugImplementation ("androidx.compose.ui:ui-test-manifest:$composeUiVersion")

    //DataStore
    implementation ("androidx.datastore:datastore-preferences:1.0.0")

    //Locale
    implementation ("androidx.appcompat:appcompat:1.7.0-alpha03")

    // INPUT
    implementation ("androidx.wear:wear-input:1.2.0-alpha02")

    // SPLASH SCREEN
    implementation ("androidx.core:core-splashscreen:1.0.1")

    // HOROLOGIST
    implementation ("com.google.android.horologist:horologist-composables:0.2.8")
    implementation ("com.google.android.horologist:horologist-audio-ui:0.2.8")
    implementation ("com.google.android.horologist:horologist-annotations:0.6.3")

    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    // HILT
    implementation ("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation ("com.google.dagger:hilt-android:2.49")

    implementation("com.github.samanzamani:persiandate:1.7.1")
    implementation("com.jakewharton.threetenabp:threetenabp:1.3.1")

    implementation ("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation ("com.google.dagger:hilt-android:2.49")
    kapt ("com.google.dagger:hilt-compiler:2.47")
}
// Allow references to generated code
kapt {
    correctErrorTypes = true
}

