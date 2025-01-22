package com.equationl.hugo_gallery_uploader

import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.equationl.hugo_gallery_uploader.constant.MinWindowSize
import com.equationl.hugo_gallery_uploader.state.rememberApplicationState
import com.equationl.hugo_gallery_uploader.view.MainView
import com.equationl.hugo_gallery_uploader.view.ShowImgView
import java.awt.Dimension
import kotlin.math.roundToInt

fun main() = application {
    val applicationState = rememberApplicationState(rememberCoroutineScope(), rememberScrollState())

    Window(
        title = "Hugo Gallery Uploader",
        onCloseRequest = {
            exitApplication()
        },
//        onKeyEvent = {
//            applicationState.onKeyEvent(it)
//        },
        state = rememberWindowState().apply {
            position = WindowPosition(Alignment.Center)
        }
    ) {
        // 设置窗口的最小尺寸
        window.minimumSize = Dimension(MinWindowSize.width.value.roundToInt(), MinWindowSize.height.value.roundToInt())

        applicationState.window = window

        MainView(applicationState)
    }

    if (applicationState.windowShowPicture != null) {
        Window(
            title = "${applicationState.windowShowPicture?.name}",
            onCloseRequest = {
                applicationState.showPicture(null)
            },
            state = rememberWindowState().apply {
                position = WindowPosition(Alignment.Center)
                placement = WindowPlacement.Fullscreen
            }
        ) {
            ShowImgView(applicationState.windowShowPicture!!)
        }
    }
}