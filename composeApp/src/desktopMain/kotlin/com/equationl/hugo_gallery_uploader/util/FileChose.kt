package com.equationl.hugo_gallery_uploader.util

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragData
import androidx.compose.ui.draganddrop.dragData
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifIFD0Directory
import com.drew.metadata.exif.ExifSubIFDDirectory
import com.drew.metadata.jpeg.JpegDirectory
import com.drew.metadata.png.PngDirectory
import com.equationl.hugo_gallery_uploader.model.PictureModel
import java.io.File
import java.net.URI
import java.util.TimeZone
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.filechooser.FileNameExtensionFilter


val legalSuffixList: Array<String> = arrayOf("jpg", "jpeg", "png")

fun showFileSelector(
    suffixList: Array<String> = legalSuffixList,
    isMultiSelection: Boolean = true,
    selectionMode: Int = JFileChooser.FILES_AND_DIRECTORIES, // 可以选择目录和文件
    selectionFileFilter: FileNameExtensionFilter? = FileNameExtensionFilter("图片(${legalSuffixList.contentToString()})", *suffixList), // 文件过滤
    onFileSelected: (Array<File>) -> Unit,
    ) {
    JFileChooser().apply {
        try {
            val lookAndFeel = UIManager.getSystemLookAndFeelClassName()
            UIManager.setLookAndFeel(lookAndFeel)
            SwingUtilities.updateComponentTreeUI(this)
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        fileSelectionMode = selectionMode
        isMultiSelectionEnabled = isMultiSelection
        fileFilter = selectionFileFilter

        val result = showOpenDialog(ComposeWindow())
        if (result == JFileChooser.APPROVE_OPTION) {
            if (isMultiSelection) {
                onFileSelected(this.selectedFiles)
            }
            else {
                val resultArray = arrayOf(this.selectedFile)
                onFileSelected(resultArray)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
fun dropAndDragTarget(onFileDrop: (List<String>) -> Unit): DragAndDropTarget {
    return object : DragAndDropTarget {
        override fun onDrop(event: DragAndDropEvent): Boolean {
            val data = event.dragData()
            if (data is DragData.FilesList) {
                val rawFileList = data.readFiles().map { URI(it).path }
                onFileDrop(rawFileList)
                return true
            }
            else {
                println("Not support drag data: ${data::class.java.simpleName}")
                return false
            }
        }
    }
}

fun filterFileList(
    fileList: List<String>,
    timeZoneID: String,
    onProgress: ((msg: String) -> Unit)? = null
): List<PictureModel> {
    val newFile = mutableListOf<File>()
    fileList.map {path ->
        newFile.add(File(path))
    }

    return filterFileList(newFile.toTypedArray(), timeZoneID, onProgress)
}

fun filterFileList(
    fileList: Array<File>,
    timeZoneID: String,
    onProgress: ((msg: String) -> Unit)? = null
): List<PictureModel> {
    val newFileList = mutableListOf<PictureModel>()

    for (file in fileList) {
        if (file.isDirectory) {
            newFileList.addAll(getAllFile(file, timeZoneID, onProgress))
        }
        else {
            if (file.extension.lowercase() in legalSuffixList) {
                onProgress?.invoke("读取文件 $file")
                newFileList.add(file.toPictureModelFromFile(timeZoneID, onProgress))
            }
        }
    }

    return newFileList
}

private fun getAllFile(
    file: File,
    timeZoneID: String,
    onProgress: ((msg: String) -> Unit)? = null
): List<PictureModel> {
    val newFileList = mutableListOf<PictureModel>()
    val fileTree = file.walk()
    fileTree.maxDepth(Int.MAX_VALUE)
        .filter { it.isFile }
        .filter { it.extension.lowercase() in legalSuffixList }
        .forEach {
            onProgress?.invoke("读取文件 $it")
            newFileList.add(it.toPictureModelFromFile(timeZoneID, onProgress))
        }

    return newFileList
}

private fun File.toPictureModelFromFile(
    timeZoneID: String,
    onProgress: ((msg: String) -> Unit)?
): PictureModel {
    try {
        val timeZone = TimeZone.getTimeZone(timeZoneID)
        val metadata = ImageMetadataReader.readMetadata(this)

        // png 没有 exif
        if (this.extension.lowercase() == "png") {
            val pngDirectory = metadata.getFirstDirectoryOfType(PngDirectory::class.java)
            val width = pngDirectory.getString(PngDirectory.TAG_IMAGE_WIDTH).toIntOrNull()
            val height = pngDirectory.getString(PngDirectory.TAG_IMAGE_HEIGHT).toIntOrNull()

            return PictureModel(this, title = this.name, imgWidth = width, imgHeight = height)
        }
        else {
            // 读取 exif 信息
            val subIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
            val ifdD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory::class.java)
            val jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory::class.java)

            val date = subIFDDirectory?.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, timeZone)
            val camera = "${ifdD0Directory?.getString(ExifIFD0Directory.TAG_MAKE) ?: ""} ${ifdD0Directory?.getString(ExifIFD0Directory.TAG_MODEL) ?: ""}"
            val exposureTime = subIFDDirectory?.getString(ExifSubIFDDirectory.TAG_EXPOSURE_TIME) ?: ""
            val aperture = subIFDDirectory?.getString(ExifSubIFDDirectory.TAG_FNUMBER) ?: ""
            val iso = subIFDDirectory?.getString(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT) ?: ""
            val focalLength = subIFDDirectory?.getString(ExifSubIFDDirectory.TAG_FOCAL_LENGTH) ?: ""
            val lens = subIFDDirectory?.getString(ExifIFD0Directory.TAG_LENS_MODEL) ?: ""

            val width = jpegDirectory.imageWidth
            val height = jpegDirectory.imageHeight

            return PictureModel(
                this,
                title = this.name,
                shotDate = date,
                cameraText = camera,
                exposureTimeText = if (exposureTime.isNotBlank()) "${exposureTime}sec" else "",
                apertureText =  if (aperture.isNotBlank()) "f/${aperture}" else "",
                isoText = if (iso.isNotBlank()) "ISO$iso" else "",
                focalLengthText = if (focalLength.isNotBlank()) "${focalLength}mm" else "",
                lensText = lens,
                imgWidth = width,
                imgHeight = height
            )
        }
    } catch (tr: Throwable) {
        tr.printStackTrace()
        onProgress?.invoke("获取 [${this}] 信息出错： ${tr.message}")
        return PictureModel(this, title = this.name)
    }
}