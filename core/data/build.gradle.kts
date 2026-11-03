plugins {
    id("toolkit.android.library")
}

android {
    namespace = "com.jawzf.pixeltoolkit.core.data"
}

dependencies {
    implementation(project(":core:platform"))
    implementation(libs.gson)
}
