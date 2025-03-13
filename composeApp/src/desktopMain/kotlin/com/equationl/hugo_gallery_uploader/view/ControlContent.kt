package com.equationl.hugo_gallery_uploader.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equationl.hugo_gallery_uploader.model.UploadHistoryModel
import com.equationl.hugo_gallery_uploader.state.ApplicationState

@Composable
fun ControlContent(
    applicationState: ApplicationState,
    modifier: Modifier
) {
    val state = applicationState.controlState
    val scrollState = rememberScrollState()

    var isShowAk by remember { mutableStateOf(false) }
    var isShowSk by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        applicationState.loadConfig()
        applicationState.loadHistory()
    }

    Box(modifier) {
        Card(
            modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState),
            shape = RoundedCornerShape(8.dp),
            elevation = 4.dp,
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                // horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.clickable {
                            state.isShowHistory = !state.isShowHistory
                        }
                    ) {
                        Text("查看上传历史", style = MaterialTheme.typography.subtitle1)
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowRight,
                            contentDescription = null,
                            modifier = Modifier.rotate(if (state.isShowHistory) 0f else 90f)
                        )
                    }
                    Spacer(Modifier.height(16.dp))

                    AnimatedVisibility(
                        visible = state.isShowHistory
                    ) {
                        HistoryList(
                            data = applicationState.historyList,
                            onClickItem = {
                                if (applicationState.pictureFileList.isEmpty()) {
                                    applicationState.readHistory(it)
                                }
                                else {
                                    applicationState.showConfirmDialog("读取历史上传数据后当前工作区会被覆盖，请确定您已上传当前工作区后再读取") {
                                        applicationState.readHistory(it)
                                    }
                                }
                            },
                            onClickDelete = {
                                applicationState.showConfirmDialog("确认删除？") {
                                    applicationState.deleteHistory(it)
                                }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.clickable {
                            state.isShowReadSetting = !state.isShowReadSetting
                        }
                    ) {
                        Text("读取配置", style = MaterialTheme.typography.subtitle1)
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowRight,
                            contentDescription = null,
                            modifier = Modifier.rotate(if (state.isShowReadSetting) 0f else 90f)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    AnimatedVisibility(
                        state.isShowReadSetting
                    ) {
                        Column {
                            OutlinedTextField(
                                value = state.timeZoneFilter.getInputValue(),
                                onValueChange = state.timeZoneFilter.onValueChange(),
                                label = {
                                    Text("日期转换时区")
                                }
                            )

                            Spacer(Modifier.height(8.dp))

                            OutlinedTextField(
                                value = state.maxHeight.getInputValue(),
                                onValueChange = state.maxHeight.onValueChange(),
                                label = {
                                    Text("缩略图最大高度（0表示不缩略）")
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Column {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.clickable {
                            state.isShowObsSetting = !state.isShowObsSetting
                        }
                    ) {
                        Text("OBS配置", style = MaterialTheme.typography.subtitle1)
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowRight,
                            contentDescription = null,
                            modifier = Modifier.rotate(if (state.isShowObsSetting) 0f else 90f)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    AnimatedVisibility(
                        state.isShowObsSetting,
                    ) {
                        Column {
                            OutlinedTextField(
                                value = state.obsAk,
                                onValueChange = {
                                    state.obsAk = it
                                },
                                label = {
                                    Text("AK")
                                },
                                visualTransformation = if (isShowAk) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            isShowAk = !isShowAk
                                        }
                                    ) {
                                        Icon(
                                            if (isShowAk) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                            contentDescription = null
                                        )
                                    }
                                }
                            )

                            Spacer(Modifier.height(8.dp))

                            OutlinedTextField(
                                value = state.obsSk,
                                onValueChange = {
                                    state.obsSk = it
                                },
                                label = {
                                    Text("SK")
                                },
                                visualTransformation = if (isShowSk) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            isShowSk = !isShowSk
                                        }
                                    ) {
                                        Icon(
                                            if (isShowSk) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                            contentDescription = null
                                        )
                                    }
                                }
                            )

                            Spacer(Modifier.height(8.dp))

                            OutlinedTextField(
                                value = state.obsBucket,
                                onValueChange = {
                                    state.obsBucket = it
                                },
                                label = {
                                    Text("Bucket")
                                }
                            )

                            Spacer(Modifier.height(8.dp))

                            OutlinedTextField(
                                value = state.obsEndpoint,
                                onValueChange = {
                                    state.obsEndpoint = it
                                },
                                label = {
                                    Text("Endpoint")
                                }
                            )

                            Spacer(Modifier.height(8.dp))

                            OutlinedTextField(
                                value = state.obsSaveFolder,
                                onValueChange = {
                                    state.obsSaveFolder = it
                                },
                                label = {
                                    Text("上传文件夹")
                                }
                            )

                            Spacer(Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = state.isAutoCreateFolder,
                                    onCheckedChange = {
                                        state.isAutoCreateFolder = it
                                    }
                                )
                                Text("是否自动按照当前日期新建文件夹", fontSize = 12.sp)
                            }

                            Spacer(Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    applicationState.updateObsConfig()
                                },
                                modifier = Modifier.padding(top = 8.dp),
                                enabled = applicationState.isInputValid()
                            ) {
                                Text("更新配置")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Column {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.clickable {
                            state.isShowImageLoadSetting = !state.isShowImageLoadSetting
                            if (state.isShowImageLoadSetting) {
                                // 加载缓存大小信息
                                applicationState.updateCacheSizeInfo()
                            }
                        }
                    ) {
                        Text("加载设置", style = MaterialTheme.typography.subtitle1)
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowRight,
                            contentDescription = null,
                            modifier = Modifier.rotate(if (state.isShowImageLoadSetting) 0f else 90f)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    AnimatedVisibility(
                        state.isShowImageLoadSetting,
                    ) {
                        Column {
                            if (state.enableImageReferer) {
                                OutlinedTextField(
                                    value = state.imageRefererUrl,
                                    onValueChange = {
                                        state.imageRefererUrl = it
                                    },
                                    label = {
                                        Text("防盗链 Referer URL")
                                    },
                                    placeholder = {
                                        Text("例如: https://example.com")
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(Modifier.height(8.dp))
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = state.enableImageReferer,
                                    onCheckedChange = {
                                        state.enableImageReferer = it
                                    }
                                )
                                Text("绕过防盗链", fontSize = 12.sp)
                            }

                            Spacer(Modifier.height(16.dp))

                            Divider()

                            Spacer(Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "图片缓存: ${state.imageCacheSize}",
                                    style = MaterialTheme.typography.body2
                                )
                                
                                Button(
                                    onClick = {
                                        applicationState.clearImageCache()
                                    }
                                ) {
                                    Text("清除缓存")
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    applicationState.updateImageLoadConfig()
                                },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("更新配置")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            applicationState.onStartProgress()
                        },
                        enabled = applicationState.pictureFileList.isNotEmpty() && applicationState.isInputValid()
                    ) {
                        Text("开始上传")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            applicationState.copyCode()
                        },
                        enabled = applicationState.pictureFileList.any { !it.remoteUrl.isNullOrBlank() }
                    ) {
                        Text("复制代码")
                    }
                }
            }
        }

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd)
                .fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState)
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun HistoryList(
    data: List<UploadHistoryModel>,
    onClickItem: (item: UploadHistoryModel) -> Unit,
    onClickDelete: (item: UploadHistoryModel) -> Unit
) {
    val scrollState = rememberLazyListState()
    Card(
       elevation = 4.dp
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().heightIn(0.dp, 200.dp)
        ) {
            LazyColumn(
                state = scrollState
            ) {
                items(
                    count = data.size,
                    key = { index -> data[index].filePath }
                ) {
                    val item = data[it]
                    ListItem(
                        modifier = Modifier.clickable {
                            onClickItem(item)
                        },
                        trailing = {
                            IconButton(
                                onClick = {
                                    onClickDelete(item)
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete"
                                )
                            }
                        },
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = item.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.body1
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Text(
                                    text = "总图片: ${item.totalPictureCount}",
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                )
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Text(
                                    text = "已上传: ${item.uploadedPictureCount}",
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                    Divider()
                }
            }

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(
                    scrollState = scrollState
                )
            )
        }
    }
}