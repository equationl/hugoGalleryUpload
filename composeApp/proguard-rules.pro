# 启用混淆，但不优化
# -dontoptimize

# 忽略所有警告
-dontwarn **

-keep class com.equationl.hugo_gallery_uploader.** { *; }

-keep class coil3.network.okhttp.internal.** { *; }

-keep class org.apache.logging.log4j.** { *; }

-keep class com.obs.** { *; }