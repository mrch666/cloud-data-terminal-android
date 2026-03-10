package com.cloudterminal

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite для запуска всех тестов
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    // Domain layer tests
    com.cloudterminal.domain.models.ModelTest::class,
    com.cloudterminal.domain.usecases.GetProductByBarcodeUseCaseTest::class,
    com.cloudterminal.domain.usecases.SaveScannedItemUseCaseTest::class,
    
    // Presentation layer tests
    com.cloudterminal.presentation.screens.scanner.ScannerViewModelTest::class,
    
    // Data layer tests (Android tests)
    com.cloudterminal.data.repository.ProductRepositoryImplTest::class
)
class TestSuite