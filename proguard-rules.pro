-keepattributes LineNumberTable,SourceFile
-keepnames class com.immrhy.learningandroidapptool.** { *; }
-keep class com.immrhy.learningandroidapptool.fermata.auto.** { *; }
-keep class org.videolan.libvlc.** { *; }
-keep class com.immrhy.learningandroidapptool.fermata.vfs.sftp.** { *; }
-keep class com.immrhy.learningandroidapptool.fermata.vfs.smb.** { *; }
-keep class com.immrhy.learningandroidapptool.fermata.vfs.gdrive.** { *; }
-keep class androidx.car.app.** { *; }
-keep class org.chromium.net.impl.NativeCronetEngineBuilderImpl { *; }

-dontwarn com.sun.jna.platform.win32.**
-dontwarn com.jcraft.jsch.PageantConnector
-dontwarn okio.*

-keepnames class androidx.media3.exoplayer.ExoPlayerImpl { *; }
-keepnames class androidx.media3.exoplayer.ExoPlayerImplInternal { *; }
