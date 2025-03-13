package com.equationl.hugo_gallery_uploader.util

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.CachePolicy
import coil3.util.DebugLogger
import com.equationl.hugo_gallery_uploader.constant.DataKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.io.File
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

object ImageUtil {
    private const val IMAGE_CACHE_DIR_NAME = "cache/"

    // 定义图片缓存目录
    private val cacheDir by lazy {
        val path = File(UserDataDir.getAppPath().toString(), IMAGE_CACHE_DIR_NAME)
        if (!path.exists()) {
            path.mkdirs()
        }
        path
    }
    
    // 设置缓存大小为 10GB
    private val cacheSize = 10 * 1024 * 1024 * 1024L
    
    // 创建磁盘缓存
    private val diskCache by lazy {
        DiskCache.Builder()
            .directory(cacheDir)
            .maxSizeBytes(cacheSize)
            .build()
    }
    
    val globalImageLoader by lazy {
        // 从配置中读取 Referer 设置
        val (enableReferer, refererUrl) = runBlocking {
            val preferences = dataStore.data.first()
            val enabled = preferences[DataKey.ENABLE_IMAGE_REFERER] ?: false
            val url = preferences[DataKey.IMAGE_REFERER_URL] ?: ""
            Pair(enabled, url)
        }

        // 创建一个 OkHttp 拦截器，为所有 HTTP 请求添加 Referer 头
        val refererInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            // 只为 HTTP 请求添加 Referer 头，且只在启用了防盗链设置时添加
            if (enableReferer && refererUrl.isNotBlank() && originalRequest.url.toString().startsWith("http")) {
                val newRequest = originalRequest.newBuilder()
                    .addHeader("Referer", refererUrl)
                    .build()
                chain.proceed(newRequest)
            } else {
                chain.proceed(originalRequest)
            }
        }

        // 创建带有拦截器的 OkHttpClient
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(refererInterceptor)
            .build()

        ImageLoader.Builder(PlatformContext.INSTANCE)
            .components {
                add(OkHttpNetworkFetcherFactory(okHttpClient))
            }
            .diskCache(diskCache)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .logger(DebugLogger())
            .build()
    }
    
    /**
     * 获取缓存大小的格式化字符串
     */
    fun getImageCacheSize(): String {
        println("cacheDir = $cacheDir")
        val cacheSize = calculateDirSize(cacheDir)
        return formatFileSize(cacheSize)
    }
    
    /**
     * 清除图片缓存
     */
    fun clearImageCache() {
        try {
            // 清除Coil的磁盘缓存
            diskCache.clear()
            
            // 同时也清除缓存目录中可能残留的文件
            deleteDirectoryContents(cacheDir)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 计算目录大小
     */
    private fun calculateDirSize(dir: File): Long {
        if (!dir.exists()) return 0
        
        var size = 0L
        val files = dir.listFiles() ?: return 0
        
        for (file in files) {
            size += if (file.isDirectory) {
                calculateDirSize(file)
            } else {
                file.length()
            }
        }
        
        return size
    }
    
    /**
     * 格式化文件大小
     */
    private fun formatFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        
        val formatter = DecimalFormat("#,##0.#")
        return formatter.format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
    }
    
    /**
     * 删除目录内容
     */
    private fun deleteDirectoryContents(directory: File) {
        if (!directory.exists() || !directory.isDirectory) return
        
        val files = directory.listFiles() ?: return
        
        for (file in files) {
            if (file.isDirectory) {
                deleteDirectoryContents(file)
            }
            file.delete()
        }
    }
}