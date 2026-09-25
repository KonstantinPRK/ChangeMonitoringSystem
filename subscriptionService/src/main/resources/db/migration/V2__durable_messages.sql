CREATE TABLE service_queue (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    message_id UUID NOT NULL,
    kind VARCHAR(32) NOT NULL,
    stream_key VARCHAR(1024) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    attempts INTEGER NOT NULL DEFAULT 0,
    available_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    locked_until TIMESTAMPTZ,
    claim_token UUID,
    last_error VARCHAR(128),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMPTZ,
    CONSTRAINT uq_queue_message UNIQUE (kind, message_id),
    CONSTRAINT ck_queue_kind CHECK (kind IN ('SUBSCRIPTION', 'LINK_NOTIFICATION', 'TRACKER_REQUEST', 'BOT_NOTIFICATION')),
    CONSTRAINT ck_queue_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),
    CONSTRAINT ck_queue_attempts CHECK (attempts >= 0)
);

CREATE INDEX ix_queue_ready ON service_queue (kind, available_at, id)
    WHERE status = 'PENDING';
CREATE INDEX ix_queue_expired ON service_queue (kind, locked_until)
    WHERE status = 'PROCESSING';
CREATE INDEX ix_queue_stream ON service_queue (kind, stream_key, id)
    WHERE status IN ('PENDING', 'PROCESSING');

