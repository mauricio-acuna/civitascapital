-- ============================================================
-- V202405300001__init_customers_schema.sql
-- Magenta Platform – módulo customers
-- PostgreSQL 16 | Schema: customers
-- Convención: UUID v7, snake_case, soft-delete, RLS
-- ============================================================

CREATE SCHEMA IF NOT EXISTS customers;
SET search_path TO customers;

-- Extensiones necesarias (ejecutar como superuser si no existen)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ────────────────────────────────────────────────────────────
-- 1. Tabla principal: customers (aggregate root)
-- ────────────────────────────────────────────────────────────
CREATE TABLE customers (
    id                  UUID        PRIMARY KEY,
    tenant_id           UUID        NOT NULL,
    type                VARCHAR(20) NOT NULL CHECK (type IN ('INDIVIDUAL','LEGAL_ENTITY','HOUSEHOLD')),
    display_name        VARCHAR(160) NOT NULL,
    status              VARCHAR(16) NOT NULL DEFAULT 'DRAFT'
                            CHECK (status IN ('DRAFT','ACTIVE','SUSPENDED','CLOSED')),
    keycloak_user_id    VARCHAR(64),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by          VARCHAR(64) NOT NULL,
    updated_by          VARCHAR(64) NOT NULL,
    version             BIGINT      NOT NULL DEFAULT 0,
    deleted_at          TIMESTAMPTZ
);

CREATE INDEX idx_customers_tenant     ON customers(tenant_id);
CREATE UNIQUE INDEX idx_customers_kc  ON customers(keycloak_user_id) WHERE keycloak_user_id IS NOT NULL;
CREATE INDEX idx_customers_status     ON customers(status) WHERE deleted_at IS NULL;

-- ────────────────────────────────────────────────────────────
-- 2. Perfil persona física
--    PII cifrada con envelope encryption (Vault KEK + DEK pgcrypto)
-- ────────────────────────────────────────────────────────────
CREATE TABLE individual_profiles (
    customer_id         UUID        PRIMARY KEY REFERENCES customers(id),
    nif_encrypted       BYTEA       NOT NULL,
    nif_hash            VARCHAR(64) NOT NULL UNIQUE,   -- HMAC-SHA256 + pepper
    first_name          VARCHAR(80) NOT NULL,
    last_name           VARCHAR(160) NOT NULL,
    birth_date          DATE        NOT NULL,
    nationality         CHAR(2)     NOT NULL DEFAULT 'ES',
    residence_country   CHAR(2)     NOT NULL DEFAULT 'ES',
    tax_residence       CHAR(2)     NOT NULL DEFAULT 'ES',
    civil_status        VARCHAR(20),
    phone_encrypted     BYTEA,
    email_encrypted     BYTEA,
    email_hash          VARCHAR(64) UNIQUE,
    address             JSONB,
    zone_id             UUID,       -- ref Areas.zoneId (sin FK cross-módulo)
    professional        JSONB,      -- ProfessionalProfile serializado
    CONSTRAINT chk_ip_adult CHECK (birth_date <= CURRENT_DATE - INTERVAL '18 years')
);

CREATE INDEX idx_ip_zone ON individual_profiles(zone_id);

-- ────────────────────────────────────────────────────────────
-- 3. Perfil persona jurídica
-- ────────────────────────────────────────────────────────────
CREATE TABLE legal_entity_profiles (
    customer_id             UUID        PRIMARY KEY REFERENCES customers(id),
    cif                     VARCHAR(12) NOT NULL UNIQUE,
    legal_name              VARCHAR(200) NOT NULL,
    trade_name              VARCHAR(200),
    legal_form              VARCHAR(20) NOT NULL,
    reg_mercantil_number    VARCHAR(40),
    founded_at              DATE,
    cnae                    VARCHAR(6),
    representative_nif_h    VARCHAR(64),    -- hash del NIF del representante
    address                 JSONB       NOT NULL,
    ubo                     JSONB       NOT NULL DEFAULT '[]'
);

-- ────────────────────────────────────────────────────────────
-- 4. Unidades familiares
-- ────────────────────────────────────────────────────────────
CREATE TABLE households (
    customer_id         UUID        PRIMARY KEY REFERENCES customers(id),
    relationship        VARCHAR(40) NOT NULL
                            CHECK (relationship IN ('MARRIAGE','REGISTERED_PARTNERSHIP','FAMILY','OTHER')),
    dependents_count    INTEGER     NOT NULL DEFAULT 0 CHECK (dependents_count >= 0)
);

CREATE TABLE household_members (
    household_id        UUID        NOT NULL REFERENCES households(customer_id) ON DELETE CASCADE,
    individual_id       UUID        NOT NULL REFERENCES individual_profiles(customer_id),
    role                VARCHAR(20) NOT NULL CHECK (role IN ('TITULAR','COTITULAR','DEPENDENT')),
    ownership_pct       NUMERIC(5,2) NOT NULL CHECK (ownership_pct BETWEEN 0 AND 100),
    PRIMARY KEY (household_id, individual_id)
);

