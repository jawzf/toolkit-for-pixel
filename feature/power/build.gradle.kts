plugins {
    id("toolkit.android.library.compose")
}

android {
    namespace = "com.jawzf.pixeltoolkit.feature.power"
}

dependencies {
    implementation(project(":core:design"))
    implementation(project(":core:platform"))
    implementation(project(":core:data"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.gson)
    implementation(libs.kotlinx.coroutines.android)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3.v150alpha24)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.reorderable)
}
