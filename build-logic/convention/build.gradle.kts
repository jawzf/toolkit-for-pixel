plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "toolkit.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
    }
}
