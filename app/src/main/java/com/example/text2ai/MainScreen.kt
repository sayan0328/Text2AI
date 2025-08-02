package com.example.text2ai

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.text2ai.ui.theme.ThemeBlue
import com.example.text2ai.ui.theme.ThemeDarkGray
import com.example.text2ai.ui.theme.ThemeDarkerGray
import com.example.text2ai.ui.theme.ThemeGray
import com.example.text2ai.ui.theme.ThemeLightGray
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(innerPadding: PaddingValues) {
    val context = LocalContext.current
    var extractedText by remember { mutableStateOf("") }
    var aiResponse by remember { mutableStateOf("AI response will appear here") }
    var userInput by remember { mutableStateOf("") }
    val permissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val expanded by remember { mutableStateOf(false) }
    var openCamera by remember { mutableStateOf(false) }
    var showCropScreen by remember { mutableStateOf(false) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            try {
                val inputStream = uri?.let { context.contentResolver.openInputStream(it) }
                capturedBitmap = BitmapFactory.decodeStream(inputStream)
                openCamera = false
                showCropScreen = true
            } catch (e: Exception) {
                Log.e("MainScreen", "Error picking image", e)
            }
        }
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(ThemeDarkerGray)
            .padding(16.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            }
        ,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(120.dp))
            Button(
                onClick = { openCamera = true },
                modifier = Modifier.padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Image(
                    painter = painterResource(R.drawable.camera_icon),
                    contentDescription = "Camera Icon",
                    modifier = Modifier.size(96.dp)
                )
            }

            HorizontalDivider(
                thickness = 2.dp,
                color = ThemeGray,
                modifier = Modifier.padding(start = 64.dp, end = 64.dp, bottom = 12.dp)
            )
            Button(
                onClick = {
                    imagePickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )

                },
                modifier = Modifier.padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Image(
                    painter = painterResource(R.drawable.gallery_icon),
                    contentDescription = "Gallery Icon",
                    modifier = Modifier.size(96.dp)
                )
            }
            Spacer(modifier = Modifier.height(120.dp))
            Text(
                text = "Extracted Text:",
                modifier = Modifier.padding(top = 16.dp),
                color = Color.White
            )
            TextField(
                value = userInput,
                onValueChange = { userInput = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                label = { Text("Edit text before sending") },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = ThemeDarkGray,
                    focusedTextColor = ThemeLightGray,
                    unfocusedTextColor = ThemeLightGray,
                    focusedLabelColor = ThemeLightGray,
                    unfocusedLabelColor = ThemeLightGray,
                    cursorColor = ThemeLightGray,
                    focusedIndicatorColor = ThemeLightGray,
                    unfocusedIndicatorColor = ThemeLightGray,
                ),
                maxLines = 4,
            )
            Button(
                onClick = {
                    if (userInput == "") {
                        Toast.makeText(context, "Text field is empty", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    aiResponse = "Waiting for response..."
                    GeminiService.askAI(userInput) { response ->
                        aiResponse = response ?: "Error getting AI response"
                    }
                },
                modifier = Modifier
                    .padding(top = 12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ThemeBlue)
            ) {
                Text(
                    text = "Send to AI",
                    color = ThemeDarkerGray
                )
            }
                Text(
                    text = aiResponse,
                    modifier = Modifier.padding(8.dp),
                    color = Color.White
                )

        }
    }

    when {
        openCamera -> {
            CameraPreview(
                modifier = Modifier.padding(innerPadding),
                onImageCaptured = { bitmap ->
                    capturedBitmap = bitmap
                    openCamera = false
                    showCropScreen = true
                },
                onClose = {
                    openCamera = false
                }
            )
        }

        showCropScreen && (capturedBitmap != null) -> {
            CropImage(
                modifier = Modifier.padding(innerPadding),
                imageBitmap = capturedBitmap!!.asImageBitmap(),
                onCropComplete = { croppedImage ->
                    extractTextFromImage(croppedImage) { text ->
                        if(text.isNotBlank()) {
                            extractedText = text
                            userInput = text
                        } else {
                            Toast.makeText(context, "No text found", Toast.LENGTH_SHORT).show()
                        }
                    }
                    showCropScreen = false
                    capturedBitmap = null
//                    capturedBitmap = croppedImage.asAndroidBitmap()
                },
                onClose = {
                    showCropScreen = false
                    capturedBitmap = null
                }
            )
        }

        // To examine the cropped image
        else ->
            capturedBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
    }

}