package com.cloudterminal.di

import android.content.Context
import androidx.room.Room
import com.cloudterminal.data.local.database.CloudTerminalDatabase
import com.cloudterminal.data.repository.ProductRepositoryImpl
import com.cloudterminal.domain.repository.ProductRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CloudTerminalDatabase {
        return Room.databaseBuilder(
            context,
            CloudTerminalDatabase::class.java,
            CloudTerminalDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration()
         .build()
    }
    
    @Provides
    @Singleton
    fun provideProductRepository(database: CloudTerminalDatabase): ProductRepository {
        return ProductRepositoryImpl(database.productDao())
    }
}