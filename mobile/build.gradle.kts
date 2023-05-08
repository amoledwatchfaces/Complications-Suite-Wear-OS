plugins {
    id ("com.android.application")
    id ("org.jetbrains.kotlin.android")
}

@Suppress("UnstableApiUsage") //TODO: CHECK LATER
android {

    compileSdk = 33

    defaultConfig {
        applicationId = "com.weartools.weekdayutccomp"
        minSdk = 23
        targetSdk = 33
        versionCode = 222
        versionName = "2.2.2"

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

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
    }

    namespace = "com.weartools.weekdayutccomp"
}

dependencies {
    implementation ("com.google.android.play:review-ktx:2.0.1")
    implementation ("com.google.android.play:review:2.0.1")
    implementation ("androidx.wear:wear-remote-interactions:1.0.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation ("androidx.activity:activity-ktx:1.7.0")
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("androidx.core:core-ktx:1.10.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")
    implementation ("com.google.android.gms:play-services-wearable:18.0.0")
}
