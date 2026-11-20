plugins {
    alias(libs.plugins.escapelauncher.android.library)
    alias(libs.plugins.escapelauncher.android.testing)
}

android {
    namespace = "com.geecee.escapelauncher.core.common"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.timber)

    implementation(project(":core:model"))
}