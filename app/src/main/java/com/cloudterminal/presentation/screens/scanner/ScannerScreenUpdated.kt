package com.cloudterminal.presentation.screens.scanner

import androidx.camera.core.CameraSelector
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CameraFront
import androidx.compose.material.icons.filled.CameraRear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cloudterminal.presentation.components.CameraPreviewNew
import com.cloudterminal.presentation.components.ScannerOverlay
import kotlinx.coroutines.launch

/**
 * Обновленный экран сканирования с интеграцией CameraX
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreenUpdated(
    cameraViewModel: CameraViewModel = hiltViewModel(),
    scannerViewModel: ScannerViewModel = hiltViewModel(),
    onNavigateToInventory: () -> Unit,
    onNavigateToProducts: () -> Unit
) {
    val context = LocalContext.current
    val cameraState by cameraViewModel.uiState.collectAsState()
    val scannerState by scannerViewModel.uiState.collectAsState()
    val scannedItems by scannerViewModel.scannedItems.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Проверка доступности камеры при запуске
    LaunchedEffect(Unit) {
        cameraViewModel.checkCameraAvailability(context)
    }
    
    // Обработка отсканированных штрих-кодов
    LaunchedEffect(cameraState.lastScannedBarcode) {
        cameraState.lastScannedBarcode?.let { barcodeValue ->
            // Здесь можно добавить логику обработки штрих-кода
            // Например, поиск товара и добавление в scannedItems
            scope.launch {
                snackbarHostState.showSnackbar(
                    "Отсканирован штрих-код: $barcodeValue"
                )
            }
        }
    }
    
    // Обработка ошибок камеры
    LaunchedEffect(cameraState.cameraError) {
        cameraState.cameraError?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    "Ошибка камеры: $error",
                    withDismissAction = true
                )
            }
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Cloud Data Terminal") },
                actions = {
                    // Кнопка переключения камеры
                    IconButton(
                        onClick = { cameraViewModel.toggleCamera() },
                        enabled = cameraState.availableCameras.size > 1
                    ) {
                        Icon(
                            imageVector = when (cameraState.lensFacing) {
                                CameraSelector.LENS_FACING_FRONT -> Icons.Default.CameraFront
                                else -> Icons.Default.CameraRear
                            },
                            contentDescription = "Switch Camera"
                        )
                    }
                    
                    // Кнопка вспышки
                    IconButton(
                        onClick = { cameraViewModel.toggleFlash() },
                        enabled = cameraState.isCameraAvailable
                    ) {
                        Icon(
                            imageVector = if (cameraState.isFlashOn) Icons.Default.FlashOff else Icons.Default.FlashOn,
                            contentDescription = "Flash"
                        )
                    }
                    
                    // Кнопка сброса сканирования
                    IconButton(
                        onClick = { cameraViewModel.resetScanning() },
                        enabled = cameraState.scanCount > 0
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = onNavigateToInventory) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inventory, contentDescription = "Inventory")
                            Text("Инвентарь", fontSize = 10.sp)
                        }
                    }
                    
                    IconButton(onClick = onNavigateToProducts) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.List, contentDescription = "Products")
                            Text("Товары", fontSize = 10.sp)
                        }
                    }
                    
                    IconButton(onClick = { /* TODO: Navigate to settings */ }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                            Text("Настройки", fontSize = 10.sp)
                        }
                    }
                    
                    IconButton(onClick = { /* TODO: Navigate to reports */ }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.BarChart, contentDescription = "Reports")
                            Text("Отчеты", fontSize = 10.sp)
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { scannerViewModel.clearScannedItems() },
                containerColor = MaterialTheme.colorScheme.error,
                enabled = scannedItems.isNotEmpty()
            ) {
                Icon(Icons.Default.Close, contentDescription = "Clear")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Камера (только если доступна)
            if (cameraState.isCameraAvailable && cameraState.isCameraActive) {
                CameraPreviewNew(
                    modifier = Modifier.fillMaxSize(),
                    isCameraActive = cameraState.isCameraActive,
                    lensFacing = cameraState.lensFacing,
                    onBarcodeScanned = { barcode ->
                        cameraViewModel.onBarcodeScanned(barcode)
                        // Также передаем в scannerViewModel для обработки
                        scannerViewModel.onBarcodeScanned(barcode)
                    },
                    onCameraError = { error ->
                        cameraViewModel.onCameraError(error)
                    },
                    onCameraInitialized = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Камера инициализирована")
                        }
                    }
                )
                
                // Оверлей сканера
                ScannerOverlay(
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Сообщение если камера не доступна
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Camera not available",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        
                        Text(
                            text = cameraState.cameraError ?: "Камера не доступна",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        
                        Button(
                            onClick = { cameraViewModel.checkCameraAvailability(context) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "Retry",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Проверить камеру")
                            }
                        }
                    }
                }
            }
            
            // Список отсканированных товаров
            if (scannedItems.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Отсканировано: ${scannedItems.size}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Text(
                                text = "Всего: ${cameraState.scanCount}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        
                        LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            items(scannedItems.take(5)) { item ->
                                ScannedItemRow(item = item)
                            }
                        }
                        
                        if (scannedItems.size > 5) {
                            Text(
                                text = "... и еще ${scannedItems.size - 5}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { scannerViewModel.saveCurrentSession() },
                                enabled = scannedItems.isNotEmpty()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Done,
                                        contentDescription = "Save",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Сохранить сессию")
                                }
                            }
                        }
                    }
                }
            }
            
            // Статистика сканирования (только если есть сканы)
            if (cameraState.scanCount > 0) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = "📊 Статистика",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Text(
                            text = "Сканов: ${cameraState.scanCount}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        cameraState.lastScannedBarcode?.let { barcode ->
                            Text(
                                text = "Последний: ${barcode.take(10)}...",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
            
            // Индикатор загрузки
            if (cameraState.isLoading || scannerState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

/**
 * Компонент для отображения строки отсканированного товара
 */
@Composable
fun ScannedItemRow(item: com.cloudterminal.domain.models.ScannedItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.barcode,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            item.productId?.let { productId ->
                Text(
                    text = "ID: $productId",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
        
        Text(
            text = "×${item.quantity}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}