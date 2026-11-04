plugins {
    id("toolkit.android.library")
}

android {
    namespace = "com.jawzf.pixeltoolkit.feature.connectivity"
}

dependencies {
    implementation(project(":core:design"))
    implementation(project(":core:platform"))
    implementation(project(":core:data"))
    compileOnly(project(":stub"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.gson)
    implementation(libs.shizuku.api)
    implementation(libs.hiddenapibypass)
}
