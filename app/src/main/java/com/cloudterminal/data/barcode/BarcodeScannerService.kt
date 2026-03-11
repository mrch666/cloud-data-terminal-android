package com.cloudterminal.data.barcode

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Сервис для работы со сканированием штрих-кодов через ML Kit
 */
@Singleton
class BarcodeScannerService @Inject constructor(
    private val context: Context
) {
    
    private var barcodeScanner: BarcodeScanner? = null
    private val ioScope = CoroutineScope(Dispatchers.IO)
    
    /**
     * Инициализация сканера штрих-кодов
     */
    fun initializeScanner(formats: List<Int> = DEFAULT_FORMATS) {
        if (barcodeScanner != null) return
        
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(*formats.toIntArray())
            .enableAllPotentialBarcodes() // Включение обнаружения всех потенциальных штрих-кодов
            .build()
        
        barcodeScanner = BarcodeScanning.getClient(options)
    }
    
    /**
     * Сканирование штрих-кодов из Bitmap
     */
    suspend fun scanFromBitmap(bitmap: Bitmap): List<Barcode> {
        return withContext(Dispatchers.IO) {
            try {
                val image = InputImage.fromBitmap(bitmap, 0)
                scanImage(image)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    /**
     * Сканирование штрих-кодов из файла
     */
    suspend fun scanFromFile(file: File): List<Barcode> {
        return withContext(Dispatchers.IO) {
            try {
                val image = InputImage.fromFilePath(context, file.toUri())
                scanImage(image)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    /**
     * Сканирование штрих-кодов из массива байтов (например, с камеры)
     */
    suspend fun scanFromBytes(
        bytes: ByteArray,
        width: Int,
        height: Int,
        rotation: Int
    ): List<Barcode> {
        return withContext(Dispatchers.IO) {
            try {
                val image = InputImage.fromByteArray(
                    bytes,
                    width,
                    height,
                    rotation,
                    InputImage.IMAGE_FORMAT_NV21
                )
                scanImage(image)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    /**
     * Основной метод сканирования изображения
     */
    private suspend fun scanImage(image: InputImage): List<Barcode> {
        return withContext(Dispatchers.IO) {
            try {
                val scanner = barcodeScanner ?: run {
                    initializeScanner()
                    barcodeScanner!!
                }
                
                scanner.process(image)
                    .await()
                    .filter { barcode ->
                        // Фильтрация валидных штрих-кодов
                        barcode.rawValue != null && barcode.rawValue.isNotBlank()
                    }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    /**
     * Получение статистики по форматам штрих-кодов
     */
    fun analyzeBarcodeFormats(barcodes: List<Barcode>): Map<Int, Int> {
        val formatCounts = mutableMapOf<Int, Int>()
        
        barcodes.forEach { barcode ->
            val count = formatCounts.getOrDefault(barcode.format, 0)
            formatCounts[barcode.format] = count + 1
        }
        
        return formatCounts
    }
    
    /**
     * Проверка поддержки формата штрих-кода
     */
    fun isFormatSupported(format: Int): Boolean {
        return DEFAULT_FORMATS.contains(format)
    }
    
    /**
     * Получение названия формата штрих-кода
     */
    fun getFormatName(format: Int): String {
        return when (format) {
            Barcode.FORMAT_EAN_13 -> "EAN-13"
            Barcode.FORMAT_EAN_8 -> "EAN-8"
            Barcode.FORMAT_UPC_A -> "UPC-A"
            Barcode.FORMAT_UPC_E -> "UPC-E"
            Barcode.FORMAT_CODE_39 -> "Code 39"
            Barcode.FORMAT_CODE_93 -> "Code 93"
            Barcode.FORMAT_CODE_128 -> "Code 128"
            Barcode.FORMAT_QR_CODE -> "QR Code"
            Barcode.FORMAT_DATA_MATRIX -> "Data Matrix"
            Barcode.FORMAT_PDF417 -> "PDF417"
            else -> "Unknown"
        }
    }
    
    /**
     * Очистка ресурсов сканера
     */
    fun cleanup() {
        barcodeScanner?.close()
        barcodeScanner = null
    }
    
    companion object {
        // Форматы по умолчанию
        val DEFAULT_FORMATS = listOf(
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_CODE_39,
            Barcode.FORMAT_CODE_93,
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_DATA_MATRIX,
            Barcode.FORMAT_PDF417
        )
        
        // Форматы для розничной торговли
        val RETAIL_FORMATS = listOf(
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_CODE_128
        )
        
        // Форматы для логистики
        val LOGISTICS_FORMATS = listOf(
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_CODE_39,
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_DATA_MATRIX
        )
    }
}