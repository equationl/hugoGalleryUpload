package com.equationl.hugo_gallery_uploader.state

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.type
import androidx.datastore.preferences.core.edit
import com.equationl.hugo_gallery_uploader.constant.DataKey
import com.equationl.hugo_gallery_uploader.constant.DefaultValue
import com.equationl.hugo_gallery_uploader.model.PictureModel
import com.equationl.hugo_gallery_uploader.util.ObsUtil
import com.equationl.hugo_gallery_uploader.util.dataStore
import com.equationl.hugo_gallery_uploader.util.filterFileList
import com.equationl.hugo_gallery_uploader.util.showFileSelector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun rememberApplicationState(
    scope: CoroutineScope,
    dialogScrollState: ScrollState
) = remember {
    ApplicationState(scope, dialogScrollState)
}


class ApplicationState(val scope: CoroutineScope, val dialogScrollState: ScrollState) {

    val controlState = ControlState()
    val imgPreviewState = ImgPreviewState()

    var pictureFileList = mutableStateListOf<PictureModel>()
    var dialogText by mutableStateOf("")

    var windowShowPicture: File? by mutableStateOf(null)
        private set
    var isShowDialog by mutableStateOf(false)
        private set
    var isDialogCloseable by mutableStateOf(true)
        private set


    fun onKeyEvent(keyEvent: KeyEvent): Boolean {

        if (keyEvent.type == KeyEventType.KeyDown) {
            when (keyEvent.key.nativeKeyCode) {
                37 -> { // 向左
                    minImgIndex()
                }

                38 -> { // 向上箭头
                    minImgIndex()
                }

                39 -> { // 向右
                    plusImgIndex()
                }

                40 -> { // 向下箭头
                    plusImgIndex()
                }
            }
        }

        return false
    }

    private fun minImgIndex() {
        if (pictureFileList.isNotEmpty()) {
            if (imgPreviewState.showImageIndex == 0) {
                imgPreviewState.showImageIndex = pictureFileList.lastIndex
            } else {
                imgPreviewState.showImageIndex--
            }

            scope.launch {
                imgPreviewState.draggableState.listState.animateScrollToItem(imgPreviewState.showImageIndex)
            }
        }
    }

    private fun plusImgIndex() {
        if (pictureFileList.isNotEmpty()) {
            if (imgPreviewState.showImageIndex == pictureFileList.lastIndex) {
                imgPreviewState.showImageIndex = 0
            } else {
                imgPreviewState.showImageIndex++
            }

            scope.launch {
                imgPreviewState.draggableState.listState.animateScrollToItem(imgPreviewState.showImageIndex)
            }
        }
    }

    fun onClickImgChoose() {
        showFileSelector(
            onFileSelected = {
                scope.launch(Dispatchers.IO) {
                    showDialog("正在读取文件……", false)
                    val oldSize = pictureFileList.size
                    val newPictureList = mutableSetOf<PictureModel>()
                    newPictureList.addAll(pictureFileList)
                    val addFileList = filterFileList(it, controlState.timeZoneFilter.getInputValue().text) {
                        showDialog(it)
                    }
                    newPictureList.addAll(addFileList)

                    pictureFileList.clear()
                    pictureFileList.addAll(newPictureList)
                    showDialog("添加完成", isDialogCloseable = true)
                    val addSize = pictureFileList.size - oldSize
                    if (addFileList.size != addSize) {
                        showDialog("有 ${addFileList.size - addSize} 个重复文件已过滤", isDialogCloseable = true)
                    }
                }
            }
        )
    }

    fun onDelImg(index: Int) {
        if (index < 0) {
            imgPreviewState.showImageIndex = 0
            pictureFileList.clear()
        } else {
            pictureFileList.removeAt(index)
        }
    }

