SET search_path TO banks;

CREATE TABLE IF NOT EXISTS idempotency_records (
    tenant_id       UUID        NOT NULL,
    idempotency_key VARCHAR(120) NOT NULL,
    request_hash    VARCHAR(128) NOT NULL,
    response_status INTEGER     NOT NULL,
    response_body   JSONB       NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (tenant_id, idempotency_key)
);

CREATE INDEX IF NOT EXISTS idx_idempotency_records_created_at
    ON idempotency_records(created_at);
