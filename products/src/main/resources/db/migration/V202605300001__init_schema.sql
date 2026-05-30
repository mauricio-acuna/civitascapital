-- ============================================================
-- V202605300001__init_schema.sql
-- Magenta · products module · Initial schema
-- ============================================================

CREATE SCHEMA IF NOT EXISTS products;
SET search_path TO products;

-- Extensions (must be installed by a superuser before this migration)
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS unaccent;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- ──────────────────────────────────────────────────────────────
-- PROPERTIES
-- ──────────────────────────────────────────────────────────────
CREATE TABLE properties (
    id                UUID PRIMARY KEY,
    tenant_id         UUID NOT NULL,
    reference         VARCHAR(80) NOT NULL,
    type              VARCHAR(32) NOT NULL,
    subtype           VARCHAR(80),
    status            VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
    owner_info        JSONB NOT NULL DEFAULT '{}',
    address           JSONB NOT NULL,
    address_exact_enc BYTEA,
    postal_code       VARCHAR(10) NOT NULL,
    coordinates       GEOGRAPHY(POINT, 4326) NOT NULL,
    zone_id           UUID NOT NULL,
    visibility        VARCHAR(24) NOT NULL DEFAULT 'NEIGHBORHOOD_ONLY',
    built_sqm         NUMERIC(8,2) NOT NULL,
    useful_sqm        NUMERIC(8,2),
    plot_sqm          NUMERIC(10,2),
    rooms             SMALLINT,
    bathrooms         SMALLINT,
    terraces          SMALLINT,
    parking_spots     SMALLINT,
    storage_rooms     SMALLINT,
    floor             SMALLINT,
    has_elevator      BOOLEAN,
    condition         VARCHAR(20),
    build_year        INTEGER,
    last_reno_year    INTEGER,
    energy            JSONB NOT NULL DEFAULT '{}',
    features          TEXT[] NOT NULL DEFAULT '{}',
    orientation       TEXT[] NOT NULL DEFAULT '{}',
    ite               JSONB,
    tags              TEXT[] NOT NULL DEFAULT '{}',
    financing         JSONB NOT NULL DEFAULT '{}',
    search_vector     tsvector GENERATED ALWAYS AS
                      (to_tsvector('spanish',
                         unaccent(coalesce(reference,'') || ' ' ||
                                  coalesce(subtype,'') || ' ' ||
                                  array_to_string(tags,' ')))) STORED,
    published_at      TIMESTAMPTZ,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by        VARCHAR(64) NOT NULL,
    updated_by        VARCHAR(64) NOT NULL,
    version           BIGINT NOT NULL DEFAULT 0,
    deleted_at        TIMESTAMPTZ,
    UNIQUE (tenant_id, reference)
);

