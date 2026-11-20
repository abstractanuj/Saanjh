package com.geecee.escapelauncher

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
class FlavoursConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.withPlugin("com.android.application") {
                extensions.findByType(ApplicationExtension::class.java)?.apply {
                    buildFeatures.buildConfig = true
                    defaultConfig {
                        buildConfigField("boolean", "IS_FOSS", "true")
                        buildConfigField("String", "APP_FLAVOUR", "\"FOSS\"")
                        resValue("string", "app_flavour", "FOSS")
                    }
                    sourceSets.getByName("main") {
                        res.directories.add("src/foss/res")
                        java.directories.add("src/foss/java")
                        kotlin.directories.add("src/foss/java")
                    }
                }
            }
            pluginManager.withPlugin("com.android.library") {
                extensions.findByType(LibraryExtension::class.java)?.apply {
                    buildFeatures.buildConfig = true
                    defaultConfig {
                        buildConfigField("boolean", "IS_FOSS", "true")
                        buildConfigField("String", "APP_FLAVOUR", "\"FOSS\"")
                    }
                    sourceSets.getByName("main") {
                        res.directories.add("src/foss/res")
                        java.directories.add("src/foss/java")
                        kotlin.directories.add("src/foss/java")
                    }
                }
            }
        }
    }
}