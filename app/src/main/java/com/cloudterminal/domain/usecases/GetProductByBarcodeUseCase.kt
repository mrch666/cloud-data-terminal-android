package com.cloudterminal.domain.usecases

import com.cloudterminal.domain.models.Product
import com.cloudterminal.domain.repository.ProductRepository
import javax.inject.Inject

class GetProductByBarcodeUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(barcode: String): Product? {
        if (barcode.isBlank()) return null
        return repository.getProductByBarcode(barcode.trim())
    }
}