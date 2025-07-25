package com.example.text2ai

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CropImage(
    imageBitmap: ImageBitmap,
) {
    var image by remember { mutableStateOf(imageBitmap) }

    var topLeft by remember { mutableStateOf(Offset(400f, 400f)) }
    var topRight by remember { mutableStateOf(Offset(800f, 400f)) }
    var bottomLeft by remember { mutableStateOf(Offset(400f, 800f)) }
    var bottomRight by remember { mutableStateOf(Offset(800f, 800f)) }

    var draggingCorner by remember { mutableStateOf<Corner?>(null) }
    var draggingCenter by remember { mutableStateOf(false) }

    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            bitmap = image,
            contentDescription = "Image Taken",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            draggingCorner = when {
                                offset.isNear(topLeft) -> Corner.TopLeft
                                offset.isNear(topRight) -> Corner.TopRight
                                offset.isNear(bottomLeft) -> Corner.BottomLeft
                                offset.isNear(bottomRight) -> Corner.BottomRight
                                else -> null
                            }
                            draggingCenter = draggingCorner == null && Rect(
                                topLeft,
                                bottomRight
                            ).contains(offset)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            when (draggingCorner) {
                                Corner.TopLeft -> {
                                    topLeft += dragAmount
                                    topRight = topRight.copy(y = topLeft.y)
                                    bottomLeft = bottomLeft.copy(x = topLeft.x)
                                }

                                Corner.TopRight -> {
                                    topRight += dragAmount
                                    topLeft = topLeft.copy(y = topRight.y)
                                    bottomRight = bottomRight.copy(x = topRight.x)
                                }

                                Corner.BottomLeft -> {
                                    bottomLeft += dragAmount
                                    topLeft = topLeft.copy(x = bottomLeft.x)
                                    bottomRight = bottomRight.copy(y = bottomLeft.y)
                                }

                                Corner.BottomRight -> {
                                    bottomRight += dragAmount
                                    topRight = topRight.copy(x = bottomRight.x)
                                    bottomLeft = bottomLeft.copy(y = bottomRight.y)
                                }

                                null -> {
                                    topLeft += dragAmount
                                    topRight += dragAmount
                                    bottomLeft += dragAmount
                                    bottomRight += dragAmount
                                }
                            }
                        },
                        onDragEnd = {
                            draggingCorner = null
                            draggingCenter = false
                        }
                    )
                }
        ) {
            val rectSize = Size(width = topRight.x - topLeft.x, height = bottomLeft.y - topLeft.y)
            drawRect(
                color = Color.Cyan,
                topLeft = topLeft,
                size = rectSize,
                style = Stroke(width = 4f)
            )
            drawCorner(topLeft)
            drawCorner(topRight)
            drawCorner(bottomLeft)
            drawCorner(bottomRight)
        }
    }
}

enum class Corner {
    TopLeft, TopRight, BottomLeft, BottomRight
}

fun Offset.isNear(point: Offset, threshold: Float = 50f): Boolean {
    return (this - point).getDistance() <= threshold
}

fun DrawScope.drawCorner(center: Offset) {
    drawCircle(color = Color.Cyan, radius = 25f, center = center)
}