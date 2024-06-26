plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.syhdzn.amigos_gemastik"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.syhdzn.amigos_gemastik"
        minSdk = 27
        targetSdk = 34
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
        mlModelBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // TensorFlow Lite versi 2.7.0
    implementation("org.tensorflow:tensorflow-lite:2.7.0")
    
    // Firebase Auth
    implementation("com.google.firebase:firebase-auth:21.0.0")

    // Firebase ML Model Interpreter Dependency
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database:20.3.1")

    // CameraX dependencies (sesuaikan versi jika diperlukan)
    implementation("androidx.camera:camera-camera2:1.3.2")
    implementation("androidx.camera:camera-lifecycle:1.1.0-beta03")
    implementation("androidx.camera:camera-view:1.1.0-beta03")

    // AndroidX dan Kotlin dependencies (sesuaikan versi jika diperlukan)
    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.appcompat:appcompat:1.4.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    implementation("androidx.activity:activity-ktx:1.4.0")
    implementation("com.google.android.material:material:1.5.0-alpha04")

    // SweetAlert library
    implementation("com.github.f0ris.sweetalert:library:1.6.2")

    // Chip Navigation Bar library
    implementation("com.github.ismaeldivita:chip-navigation-bar:1.4.0")

    // Testing dependencies (junit, espresso, dll.)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    // Other dependencies
    implementation("androidx.camera:camera-core:1.2.0")
    implementation("androidx.camera:camera-camera2:1.2.0")
    implementation("androidx.camera:camera-lifecycle:1.2.0")
    implementation ("androidx.core:core-ktx:1.7.0")
    implementation ("com.github.sina-seyfi:AdvancedCardView:1.0.1")
    implementation ("com.github.amarjain07:StickyScrollView:1.0.3")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.google.android.gms:play-services-maps:18.0.2")
    implementation ("com.google.firebase:firebase-storage:20.0.1")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation ("com.google.android.material:material:1.4.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.google.maps.android:android-maps-utils:2.3.0")

}