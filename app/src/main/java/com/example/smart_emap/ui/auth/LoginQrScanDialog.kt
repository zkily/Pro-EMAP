package com.example.smart_emap.ui.auth

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.smart_emap.ui.theme.LoginColors
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun LoginQrScanDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onScanned: (String) -> Unit,
) {
    // remember* 必须始终按相同顺序调用；不可在 if (!visible) return 之后才 remember。
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var useFrontCamera by remember { mutableStateOf(true) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    var cameraKey by remember { mutableIntStateOf(0) }
    val scannedOnce = remember { AtomicBoolean(false) }
    val previewView = remember {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasPermission = granted
        cameraError = if (granted) null else "カメラ権限が必要です"
        if (granted) cameraKey++
    }

    LaunchedEffect(visible) {
        if (!visible) return@LaunchedEffect
        scannedOnce.set(false)
        cameraError = null
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    DisposableEffect(visible, hasPermission, useFrontCamera, cameraKey) {
        if (!visible || !hasPermission) {
            return@DisposableEffect onDispose { }
        }
        scannedOnce.set(false)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor = Executors.newSingleThreadExecutor()
        val barcodeScanner = BarcodeScanning.getClient()
        var cameraProvider: ProcessCameraProvider? = null

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val preferred = if (useFrontCamera) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }
                val fallback = if (useFrontCamera) {
                    CameraSelector.DEFAULT_BACK_CAMERA
                } else {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                }
                val analysis = ImageAnalysis.Builder()
                    .setResolutionSelector(
                        ResolutionSelector.Builder()
                            .setResolutionStrategy(
                                ResolutionStrategy(
                                    Size(1280, 720),
                                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER,
                                ),
                            )
                            .build(),
                    )
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(executor) { imageProxy ->
                    if (scannedOnce.get()) {
                        imageProxy.close()
                        return@setAnalyzer
                    }
                    val mediaImage = imageProxy.image
                    if (mediaImage == null) {
                        imageProxy.close()
                        return@setAnalyzer
                    }
                    val image = InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.imageInfo.rotationDegrees,
                    )
                    barcodeScanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            if (scannedOnce.get()) return@addOnSuccessListener
                            val value = barcodes
                                .asSequence()
                                .mapNotNull { it.rawValue?.trim() }
                                .firstOrNull { it.isNotEmpty() }
                            if (value != null && scannedOnce.compareAndSet(false, true)) {
                                onScanned(value)
                            }
                        }
                        .addOnCompleteListener { imageProxy.close() }
                }
                cameraProvider?.unbindAll()
                try {
                    cameraProvider?.bindToLifecycle(lifecycleOwner, preferred, preview, analysis)
                } catch (_: Exception) {
                    cameraProvider?.unbindAll()
                    cameraProvider?.bindToLifecycle(lifecycleOwner, fallback, preview, analysis)
                }
                cameraError = null
            } catch (e: Exception) {
                cameraError = e.message?.takeIf { it.isNotBlank() }
                    ?: "カメラを起動できませんでした"
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            scannedOnce.set(false)
            executor.shutdown()
            barcodeScanner.close()
            runCatching { cameraProvider?.unbindAll() }
        }
    }

    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("QRコードでログイン", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "ログインQRを枠内に合わせてください",
                    fontSize = 12.sp,
                    color = LoginColors.TextMuted,
                )
                if (!hasPermission) {
                    Text("カメラ権限が必要です", fontSize = 12.sp, color = Color(0xFFDC2626))
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("権限を許可")
                    }
                } else if (cameraError != null) {
                    Text(cameraError!!, fontSize = 12.sp, color = Color(0xFFDC2626))
                    Button(onClick = {
                        cameraError = null
                        cameraKey++
                    }) {
                        Text("カメラを再起動")
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center,
                    ) {
                        AndroidView(
                            factory = { previewView },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                        )
                        Box(
                            modifier = Modifier
                                .size(width = 220.dp, height = 220.dp)
                                .border(2.dp, Color(0xFF10B981), RoundedCornerShape(8.dp)),
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            useFrontCamera = !useFrontCamera
                            cameraKey++
                        },
                        enabled = hasPermission,
                    ) {
                        Icon(Icons.Default.Cameraswitch, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(if (useFrontCamera) "背面カメラ" else "前面カメラ", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("閉じる") }
        },
    )
}
