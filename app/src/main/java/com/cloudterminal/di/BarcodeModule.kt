package com.cloudterminal.di

import android.content.Context
import com.cloudterminal.data.barcode.BarcodeScannerService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Модуль для внедрения зависимостей связанных со сканированием штрих-кодов
 */
@Module
@InstallIn(SingletonComponent::class)
object BarcodeModule {
    
    @Provides
    @Singleton
    fun provideBarcodeScannerService(@ApplicationContext context: Context): BarcodeScannerService {
        return BarcodeScannerService(context)
    }
}