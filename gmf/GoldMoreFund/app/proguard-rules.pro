# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/yale/Public/android-sdk/tools/proguard/proguard-android.txt
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

# start of gson

-keepclassmembers class * extends java.io.Serializable {
    <fields>;
    static <methods>;
}

# end of gson

# start of rx_android
-dontwarn rx.**
-dontnote rx.**
-keep class rx.** { *;}

# end of rx_android

# start of gmf
-dontwarn android.support.**
-dontnote android.support.**

-keep class com.goldmf.GMFund.cmd.** { *;}
-keep class com.goldmf.GMFund.** implements com.goldmf.GMFund.base.KeepClassProtocol {*;}
-keep class yale.extension.** implements com.goldmf.GMFund.base.KeepClassProtocol {*;}

# end of gmf

# start of umeng statistic service

-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}

-keep public class com.goldmf.GMFund.R$*{
public static final int *;
}

# end of umeng statistic service

# start of wechat service

-keep class com.tencent.mm.sdk.** {
   *;
}

# end of wechat service

# start of umeng share service

-dontshrink
-dontoptimize
-dontwarn com.google.android.maps.**
-dontwarn android.webkit.**
-dontwarn com.umeng.**
-dontwarn com.tencent.weibo.sdk.**
-dontwarn com.facebook.**
-dontwarn javax.**
-dontnote com.google.android.maps.**
-dontnote android.webkit.**
-dontnote com.umeng.**
-dontnote com.tencent.weibo.sdk.**
-dontnote com.facebook.**
-dontnote javax.**

-keep enum com.facebook.**
-keepattributes Exceptions,InnerClasses,Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

-keep public interface com.facebook.**
-keep public interface com.tencent.**
-keep public interface com.umeng.socialize.**
-keep public interface com.umeng.socialize.sensor.**
-keep public interface com.umeng.scrshot.**

-keep public class com.umeng.socialize.* {*;}
-keep public class javax.**
-keep public class android.webkit.**

-keep class com.facebook.**
-keep class com.facebook.** { *; }
-keep class com.umeng.scrshot.**
-keep public class com.tencent.** {*;}
-keep class com.umeng.socialize.sensor.**
-keep class com.umeng.socialize.handler.**
-keep class com.umeng.socialize.handler.*
-keep class com.tencent.mm.sdk.modelmsg.WXMediaMessage {*;}
-keep class com.tencent.mm.sdk.modelmsg.** implements com.tencent.mm.sdk.modelmsg.WXMediaMessage$IMediaObject {*;}

-keep class im.yixin.sdk.api.YXMessage {*;}
-keep class im.yixin.sdk.api.** implements im.yixin.sdk.api.YXMessage$YXMessageData{*;}

-dontwarn twitter4j.**
-keep class twitter4j.** { *; }

-keep class com.tencent.** {*;}
-dontwarn com.tencent.**
-keep public class com.umeng.soexample.R$*{
    public static final int *;
}
-keep public class com.umeng.soexample.R$*{
    public static final int *;
}
-keep class com.tencent.open.TDialog$*
-keep class com.tencent.open.TDialog$* {*;}
-keep class com.tencent.open.PKDialog
-keep class com.tencent.open.PKDialog {*;}
-keep class com.tencent.open.PKDialog$*
-keep class com.tencent.open.PKDialog$* {*;}

-keep class com.sina.** {*;}
-dontwarn com.sina.**
-dontnote com.sina.**
-keep class  com.alipay.share.sdk.** {
   *;
}
-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keep class com.linkedin.** { *; }
-keepattributes Signature

# end of umeng share service

# start of retrolambda

-dontwarn java.lang.invoke.*
-dontwarn java.nio.**
-dontwarn sun.misc.**
-dontnote java.lang.invoke.*
-dontnote java.nio.**
-dontnote sun.misc.**

# end of retrolambda

# start of jpush

-dontwarn cn.jpush.**
-keep class cn.jpush.** { *;}

-dontwarn com.google.**
-keep class com.google.protobuf.** {*;}


# end of jpush

# start of okhttp

-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn org.codehaus.**
-dontnote org.codehaus.**

# end of okhttp

# start of qiniu

-dontwarn com.qiniu.android.**
-dontwarn com.loopj.android.http.**
-keep class com.qiniu.android.** { *; }
-keep class com.loopj.android.http.** { *; }

# end of qiniu
