package com.example.smart_emap.ui.mes.inspection

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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

@Composable
fun InspectionPersistentScannerOverlay(
    visible: Boolean,
    lastCode: String,
    lastAtDisplay: String,
    onScanned: (String) -> Unit,
    cameraPermissionRequired: String,
    cameraPermissionWaiting: String,
    cameraStartFailed: String,
    persistentScannerWaiting: String,
    persistentScannerLatest: String,
    persistentScannerTime: String,
    modifier: Modifier = Modifier,
) {
    // remember* は visible に関係なく毎回同じ順で呼ぶ（早期 return 禁止）
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
    val previewView = remember { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }
    // 読取後は画面からQRが消えるまで再発火しない（移動中の再検出を抑止）
    val lastEmitAt = remember { AtomicLong(0L) }
    val latchActive = remember { AtomicBoolean(false) }
    val clearSinceMs = remember { AtomicLong(0L) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasPermission = granted
        cameraError = if (granted) null else cameraPermissionRequired
        if (granted) cameraKey++
    }

    LaunchedEffect(visible) {
        if (!visible) return@LaunchedEffect
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    DisposableEffect(visible, hasPermission, useFrontCamera, cameraKey) {
        if (!visible || !hasPermission) {
            onDispose { }
            return@DisposableEffect onDispose { }
        }
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
                val selector = if (useFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
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
                    val mediaImage = imageProxy.image
                    if (mediaImage == null) {
                        imageProxy.close()
                        return@setAnalyzer
                    }
                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    barcodeScanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            val value = barcodes
                                .asSequence()
                                .mapNotNull { it.rawValue?.trim() }
                                .firstOrNull { it.isNotEmpty() }
                            val now = System.currentTimeMillis()
                            if (value != null) {
                                // QRがまだ映っている間はクリア待ちをリセット
                                clearSinceMs.set(0L)
                                if (latchActive.get()) return@addOnSuccessListener
                                // 2秒以内の連続読取を抑止
                                if (now - lastEmitAt.get() < SCAN_MIN_INTERVAL_MS) {
                                    return@addOnSuccessListener
                                }
                                lastEmitAt.set(now)
                                latchActive.set(true)
                                onScanned(value)
                            } else if (latchActive.get()) {
                                // 検出消失が一定時間続いたら次の読取を許可
                                val since = clearSinceMs.get()
                                if (since == 0L) {
                                    clearSinceMs.set(now)
                                } else if (now - since >= SCAN_CLEAR_HOLD_MS) {
                                    latchActive.set(false)
                                    clearSinceMs.set(0L)
                                }
                            }
                        }
                        .addOnCompleteListener { imageProxy.close() }
                }
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(lifecycleOwner, selector, preview, analysis)
            } catch (_: Exception) {
                cameraError = cameraStartFailed
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            executor.shutdown()
            barcodeScanner.close()
            runCatching { cameraProvider?.unbindAll() }
        }
    }

    if (!visible) return

    Box(
        modifier = modifier
            .size(width = 220.dp, height = 178.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xCC0B1220))
            .border(1.dp, Color(0xFF2A3444), RoundedCornerShape(12.dp))
            .padding(8.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "QR読取（稼働中）",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                IconButton(
                    onClick = {
                        useFrontCamera = !useFrontCamera
                        cameraKey++
                    },
                    modifier = Modifier.size(22.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
            if (!hasPermission) {
                Text(cameraPermissionWaiting, color = Color(0xFFFCA5A5), fontSize = 10.sp)
            } else if (cameraError != null) {
                Text(cameraError ?: "", color = Color(0xFFFCA5A5), fontSize = 10.sp)
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(112.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center,
                ) {
                    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxWidth().height(112.dp))
                    Box(
                        modifier = Modifier
                            .size(width = 132.dp, height = 68.dp)
                            .border(2.dp, Color(0xFF10B981), RoundedCornerShape(6.dp)),
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (lastCode.isBlank()) {
                    persistentScannerWaiting
                } else {
                    persistentScannerLatest.replace("{code}", lastCode)
                },
                color = Color(0xFFE5E7EB),
                fontSize = 10.sp,
                maxLines = 1,
            )
            if (lastAtDisplay.isNotBlank()) {
                Text(
                    text = persistentScannerTime.replace("{time}", lastAtDisplay),
                    color = Color(0xFF9CA3AF),
                    fontSize = 9.sp,
                )
            }
        }
    }
}

@Composable
fun InspectionQrScanSuccessBanner(
    info: QrScanSuccessInfo?,
    successTitle: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pulse = rememberInfiniteTransition(label = "qrSuccessPulse")
    val glowAlpha by pulse.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "qrSuccessGlow",
    )
    val glowScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "qrSuccessScale",
    )

    AnimatedVisibility(
        visible = info != null,
        enter = scaleIn(
            initialScale = 0.82f,
            animationSpec = tween(280, easing = FastOutSlowInEasing),
        ) + fadeIn(tween(220)),
        exit = scaleOut(targetScale = 0.9f, animationSpec = tween(200)) + fadeOut(tween(180)),
        modifier = modifier,
    ) {
        if (info == null) return@AnimatedVisibility
        val cardShape = RoundedCornerShape(22.dp)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 20.dp),
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        scaleX = glowScale
                        scaleY = glowScale
                        alpha = glowAlpha * 0.55f
                    }
                    .border(5.dp, Color(0xFF22C55E), cardShape),
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .widthIn(min = 300.dp, max = 440.dp)
                    .shadow(28.dp, cardShape, spotColor = Color(0xCC16A34A))
                    .clip(cardShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFFFFFFF), Color(0xFFECFDF5), Color(0xFFD1FAE5)),
                        ),
                    )
                    .border(
                        width = 4.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                Color(0xFF22C55E),
                                Color(0xFFFACC15),
                                Color(0xFF10B981),
                                Color(0xFF22C55E),
                            ),
                        ),
                        shape = cardShape,
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    )
                    .padding(horizontal = 28.dp, vertical = 22.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(10.dp, CircleShape, spotColor = Color(0x8816A34A))
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF22C55E), Color(0xFF059669))),
                        )
                        .border(3.dp, Color(0xFFFACC15), CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp),
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = successTitle,
                    color = Color(0xFF047857),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = info.productCd,
                    color = Color(0xFF065F46),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFD1FAE5))
                        .border(1.5.dp, Color(0xFF34D399), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                )
                if (info.productName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = info.productName,
                        color = Color(0xFF0F766E),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = info.timeDisplay,
                    color = Color(0xFF64748B),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
fun InspectionQrScanCooldownBanner(
    info: QrScanNoticeInfo?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pulse = rememberInfiniteTransition(label = "qrCooldownPulse")
    val glowAlpha by pulse.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "qrCooldownGlow",
    )
    val glowScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "qrCooldownScale",
    )

    AnimatedVisibility(
        visible = info != null,
        enter = scaleIn(
            initialScale = 0.82f,
            animationSpec = tween(280, easing = FastOutSlowInEasing),
        ) + fadeIn(tween(220)),
        exit = scaleOut(targetScale = 0.9f, animationSpec = tween(200)) + fadeOut(tween(180)),
        modifier = modifier,
    ) {
        if (info == null) return@AnimatedVisibility
        val cardShape = RoundedCornerShape(22.dp)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 20.dp),
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        scaleX = glowScale
                        scaleY = glowScale
                        alpha = glowAlpha * 0.55f
                    }
                    .border(5.dp, Color(0xFFF59E0B), cardShape),
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .widthIn(min = 300.dp, max = 440.dp)
                    .shadow(28.dp, cardShape, spotColor = Color(0xCCD97706))
                    .clip(cardShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFFFFFFF), Color(0xFFFFFBEB), Color(0xFFFEF3C7)),
                        ),
                    )
                    .border(
                        width = 4.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                Color(0xFFF59E0B),
                                Color(0xFFFACC15),
                                Color(0xFFEA580C),
                                Color(0xFFF59E0B),
                            ),
                        ),
                        shape = cardShape,
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    )
                    .padding(horizontal = 28.dp, vertical = 22.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(10.dp, CircleShape, spotColor = Color(0x88D97706))
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706))),
                        )
                        .border(3.dp, Color(0xFFFACC15), CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp),
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = info.title,
                    color = Color(0xFFB45309),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = info.detail,
                    color = Color(0xFF92400E),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
