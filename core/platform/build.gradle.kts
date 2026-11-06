plugins {
    id("toolkit.android.library")
}

android {
    namespace = "com.jawzf.pixeltoolkit.core.platform"
}

dependencies {
    implementation(project(":core:design"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.car.app)
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
}
