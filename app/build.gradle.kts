plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.example.testmaster"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.testmaster"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.firebase:firebase-firestore:25.0.0")
    implementation("com.google.firebase:firebase-storage:21.0.0")
    implementation("com.google.firebase:firebase-crashlytics:19.1.0")
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.0")
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation ("androidx.recyclerview:recyclerview:1.2.1")
    implementation ("com.squareup.picasso:picasso:2.71828")
    implementation ("com.github.yalantis:ucrop:2.2.8")
    implementation ("com.facebook.shimmer:shimmer:0.5.0")
    implementation("com.google.code.gson:gson:2.10.1")
}
