package com.equationl.hugo_gallery_uploader.util

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

object Util {
    fun String.copyToClipboard() {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val transferable = StringSelection(this)
        clipboard.setContents(transferable, transferable)
    }
}