plugins {
    alias(libs.plugins.escapelauncher.android.feature)
}

android {
    namespace = "com.geecee.escapelauncher.feature.gestures"
}

dependencies {
    implementation(project(":core:theme"))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    
    // Core Compose dependencies should be in the escapelauncher.android.feature plugin.
    // If not, they are included through other modules, or we might need to add them here.
}