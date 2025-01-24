package com.equationl.hugo_gallery_uploader.model

import java.io.File
import java.util.Date

/**
* 加载后的图片模型
* */
data class PictureModel(
    val file: File,
    var title: String? = null,
    val shotDate: Date? = null,
    val cameraText: String? = null,
    val lensText: String? = null,
    val focalLengthText: String? = null,
    val isoText: String? = null,
    val exposureTimeText: String? = null,
    val apertureText: String? = null,
    var remoteUrl: String? = null,
    var imgWidth: Int? = null,
    var imgHeight: Int? = null
) {
    override fun equals(other: Any?): Boolean {
        return if (other is PictureModel) {
            file.path == other.file .path
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        var result = file.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (shotDate?.hashCode() ?: 0)
        result = 31 * result + (cameraText?.hashCode() ?: 0)
        result = 31 * result + (lensText?.hashCode() ?: 0)
        result = 31 * result + (focalLengthText?.hashCode() ?: 0)
        result = 31 * result + (isoText?.hashCode() ?: 0)
        result = 31 * result + (exposureTimeText?.hashCode() ?: 0)
        result = 31 * result + (apertureText?.hashCode() ?: 0)
        result = 31 * result + (remoteUrl?.hashCode() ?: 0)
        result = 31 * result + (imgWidth ?: 0)
        result = 31 * result + (imgHeight ?: 0)
        return result
    }
}
