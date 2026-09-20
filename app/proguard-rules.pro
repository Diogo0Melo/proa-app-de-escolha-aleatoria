# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# Gson serialization (used in Room TypeConverters)
-keep class com.ogos.apprandomizador.model.** { *; }
-keepclassmembers class com.ogos.apprandomizador.model.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**