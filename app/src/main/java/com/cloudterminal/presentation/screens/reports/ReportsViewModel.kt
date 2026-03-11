package com.cloudterminal.presentation.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudterminal.domain.models.Product
import com.cloudterminal.domain.models.ScannedItem
import com.cloudterminal.domain.models.SyncSession
import com.cloudterminal.domain.repository.ProductRepository
import com.cloudterminal.domain.repository.ScannedItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel для экрана отчетов
 */
@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val scannedItemRepository: ScannedItemRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()
    
    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()
    
    private val _scanStats = MutableStateFlow(ScanStatistics())
    val scanStats: StateFlow<ScanStatistics> = _scanStats.asStateFlow()
    
    init {
        // Загрузка данных для отчетов при инициализации
        loadReportsData()
    }
    
    /**
     * Загрузка данных для отчетов
     */
    fun loadReportsData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                // Загрузка товаров и отсканированных элементов
                val products = productRepository.getAllProducts()
                val scannedItems = scannedItemRepository.getScannedItems(null)
                
                // Генерация отчетов
                generateReports(products, scannedItems)
                
                // Расчет статистики
                calculateStatistics(scannedItems)
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        totalProducts = products.size,
                        totalScans = scannedItems.size
                    )
                }
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Ошибка загрузки данных: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Генерация отчетов
     */
    private fun generateReports(products: List<Product>, scannedItems: List<ScannedItem>) {
        val reportsList = mutableListOf<Report>()
        
        // 1. Отчет по сканированиям
        reportsList.add(generateScanReport(scannedItems))
        
        // 2. Отчет по товарам
        reportsList.add(generateProductsReport(products))
        
        // 3. Отчет по активности
        reportsList.add(generateActivityReport(scannedItems))
        
        // 4. Отчет по эффективности
        reportsList.add(generateEfficiencyReport(scannedItems, products))
        
        _reports.update { reportsList }
    }
    
    /**
     * Генерация отчета по сканированиям
     */
    private fun generateScanReport(scannedItems: List<ScannedItem>): Report {
        val totalScans = scannedItems.size
        val uniqueProducts = scannedItems.map { it.barcode }.distinct().size
        val totalQuantity = scannedItems.sumOf { it.quantity }
        
        val today = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayScans = scannedItems.count {
            dateFormat.format(Date(it.scannedAt)) == dateFormat.format(today)
        }
        
        return Report(
            id = "scan_report_${System.currentTimeMillis()}",
            title = "Отчет по сканированиям",
            type = ReportType.SCAN_STATS,
            data = mapOf(
                "total_scans" to totalScans.toString(),
                "unique_products" to uniqueProducts.toString(),
                "total_quantity" to totalQuantity.toString(),
                "today_scans" to todayScans.toString(),
                "avg_per_day" to (if (totalScans > 0) (totalScans / 30.0).format(1) else "0.0")
            ),
            generatedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * Генерация отчета по товарам
     */
    private fun generateProductsReport(products: List<Product>): Report {
        val totalProducts = products.size
        val productsWithPrice = products.count { it.price != null }
        val productsWithoutPrice = products.count { it.price == null }
        val totalValue = products.sumOf { it.price ?: 0.0 }
        
        val categories = products.mapNotNull { it.category }.distinct()
        
        return Report(
            id = "products_report_${System.currentTimeMillis()}",
            title = "Отчет по товарам",
            type = ReportType.PRODUCTS,
            data = mapOf(
                "total_products" to totalProducts.toString(),
                "with_price" to productsWithPrice.toString(),
                "without_price" to productsWithoutPrice.toString(),
                "total_value" to totalValue.format(2),
                "categories_count" to categories.size.toString(),
                "avg_price" to (if (productsWithPrice > 0) (totalValue / productsWithPrice).format(2) else "0.00")
            ),
            generatedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * Генерация отчета по активности
     */
    private fun generateActivityReport(scannedItems: List<ScannedItem>): Report {
        if (scannedItems.isEmpty()) {
            return Report(
                id = "activity_report_${System.currentTimeMillis()}",
                title = "Отчет по активности",
                type = ReportType.ACTIVITY,
                data = mapOf("message" to "Нет данных о сканированиях"),
                generatedAt = System.currentTimeMillis()
            )
        }
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val scansByDay = scannedItems.groupBy {
            dateFormat.format(Date(it.scannedAt))
        }
        
        val mostActiveDay = scansByDay.maxByOrNull { it.value.size }
        val leastActiveDay = scansByDay.minByOrNull { it.value.size }
        
        return Report(
            id = "activity_report_${System.currentTimeMillis()}",
            title = "Отчет по активности",
            type = ReportType.ACTIVITY,
            data = mapOf(
                "total_days" to scansByDay.size.toString(),
                "most_active_day" to (mostActiveDay?.key ?: "Нет данных"),
                "most_active_count" to (mostActiveDay?.value?.size?.toString() ?: "0"),
                "least_active_day" to (leastActiveDay?.key ?: "Нет данных"),
                "least_active_count" to (leastActiveDay?.value?.size?.toString() ?: "0"),
                "avg_per_day" to (scannedItems.size.toDouble() / scansByDay.size).format(1)
            ),
            generatedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * Генерация отчета по эффективности
     */
    private fun generateEfficiencyReport(scannedItems: List<ScannedItem>, products: List<Product>): Report {
        val productBarcodes = products.map { it.barcode }.toSet()
        val scannedBarcodes = scannedItems.map { it.barcode }.toSet()
        
        val foundProducts = scannedBarcodes.count { it in productBarcodes }
        val notFoundProducts = scannedBarcodes.count { it !in productBarcodes }
        
        val efficiency = if (scannedBarcodes.isNotEmpty()) {
            (foundProducts.toDouble() / scannedBarcodes.size * 100).format(1)
        } else {
            "0.0"
        }
        
        return Report(
            id = "efficiency_report_${System.currentTimeMillis()}",
            title = "Отчет по эффективности",
            type = ReportType.EFFICIENCY,
            data = mapOf(
                "total_scans" to scannedBarcodes.size.toString(),
                "found_products" to foundProducts.toString(),
                "not_found" to notFoundProducts.toString(),
                "efficiency" to "$efficiency%",
                "coverage" to "${((foundProducts.toDouble() / productBarcodes.size) * 100).format(1)}%"
            ),
            generatedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * Расчет статистики сканирования
     */
    private fun calculateStatistics(scannedItems: List<ScannedItem>) {
        if (scannedItems.isEmpty()) {
            _scanStats.update { ScanStatistics() }
            return
        }
        
        val scanTimes = scannedItems.map { it.scannedAt }.sorted()
        val firstScan = scanTimes.first()
        val lastScan = scanTimes.last()
        
        val scansPerDay = if (scanTimes.isNotEmpty()) {
            val daysDiff = (lastScan - firstScan) / (1000 * 60 * 60 * 24) + 1
            scannedItems.size.toDouble() / daysDiff
        } else {
            0.0
        }
        
        _scanStats.update {
            it.copy(
                totalScans = scannedItems.size,
                uniqueProducts = scannedItems.map { it.barcode }.distinct().size,
                totalQuantity = scannedItems.sumOf { it.quantity },
                firstScanDate = firstScan,
                lastScanDate = lastScan,
                scansPerDay = scansPerDay,
                avgQuantityPerScan = scannedItems.map { it.quantity }.average()
            )
        }
    }
    
    /**
     * Экспорт отчета
     */
    fun exportReport(reportId: String, format: ExportFormat) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, errorMessage = null) }
            
            try {
                val report = _reports.value.find { it.id == reportId }
                if (report == null) {
                    throw IllegalArgumentException("Отчет не найден")
                }
                
                val exportContent = when (format) {
                    ExportFormat.CSV -> exportToCsv(report)
                    ExportFormat.PDF -> exportToPdf(report)
                    ExportFormat.HTML -> exportToHtml(report)
                }
                
                // TODO: Сохранение файла или отправка
                
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        successMessage = "Отчет экспортирован в ${format.displayName}"
                    )
                }
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        errorMessage = "Ошибка экспорта: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Экспорт всех отчетов
     */
    fun exportAllReports(format: ExportFormat) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, errorMessage = null) }
            
            try {
                // TODO: Реализовать экспорт всех отчетов
                kotlinx.coroutines.delay(2000)
                
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        successMessage = "Все отчеты экспортированы в ${format.displayName}"
                    )
                }
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        errorMessage = "Ошибка экспорта: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Обновление периода отчетов
     */
    fun updateReportPeriod(period: ReportPeriod) {
        _uiState.update { it.copy(selectedPeriod = period) }
        // TODO: Перегенерация отчетов для выбранного периода
    }
    
    /**
     * Очистка ошибки
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    /**
     * Очистка сообщения об успехе
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
    
    /**
     * Обновление отчета
     */
    fun refreshReports() {
        loadReportsData()
    }
    
    // Вспомогательные методы для экспорта
    private fun exportToCsv(report: Report): String {
        val header = "Параметр,Значение"
        val rows = report.data.entries.joinToString("\n") { (key, value) ->
            "$key,$value"
        }
        return "$header\n$rows"
    }
    
    private fun exportToPdf(report: Report): String {
        // TODO: Реализовать генерацию PDF
        return "PDF content for ${report.title}"
    }
    
    private fun exportToHtml(report: Report): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>${report.title}</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; }
                    h1 { color: #333; }
                    table { border-collapse: collapse; width: 100%; }
                    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                    th { background-color: #f2f2f2; }
                </style>
            </head>
            <body>
                <h1>${report.title}</h1>
                <p>Сгенерировано: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(report.generatedAt))}</p>
                <table>
                    <tr><th>Параметр</th><th>Значение</th></tr>
                    ${report.data.entries.joinToString("") { (key, value) ->
                        "<tr><td>$key</td><td>$value</td></tr>"
                    }}
                </table>
            </body>
            </html>
        """.trimIndent()
    }
    
    /**
     * Форматирование числа с указанным количеством знаков после запятой
     */
    private fun Double.format(digits: Int) = "%.${digits}f".format(this)
}

/**
 * Состояние UI для экрана отчетов
 */
data class ReportsUiState(
    val isLoading: Boolean = true,
    val isExporting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val selectedPeriod: ReportPeriod = ReportPeriod.LAST_30_DAYS,
    val totalProducts: Int = 0,
    val totalScans: Int = 0
)

/**
 * Модель отчета
 */
data class Report(
    val id: String,
    val title: String,
    val type: ReportType,
    val data: Map<String, String>,
    val generatedAt: Long
)

/**
 * Типы отчетов
 */
enum class ReportType {
    SCAN_STATS,
    PRODUCTS,
    ACTIVITY,
    EFFICIENCY,
    INVENTORY
}

/**
 * Периоды отчетов
 */
enum class ReportPeriod(val displayName: String) {
    TODAY("Сегодня"),
    YESTERDAY("Вчера"),
    LAST_7_DAYS("Последние 7 дней"),
    LAST_30_DAYS("Последние 30 дней"),
    THIS_MONTH("Этот месяц"),
    LAST_MONTH("Прошлый месяц"),
    ALL_TIME("За все время")
}

/**
 * Форматы экспорта
 */
enum class ExportFormat(val displayName: String) {
    CSV("CSV"),
    PDF("PDF"),
    HTML("HTML")
}

/**
 * Статистика сканирования
 */
data class ScanStatistics(
    val totalScans: Int = 0,
    val uniqueProducts: Int = 0,
    val totalQuantity: Int = 0,
    val firstScanDate: Long = 0,
    val lastScanDate: Long = 0,
    val scansPerDay: Double = 0.0,
    val avgQuantityPerScan: Double = 0.0
)