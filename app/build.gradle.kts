plugins {
    alias(libs.plugins.escapelauncher.android.application)
    alias(libs.plugins.escapelauncher.android.compose)
    alias(libs.plugins.escapelauncher.android.composeui)
    alias(libs.plugins.escapelauncher.android.hilt)
    alias(libs.plugins.escapelauncher.android.testing)
    alias(libs.plugins.escapelauncher.android.room)
    alias(libs.plugins.kotlin.serialization)
}

val baseVersionCode = "3.0"

android {
    namespace = "com.geecee.escapelauncher"

    defaultConfig {
        applicationId = "com.geecee.escapelauncher"
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 3
        versionName = baseVersionCode
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        resValue("string", "app_version", baseVersionCode)
        resValue("string", "app_name", "Sareprne")
        resValue("string", "app_flavour", "Unknown Flavor")

        buildConfigField("String", "APP_VERSION", "\"$baseVersionCode\"")
        buildConfigField("String", "APP_NAME", "\"Sareprne\"")
        buildConfigField("String", "APP_FLAVOUR", "\"Unknown Flavor\"")
    }
    signingConfigs {
        create("release") {
            storeFile = rootProject.file("release.keystore")
            storePassword = "escape123"
            keyAlias = "escape"
            keyPassword = "escape123"
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            resValue("string", "app_name", "Sareprne Dev")
            buildConfigField("String", "APP_NAME", "\"Sareprne Dev\"")
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            resValue("string", "app_name", "Sareprne")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "../proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        resValues = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.bundles.androidx.core)
    implementation(libs.bundles.compose.ui)
    implementation(libs.bundles.navigation3)
    implementation(libs.bundles.hilt)
    implementation(libs.timber)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    
    // Performance
    implementation(libs.androidx.profileinstaller)

    // JSON Parsing
    implementation(libs.gson)

    // Testing Libraries
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debugging Tools
    debugImplementation(libs.androidx.ui.test.manifest)

    // Modules
    implementation(project(":core:data"))
    implementation(project(":core:ui"))
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:theme"))
    implementation(project(":core:analytics"))
    implementation(project(":core:cloudmessaging"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:gestures"))
    implementation(project(":feature:widgets"))
}