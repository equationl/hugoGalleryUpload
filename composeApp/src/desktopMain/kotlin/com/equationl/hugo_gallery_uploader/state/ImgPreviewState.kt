package com.equationl.hugo_gallery_uploader.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.equationl.hugo_gallery_uploader.widget.DraggableListState


class ImgPreviewState {
    lateinit var draggableState: DraggableListState

    var showImageIndex by mutableStateOf(0)

    var listItemType by mutableStateOf(ImgListItemType.TEXT)
}

enum class ImgListItemType(val showText: String) {
    TEXT("仅标题"),
    IMAGE("预览图")
}

fun ImgListItemType.toggleImgListItemType(): ImgListItemType {
    return when (this) {
        ImgListItemType.TEXT -> ImgListItemType.IMAGE
        ImgListItemType.IMAGE -> ImgListItemType.TEXT
    }
}