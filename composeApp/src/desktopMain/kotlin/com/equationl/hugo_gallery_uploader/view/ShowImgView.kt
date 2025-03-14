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
import androidx.compose.material.icons.outlined.Download
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import coil3.PlatformContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import com.equationl.hugo_gallery_uploader.util.ImageUtil
import kotlinx.coroutines.delay
import org.jetbrains.skia.Image
import java.io.File
import kotlin.math.sign

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ShowImgView(
    img: Any?
) {
    var scaleNumber by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isShowScaleTip by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    var imgSize by remember { mutableStateOf(IntSize.Zero) }

    // 获取适配后的图片尺寸（考虑ContentScale.Fit的影响）
    fun getFittedImageSize(): Pair<Float, Float> {
        if (imgSize.width <= 0 || imgSize.height <= 0 || boxSize.width <= 0 || boxSize.height <= 0) {
            return Pair(0f, 0f)
        }

        val imageRatio = imgSize.width.toFloat() / imgSize.height.toFloat()
        val boxRatio = boxSize.width.toFloat() / boxSize.height.toFloat()

        // 按照ContentScale.Fit逻辑计算适配后的图片尺寸
        val fittedWidth: Float
        val fittedHeight: Float

        if (imageRatio > boxRatio) {
            // 图片更宽，以宽度为准缩放
            fittedWidth = boxSize.width.toFloat()
            fittedHeight = fittedWidth / imageRatio
        } else {
            // 图片更高，以高度为准缩放
            fittedHeight = boxSize.height.toFloat()
            fittedWidth = fittedHeight * imageRatio
        }

        return Pair(fittedWidth, fittedHeight)
    }

    // 计算允许的最大偏移量，确保图片不会移出窗口
    fun calculateBoundedOffset(proposedOffset: Offset): Offset {
        val (fittedWidth, fittedHeight) = getFittedImageSize()

        // 图片缩放后的实际尺寸
        val scaledImgWidth = fittedWidth * scaleNumber
        val scaledImgHeight = fittedHeight * scaleNumber

        // 如果图片小于窗口，则强制居中（偏移为0）
        if (scaledImgWidth <= boxSize.width && scaledImgHeight <= boxSize.height) {
            return Offset.Zero
        }

        // 计算可移动的最大距离
        // 水平方向
        val maxOffsetX = if (scaledImgWidth > boxSize.width) {
            (scaledImgWidth - boxSize.width) / 2f
        } else {
            0f
        }

        // 垂直方向
        val maxOffsetY = if (scaledImgHeight > boxSize.height) {
            (scaledImgHeight - boxSize.height) / 2f
        } else {
            0f
        }

        // 限制偏移量不超出边界
        // 当图片边缘与窗口边缘对齐时，offset的范围是[-maxOffsetX, maxOffsetX]
        val boundedX = proposedOffset.x.coerceIn(-maxOffsetX, maxOffsetX)
        val boundedY = proposedOffset.y.coerceIn(-maxOffsetY, maxOffsetY)

        return Offset(boundedX, boundedY)
    }

    LaunchedEffect(isShowScaleTip) {
        delay(2000)
        isShowScaleTip = false
    }

    // 当缩放比例改变时重新计算偏移量约束
    LaunchedEffect(scaleNumber, boxSize, imgSize) {
        offset = calculateBoundedOffset(offset)
    }

    Box(
        Modifier
            .fillMaxSize()
            .onGloballyPositioned { boxSize = it.size }
            .onPointerEvent(PointerEventType.Scroll) {
                val pointerPosition = it.changes.first().position
                
                // 计算中心点
                val centerX = boxSize.width / 2f
                val centerY = boxSize.height / 2f

                // 计算鼠标相对于中心的位置
                // 需要考虑当前偏移量：鼠标在屏幕上的位置转换到图片坐标系中的位置
                val mouseX = (pointerPosition.x - centerX - offset.x) / scaleNumber
                val mouseY = (pointerPosition.y - centerY - offset.y) / scaleNumber
                
                // 更新缩放系数，使用不同的步进值
                val scrollDelta = it.changes.first().scrollDelta.y
                
                // 根据当前缩放值和滚动方向选择合适的步进值
                val step = when {
                    // 放大时（向上滚动）
                    scrollDelta < 0 -> {
                        if (scaleNumber < 1.0f) 0.1f else 1.0f
                    }
                    // 缩小时（向下滚动）
                    else -> {
                        if (scaleNumber <= 1.0f) 0.1f else 1.0f
                    }
                }
                
                val deltaScale = if (scrollDelta != 0f) sign(scrollDelta) * step else 0f
                
                scaleNumber = (scaleNumber - deltaScale).coerceIn(0.1f, 20f)
                
                // 计算新的偏移量，保持鼠标下的图片点不变
                // 新偏移量 = 鼠标位置 - 中心点 - (鼠标在图片上的位置 × 新缩放系数)
                val newOffsetX = pointerPosition.x - centerX - (mouseX * scaleNumber)
                val newOffsetY = pointerPosition.y - centerY - (mouseY * scaleNumber)
                
                // 应用边界限制
                offset = calculateBoundedOffset(Offset(newOffsetX, newOffsetY))
                
                isShowScaleTip = true
            }
            .onPointerEvent(PointerEventType.Move) {
                if (it.changes.first().pressed) {
                    val proposedOffset = offset - (it.changes.first().previousPosition - it.changes.first().position)
                    // 应用边界限制
                    offset = calculateBoundedOffset(proposedOffset)
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
                        // 保存图片尺寸
                        LaunchedEffect(bitmap) {
                            imgSize = IntSize(bitmap.width, bitmap.height)
                            // 初始化时应用边界限制
                            offset = calculateBoundedOffset(offset)
                        }
                        
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
                // 对于网络图片，使用状态跟踪原始图片尺寸
                var asyncImgLoaded by remember { mutableStateOf(false) }
                var originalImageSize by remember { mutableStateOf(IntSize.Zero) }
                
                AsyncImage(
                    model = ImageRequest.Builder(PlatformContext.INSTANCE)
                        .data(img)
                        .crossfade(true)
                        .size(Size.ORIGINAL)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    imageLoader = ImageUtil.globalImageLoader,
                    placeholder = rememberVectorPainter(Icons.Outlined.Download),
                    onSuccess = { state ->
                        // 成功加载图片后获取原始尺寸
                        state.painter.intrinsicSize.let { size ->
                            if (size.width > 0 && size.height > 0) {
                                originalImageSize = IntSize(size.width.toInt(), size.height.toInt())
                                imgSize = originalImageSize  // 使用原始尺寸而不是适配后的尺寸
                                asyncImgLoaded = true
                            }
                        }
                    },
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
                if (scaleNumber >= 1.0f) "${scaleNumber.toInt()}X" else "%.1fX".format(scaleNumber),
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