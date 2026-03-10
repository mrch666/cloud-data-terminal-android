package com.cloudterminal.domain.models

enum class BarcodeFormat(val displayName: String) {
    EAN_13("EAN-13"),
    EAN_8("EAN-8"),
    UPC_A("UPC-A"),
    UPC_E("UPC-E"),
    CODE_39("Code 39"),
    CODE_93("Code 93"),
    CODE_128("Code 128"),
    QR_CODE("QR Code"),
    DATA_MATRIX("Data Matrix"),
    PDF_417("PDF417")
}