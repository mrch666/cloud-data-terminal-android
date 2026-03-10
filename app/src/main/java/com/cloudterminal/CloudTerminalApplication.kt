package com.cloudterminal

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkManager
import com.cloudterminal.data.di.AppModule
import com.cloudterminal.data.sync.SyncWorkerFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import timber.log.Timber

@HiltAndroidApp
class CloudTerminalApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: SyncWorkerFactory
    
    override fun onCreate() {
        super.onCreate()
        
        // Инициализация логирования
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        // Инициализация WorkManager с кастомной фабрикой
        WorkManager.initialize(this, workManagerConfiguration)
        
        // Инициализация модулей приложения
        AppModule.init(this)
        
        Timber.d("CloudTerminalApplication initialized")
    }
    
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.ERROR)
            .build()
    }
    
    companion object {
        // Глобальный контекст приложения
        lateinit var appContext: Context
            private set
    }
    
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        appContext = base
    }
}