-- ────────────────────────────────────────────────────────────
-- 5. Snapshots financieros
-- ────────────────────────────────────────────────────────────
CREATE TABLE financial_snapshots (
    id              UUID        PRIMARY KEY,
    customer_id     UUID        NOT NULL REFERENCES customers(id),
    as_of           DATE        NOT NULL,
    net_income      NUMERIC(12,2) NOT NULL CHECK (net_income >= 0),
    payments        SMALLINT    NOT NULL DEFAULT 12 CHECK (payments IN (12,14)),
    gross_income_yr NUMERIC(12,2) CHECK (gross_income_yr >= 0),
    other_debt      NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (other_debt >= 0),
    cirbe_flag      BOOLEAN     NOT NULL DEFAULT FALSE,
    own_funds       NUMERIC(14,2) NOT NULL DEFAULT 0 CHECK (own_funds >= 0),
    existing_props  INTEGER     NOT NULL DEFAULT 0 CHECK (existing_props >= 0),
    rental_income   NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (rental_income >= 0),
    confidence      NUMERIC(4,3) NOT NULL DEFAULT 0 CHECK (confidence BETWEEN 0 AND 1),
    computed        JSONB       NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (customer_id, as_of)
);

CREATE INDEX idx_fs_customer_date ON financial_snapshots(customer_id, as_of DESC);

-- ────────────────────────────────────────────────────────────
-- 6. Estado KYC
-- ────────────────────────────────────────────────────────────
CREATE TABLE kyc_states (
    customer_id     UUID        PRIMARY KEY REFERENCES customers(id),
    status          VARCHAR(16) NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING','VERIFIED','REJECTED','EXPIRED')),
    provider        VARCHAR(40) NOT NULL,
    id_doc_type     VARCHAR(16),
    id_doc_number_h VARCHAR(64),    -- hash del número de documento
    checks          JSONB       NOT NULL DEFAULT '{}',
    score           INTEGER     CHECK (score BETWEEN 0 AND 100),
    verified_at     TIMESTAMPTZ,
    expires_at      TIMESTAMPTZ,
    provider_ref    VARCHAR(120)
);

-- ────────────────────────────────────────────────────────────
-- 7. Preferencias de búsqueda
-- ────────────────────────────────────────────────────────────
CREATE TABLE search_preferences (
    id              UUID        PRIMARY KEY,
    customer_id     UUID        NOT NULL REFERENCES customers(id),
    operation_type  VARCHAR(16) NOT NULL CHECK (operation_type IN ('SALE','RENT','RENT_TO_OWN','EXCHANGE')),
    property_types  TEXT[]      NOT NULL DEFAULT '{}',
    price_min       NUMERIC(14,2),
    price_max       NUMERIC(14,2),
    surface_min     INTEGER,
    rooms_min       SMALLINT,
    bathrooms_min   SMALLINT,
    zone_ids        UUID[]      NOT NULL DEFAULT '{}',
    requires_fiber  BOOLEAN     NOT NULL DEFAULT FALSE,
    max_risk_occup  INTEGER,
    alert_channel   VARCHAR(8)  NOT NULL DEFAULT 'NONE'
                        CHECK (alert_channel IN ('NONE','EMAIL','PUSH','BOTH')),
    alert_frequency VARCHAR(10) NOT NULL DEFAULT 'WEEKLY'
                        CHECK (alert_frequency IN ('REALTIME','DAILY','WEEKLY')),
    active          BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_sp_price CHECK (price_max IS NULL OR price_min IS NULL OR price_max >= price_min)
);

CREATE INDEX idx_sp_customer ON search_preferences(customer_id);
CREATE INDEX idx_sp_zones    ON search_preferences USING gin (zone_ids);
CREATE INDEX idx_sp_active   ON search_preferences(active) WHERE active = TRUE;

-- ────────────────────────────────────────────────────────────
-- 8. Documentos
-- ────────────────────────────────────────────────────────────
CREATE TABLE documents (
    id                  UUID        PRIMARY KEY,
    customer_id         UUID        NOT NULL REFERENCES customers(id),
    kind                VARCHAR(32) NOT NULL
                            CHECK (kind IN ('PAYSLIP','IRPF','WORK_HISTORY','MORTGAGE_RECEIPT','ID_DOC','BANK_STATEMENT')),
    filename            VARCHAR(255) NOT NULL,
    mime_type           VARCHAR(80) NOT NULL,
    size_bytes          BIGINT      NOT NULL CHECK (size_bytes > 0),
    storage_uri         TEXT        NOT NULL,
    sha256              CHAR(64)    NOT NULL,
    ocr_parsed          JSONB,
    validation_status   VARCHAR(16) NOT NULL DEFAULT 'PENDING'
                            CHECK (validation_status IN ('PENDING','VALID','INVALID')),
    uploaded_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at          TIMESTAMPTZ
);

CREATE INDEX idx_doc_customer ON documents(customer_id);
CREATE INDEX idx_doc_status   ON documents(customer_id, validation_status);

