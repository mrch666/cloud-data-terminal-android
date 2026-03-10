# 🚀 БЫСТРЫЙ СТАРТ: Cloud Data Terminal Android

## 📋 ПРЕДВАРИТЕЛЬНЫЕ ТРЕБОВАНИЯ

### **Для разработки:**
- ✅ **Android Studio** Arctic Fox (2020.3.1) или новее
- ✅ **JDK 11** или новее
- ✅ **Android SDK** 31+ (Android 12)
- ✅ **Эмулятор Android** или физическое устройство

### **Для устройства:**
- ✅ **Android 8.0+** (API 26)
- ✅ **Камера** с автофокусом
- ✅ **1GB+ свободной памяти**

## ⚡ БЫСТРЫЙ ЗАПУСК

### **Шаг 1: Клонирование репозитория**
```bash
git clone https://github.com/yourusername/cloud-data-terminal-android.git
cd cloud-data-terminal-android
```

### **Шаг 2: Открытие в Android Studio**
1. Запустите Android Studio
2. Выберите "Open" или "Open Project"
3. Найдите папку `cloud-data-terminal-android`
4. Нажмите "OK"

### **Шаг 3: Сборка проекта**
1. Дождитесь синхронизации Gradle
2. Нажмите "Build" → "Make Project" (Ctrl+F9)
3. Убедитесь что сборка прошла успешно

### **Шаг 4: Запуск на эмуляторе**
1. Создайте эмулятор (если нет):
   - Tools → Device Manager → Create Device
   - Выберите Pixel 5, API 31
2. Запустите приложение:
   - Нажмите "Run" → "Run 'app'" (Shift+F10)
   - Выберите эмулятор
   - Нажмите "OK"

### **Шаг 5: Запуск на физическом устройстве**
1. Включите "Developer options":
   - Настройки → О телефоне → Номер сборки (тапнуть 7 раз)
2. Включите "USB debugging":
   - Настройки → Для разработчиков → Отладка по USB
3. Подключите устройство по USB
4. Разрешите отладку на устройстве
5. Запустите приложение из Android Studio

## 🔧 КОНФИГУРАЦИЯ

### **1. Настройка API ключей**
Создайте файл `local.properties` в корне проекта:
```properties
# Google Cloud API Key для ML Kit (опционально)
GOOGLE_CLOUD_API_KEY=your_google_cloud_api_key_here

# Базовый URL API
API_BASE_URL=https://api.cloudterminal.app/v1

# Ключ для отладки
DEBUG_API_KEY=debug_key_123
```

### **2. Настройка сборок**
В `app/build.gradle.kts`:
```kotlin
buildTypes {
    debug {
        buildConfigField("String", "API_BASE_URL", "\"https://dev.api.cloudterminal.app/v1\"")
    }
    release {
        buildConfigField("String", "API_BASE_URL", "\"https://api.cloudterminal.app/v1\"")
    }
}
```

### **3. Разрешения**
При первом запуске предоставьте разрешения:
- ✅ **Камера** - для сканирования штрих-кодов
- ✅ **Интернет** - для синхронизации данных
- ✅ **Хранилище** - для сохранения данных

## 📱 ТЕСТИРОВАНИЕ ФУНКЦИОНАЛА

### **Тест 1: Сканирование штрих-кода**
1. Запустите приложение
2. На главном экране наведите камеру на штрих-код
3. Убедитесь что:
   - Штрих-код распознается
   - Звук сканирования воспроизводится
   - Товар добавляется в список

### **Тест 2: Работа с отсканированными товарами**
1. Отсканируйте несколько товаров
2. Нажмите на иконку "Инвентарь" внизу
3. Проверьте что:
   - Все товары отображаются
   - Можно редактировать количество
   - Можно удалять товары

### **Тест 3: Настройки подключения**
1. Перейдите в "Настройки"
2. Введите тестовые данные API:
   ```
   URL: https://dev.api.cloudterminal.app/v1
   API Key: test_key_123
   ```
