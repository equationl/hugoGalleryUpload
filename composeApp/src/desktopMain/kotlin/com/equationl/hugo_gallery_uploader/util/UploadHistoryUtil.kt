package com.equationl.hugo_gallery_uploader.util

import com.equationl.hugo_gallery_uploader.model.PictureModel
import com.equationl.hugo_gallery_uploader.model.UploadHistoryModel
import com.equationl.hugo_gallery_uploader.util.JsonUtils.fromJsonList
import com.equationl.hugo_gallery_uploader.util.JsonUtils.toJson
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

object UploadHistoryUtil {
    private const val SAVE_UPLOAD_HISTORY_DIR = "upload_history/"
    private const val SAVE_FILE_EXT = "json"

    fun saveUploadHistory(data: List<PictureModel>) {
        try {
            val savePath = getSavePath()
            val saveString = data.toJson()

            val file = File(savePath, "${System.currentTimeMillis()}.$SAVE_FILE_EXT")
            if (!file.exists()) file.createNewFile()
            file.writeText(saveString)
        } catch (tr: Throwable) {
            println("saveUploadHistory error: ${tr.stackTraceToString()}")
        }
    }

    fun getUploadHistoryList(): List<UploadHistoryModel> {
        val savePath = getSavePath()
        val result = mutableListOf<UploadHistoryModel>()
        savePath.walk()
            .filter { it.isFile }
            .filter { it.extension.lowercase() == SAVE_FILE_EXT }
            .sortedByDescending { it.nameWithoutExtension.toLongOrNull() ?: 0 }
            .forEach { file -> result.add(file.toUploadHistoryModel()) }
        return result
    }

    fun getUploadHistory(name: String): List<PictureModel>? {
        val savePath = getSavePath()
        val file = File(savePath, name)
        if (file.exists()) {
            val text = file.readText()
            return text.fromJsonList(PictureModel::class.java)
        }
        return null
    }

    fun deleteUploadHistory(name: String) {
        try {
            val savePath = getSavePath()
            val file = File(savePath, name)
            if (file.exists()) {
                file.delete()
            }
        } catch (tr: Throwable) {
            println("deleteUploadHistory error: ${tr.stackTraceToString()}")
        }
    }

    private fun File.toUploadHistoryModel(): UploadHistoryModel {
        val title = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(nameWithoutExtension.toLongOrNull() ?: 0)

        return UploadHistoryModel(name, path, title)
    }


    private fun getSavePath(): File {
        val userDataDir = UserDataDir.getAppPath().toString()
        val file = File(userDataDir, SAVE_UPLOAD_HISTORY_DIR)
        if (!file.exists()) {
            file.mkdirs()
        }

        return file
    }
}