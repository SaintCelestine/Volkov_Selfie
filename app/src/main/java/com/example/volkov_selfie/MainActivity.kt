package com.example.volkov_selfie

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.example.volkov_selfie.ui.theme.Volkov_SelfieTheme
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : ComponentActivity() {
    private var photoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Volkov_SelfieTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainContent(
                        modifier = Modifier.padding(innerPadding),
                        onTakeSelfie = { checkPermissionsAndTakeSelfie() },
                        onSendSelfie = { dispatchSendEmailIntent() },
                        photoUri = photoUri
                    )
                }
            }
        }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && photoUri != null) {
            Log.d("MainActivity", "Image captured successfully.")
            setContent {
                Volkov_SelfieTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        MainContent(
                            modifier = Modifier.padding(innerPadding),
                            onTakeSelfie = { checkPermissionsAndTakeSelfie() },
                            onSendSelfie = { dispatchSendEmailIntent() },
                            photoUri = photoUri
                        )
                    }
                }
            }
        } else {
            Log.e("MainActivity", "Failed to capture image.")
        }
    }

    private fun checkPermissionsAndTakeSelfie() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        } else {
            dispatchTakePictureIntent()
        }
    }

    private fun dispatchTakePictureIntent() {
        try {
            val photoFile = createImageFile()
            photoUri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                photoFile
            )
            photoUri?.let {
                Log.d("MainActivity", "Launching camera intent.")
                takePictureLauncher.launch(it)
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            Log.e("MainActivity", "Error occurred while creating the File")
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    private fun dispatchSendEmailIntent() {
        photoUri?.let { uri ->
            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/image"
                putExtra(Intent.EXTRA_EMAIL, arrayOf("hodovychenko@op.edu.ua"))
                putExtra(Intent.EXTRA_SUBJECT, "DigiJED Волков Євгеній")
                putExtra(Intent.EXTRA_TEXT, "Я люблю чай та хочу спати\n\n репозиторій: https://github.com/SaintCelestine/Volkov_Selfie.git")
                putExtra(Intent.EXTRA_STREAM, uri)
            }
            startActivity(Intent.createChooser(emailIntent, "Відправити листа на пошту"))
        }
    }
}

@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    onTakeSelfie: () -> Unit,
    onSendSelfie: () -> Unit,
    photoUri: Uri?
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        photoUri?.let {
            val context = LocalContext.current
            val bitmap = remember(photoUri) {
                try {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("MainContent", "Error occurred while getting the bitmap")
                    null
                }
            }
            bitmap?.let { image ->
                Image(bitmap = image.asImageBitmap(), contentDescription = null, modifier = Modifier.size(250.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onTakeSelfie) {
            Text("Сфоткати селфі")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onSendSelfie) {
            Text("Відправити селфі")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainContentPreview() {
    Volkov_SelfieTheme {
        MainContent(
            onTakeSelfie = {},
            onSendSelfie = {},
            photoUri = null
        )
    }
}
