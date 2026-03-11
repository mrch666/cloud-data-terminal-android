# Cloud Data Terminal - Android приложение

## Описание
Мобильное приложение для сканирования штрих-кодов товаров и управления инвентарем с облачной синхронизацией.

## Функции
- 📷 Сканирование штрих-кодов с помощью камеры
- 📦 Управление инвентарем товаров
- 📊 Генерация отчетов и статистики
- ⚙️ Настройки API и синхронизации
- 💾 Локальное хранение данных (Room Database)
- 🔄 Синхронизация с облачным сервером

## Технологии
- Kotlin
- Jetpack Compose (UI)
- CameraX (работа с камерой)
- ML Kit (распознавание штрих-кодов)
- Room (локальная база данных)
- Retrofit (сетевые запросы)
- Dagger Hilt (dependency injection)
- WorkManager (фоновая синхронизация)

## Структура проекта
```
cloud-data-terminal-android/
├── app/
│   ├── src/main/java/com/cloudterminal/
│   │   ├── presentation/     # UI слой (Compose)
│   │   ├── domain/          # Бизнес-логика
│   │   ├── data/            # Data слой
│   │   └── di/              # Dependency Injection
│   └── src/main/res/        # Ресурсы приложения
├── build.gradle.kts         # Конфигурация сборки
└── settings.gradle.kts      # Настройки проекта
```

## Быстрый старт

### Требования
- Android Studio Flamingo или выше
- Android SDK 34
- Java 17

### Сборка
1. Клонируйте репозиторий
2. Откройте проект в Android Studio
3. Дождитесь синхронизации Gradle
4. Нажмите Run для запуска на эмуляторе или устройстве

### Или через командную строку:
```bash
./gradlew assembleDebug
```

## Конфигурация

### Настройка API
Создайте файл `local.properties` в корне проекта:
```properties
# Базовый URL API сервера
API_BASE_URL=http://your-server.com/api

# Ключ API (если требуется)
API_KEY=your_api_key_here

# Настройки синхронизации
SYNC_INTERVAL_MINUTES=15
AUTO_SYNC_ENABLED=true
```

### Настройка камеры
Приложение требует разрешения:
- `CAMERA` - для сканирования штрих-кодов
- `INTERNET` - для синхронизации данных
- `ACCESS_NETWORK_STATE` - для проверки подключения

## Разработка

### Архитектура
Приложение использует Clean Architecture + MVVM:
- **Presentation Layer**: Jetpack Compose + ViewModel
- **Domain Layer**: Use Cases + Repository Interfaces
- **Data Layer**: Repository Implementations + Data Sources

### Тестирование
- Unit тесты: JUnit 5 + Mockito
- UI тесты: Compose Testing
- Интеграционные тесты: Hilt Testing

### Code Style
- Kotlin Style Guide от Google
- Использование корутин для асинхронных операций
- Immutable data classes
- Dependency Injection через Hilt

## Деплой

### Сборка релизной версии
```bash
./gradlew assembleRelease
```

### Подписание APK
1. Создайте ключ подписи:
```bash
keytool -genkey -v -keystore my-release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias my-alias
```

2. Настройте подпись в `app/build.gradle.kts`

## Лицензия
MIT License - смотрите файл [LICENSE](LICENSE)

## Поддержка
Для вопросов и предложений создавайте issues в репозитории.
