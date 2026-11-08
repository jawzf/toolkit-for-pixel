plugins {
    id("toolkit.android.library.compose")
}

android {
    namespace = "com.jawzf.pixeltoolkit.feature.island"
}

dependencies {
    implementation(project(":core:design"))
    implementation(project(":core:platform"))
    implementation(project(":core:data"))
    implementation(project(":island-sdk"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.gson)
    implementation(libs.androidx.palette)
    implementation(libs.hiddenapibypass)
    implementation(libs.kotlinx.coroutines.android)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3.v150alpha24)
}