    fun showDialog(newMsg: String, isAppend: Boolean = true, isScroll: Boolean = true, isDialogCloseable: Boolean = false) {
        this.isDialogCloseable = isDialogCloseable
        val totalTextLength = 10000
        var tempText = if (isAppend) "$dialogText\n$newMsg" else newMsg
        if (tempText.length > totalTextLength) {
            tempText = "……" + tempText.substring(tempText.length - totalTextLength)
        }

        dialogText = tempText

        if (isScroll) {
            scope.launch {
                delay(100) // 需要等待重组完成才能 scroll
                dialogScrollState.scrollTo(dialogScrollState.maxValue)
            }
        }

        if (!isShowDialog) isShowDialog = true
    }

    fun closeDialog(isForceClose: Boolean = false) {
        if (isDialogCloseable || isForceClose) {
            dialogText = ""
            isShowDialog = false
        }
    }

    fun showPicture(picture: File?) {
        windowShowPicture = picture
    }

    fun isInputValid(): Boolean {
        return controlState.obsAk.isNotBlank() && controlState.obsSk.isNotBlank() && controlState.obsBucket.isNotBlank() && controlState.obsEndpoint.isNotBlank()
    }

    fun updateObsConfig(changeConfig: Boolean = true) {
        scope.launch(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[DataKey.TIME_ZONE] = controlState.timeZoneFilter.getInputValue().text
                preferences[DataKey.OBS_ACCESS_KEY] = controlState.obsAk
                preferences[DataKey.OBS_SECRET_KEY] = controlState.obsSk
                preferences[DataKey.OBS_BUCKET] = controlState.obsBucket
                preferences[DataKey.OBS_ENDPOINT] = controlState.obsEndpoint
                preferences[DataKey.OBS_SAVE_FOLDER] = controlState.obsSaveFolder
                preferences[DataKey.IS_AUTO_CREATE_FOLDER] = controlState.isAutoCreateFolder
                preferences[DataKey.ZOOM_MAX_HEIGHT] = controlState.maxHeight.getInputValue().text.toIntOrNull() ?: 0
            }
        }


