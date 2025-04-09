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
import com.equationl.hugo_gallery_uploader.model.UploadHistoryModel
import com.equationl.hugo_gallery_uploader.util.ImageUtil
import com.equationl.hugo_gallery_uploader.util.ObsUtil
import com.equationl.hugo_gallery_uploader.util.UploadHistoryUtil
import com.equationl.hugo_gallery_uploader.util.Util.copyToClipboard
import com.equationl.hugo_gallery_uploader.util.dataStore
import com.equationl.hugo_gallery_uploader.util.filterFileList
import com.equationl.hugo_gallery_uploader.util.showFileSelector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    var historyList = mutableStateListOf<UploadHistoryModel>()
    var dialogText by mutableStateOf("")
    var inputDialogValue by mutableStateOf("")

    var windowShowPicture: Any? by mutableStateOf(null)
        private set
    var isShowDialog by mutableStateOf(false)
        private set
    var isDialogCloseable by mutableStateOf(true)
        private set

    var isShowInputDialog by mutableStateOf(false)
        private set
    var inputDialogTitle by mutableStateOf("请输入")
        private set

    var isShowConfirmDialog by mutableStateOf(false)
        private set
    var confirmDialogContent by mutableStateOf("请输入")
        private set

    // 是否跳过删除确认对话框（仅在当前会话有效）
    var skipDeleteConfirmation by mutableStateOf(false)
        private set


    var onInputDialogConfirm: (() -> Unit)? = null
    var onConfirmDialogConfirm: (() -> Unit)? = null
    // 确认对话框的勾选框状态改变回调
    var onConfirmDialogCheckboxChange: ((Boolean) -> Unit)? = null


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
                
                8, 46 -> { // Backspace 或 Delete 键
                    if (pictureFileList.isNotEmpty() && imgPreviewState.showImageIndex >= 0 && imgPreviewState.showImageIndex < pictureFileList.size) {
                        if (skipDeleteConfirmation) {
                            onDelImg(imgPreviewState.showImageIndex)
                        } else {
                            showDeleteConfirmDialog(imgPreviewState.showImageIndex)
                        }
                        return true
                    }
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

    fun showInputDialog(title: String, defaultValue: String, onInputDialogConfirm: (() -> Unit)? = null) {
        this.onInputDialogConfirm = onInputDialogConfirm
        inputDialogValue = defaultValue
        inputDialogTitle = title
        isShowInputDialog = true
    }

    fun editPictureModelTitle(inputDialogValue: String, index: Int) {
        if (inputDialogValue.isNotBlank()) {
            val pictureModel = pictureFileList.removeAt(index)
            pictureModel.title = inputDialogValue
            pictureFileList.add(index, pictureModel)
        }
    }

    fun closeInputDialog() {
        isShowInputDialog = false
    }


    fun showConfirmDialog(content: String, onConfirmDialogConfirm: (() -> Unit)? = null) {
        this.onConfirmDialogConfirm = onConfirmDialogConfirm
        confirmDialogContent = content
        isShowConfirmDialog = true
    }

    fun showDeleteConfirmDialog(index: Int) {
        val fileName = pictureFileList[index].file.name
        this.onConfirmDialogConfirm = {
            onConfirmDialogCheckboxChange = null
            onDelImg(index)
        }
        this.onConfirmDialogCheckboxChange = { checked ->
            skipDeleteConfirmation = checked
        }
        confirmDialogContent = "确定要删除图片 \"$fileName\" 吗？"
        isShowConfirmDialog = true
    }

    fun closeConfirmDialog() {
        isShowConfirmDialog = false
    }

    fun showPictureDetail(pictureModel: PictureModel) {
        var text = ""
        text += "标题：${pictureModel.title}\n"
        text += "链接：${pictureModel.remoteUrl}\n"
        text += "相机：${pictureModel.cameraText}\n"
        text += "镜头：${pictureModel.lensText}\n"
        text += "日期：${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(pictureModel.shotDate)}\n"
        text += "快门：${pictureModel.exposureTimeText}\n"
        text += "光圈：${pictureModel.apertureText}\n"
        text += "iso：${pictureModel.isoText}\n"
        text += "焦距：${pictureModel.focalLengthText}\n"
        text += "尺寸：${pictureModel.imgWidth}x${pictureModel.imgHeight}\n"

        showDialog(text, isAppend = false, isDialogCloseable = true)
    }

    fun showPicture(picture: Any?) {
        windowShowPicture = when (picture) {
            is File -> picture
            is String -> picture
            else -> null
        }
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

    fun updateImageLoadConfig() {
        scope.launch(Dispatchers.IO) {
            dataStore.edit { mutablePreferences ->
                mutablePreferences[DataKey.IMAGE_REFERER_URL] = controlState.imageRefererUrl
                mutablePreferences[DataKey.ENABLE_IMAGE_REFERER] = controlState.enableImageReferer
            }

            showDialog("图片加载配置已保存！重启后生效", isAppend = false, isDialogCloseable = true)
        }
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

            UploadHistoryUtil.saveUploadHistory(pictureFileList)
            loadHistory()

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

            if (controlState.maxHeight.getInputValue().text.toInt() == 0) {
                imgThumbnailWidth += "${pictureModel.imgWidth ?: DefaultValue.DEFAULT_THUMBNAIL_SIZE},"
                imgThumbnailHeight += "${pictureModel.imgHeight ?: DefaultValue.DEFAULT_THUMBNAIL_SIZE},"
            }
            else {
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
            }

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

        codeResult.copyToClipboard()

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
                
                // 加载图片加载配置
                controlState.imageRefererUrl = preferences[DataKey.IMAGE_REFERER_URL] ?: ""
                controlState.enableImageReferer = preferences[DataKey.ENABLE_IMAGE_REFERER] ?: false
                
                // 加载缓存大小信息
                updateCacheSizeInfo()
            }
        }
    }

    fun loadHistory() {
        scope.launch(Dispatchers.IO) {
            historyList.clear()
            historyList.addAll(UploadHistoryUtil.getUploadHistoryList())
            println("historyList = $historyList")
        }
    }

    fun readHistory(uploadHistoryModel: UploadHistoryModel) {
        scope.launch {
            val newPictureModel = UploadHistoryUtil.getUploadHistory(uploadHistoryModel.fileName)
            if (!newPictureModel.isNullOrEmpty()) {
                pictureFileList.clear()
                pictureFileList.addAll(newPictureModel)
            }
        }
    }

    fun deleteHistory(uploadHistoryModel: UploadHistoryModel) {
        scope.launch {
            UploadHistoryUtil.deleteUploadHistory(uploadHistoryModel.fileName)
            historyList.remove(uploadHistoryModel)
        }
    }

    fun reorderPicture(fromIndex: Int, toIndex: Int) {
        pictureFileList.apply {
            add(toIndex, removeAt(fromIndex))
        }
    }

    /**
     * 更新缓存大小信息
     */
    fun updateCacheSizeInfo() {
        scope.launch(Dispatchers.IO) {
            controlState.imageCacheSize = ImageUtil.getImageCacheSize()
        }
    }
    
    /**
     * 清除图片缓存
     */
    fun clearImageCache() {
        scope.launch(Dispatchers.IO) {
            ImageUtil.clearImageCache()
            updateCacheSizeInfo()
            showDialog("缓存已清除", isAppend = false, isDialogCloseable = true)
        }
    }
}