package com.equationl.hugo_gallery_uploader.util

import androidx.compose.ui.awt.ComposeWindow
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifSubIFDDirectory
import com.drew.metadata.jpeg.JpegDirectory
import com.equationl.hugo_gallery_uploader.model.PictureModel
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetDropEvent
import java.io.File
import java.util.*
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

fun dropFileTarget(
    onFileDrop: (List<String>) -> Unit
): DropTarget {
    return object : DropTarget() {
        override fun drop(event: DropTargetDropEvent) {

            event.acceptDrop(DnDConstants.ACTION_REFERENCE)
            val dataFlavors = event.transferable.transferDataFlavors
            dataFlavors.forEach {
                if (it == DataFlavor.javaFileListFlavor) {
                    val list = event.transferable.getTransferData(it) as List<*>

                    val pathList = mutableListOf<String>()
                    list.forEach { filePath ->
                        pathList.add(filePath.toString())
                    }
                    onFileDrop(pathList)
                }
            }
            event.dropComplete(true)
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
            return PictureModel(this, title = this.name)
        }
        else {
            // 读取 exif 信息
            val exifDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
            val date = exifDirectory?.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, timeZone)

            val jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory::class.java)
            if (jpegDirectory == null) {
                onProgress?.invoke("获取 [${this}] 信息出错2： 无法读取到任何元数据")
                return PictureModel(this, title = this.name)
            }
            else {
                // TODO 读取图片信息
                return PictureModel(
                    this,
                    title = this.name
                )
            }
        }
    } catch (tr: Throwable) {
        tr.printStackTrace()
        onProgress?.invoke("获取 [${this}] 信息出错： ${tr.message}")
        return PictureModel(this, title = this.name)
    }
}