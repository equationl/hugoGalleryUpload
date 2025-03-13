package com.equationl.hugo_gallery_uploader.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState
import com.equationl.hugo_gallery_uploader.model.PictureModel
import com.equationl.hugo_gallery_uploader.state.ApplicationState
import com.equationl.hugo_gallery_uploader.util.dropAndDragTarget
import com.equationl.hugo_gallery_uploader.util.filterFileList

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainView(applicationState: ApplicationState) {
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
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .dragAndDropTarget(
                        shouldStartDragAndDrop = { true },
                        target = dropAndDragTarget {
                            applicationState.showDialog("正在读取文件……", false)
                            val oldSize = applicationState.pictureFileList.size
                            val newPictureList = mutableSetOf<PictureModel>()
                            newPictureList.addAll(applicationState.pictureFileList)
                            val addFileList = filterFileList(
                                it,
                                applicationState.controlState.timeZoneFilter.getInputValue().text,
                                onProgress = {
                                    applicationState.showDialog(it)
                                }
                            )
                            newPictureList.addAll(addFileList)
                            applicationState.pictureFileList.clear()
                            applicationState.pictureFileList.addAll(newPictureList)

                            applicationState.showDialog("添加完成", isDialogCloseable = true)
                            val addSize = applicationState.pictureFileList.size - oldSize
                            if (addFileList.size != addSize) {
                                applicationState.showDialog("有 ${addFileList.size - addSize} 个重复文件已过滤", isDialogCloseable = true)
                            }
                        }
                    )
            )

            ControlContent(
                applicationState = applicationState,
                modifier = Modifier.fillMaxSize().weight(1f)
            )
        }
    }

    if (applicationState.isShowDialog) {
        DialogWindow(
            onCloseRequest = { applicationState.closeDialog() },
            title = if (applicationState.isDialogCloseable) "处理完成" else "处理中",
            resizable = false,
            state = rememberDialogState(width = 600.dp, height = 400.dp)
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

    if (applicationState.isShowInputDialog) {
        DialogWindow(
            onCloseRequest = { applicationState.closeInputDialog() },
            title = applicationState.inputDialogTitle,
            resizable = false,
            state = rememberDialogState(width = 400.dp, height = 200.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = applicationState.inputDialogValue,
                    onValueChange = {
                        applicationState.inputDialogValue = it
                    },
                    label = { Text(applicationState.inputDialogTitle) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = {
                        applicationState.onInputDialogConfirm?.invoke()
                    }) {
                        Text("确定")
                    }
                    Button(onClick = {applicationState.closeInputDialog()}) {
                        Text("取消")
                    }
                }
            }
        }
    }

    if (applicationState.isShowConfirmDialog) {
        DialogWindow(
            onCloseRequest = { applicationState.closeConfirmDialog() },
            title = "提示",
            resizable = false,
            state = rememberDialogState(width = 400.dp, height = 200.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(applicationState.confirmDialogContent, style = MaterialTheme.typography.body1)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = {
                        applicationState.closeConfirmDialog()
                        applicationState.onConfirmDialogConfirm?.invoke()
                    }) {
                        Text("确定")
                    }
                    Button(onClick = {applicationState.closeConfirmDialog()}) {
                        Text("取消")
                    }
                }
            }
        }
    }


}