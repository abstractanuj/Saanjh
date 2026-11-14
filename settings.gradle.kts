pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Escape Launcher"
includeBuild("build-logic")
include(":app")
include(":core:ui")
include(":core:model")
include(":core:data")
include(":core:domain")
include(":core:common")
include(":domain:time")
include(":core:di")
include(":core:theme")
include(":core:analytics")
include(":core:cloudmessaging")
include(":feature:settings")
include(":feature:gestures")
include(":feature:widgets")
