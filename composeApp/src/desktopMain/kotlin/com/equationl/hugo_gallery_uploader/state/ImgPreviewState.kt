package com.equationl.hugo_gallery_uploader.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.equationl.hugo_gallery_uploader.widget.DraggableListState


class ImgPreviewState {
    lateinit var draggableState: DraggableListState

    var showImageIndex by mutableStateOf(0)

    var isReorderAble by mutableStateOf(false)
}