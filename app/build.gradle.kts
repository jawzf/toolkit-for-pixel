import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.aboutlibraries)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        optIn.addAll(
            "androidx.compose.material3.ExperimentalMaterial3Api",
            "androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
            "androidx.compose.foundation.ExperimentalFoundationApi",
            "androidx.compose.ui.text.ExperimentalTextApi",
            "androidx.compose.foundation.layout.ExperimentalLayoutApi",
        )
    }
}

android {
    namespace = "com.jawzf.pixeltoolkit"
    compileSdk = 37
    compileSdkMinor = 1

    androidResources {
        localeFilters += listOf("en")
    }

    defaultConfig {
        applicationId = "com.jawzf.pixeltoolkit"
        minSdk = 26
        targetSdk = 37
        versionCode = 69
        versionName = "20.1-beta.3"

    }

    buildTypes {
//        optimized dev build
//           debug {
//              isMinifyEnabled = true
//              isShrinkResources = true
//              isDebuggable = false
//              proguardFiles(
//                  getDefaultProguardFile("proguard-android-optimize.txt"),
//                  "proguard-rules.pro"
//              )
//           }
//        end

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        buildConfig = true
        aidl = true
    }
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
    lint {
        baseline = file("lint-baseline.xml")
    }
}

dependencies {
    implementation(project(":core:design"))
    implementation(project(":core:platform"))
    implementation(project(":core:data"))
    implementation(project(":feature:connectivity"))
    implementation(project(":feature:sound"))
    implementation(project(":feature:power"))
    compileOnly(project(":stub"))
    implementation(libs.aboutlibraries.core)
    implementation(libs.aboutlibraries.compose.m3)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigationevent)
    implementation(platform(libs.androidx.compose.bom))

    // Android 12+ SplashScreen API with backward compatibility attributes
    implementation(libs.androidx.core.splashscreen)

    // Force latest Material3 1.5.0-alpha17 for new MaterialShapes
    implementation(libs.androidx.compose.material3.v150alpha24)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.biometric)

    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.material)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.foundation)
    testImplementation(libs.junit)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Shizuku
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
    implementation("com.github.d4rken-org.porter-api:shizuku-bridge:+")
    implementation(libs.androidx.car.app)

    // Hidden API Bypass
    implementation(libs.hiddenapibypass)

    // Gson for JSON serialization
    implementation(libs.gson.v2140)
    implementation(libs.androidx.palette)

    // Reorderable library
    implementation(libs.reorderable)

    // Volume Long Press
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.api.v1315)

    // Google Maps & Location
    implementation(libs.play.services.location)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.gson)
    implementation(project(":island-sdk"))

    // Kotlin Reflect for dynamic sealed class serialization
    implementation(kotlin("reflect"))

    // SymSpell for word suggestions
    implementation(libs.symspellkt)

    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    implementation(libs.androidx.compose.material.icons.extended) // Compatible with Compose BOM

    // GSMArena Parsing
    implementation(libs.jsoup)
    implementation(libs.androidx.graphics.shapes)

    // AutoUpdater
    implementation(libs.autoupdater)

    // Media3 for Live Wallpaper & Online Help Media
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.ui)


    // Lottie for animations
    implementation(libs.lottie.compose)


    // QR Code Engine
    implementation(libs.zxing.core)
    implementation(libs.play.services.code.scanner)
}
