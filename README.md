# ChangeMonitoringSystem

Учебная микросервисная система отслеживания изменений GitHub и Stack Overflow с доставкой уведомлений через Telegram или VK.

## Архитектура

```text
Telegram / VK
      │
      ▼
 BotService ── HTTP ──► SubscriptionService ◄── HTTP ── TrackerService
      ▲                         │                         │
      └──── уведомления ────────┘                         ├── GitHub API
                                                        └── Stack Exchange API
```

Каждое приложение — **самостоятельный Spring Boot JAR, Docker-образ и Compose-проект**. В Docker Desktop отображаются три группы: `bot-service`, `subscription-service` и `tracker-service`. Сервисы не вызывают Java-методы друг друга: обмен идет через HTTP или Kafka по общей Docker-сети.

| Сервис | Ответственность | HTTP | Метрики | PostgreSQL | Redis |
|---|---|---:|---:|---:|---:|
| `botService` | мессенджеры, команды, диалоги, доставка сообщений | 8081 | 9081 | 5433 | 6379 |
| `subscriptionService` | пользователи, подписки, маршрутизация и долговечные очереди | 8080 | 9080 | 5432 | 6381 |
| `trackerService` | GitHub/Stack Overflow, снимки ресурсов и поиск изменений | 8082 | 9082 | 5434 | 6380 |

Общая Kafka доступна на `localhost:9092`, Prometheus — на [localhost:9090](http://localhost:9090), Grafana — на [localhost:3000](http://localhost:3000).

## Быстрый запуск

Нужны **JDK 20+** и Docker Desktop. Maven отдельно устанавливать не требуется — используется wrapper.

Для нового клона сначала создайте локальные конфигурации и заполните пароли и токены:

```bash
cp botService/.env.example botService/.env
cp subscriptionService/.env.example subscriptionService/.env
cp trackerService/.env.example trackerService/.env
```

Значение `INTERNAL_API_TOKEN` / `SUBSCRIPTION_INTERNAL_API_TOKEN` должно совпадать во всех трех файлах. Файлы `.env` игнорируются Git, а `.env.example` не содержит секретов.

Общая сеть создается один раз. После этого три Compose-проекта можно запускать независимо:

```bash
./mvnw -DskipTests clean package
docker network inspect change-monitoring-system-network >/dev/null 2>&1 || \
  docker network create change-monitoring-system-network

docker compose -f subscriptionService/compose.yml up --build -d
docker compose -f botService/compose.yml up --build -d
docker compose -f trackerService/compose.yml up --build -d
```

Рекомендуемый первый запуск начинается с `subscription-service`, потому что вместе с ним поднимается общая Kafka. После создания контейнеров три группы можно останавливать и запускать в Docker Desktop в любом порядке: сетевые регистрации и недоставленные операции будут повторены автоматически.

Проверка приложений:

```bash
curl http://localhost:9080/health
curl http://localhost:9081/health
curl http://localhost:9082/health
```

## Остановка и сохранность данных

Обычная остановка безопасна:

```bash
docker compose -f botService/compose.yml stop
docker compose -f subscriptionService/compose.yml stop
docker compose -f trackerService/compose.yml stop
```

Для повторного запуска замените `stop` на `start`. Команда `down` удаляет контейнеры выбранного сервиса, но **сохраняет именованные volume**. Вариант `down -v` удалит также его данные — используйте его только для полного сброса.

Приложения обрабатывают `SIGTERM`, прекращают брать новую работу, ждут выполняющиеся задачи и закрывают HTTP-серверы и пулы соединений. Docker дает им до 60 секунд на graceful shutdown.

## Гарантии доставки

- **Входящие сообщения** сначала записываются в PostgreSQL вместе с checkpoint мессенджера. После перезапуска Telegram запрашивается с сохраненного `offset`, а уникальный `(source, external_id)` отсекает повторное чтение.
- **Межсервисные команды и уведомления** хранятся в PostgreSQL, выдаются исполнителю по lease и возвращаются в работу после истечения lease.
- **Идемпотентные идентификаторы** защищают переходы Tracker → Subscription → Bot от повторной обработки.
- **Redis не является источником истины**: в нем находятся только кэш `/list`, rate limit и quota. Потеря Redis ухудшит производительность, но не удалит подписки или уведомления.
- Для временных сетевых ошибок доставка повторяется с ограниченным exponential backoff без лимита попыток.

С Telegram возможна узкая граница дублирования: если Telegram принял `sendMessage`, а процесс завершился до фиксации результата в PostgreSQL, сообщение будет отправлено повторно. Полностью совместить «не потерять» и «никогда не продублировать» нельзя без идемпотентного ключа на стороне Telegram. Поэтому внешний участок имеет гарантию **at-least-once**, а внутренние участки идемпотентны.

## HTTP и Kafka

По умолчанию уведомления идут по HTTP. Для Kafka установите одновременно:

```text
botService/.env:          MESSAGE_TRANSPORT=KAFKA
subscriptionService/.env: SUBSCRIPTION_MESSAGE_TRANSPORT=KAFKA
trackerService/.env:      MESSAGE_TRANSPORT=KAFKA
```

После изменения пересоздайте приложения:

```bash
docker compose -f subscriptionService/compose.yml up -d --force-recreate subscription-service
docker compose -f botService/compose.yml up -d --force-recreate bot-service
docker compose -f trackerService/compose.yml up -d --force-recreate tracker-service
```

Названия топиков задаются только через конфигурацию. Некорректные сообщения попадают в DLQ.

## Просмотр баз данных

В IntelliJ IDEA, DataGrip или DBeaver создайте три PostgreSQL Data Source:

- `localhost:5432` — реквизиты `SUBSCRIPTION_DB_*`;
- `localhost:5433` — реквизиты `POSTGRES_*` из `botService/.env`;
- `localhost:5434` — реквизиты `POSTGRES_*` из `trackerService/.env`.

Таблицы можно посмотреть и без графического клиента:

```bash
docker exec -it subscription-postgres sh
psql -U "$SUBSCRIPTION_DB_USER" -d "$SUBSCRIPTION_DB_NAME"
\dt
```

Для bot/tracker-контейнеров используются `$POSTGRES_USER` и `$POSTGRES_DB`.

## Мониторинг и Kubernetes

Prometheus собирает `/metrics` с выделенных management-портов. Grafana автоматически получает Prometheus Data Source и три дашборда из `infrastructure/monitoring`.

Kubernetes-манифесты приложений находятся в `<service>/infrastructure/kubernetes`. PostgreSQL, Redis и Kafka считаются внешней инфраструктурой кластера; их адреса передаются через `ConfigMap`, а секреты — через `Secret`.

## Структура

```text
ChangeMonitoringSystem/
├── botService/
│   └── compose.yml
├── subscriptionService/
│   └── compose.yml
├── trackerService/
│   └── compose.yml
├── infrastructure/monitoring/
├── pom.xml
└── README.md
```

Подробнее: [BotService](botService/README.md), [SubscriptionService](subscriptionService/README.md), [TrackerService](trackerService/README.md).

Тестовые исходники намеренно не добавлены. Текущая проверка проекта — компиляция Maven, реальный запуск контейнеров, Flyway-миграции, health endpoints и проверка подключений.
