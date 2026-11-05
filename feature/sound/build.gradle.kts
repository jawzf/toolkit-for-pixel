plugins {
    id("toolkit.android.library")
}

android {
    namespace = "com.jawzf.pixeltoolkit.feature.sound"
}

dependencies {
    implementation(project(":core:design"))
    implementation(project(":core:platform"))
    implementation(project(":core:data"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.gson)
    implementation(libs.shizuku.api)
    implementation(libs.hiddenapibypass)
    implementation(libs.kotlinx.coroutines.android)
}
