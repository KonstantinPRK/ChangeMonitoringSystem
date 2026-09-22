# Kubernetes layout

This directory contains only the ChangeMonitoringSystem workload and platform-level Kubernetes resources.

Every bot and tracker keeps its own manifests in its local `deploy/kubernetes` directory. Each runtime service receives ordinary settings from a ConfigMap and credentials from a Secret. A newly deployed bot or tracker registers itself in ChangeMonitoringSystem after becoming ready.

Concrete manifests will be added together with HTTP servers, health endpoints and container ports.
