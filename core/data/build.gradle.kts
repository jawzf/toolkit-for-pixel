plugins {
    id("toolkit.android.library.compose")
}

android {
    namespace = "com.jawzf.pixeltoolkit.core.data"
}

dependencies {
    implementation(project(":core:platform"))
    implementation(project(":core:design"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3.v150alpha24)
    implementation(libs.androidx.core.ktx)
    implementation(libs.gson)
    implementation(kotlin("reflect"))
}
