# Dynamic service registration

Bots and trackers use separate registration contracts and registries.

## Bot

1. Start a new bot container.
2. Wait until its messenger client and user database are ready.
3. Register through Bot Registry API.
4. Periodically confirm its availability (the distributed-systems pattern known as heartbeat).
5. Receive notifications through Bot API.

Multiple bots may advertise the same communication channel. Their botId values remain different.

## Tracker

1. Start a new tracker container.
2. Wait until its provider client and database are ready.
3. Register through Tracker Registry API.
4. Advertise supportedHosts and capabilities.
5. Periodically confirm its availability.

ChangeMonitoringSystem routes a resource URL by normalized host. Two different trackers cannot own the same host, and every trackerId identifies one unique tracker.

Services are never loaded into the ChangeMonitoringSystem JVM. Docker or Kubernetes starts a separate process, and registration makes it available without restarting the system.
