package com.equationl.hugo_gallery_uploader.model

import java.io.File
import java.util.Date

/**
* TODO 加载后的图片模型
* */
data class PictureModel(
    val file: File,
    val title: String? = null,
    val shotDate: Date? = null,
    val cameraText: String? = null,
    val lensText: String? = null,
    val focalLengthText: String? = null,
    val isoText: String? = null,
    val exposureTimeText: String? = null,
    val apertureText: String? = null,
    val remoteUrl: String? = null,
)
