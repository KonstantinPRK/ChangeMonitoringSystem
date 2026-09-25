CREATE TABLE tracked_resources (
    id BIGSERIAL PRIMARY KEY,
    subscription_link_id BIGINT NOT NULL UNIQUE,
    domain VARCHAR(253) NOT NULL,
    address VARCHAR(2048) NOT NULL UNIQUE,
    canonical_url VARCHAR(2048) NOT NULL,
    provider_key VARCHAR(64) NOT NULL,
    resource_kind VARCHAR(64) NOT NULL,
    remote_resource_key VARCHAR(512) NOT NULL,
    subscription_revision BIGINT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    synchronization_generation UUID NOT NULL,
    next_scrape_at TIMESTAMPTZ NOT NULL,
    failure_count INTEGER NOT NULL DEFAULT 0,
    last_error TEXT,
    locked_until TIMESTAMPTZ,
    claim_token UUID,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT tracked_resources_failure_count_check CHECK (failure_count >= 0),
    CONSTRAINT tracked_resources_provider_remote_key_unique UNIQUE (
        provider_key,
        remote_resource_key
    )
);

CREATE TABLE resource_snapshots (
    tracked_resource_id BIGINT PRIMARY KEY REFERENCES tracked_resources(id) ON DELETE CASCADE,
    version_token VARCHAR(500),
    remote_updated_at TIMESTAMPTZ NOT NULL,
    resource_state VARCHAR(100) NOT NULL,
    content_hash VARCHAR(64) NOT NULL,
    summary_json JSONB NOT NULL,
    observed_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE link_synchronizations (
    tracker_id VARCHAR(128) PRIMARY KEY,
    generation_id UUID NOT NULL,
    after_id BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    lease_until TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    last_error TEXT,
    CONSTRAINT link_synchronizations_status_check CHECK (
        status IN ('RUNNING', 'COMPLETED', 'FAILED')
    )
);

CREATE TABLE link_sync_rejections (
    subscription_link_id BIGINT PRIMARY KEY,
    address VARCHAR(2048) NOT NULL,
    reason TEXT NOT NULL,
    synchronization_generation UUID NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE notification_outbox (
    id BIGSERIAL PRIMARY KEY,
    event_id UUID NOT NULL UNIQUE,
    tracked_resource_id BIGINT NOT NULL REFERENCES tracked_resources(id) ON DELETE CASCADE,
    payload_json JSONB NOT NULL,
    status VARCHAR(20) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    available_at TIMESTAMPTZ NOT NULL,
    locked_until TIMESTAMPTZ,
    claim_token UUID,
    last_error TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    CONSTRAINT notification_outbox_status_check CHECK (
        status IN ('PENDING', 'PROCESSING', 'RETRY', 'COMPLETED', 'FAILED')
    ),
    CONSTRAINT notification_outbox_attempts_check CHECK (attempts >= 0)
);

CREATE INDEX tracked_resources_scrape_idx
    ON tracked_resources (provider_key, next_scrape_at, id)
    WHERE active = TRUE;

CREATE INDEX tracked_resources_generation_idx
    ON tracked_resources (synchronization_generation)
    WHERE active = TRUE;

CREATE INDEX notification_outbox_ready_idx
    ON notification_outbox (available_at, id)
    WHERE status IN ('PENDING', 'RETRY');

CREATE INDEX notification_outbox_stream_idx
    ON notification_outbox (tracked_resource_id, id)
    WHERE status IN ('PENDING', 'PROCESSING', 'RETRY');
