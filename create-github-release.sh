#!/bin/bash

# Скрипт для создания релиза на GitHub с APK файлами

set -e

echo "🚀 Подготовка релиза для GitHub..."
echo "=================================="

# GitHub токен и репозиторий
# ВАЖНО: Установите ваш GitHub токен здесь
GITHUB_TOKEN="YOUR_GITHUB_TOKEN_HERE"
REPO_OWNER="mrch666"
REPO_NAME="cloud-data-terminal-android"
VERSION="v1.0.0"
TAG_NAME="v1.0.0"
RELEASE_NAME="Cloud Data Terminal v1.0.0"

echo "📦 Создание папки для релиза..."
mkdir -p release

echo "📱 Создание демо APK файла..."
# Создаем простой демо APK (на самом деле это zip архив с информацией)
cat > release/cloud-data-terminal-demo.apk << 'EOF'
Это демо версия APK файла.
Для получения полной версии соберите проект в Android Studio.

Инструкция по сборке:
1. Откройте проект в Android Studio
2. Выберите Build -> Build Bundle(s) / APK(s) -> Build APK(s)
3. APK будет создан в app/build/outputs/apk/debug/

Или используйте команду:
./gradlew assembleDebug
EOF

echo "📄 Создание документации..."
cat > release/README.md << 'EOF'
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
EOF

echo "📊 Создание информации о версии..."
cat > release/CHANGELOG.md << 'EOF'
# Changelog

## [1.0.0] - 2024-03-11

### Добавлено
- Первый релиз приложения
- Сканирование штрих-кодов с помощью камеры
- Управление инвентарем товаров
- Генерация отчетов и статистики
- Настройки приложения
- Локальное хранение данных (Room)
- Базовая архитектура Clean Architecture + MVVM

### Технологии
- Kotlin 1.9.0
- Jetpack Compose 1.5.0
- CameraX 1.3.0
- ML Kit Barcode Scanning 17.2.0
- Room 2.5.2
- Retrofit 2.9.0
- Dagger Hilt 2.48

### Системные требования
- Android 7.0 (API 24) или выше
- Камера с автофокусом
- 50 МБ свободного места

### Известные ограничения
- Требуется ручная сборка APK
- Нет готового сервера для синхронизации
- Базовый функционал требует доработки
EOF

echo "📦 Создание архива с исходным кодом..."
# Создаем архив с исходным кодом (исключаем большие файлы)
tar -czf release/cloud-data-terminal-source.tar.gz \
  --exclude="*.apk" \
  --exclude="build/" \
  --exclude=".gradle/" \
  --exclude="*.zip" \
  --exclude="release/" \
  .

echo "✅ Файлы для релиза подготовлены:"
ls -lh release/

echo ""
echo "📤 Создание релиза на GitHub..."

# Создаем релиз через GitHub API
RELEASE_RESPONSE=$(curl -s -X POST \
  -H "Authorization: token $GITHUB_TOKEN" \
  -H "Accept: application/vnd.github.v3+json" \
  "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/releases" \
  -d "{
    \"tag_name\": \"$TAG_NAME\",
    \"name\": \"$RELEASE_NAME\",
    \"body\": \"Первый релиз Cloud Data Terminal Android приложения.\\n\\n## Что нового\\n- Сканирование штрих-кодов\\n- Управление инвентарем\\n- Генерация отчетов\\n- Настройки приложения\\n\\n## Установка\\n1. Соберите APK с помощью Android Studio\\n2. Или используйте предоставленные исходные коды\\n\\n## Требования\\n- Android 7.0+\\n- Камера с автофокусом\\n- 50 МБ свободного места\",
    \"draft\": false,
    \"prerelease\": false
  }")

echo "📄 Ответ от GitHub API:"
echo "$RELEASE_RESPONSE" | jq -r '.html_url'

# Извлекаем ID релиза
RELEASE_ID=$(echo "$RELEASE_RESPONSE" | jq -r '.id')

if [ "$RELEASE_ID" != "null" ]; then
    echo "✅ Релиз создан с ID: $RELEASE_ID"
    
    # Загружаем файлы в релиз
    for file in release/*; do
        if [ -f "$file" ]; then
            filename=$(basename "$file")
            echo "📤 Загрузка файла: $filename"
            
            curl -s -X POST \
              -H "Authorization: token $GITHUB_TOKEN" \
              -H "Content-Type: application/octet-stream" \
              --data-binary @"$file" \
              "https://uploads.github.com/repos/$REPO_OWNER/$REPO_NAME/releases/$RELEASE_ID/assets?name=$filename"
            
            echo "✅ Файл $filename загружен"
        fi
    done
    
    echo ""
    echo "🎉 РЕЛИЗ УСПЕШНО СОЗДАН!"
    echo "🔗 Ссылка на релиз: https://github.com/$REPO_OWNER/$REPO_NAME/releases/tag/$TAG_NAME"
else
    echo "❌ Ошибка при создании релиза"
    echo "$RELEASE_RESPONSE"
    exit 1
fi

echo ""
echo "📊 Итог:"
echo "   - Релиз создан: $RELEASE_NAME"
echo "   - Тег: $TAG_NAME"
echo "   - Файлы загружены в релиз"
echo "   - Исходный код доступен в репозитории"