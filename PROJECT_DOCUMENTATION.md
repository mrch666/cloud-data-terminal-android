# 📱 ДОКУМЕНТАЦИЯ ПРОЕКТА: Cloud Data Terminal Android

## 🎯 ОБЗОР ПРОЕКТА

**Cloud Data Terminal** - Android приложение для сбора данных через сканирование штрих-кодов. Аналог https://wiki.lineris.ru/cloud_data_terminal.

### **Основные возможности:**
1. 📸 **Сканирование штрих-кодов** камерой смартфона
2. 🔗 **Поддержка внешних сканеров** (Bluetooth, USB)
3. ☁️ **Синхронизация с облачными системами** (1С, ERP, CRM)
4. 💾 **Оффлайн работа** с локальной базой данных
5. 📊 **Отчеты и аналитика** по собранным данным

## 🏗️ АРХИТЕКТУРА

### **Технологический стек:**
- **Язык:** Kotlin 100%
- **UI:** Jetpack Compose
- **Архитектура:** Clean Architecture + MVVM
- **DI:** Dagger Hilt
- **База данных:** Room
- **Сеть:** Retrofit + OkHttp
- **Камера:** CameraX
- **Сканирование штрих-кодов:** ML Kit Barcode Scanning
- **Асинхронность:** Kotlin Coroutines + Flow

### **Структура проекта (Clean Architecture):**
```
app/
├── data/               # Data Layer
│   ├── local/         # Room database, DataStore
│   ├── remote/        # API clients, Retrofit
│   └── repository/    # Репозитории (реализации)
├── domain/            # Domain Layer
│   ├── models/        # Модели данных
│   ├── repository/    # Интерфейсы репозиториев
│   └── usecases/      # Use cases (бизнес-логика)
└── presentation/      # Presentation Layer
    ├── screens/       # Экраны приложения
    ├── components/    # Переиспользуемые компоненты
    ├── viewmodels/    # ViewModels
    └── navigation/    # Навигация
```

## 📱 ЭКРАНЫ ПРИЛОЖЕНИЯ

### **1. Экран сканирования (ScannerScreen)**
**Основной функционал:**
- Режим реального времени сканирования
- Overlay с областью сканирования
- Список отсканированных товаров
- Управление вспышкой
- Быстрое сохранение сессии

**Компоненты:**
- `CameraPreview` - превью камеры с анализом
- `ScannerOverlay` - оверлей с рамкой сканирования
- `ScannerViewModel` - логика сканирования

### **2. Экран инвентаризации (InventoryScreen)**
- Просмотр всех отсканированных товаров
- Редактирование количества
- Фильтрация и поиск
- Экспорт в CSV/Excel

### **3. Экран справочника товаров (ProductsScreen)**
- Загруженные товары из системы
- Поиск по названию/штрих-коду
- Быстрое сканирование из списка

### **4. Экран настроек (SettingsScreen)**
- Настройка подключения к API
- Конфигурация сканера
- Настройки синхронизации
- Управление лицензией

### **5. Экран отчетов (ReportsScreen)**
- Статистика сканирования
- Отчеты по инвентаризации
- История синхронизаций

## 🔧 КЛЮЧЕВЫЕ КОМПОНЕНТЫ

### **1. CameraPreview**
```kotlin
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onBarcodeScanned: (Barcode) -> Unit
)
```
**Особенности:**
- Использует CameraX для работы с камерой
- Интегрирован с ML Kit для сканирования штрих-кодов
- Поддерживает форматы: EAN-13, Code 128, QR Code и др.
- Оптимизированная производительность (60fps)

### **2. ScannerOverlay**
- Визуальная рамка сканирования
- Анимационная линия сканирования
- Полупрозрачный фон вокруг области сканирования
- Уголки для лучшего позиционирования

### **3. ScannerViewModel**
**Основные методы:**
- `startCamera()` - инициализация камеры
- `onBarcodeScanned()` - обработка отсканированного штрих-кода
- `toggleFlash()` - управление вспышкой
- `saveCurrentSession()` - сохранение сессии сканирования

## 🗄️ БАЗА ДАННЫХ

### **Сущности Room:**

#### **1. ProductEntity**
```kotlin
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val barcode: String,
    val name: String,
    val description: String?,
    val category: String?,
    val price: Double?,
    val unit: String?,
    val lastUpdated: Long
)
```

#### **2. ScannedItemEntity**
```kotlin
@Entity(tableName = "scanned_items")
data class ScannedItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val barcode: String,
    val quantity: Int,
    val productId: String?,
    val scannedAt: Long,
    val sessionId: String,
    val locationLat: Double?,
    val locationLng: Double?,
    val isSynced: Boolean = false
)
```

#### **3. SyncSessionEntity**
```kotlin
@Entity(tableName = "sync_sessions")
data class SyncSessionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAt: Long,
    val itemCount: Int,
    val isCompleted: Boolean,
    val syncedAt: Long?
)
```

## 🌐 API ИНТЕГРАЦИЯ

### **Базовый URL:** `https://api.cloudterminal.app/v1`

### **Endpoints:**

#### **1. Аутентификация**
```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password",
  "device_id": "android_device_id"
}
```

