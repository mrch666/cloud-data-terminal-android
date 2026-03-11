package com.cloudterminal.presentation.components

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

/**
 * Улучшенный компонент CameraPreview с поддержкой:
 * - Управления жизненным циклом камеры
 * - Обработки разрешений
 * - Обработки ошибок
 * - Конфигурации сканера
 */
@Composable
fun CameraPreviewNew(
    modifier: Modifier = Modifier,
    isCameraActive: Boolean = true,
    lensFacing: CameraSelector.LensFacing = CameraSelector.LENS_FACING_BACK,
    onBarcodeScanned: (Barcode) -> Unit,
    onCameraError: (Throwable) -> Unit = {},
    onCameraInitialized: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var isInitialized by remember { mutableStateOf(false) }
    
    // Инициализация CameraProvider
    LaunchedEffect(Unit) {
        try {
            val provider = withContext(Dispatchers.IO) {
                ProcessCameraProvider.getInstance(context).get()
            }
            cameraProvider = provider
            isInitialized = true
            onCameraInitialized()
        } catch (e: Exception) {
            onCameraError(e)
            Log.e("CameraPreviewNew", "Failed to initialize camera", e)
        }
    }
    
    // Очистка при уничтожении
    DisposableEffect(Unit) {
        onDispose {
            cameraProvider?.unbindAll()
        }
    }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (isInitialized && isCameraActive) {
            AndroidView(
                factory = { ctx ->
                    CameraPreviewView(
                        context = ctx,
                        cameraProvider = cameraProvider!!,
                        lifecycleOwner = lifecycleOwner,
                        lensFacing = lensFacing,
                        onBarcodeScanned = onBarcodeScanned,
                        onCameraError = onCameraError
                    )
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Внутренний класс для управления View камеры
 */
private class CameraPreviewView(
    private val context: Context,
    private val cameraProvider: ProcessCameraProvider,
    private val lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    private val lensFacing: CameraSelector.LensFacing,
    private val onBarcodeScanned: (Barcode) -> Unit,
    private val onCameraError: (Throwable) -> Unit
) : PreviewView(context) {
    
    private val analysisExecutor = Executors.newSingleThreadExecutor()
    private var isBound = false
    
    init {
        setupCamera()
    }
    
    private fun setupCamera() {
        try {
            // Настройка сканера штрих-кодов
            val barcodeScannerOptions = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_CODE_128,
                    Barcode.FORMAT_CODE_39,
                    Barcode.FORMAT_CODE_93,
                    Barcode.FORMAT_EAN_13,
                    Barcode.FORMAT_EAN_8,
                    Barcode.FORMAT_UPC_A,
                    Barcode.FORMAT_UPC_E,
                    Barcode.FORMAT_QR_CODE,
                    Barcode.FORMAT_DATA_MATRIX,
                    Barcode.FORMAT_PDF417
                )
                .build()
            
            val barcodeScanner = BarcodeScanning.getClient(barcodeScannerOptions)
            
            // Настройка превью
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(surfaceProvider)
            
            // Настройка анализа изображений
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetRotation(display.rotation)
                .build()
            
            imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image = InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.imageInfo.rotationDegrees
                    )
                    
                    barcodeScanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            for (barcode in barcodes) {
                                onBarcodeScanned(barcode)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("CameraPreviewView", "Barcode scanning error", e)
                            onCameraError(e)
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                }
            }
            
            // Выбор камеры
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()
            
            // Unbind предыдущих use cases
            cameraProvider.unbindAll()
            
            // Bind use cases к камере
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
            
            isBound = true
            Log.d("CameraPreviewView", "Camera successfully bound")
            
        } catch (e: Exception) {
            Log.e("CameraPreviewView", "Camera setup failed", e)
            onCameraError(e)
        }
    }
    
    fun release() {
        if (isBound) {
            cameraProvider.unbindAll()
            analysisExecutor.shutdown()
            isBound = false
            Log.d("CameraPreviewView", "Camera released")
        }
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        release()
    }
}

/**
 * Вспомогательная функция для проверки доступности камеры
 */
fun isCameraAvailable(context: Context): Boolean {
    return try {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        cameraProvider.hasCamera(cameraSelector)
    } catch (e: Exception) {
        false
    }
}

/**
 * Получение списка доступных камер
 */
fun getAvailableCameras(context: Context): List<CameraSelector.LensFacing> {
    val availableCameras = mutableListOf<CameraSelector.LensFacing>()
    
    return try {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        
        if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
            availableCameras.add(CameraSelector.LENS_FACING_BACK)
        }
        
        if (cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
            availableCameras.add(CameraSelector.LENS_FACING_FRONT)
        }
        
        availableCameras
    } catch (e: Exception) {
        emptyList()
    }
}