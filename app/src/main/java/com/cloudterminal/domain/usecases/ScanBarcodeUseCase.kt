package com.cloudterminal.domain.usecases

import com.google.mlkit.vision.barcode.common.Barcode
import javax.inject.Inject

class ScanBarcodeUseCase @Inject constructor() {
    operator fun invoke(barcode: Barcode): String? {
        return barcode.rawValue
    }
}