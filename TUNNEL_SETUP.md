# Настройка соединения через cloudpub

## Актуальные агенты и туннели

### Агент 1: DESKTOP-SVF1BST (основной)
- **GUID:** 243c975d-19e8-4adf-9a33-a52d01a78d14
- **Поддомен:** disloyally-limber-klipspringer.cloudpub.ru
- **URL:** https://disloyally-limber-klipspringer.cloudpub.ru/

### Агент 2: mikhailovvlad (новый аккаунт)
- **GUID:** 1f3cd597-af45-4420-8d57-a6f3cf2fbc5b
- **Поддомен:** sourly-benevolent-sanderling.cloudpub.ru
- **URL:** https://sourly-benevolent-sanderling.cloudpub.ru/

## Какой туннель использовать?

**Рекомендуется Агент 2** (новый аккаунт mikhailovvlad) - он стабильнее работает.

Чтобы переключиться, измените в `start-with-tunnel.bat`:
```bat
REM Закомментируйте Вариант 1:
REM set "SERVICE_GUID=243c975d-19e8-4adf-9a33-a52d01a78d14"
REM set "TUNNEL_URL=https://disloyally-limber-klipspringer.cloudpub.ru"

REM Раскомментируйте Вариант 2:
set "SERVICE_GUID=1f3cd597-af45-4420-8d57-a6f3cf2fbc5b"
set "TUNNEL_URL=https://sourly-benevolent-sanderling.cloudpub.ru"
```

## Инструкция по запуску сервера с туннелем

### Шаг 1: Запуск сервера с туннелем

1. Откройте терминал в IntelliJ IDEA (или cmd)
2. Перейдите в папку сервера:
   ```
   cd "d:\android projects\powerlifting-assistant-full\powerlifting-assistant-server"
   ```
3. Запустите сервер с туннелем:
   ```
   start-with-tunnel.bat
   ```

   **ИЛИ** выполните команды вручную:
   ```bash
   # Запуск сервера
   gradlew run

   # В НОВОМ окне терминала - запуск туннеля
   cd "d:\android projects\android projects\clo-3.0.2-stable-windows-x86_64"
   .\clo.exe publish http 8080
   ```

### Шаг 2: Получение URL туннеля

После запуска туннеля вы увидите список активных туннелей. URL будет выглядеть примерно так:
```
https://disloyally-limber-klipspringer.cloudpub.ru
```

Для просмотра URL выполните:
```bash
cd "d:\android projects\android projects\clo-3.0.2-stable-windows-x86_64"
.\clo.exe list
```

Или используйте скрипт:
```
d:\android projects\get-tunnel-url.bat
```

### Шаг 3: Сборка клиента с новым URL

1. Откройте терминал в папке клиента:
   ```
   cd "d:\android projects\android projects\powerlifting_assistant"
   ```

2. Соберите приложение с URL вашего туннеля:
   ```bash
   gradlew assembleDebug -PPOWERLIFT_SERVER_BASE_URL="https://disloyally-limber-klipspringer.cloudpub.ru/"
   ```

   Или используйте второй агент:
   ```bash
   gradlew assembleDebug -PPOWERLIFT_SERVER_BASE_URL="https://sourly-benevolent-sanderling.cloudpub.ru/"
   ```

3. Установите APK на устройство/эмулятор:
   ```
   app\build\outputs\apk\debug\app-debug.apk
   ```

### Шаг 4: Проверка соединения

1. Запустите приложение на Android устройстве
2. Попробуйте выполнить любой запрос к серверу (например, авторизация)
3. Проверьте логи сервера в IntelliJ IDEA

## Быстрые команды

| Действие | Команда |
|----------|---------|
| Запустить сервер с туннелем | `start-with-tunnel.bat` |
| Получить URL туннеля | `get-tunnel-url.bat` |
| Остановить все туннели | `clo.exe stop-all` |
| Сборка клиента (агент 1) | `gradlew assembleDebug -PPOWERLIFT_SERVER_BASE_URL="https://disloyally-limber-klipspringer.cloudpub.ru/"` |
| Сборка клиента (агент 2) | `gradlew assembleDebug -PPOWERLIFT_SERVER_BASE_URL="https://sourly-benevolent-sanderling.cloudpub.ru/"` |

## Примечания

- **CORS** уже настроен на сервере (`anyHost()`)
- **HTTPS** предоставляется туннелем cloudpub автоматически
- **DEV_BYPASS_AUTH=true** включен в `server-local.properties` для локальной разработки
- Для продакшена отключите `DEV_BYPASS_AUTH` и настройте Firebase Admin SDK

## Troubleshooting

### Ошибка: "Port already in use"
Закройте другие процессы на порту 8080 или измените порт в `server-local.properties`

### Ошибка: "Unauthorized" на клиенте
Проверьте, что:
- Firebase настроен правильно
- `DEV_BYPASS_AUTH=true` для локальной разработки
- Заголовок `X-DEV-UID` передаётся (опционально)

### Туннель не подключается
Проверьте:
- API токен: `clo.exe set token <your-token>`
- Интернет соединение
- Порт сервера: `clo.exe list`


это я писал а не нейронка
просмотр активности тунелей
.\clo.exe ls

запуск тунеля 
.\clo.exe start 243c975d-19e8-4adf-9a33-a52d01a78d14