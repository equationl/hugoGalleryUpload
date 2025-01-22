package com.equationl.hugo_gallery_uploader.state

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.awt.ComposeWindow
import com.equationl.hugo_gallery_uploader.model.PictureModel
import com.equationl.hugo_gallery_uploader.util.ObsUtil
import com.equationl.hugo_gallery_uploader.util.filterFileList
import com.equationl.hugo_gallery_uploader.util.showFileSelector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun rememberApplicationState(
    scope: CoroutineScope,
    dialogScrollState: ScrollState
) = remember {
    ApplicationState(scope, dialogScrollState)
}


class ApplicationState(val scope: CoroutineScope, val dialogScrollState: ScrollState) {
    lateinit var window: ComposeWindow

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

    fun onClickImgChoose() {
        showFileSelector(
            onFileSelected = {
                scope.launch(Dispatchers.IO) {
                    showDialog("正在读取文件……", false)
                    pictureFileList.addAll(
                        filterFileList(it, controlState.timeZoneFilter.getInputValue().text) {
                            showDialog(it)
                        }
                    )
                    //changeDialogText("正在重新排序……", false)
                    //reSortFileList()
                    //isRunning = false
                    //changeDialogText("", isAppend = false, isScroll = false)
                    showDialog("添加完成", isDialogCloseable = true)
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

    fun updateObsConfig() {
        ObsUtil.initObsClient(controlState.obsAk, controlState.obsSk, controlState.obsEndpoint, true)
    }

    fun onStartProgress() {
        // TODO
//        scope.launch(Dispatchers.IO) {
//            showDialog("开始上传")
//            ObsUtil.initObsClient(controlState.obsAk, controlState.obsSk, controlState.obsEndpoint)
//            pictureFileList.forEachIndexed { index, pictureModel ->
//                showDialog("正在上传 ${pictureModel.file.name}")
//                val result = ObsUtil.uploadFile(
//                    pictureModel.file,
//                    controlState.obsBucket,
//                    controlState.obsSaveFolder
//                )
//                if (result.isSuccess) {
//                    showDialog("${pictureModel.file.name} 上传成功")
//                }
//                else {
//
//                }
//            }
//        }
    }


}