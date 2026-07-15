# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles settings in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Adjust optimization settings if necessary.
# By default, optimization is enabled.
# To disable optimization, uncomment the following line:
# -dontoptimize

# Keep database entities and DAOs safe from shrinking/obfuscation (Room)
-keep class com.me.moneytracker.data.** { *; }

# Keep domain models and serialized models intact
-keepclassmembers class * {
    @androidx.room.Entity *;
    @androidx.room.Dao *;
    @androidx.room.Query *;
}

# Keep Compose rules
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}