fun InspectionQrScanNoticeBanner(
    info: QrScanNoticeInfo?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = info != null,
        enter = slideInVertically(initialOffsetY = { -it }, animationSpec = tween(320)) + fadeIn(tween(280)),
        exit = slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(260)) + fadeOut(tween(220)),
        modifier = modifier,
    ) {
        if (info == null) return@AnimatedVisibility

        val (bgBrush, border, icon, titleColor) = when (info.kind) {
            QrScanNoticeKind.Mismatch -> Quadruple(
                Brush.horizontalGradient(listOf(Color(0xFFFFF1F2), Color(0xFFFFE4E6), Color(0xFFFFF1F2))),
                Color(0xFFFCA5A5),
                Icons.Default.Close,
                Color(0xFFDC2626),
            )
            QrScanNoticeKind.Cooldown -> Quadruple(
                Brush.horizontalGradient(listOf(Color(0xFFFFFBEB), Color(0xFFFFF3C7), Color(0xFFFFFBEB))),
                Color(0xFFFBBF24),
                Icons.Default.Schedule,
                Color(0xFFB45309),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .widthIn(max = 720.dp)
                .shadow(10.dp, RoundedCornerShape(12.dp), spotColor = Color(0x400D9488))
                .clip(RoundedCornerShape(12.dp))
                .background(bgBrush)
                .border(1.dp, border, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = titleColor,
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    text = "${info.title}　${info.detail}",
                    color = titleColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

/** 同一QRの連続検出を抑止する最短間隔 */
private const val SCAN_MIN_INTERVAL_MS = 2_000L

/** QRが画面から消えてから再アームするまでの保持時間（移開中のちらつき対策） */
private const val SCAN_CLEAR_HOLD_MS = 400L
