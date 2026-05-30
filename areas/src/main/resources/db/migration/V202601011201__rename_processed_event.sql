-- ─────────────────────────────────────────────────────────────────────────────
-- V202601011201 — Renombrar processed_event → processed_message
--
-- Motivo: spec exige processed_message(consumer_name, event_id) con PK compuesta
-- para distinguir idempotencia por consumidor. La tabla anterior usaba event_id
-- como PK único, lo que no diferenciaba entre consumidores distintos.
-- ─────────────────────────────────────────────────────────────────────────────

SET search_path = areas;

-- 1. Renombrar tabla
ALTER TABLE processed_event RENAME TO processed_message;

-- 2. Renombrar columna: topic → consumer_name
ALTER TABLE processed_message RENAME COLUMN topic TO consumer_name;

-- 3. Eliminar PK anterior (event_id solo)
ALTER TABLE processed_message DROP CONSTRAINT processed_event_pkey;

-- 4. Crear PK compuesta (consumer_name, event_id)
ALTER TABLE processed_message
    ADD CONSTRAINT processed_message_pkey PRIMARY KEY (consumer_name, event_id);

-- 5. Índice de apoyo para búsquedas por event_id cruzando consumidores (opcional)
CREATE INDEX IF NOT EXISTS idx_processed_message_event_id
    ON processed_message (event_id);
