# 项目基础 ProGuard 规则
# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Moshi
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.Json class *
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# Device Owner / QR Provisioning 入口（不可被混淆裁剪）
-keep class com.example.smart_emap.admin.SmartEmapDeviceAdminReceiver { *; }
-keep class com.example.smart_emap.admin.GetProvisioningModeActivity { *; }
-keep class com.example.smart_emap.admin.AdminPolicyComplianceActivity { *; }
-keep class com.example.smart_emap.admin.SmartEmapBootReceiver { *; }

# 项目数据模型 (Dto)
-keep class com.example.smart_emap.data.model.** { *; }
-keep @interface com.example.smart_emap.data.model.MesDefectByItem
-keep class com.example.smart_emap.data.model.MesDefectByItemAdapterFactory { *; }
-keep class com.example.smart_emap.data.model.MesDefectByItemMapAdapter { *; }
