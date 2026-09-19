# ChangeDetectionBot

Multi-module Maven project containing two independently deployable applications:

- `bot` receives messenger updates and communicates with users;
- `tracker` stores subscriptions and checks tracked resources for changes.

Operational configuration is grouped under `deploy`. Database migrations belong
to `tracker`, because that application owns the relational database schema.
