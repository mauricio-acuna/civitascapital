-- V202506300001__init_schema.sql
-- Módulo servicios – esquema inicial con RLS y outbox
-- Convención Magenta: snake_case, UUID v7, auditoría obligatoria

CREATE SCHEMA IF NOT EXISTS services;
SET search_path TO services;

-- ── service_definitions (catálogo) ──────────────────────────────────────────
CREATE TABLE service_definitions (
    id              UUID PRIMARY KEY,
    code            VARCHAR(40)  NOT NULL UNIQUE,
    name            VARCHAR(160) NOT NULL,
    description     TEXT         NOT NULL,
    category        VARCHAR(40)  NOT NULL,
    pricing_model   VARCHAR(24)  NOT NULL,
    base_price      NUMERIC(14,2),
    price_formula   TEXT,
    sla_hours       INTEGER      NOT NULL DEFAULT 72,
    workflow_key    VARCHAR(80)  NOT NULL,
    inputs_schema   JSONB        NOT NULL DEFAULT '{}',
    outputs_schema  JSONB        NOT NULL DEFAULT '{}',
    requires_kyc    BOOLEAN      NOT NULL DEFAULT FALSE,
    valid_for       TEXT[]       NOT NULL DEFAULT '{}',
    status          VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    version         BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_svcdef_status ON service_definitions(status);
CREATE INDEX idx_svcdef_category ON service_definitions(category);

-- ── partners ─────────────────────────────────────────────────────────────────
CREATE TABLE partners (
    id                UUID        PRIMARY KEY,
    tenant_id         UUID        NOT NULL,
    code              VARCHAR(40) NOT NULL UNIQUE,
    name              VARCHAR(160) NOT NULL,
    kind              VARCHAR(20)  NOT NULL,
    services          TEXT[]      NOT NULL DEFAULT '{}',
    coverage_zone_ids UUID[]      NOT NULL DEFAULT '{}',
    commission_pct    NUMERIC(5,2),
    rating            NUMERIC(3,2),
    nps_score         SMALLINT,
    active            BOOLEAN     NOT NULL DEFAULT TRUE,
    contract_ref      VARCHAR(120),
    sepa_iban_enc     BYTEA,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    version           BIGINT      NOT NULL DEFAULT 0
);

CREATE INDEX idx_partner_tenant  ON partners(tenant_id);
CREATE INDEX idx_partner_kind    ON partners(kind);
CREATE INDEX idx_partner_active  ON partners(active) WHERE active = TRUE;
CREATE INDEX idx_partner_services ON partners USING gin(services);
CREATE INDEX idx_partner_zones    ON partners USING gin(coverage_zone_ids);

-- ── service_orders ────────────────────────────────────────────────────────────
CREATE TABLE service_orders (
    id                   UUID        PRIMARY KEY,
    tenant_id            UUID        NOT NULL,
    service_code         VARCHAR(40) NOT NULL,
    customer_id          UUID        NOT NULL,
    property_id          UUID,
    operation_id         UUID,
    bank_product_id      UUID,
    inputs               JSONB       NOT NULL DEFAULT '{}',
    price_quoted         NUMERIC(14,2) NOT NULL,
    price_final          NUMERIC(14,2),
    currency             CHAR(3)     NOT NULL DEFAULT 'EUR',
    status               VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
    workflow_instance_id VARCHAR(80),
    partner_id           UUID        REFERENCES partners(id),
    sla_due_at           TIMESTAMPTZ,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at         TIMESTAMPTZ,
    deleted_at           TIMESTAMPTZ,
    version              BIGINT      NOT NULL DEFAULT 0
);

CREATE INDEX idx_so_tenant    ON service_orders(tenant_id);
CREATE INDEX idx_so_customer  ON service_orders(customer_id);
CREATE INDEX idx_so_status    ON service_orders(status, created_at DESC);
CREATE INDEX idx_so_workflow  ON service_orders(workflow_instance_id)
    WHERE workflow_instance_id IS NOT NULL;
CREATE INDEX idx_so_sla_due   ON service_orders(sla_due_at)
    WHERE status NOT IN ('COMPLETED','CANCELLED','FAILED') AND deleted_at IS NULL;
CREATE INDEX idx_so_inputs    ON service_orders USING gin(inputs);

-- Row Level Security
ALTER TABLE service_orders ENABLE ROW LEVEL SECURITY;

CREATE POLICY so_tenant_isolation ON service_orders
    USING (tenant_id = current_setting('app.tenant_id', TRUE)::uuid);

-- ── service_order_events (historial de estado) ───────────────────────────────
CREATE TABLE service_order_events (
    id          UUID        PRIMARY KEY,
    order_id    UUID        NOT NULL REFERENCES service_orders(id),
    from_status VARCHAR(16),
    to_status   VARCHAR(16) NOT NULL,
    reason      TEXT,
    actor       VARCHAR(64) NOT NULL,
    payload     JSONB,
    at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_soe_order ON service_order_events(order_id, at DESC);

-- ── deliverables ──────────────────────────────────────────────────────────────
CREATE TABLE deliverables (
    id          UUID        PRIMARY KEY,
    order_id    UUID        NOT NULL REFERENCES service_orders(id),
    kind        VARCHAR(24) NOT NULL,
    storage_uri TEXT        NOT NULL,
    sha256      CHAR(64)    NOT NULL,
    signed_by   VARCHAR(160),
    issued_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_deliv_order ON deliverables(order_id);

-- ── payments ──────────────────────────────────────────────────────────────────
CREATE TABLE payments (
    id             UUID          PRIMARY KEY,
    order_id       UUID          NOT NULL REFERENCES service_orders(id),
    direction      VARCHAR(10)   NOT NULL,
    amount         NUMERIC(14,2) NOT NULL,
    currency       CHAR(3)       NOT NULL DEFAULT 'EUR',
    method         VARCHAR(16)   NOT NULL,
    provider_ref   VARCHAR(120),
    status         VARCHAR(16)   NOT NULL,
    vat_pct        NUMERIC(5,2)  NOT NULL DEFAULT 21,
    invoice_number VARCHAR(40),
    at             TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_pay_order  ON payments(order_id);
CREATE INDEX idx_pay_status ON payments(status);

-- ── profile_search_briefs ─────────────────────────────────────────────────────
CREATE TABLE profile_search_briefs (
    order_id            UUID          PRIMARY KEY REFERENCES service_orders(id),
    min_income_multiple NUMERIC(4,2)  NOT NULL,
    contract_type       VARCHAR(20),
    max_dependents      SMALLINT,
    requires_guarantor  BOOLEAN,
    pets_allowed        BOOLEAN,
    target_move_in_date DATE,
    shortlist           JSONB         NOT NULL DEFAULT '[]'
);

-- ── property_search_briefs ────────────────────────────────────────────────────
CREATE TABLE property_search_briefs (
    order_id       UUID          PRIMARY KEY REFERENCES service_orders(id),
    target_ticket  NUMERIC(14,2) NOT NULL,
    zone_ids       UUID[]        NOT NULL DEFAULT '{}',
    property_types TEXT[]        NOT NULL DEFAULT '{}',
    must_haves     JSONB         NOT NULL DEFAULT '[]',
    nice_to_haves  JSONB         NOT NULL DEFAULT '[]',
    deadline       DATE,
    shortlist      JSONB         NOT NULL DEFAULT '[]'
);

-- ── outbox_event (Transactional Outbox Pattern) ───────────────────────────────
CREATE TABLE outbox_event (
    id           UUID        PRIMARY KEY,
    aggregate    VARCHAR(64) NOT NULL,
    aggregate_id UUID        NOT NULL,
    type         VARCHAR(120) NOT NULL,
    topic        VARCHAR(120) NOT NULL,
    payload      JSONB       NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at TIMESTAMPTZ
);

CREATE INDEX idx_outbox_unpublished ON outbox_event(created_at)
    WHERE published_at IS NULL;

-- ── processed_events (idempotencia consumidores Kafka) ────────────────────────
CREATE TABLE processed_events (
    event_id    UUID        PRIMARY KEY,
    topic       VARCHAR(120) NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ── invoice_sequences (numeración correlativa de facturas) ───────────────────
CREATE TABLE invoice_sequences (
    series      VARCHAR(10) NOT NULL,
    year        SMALLINT    NOT NULL,
    last_number INTEGER     NOT NULL DEFAULT 0,
    PRIMARY KEY (series, year)
);

INSERT INTO invoice_sequences (series, year) VALUES ('B2C', EXTRACT(YEAR FROM now())::SMALLINT);
INSERT INTO invoice_sequences (series, year) VALUES ('B2B', EXTRACT(YEAR FROM now())::SMALLINT);
