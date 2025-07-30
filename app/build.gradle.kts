import java.util.Properties

// Load properties from file
val apikeyProperties = Properties().apply {
    load(rootProject.file("apikey.properties").inputStream())
}
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.android.projectandroid"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.android.projectandroid"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // BuildConfig field
        buildConfigField("String", "GOOGLE_MAP_API_KEY", "\"${apikeyProperties["API_KEY"]}\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Optional: Use manifestPlaceholders to inject into AndroidManifest.xml
    buildTypes {
        getByName("release") {
            manifestPlaceholders["GOOGLE_MAP_API_KEY"] = apikeyProperties["API_KEY"]?.toString() ?: ""
        }
        getByName("debug") {
            manifestPlaceholders["GOOGLE_MAP_API_KEY"] = apikeyProperties["API_KEY"]?.toString() ?: ""
        }
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.google.android.gms:play-services-auth:19.2.0")
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.google.maps.android:android-maps-utils:2.4.0")
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    implementation ("com.github.bumptech.glide:glide:4.15.1")

    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.swiperefreshlayout)
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")
    implementation (libs.play.services.auth)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}