3. Нажмите "Сохранить"
4. Проверьте подключение

## 🐛 ОТЛАДКА

### **Распространенные проблемы:**

#### **1. Ошибка сборки: "Failed to resolve"**
```bash
# Решение: Обновите зависимости
./gradlew clean
./gradlew build --refresh-dependencies
```

#### **2. Камера не работает на эмуляторе**
- Используйте физическое устройство
- Или настройте эмулятор с камерой:
  - Edit AVD → Show Advanced Settings → Camera: Webcam0

#### **3. Ошибка разрешений**
```kotlin
// Проверьте манифест
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" />
```

#### **4. ML Kit не распознает штрих-коды**
- Проверьте интернет соединение (первый запуск)
- Убедитесь что штрих-код в фокусе
- Попробуйте разные форматы (EAN-13, Code 128)

### **Логирование:**
```kotlin
// Включите логи в build.gradle
buildTypes {
    debug {
        isDebuggable = true
        isMinifyEnabled = false
    }
}

// Просмотр логов
adb logcat -s CloudTerminal
```

## 📦 СОЗДАНИЕ APK

### **Debug APK:**
```bash
./gradlew assembleDebug
# APK будет в: app/build/outputs/apk/debug/
```

### **Release APK:**
```bash
# 1. Создайте keystore (если нет)
keytool -genkey -v -keystore my-release-key.keystore \
  -alias cloudterminal -keyalg RSA -keysize 2048 -validity 10000

# 2. Настройте signingConfig в build.gradle
signingConfigs {
    release {
        storeFile file("my-release-key.keystore")
        storePassword "password"
        keyAlias "cloudterminal"
        keyPassword "password"
    }
}

# 3. Соберите release APK
./gradlew assembleRelease
```

## 🧪 ТЕСТИРОВАНИЕ

### **Unit тесты:**
```bash
./gradlew test
```

### **Инструментальные тесты:**
```bash
# На эмуляторе
./gradlew connectedAndroidTest

# На конкретном устройстве
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.device=emulator-5554
```

### **UI тесты:**
```bash
./gradlew app:recordDebugAndroidTestScreenshotTest
./gradlew app:verifyDebugAndroidTestScreenshotTest
```

## 🔄 ОБНОВЛЕНИЕ ПРОЕКТА

### **Обновление зависимостей:**
```bash
# Показать устаревшие зависимости
./gradlew dependencyUpdates

# Обновить все зависимости
./gradlew build --refresh-dependencies
```

### **Миграция Android Studio:**
1. Сделайте бэкап проекта
2. Откройте в новой версии Android Studio
3. Дождитесь миграции Gradle
4. Исправьте deprecated API

## 📞 ПОДДЕРЖКА

### **Полезные ресурсы:**
- 📚 **Документация:** `/PROJECT_DOCUMENTATION.md`
- 🐛 **Issues:** GitHub Issues
- 💬 **Чат:** Telegram @cloudterminal_dev
- 📧 **Email:** dev@cloudterminal.app

### **Отладка с логами:**
```bash
# Фильтрованные логи приложения
adb logcat -s CloudTerminal:I *:S

# Все логи
adb logcat -v time > logcat.txt

# Очистить логи
adb logcat -c
```

## 🎯 ЧТО ДАЛЬШЕ?

### **Для разработчиков:**
1. Изучите архитектуру в `PROJECT_DOCUMENTATION.md`
2. Добавьте новые фичи в `features/` директорию
3. Напишите тесты для нового кода
4. Создайте Pull Request

### **Для тестировщиков:**
1. Протестируйте все сценарии использования
2. Проверьте работу на разных устройствах
3. Тестируйте edge cases
4. Заводите баги в Issues

### **Для пользователей:**
1. Скачайте APK из Releases
2. Протестируйте в реальных условиях
3. Оставьте feedback
4. Предложите новые функции

---

**Удачи в разработке!** 🚀

*Если возникли проблемы - создайте Issue на GitHub или напишите в чат поддержки.*