package com.cloudterminal.domain.usecases

import com.cloudterminal.domain.models.BarcodeFormat
import com.cloudterminal.domain.models.Product
import com.cloudterminal.domain.repository.ProductRepository
import com.google.mlkit.vision.barcode.Barcode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case для обработки отсканированных штрих-кодов
 */
class ProcessBarcodeUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    
    /**
     * Обработка штрих-кода с поиском товара в базе данных
     */
    suspend operator fun invoke(barcode: Barcode): BarcodeProcessingResult {
        val barcodeValue = barcode.rawValue ?: return BarcodeProcessingResult.Error(
            error = "Пустой штрих-код",
            barcodeValue = null
        )
        
        // Валидация штрих-кода
        if (!isValidBarcode(barcodeValue)) {
            return BarcodeProcessingResult.Error(
                error = "Неверный формат штрих-кода",
                barcodeValue = barcodeValue
            )
        }
        
        // Поиск товара в базе данных
        val product = productRepository.getProductByBarcode(barcodeValue)
        
        return if (product != null) {
            BarcodeProcessingResult.Success(
                barcodeValue = barcodeValue,
                product = product,
                barcodeFormat = mapBarcodeFormat(barcode.format),
                isNewProduct = false
            )
        } else {
            BarcodeProcessingResult.NotFound(
                barcodeValue = barcodeValue,
                barcodeFormat = mapBarcodeFormat(barcode.format),
                suggestedProductName = generateProductName(barcodeValue)
            )
        }
    }
    
    /**
     * Потоковая обработка штрих-кодов
     */
    fun processBarcodesStream(barcodes: List<Barcode>): Flow<BarcodeProcessingResult> = flow {
        for (barcode in barcodes) {
            val result = invoke(barcode)
            emit(result)
        }
    }
    
    /**
     * Валидация штрих-кода
     */
    private fun isValidBarcode(barcode: String): Boolean {
        if (barcode.isBlank()) return false
        
        // Проверка длины (минимальная длина для большинства форматов)
        if (barcode.length < 4) return false
        
        // Проверка на допустимые символы
        val validPattern = Regex("^[A-Za-z0-9\\-\\_\\.\\+\\/\\*\\=\\@\\#\\$\\%\\&]+$")
        if (!validPattern.matches(barcode)) return false
        
        return true
    }
    
    /**
     * Преобразование формата ML Kit Barcode в доменный формат
     */
    private fun mapBarcodeFormat(mlKitFormat: Int): BarcodeFormat {
        return when (mlKitFormat) {
            Barcode.FORMAT_EAN_13 -> BarcodeFormat.EAN_13
            Barcode.FORMAT_EAN_8 -> BarcodeFormat.EAN_8
            Barcode.FORMAT_UPC_A -> BarcodeFormat.UPC_A
            Barcode.FORMAT_UPC_E -> BarcodeFormat.UPC_E
            Barcode.FORMAT_CODE_39 -> BarcodeFormat.CODE_39
            Barcode.FORMAT_CODE_93 -> BarcodeFormat.CODE_93
            Barcode.FORMAT_CODE_128 -> BarcodeFormat.CODE_128
            Barcode.FORMAT_QR_CODE -> BarcodeFormat.QR_CODE
            Barcode.FORMAT_DATA_MATRIX -> BarcodeFormat.DATA_MATRIX
            Barcode.FORMAT_PDF417 -> BarcodeFormat.PDF_417
            else -> BarcodeFormat.CODE_128 // fallback
        }
    }
    
    /**
     * Генерация предложенного названия товара на основе штрих-кода
     */
    private fun generateProductName(barcode: String): String {
        return when {
            barcode.startsWith("590") -> "Продукт EAN-13: ${barcode.take(8)}..."
            barcode.startsWith("00") -> "Продукт UPC: ${barcode.take(6)}..."
            barcode.matches(Regex("^[0-9]+$")) -> "Товар #${barcode.takeLast(6)}"
            else -> "Товар: ${barcode.take(20)}"
        }
    }
}

/**
 * Результат обработки штрих-кода
 */
sealed class BarcodeProcessingResult {
    data class Success(
        val barcodeValue: String,
        val product: Product,
        val barcodeFormat: BarcodeFormat,
        val isNewProduct: Boolean
    ) : BarcodeProcessingResult()
    
    data class NotFound(
        val barcodeValue: String,
        val barcodeFormat: BarcodeFormat,
        val suggestedProductName: String
    ) : BarcodeProcessingResult()
    
    data class Error(
        val error: String,
        val barcodeValue: String?
    ) : BarcodeProcessingResult()
}

/**
 * Статистика сканирования
 */
data class BarcodeScanningStats(
    val totalScans: Int = 0,
    val successfulScans: Int = 0,
    val notFoundScans: Int = 0,
    val errorScans: Int = 0,
    val lastScanTime: Long = 0,
    val mostCommonFormat: BarcodeFormat? = null
)