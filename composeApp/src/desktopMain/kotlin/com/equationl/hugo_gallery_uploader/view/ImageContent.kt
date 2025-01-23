package com.equationl.hugo_gallery_uploader.view

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.PlatformContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.equationl.hugo_gallery_uploader.state.ApplicationState
import com.equationl.hugo_gallery_uploader.util.legalSuffixList
import com.equationl.hugo_gallery_uploader.widget.dragHandle
import com.equationl.hugo_gallery_uploader.widget.draggableItems
import com.equationl.hugo_gallery_uploader.widget.draggableItemsIndexed
import com.equationl.hugo_gallery_uploader.widget.rememberDraggableListState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ImageContent(
    applicationState: ApplicationState,
    modifier: Modifier
) {
    val state = applicationState.imgPreviewState

    val draggableState = rememberDraggableListState(
        onMove = { fromIndex, toIndex ->
            applicationState.reorderPicture(fromIndex, toIndex)
        }
    )

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
                    AsyncImage(
                        model = ImageRequest.Builder(PlatformContext.INSTANCE)
                            .data(applicationState.pictureFileList[state.showImageIndex.coerceAtMost(applicationState.pictureFileList.lastIndex)].file,)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                applicationState.showPicture(applicationState.pictureFileList[state.showImageIndex.coerceAtMost(applicationState.pictureFileList.lastIndex)].file)
                            },
                        contentScale = ContentScale.Fit,
                        placeholder = rememberVectorPainter(Icons.Outlined.Download)
                    )
                }


                Row(
                    modifier = Modifier.fillMaxSize().weight(0.15f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Button(onClick = {
                            applicationState.onClickImgChoose()
                        }) {
                            Text("添加")
                        }

                        Spacer(Modifier.width(4.dp))

                        Button(onClick = { applicationState.onDelImg(-1) }) {
                            Text("清空")
                        }
                    }

                    Text("${state.showImageIndex + 1}/${applicationState.pictureFileList.size}")

                    Button(onClick = { state.isReorderAble = !state.isReorderAble }) {
                        Text("排序：${state.isReorderAble}")
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    LazyColumn(
                        state = draggableState.listState,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        draggableItemsIndexed(
                            state = draggableState,
                            items = applicationState.pictureFileList,
                            key = { _, item ->  item.file.path }
                        ) { index, pictureModel, isDragging ->
                            Row(
                                modifier = Modifier.fillMaxWidth().then(
                                    if (isDragging) Modifier.background(MaterialTheme.colors.surface)
                                    else if (state.showImageIndex == index) Modifier.background(MaterialTheme.colors.secondary)
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
                                if (state.isReorderAble) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(PlatformContext.INSTANCE)
                                            .data(pictureModel.file)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(150.dp)
                                            .clickable {
                                                applicationState.showPicture(pictureModel.file)
                                            },
                                        contentScale = ContentScale.Inside,
                                        placeholder = rememberVectorPainter(Icons.Outlined.Download)
                                    )
                                }

                                Text(
                                    pictureModel.file.name,
                                    modifier = Modifier.clickable {
                                        if (!state.isReorderAble) {
                                            state.showImageIndex = index
                                        }
                                    }.weight(0.9f),
                                    color = if (state.showImageIndex == index) MaterialTheme.colors.onSecondary else Color.Unspecified
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.weight(0.1f)
                                ) {

                                    if (!state.isReorderAble) {
                                        if (!pictureModel.remoteUrl.isNullOrBlank()) {
                                            Icon(
                                                imageVector = Icons.Rounded.CloudUpload,
                                                contentDescription = null,
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.Rounded.Delete,
                                            contentDescription = null,
                                            modifier = Modifier.clickable {
                                                applicationState.onDelImg(index)
                                            }
                                        )
                                    }

                                    if (state.isReorderAble) {
                                        Icon(
                                            modifier = Modifier.dragHandle(
                                                state = draggableState,
                                                key = pictureModel.file.path
                                            ),
                                            imageVector = Icons.Default.DragHandle,
                                            contentDescription = "Reorder icon"
                                        )
                                    }
                                }
                            }
                        }

                    }

                    VerticalScrollbar(
                        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                        adapter = rememberScrollbarAdapter(
                            scrollState = draggableState.listState
                        )
                    )
                }
            }
        }
    }
}