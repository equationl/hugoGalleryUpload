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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowRight
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                    Text("读取配置", style = MaterialTheme.typography.subtitle1)

                    Spacer(Modifier.height(16.dp))

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

                Spacer(Modifier.height(32.dp))

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