package com.equationl.hugo_gallery_uploader.view

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                    Text("读取配置：")

                    OutlinedTextField(
                        value = state.timeZoneFilter.getInputValue(),
                        onValueChange = state.timeZoneFilter.onValueChange(),
                        label = {
                            Text("时区")
                        }
                    )
                }

                Column {
                    Text("OBS配置：")

                    OutlinedTextField(
                        value = state.obsAk,
                        onValueChange = {
                            state.obsAk = it
                        },
                        label = {
                            Text("AK")
                        }
                    )

                    OutlinedTextField(
                        value = state.obsSk,
                        onValueChange = {
                            state.obsSk = it
                        },
                        label = {
                            Text("SK")
                        }
                    )

                    OutlinedTextField(
                        value = state.obsBucket,
                        onValueChange = {
                            state.obsBucket = it
                        },
                        label = {
                            Text("Bucket")
                        }
                    )

                    OutlinedTextField(
                        value = state.obsEndpoint,
                        onValueChange = {
                            state.obsEndpoint = it
                        },
                        label = {
                            Text("Endpoint")
                        }
                    )


                    OutlinedTextField(
                        value = state.obsSaveFolder,
                        onValueChange = {
                            state.obsSaveFolder = it
                        },
                        label = {
                            Text("上传文件夹")
                        }
                    )

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