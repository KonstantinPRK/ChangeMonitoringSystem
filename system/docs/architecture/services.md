# Service boundaries

**ChangeMonitoringSystem** registers bots and trackers, tracks their confirmed availability and routes network requests.

**Bots** own messenger integration, users, dialog state and their cache.

**Trackers** own provider-specific API integration, subscriptions, checking state, migrations and persistence.

A tracker advertises **supportedHosts** during registration. The system builds a host-to-tracker index and does not contain provider-specific URL parsers.

New services are started as separate Docker containers or Kubernetes workloads and register without restarting ChangeMonitoringSystem.
