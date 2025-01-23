package com.equationl.hugo_gallery_uploader.state

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue


class ImgPreviewState {
    var showImageIndex by mutableStateOf(0)

    var isReorderAble by mutableStateOf(false)
}