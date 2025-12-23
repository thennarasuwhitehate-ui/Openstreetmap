# NaviBharat ProGuard Rules

# Keep Android components
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep Hilt generated code
-keep class dagger.hilt.** { *; }
-keep class **_Factory { *; }
-keep class **_MembersInjector { *; }

# Keep Room database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *

# Keep Retrofit
-keepattributes Signature
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Keep Gson
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# Keep Firebase
-keep class com.google.firebase.** { *; }
-keep class com.firebase.** { *; }

# Keep Google Maps
-keep class com.google.android.gms.maps.** { *; }

# Keep Supabase
-keep class io.github.supabase.** { *; }
-keep class io.ktor.** { *; }

# Keep serialization classes
-keep class kotlinx.serialization.** { *; }

# Keep annotations
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.SerializersKt

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
