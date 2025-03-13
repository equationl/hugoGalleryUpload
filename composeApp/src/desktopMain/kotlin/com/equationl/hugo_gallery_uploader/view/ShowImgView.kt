package com.equationl.hugo_gallery_uploader.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Error
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.sp
import coil3.PlatformContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.equationl.hugo_gallery_uploader.util.ImageUtil
import kotlinx.coroutines.delay
import org.jetbrains.skia.Image
import java.io.File

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ShowImgView(
    img: Any?
) {
    var scaleNumber by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isShowScaleTip by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(isShowScaleTip) {
        delay(2000)
        isShowScaleTip = false
    }

    Box(
        Modifier
            .fillMaxSize()
            .onPointerEvent(PointerEventType.Scroll) {
                scaleNumber = (scaleNumber - it.changes.first().scrollDelta.y).coerceIn(1f, 20f)
                isShowScaleTip = true
            }
            .onPointerEvent(PointerEventType.Move) {
                if (it.changes.first().pressed) {
                    offset -= (it.changes.first().previousPosition - it.changes.first().position)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        when (img) {
            is File -> {
                if (!img.exists()) {
                    isError = true
                    Icon(
                        imageVector = Icons.Outlined.Error,
                        contentDescription = "文件不存在",
                        tint = MaterialTheme.colors.error,
                        modifier = Modifier.fillMaxSize(0.5f)
                    )
                } else {
                    val bitmap = remember(img) {
                        runCatching {
                            Image.makeFromEncoded(img.readBytes()).toComposeImageBitmap()
                        }.getOrNull()
                    }

                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = scaleNumber
                                    scaleY = scaleNumber

                                    translationX = offset.x
                                    translationY = offset.y
                                }
                        )
                    } else {
                        isError = true
                        Icon(
                            imageVector = Icons.Outlined.Error,
                            contentDescription = "文件读取失败",
                            tint = MaterialTheme.colors.error,
                            modifier = Modifier.fillMaxSize(0.5f)
                        )
                    }
                }
            }
            is String -> {
                AsyncImage(
                    model = ImageRequest.Builder(PlatformContext.INSTANCE)
                        .data(img)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    imageLoader = ImageUtil.globalImageLoader,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scaleNumber
                            scaleY = scaleNumber

                            translationX = offset.x
                            translationY = offset.y
                        },
                    error = rememberVectorPainter(Icons.Outlined.Error),
                    onError = { isError = true }
                )
            }
            else -> {
                isError = true
                Icon(
                    imageVector = Icons.Outlined.Error,
                    contentDescription = "无效的图片数据",
                    tint = MaterialTheme.colors.error,
                    modifier = Modifier.fillMaxSize(0.5f)
                )
            }
        }

        AnimatedVisibility(
            visible = isShowScaleTip && !isError
        ) {
            Text(
                "${scaleNumber}X",
                modifier = Modifier.background(MaterialTheme.colors.surface),
                color = MaterialTheme.colors.onSurface,
                fontSize = 48.sp
            )
        }

        AnimatedVisibility(
            visible = (offset != Offset.Zero || scaleNumber != 1f) && !isError,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            OutlinedButton(
                onClick = {
                    offset = Offset.Zero
                    scaleNumber = 1f
                },
            ) {
                Text("恢复")
            }
        }
    }
}