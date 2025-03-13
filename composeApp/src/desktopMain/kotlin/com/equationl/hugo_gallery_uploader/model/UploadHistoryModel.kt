package com.equationl.hugo_gallery_uploader.model

data class UploadHistoryModel(
    val fileName: String,
    val filePath: String,
    val title: String,
    val totalPictureCount: Int = 0,
    val uploadedPictureCount: Int = 0
)
