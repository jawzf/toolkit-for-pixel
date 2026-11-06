plugins {
    id("toolkit.android.library")
}

android {
    namespace = "com.jawzf.pixeltoolkit.feature.tiles"
}

dependencies {
    implementation(project(":core:design"))
    implementation(project(":core:platform"))
    implementation(project(":core:data"))
    implementation(libs.androidx.core.ktx)
}
