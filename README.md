# application.ChangeMonitoringSystem

application.ChangeMonitoringSystem is a Maven multi-module system made of five independently deployable Java services:

- ChangeMonitoringSystem — runtime service registry and request router;
- ChangeDetectionBot — Telegram communication service;
- UpdateTrackingSystemBot — VK communication service;
- GitHubTracker — GitHub API monitoring service;
- StackOverflowTracker — StackOverflow API monitoring service.

Services communicate over network contracts. No runtime service depends on another service's Java classes.
