# 📱 Cloud Data Terminal Android

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)

**Android приложение для сканирования штрих-кодов товаров с облачной синхронизацией.**  
Аналог [Cloud Data Terminal](https://wiki.lineris.ru/cloud_data_terminal) для мобильных устройств.

## 🎯 Особенности

- **📸 Сканирование штрих-кодов** камерой в реальном времени
- **🗄️ Локальное хранение** в Room базе данных
- **🌐 Облачная синхронизация** через REST API
- **📊 5 основных экранов:** Сканер, Инвентаризация, Товары, Настройки, Отчеты
- **📈 Экспорт данных** в CSV/Excel форматах
- **🔧 Современная архитектура** Clean Architecture + MVVM

## 🏗️ Архитектура

```
📁 app/src/main/java/com/cloudterminal/
├── 📁 presentation/                    # UI слой (Jetpack Compose)
│   ├── 📁 screens/                     # Экраны приложения
│   ├── 📁 components/                  # UI компоненты
│   └── 📁 navigation/                  # Навигация
├── 📁 domain/                          # Бизнес-логика
│   ├── 📁 models/                      # Модели данных
│   ├── 📁 usecases/                    # Use cases
│   └── 📁 repository/                  # Интерфейсы репозиториев
├── 📁 data/                            # Data слой
│   ├── 📁 local/                       # Локальное хранилище (Room)
│   ├── 📁 remote/                      # Сетевое взаимодействие (Retrofit)
│   └── 📁 repository/                  # Реализации репозиториев
├── 📁 di/                              # Dependency Injection (Dagger Hilt)
└── 📁 ui/theme/                        # Тема приложения (Material Design 3)
```

## 🛠 Технологический стек

- **Язык:** 100% Kotlin
- **UI:** Jetpack Compose + Material Design 3
- **Архитектура:** Clean Architecture + MVVM
- **Камера:** CameraX
- **Штрих-коды:** ML Kit Barcode Scanning
- **База данных:** Room
- **Сеть:** Retrofit + OkHttp
- **DI:** Dagger Hilt
- **Асинхронность:** Coroutines + Flow
- **Тестирование:** JUnit, Mockito, Coroutines Test

## 🚀 Быстрый старт

### Требования
- Android Studio Arctic Fox+
- JDK 11+
- Android SDK 31+
- Устройство/эмулятор с Android 8.0+

### Установка
```bash
# Клонировать репозиторий
git clone https://github.com/mrch666/cloud-data-terminal-android.git
cd cloud-data-terminal-android

# Открыть в Android Studio
# File → Open → Выберите папку проекта

# Собрать проект
# Build → Make Project (Ctrl+F9)

# Запустить на эмуляторе/устройстве
# Run → Run 'app' (Shift+F10)
```

## 📱 Основные экраны

### 1. ScannerScreen
- Режим реального времени сканирования
- Поддержка форматов: EAN-13, Code 128, QR Code, Data Matrix
- Визуальный оверлей сканирования
- Управление вспышкой
- Список отсканированных товаров

### 2. InventoryScreen
- Просмотр текущего инвентаря
- Фильтрация и поиск товаров
- Корректировка остатков
- История изменений

### 3. ProductsScreen
- Справочник товаров
- Добавление/редактирование товаров
- Импорт из CSV/Excel
- Категории и теги

### 4. SettingsScreen
- Настройки API endpoints
- Конфигурация синхронизации
- Управление разрешениями
- Экспорт данных

### 5. ReportsScreen
- Отчеты по продажам
- Аналитика остатков
- История сканирований
- Экспорт отчетов

## 🧪 Тестирование

Проект включает **60+ тестов**:
- **Unit тесты:** ViewModel, Use Cases, Models
- **Integration тесты:** Room database
- **Test coverage:** ~80% основных компонентов

```bash
# Запуск всех тестов
./gradlew test

# Запуск конкретных тестов
./gradlew test --tests "*ScannerViewModelTest"
./gradlew test --tests "*RepositoryImplTest"
```

## 📊 Документация

- [📖 PROJECT_DOCUMENTATION.md](PROJECT_DOCUMENTATION.md) - Полная техническая документация
- [🚀 QUICK_START.md](QUICK_START.md) - Инструкция по запуску
- [🧪 TESTING_REPORT.md](TESTING_REPORT.md) - Отчет о тестировании (60+ тестов)
- [📱 APK_BUILD_REPORT.md](APK_BUILD_REPORT.md) - Отчет о сборке APK
- [🏆 FINAL_PROJECT_REPORT.md](FINAL_PROJECT_REPORT.md) - Финальный отчет о проекте

## 📈 Бизнес-модель

### Монетизация (как у оригинала):
- **Бесплатная версия:** Ограниченный функционал + реклама
- **Платная версия:** $4.99/месяц или $49.99/год
- **Корпоративная:** Кастомные интеграции, White-label

### Целевая аудитория:
- 🏪 Розничные магазины
- 🏭 Производственные предприятия
- 📦 Логистические компании
- 🏬 Сетевые ритейлеры

## 🤝 Вклад в проект

1. Форкните репозиторий
2. Создайте ветку для новой фичи (`git checkout -b feature/amazing-feature`)
3. Закоммитьте изменения (`git commit -m 'Add amazing feature'`)
4. Запушьте в ветку (`git push origin feature/amazing-feature`)
5. Откройте Pull Request

## 📄 Лицензия

Этот проект лицензирован под MIT License - смотрите файл [LICENSE](LICENSE) для деталей.

## 📞 Контакты

- **Репозиторий:** [https://github.com/mrch666/cloud-data-terminal-android](https://github.com/mrch666/cloud-data-terminal-android)
- **Issues:** [GitHub Issues](https://github.com/mrch666/cloud-data-terminal-android/issues)
- **Вопросы:** Откройте issue или обсуждение

## 🏆 Статус проекта

**✅ Проект полностью готов к разработке!**

- **Структура:** 100% готова
- **Архитектура:** 100% готова
- **Тесты:** 60+ тестов готовы
- **Документация:** Полная документация
- **APK:** Демо версия доступна

---

**⭐ Если проект вам понравился, поставьте звезду на GitHub!** ⭐

---
*Проект создан как аналог [Cloud Data Terminal](https://wiki.lineris.ru/cloud_data_terminal) для мобильных устройств.*