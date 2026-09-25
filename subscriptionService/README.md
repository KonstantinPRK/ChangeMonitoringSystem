# SubscriptionService

SubscriptionService — центральный сервис подписок. Он не знает, как бот общается с пользователем и как tracker читает GitHub: его зона ответственности — пользователи, связи `пользователь ↔ ссылка`, маршрутизация и надежный обмен сообщениями.

## Поток данных

```text
BotService → subscription request → service_queue → SubscriptionManager
                                                   │
                                                   ├── PostgreSQL
                                                   └── tracker request

TrackerService → link update → service_queue → NotificationManager
                                             │
                                             └── notification per subscriber → BotService
```

`service_queue` — долговечная очередь PostgreSQL. Исполнитель атомарно захватывает запись с `FOR UPDATE SKIP LOCKED`, получает `claim_token` и lease. После аварии просроченная запись снова доступна. `stream_key` сохраняет порядок команд одного пользователя или одной ссылки.

## Основные пакеты

- `bot` — API для ботов, registry и маршрутизация по `botId`;
- `tracker` — API для tracker-сервисов, registry и маршрутизация по host;
- `subscriptions` — изменение подписок и центральный `SubscriptionService` lifecycle;
- `user`, `persistence` — JPA/Hibernate-модель и запросы;
- `queue` — универсальная долговечная очередь;
- `notification`, `link` — формирование уведомлений и команд tracker-сервису;
- `messaging` — Kafka, JSON validation и DLQ;
- `cache` — Redis-кэш списков подписок;
- `registration` — динамическая регистрация и подтверждение доступности.

## PostgreSQL

| Таблица | Назначение |
|---|---|
| `service_users` | идентичность пользователя внутри конкретного `botId` |
| `tracked_links` | нормализованные уникальные ссылки и tracking revision |
| `subscriptions` | связь many-to-many пользователя и ссылки |
| `subscription_tags` | упорядоченные теги подписки |
| `subscription_filters` | упорядоченные фильтры подписки |
| `service_queue` | входящие команды и исходящие уведомления |

Схема создается Flyway, а Hibernate работает с `ddl-auto=validate`: библиотека проверяет mapping, но не меняет таблицы самостоятельно. Это позволяет видеть SQL-структуру целиком в миграциях.

Изменение подписки блокирует соответствующие user/link строки. Поэтому определение «первый подписчик» и «последний подписчик» остается корректным при конкурентных запросах.

## Redis

Кэшируется только чтение списка подписок. Ключ содержит `subscriptions_revision` пользователя:

```text
subscriptions:{userId}:{revision}:{offset}:{limit}
```

При изменении подписки revision увеличивается, поэтому следующая выдача не может прочитать устаревший ключ. Redis работает как **cache-aside**, а PostgreSQL остается источником истины.

## HTTP API

Для BotService:

```text
POST /api/v1/subscriptions/requests
POST /api/v1/subscriptions/requests/batch
GET  /api/v1/subscriptions/requests/{requestId}
GET  /api/v1/subscriptions
GET  /api/v1/subscriptions/available
```

Для TrackerService:

```text
POST /api/v1/updates
GET  /api/v1/updates/{eventId}
GET  /api/v1/trackers/{trackerId}/links
```

Динамическая регистрация:

```text
POST   /internal/bots
PUT    /internal/bots/{botId}/availability
DELETE /internal/bots/{botId}
POST   /internal/trackers
PUT    /internal/trackers/{trackerId}/availability
DELETE /internal/trackers/{trackerId}
```

`availability` — подтверждение, что удаленный процесс еще работает. Запись живет ограниченное время; сервис периодически продлевает срок. Это не загрузка JAR внутрь JVM: новый контейнер регистрируется через сеть и начинает участвовать в маршрутизации без перезапуска SubscriptionService.

## HTTP и Kafka

`SUBSCRIPTION_MESSAGE_TRANSPORT=HTTP` отправляет уведомления прямо в BotService. Значение `KAFKA` выбирает `KafkaNotificationSender` и listener обновлений tracker-сервиса.

Топики задаются конфигурацией:

- `subscription.updates` — события tracker → subscription;
- `subscription.notifications` — уведомления subscription → bot;
- `subscription.updates.dlq` — сообщения, которые нельзя распарсить или валидировать.

## Сборка и запуск

Из корня проекта:

```bash
./mvnw -pl subscriptionService -am -DskipTests package
docker compose -f subscriptionService/compose.yml up --build -d
```

Compose-проект отображается в Docker Desktop как `subscription-service`. Помимо приложения и его хранилищ, в эту группу входит общая Kafka и мониторинг Prometheus/Grafana.

Проверка:

```bash
curl http://localhost:9080/health
curl http://localhost:9080/metrics
curl http://localhost:8080/internal/bots
curl http://localhost:8080/internal/trackers
```

## Остановка

Сначала прекращается захват новых записей, затем `QueueProcessor` ожидает активную обработку. Внешние HTTP/Kafka-операции не входят в одну транзакцию с удаленным сервисом, поэтому после сомнительного результата возможен повтор. Получатели дедуплицируют его по `requestId`, `eventId` или `notificationId`.
