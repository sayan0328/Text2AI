package com.example.text2ai

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.text2ai.ui.theme.ThemeDarkerGray

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CropImage(
    modifier: Modifier,
    imageBitmap: ImageBitmap,
    onCropComplete: (ImageBitmap) -> Unit,
    onClose: () -> Unit
) {
    var image by remember {
        mutableStateOf(imageBitmap)
    }

    var topLeft by remember { mutableStateOf(Offset(400f, 400f)) }
    var topRight by remember { mutableStateOf(Offset(800f, 400f)) }
    var bottomLeft by remember { mutableStateOf(Offset(400f, 800f)) }
    var bottomRight by remember { mutableStateOf(Offset(800f, 800f)) }

    var draggingCorner by remember { mutableStateOf<Corner?>(null) }
    var draggingCenter by remember { mutableStateOf(false) }



    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeDarkerGray)
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = image,
            contentDescription = "Captured Image",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        Canvas(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(onDragStart = { offset ->
                    draggingCorner = when {
                        offset.isNear(topLeft) -> Corner.TopLeft
                        offset.isNear(topRight) -> Corner.TopRight
                        offset.isNear(bottomLeft) -> Corner.BottomLeft
                        offset.isNear(bottomRight) -> Corner.BottomRight
                        else -> null
                    }

                    draggingCenter = draggingCorner == null && Rect(
                        topLeft, bottomRight
                    ).contains(offset)
                }, onDrag = { change, dragAmount ->
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

                        null -> if (draggingCenter) {
                            topLeft += dragAmount
                            topRight += dragAmount
                            bottomLeft += dragAmount
                            bottomRight += dragAmount
                        }
                    }
                }, onDragEnd = {
                    draggingCorner = null
                    draggingCenter = false
                })
            }) {
            val rectSize = Size(
                width = topRight.x - topLeft.x, height = bottomLeft.y - topLeft.y
            )

            drawRect(
                color = Color.White, topLeft = topLeft, size = rectSize, style = Stroke(width = 4f)
            )

            drawHandle(topLeft)
            drawHandle(topRight)
            drawHandle(bottomLeft)
            drawHandle(bottomRight)
        }

        Image(
            painter = painterResource(R.drawable.done_icon),
            contentDescription = "Shutter Icon",
            modifier = Modifier
                .padding(25.dp)
                .size(70.dp)
                .clip(RoundedCornerShape(70.dp))
                .align(Alignment.BottomCenter)
                .clickable{
                    val croppedBitmap = cropBitmap(
                        imageBitmap,
                        Rect(topLeft, bottomRight),
                        canvasWidth = constraints.maxWidth.toFloat(),
                        canvasHeight = constraints.maxHeight.toFloat()
                    )
                    onCropComplete(croppedBitmap)
                }
        )

        Image(
            painter = painterResource(R.drawable.close_icon),
            contentDescription = "Close Icon",
            modifier = Modifier
                .padding(12.dp)
                .size(16.dp)
                .align(Alignment.TopEnd)
                .clickable{
                    onClose()
                }
        )
    }
}

enum class Corner {
    TopLeft, TopRight, BottomLeft, BottomRight
}

fun Offset.isNear(point: Offset, threshold: Float = 50f): Boolean {
    return (this - point).getDistance() <= threshold
}

fun DrawScope.drawHandle(center: Offset) {
    drawCircle(
        color = Color.White, radius = 20f, center = center
    )
    drawCircle(
        color = Color.Cyan, radius = 15f, center = center
    )
}