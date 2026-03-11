package com.cloudterminal.presentation.screens.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cloudterminal.R
import com.cloudterminal.domain.models.ScannedItem
import com.cloudterminal.presentation.components.CameraPreviewNew
import com.cloudterminal.presentation.components.ScannerOverlay
import com.cloudterminal.ui.theme.*

/**
 * Экран сканирования с Jetpack Compose
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreenCompose(
    viewModel: ScannerViewModelUpdated = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scannedItems by viewModel.scannedItems.collectAsState()
    val scanningStats by viewModel.scanningStats.collectAsState()
    
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.scanner),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    // Кнопка вспышки
                    IconButton(
                        onClick = { viewModel.toggleFlash() }
                    ) {
                        Icon(
                            imageVector = if (uiState.isFlashOn) {
                                Icons.Filled.FlashOn
                            } else {
                                Icons.Filled.FlashOff
                            },
                            contentDescription = stringResource(R.string.flash),
                            tint = if (uiState.isFlashOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // Кнопка сохранения сессии
                    IconButton(
                        onClick = { viewModel.saveCurrentSession() }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = stringResource(R.string.save_session),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Кнопка очистки списка
                    IconButton(
                        onClick = { viewModel.clearScannedItems() }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = stringResource(R.string.clear_list),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        bottomBar = {
            ScannerBottomBar(
                totalScanned = scannedItems.size,
                totalQuantity = viewModel.getTotalScannedQuantity(),
                uniqueCount = viewModel.getUniqueScannedCount(),
                onSaveSession = { viewModel.saveCurrentSession() },
                onClearList = { viewModel.clearScannedItems() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Камера и область сканирования
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Предпросмотр камеры
                CameraPreviewNew(
                    modifier = Modifier.fillMaxSize(),
                    isFlashOn = uiState.isFlashOn,
                    onBarcodeScanned = { barcode ->
                        viewModel.onBarcodeScanned(barcode)
                    }
                )
                
                // Оверлей сканера
                ScannerOverlay(
                    modifier = Modifier.fillMaxSize()
                )
                
                // Индикатор загрузки
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Сообщения об ошибках и успехах
            if (uiState.errorMessage != null) {
                ErrorMessage(
                    message = uiState.errorMessage!!,
                    onDismiss = { viewModel.clearError() }
                )
            }
            
            if (uiState.scanSuccessMessage != null) {
                SuccessMessage(
                    message = uiState.scanSuccessMessage!!,
                    onDismiss = { viewModel.clearSuccessMessage() }
                )
            }
            
            // Список отсканированных элементов
            ScannedItemsList(
                items = scannedItems,
                onRemoveItem = { barcode -> viewModel.removeScannedItem(barcode) },
                onUpdateQuantity = { barcode, quantity -> 
                    viewModel.updateItemQuantity(barcode, quantity) 
                }
            )
        }
    }
}

/**
 * Нижняя панель сканера со статистикой
 */
@Composable
private fun ScannerBottomBar(
    totalScanned: Int,
    totalQuantity: Int,
    uniqueCount: Int,
    onSaveSession: () -> Unit,
    onClearList: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Статистика
            Column {
                Text(
                    text = "Всего: $totalScanned",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Количество: $totalQuantity",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Уникальных: $uniqueCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Кнопки действий
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Кнопка сохранения
                Button(
                    onClick = onSaveSession,
                    enabled = totalScanned > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Сохранить",
                        fontSize = 12.sp
                    )
                }
                
                // Кнопка очистки
                Button(
                    onClick = onClearList,
                    enabled = totalScanned > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Очистить",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

/**
 * Список отсканированных элементов
 */
@Composable
private fun ScannedItemsList(
    items: List<ScannedItem>,
    onRemoveItem: (String) -> Unit,
    onUpdateQuantity: (String, Int) -> Unit
) {
    if (items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Нет отсканированных элементов",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        return
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(items, key = { it.barcode }) { item ->
            ScannedItemCard(
                item = item,
                onRemove = { onRemoveItem(item.barcode) },
                onUpdateQuantity = { quantity -> onUpdateQuantity(item.barcode, quantity) }
            )
        }
    }
}

/**
 * Карточка отсканированного элемента
 */
@Composable
private fun ScannedItemCard(
    item: ScannedItem,
    onRemove: () -> Unit,
    onUpdateQuantity: (Int) -> Unit
) {
    var quantity by remember { mutableStateOf(item.quantity) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Информация о товаре
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Штрих-код: ${item.barcode}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (item.productId != null) {
                    Text(
                        text = "ID товара: ${item.productId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Text(
                    text = "Время: ${formatTime(item.scannedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Управление количеством
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Кнопка уменьшения
                IconButton(
                    onClick = {
                        if (quantity > 1) {
                            quantity--
                            onUpdateQuantity(quantity)
                        } else {
                            onRemove()
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Remove,
                        contentDescription = "Уменьшить",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Количество
                Text(
                    text = quantity.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.Center
                )
                
                // Кнопка увеличения
                IconButton(
                    onClick = {
                        quantity++
                        onUpdateQuantity(quantity)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Увеличить",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Кнопка удаления
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Сообщение об ошибке
 */
@Composable
private fun ErrorMessage(
    message: String,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Закрыть",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Сообщение об успехе
 */
@Composable
private fun SuccessMessage(
    message: String,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Закрыть",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Форматирование времени
 */
private fun formatTime(timestamp: Long): String {
    return try {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        format.format(date)
    } catch (e: Exception) {
        "Н/Д"
    }
}