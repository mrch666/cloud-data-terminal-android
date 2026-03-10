# 📱 Cloud Data Terminal - Android приложение

**Аналог:** https://wiki.lineris.ru/cloud_data_terminal  
**Назначение:** Терминал сбора данных для инвентаризации, учета товаров по штрих-кодам  
**Платформа:** Android 8.0+ (API 26+)  
**Технологии:** Kotlin, Jetpack Compose, CameraX, ML Kit, Room Database

## 🎯 ОСНОВНОЙ ФУНКЦИОНАЛ

### **1. Сканирование штрих-кодов:**
- 📸 Использование камеры смартфона как сканера
- 🔗 Поддержка внешних сканеров (Bluetooth, USB)
- 📊 Поддержка форматов: UPC-A, EAN-13, Code 128, QR Code, Data Matrix

### **2. Управление данными:**
- 📥 Загрузка справочников товаров из облачных систем
- 📤 Выгрузка результатов инвентаризации
- 🔄 Синхронизация с 1С, ERP, CRM системами

### **3. Работа оффлайн:**
- 💾 Локальная база данных (Room)
- 📁 Кэширование справочников
- ⚡ Автосинхронизация при появлении сети

### **4. Интеграции:**
- 🌐 REST API для интеграции с любыми системами
- 📡 WebSocket для реального времени
- 🔌 Плагины для 1С, WooCommerce, OpenCart

## 🏗️ АРХИТЕКТУРА ПРОЕКТА

```
cloud-data-terminal-android/
├── app/                          # Основное приложение
│   ├── src/main/
│   │   ├── java/com/cloudterminal/
│   │   │   ├── di/               # Dependency Injection (Hilt)
│   │   │   ├── data/             # Data layer
│   │   │   │   ├── local/        # Room database
│   │   │   │   ├── remote/       # API clients
│   │   │   │   └── repository/   # Репозитории
│   │   │   ├── domain/           # Domain layer
│   │   │   │   ├── models/       # Модели данных
│   │   │   │   ├── usecases/     # Use cases
│   │   │   │   └── repository/   # Интерфейсы репозиториев
│   │   │   ├── presentation/     # Presentation layer
│   │   │   │   ├── screens/      # Экраны приложения
│   │   │   │   ├── components/   # Компоненты UI
│   │   │   │   ├── viewmodels/   # ViewModels
│   │   │   │   └── navigation/   # Навигация
│   │   │   └── utils/            # Утилиты, расширения
│   │   └── res/                  # Ресурсы
│   │       ├── drawable/         # Иконки, изображения
│   │       ├── layout/           # XML layouts (если нужно)
│   │       └── values/           # Строки, цвета, стили
│   └── build.gradle.kts          # Конфигурация модуля
├── core/                         # Общие модули
│   ├── barcode-scanner/          # Модуль сканирования
│   ├── database/                 # Общая БД
│   └── network/                  # Сетевая логика
├── features/                     # Feature модули
│   ├── inventory/                # Инвентаризация
│   ├── sync/                     # Синхронизация
│   └── settings/                 # Настройки
├── build.gradle.kts              # Корневой build.gradle
├── gradle.properties             # Свойства Gradle
├── settings.gradle.kts           # Настройки проекта
└── README.md                     # Этот файл
```

## 📱 ЭКРАНЫ ПРИЛОЖЕНИЯ

### **1. Экран сканирования (главный):**
- Видоискатель камеры
- Результат сканирования в реальном времени
- Быстрые действия (добавить/удалить/изменить количество)

### **2. Список отсканированных товаров:**
- Таблица с товарами
- Поиск и фильтрация
- Группировка по категориям

### **3. Справочник товаров:**
- Загруженные из системы товары
- Поиск по названию/штрих-коду
- Быстрое сканирование из списка

### **4. Настройки:**
- Настройка подключения к системе
- Конфигурация сканера
- Настройки синхронизации

### **5. Отчеты:**
- Статистика сканирования
- Отчеты по инвентаризации
- История синхронизаций

## 🔧 ТЕХНИЧЕСКИЕ ХАРАКТЕРИСТИКИ

### **Минимальные требования:**
- **Android:** 8.0 (API 26)
- **Память:** 100MB свободного места
- **Камера:** С автофокусом (для сканирования)
- **Интернет:** Для синхронизации (работает оффлайн)

### **Поддерживаемые форматы штрих-кодов:**

| Тип | Форматы | Использование |
|-----|---------|---------------|
| **Продуктовые 1D** | UPC-A, UPC-E, EAN-8, EAN-13 | Розничная торговля |
| **Индустриальные 1D** | Code 39, Code 93, Code 128, ITF | Логистика, производство |
| **Двумерные 2D** | QR Code, Data Matrix, PDF417 | Документы, ссылки |

### **Интеграционные возможности:**
- **REST API:** Стандартные endpoints для обмена данными
- **Webhooks:** Уведомления о событиях
- **Файловый обмен:** CSV, Excel, JSON импорт/экспорт
- **1С совместимость:** Поддержка форматов 1С:Предприятие

## 🚀 БЫСТРЫЙ СТАРТ

