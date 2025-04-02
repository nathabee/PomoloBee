plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
}


android {
    namespace = "de.nathabee.pomolobee"
    compileSdk = 35

    defaultConfig {
        applicationId = "de.nathabee.pomolobee"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        languageVersion = "1.9" // ✅ Force Kotlin 1.9 to avoid KAPT issues
    }

    buildFeatures {
        compose = true // ✅ Enables Jetpack Compose
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeBom.get() // ✅ Uses correct Compose compiler
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }  // ✅ Fixed: Correctly closed `android {}` block
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
    implementation(libs.play.services.awareness)
    implementation(libs.protolite.well.known.types)
    implementation(libs.androidx.documentfile)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Jetpack Navigation (Compose)
    implementation(libs.androidx.navigation.compose)

    // DataStore
    implementation(libs.androidx.datastore.preferences)


    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Coil for SVG support
    implementation(libs.coil)
    implementation(libs.coil.svg)

    //interface json
    implementation(libs.gson)
    // OpenCV
    implementation(libs.opencv)

    // Glide
    implementation(libs.glide)
    ksp(libs.glideCompiler)

    // Debugging
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


}



