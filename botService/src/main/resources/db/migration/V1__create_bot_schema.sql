CREATE TABLE bot_users (
    id UUID PRIMARY KEY,
    bot_id VARCHAR(100) NOT NULL,
    messenger VARCHAR(30) NOT NULL,
    external_user_id VARCHAR(100) NOT NULL,
    chat_id VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT bot_users_identity_unique UNIQUE (bot_id, messenger, external_user_id, chat_id),
    CONSTRAINT bot_users_status_check CHECK (status IN ('ACTIVE', 'DELETED'))
);

CREATE TABLE conversation_sessions (
    user_id UUID PRIMARY KEY REFERENCES bot_users(id) ON DELETE CASCADE,
    state VARCHAR(50) NOT NULL,
    draft_link TEXT,
    tags_json TEXT NOT NULL DEFAULT '[]',
    filters_json TEXT NOT NULL DEFAULT '[]',
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT conversation_state_check CHECK (state IN (
        'WAITING_COMMAND',
        'WAITING_TRACK_LINK',
        'WAITING_TRACK_TAGS',
        'WAITING_TRACK_FILTERS',
        'WAITING_UNTRACK_LINK',
        'WAITING_DELETE_CONFIRMATION'
    ))
);

CREATE TABLE messenger_checkpoints (
    source VARCHAR(50) PRIMARY KEY,
    next_offset BIGINT NOT NULL
);

CREATE TABLE incoming_messages (
    id UUID PRIMARY KEY,
    source VARCHAR(50) NOT NULL,
    external_id BIGINT NOT NULL,
    external_user_id VARCHAR(100) NOT NULL,
    chat_id VARCHAR(100) NOT NULL,
    text TEXT NOT NULL,
    received_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(30) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    available_at TIMESTAMPTZ NOT NULL,
    locked_until TIMESTAMPTZ,
    claim_token UUID,
    last_error TEXT,
    CONSTRAINT incoming_messages_source_id_unique UNIQUE (source, external_id),
    CONSTRAINT incoming_messages_status_check CHECK (
        status IN ('PENDING', 'PROCESSING', 'RETRY', 'COMPLETED', 'FAILED')
    ),
    CONSTRAINT incoming_messages_attempts_check CHECK (attempts >= 0)
);

CREATE INDEX incoming_messages_ready_idx
    ON incoming_messages (available_at, received_at)
    WHERE status IN ('PENDING', 'RETRY');

CREATE TABLE subscription_operations (
    id UUID PRIMARY KEY,
    operation_type VARCHAR(40) NOT NULL,
    user_id UUID NOT NULL REFERENCES bot_users(id) ON DELETE CASCADE,
    link_domain VARCHAR(255),
    link_address TEXT,
    tags_json TEXT NOT NULL DEFAULT '[]',
    filters_json TEXT NOT NULL DEFAULT '[]',
    status VARCHAR(30) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    available_at TIMESTAMPTZ NOT NULL,
    locked_until TIMESTAMPTZ,
    claim_token UUID,
    last_error TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT subscription_operations_type_check CHECK (
        operation_type IN ('TRACK', 'UNTRACK', 'LIST', 'STOP', 'DELETE_ACCOUNT')
    ),
    CONSTRAINT subscription_operations_status_check CHECK (
        status IN ('PENDING', 'PROCESSING', 'RETRY', 'COMPLETED', 'FAILED')
    ),
    CONSTRAINT subscription_operations_attempts_check CHECK (attempts >= 0)
);

CREATE INDEX subscription_operations_ready_idx
    ON subscription_operations (available_at, created_at)
    WHERE status IN ('PENDING', 'RETRY');

CREATE TABLE received_notifications (
    notification_id UUID PRIMARY KEY,
    event_id UUID NOT NULL,
    user_id UUID NOT NULL REFERENCES bot_users(id) ON DELETE CASCADE,
    received_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE outgoing_messages (
    id UUID PRIMARY KEY,
    deduplication_key VARCHAR(200) NOT NULL UNIQUE,
    user_id UUID NOT NULL REFERENCES bot_users(id) ON DELETE CASCADE,
    chat_id VARCHAR(100) NOT NULL,
    text TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    available_at TIMESTAMPTZ NOT NULL,
    locked_until TIMESTAMPTZ,
    claim_token UUID,
    messenger_message_id VARCHAR(100),
    last_error TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT outgoing_messages_status_check CHECK (
        status IN ('PENDING', 'PROCESSING', 'RETRY', 'COMPLETED', 'FAILED')
    ),
    CONSTRAINT outgoing_messages_attempts_check CHECK (attempts >= 0)
);

CREATE INDEX outgoing_messages_ready_idx
    ON outgoing_messages (available_at, created_at)
    WHERE status IN ('PENDING', 'RETRY');
