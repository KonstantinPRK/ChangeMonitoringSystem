# BotService

BotService — независимый шлюз между пользователями мессенджеров и SubscriptionService. Сейчас подключены Telegram и VK; новая платформа добавляется реализацией `MessengerClient`, `MessageUpdateSource` и `MessageSender`.

## Поток данных

```text
Telegram/VK long polling
        │
        ▼
IncomingMessageAcceptor → incoming_messages → IncomingMessageDispatcher
        │                                      │
        │                                      ▼
        │                              команды и диалоги
        │                                      │
        ▼                                      ▼
messenger_checkpoints              subscription_operations
                                               │
                                               ▼
                                    SubscriptionService HTTP API

SubscriptionService → received_notifications → outgoing_messages → Telegram/VK
```

Здесь нет бесконечного опроса коллекций. Новая работа будит `SerializedWorker` сигналом, а `@Scheduled` раз в секунду служит только восстановительной проверкой после сбоя или перезапуска.

## Основные пакеты

- `messenger` — общие контракты платформы сообщений;
- `telegram`, `vk` — API-клиенты, long polling, парсеры и отправители;
- `inbox` — долговечный входной ящик сообщений;
- `command`, `interaction` — команды и состояние диалога;
- `subscription` — клиент SubscriptionService, операции и кэш `/list`;
- `notification` — HTTP/Kafka-прием уведомлений;
- `outbox` — долговечная очередь исходящих сообщений;
- `registration` — регистрация клиентов и подтверждение доступности;
- `work` — неблокирующие сериализованные исполнители.

## PostgreSQL

Flyway создает:

| Таблица | Назначение |
|---|---|
| `bot_users` | локальная идентичность пользователя конкретного бота |
| `conversation_sessions` | текущее состояние многошагового диалога |
| `messenger_checkpoints` | следующий Telegram offset или VK timestamp |
| `incoming_messages` | inbox входящих сообщений |
| `subscription_operations` | команды к SubscriptionService |
| `received_notifications` | дедупликация уведомлений по `notification_id` |
| `outgoing_messages` | outbox сообщений пользователю |

Полученные сообщения и checkpoint сохраняются в **одной транзакции**. Если commit не состоялся, Telegram вернет обновления еще раз; уникальный индекс не позволит обработать их дважды.

## Redis

Redis хранит только кэш результата `/list`. Ключ включает ревизию списка подписок, поэтому после изменения данных используется уже новый ключ, а старый автоматически исчезает по TTL.

## API

```text
POST /api/v1/notifications
```

Endpoint принимает уведомление от SubscriptionService и отвечает `202 Accepted`. В режиме Kafka ту же роль выполняет `KafkaNotificationListener`; ошибки JSON или валидации отправляются в DLQ.

Management endpoints:

```text
GET http://localhost:9081/health
GET http://localhost:9081/metrics
```

## Конфигурация

Локальные значения находятся в `.env`, безопасный шаблон — `.env.example`.

Ключевые параметры:

- `TELEGRAM_BOT_TOKEN`, `VK_ACCESS_TOKEN` — секреты платформ;
- `TELEGRAM_BOT_ENABLED`, `VK_BOT_ENABLED` — включение клиента;
- `BOT_BASE_URL` — адрес, по которому SubscriptionService вызывает бот;
- `SUBSCRIPTION_SERVICE_BASE_URL` — адрес SubscriptionService;
- `MESSAGE_TRANSPORT=HTTP|KAFKA` — транспорт уведомлений;
- `INTERNAL_API_TOKEN` — Bearer token межсервисных запросов;
- `BOT_USER_DB_*`, `BOT_REDIS_*` — подключения к хранилищам.

## Сборка и запуск

Из корня проекта:

```bash
./mvnw -pl botService -am -DskipTests package
docker compose -f botService/compose.yml up --build -d
```

Compose-проект отображается в Docker Desktop как `bot-service` и содержит приложение, его PostgreSQL и Redis. Для запуска JAR вне Docker сначала поднимите эти хранилища и используйте локальные host/port из `.env`:

```bash
cd botService
java -jar target/BotService.jar
```

## Надежность и остановка

При остановке сервис прекращает long polling и захват новых inbox/outbox-записей, ожидает выполняющиеся задачи и только затем закрывает соединения. Незавершенная запись остается `PROCESSING`; после истечения lease она снова становится доступной.

VK получает стабильный `random_id` и дедуплицирует повтор. Telegram не принимает идемпотентный ключ для `sendMessage`, поэтому после аварии между внешней отправкой и локальным commit теоретически возможен дубль; потеря сообщения при этом не допускается.