### **Для разработчиков:**
```bash
# 1. Клонировать репозиторий
git clone https://github.com/yourusername/cloud-data-terminal-android.git

# 2. Открыть в Android Studio
# 3. Запустить на эмуляторе или устройстве
```

### **Требования для сборки:**
- Android Studio Arctic Fox (2020.3.1) или новее
- JDK 11
- Android SDK 31+

## 📦 УСТАНОВКА

### **Google Play:**
[Ссылка будет после публикации]

### **APK файл:**
Скачать последнюю версию APK с GitHub Releases

### **Для устройств без Google Play:**
1. Включите "Установку из неизвестных источников"
2. Скачайте APK файл
3. Запустите установку

## 🔌 ИНТЕГРАЦИЯ

### **API Endpoints:**

#### **1. Получение справочника товаров:**
```
GET /api/products
Headers: 
  Authorization: Bearer {token}
  X-API-Key: {api_key}
```

#### **2. Отправка результатов сканирования:**
```
POST /api/inventory
Headers:
  Authorization: Bearer {token}
  Content-Type: application/json

Body:
{
  "device_id": "string",
  "session_id": "string",
  "items": [
    {
      "barcode": "string",
      "quantity": number,
      "scanned_at": "ISO8601",
      "location": {"lat": number, "lng": number}
    }
  ]
}
```

### **Пример конфигурации:**
```json
{
  "api_url": "https://your-system.com/api",
  "api_key": "your_api_key_here",
  "sync_interval": 300,
  "auto_upload": true,
  "barcode_formats": ["EAN_13", "CODE_128", "QR_CODE"]
}
```

## 💡 ОСОБЕННОСТИ

### **1. Оффлайн работа:**
- Сканирование без интернета
- Локальное хранение данных
- Автоматическая синхронизация при подключении

### **2. Поддержка внешних сканеров:**
- Bluetooth сканеры (HID mode)
- USB сканеры через OTG
- Специализированные ТСД (DataLogic, Zebra)

### **3. Безопасность:**
- Шифрование локальной БД
- HTTPS все соединения
- JWT аутентификация
- Защита от перехвата данных

### **4. Производительность:**
- Быстрое сканирование (до 60fps)
- Оптимизированная работа с БД
- Минимальное потребление батареи

## 📊 БИЗНЕС-МОДЕЛЬ

### **Бесплатная версия:**
- ✅ Базовое сканирование
- ✅ Локальное хранение
- ✅ CSV экспорт
- ❌ Ограничение: 100 сканирований в день
- ❌ Реклама

### **Платная версия:**
- ✅ Все функции бесплатной версии
- ✅ Без ограничений
- ✅ Без рекламы
- ✅ Приоритетная поддержка
- ✅ Кастомные интеграции

**Стоимость:** $4.99/месяц или $49.99/год

## 🛠 РАЗРАБОТКА

### **Используемые библиотеки:**
```kotlin
// UI
implementation("androidx.compose.ui:ui:1.5.0")
implementation("androidx.compose.material3:material3:1.1.0")

// Camera
implementation("androidx.camera:camera-camera2:1.3.0")
implementation("androidx.camera:camera-lifecycle:1.3.0")
implementation("androidx.camera:camera-view:1.3.0")

// Barcode scanning
implementation("com.google.mlkit:barcode-scanning:17.2.0")

// Database
implementation("androidx.room:room-runtime:2.5.0")
implementation("androidx.room:room-ktx:2.5.0")

// Networking
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

// Dependency Injection
implementation("com.google.dagger:hilt-android:2.47")
```

### **Code style:**
- **Язык:** Kotlin 100%
- **Архитектура:** Clean Architecture + MVVM
- **UI:** Jetpack Compose
- **Асинхронность:** Kotlin Coroutines + Flow
- **Тестирование:** JUnit, Espresso, MockK

## 📈 ДОРОЖНАЯ КАРТА

### **Версия 1.0 (MVP):**
- [ ] Базовое сканирование камерой
- [ ] Локальное хранение данных
- [ ] CSV импорт/экспорт
- [ ] Настройки подключения

### **Версия 1.5:**
- [ ] Поддержка внешних сканеров
- [ ] Синхронизация с облаком
- [ ] Расширенные отчеты
- [ ] Мультиязычность

### **Версия 2.0:**
- [ ] Интеграция с 1С
- [ ] Пакетное сканирование
- [ ] Голосовое управление
- [ ] Расширенная аналитика

## 🤝 КОНТРИБЬЮТИНГ

1. Форкните репозиторий
2. Создайте ветку для фичи
3. Коммитьте изменения
4. Отправьте Pull Request

## 📄 ЛИЦЕНЗИЯ

MIT License - смотрите файл [LICENSE](LICENSE)

## 📞 КОНТАКТЫ

- **Email:** support@cloudterminal.app
- **Telegram:** @cloudterminal_support
- **Документация:** https://docs.cloudterminal.app
- **API документация:** https://api.cloudterminal.app/docs

---

*Проект находится в активной разработке*
*Последнее обновление: 2026-03-10*