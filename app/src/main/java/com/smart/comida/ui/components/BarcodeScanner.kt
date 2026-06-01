package com.smart.comida.ui.components

import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(markerClass = [ExperimentalGetImage::class])
@Composable
fun BarcodeScannerView(
    onBarcodeDetected: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                val previewView = PreviewView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val scanner = BarcodeScanning.getClient(
                        BarcodeScannerOptions.Builder()
                            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                            .build()
                    )

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor) { imageProxy ->
                                val mediaImage = imageProxy.image
                                if (mediaImage != null) {
                                    val image = InputImage.fromMediaImage(
                                        mediaImage,
                                        imageProxy.imageInfo.rotationDegrees
                                    )

                                    scanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            if (barcodes.isNotEmpty()) {
                                                barcodes.firstOrNull()?.rawValue?.let { barcode ->
                                                    onBarcodeDetected(barcode)
                                                }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("BarcodeScanner", "Error al escanear", e)
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                        }
                                } else {
                                    imageProxy.close()
                                }
                            }
                        }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        Log.e("BarcodeScanner", "Error al iniciar cámara", e)
                    }
                }, ContextCompat.getMainExecutor(context))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        BarcodeScannerOverlay()

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.35f), shape = MaterialTheme.shapes.large)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Cerrar",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun BarcodeScannerOverlay() {
    val colorScheme = MaterialTheme.colorScheme

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scanWidth = maxWidth * 0.82f
        val scanHeight = 180.dp
        val frameTopOffset = maxHeight * 0.34f

        ScannerMask(
            modifier = Modifier.fillMaxSize(),
            topOffset = frameTopOffset,
            scanWidth = scanWidth,
            scanHeight = scanHeight
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 24.dp)
                .offset(y = frameTopOffset - 96.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Escaneando producto",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Coloca el código de barras dentro del recuadro",
                color = Color.White.copy(alpha = 0.86f),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        ScannerFrame(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = frameTopOffset)
                .width(scanWidth)
                .height(scanHeight),
            accentColor = colorScheme.primary
        )

        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = frameTopOffset + scanHeight + 28.dp),
            color = Color.Black.copy(alpha = 0.45f),
            contentColor = Color.White,
            shape = MaterialTheme.shapes.large
        ) {
            Text(
                text = "Buscando el código...",
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ScannerMask(
    modifier: Modifier = Modifier,
    topOffset: Dp,
    scanWidth: Dp,
    scanHeight: Dp
) {
    Canvas(modifier = modifier) {
        val frameWidth = scanWidth.toPx()
        val frameHeight = scanHeight.toPx()
        val frameLeft = (size.width - frameWidth) / 2f
        val frameTop = topOffset.toPx()
        val frameRight = frameLeft + frameWidth
        val frameBottom = frameTop + frameHeight
        val scrim = Color.Black.copy(alpha = 0.58f)

        drawRect(scrim, topLeft = Offset.Zero, size = Size(size.width, frameTop))
        drawRect(scrim, topLeft = Offset(0f, frameBottom), size = Size(size.width, size.height - frameBottom))
        drawRect(scrim, topLeft = Offset(0f, frameTop), size = Size(frameLeft, frameHeight))
        drawRect(scrim, topLeft = Offset(frameRight, frameTop), size = Size(size.width - frameRight, frameHeight))
    }
}

@Composable
private fun ScannerFrame(
    modifier: Modifier = Modifier,
    accentColor: Color
) {
    val transition = rememberInfiniteTransition(label = "barcodeScannerLine")
    val scanLineProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "barcodeScannerLineProgress"
    )

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(Color.White.copy(alpha = 0.06f))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cornerLength = 34.dp.toPx()
            val strokeWidth = 4.dp.toPx()
            val cornerRadius = 18.dp.toPx()
            val lineY = size.height * scanLineProgress
            val linePadding = 18.dp.toPx()

            drawRoundRect(
                color = Color.White.copy(alpha = 0.36f),
                size = size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
                style = Stroke(width = 1.dp.toPx())
            )

            drawLine(accentColor, Offset(0f, 0f), Offset(cornerLength, 0f), strokeWidth, StrokeCap.Round)
            drawLine(accentColor, Offset(0f, 0f), Offset(0f, cornerLength), strokeWidth, StrokeCap.Round)
            drawLine(accentColor, Offset(size.width, 0f), Offset(size.width - cornerLength, 0f), strokeWidth, StrokeCap.Round)
            drawLine(accentColor, Offset(size.width, 0f), Offset(size.width, cornerLength), strokeWidth, StrokeCap.Round)
            drawLine(accentColor, Offset(0f, size.height), Offset(cornerLength, size.height), strokeWidth, StrokeCap.Round)
            drawLine(accentColor, Offset(0f, size.height), Offset(0f, size.height - cornerLength), strokeWidth, StrokeCap.Round)
            drawLine(accentColor, Offset(size.width, size.height), Offset(size.width - cornerLength, size.height), strokeWidth, StrokeCap.Round)
            drawLine(accentColor, Offset(size.width, size.height), Offset(size.width, size.height - cornerLength), strokeWidth, StrokeCap.Round)

            drawLine(
                color = accentColor.copy(alpha = 0.9f),
                start = Offset(linePadding, lineY),
                end = Offset(size.width - linePadding, lineY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