-- ────────────────────────────────────────────────────────────
-- 9. Consentimientos RGPD
-- ────────────────────────────────────────────────────────────
CREATE TABLE rgpd_consents (
    id              UUID        PRIMARY KEY,
    customer_id     UUID        NOT NULL REFERENCES customers(id),
    purpose         VARCHAR(80) NOT NULL,
    granted         BOOLEAN     NOT NULL,
    granted_at      TIMESTAMPTZ NOT NULL,
    revoked_at      TIMESTAMPTZ,
    legal_basis     VARCHAR(40) NOT NULL,
    evidence        JSONB,
    UNIQUE (customer_id, purpose)
);

CREATE INDEX idx_consent_customer ON rgpd_consents(customer_id);

-- ────────────────────────────────────────────────────────────
-- 10. Outbox de eventos (Outbox Pattern)
-- ────────────────────────────────────────────────────────────
CREATE TABLE outbox_event (
    id              UUID        PRIMARY KEY,
    aggregate       VARCHAR(64) NOT NULL,
    aggregate_id    UUID        NOT NULL,
    tenant_id       UUID        NOT NULL,
    type            VARCHAR(120) NOT NULL,
    payload         JSONB       NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at    TIMESTAMPTZ
);

CREATE INDEX idx_outbox_unpublished ON outbox_event(created_at) WHERE published_at IS NULL;

-- ────────────────────────────────────────────────────────────
-- 11. Idempotencia de eventos consumidos
-- ────────────────────────────────────────────────────────────
CREATE TABLE processed_event (
    event_id        UUID        PRIMARY KEY,
    processed_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ────────────────────────────────────────────────────────────
-- 12. Row Level Security
-- ────────────────────────────────────────────────────────────
ALTER TABLE customers               ENABLE ROW LEVEL SECURITY;
ALTER TABLE individual_profiles     ENABLE ROW LEVEL SECURITY;
ALTER TABLE legal_entity_profiles   ENABLE ROW LEVEL SECURITY;
ALTER TABLE households              ENABLE ROW LEVEL SECURITY;
ALTER TABLE household_members       ENABLE ROW LEVEL SECURITY;
ALTER TABLE financial_snapshots     ENABLE ROW LEVEL SECURITY;
ALTER TABLE kyc_states              ENABLE ROW LEVEL SECURITY;
ALTER TABLE search_preferences      ENABLE ROW LEVEL SECURITY;
ALTER TABLE documents               ENABLE ROW LEVEL SECURITY;
ALTER TABLE rgpd_consents           ENABLE ROW LEVEL SECURITY;

-- Política base: el tenant_id del JWT debe coincidir con el de la fila
CREATE POLICY tenant_isolation ON customers
    USING (tenant_id = current_setting('app.tenant_id', TRUE)::uuid);

CREATE POLICY tenant_isolation ON individual_profiles
    USING (EXISTS (SELECT 1 FROM customers c
                   WHERE c.id = individual_profiles.customer_id
                     AND c.tenant_id = current_setting('app.tenant_id', TRUE)::uuid));

CREATE POLICY tenant_isolation ON legal_entity_profiles
    USING (EXISTS (SELECT 1 FROM customers c
                   WHERE c.id = legal_entity_profiles.customer_id
                     AND c.tenant_id = current_setting('app.tenant_id', TRUE)::uuid));

CREATE POLICY tenant_isolation ON households
    USING (EXISTS (SELECT 1 FROM customers c
                   WHERE c.id = households.customer_id
                     AND c.tenant_id = current_setting('app.tenant_id', TRUE)::uuid));

CREATE POLICY tenant_isolation ON household_members
    USING (EXISTS (SELECT 1 FROM customers c
                   INNER JOIN households h ON h.customer_id = c.id
                   WHERE h.customer_id = household_members.household_id
                     AND c.tenant_id = current_setting('app.tenant_id', TRUE)::uuid));

CREATE POLICY tenant_isolation ON financial_snapshots
    USING (EXISTS (SELECT 1 FROM customers c
                   WHERE c.id = financial_snapshots.customer_id
                     AND c.tenant_id = current_setting('app.tenant_id', TRUE)::uuid));

CREATE POLICY tenant_isolation ON kyc_states
    USING (EXISTS (SELECT 1 FROM customers c
                   WHERE c.id = kyc_states.customer_id
                     AND c.tenant_id = current_setting('app.tenant_id', TRUE)::uuid));

CREATE POLICY tenant_isolation ON search_preferences
    USING (EXISTS (SELECT 1 FROM customers c
                   WHERE c.id = search_preferences.customer_id
                     AND c.tenant_id = current_setting('app.tenant_id', TRUE)::uuid));

CREATE POLICY tenant_isolation ON documents
    USING (EXISTS (SELECT 1 FROM customers c
                   WHERE c.id = documents.customer_id
                     AND c.tenant_id = current_setting('app.tenant_id', TRUE)::uuid));

CREATE POLICY tenant_isolation ON rgpd_consents
    USING (EXISTS (SELECT 1 FROM customers c
                   WHERE c.id = rgpd_consents.customer_id
                     AND c.tenant_id = current_setting('app.tenant_id', TRUE)::uuid));
