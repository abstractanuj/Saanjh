plugins {
    alias(libs.plugins.escapelauncher.android.feature)
    alias(libs.plugins.escapelauncher.android.compose)
    alias(libs.plugins.escapelauncher.android.composeui)
}

android {
    namespace = "com.geecee.escapelauncher.feature.homescreen"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
    implementation(project(":core:theme"))
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":core:analytics"))
    implementation(project(":domain:time"))
    implementation(project(":feature:newwidgets"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.timber)
}
