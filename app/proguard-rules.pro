# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

##jackson
#-keep class com.fasterxml.jackson.databind.ObjectMapper {
#    public <methods>;
#    protected <methods>;
#}
#-keep class com.fasterxml.jackson.databind.ObjectWriter {
#    public ** writeValueAsString(**);
#}

## Jackson
#-keep @com.fasterxml.jackson.annotation.JsonIgnoreProperties class * { *; }
#-keep class com.fasterxml.** { *; }
#-keep class org.codehaus.** { *; }
#-keepnames class com.fasterxml.jackson.** { *; }
#-keepclassmembers public final enum com.fasterxml.jackson.annotation.JsonAutoDetect$Visibility {
#    public static final com.fasterxml.jackson.annotation.JsonAutoDetect$Visibility *;
#}
#
#-keep class com.fasterxml.jackson.annotation.** { *; }
#
#-dontwarn com.fasterxml.jackson.databind.**
#
## General
#-keepattributes SourceFile,LineNumberTable,*Annotation*,EnclosingMethod,Signature,Exceptions,InnerClasses
#
#-dontwarn org.w3c.dom.bootstrap.DOMImplementationRegistry
#
## OkHttp
#-keepattributes Signature
#-keepattributes *Annotation*
#-keep class okhttp3.** { *; }
#-keep interface okhttp3.** { *; }
#-dontwarn okhttp3.**
#
## Okio
#-keep class sun.misc.Unsafe { *; }
#-dontwarn java.nio.file.*
#-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
#-dontwarn okio.**
#
## Retrofit 2.X
### https://square.github.io/retrofit/ ##
#
#-dontwarn retrofit2.**
#-keep class retrofit2.** { *; }
#-keepattributes Signature
#-keepattributes Exceptions
#
#-keepclasseswithmembers class * {
#    @retrofit2.http.* <methods>;
#}
#
### GSON 2.2.4 specific rules ##
#
## Gson uses generic type information stored in a class file when working with fields. Proguard
## removes such information by default, so configure it to keep all of it.
#-keepattributes Signature
#
## For using GSON @Expose annotation
#-keepattributes *Annotation*
#
#-keepattributes EnclosingMethod
#
## Gson specific classes
#-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }
#
##
##-dontnote com.github.sujithkanna:smileyrating.**
##-dontnote me.dm7.barcodescanner:zxing.**
##-dontnote com.sdsmdg.tastytoast:tastytoast.**
##-dontnote com.flaviofaria:kenburnsview.**
##-dontnote com.ramotion.directselect:direct-select.**
##-dontnote com.github.devsideal:ReadMoreOption.**
##-dontnote com.github.PhilJay:MPAndroidChart.**
##-dontnote com.chaos.view:pinview.**
##-dontnote com.yarolegovich:sliding-root-nav.**
##-dontnote com.amulyakhare:com.amulyakhare.textdrawable.**
##-dontnote io.michaelrocks:libphonenumber-android.**
##-dontnote com.github.joielechong:countrycodepicker.**
##-dontnote fm.jiecao:jiecaovideoplayer.**
#
#
## Glide specific rules #
## https://github.com/bumptech/glide
#
#-keep public class * implements com.bumptech.glide.module.GlideModule
#-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
#    **[] $VALUES;
#    public *;
#}
#
##android.support.design
#-dontwarn android.support.design.**
#-keep class android.support.design.** { *; }
#-keep interface android.support.design.** { *; }
#-keep public class android.support.design.R$* { *; }
#
##android.support.v7
#-keep public class android.support.v7.widget.** { *; }
#-keep public class android.support.v7.internal.widget.** { *; }
#-keep public class android.support.v7.internal.view.menu.** { *; }
#
#-keep public class * extends android.support.v4.view.ActionProvider {
#    public <init>(android.content.Context);
#}
#
#
#
#
## http://stackoverflow.com/questions/29679177/cardview-shadow-not-appearing-in-lollipop-after-obfuscate-with-proguard/29698051
#-keep class android.support.v7.widget.RoundRectDrawable { *; }
#
## Retain generated class which implement Unbinder.
#-keep public class * implements butterknife.Unbinder { public <init>(**, android.view.View); }
#
## Prevent obfuscation of types which use ButterKnife annotations since the simple name
## is used to reflectively look up the generated ViewBinding.
#-keep class butterknife.*
#-keepclasseswithmembernames class * { @butterknife.* <methods>; }
#-keepclasseswithmembernames class * { @butterknife.* <fields>; }
#
##pl gif
#-keep public class pl.droidsonroids.gif.GifIOException{<init>(int);}
#-keep class pl.droidsonroids.gif.GifInfoHandle{<init>(long,int,int,int);}