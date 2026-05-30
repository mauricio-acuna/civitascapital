-- Migración inicial del módulo areas
-- V202601011200__init.sql
-- Requiere extensiones: postgis, pg_trgm, unaccent

CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS unaccent;

CREATE SCHEMA IF NOT EXISTS areas;
SET search_path TO areas;

-- ─────────────────────────────────────────────────────────────────────────────
-- TABLA: zones
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE zones (
    id              UUID PRIMARY KEY,
    tenant_id       UUID NOT NULL,
    code            VARCHAR(120) NOT NULL,
    name            VARCHAR(160) NOT NULL,
    type            VARCHAR(20)  NOT NULL,
    parent_id       UUID REFERENCES zones(id),
    ine_code        VARCHAR(20),
    postal_codes    TEXT[]       NOT NULL DEFAULT '{}',
    centroid        GEOGRAPHY(POINT, 4326) NOT NULL,
    boundary        GEOGRAPHY(MULTIPOLYGON, 4326),
    population      INTEGER,
    area_km2        NUMERIC(12,4),
    status          VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    tags            TEXT[]       NOT NULL DEFAULT '{}',
    search_vector   tsvector GENERATED ALWAYS AS (
                        to_tsvector('spanish', unaccent(
                            coalesce(name, '') || ' ' || coalesce(code, '')
                        ))
                    ) STORED,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by      VARCHAR(64)  NOT NULL,
    updated_by      VARCHAR(64)  NOT NULL,
    version         BIGINT       NOT NULL DEFAULT 0,
    deleted_at      TIMESTAMPTZ,
    CONSTRAINT uq_zones_code          UNIQUE (code),
    CONSTRAINT uq_zones_tenant_code   UNIQUE (tenant_id, code),
    CONSTRAINT chk_zones_status       CHECK (status IN ('ACTIVE', 'DEPRECATED')),
    CONSTRAINT chk_zones_type         CHECK (type IN (
        'COUNTRY','REGION','PROVINCE','COUNTY','MUNICIPALITY',
        'DISTRICT','NEIGHBORHOOD','URBANIZATION','STREET','BUILDING'
    ))
);

CREATE INDEX idx_zones_parent      ON areas.zones(parent_id);
CREATE INDEX idx_zones_type        ON areas.zones(type);
CREATE INDEX idx_zones_postal      ON areas.zones USING gin (postal_codes);
CREATE INDEX idx_zones_search      ON areas.zones USING gin (search_vector);
CREATE INDEX idx_zones_centroid    ON areas.zones USING gist (centroid);
CREATE INDEX idx_zones_boundary    ON areas.zones USING gist (boundary);
CREATE INDEX idx_zones_tenant      ON areas.zones(tenant_id);
CREATE INDEX idx_zones_trgm        ON areas.zones USING gin (name gin_trgm_ops);

-- Row Level Security
ALTER TABLE areas.zones ENABLE ROW LEVEL SECURITY;
CREATE POLICY zones_tenant_isolation ON areas.zones
    USING (tenant_id = current_setting('app.tenant_id', true)::uuid);

-- ─────────────────────────────────────────────────────────────────────────────
-- TABLA: price_indices  (particionada por período)
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE price_indices (
    id                UUID         NOT NULL,
    tenant_id         UUID         NOT NULL,
    zone_id           UUID         NOT NULL REFERENCES areas.zones(id),
    property_type     VARCHAR(24)  NOT NULL,
    operation_type    VARCHAR(16)  NOT NULL,
    period            DATE         NOT NULL,  -- primer día del mes
    price_per_sqm     NUMERIC(12,2) NOT NULL,
    currency          CHAR(3)       NOT NULL DEFAULT 'EUR',
    yoy_delta_pct     NUMERIC(6,3),
    mom_delta_pct     NUMERIC(6,3),
    sample_size       INTEGER       NOT NULL,
    confidence        NUMERIC(4,3)  NOT NULL,
    source            VARCHAR(24)   NOT NULL,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT pk_price_indices PRIMARY KEY (id, period),
    CONSTRAINT uq_price_indices UNIQUE (zone_id, property_type, operation_type, period, source),
    CONSTRAINT chk_confidence   CHECK (confidence BETWEEN 0 AND 1),
    CONSTRAINT chk_operation    CHECK (operation_type IN ('SALE', 'RENT')),
    CONSTRAINT chk_source       CHECK (source IN ('INTERNAL','IDEALISTA','FOTOCASA','INE','MITMA'))
) PARTITION BY RANGE (period);