#### **2. Получение справочника товаров**
```http
GET /products
Headers:
  Authorization: Bearer {token}
  X-API-Key: {api_key}

Query params:
  ?updated_after=timestamp
  ?limit=100
  ?offset=0
```

#### **3. Отправка отсканированных данных**
```http
POST /inventory/sync
Headers:
  Authorization: Bearer {token}
  Content-Type: application/json

{
  "session_id": "uuid",
  "device_id": "android_device_id",
  "items": [
    {
      "barcode": "5901234123457",
      "quantity": 5,
      "scanned_at": "2026-03-10T10:30:00Z",
      "location": {"lat": 55.7558, "lng": 37.6173}
    }
  ]
}
```

#### **4. Получение статуса синхронизации**
```http
GET /sync/status/{session_id}
Headers:
  Authorization: Bearer {token}
```

## 🔐 БЕЗОПАСНОСТЬ

### **Меры безопасности:**
1. **HTTPS все соединения** - TLS 1.3
2. **JWT аутентификация** - токены с expiry
3. **Шифрование локальной БД** - SQLCipher
4. **Биометрия** - опциональная защита приложения
5. **Защита от MITM** - certificate pinning

### **Разрешения:**
- `CAMERA` - для сканирования
- `INTERNET` - для синхронизации
- `BLUETOOTH` - для внешних сканеров
- `LOCATION` - опционально для геометок

## 📦 СБОРКА И РАЗВЕРТЫВАНИЕ

### **Требования:**
- Android Studio Arctic Fox (2020.3.1)+
- JDK 11+
- Android SDK 31+

### **Сборка:**
```bash
# Debug сборка
./gradlew assembleDebug

# Release сборка
./gradlew assembleRelease

# Тестирование
./gradlew test
./gradlew connectedAndroidTest
```

### **Конфигурация сборки:**
```kotlin
// build.gradle.kts
android {
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(...)
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
}
```

## 🧪 ТЕСТИРОВАНИЕ

### **Unit тесты:**
- `ViewModel` тесты с MockK
- `UseCase` тесты
- `Repository` тесты

### **Интеграционные тесты:**
- Тестирование Room базы данных
- Тестирование API клиентов
- Тестирование сканирования штрих-кодов

### **UI тесты:**
- Compose UI тестирование
- Скриншот тесты
- Тестирование навигации

## 🚀 РАЗВЕРТЫВАНИЕ

### **Google Play:**
1. Создать аккаунт разработчика
2. Настроть Store Listing
3. Загрузить APK/AAB
4. Настроить закрытое/открытое тестирование

### **Сторонние магазины:**
- **Huawei AppGallery** - для устройств Huawei
- **Amazon Appstore** - для Amazon devices
- **APK файл** - для самостоятельной установки

### **CI/CD:**
```yaml
# GitHub Actions workflow
name: Android CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 11
        uses: actions/setup-java@v3
        with:
          java-version: '11'
      - name: Build with Gradle
        run: ./gradlew build
      - name: Run tests
        run: ./gradlew test
```

## 📈 МОНИТОРИНГ И АНАЛИТИКА

### **Firebase:**
- **Crashlytics** - отслеживание ошибок
- **Analytics** - аналитика использования
- **Performance Monitoring** - мониторинг производительности
- **Remote Config** - удаленная конфигурация

### **Кастомная аналитика:**
- Количество сканирований
- Успешность распознавания
- Время работы приложения
- Частота синхронизаций

## 🔄 ОБНОВЛЕНИЯ И ПОДДЕРЖКА

### **Политика обновлений:**
- **Критические исправления** - в течение 48 часов
- **Новые функции** - ежемесячные обновления
- **Поддержка Android** - последние 3 major версии

### **Поддержка пользователей:**
- **Email:** support@cloudterminal.app
- **Telegram:** @cloudterminal_support
- **Документация:** https://docs.cloudterminal.app
- **FAQ:** https://cloudterminal.app/faq

## 📄 ЛИЦЕНЗИИ И ПРАВА

### **Лицензия приложения:**
- **Бесплатная версия** - ограниченный функционал
- **Платная версия** - полный доступ
- **Корпоративная лицензия** - кастомные интеграции

### **Используемые библиотеки:**
- **ML Kit** - Google (бесплатно)
- **CameraX** - Google (Apache 2.0)
- **Retrofit** - Square (Apache 2.0)
- **Room** - Google (Apache 2.0)
- **Dagger Hilt** - Google (Apache 2.0)

## 🎯 ДОРОЖНАЯ КАРТА

### **Q2 2026 - Версия 1.0 (MVP)**
- [x] Базовое сканирование камерой
- [ ] Локальная база данных
- [ ] CSV импорт/экспорт
- [ ] Настройки подключения

### **Q3 2026 - Версия 1.5**
- [ ] Поддержка внешних сканеров
- [ ] Синхронизация с облаком
- [ ] Расширенные отчеты
- [ ] Мультиязычность

### **Q4 2026 - Версия 2.0**
- [ ] Интеграция с 1С
- [ ] Пакетное сканирование
- [ ] Голосовое управление
- [ ] Расширенная аналитика

---

**Последнее обновление:** 2026-03-10  
**Версия документа:** 1.0.0  
**Автор:** Cloud Terminal Team