# To enable ProGuard in your project, edit project.properties
# to define the proguard.config property as described in that file.
#
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-ignorewarnings

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}


-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService

-keepattributes Signature
-keep class com.google.** { *; }

-keepclasseswithmembers  class * {
    native <methods>;
}

-keepclasseswithmembers  class * {
    public <init>(android.content.Context);
}

-keepclasseswithmembers  class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers  class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class com.google.gson.** {*;}
-keep class android.support.v4.** {*;}



-dontwarn com.baidu.**
-dontwarn com.google.android.gms.**
#-dontwarn com.example.load.db.**
-dontwarn com.csr.btsmart.**
-dontwarn org.apache.commons.lang3.**
-dontwarn  org.apache.commons.codec.**
#-dontobfuscate

-keep class com.google.android.gms.** {*;}
-keep class com.baidu.** { *; }
-keep class vi.com.gdi.bgl.android.**{*;}
-keep class com.csr.btsmart.** { *; }
#-keep class com.example.load.dbs.** {*;}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}
-keep class * implements android.os.Parcelable{*;}
-keep class * implements java.io.Serializable{*;}
-keepclassmembers class * extends android.database.sqlite.SQLiteOpenHelper {
    public protected private <fields>;
    public protected <methods>;
}
#-keep class * implements java.io.Serializable{*;}
#umeng #########################################
-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}
-keep public class com.umeng.fb.ui.ThreadView {
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
# 如果仍存在问题，可以再做如下操作：
# 添加第三方jar包
#-libraryjars libs/umeng-sdk.jar
# 以下类过滤不混淆
#-keep public class * extends com.umeng.**
# 以下包不进行过滤
#-keep class com.umeng.** { *; }
#umeng #########################################


# umeng share 混淆
-dontshrink
-dontoptimize
-dontwarn com.google.android.maps.**
-dontwarn android.webkit.WebView
-dontwarn com.umeng.**
-dontwarn com.tencent.weibo.sdk.**
-dontwarn com.facebook.**

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
-keep class com.umeng.scrshot.**
-keep public class com.tencent.** {*;}
-keep class com.umeng.socialize.sensor.**

-keep class com.tencent.mm.sdk.modelmsg.WXMediaMessage {*;}

-keep class com.tencent.mm.sdk.modelmsg.** implements com.tencent.mm.sdk.modelmsg.WXMediaMessage$IMediaObject {*;}

-keep class im.yixin.sdk.api.YXMessage {*;}
-keep class im.yixin.sdk.api.** implements im.yixin.sdk.api.YXMessage$YXMessageData{*;}

-keep class cn.sharesdk.**{*;}
-keep class com.sina.**{*;}
-keep class **.R$* {*;}
-keep class **.R{*;}
-dontwarn cn.sharesdk.**
-dontwarn **.R$*
#####################################


#########leancloud###############################
-keepattributes Signature
-dontwarn com.jcraft.jzlib.**
-keep class com.jcraft.jzlib.**  { *;}

-dontwarn sun.misc.**
-keep class sun.misc.** { *;}

-dontwarn com.alibaba.fastjson.**
-keep class com.alibaba.fastjson.** { *;}

-dontwarn sun.security.**
-keep class sun.security.** { *; }

-dontwarn com.google.**
-keep class com.google.** { *;}


-keep public class android.net.http.SslError
-keep public class android.webkit.WebViewClient

-dontwarn android.webkit.WebView
-dontwarn android.net.http.SslError
-dontwarn android.webkit.WebViewClient

-dontwarn android.support.**

-dontwarn org.apache.**
-keep class org.apache.** { *;}

-dontwarn org.jivesoftware.smack.**
-keep class org.jivesoftware.smack.** { *;}

-dontwarn com.loopj.**
-keep class com.loopj.** { *;}

-dontwarn org.xbill.**
-keep class org.xbill.** { *;}

-keepattributes *Annotation*
########################################




-keepattributes Signature


##########zxing###########################
-dontwarn com.google.zxing.** -keep class com.google.zxing.**{*;}
-keepclasseswithmembernames class * { native <methods>; }

-keepattributes *Annotation*
-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}

