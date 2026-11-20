plugins {
    alias(libs.plugins.escapelauncher.android.feature)
}

android {
    namespace = "com.geecee.escapelauncher.feature.settings"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:model"))
    implementation(project(":core:theme"))

    implementation(libs.coil.compose)
    implementation(libs.androidx.hilt.navigation.compose)
}