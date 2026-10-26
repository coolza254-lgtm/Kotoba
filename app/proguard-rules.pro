# R8 Optimizations for Jetpack Compose & Kotoba
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-keep class com.kotoba.app.data.model.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
