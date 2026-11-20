plugins {
    alias(libs.plugins.escapelauncher.android.library)
    alias(libs.plugins.escapelauncher.android.hilt)
    alias(libs.plugins.escapelauncher.android.testing)
    alias(libs.plugins.escapelauncher.android.room)
}

android {
    namespace = "com.geecee.escapelauncher.core.data"

    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.palette)
    implementation(libs.androidx.palette.ktx)
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:di"))
    implementation(project(":core:common"))

    implementation(libs.hilt.android)
    implementation(libs.timber)
    ksp(libs.hilt.compiler)
}
