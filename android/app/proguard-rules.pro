# Proguard & R8 Optimization Rules for Bangladesh Electricity Bill Calculator

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-dontwarn javax.annotation.**

# Room Database
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public void <init>();
}
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Gson Serialization (Backup & Restore data models)
-keepclassmembers enum * { *; }
-keep class com.bangla.electricitybill.data.backup.** { *; }
-keep class com.bangla.electricitybill.data.entity.** { *; }
-keep class com.bangla.electricitybill.domain.model.** { *; }

# Dagger Hilt
-keep class * extends android.app.Application
-keep class * extends android.app.Activity
-keep class * extends androidx.fragment.app.Fragment
-keep class * extends androidx.lifecycle.ViewModel
-dontwarn dagger.hilt.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# Strip unnecessary debug logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}
