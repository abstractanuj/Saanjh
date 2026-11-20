plugins {
    alias(libs.plugins.escapelauncher.android.application)
    alias(libs.plugins.escapelauncher.android.compose)
    alias(libs.plugins.escapelauncher.android.composeui)
    alias(libs.plugins.escapelauncher.android.hilt)
    alias(libs.plugins.escapelauncher.android.testing)
    alias(libs.plugins.escapelauncher.android.room)
}

val baseVersionCode = "3.0"

android {
    namespace = "com.geecee.escapelauncher"

    defaultConfig {
        applicationId = "com.sareprne.saanjh"
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 3
        versionName = baseVersionCode
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        resValue("string", "app_version", baseVersionCode)
        resValue("string", "app_name", "saanjh")
        resValue("string", "app_flavour", "FOSS")

        buildConfigField("String", "APP_VERSION", "\"$baseVersionCode\"")
        buildConfigField("String", "APP_NAME", "\"saanjh\"")
        buildConfigField("String", "APP_FLAVOUR", "\"FOSS\"")
    }

    signingConfigs {
        create("debugConfig") {
            storeFile = file("${rootDir}/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        create("release") {
            val releaseKeystore = rootProject.file("release.keystore")
            storeFile = if (releaseKeystore.exists()) releaseKeystore else file("${rootDir}/debug.keystore")
            storePassword = if (releaseKeystore.exists()) "escape123" else "android"
            keyAlias = if (releaseKeystore.exists()) "escape" else "androiddebugkey"
            keyPassword = if (releaseKeystore.exists()) "escape123" else "android"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debugConfig")
            resValue("string", "app_name", "saanjh")
            buildConfigField("String", "APP_NAME", "\"saanjh\"")
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            resValue("string", "app_name", "saanjh")
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
        buildConfig = true
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
    implementation(libs.bundles.hilt)
    implementation(libs.timber)

    // Performance
    implementation(libs.androidx.profileinstaller)

    // Modules
    implementation(project(":core:data"))
    implementation(project(":core:ui"))
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:theme"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:gestures"))
    implementation(project(":feature:widgets"))
}