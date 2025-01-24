package com.equationl.hugo_gallery_uploader.view

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.PlatformContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.equationl.hugo_gallery_uploader.state.ApplicationState
import com.equationl.hugo_gallery_uploader.state.ImgListItemType
import com.equationl.hugo_gallery_uploader.state.toggleImgListItemType
import com.equationl.hugo_gallery_uploader.util.legalSuffixList
import com.equationl.hugo_gallery_uploader.widget.dragHandle
import com.equationl.hugo_gallery_uploader.widget.draggableItemsIndexed
import com.equationl.hugo_gallery_uploader.widget.rememberDraggableListState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ImageContent(
    applicationState: ApplicationState,
    modifier: Modifier
) {
    val state = applicationState.imgPreviewState

    state.draggableState = rememberDraggableListState(
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
                        TextButton(onClick = {
                            applicationState.onClickImgChoose()
                        }) {
                            Text("添加")
                        }

                        Spacer(Modifier.width(4.dp))

                        TextButton(onClick = {
                            applicationState.showConfirmDialog("确定清空所有图片？") {
                                applicationState.onDelImg(-1)
                            }
                        }) {
                            Text("清空")
                        }
                    }

                    Text("${state.showImageIndex + 1}/${applicationState.pictureFileList.size}")

                    TextButton(onClick = { state.listItemType = state.listItemType.toggleImgListItemType() }) {
                        Text("显示方式：${state.listItemType.showText}")
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    LazyColumn(
                        state = state.draggableState.listState,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        draggableItemsIndexed(
                            state = state.draggableState,
                            items = applicationState.pictureFileList,
                            key = { _, item ->  item.file.path }
                        ) { index, pictureModel, isDragging ->
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().then(
                                        //if (isDragging) Modifier.background(MaterialTheme.colors.onSurface)
                                        if (state.showImageIndex == index) Modifier.background(MaterialTheme.colors.secondary)
                                        else Modifier
//                                    else {
//                                        if (index % 2 == 0) {
//                                            Modifier.background(MaterialTheme.colors.secondaryVariant)
//                                        }
//                                        else {
//                                            Modifier
//                                        }
//                                    }
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    if (state.listItemType == ImgListItemType.IMAGE) {
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
                                        "${index + 1}.${pictureModel.title ?: pictureModel.file.name}",
                                        modifier = Modifier.clickable {
                                            if (state.listItemType == ImgListItemType.TEXT) {
                                                state.showImageIndex = index
                                            }
                                        }.weight(0.8f),
                                        color = if (state.showImageIndex == index) MaterialTheme.colors.onSecondary else Color.Unspecified
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.End,
                                        modifier = Modifier.weight(0.2f)
                                    ) {

                                        if (state.listItemType == ImgListItemType.TEXT) {
                                            if (!pictureModel.remoteUrl.isNullOrBlank()) {
                                                Icon(
                                                    imageVector = Icons.Rounded.CloudUpload,
                                                    contentDescription = null,
                                                    tint = LocalContentColor.current.copy(alpha = 0.5f)
                                                )
                                            }

                                            Icon(
                                                imageVector = Icons.Rounded.Delete,
                                                contentDescription = null,
                                                modifier = Modifier.clickable {
                                                    applicationState.showConfirmDialog("确定删除？") {
                                                        applicationState.onDelImg(index)
                                                    }
                                                }
                                            )
                                        }

                                        if (state.listItemType == ImgListItemType.IMAGE) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "edit title",
                                                modifier = Modifier.clickable {
                                                    applicationState.showInputDialog(
                                                        title = "修改标题",
                                                        defaultValue = pictureModel.title ?: pictureModel.file.name,
                                                        onInputDialogConfirm = {
                                                            applicationState.closeInputDialog()
                                                            applicationState.editPictureModelTitle(applicationState.inputDialogValue, index)
                                                        }
                                                    )
                                                }
                                            )


                                            Icon(
                                                modifier = Modifier.dragHandle(
                                                    state = state.draggableState,
                                                    key = pictureModel.file.path
                                                ),
                                                imageVector = Icons.Default.DragHandle,
                                                contentDescription = "Reorder icon"
                                            )
                                        }
                                    }
                                }

                                Divider()
                            }
                        }

                    }

                    VerticalScrollbar(
                        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                        adapter = rememberScrollbarAdapter(
                            scrollState = state.draggableState.listState
                        )
                    )
                }
            }
        }
    }
}