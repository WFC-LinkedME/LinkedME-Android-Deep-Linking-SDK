# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/LinkedME06/Library/Android/android-sdk-mac/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

################common###############

-dontoptimize
-ignorewarnings
-verbose
#-dontshrink
#-dontwarn
-keepattributes Signature
-keepattributes EnclosingMethod
-keepattributes InnerClasses

-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-keepparameternames

#!!!!!打包的时候将下面三行注释打开
#-libraryjars /Users/LinkedME06/Library/Android/android-sdk-mac/platforms/android-23/android.jar
#-libraryjars /Users/LinkedME06/Library/Android/android-sdk-mac/extras/android/m2repository/com/android/support/support-v4/23.2.0/support-v4-23.2.0-sources.jar
#-libraryjars /Users/LinkedME06/Library/Android/android-sdk-mac/extras/android/support/annotations/android-support-annotations.jar

-keepattributes *Annotation*
-keepattributes Exceptions
-dontwarn android.support.**
-keep class android.support.** { *; }

# Preserve all View implementations, their special context constructors, and
# their setters.
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
    public void get*(...);
}

# Preserve all fundamental application classes.
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends java.lang.Throwable {*;}
-keep public class * extends java.lang.Exception {*;}

#link-page start
-keep class com.microquation.linkedme.android.callback.** {*; }
-keep class com.microquation.linkedme.android.log.** {*; }
-keep class com.microquation.linkedme.android.v4.** {*; }
-keep class com.microquation.linkedme.android.indexing.LMUniversalObject {
*;
}
-keep class com.microquation.linkedme.android.indexing.LMUniversalObject$CONTENT_INDEX_MODE {
*;
}
-keep class com.microquation.linkedme.android.log.LMErrorCode{
*;
}
-keep class com.microquation.linkedme.android.referral.PrefHelper{
public <methods>;
}
-keep class com.microquation.linkedme.android.util.LinkProperties {
*;
}
-keep class * implements com.microquation.linkedme.android.callback.LMUniversalReferralInitListener{
*;
}
-keep class com.microquation.linkedme.android.LinkedME {
public <fields>;
public <methods>;
}
-keep class com.microquation.linkedme.android.moniter.LMTracking {
*;
}
#link-page end