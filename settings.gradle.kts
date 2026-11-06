pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "ToolkitForPixel"
include(":app")
include(":stub")
include(":island-sdk")
include(":island-sdk-compose")
include(":core:design")
include(":core:platform")
include(":core:data")
include(":feature:connectivity")
include(":feature:sound")
include(":feature:power")
include(":feature:security")
include(":feature:apps")
include(":feature:input")
include(":feature:notifications")