# keep setters in Views so that animations can still work.
# see http://proguard.sourceforge.net/manual/examples.html#beans
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

# We want to keep methods in Activity that could be used in the XML attribute onClick
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.**
######################################




#########okhttp#############################
-dontwarn com.squareup.okhttp.**
-keep class com.squareup.okhttp.** { *;}
-dontwarn okio.**
#########okhttp#############################




#########支付宝#############################
-dontwarn com.alipay.**
-keep class com.alipay.** { *;}
#########支付宝#############################


#########qq登陆#############################
-keep class com.tencent.open.TDialog$*
-keep class com.tencent.open.TDialog$* {*;}
-keep class com.tencent.open.PKDialog
-keep class com.tencent.open.PKDialog {*;}
-keep class com.tencent.open.PKDialog$*
-keep class com.tencent.open.PKDialog$* {*;}
#mta统计
-keep class com.tencent.stat.**  {* ;}
-keep class com.tencent.mid.**  {* ;}
######################################


#########百川和趣拍#############################
-keep class sun.misc.Unsafe { *; }
-keep class com.taobao.** {*;}
-keep class com.alibaba.** {*;}
-keep class com.alipay.** {*;}
-dontwarn com.taobao.**
-dontwarn com.alibaba.**
-dontwarn com.alipay.**
-keep class com.ut.** {*;}
-dontwarn com.ut.**
-keep class com.ta.** {*;}
-dontwarn com.ta.**
-dontwarn com.alibaba.**
-keep class com.alibaba.**
-keepclassmembers class com.alibaba.** {
    *;
}
-keep class com.taobao.**
-keepclassmembers class com.taobao.** {
    *;
}

-dontwarn com.google.common.**
-dontwarn com.fasterxml.jackson.**
-dontwarn com.amap.api.**
-dontwarn net.jcip.annotations.**

-keepattributes Annotation,EnclosingMethod,Signature
-keep class com.fasterxml.jackson.**
-keepclassmembers class com.fasterxml.jackson.** {
    *;
}

-keep class com.duanqu.**
-keepclassmembers class com.duanqu.** {
    *;
}
#########qq登陆#############################



###########alimama广告联盟#################################
-dontwarn android.taobao.**
-dontwarn com.taobao.**
-dontwarn com.alimama.mobile.**

-keep class com.taobao.** {*; }
-keep class com.alimama.mobile.**{*; }
-keep class android.taobao.** {*; }
############################################








###################jtds#########################
-keep class net.sourceforge.jtds.jdbc.** { *; }
-keep class net.sourceforge.jtds.jdbc.cache.** { *; }
-keep class net.sourceforge.jtds.jdbcx.** { *; }
-keep class net.sourceforge.jtds.jdbcx.proxy.** { *; }
-keep class net.sourceforge.jtds.ssl.** { *; }
-keep class net.sourceforge.jtds.util.** { *; }
############################################


###################TalkingData#########################
-dontwarn com.tendcloud.tenddata.**
-keep class com.tendcloud.** {*;}
-keep public class com.tendcloud.tenddata.** { public protected *;}
-keepclassmembers class com.tendcloud.tenddata.**{
public void *(***);
}
-keep class com.talkingdata.sdk.TalkingDataSDK {public *;}
-keep class com.apptalkingdata.** {*;}
-keep class dice.** {*; }
-dontwarn dice.**
###################jtds#########################


-keep class com.google.android.material.** {*;}
-keep class androidx.** {*;}
-keep public class * extends androidx.**
-keep interface androidx.** {*;}
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**
-dontwarn androidx.**


-keep class org.greenrobot.greendao.**{*;}
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {     public static java.lang.String TABLENAME; }
-keep class **$Properties{*;}