        ObsUtil.initObsClient(controlState.obsAk, controlState.obsSk, controlState.obsEndpoint, changeConfig)
    }

    fun onStartProgress() {
        scope.launch(Dispatchers.IO) {
            showDialog("开始上传")
            var saveFolder = controlState.obsSaveFolder
            // if (!saveFolder.startsWith("/")) saveFolder = "/$saveFolder"
            if (controlState.isAutoCreateFolder) {
                saveFolder = "$saveFolder/${SimpleDateFormat("yyyy/MM/dd").format(Date())}"
            }

            updateObsConfig(changeConfig = false)
            val tempPictureModel = mutableListOf<PictureModel>()
            for (pictureModel in pictureFileList) {
                if (!pictureModel.remoteUrl.isNullOrBlank()) {
                    showDialog("${pictureModel.file.name} 已上传过，不再重复上传")
                    tempPictureModel.add(pictureModel)
                    continue
                }
                showDialog("正在上传 ${pictureModel.file.name}")
                val result = ObsUtil.uploadFile(
                    pictureModel.file,
                    controlState.obsBucket,
                    saveFolder
                )
                if (result.isSuccess) {
                    showDialog("${pictureModel.file.name} 上传成功")
                    pictureModel.remoteUrl = result.getOrNull()
                }
                else {
                    println("uploadFile error: ${result.exceptionOrNull()?.stackTraceToString()}")
                    showDialog("${pictureModel.file.name} 上传失败： ${result.exceptionOrNull()?.message}")
                }
                tempPictureModel.add(pictureModel)
            }

            pictureFileList.clear()
            pictureFileList.addAll(tempPictureModel)

            showDialog("${pictureFileList.size}个文件已全部上传完成，请关闭弹窗后点击 “复制代码” 获取生成结果", isDialogCloseable = true)
        }
    }

    fun copyCode() {
        var imgPath = ""
        var imgDes = ""
        var imgTitle = ""
        var imgThumbnailWidth = ""
        var imgThumbnailHeight = ""

        for (pictureModel in pictureFileList) {
            if (pictureModel.remoteUrl.isNullOrBlank()) {
                continue
            }

            val dateText = if (pictureModel.shotDate != null) {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(pictureModel.shotDate)
            } else {
                ""
            }

            var cameraeText = pictureModel.cameraText ?: ""
            if (cameraeText.isNotBlank() && !pictureModel.lensText.isNullOrBlank()) {
                cameraeText += " + ${pictureModel.lensText}"
            }

            var currentDes = "$cameraeText <br> ${pictureModel.focalLengthText ?: ""} ${pictureModel.apertureText ?: ""} ${pictureModel.exposureTimeText ?: ""} ${pictureModel.isoText ?: ""}<br>${dateText},"
            if (currentDes.replace("<br>", "").isBlank()) {
                currentDes = ""
            }

            var thumbnailHeight = pictureModel.imgHeight ?: DefaultValue.DEFAULT_THUMBNAIL_SIZE
            val thumbnailWidth: Int
            if (thumbnailHeight > controlState.maxHeight.getInputValue().text.toInt()) {
                thumbnailHeight = controlState.maxHeight.getInputValue().text.toInt()
                thumbnailWidth = thumbnailHeight * (pictureModel.imgWidth ?: 1) / (pictureModel.imgHeight ?: 1)
            }
            else {
                thumbnailWidth = pictureModel.imgWidth ?: DefaultValue.DEFAULT_THUMBNAIL_SIZE
            }

            imgThumbnailWidth += "$thumbnailWidth,"
            imgThumbnailHeight += "$thumbnailHeight,"

            imgPath += "${pictureModel.remoteUrl ?: ""},"
            imgTitle += "${pictureModel.title ?: ""},"
            imgDes += currentDes
        }

        if (imgPath.isNotEmpty() && imgPath.last() == ',') {
            imgPath = imgPath.dropLast(1)
        }
        if (imgDes.isNotEmpty() && imgDes.last() == ',') {
            imgDes = imgDes.dropLast(1)
        }
        if (imgTitle.isNotEmpty() && imgTitle.last() == ',') {
            imgTitle = imgTitle.dropLast(1)
        }
        if (imgThumbnailWidth.isNotEmpty() && imgThumbnailWidth.last() == ',') {
            imgThumbnailWidth = imgThumbnailWidth.dropLast(1)
        }
        if (imgThumbnailHeight.isNotEmpty() && imgThumbnailHeight.last() == ',') {
            imgThumbnailHeight = imgThumbnailHeight.dropLast(1)
        }

        val codeResult = """
            {{<gallery 
            imgPath="$imgPath" 
            imgDes="$imgDes" 
            imgTitle="$imgTitle"
            imgWidth="$imgThumbnailWidth"
            imgHeight="$imgThumbnailHeight">}}
        """.trimIndent()

        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val transferable = StringSelection(codeResult)
        clipboard.setContents(transferable, transferable)

        showDialog("$codeResult \n\n已将上面的代码复制到剪切板，你也可以自行复制", isDialogCloseable = true)
    }

    fun loadConfig() {
        scope.launch(Dispatchers.IO) {
            dataStore.data.collect { preferences ->
                controlState.obsAk = preferences[DataKey.OBS_ACCESS_KEY] ?: ""
                controlState.obsSk = preferences[DataKey.OBS_SECRET_KEY] ?: ""
                controlState.obsEndpoint = preferences[DataKey.OBS_ENDPOINT] ?: ""
                controlState.obsBucket = preferences[DataKey.OBS_BUCKET] ?: ""
                controlState.obsSaveFolder = preferences[DataKey.OBS_SAVE_FOLDER] ?: ""
                controlState.isAutoCreateFolder = preferences[DataKey.IS_AUTO_CREATE_FOLDER] ?: false
                controlState.timeZoneFilter.setValue(preferences[DataKey.TIME_ZONE] ?: DefaultValue.DEFAULT_TIME_ZONE)
                controlState.maxHeight.setValue((preferences[DataKey.ZOOM_MAX_HEIGHT] ?: 0).toString())
            }
        }
    }

    fun reorderPicture(fromIndex: Int, toIndex: Int) {
        pictureFileList.apply {
            add(toIndex, removeAt(fromIndex))
        }
    }
}