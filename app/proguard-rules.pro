# Autografr ProGuard Rules

# Firebase
-keepattributes Signature
-keepattributes *Annotation*

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep domain models for Firestore
-keep class com.autografr.app.data.remote.dto.** { *; }
