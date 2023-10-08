plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    signingConfigs {
        create("release") {
            storeFile = file("C:\\Users\\amoledwatchfaces\\WatchFaceStudio\\keystore\\keystore.jks")
            storePassword = "Coldplay9651"
            keyAlias = "key0"
            keyPassword = "Coldplay9651"
        }
    }
    namespace = "com.weartools.weekdayutccomp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.weartools.weekdayutccomp"
        minSdk = 23
        //noinspection OldTargetApi
        targetSdk = 33
        versionCode = 10000127
        versionName = "1.2.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        getByName("debug")  {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile(name = "proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = true
        }
    }
    testBuildType = "debug"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // PREVIOUS APP
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    implementation ("com.google.android.gms:play-services-wearable:18.1.0")

    implementation ("com.google.android.play:review-ktx:2.0.1")
    implementation ("com.google.android.play:review:2.0.1")
    implementation ("androidx.wear:wear-remote-interactions:1.0.0")
    
    // SPLASH SCREEN
    implementation ("androidx.core:core-splashscreen:1.0.1")

    // ICONS
    implementation ("androidx.compose.material:material-icons-extended:1.5.1")

    // COMPOSE
    implementation ("androidx.activity:activity-compose:1.7.2")

    implementation ("androidx.compose.material3:material3:1.2.0-alpha08")
    implementation ("androidx.compose.material3:material3-window-size-class:1.2.0-alpha08")

    implementation(platform("androidx.compose:compose-bom:2023.05.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")

    implementation ("androidx.compose.material3:material3")
    implementation ("androidx.compose.material:material:1.5.1")

    // NAVIGATION
    implementation("androidx.navigation:navigation-compose:2.7.3")

    implementation ("androidx.activity:activity-ktx:1.8.0-rc01")
    implementation ("androidx.core:core-ktx:1.12.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // DEBUG
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}