package com.cloudterminal.presentation.screens.products

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cloudterminal.R
import com.cloudterminal.domain.models.Product
import com.cloudterminal.ui.theme.*

/**
 * Экран управления товарами с Jetpack Compose
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreenCompose(
    viewModel: ProductsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val products by viewModel.products.collectAsState()
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    
    // Открытие диалога редактирования
    val showEditDialog = remember { mutableStateOf(false) }
    
    // Открытие диалога удаления
    val showDeleteDialog = remember { mutableStateOf(false) }
    val productToDelete = remember { mutableStateOf<Product?>(null) }
    
    // Открытие диалога импорта
    val showImportDialog = remember { mutableStateOf(false) }
    
    // Открытие диалога очистки
    val showClearDialog = remember { mutableStateOf(false) }
    
    // Открытие диалога статистики
    val showStatsDialog = remember { mutableStateOf(false) }
    
    LaunchedEffect(selectedProduct) {
        showEditDialog.value = selectedProduct != null
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.products_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    // Кнопка импорта
                    IconButton(
                        onClick = { showImportDialog.value = true }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Upload,
                            contentDescription = stringResource(R.string.import_csv),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Кнопка экспорта
                    IconButton(
                        onClick = { 
                            // TODO: Реализовать экспорт
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Download,
                            contentDescription = stringResource(R.string.export_csv),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Кнопка статистики
                    IconButton(
                        onClick = { showStatsDialog.value = true }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Assessment,
                            contentDescription = "Статистика",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Кнопка очистки
                    IconButton(
                        onClick = { showClearDialog.value = true }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteSweep,
                            contentDescription = "Очистить все",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    // Кнопка обновления
                    IconButton(
                        onClick = { viewModel.loadProducts() }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Обновить",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.createNewProduct() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.add_new_product)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Поиск
            SearchSection(
                searchQuery = uiState.searchQuery,
                onSearchChange = { query -> viewModel.searchProducts(query) }
            )
            
            // Статистика
            ProductsStatsSection(
                stats = viewModel.getProductsStats(),
                onClick = { showStatsDialog.value = true }
            )
            
            // Сообщения об ошибках и успехах
            if (uiState.errorMessage != null) {
                ErrorMessage(
                    message = uiState.errorMessage!!,
                    onDismiss = { viewModel.clearError() }
                )
            }
            
            if (uiState.successMessage != null) {
                SuccessMessage(
                    message = uiState.successMessage!!,
                    onDismiss = { viewModel.clearSuccessMessage() }
                )
            }
            
            // Индикатор загрузки
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                // Список товаров
                ProductsList(
                    products = products,
                    onProductClick = { product -> viewModel.selectProduct(product) },
                    onDeleteClick = { product -> 
                        productToDelete.value = product
                        showDeleteDialog.value = true
                    }
                )
            }
        }
        
        // Диалог редактирования товара
        if (showEditDialog.value && selectedProduct != null) {
            EditProductDialog(
                product = selectedProduct!!,
                onSave = { product ->
                    viewModel.saveProduct(product)
                    showEditDialog.value = false
                },
                onCancel = {
                    viewModel.cancelEditing()
                    showEditDialog.value = false
                }
            )
        }
        
        // Диалог подтверждения удаления
        if (showDeleteDialog.value && productToDelete.value != null) {
            DeleteConfirmationDialog(
                productName = productToDelete.value!!.name,
                onConfirm = {
                    viewModel.deleteProduct(productToDelete.value!!.id)
                    productToDelete.value = null
                    showDeleteDialog.value = false
                },
                onDismiss = {
                    productToDelete.value = null
                    showDeleteDialog.value = false
                }
            )
        }
        
        // Диалог импорта
        if (showImportDialog.value) {
            ImportDialog(
                onImport = { csvContent ->
                    viewModel.importFromCsv(csvContent)
                    showImportDialog.value = false
                },
                onDismiss = { showImportDialog.value = false }
            )
        }
        
        // Диалог очистки
        if (showClearDialog.value) {
            ClearConfirmationDialog(
                onConfirm = {
                    viewModel.clearAllProducts()
                    showClearDialog.value = false
                },
                onDismiss = { showClearDialog.value = false }
            )
        }
        
        // Диалог статистики
        if (showStatsDialog.value) {
            StatsDialog(
                stats = viewModel.getProductsStats(),
                onDismiss = { showStatsDialog.value = false }
            )
        }
    }
}

/**
 * Секция поиска
 */
@Composable
private fun SearchSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Поиск товаров...",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Поиск"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchChange("") }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Очистить"
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        
        if (searchQuery.isNotEmpty()) {
            Text(
                text = "Найдено: ${searchQuery.length} результатов",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Секция статистики товаров
 */
@Composable
private fun ProductsStatsSection(
    stats: ProductsStats,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Статистика товаров",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Информация",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Всего",
                    value = stats.totalProducts.toString(),
                    icon = Icons.Filled.Inventory
                )
                
                StatItem(
                    label = "Со штрих-кодом",
                    value = stats.productsWithBarcode.toString(),
                    icon = Icons.Filled.QrCode
                )
                
                StatItem(
                    label = "Без штрих-кода",
                    value = stats.productsWithoutBarcode.toString(),
                    icon = Icons.Filled.Warning
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "С ценой",
                    value = stats.productsWithPrice.toString(),
                    icon = Icons.Filled.AttachMoney
                )
                
                StatItem(
                    label = "Без цены",
                    value = stats.productsWithoutPrice.toString(),
                    icon = Icons.Filled.MoneyOff
                )
                
                StatItem(
                    label = "Категорий",
                    value = stats.categoriesCount.toString(),
                    icon = Icons.Filled.Category
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                StatItem(
                    label = "Общая стоимость",
                    value = String.format("%.2f ₽", stats.totalValue),
                    icon = Icons.Filled.AccountBalance
                )
            }
        }
    }
}

/**
 * Элемент статистики
 */
@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(20.dp)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Список товаров
 */
@Composable
private fun ProductsList(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    onDeleteClick: (Product) -> Unit
) {
    if (products.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Inventory2,
                    contentDescription = "Нет товаров",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(64.dp)
                )
                
                Text(
                    text = "Нет товаров",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Нажмите кнопку + чтобы добавить товар",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .weight(1f),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(products, key = { it.id }) { product ->
            ProductCard(
                product = product,
                onClick = { onProductClick(product) },
                onDelete = { onDeleteClick(product) }
            )
        }
    }
}

/**
 * Карточка товара
 */
@Composable
private fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Заголовок с названием и ценой
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                if (product.price != null) {
                    Text(
                        text = String.format("%.2f ₽", product.price),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Штрих-код
            Text(
                text = "Штрих-код: ${product.barcode}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Категория и единица измерения
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (product.category != null) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = product.category,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                if (!product.unit.isNullOrBlank()) {
                    Text(
                        text = product.unit,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Описание (если есть)
            if (!product.description.isNullOrBlank()) {
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Кнопка удаления
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon