#!/bin/bash

# Скрипт для сборки релизного APK Cloud Data Terminal

set -e

echo "🚀 Начинаем сборку релизного APK для Cloud Data Terminal..."
echo "=========================================================="

# Проверяем наличие Gradle wrapper
if [ ! -f "./gradlew" ]; then
    echo "❌ Ошибка: Gradle wrapper не найден!"
    exit 1
fi

# Даем права на выполнение
chmod +x ./gradlew

echo "📦 Очистка предыдущих сборок..."
./gradlew clean

echo "🔧 Сборка debug APK..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo "✅ Debug APK успешно собран!"
    DEBUG_APK=$(find ./app/build/outputs/apk -name "*.apk" | head -1)
    echo "📱 Debug APK: $DEBUG_APK"
else
    echo "❌ Ошибка при сборке debug APK!"
    exit 1
fi

echo "🔧 Сборка release APK..."
./gradlew assembleRelease

if [ $? -eq 0 ]; then
    echo "✅ Release APK успешно собран!"
    RELEASE_APK=$(find ./app/build/outputs/apk -name "*release*.apk" | head -1)
    echo "📱 Release APK: $RELEASE_APK"
else
    echo "❌ Ошибка при сборке release APK!"
    exit 1
fi

echo "📊 Создание отчета о сборке..."
cat > APK_BUILD_REPORT.md << EOF
# Отчет о сборке APK

## Информация о сборке
- Дата сборки: $(date)
- Ветка: $(git branch --show-current)
- Коммит: $(git rev-parse --short HEAD)

## Собранные APK файлы

### Debug APK
- Путь: \`$DEBUG_APK\`
- Размер: $(du -h "$DEBUG_APK" | cut -f1)

### Release APK
- Путь: \`$RELEASE_APK\`
- Размер: $(du -h "$RELEASE_APK" | cut -f1)

## Инструкция по установке

1. **На Android устройстве:**
   - Включите "Неизвестные источники" в настройках безопасности
   - Скачайте APK файл на устройство
   - Откройте файл и нажмите "Установить"

2. **Через ADB (для разработчиков):**
   \`\`\`bash
   adb install $RELEASE_APK
   \`\`\`

## Проверка подписи APK
\`\`\`bash
apksigner verify --verbose $RELEASE_APK
\`\`\`

## Архитектура приложения
Приложение поддерживает следующие архитектуры:
- arm64-v8a (64-bit ARM)
- armeabi-v7a (32-bit ARM)
- x86_64 (64-bit x86)
- x86 (32-bit x86)

## Минимальные требования
- Android API 24 (Android 7.0 Nougat)
- 50 МБ свободного места
- Разрешение на использование камеры
- Разрешение на доступ к хранилищу

## Известные проблемы
- Для работы сканера требуется камера с автофокусом
- Рекомендуется Android 10+ для лучшей производительности
EOF

echo "📄 Отчет создан: APK_BUILD_REPORT.md"

echo "📁 Создание папки для релиза..."
mkdir -p release

echo "📋 Копирование APK файлов в папку release..."
cp "$DEBUG_APK" release/cloud-data-terminal-debug.apk
cp "$RELEASE_APK" release/cloud-data-terminal-release.apk

echo "📝 Создание README для релиза..."
cat > release/README.md << EOF
# Cloud Data Terminal - Мобильное приложение

## Описание
Мобильное приложение для сканирования штрих-кодов товаров и управления инвентарем.

## Файлы

### cloud-data-terminal-release.apk
- Релизная версия приложения
- Оптимизирована для производительности
- Подписана debug ключом

### cloud-data-terminal-debug.apk
- Отладочная версия приложения
- Включены логи и отладка
- Для тестирования и разработки

## Установка

1. Скачайте \`cloud-data-terminal-release.apk\` на ваше Android устройство
2. В настройках безопасности разрешите установку из неизвестных источников
3. Откройте скачанный файл и нажмите "Установить"

## Системные требования
- Android 7.0 (API 24) или выше
- Камера с автофокусом
- 50 МБ свободного места
- Интернет-соединение (для синхронизации)

## Функции
- 📷 Сканирование штрих-кодов с помощью камеры
- 📦 Управление инвентарем товаров
- 📊 Генерация отчетов
- ⚙️ Настройки API и синхронизации
- 💾 Локальное хранение данных
- 🔄 Синхронизация с облачным сервером

## Исходный код
Исходный код доступен в репозитории: [GitHub](https://github.com/mrch666/cloud-data-terminal-android)

## Поддержка
Для вопросов и предложений создавайте issues в репозитории проекта.
EOF

echo "📊 Создание информации о версии..."
cat > release/VERSION.md << EOF
# Версия приложения

## Текущая версия
- Версия: 1.0.0
- Код версии: 1
- Дата сборки: $(date)

## История изменений

### Версия 1.0.0
- Первый релиз приложения
- Реализовано сканирование штрих-кодов
- Добавлено управление инвентарем
- Реализованы отчеты и статистика
- Добавлены настройки приложения

## Зависимости
- Kotlin 1.9.0
- Jetpack Compose 1.5.0
- CameraX 1.3.0
- ML Kit Barcode Scanning 17.2.0
- Room 2.5.2
- Retrofit 2.9.0
- Dagger Hilt 2.48

## Поддерживаемые архитектуры
- arm64-v8a
- armeabi-v7a  
- x86_64
- x86
EOF

echo "✅ Сборка завершена успешно!"
echo "📁 APK файлы находятся в папке: release/"
echo ""
echo "📱 Debug APK: release/cloud-data-terminal-debug.apk"
echo "📱 Release APK: release/cloud-data-terminal-release.apk"
echo ""
echo "📄 Документация:"
echo "   - APK_BUILD_REPORT.md"
echo "   - release/README.md"
echo "   - release/VERSION.md"

# Показываем размер файлов
echo ""
echo "📊 Размеры файлов:"
ls -lh release/*.apk