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

//Add DataStore deps, for know I'm gonna leave this out of a convention just because it's only gonna
//be needed in this module
dependencies {
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.palette)
    implementation(libs.androidx.palette.ktx)
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:di"))
    implementation(project(":core:common"))

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.common)
    implementation(libs.okhttp)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.gson)
    implementation(libs.gson)
    implementation(libs.timber)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)
}
