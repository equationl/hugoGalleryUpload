package com.equationl.hugo_gallery_uploader.view

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.equationl.hugo_gallery_uploader.state.ApplicationState
import com.equationl.hugo_gallery_uploader.util.dropFileTarget
import com.equationl.hugo_gallery_uploader.util.filterFileList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MainView(applicationState: ApplicationState) {
    applicationState.window.contentPane.dropTarget = dropFileTarget {
        applicationState.scope.launch(Dispatchers.IO) {
            applicationState.showDialog("正在读取文件……", false)

            applicationState.pictureFileList.addAll(
                filterFileList(
                    it,
                    applicationState.controlState.timeZoneFilter.getInputValue().text,
                    onProgress = {
                        applicationState.showDialog(it)
                    }
                )
            )

            // applicationState.showDialog("正在重新排序……", false)
            // applicationState.reSortFileList()

            applicationState.showDialog("添加完成", isDialogCloseable = true)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Row (
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            ImageContent(
                applicationState = applicationState,
                modifier = Modifier.fillMaxSize().weight(1f)
            )

            ControlContent(
                applicationState = applicationState,
                modifier = Modifier.fillMaxSize().weight(1f)
            )
        }
    }

    if (applicationState.isShowDialog) {
        Dialog(
            onCloseRequest = { applicationState.closeDialog() },
            title = if (applicationState.isDialogCloseable) "处理完成" else "处理中",
            resizable = false
        ) {
            Box {
                Column(
                    modifier = Modifier.fillMaxSize().padding(8.dp).verticalScroll(applicationState.dialogScrollState),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SelectionContainer {
                        Text(applicationState.dialogText)
                    }
                }

                VerticalScrollbar(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(applicationState.dialogScrollState)
                )
            }
        }
    }


}