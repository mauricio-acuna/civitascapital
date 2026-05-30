SET search_path TO banks;

ALTER TABLE processed_event
    ADD COLUMN IF NOT EXISTS consumer_name VARCHAR(120) NOT NULL DEFAULT 'legacy';

ALTER TABLE processed_event
    DROP CONSTRAINT IF EXISTS processed_event_pkey;

ALTER TABLE processed_event
    ADD CONSTRAINT processed_event_pkey PRIMARY KEY (consumer_name, event_id);

CREATE INDEX IF NOT EXISTS idx_processed_event_processed_at
    ON processed_event(processed_at);
