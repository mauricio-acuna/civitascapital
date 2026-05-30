-- ============================================================
-- V202605300003__outbox.sql
-- Transactional Outbox pattern table
-- ============================================================
SET search_path TO products;

CREATE TABLE outbox_event (
    id           UUID PRIMARY KEY,
    aggregate    VARCHAR(64) NOT NULL,
    aggregate_id UUID NOT NULL,
    type         VARCHAR(120) NOT NULL,
    payload      JSONB NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at TIMESTAMPTZ
);

CREATE INDEX idx_outbox_unpublished ON outbox_event(created_at)
    WHERE published_at IS NULL;

-- Processed events (idempotency guard for consumers)
CREATE TABLE processed_event (
    event_id    UUID PRIMARY KEY,
    consumer    VARCHAR(120) NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