CREATE INDEX idx_prop_status    ON properties(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_prop_type      ON properties(type)   WHERE deleted_at IS NULL;
CREATE INDEX idx_prop_zone      ON properties(zone_id);
CREATE INDEX idx_prop_coords    ON properties USING gist (coordinates);
CREATE INDEX idx_prop_search    ON properties USING gin (search_vector);
CREATE INDEX idx_prop_features  ON properties USING gin (features);
CREATE INDEX idx_prop_tags      ON properties USING gin (tags);

-- ──────────────────────────────────────────────────────────────
-- OPERATIONS
-- ──────────────────────────────────────────────────────────────
CREATE TABLE operations (
    id                UUID PRIMARY KEY,
    property_id       UUID NOT NULL REFERENCES properties(id) ON DELETE CASCADE,
    type              VARCHAR(16) NOT NULL,
    price             NUMERIC(14,2) NOT NULL,
    currency          CHAR(3) NOT NULL DEFAULT 'EUR',
    deposit_months    SMALLINT,
    min_contract_mo   SMALLINT,
    rent_to_own       JSONB,
    exchange_wishes   JSONB,
    negotiable        BOOLEAN NOT NULL DEFAULT FALSE,
    available_from    DATE,
    commission_pct    NUMERIC(5,2),
    status            VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
    exclusivity       BOOLEAN NOT NULL DEFAULT FALSE,
    published_at      TIMESTAMPTZ,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    version           BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_price_pos CHECK (price > 0)
);

CREATE INDEX idx_op_property    ON operations(property_id);
CREATE INDEX idx_op_type_status ON operations(type, status);

-- ──────────────────────────────────────────────────────────────
-- MEDIA ASSETS
-- ──────────────────────────────────────────────────────────────
CREATE TABLE media_assets (
    id           UUID PRIMARY KEY,
    property_id  UUID NOT NULL REFERENCES properties(id) ON DELETE CASCADE,
    kind         VARCHAR(20) NOT NULL,
    storage_uri  TEXT NOT NULL,
    mime_type    VARCHAR(80) NOT NULL,
    size_bytes   BIGINT NOT NULL,
    width        INTEGER,
    height       INTEGER,
    ai_tags      TEXT[] NOT NULL DEFAULT '{}',
    "order"      INTEGER NOT NULL DEFAULT 0,
    is_cover     BOOLEAN NOT NULL DEFAULT FALSE,
    uploaded_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_media_property ON media_assets(property_id);

-- ──────────────────────────────────────────────────────────────
-- FAVORITES
-- ──────────────────────────────────────────────────────────────
CREATE TABLE favorites (
    customer_id UUID NOT NULL,
    property_id UUID NOT NULL REFERENCES properties(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (customer_id, property_id)
);

-- ──────────────────────────────────────────────────────────────
-- LEADS
-- ──────────────────────────────────────────────────────────────
CREATE TABLE leads (
    id             UUID PRIMARY KEY,
    property_id    UUID NOT NULL REFERENCES properties(id),
    operation_id   UUID REFERENCES operations(id),
    customer_id    UUID,
    anon_contact   JSONB,
    source         VARCHAR(24) NOT NULL,
    message        TEXT,
    status         VARCHAR(16) NOT NULL DEFAULT 'NEW',
    assigned_agent VARCHAR(64),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_lead_property ON leads(property_id);
CREATE INDEX idx_lead_status   ON leads(status, created_at DESC);

-- ──────────────────────────────────────────────────────────────
-- VISITS (with EXCLUDE GIST for agent slot conflict prevention)
-- ──────────────────────────────────────────────────────────────
CREATE TABLE visits (
    id           UUID PRIMARY KEY,
    property_id  UUID NOT NULL REFERENCES properties(id),
    customer_id  UUID NOT NULL,
    agent_id     VARCHAR(64) NOT NULL,
    slot_start   TIMESTAMPTZ NOT NULL,
    slot_end     TIMESTAMPTZ NOT NULL,
    mode         VARCHAR(16) NOT NULL,
    status       VARCHAR(16) NOT NULL DEFAULT 'REQUESTED',
    feedback     JSONB,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_slot CHECK (slot_end > slot_start),
    EXCLUDE USING gist (
        agent_id WITH =,
        tstzrange(slot_start, slot_end) WITH &&
    ) WHERE (status IN ('REQUESTED','CONFIRMED'))
);

CREATE INDEX idx_visit_property  ON visits(property_id);
CREATE INDEX idx_visit_customer  ON visits(customer_id);

-- ──────────────────────────────────────────────────────────────
-- PROPERTY VIEWS (partitioned by month)
-- ──────────────────────────────────────────────────────────────
CREATE TABLE property_views (
    id          UUID NOT NULL,
    property_id UUID NOT NULL,
    user_id     UUID,
    anon_id     UUID,
    at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    channel     VARCHAR(16),
    referrer    TEXT
) PARTITION BY RANGE (at);

CREATE TABLE property_views_2026_05 PARTITION OF property_views
    FOR VALUES FROM ('2026-05-01') TO ('2026-06-01');

CREATE TABLE property_views_2026_06 PARTITION OF property_views
    FOR VALUES FROM ('2026-06-01') TO ('2026-07-01');

CREATE INDEX idx_views_property ON property_views(property_id, at DESC);

-- ──────────────────────────────────────────────────────────────
-- TRANSACTIONS
-- ──────────────────────────────────────────────────────────────
CREATE TABLE transactions (
    id                 UUID PRIMARY KEY,
    tenant_id          UUID NOT NULL,
    property_id        UUID NOT NULL REFERENCES properties(id),
    operation_id       UUID NOT NULL REFERENCES operations(id),
    type               VARCHAR(16) NOT NULL,
    final_price        NUMERIC(14,2) NOT NULL,
    currency           CHAR(3) NOT NULL DEFAULT 'EUR',
    surface_sqm        NUMERIC(8,2) NOT NULL,
    price_per_sqm      NUMERIC(12,2) NOT NULL,
    buyer_customer_id  UUID,
    seller_customer_id UUID,
    bank_product_id    UUID,
    mortgage_amount    NUMERIC(14,2),
    ltv                NUMERIC(5,2),
    closed_at          DATE NOT NULL,
    deed_notary_proto  VARCHAR(120),
    source             VARCHAR(16) NOT NULL DEFAULT 'PLATFORM',
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_tx_property ON transactions(property_id);
CREATE INDEX idx_tx_tenant   ON transactions(tenant_id);
CREATE INDEX idx_tx_closed   ON transactions(closed_at);
