plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id ("dagger.hilt.android.plugin")
    id ("kotlin-parcelize")
    id ("kotlin-kapt")
}

android {
    namespace = "com.vishnuhs.expensetracker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.vishnuhs.expensetracker"
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
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            ndk.debugSymbolLevel = "FULL"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "2025.08.00"
    }
    ndkVersion = "29.0.13846066 rc3"
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Core Android
    implementation (libs.androidx.core.ktx)
    implementation (libs.androidx.lifecycle.runtime.ktx)
    implementation (libs.androidx.activity.compose)

    // Compose BOM
    implementation ("androidx.compose:compose-bom:2025.08.00")
    implementation ("androidx.compose.ui:ui")
    implementation ("androidx.compose.ui:ui-graphics")
    implementation ("androidx.compose.ui:ui-tooling-preview")
    implementation ("androidx.compose.material3:material3")
    implementation ("androidx.compose.material:material-icons-extended")
    implementation ("androidx.compose.runtime:runtime-android:1.9.0")

    // Navigation
    implementation ("androidx.navigation:navigation-compose:2.9.3")

    // ViewModel
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.2")

    // Room Database
    implementation ("androidx.room:room-runtime:2.7.2")
    implementation ("androidx.room:room-ktx:2.7.2")
    kapt ("androidx.room:room-compiler:2.7.2")

    // Hilt for DI
    implementation ("com.google.dagger:hilt-android:2.57")
    kapt ("com.google.dagger:hilt-compiler:2.57")
    implementation ("androidx.hilt:hilt-navigation-compose:1.2.0")

    // CameraX
    implementation ("androidx.camera:camera-core:1.4.2")
    implementation ("androidx.camera:camera-camera2:1.4.2")
    implementation ("androidx.camera:camera-lifecycle:1.4.2")
    implementation ("androidx.camera:camera-view:1.4.2")

    // ML Kit Text Recognition
    //implementation ("com.google.mlkit:text-recognition:16.0.1")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.1")

    // Image loading
    implementation ("io.coil-kt:coil-compose:2.7.0")

    // Date picker
    implementation ("io.github.vanpra.compose-material-dialogs:datetime:0.9.0")

    // Charts
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Permissions
    implementation ("com.google.accompanist:accompanist-permissions:0.37.3")

    // Testing
    testImplementation (libs.junit)
    androidTestImplementation (libs.androidx.junit)
    androidTestImplementation (libs.androidx.espresso.core)
    androidTestImplementation ("androidx.compose:compose-bom:2025.08.00")
    androidTestImplementation ("androidx.compose.ui:ui-test-junit4")
    debugImplementation ("androidx.compose.ui:ui-tooling")
    debugImplementation ("androidx.compose.ui:ui-test-manifest")
}