# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Gson rules
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Ensure @Keep annotations are always honored
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Shizuku rules
-keep class rikka.shizuku.** { *; }
-keep class dev.rikka.shizuku.** { *; }

# Kotlin Reflect
-keep class kotlin.reflect.** { *; }
-keep class com.jawzf.pixeltoolkit.domain.model.** { *; }
-keep class com.jawzf.pixeltoolkit.domain.diy.** { *; }

# Prevent over-minification of settings and registry classes
-keep class com.jawzf.pixeltoolkit.data.repository.** { *; }
-keep class com.jawzf.pixeltoolkit.domain.registry.** { *; }

# Keep IME related models and suggestion logic
-keep class com.jawzf.pixeltoolkit.ime.** { *; }
-keep class com.jawzf.pixeltoolkit.ui.ime.** { *; }
-keepclassmembers class com.jawzf.pixeltoolkit.ime.** { *; }
-keepclassmembers class com.jawzf.pixeltoolkit.ui.ime.** { *; }

# Data models for Gson
-keep class com.jawzf.pixeltoolkit.data.model.** { *; }
-keepclassmembers class com.jawzf.pixeltoolkit.data.model.** { *; }

# Keep ViewModel constructors for reflection-based instantiation
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

# Ensure anonymous TypeToken subclasses (used for GSON generic lists) are kept
-keepclassmembers class * extends com.google.gson.reflect.TypeToken {
    protected <init>(...);
}

# SLF4J logging rules (prevent R8 missing class warnings)
-dontwarn org.slf4j.**
-keep class org.slf4j.** { *; }

# Keep R.string class and fields for runtime translation key reflection lookup
-keep class com.jawzf.pixeltoolkit.R$string { *; }
-keepclassmembers class com.jawzf.pixeltoolkit.R$string {
    public static <fields>;
}
-keep class com.jawzf.pixeltoolkit.domain.genai.** { *; }
-keep class com.jawzf.pixeltoolkit.appfunctions.** { *; }
-keep class androidx.appfunctions.** { *; }

