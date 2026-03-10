# Руководство по разработке

## 🏗️ Архитектура проекта

Проект использует **Clean Architecture + MVVM**:

### Слои архитектуры:

1. **Presentation Layer** (UI):
   - Jetpack Compose экраны
   - ViewModel для управления состоянием
   - Навигация через Compose Navigation

2. **Domain Layer** (Бизнес-логика):
   - Use Cases (интеркторы)
   - Модели данных
   - Интерфейсы репозиториев

3. **Data Layer** (Данные):
   - Реализации репозиториев
   - Room для локальной БД
   - Retrofit для сетевых запросов
   - Мапперы между слоями

## 📁 Структура пакетов

```
com.cloudterminal/
├── presentation/           # UI слой
│   ├── screens/           # Экраны приложения
│   ├── components/        # Переиспользуемые компоненты
│   └── navigation/        # Навигация
├── domain/                # Бизнес-логика
│   ├── models/           # Модели данных
│   ├── usecases/         # Use cases
│   └── repository/       # Интерфейсы репозиториев
├── data/                  # Data слой
│   ├── local/            # Локальное хранилище
│   ├── remote/           # Сетевое взаимодействие
│   └── repository/       # Реализации репозиториев
├── di/                   # Dependency Injection
└── ui/theme/             # Тема и стили
```

## 🧪 Тестирование

### Типы тестов:

1. **Unit тесты** (`app/src/test/`):
   - Тестирование ViewModel
   - Тестирование Use Cases
   - Тестирование моделей

2. **Instrumentation тесты** (`app/src/androidTest/`):
   - UI тесты
   - Интеграционные тесты
   - Тесты базы данных

### Запуск тестов:
```bash
# Все тесты
./gradlew test

# Только unit тесты
./gradlew testDebugUnitTest

# Только Android тесты
./gradlew connectedAndroidTest
```

## 📝 Стиль кода

### Kotlin стиль:
- Следуем [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Используем `ktlint` для проверки стиля

### Именование:
- **Классы:** `PascalCase` (например, `ScannerViewModel`)
- **Функции:** `camelCase` (например, `getProductByBarcode`)
- **Переменные:** `camelCase` (например, `productList`)
- **Константы:** `UPPER_SNAKE_CASE` (например, `API_BASE_URL`)

### Комментарии:
- Используем KDoc для публичных API
- Комментируем сложную бизнес-логику
- Избегаем очевидных комментариев

## 🔧 Настройка окружения

### Требования:
- Android Studio Arctic Fox+
- JDK 11+
- Android SDK 31+

### Шаги настройки:
1. Клонировать репозиторий
2. Открыть проект в Android Studio
3. Дождаться синхронизации Gradle
4. Настроить подпись для релизных сборок

### Конфигурация подписи:
Создайте файл `keystore.properties` в корне проекта:
```properties
storePassword=your_password
keyPassword=your_password
keyAlias=key
storeFile=keystore.jks
```

## 🚀 Процесс разработки

### 1. Создание новой фичи:
```bash
# Создать новую ветку
git checkout -b feature/название-фичи

# Разработать фичу
# Написать тесты
# Проверить код (lint, tests)

# Закоммитить изменения
git add .
git commit -m "feat: добавить название фичи"

# Запушить в репозиторий
git push origin feature/название-фичи

# Создать Pull Request
```

### 2. Коммит-сообщения:
Используем [Conventional Commits](https://www.conventionalcommits.org/):
- `feat:` Новая функциональность
- `fix:` Исправление бага
- `docs:` Изменения в документации
- `style:` Форматирование кода
- `refactor:` Рефакторинг кода
- `test:` Добавление тестов
- `chore:` Обновление зависимостей, настройки

### 3. Code Review:
- Проверять минимум 1 ревьювер
- Комментировать конструктивно
- Исправлять замечания до мерджа
- Проверять тесты и документацию

## 📦 Сборка и релиз

### Типы сборок:
- **Debug:** Для разработки, с отладкой
- **Release:** Для публикации, с оптимизацией

### Сборка APK:
```bash
# Debug сборка
./gradlew assembleDebug

# Release сборка
./gradlew assembleRelease
```

### Публикация:
1. Увеличить версию в `build.gradle.kts`
2. Собрать release APK
3. Протестировать на устройствах
4. Загрузить в Google Play Console

## 🔍 Отладка

### Инструменты:
- **Logcat:** Логи приложения
- **Android Profiler:** Профилирование памяти и CPU
- **Layout Inspector:** Инспектор UI
- **Database Inspector:** Просмотр БД

### Распространенные проблемы:

1. **Ошибки сборки:**
   - Очистить проект: `./gradlew clean`
   - Пересинхронизировать Gradle
   - Проверить зависимости

2. **Ошибки времени выполнения:**
   - Проверить Logcat
   - Включить детальные логи
   - Использовать точки останова

3. **Проблемы с UI:**
   - Использовать Layout Inspector
   - Проверить состояния Compose
   - Тестировать на разных размерах экрана

## 🤝 Совместная работа

### Коммуникация:
- Использовать Issues для багов и фич
- Обсуждать архитектурные решения в Discussions
- Вести документацию актуальной

### Распределение задач:
1. Создать Issue с описанием задачи
2. Назначить исполнителя
3. Обсудить подход к реализации
4. Разработать и протестировать
5. Провести Code Review
6. Замержить в основную ветку

## 📚 Полезные ресурсы

### Документация:
- [Android Developer Docs](https://developer.android.com/docs)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Retrofit](https://square.github.io/retrofit/)

### Инструменты:
- [Android Studio](https://developer.android.com/studio)
- [Git](https://git-scm.com/doc)
- [Gradle](https://gradle.org/docs/)

### Сообщество:
- [Stack Overflow](https://stackoverflow.com/questions/tagged/android)
- [Kotlin Slack](https://kotlinlang.slack.com/)
- [Android Developers Discord](https://discord.gg/androiddev)

---

**Спасибо за вклад в проект!** 🚀