plugins {
    id ("com.android.application")
    id ("org.jetbrains.kotlin.android")
    id ("kotlin-parcelize")
}

@Suppress("UnstableApiUsage") //TODO: CHECK LATER
android {
    compileSdk = 33

    defaultConfig {
        applicationId = "com.weartools.weekdayutccomp"
        minSdk = 27
        targetSdk = 33
        versionCode = 217
        versionName = "2.1.7"
        resourceConfigurations += listOf("en", "cs", "de", "el", "es", "it", "pt", "ro", "sk", "zh")
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
        kotlinCompilerExtensionVersion = "1.4.6"
    }
    namespace = "com.weartools.weekdayutccomp"
}

dependencies {
    val composeUiVersion = rootProject.extra["compose_version"]
    val composeWearVersion = rootProject.extra["compose_wear_version"]

    // NEW MOON / SUNRISE / SUNSET COMP
    //request permission
    implementation ("com.google.accompanist:accompanist-permissions:0.25.0")
    //location
    implementation ("com.google.android.gms:play-services-location:21.0.1")

    // Used for WorkManager
    implementation ("androidx.work:work-runtime:2.8.1")
    implementation ("androidx.work:work-runtime-ktx:2.8.1")

    // MOON PHASE HELPER
    implementation ("org.shredzone.commons:commons-suncalc:3.7")

    // WEAR OS
    implementation ("com.google.android.gms:play-services-wearable:18.0.0")
    implementation ("androidx.wear.watchface:watchface-complications-data-source-ktx:1.1.1")
    implementation ("androidx.wear:wear:1.2.0")

    //COMPOSE
    implementation ("androidx.preference:preference-ktx:1.2.0")
    implementation ("androidx.core:core-ktx:1.10.0")
    implementation ("androidx.compose.ui:ui:$composeUiVersion")
    implementation ("androidx.wear.compose:compose-material:$composeWearVersion")
    implementation ("androidx.wear.compose:compose-foundation:$composeWearVersion")
    implementation ("androidx.compose.ui:ui-tooling-preview:$composeUiVersion")
    implementation ("androidx.activity:activity-compose:1.7.1")
    implementation ("androidx.compose.material:material-icons-core:$composeUiVersion")

    androidTestImplementation ("androidx.compose.ui:ui-test-junit4:$composeUiVersion")
    debugImplementation ("androidx.compose.ui:ui-tooling:$composeUiVersion")
    debugImplementation ("androidx.compose.ui:ui-test-manifest:$composeUiVersion")

    //Pref
    implementation ("androidx.datastore:datastore-preferences:1.0.0")

    //Locale
    implementation ("androidx.appcompat:appcompat:1.7.0-alpha02")

    // INPUT
    implementation ("androidx.wear:wear-input:1.2.0-alpha02")
}