<h1 align="center">🏋️ Powerlifting Assistant</h1>

<p align="center">
  <strong>Android-приложение для пауэрлифтеров: программы тренировок, дневник питания, аналитика и многое другое.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white"/>
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white"/>
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white"/>
  <img src="https://img.shields.io/badge/Firebase-Auth-FFCA28?style=for-the-badge&logo=firebase&logoColor=black"/>
  <img src="https://img.shields.io/badge/DI-Hilt-E91E63?style=for-the-badge&logo=google&logoColor=white"/>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Architecture-MVVM-orange?style=flat-square"/>
  <img src="https://img.shields.io/badge/Min%20SDK-26-blue?style=flat-square"/>
  <img src="https://img.shields.io/badge/Target%20SDK-35-blue?style=flat-square"/>
  <img src="https://img.shields.io/badge/License-MIT-green?style=flat-square"/>
</p>

---

## О проекте

**Powerlifting Assistant** — клиентская часть мобильного приложения для спортсменов-пауэрлифтеров. Приложение взаимодействует с REST API сервером, обеспечивает персонализированную генерацию программ тренировок, отслеживание питания, восстановления и личных рекордов.

---

## Функциональность

| Модуль | Описание |
|--------|----------|
| **Авторизация** | Firebase Auth (email + password), автоматическое определение сессии при запуске |
| **Главная** | Сводные карточки (калории, ИМТ, достижения), календарь тренировок, быстрые действия |
| **Программа** | Генерация персональной программы на сервере, просмотр ближайших тренировок |
| **Восстановление** | Опросник (сон, самочувствие, усталость) → рекомендация от сервера → старт тренировки |
| **Тренировка** | Журнал подходов (вес, повторения, RPE), таймер, завершение с оценкой |
| **История** | Список прошедших тренировок с длительностью и статусом самочувствия |
| **Питание** | Дневник калорий и белка, цели по КБЖУ, добавление/удаление приёмов пищи |
| **Достижения** | Личные рекорды и памятные события: добавление и удаление записей |
| **Калькулятор** | Подбор блинов на штангу по заданному рабочему весу |
| **ИМТ** | Расчёт индекса массы тела с интерпретацией результата |
| **Профиль** | 1RM по приседу, жиму и тяге, рост, вес, цели по питанию |
| **Уведомления** | WorkManager-напоминания о тренировках |

---

## Архитектура

```
presentation/
├── screens/          ← Jetpack Compose UI (экраны)
├── viewmodel/        ← ViewModel + StateFlow
└── navigation/       ← NavHost + BottomBar (5 вкладок)

data/
├── api/              ← Retrofit интерфейс + DTO-модели
├── repo/             ← Репозитории (Profile, Workout, Nutrition, Achievements, Program)
└── auth/             ← Firebase токен-провайдер

di/                   ← Hilt модули
notifications/        ← WorkManager (ReminderWorker, NotificationUtils)
```

**Паттерн:** MVVM + Repository  
**DI:** Hilt (SingletonComponent)  
**Навигация:** Navigation Compose — `auth → main`, внутри main — 5 вкладок + вложенные маршруты

---

## Стек технологий

| Категория | Библиотека / Инструмент |
|-----------|------------------------|
| Язык | Kotlin 1.9 |
| UI | Jetpack Compose + Material 3 |
| Навигация | Navigation Compose |
| DI | Dagger Hilt 2.51 |
| Сеть | Retrofit 2 + OkHttp |
| Сериализация | kotlinx.serialization |
| Auth | Firebase Authentication |
| Фоновые задачи | WorkManager |
| Архитектура | MVVM, StateFlow, Coroutines |
| Сборка | Gradle Kotlin DSL |

---

## Быстрый старт

### 1. Firebase

Замените заглушку на свой конфигурационный файл:

```
app/google-services.json   ← ваш файл из Firebase Console
```

> Текущий файл в репозитории — **заглушка**. Авторизация не будет работать без реального Firebase-проекта.

### 2. URL сервера

По умолчанию приложение обращается к `http://10.0.2.2:8080/` (эмулятор → localhost).

Для подключения к удалённому серверу передайте Gradle-property при сборке:

```bash
./gradlew :app:assembleDebug -PPOWERLIFT_SERVER_BASE_URL="https://your-server.example.com/"
```

### 3. HTTP (cleartext)

В `AndroidManifest.xml` включено `android:usesCleartextTraffic="true"` для работы с локальным `http://`-сервером. При деплое на продакшн используйте HTTPS и отключите этот флаг.

---

## Тесты

```bash
./gradlew :app:testDebugUnitTest
```

| Тест | Покрытие |
|------|----------|
| `PlateCalculatorTest` | Алгоритм подбора блинов |
| `WorkoutFlowTest` | Логика тренировочного потока |

---

## Связанные репозитории

> Серверная часть (REST API) разрабатывается отдельно. Клиент ожидает API по схеме, описанной в `PowerliftingApi.kt`.

---

## Лицензия

Распространяется под лицензией **MIT**. Подробнее см. [LICENSE](LICENSE).
