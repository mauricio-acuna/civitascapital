-- =============================================================
-- V1 — Esquema inicial del módulo banks
-- Magenta Platform · Bounded Context: Entidades financieras
-- =============================================================
CREATE SCHEMA IF NOT EXISTS banks;
SET search_path TO banks;

-- ── Entidades financieras ──────────────────────────────────
CREATE TABLE banks (
    id              UUID PRIMARY KEY,
    tenant_id       UUID NOT NULL,
    code            VARCHAR(11)  NOT NULL,          -- BIC / código interno
    name            VARCHAR(160) NOT NULL,
    brand           VARCHAR(80),
    country         CHAR(2)      NOT NULL DEFAULT 'ES',
    bde_registry_nr VARCHAR(20),
    rating          VARCHAR(4),                     -- AAA..D (S&P/Moody's)
    logo_url        TEXT,
    website_url     TEXT,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by      VARCHAR(64)  NOT NULL,
    updated_by      VARCHAR(64)  NOT NULL,
    version         BIGINT       NOT NULL DEFAULT 0,
    deleted_at      TIMESTAMPTZ,
    CONSTRAINT uq_banks_code UNIQUE (code)
);

CREATE INDEX idx_banks_active  ON banks(active) WHERE deleted_at IS NULL;
CREATE INDEX idx_banks_rating  ON banks(rating);

-- ── Canales de contacto (embeds de Bank) ──────────────────
CREATE TABLE bank_contact_channels (
    id        UUID        PRIMARY KEY,
    bank_id   UUID        NOT NULL REFERENCES banks(id) ON DELETE CASCADE,
    type      VARCHAR(20) NOT NULL,   -- EMAIL, PHONE, WEB, BRANCH
    value     TEXT        NOT NULL,
    label     VARCHAR(80)
);

CREATE INDEX idx_bcc_bank ON bank_contact_channels(bank_id);

-- ── Productos hipotecarios ────────────────────────────────
CREATE TABLE loan_products (
    id                       UUID         PRIMARY KEY,
    tenant_id                UUID         NOT NULL,
    bank_id                  UUID         NOT NULL REFERENCES banks(id),
    sku                      VARCHAR(80)  NOT NULL,
    name                     VARCHAR(160) NOT NULL,
    category                 VARCHAR(40)  NOT NULL,  -- MORTGAGE, BRIDGE, PERSONAL_FOR_DOWNPAYMENT, RENT_GUARANTEE_LOAN, FIRST_HOME_AID, GREEN_RENOVATION
    rate_type                VARCHAR(20)  NOT NULL,  -- FIXED, VARIABLE_EURIBOR, MIXED
    tin_initial_pct          NUMERIC(6,4) NOT NULL,
    tin_index_reference      VARCHAR(20),            -- EURIBOR_12M
    tin_margin_pct           NUMERIC(6,4),
    tin_fixed_years          INTEGER,
    ltv_max_pct              NUMERIC(5,2) NOT NULL,
    ltc_max_pct              NUMERIC(5,2),
    ticket_min               NUMERIC(14,2) NOT NULL,
    ticket_max               NUMERIC(14,2) NOT NULL,
    term_min_months          INTEGER       NOT NULL,
    term_max_months          INTEGER       NOT NULL,
    eligibility              JSONB         NOT NULL DEFAULT '{"all":[]}',
    bundling                 JSONB         NOT NULL DEFAULT '[]',
    fee_opening_pct          NUMERIC(5,3) DEFAULT 0,
    fee_study_pct            NUMERIC(5,3) DEFAULT 0,
    fee_early_repayment_pct  NUMERIC(5,3) DEFAULT 0,
    scheme                   VARCHAR(24)  NOT NULL DEFAULT 'STANDARD',    -- STANDARD | NINETY_FIVE_FIVE
    promo_code               VARCHAR(40),
    valid_from               DATE         NOT NULL,
    valid_to                 DATE,
    status                   VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',     -- DRAFT | ACTIVE | DEPRECATED
    created_at               TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at               TIMESTAMPTZ  NOT NULL DEFAULT now(),
    version                  BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uq_lp_bank_sku UNIQUE (bank_id, sku),
    CONSTRAINT chk_lp_ltv CHECK (ltv_max_pct <= 100),
    CONSTRAINT chk_lp_scheme_ltv CHECK (
        scheme <> 'NINETY_FIVE_FIVE' OR ltv_max_pct >= 90
    )
);

CREATE INDEX idx_lp_bank_id     ON loan_products(bank_id);
CREATE INDEX idx_lp_scheme      ON loan_products(scheme);
CREATE INDEX idx_lp_promo       ON loan_products(promo_code);
CREATE INDEX idx_lp_category    ON loan_products(category);
CREATE INDEX idx_lp_ltv         ON loan_products(ltv_max_pct);
CREATE INDEX idx_lp_status      ON loan_products(status);
CREATE INDEX idx_lp_eligibility ON loan_products USING gin (eligibility);

-- ── Simulaciones de préstamo ──────────────────────────────
CREATE TABLE loan_simulations (
    id               UUID          PRIMARY KEY,
    tenant_id        UUID          NOT NULL,
    customer_id      UUID,
    product_id       UUID          NOT NULL REFERENCES loan_products(id),
    property_id      UUID,
    zone_id          UUID,
    requested_amount NUMERIC(14,2) NOT NULL,
    property_price   NUMERIC(14,2),
    surface_sqm      NUMERIC(8,2),
    property_type    VARCHAR(30),
    operation_type   VARCHAR(20),
    term_months      INTEGER       NOT NULL,
    borrower         JSONB         NOT NULL,
    taxes            JSONB         NOT NULL,
    result           JSONB         NOT NULL,
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_sim_customer ON loan_simulations(customer_id, created_at DESC);
CREATE INDEX idx_sim_product  ON loan_simulations(product_id);

-- ── Pre-aprobaciones ──────────────────────────────────────
CREATE TABLE preapprovals (
    id           UUID          PRIMARY KEY,
    tenant_id    UUID          NOT NULL,
    customer_id  UUID          NOT NULL,
    product_id   UUID          NOT NULL REFERENCES loan_products(id),
    property_id  UUID,
    amount       NUMERIC(14,2) NOT NULL,
    term_months  INTEGER       NOT NULL,
    ltv          NUMERIC(5,2)  NOT NULL,
    status       VARCHAR(16)   NOT NULL,   -- REQUESTED | IN_REVIEW | APPROVED | REJECTED | EXPIRED
    conditions   JSONB         NOT NULL DEFAULT '[]',
    expires_at   TIMESTAMPTZ,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    version      BIGINT        NOT NULL DEFAULT 0
);

CREATE INDEX idx_preap_customer ON preapprovals(customer_id);
CREATE INDEX idx_preap_status   ON preapprovals(status);
CREATE INDEX idx_preap_product  ON preapprovals(product_id);

-- RLS: cada tenant solo ve sus filas
ALTER TABLE preapprovals ENABLE ROW LEVEL SECURITY;

CREATE POLICY preap_tenant ON preapprovals
    USING (tenant_id = current_setting('app.tenant_id', TRUE)::uuid);

-- Historial de transiciones (append-only)
CREATE TABLE preapproval_events (
    id             UUID        PRIMARY KEY,
    preapproval_id UUID        NOT NULL REFERENCES preapprovals(id),
    from_status    VARCHAR(16),
    to_status      VARCHAR(16) NOT NULL,
    reason         TEXT,
    actor          VARCHAR(64) NOT NULL,
    at             TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_preap_ev_preap ON preapproval_events(preapproval_id, at DESC);

-- Trigger que impide UPDATE en preapproval_events (append-only)
CREATE OR REPLACE FUNCTION trg_preapproval_events_immutable()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION 'preapproval_events is append-only';
END;
$$;

CREATE TRIGGER trg_preap_ev_no_update
    BEFORE UPDATE ON preapproval_events
    FOR EACH ROW EXECUTE FUNCTION trg_preapproval_events_immutable();

-- ── Tasaciones (Appraisals) ───────────────────────────────
CREATE TABLE appraisals (
    id              UUID          PRIMARY KEY,
    tenant_id       UUID          NOT NULL,
    property_id     UUID          NOT NULL,
    customer_id     UUID,
    provider_id     UUID          NOT NULL,
    regulation      VARCHAR(20)   NOT NULL DEFAULT 'ECO_805_2003',
    market_value    NUMERIC(14,2) NOT NULL,
    mortgage_value  NUMERIC(14,2) NOT NULL,
    surface_sqm     NUMERIC(8,2)  NOT NULL,
    issued_at       DATE          NOT NULL,
    valid_until     DATE          NOT NULL,
    pdf_url         TEXT,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT chk_appraisal_valid CHECK (valid_until > issued_at)
);

CREATE INDEX idx_appraisal_property ON appraisals(property_id);
CREATE INDEX idx_appraisal_customer ON appraisals(customer_id);

-- Relación tasación ↔ pre-aprobaciones que la usaron
CREATE TABLE appraisal_preapproval (
    appraisal_id    UUID NOT NULL REFERENCES appraisals(id),
    preapproval_id  UUID NOT NULL REFERENCES preapprovals(id),
    PRIMARY KEY (appraisal_id, preapproval_id)
);

-- ── Tipos Euríbor ─────────────────────────────────────────
CREATE TABLE euribor_rates (
    period       DATE         PRIMARY KEY,
    rate_12m_pct NUMERIC(6,4) NOT NULL,
    source       VARCHAR(20)  NOT NULL DEFAULT 'EMMI'
);

-- ── Outbox de eventos de dominio (Transactional Outbox) ───
CREATE TABLE outbox_event (
    id           UUID        PRIMARY KEY,
    aggregate    VARCHAR(64),
    aggregate_id UUID,
    type         VARCHAR(120),
    payload      JSONB,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at TIMESTAMPTZ
);

CREATE INDEX idx_outbox_unpublished ON outbox_event(created_at)
    WHERE published_at IS NULL;

-- ── Idempotencia de consumidores Kafka ────────────────────
CREATE TABLE processed_event (
    event_id    UUID        PRIMARY KEY,
    topic       VARCHAR(120) NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
