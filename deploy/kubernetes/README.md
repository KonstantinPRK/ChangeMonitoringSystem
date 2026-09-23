# Kubernetes layout

This directory contains workloads for SubscriptionService, bots and trackers, together with platform-level Kubernetes resources.

Every runtime service receives ordinary settings from a ConfigMap and credentials from a Secret. A newly deployed bot or tracker registers itself in SubscriptionService after becoming ready.

Concrete manifests will be added together with HTTP servers, health endpoints and container ports.