CREATE TABLE price_indices_2025 PARTITION OF areas.price_indices
    FOR VALUES FROM ('2025-01-01') TO ('2026-01-01');

CREATE TABLE price_indices_2026 PARTITION OF areas.price_indices
    FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');

CREATE TABLE price_indices_2027 PARTITION OF areas.price_indices
    FOR VALUES FROM ('2027-01-01') TO ('2028-01-01');

CREATE INDEX idx_pi_zone_type_op ON areas.price_indices(zone_id, property_type, operation_type, period DESC);
CREATE INDEX idx_pi_tenant       ON areas.price_indices(tenant_id);

-- Vista materializada para últimos precios (refrescada por job nightly)
CREATE MATERIALIZED VIEW latest_price_view AS
SELECT DISTINCT ON (zone_id, property_type, operation_type)
    id, zone_id, property_type, operation_type, period,
    price_per_sqm, currency, yoy_delta_pct, confidence, source
FROM areas.price_indices
WHERE confidence >= 0.5
ORDER BY zone_id, property_type, operation_type, period DESC;

CREATE UNIQUE INDEX idx_lpv_pk ON areas.latest_price_view (zone_id, property_type, operation_type);

-- ─────────────────────────────────────────────────────────────────────────────
-- TABLA: zone_enrichment
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE zone_enrichment (
    zone_id                  UUID PRIMARY KEY REFERENCES areas.zones(id),
    tenant_id                UUID NOT NULL,
    fiber_coverage_pct       INTEGER,
    has_hospital             BOOLEAN  NOT NULL DEFAULT FALSE,
    hospital_kind            VARCHAR(32) NOT NULL DEFAULT 'NONE',
    train_to_hub_minutes     INTEGER,
    highway_distance_km      NUMERIC(8,2),
    supermarkets_count       INTEGER,
    risk_occupation_score    INTEGER,
    depopulation_risk        VARCHAR(8) NOT NULL DEFAULT 'LOW',
    quality_of_life_index    INTEGER,
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_fiber     CHECK (fiber_coverage_pct IS NULL OR fiber_coverage_pct BETWEEN 0 AND 100),
    CONSTRAINT chk_occ_score CHECK (risk_occupation_score IS NULL OR risk_occupation_score BETWEEN 0 AND 100),
    CONSTRAINT chk_qol       CHECK (quality_of_life_index IS NULL OR quality_of_life_index BETWEEN 0 AND 100),
    CONSTRAINT chk_hospital_kind CHECK (hospital_kind IN ('NONE','PRIMARY_CARE','GENERAL','REFERENCE_UNIVERSITY')),
    CONSTRAINT chk_depop     CHECK (depopulation_risk IN ('LOW','MEDIUM','HIGH'))
);

-- ─────────────────────────────────────────────────────────────────────────────
-- TABLA: zone_demand_snapshots
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE zone_demand_snapshots (
    id                  UUID PRIMARY KEY,
    zone_id             UUID    NOT NULL REFERENCES areas.zones(id),
    period              DATE    NOT NULL,
    searches            INTEGER NOT NULL DEFAULT 0,
    leads               INTEGER NOT NULL DEFAULT 0,
    viewed_properties   INTEGER NOT NULL DEFAULT 0,
    saved_searches      INTEGER NOT NULL DEFAULT 0,
    supply_demand_ratio NUMERIC(8,4),
    CONSTRAINT uq_demand_zone_period UNIQUE (zone_id, period)
);

CREATE INDEX idx_demand_zone ON areas.zone_demand_snapshots(zone_id, period DESC);

-- ─────────────────────────────────────────────────────────────────────────────
-- TABLA: outbox_event  (Transactional Outbox Pattern)
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE outbox_event (
    id           UUID        PRIMARY KEY,
    aggregate    VARCHAR(64) NOT NULL,
    aggregate_id UUID        NOT NULL,
    type         VARCHAR(120) NOT NULL,
    payload      JSONB       NOT NULL,
    tenant_id    UUID,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at TIMESTAMPTZ
);

CREATE INDEX idx_outbox_unpublished ON areas.outbox_event(created_at)
    WHERE published_at IS NULL;

-- ─────────────────────────────────────────────────────────────────────────────
-- TABLA: processed_event  (idempotencia consumidores Kafka)
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE processed_event (
    event_id     UUID        PRIMARY KEY,
    topic        VARCHAR(120) NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
