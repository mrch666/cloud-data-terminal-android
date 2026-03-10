package com.cloudterminal.domain.usecases

import com.cloudterminal.domain.models.ScannedItem
import com.cloudterminal.domain.repository.ScannedItemRepository
import javax.inject.Inject

class SaveScannedItemUseCase @Inject constructor(
    private val repository: ScannedItemRepository
) {
    suspend operator fun invoke(barcode: String, quantity: Int, productId: String?) {
        if (barcode.isBlank()) return
        
        val scannedItem = ScannedItem(
            barcode = barcode.trim(),
            quantity = quantity,
            productId = productId
        )
        
        repository.saveScannedItem(scannedItem)
    }
}