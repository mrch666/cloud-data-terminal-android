package com.cloudterminal.domain.models

data class AppSettings(
    val apiUrl: String = "",
    val apiKey: String = "",
    val syncInterval: Int = 300, // 5 minutes in seconds
    val autoUpload: Boolean = true,
    val enableFlash: Boolean = false,
    val enableSound: Boolean = true,
    val barcodeFormats: Set<BarcodeFormat> = setOf(
        BarcodeFormat.EAN_13,
        BarcodeFormat.EAN_8,
        BarcodeFormat.UPC_A,
        BarcodeFormat.CODE_128,
        BarcodeFormat.QR_CODE
    )
)