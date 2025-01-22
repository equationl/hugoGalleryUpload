package com.equationl.hugo_gallery_uploader.view

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.equationl.hugo_gallery_uploader.state.ApplicationState
import com.equationl.hugo_gallery_uploader.util.legalSuffixList

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun ImageContent(
    applicationState: ApplicationState,
    modifier: Modifier
) {

    val state = applicationState.imgPreviewState
    state.lazyListState = rememberLazyListState()

    Card(
        onClick = {
            applicationState.onClickImgChoose()
        },
        modifier = modifier.padding(16.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp,
        backgroundColor = MaterialTheme.colors.surface,
        enabled = applicationState.pictureFileList.isEmpty()
    ) {
        if (applicationState.pictureFileList.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "请点击选择文件（夹）或拖拽文件（夹）至此\n仅支持 ${legalSuffixList.contentToString()}",
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    Image(
                        bitmap = applicationState.pictureFileList[state.showImageIndex.coerceAtMost(applicationState.pictureFileList.lastIndex)].file.inputStream().buffered()
                            .use(::loadImageBitmap),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                applicationState.showPicture(applicationState.pictureFileList[state.showImageIndex.coerceAtMost(applicationState.pictureFileList.lastIndex)].file)
                            },
                        contentScale = ContentScale.Fit
                    )
                }


                Row(
                    modifier = Modifier.fillMaxSize().weight(0.15f),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = {
                        applicationState.onClickImgChoose()
                    }) {
                        Text("添加")
                    }

                    Text("${state.showImageIndex + 1}/${applicationState.pictureFileList.size}")

                    Button(onClick = { applicationState.onDelImg(-1) }) {
                        Text("清空")
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    var lastTitle = remember { "" }
                    LazyColumn(
                        state = state.lazyListState,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        applicationState.pictureFileList.forEachIndexed { index, pictureModel ->
                            if (lastTitle != pictureModel.file.parent) {
                                stickyHeader {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Surface {
                                            Text(pictureModel.file.parent)
                                        }
                                    }
                                }
                                lastTitle = pictureModel.file.parent
                            }

                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().then(
                                        if (state.showImageIndex == index) Modifier.background(MaterialTheme.colors.secondary)
                                        else {
                                            if (index % 2 == 0) {
                                                Modifier.background(MaterialTheme.colors.secondaryVariant)
                                            }
                                            else {
                                                Modifier
                                            }
                                        }
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        pictureModel.file.name,
                                        modifier = Modifier.clickable {
                                            state.showImageIndex = index
                                        }.weight(0.9f),
                                        color = if (state.showImageIndex == index) MaterialTheme.colors.onSecondary else Color.Unspecified
                                    )

                                    Icon(
                                        imageVector = Icons.Rounded.Delete,
                                        contentDescription = null,
                                        modifier = Modifier.clickable {
                                            applicationState.onDelImg(index)
                                        }.weight(0.1f)
                                    )
                                }
                            }
                        }
                    }

                    VerticalScrollbar(
                        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                        adapter = rememberScrollbarAdapter(
                            scrollState = state.lazyListState
                        )
                    )
                }
            }
        }
    }
}