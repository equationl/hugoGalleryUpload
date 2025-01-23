package com.equationl.hugo_gallery_uploader.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import com.equationl.hugo_gallery_uploader.constant.DefaultValue
import com.equationl.hugo_gallery_uploader.util.FilterGMT

class ControlState {
    val timeZoneFilter = FilterGMT(TextFieldValue(DefaultValue.DEFAULT_TIME_ZONE))
    var obsAk by mutableStateOf("")
    var obsSk by mutableStateOf("")
    var obsEndpoint by mutableStateOf("")
    var obsBucket by mutableStateOf("")
    var obsSaveFolder by mutableStateOf("")
    var isAutoCreateFolder by mutableStateOf(true)
}