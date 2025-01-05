package com.noryangjin.simplesignage

import android.graphics.BitmapFactory
import android.provider.ContactsContract.CommonDataKinds.Website.URL
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.URL

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PhotoScreen() {
    var currentBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = Unit) {
        while (true) {
            try {
                isLoading = true
                withContext(Dispatchers.IO) {
                    try {
                        val url = URL("https://picsum.photos/1920/1080?${System.currentTimeMillis()}")
                        Log.d("PhotoScreen", "Downloading image from: $url")

                        val connection = url.openConnection()
                        connection.connectTimeout = 5000
                        connection.readTimeout = 5000
                        connection.connect()

                        val inputStream = connection.getInputStream()
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        if (bitmap != null) {
                            currentBitmap = bitmap.asImageBitmap()
                            Log.d("PhotoScreen", "Image downloaded successfully")
                        } else {
                            Log.e("PhotoScreen", "Failed to decode bitmap")
                        }
                        inputStream.close()
                    } catch (e: Exception) {
                        Log.e("PhotoScreen", "Error downloading image: ${e.message}")
                        e.printStackTrace()
                    }
                }
                isLoading = false
                delay(1000)
            } catch (e: Exception) {
                Log.e("PhotoScreen", "Error in LaunchedEffect: ${e.message}")
                isLoading = false
                delay(1000)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        }

        currentBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap,
                contentDescription = "Random Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

