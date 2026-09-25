# TrackerService

TrackerService — единый сервис отслеживания ссылок. GitHub и Stack Overflow не являются отдельными микросервисами: это взаимозаменяемые connectors внутри одного JAR с общей базой ресурсов, снимков и исходящих уведомлений.

## Архитектура connector

```text
URL → ConnectorRouter → TrackingConnector
                         ├── LinkResolver
                         ├── ResourceInspector
                         └── ProviderFailurePolicy
```

`ConnectorRegistry` строит индексы по `providerKey` и host, поэтому выбор connector выполняется за **O(1)**, а не последовательным перебором всех API.

Сейчас доступны:

- `GitHubConnector` — repositories, issues и pull requests;
- `StackOverflowConnector` — questions через Stack Exchange API.

Чтобы добавить новый источник, нужно реализовать `TrackingConnector` и его три узкие зависимости. Spring автоматически передаст bean в `ConnectorRegistry`; существующий pipeline менять не требуется.

## Поток данных

```text
SubscriptionService ──► LinkRequestController ──► запрос синхронизации
        │
        └──── GET active links ──► LinkReconciler ──► tracked_resources
                                                        │
                                                        ▼
                                                   ScrapeDispatcher
                                                        │
                                              GitHub / Stack Exchange
                                                        │
                                                        ▼
                                              resource_snapshots
                                                        │ change
                                                        ▼
                                              notification_outbox
                                                        │
                                                        ▼
                                               SubscriptionService
```

Команда `/api/v1/links/requests` запускает немедленную сверку. Периодическая полная синхронизация остается repair-механизмом: она восстанавливает каталог, если отдельная команда потерялась или сервис долго не работал.

## PostgreSQL

| Таблица | Назначение |
|---|---|
| `tracked_resources` | общий каталог GitHub/Stack Overflow ресурсов |
| `resource_snapshots` | последний наблюдаемый снимок ресурса |
| `link_synchronizations` | generation, cursor и lease полной сверки |
| `link_sync_rejections` | ссылки, которые connector не смог распознать |
| `notification_outbox` | долговечные события изменений |

Scrape-задачи не копируются в отдельную очередь: готовность определяется индексированными полями `next_scrape_at`, `locked_until` и `active` самой строки ресурса. Захват выполняется через `FOR UPDATE SKIP LOCKED`, поэтому несколько worker не получают одну ссылку одновременно.

## Redis

Redis хранит только оперативные ограничения внешних API:

- GitHub rate-limit и время reset;
- Stack Overflow quota и backoff.

Снимки и расписание scrape находятся в PostgreSQL, поэтому очистка Redis не теряет отслеживаемые ссылки.

## Изменения и outbox

После запроса к API новый `ResourceSnapshot` сравнивается с сохраненным. При изменении snapshot и запись `notification_outbox` создаются в одной транзакции. Только после commit подается сигнал dispatcher.

Отправка выполняется по HTTP или Kafka. Если подтверждение не получено, outbox сохраняет событие и повторяет доставку с exponential backoff. `eventId` остается неизменным, поэтому SubscriptionService может безопасно удалить дубль.

## API

```text
POST /api/v1/links/requests
```

Endpoint принимает команду изменения ссылки и инициирует синхронизацию с SubscriptionService. Межсервисный Bearer token проверяется, если `INTERNAL_API_TOKEN` непустой.

Management endpoints:

```text
GET http://localhost:9082/health
GET http://localhost:9082/metrics
```

## Конфигурация

Основные переменные `.env`:

- `GITHUB_TOKEN` — повышает GitHub rate limit;
- `STACK_OVERFLOW_KEY` — необязательный Stack Apps key;
- `GITHUB_SUPPORTED_HOSTS`, `STACK_OVERFLOW_SUPPORTED_HOSTS` — host routing;
- `TRACKER_SERVICE_BASE_URL` — адрес для регистрации;
- `SUBSCRIPTION_SERVICE_BASE_URL` — центральный сервис;
- `MESSAGE_TRANSPORT=HTTP|KAFKA`;
- `TRACKER_SERVICE_DB_*`, `TRACKER_SERVICE_REDIS_*`;
- `INTERNAL_API_TOKEN` — общий межсервисный Bearer token.

## Сборка и запуск

Из корня проекта:

```bash
./mvnw -pl trackerService -am -DskipTests package
docker compose -f trackerService/compose.yml up --build -d
```

Compose-проект отображается в Docker Desktop как `tracker-service` и содержит приложение, его PostgreSQL и Redis.

Проверка регистрации и поддерживаемых host:

```bash
curl http://localhost:8080/internal/trackers
curl http://localhost:8080/api/v1/subscriptions/available
curl http://localhost:9082/health
```

## Остановка и восстановление

При `SIGTERM` scheduler перестает начинать новые synchronization/scrape, dispatcher перестает захватывать outbox, а executor ожидает уже запущенные задачи. Если процесс завершится раньше результата, lease истечет и запись будет подобрана после следующего запуска.

Именованные Docker volume сохраняют каталог, snapshots, outbox и rate-limit между `docker compose -f trackerService/compose.yml stop/start`. Kafka offsets хранятся в volume Compose-проекта `subscription-service`.
