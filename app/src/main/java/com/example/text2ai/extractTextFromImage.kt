package com.example.text2ai

import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

@OptIn(ExperimentalGetImage::class)
fun extractTextFromImage(image: ImageProxy, onResult: (String) -> Unit) {
    val mediaImage = image.image
    if(mediaImage != null) {
        val imageInput = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(imageInput)
            .addOnSuccessListener { visionText ->
                val extractedText = visionText.text
                onResult(extractedText)
            }
            .addOnFailureListener { e ->
                Log.e("OCR", "Text recognition failed", e)
            }
            .addOnCompleteListener {
                image.close()
            }
    }
}