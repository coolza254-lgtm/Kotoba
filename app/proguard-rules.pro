# ProGuard / R8 rules for Kotoba

# Keep Gson serialization & deserialization intact
-keepattributes Signature
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.** { *; }

# Keep all data models, DAOs, and database classes
-keep class com.kotoba.app.data.** { *; }
-keepclassmembers class com.kotoba.app.data.** { *; }
-keep class com.kotoba.app.updater.** { *; }
-keepclassmembers class com.kotoba.app.updater.** { *; }

# Keep Room SQLite
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

-dontwarn okhttp3.**
-dontwarn okio.**
