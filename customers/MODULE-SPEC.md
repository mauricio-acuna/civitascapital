# Módulo `customers` — Especificación técnica (autocontenida)

> Plataforma **Magenta** · Bounded Context: **Identidad, perfil y solvencia del cliente**
> Stack: Java 25 · Spring Boot 4.0 · PostgreSQL 18 · Angular 21 · Kafka 4.3 · Keycloak 26
> Puerto local: **8083** · BD: `customers_db` · Esquema: `customers`
>
> Documento autocontenido. Contratos cross-cutting en `../ARCHITECTURE.md`.

---

## 1. Misión del módulo

Modelar al **cliente** de Magenta en sus tres formas:

1. **Persona física** (`INDIVIDUAL`) — comprador/arrendatario individual.
2. **Persona jurídica** (`LEGAL_ENTITY`) — empresas, SOCIMIs, family offices, promotoras.
3. **Unidad familiar** (`HOUSEHOLD`) — agregación de personas físicas con titularidad conjunta
   (matrimonio, pareja de hecho, familia que aspira a vivienda habitual con cofinanciación).

El módulo es **fuente de verdad** de:

- Identidad y datos de contacto.
- KYC / AML (NIF, NIE, CIF, pasaporte, residencia fiscal, PEP, sanciones).
- Perfil financiero (ingresos, deudas, ahorro, capacidad hipotecaria estimada).
- Preferencias y búsquedas guardadas (ZoneIds + filtros + alertas).
- Documentación (nóminas, IRPF, vida laboral, recibos hipoteca).
- Consentimientos RGPD y trazabilidad.

Otros módulos consultan a `customers` para **evaluar solvencia**, **personalizar resultados**
y **construir workflows** (pre-aprobaciones, contratos, seguros).

---

## 2. Casos de uso principales

| ID    | Caso de uso                                                          | Actor          |
|-------|----------------------------------------------------------------------|----------------|
| UC-C1 | Registro de cliente (persona física) con verificación email/SMS      | Anónimo        |
| UC-C2 | Alta de persona jurídica con verificación CIF + RegMercantil         | Admin/Agent    |
| UC-C3 | Crear unidad familiar y vincular titulares con su % de titularidad   | Customer       |
| UC-C4 | Completar perfil financiero (asistido por simulador `banks`)         | Customer       |
| UC-C5 | Subir documentación (nóminas, IRPF, recibos) con OCR y validación    | Customer       |
| UC-C6 | Ejecutar KYC (proveedor externo) y obtener score                     | Sistema        |
| UC-C7 | Guardar búsqueda con alerta diaria/semanal por email/push            | Customer       |
| UC-C8 | Recibir matching de inmuebles según perfil financiero + preferencias | Sistema        |
| UC-C9 | Ejercer derechos RGPD (acceso, rectificación, supresión, portabilidad)| Customer      |
| UC-C10| Calcular capacidad hipotecaria estimada (cuota máxima sostenible)    | Sistema        |
| UC-C11| Compartir perfil con banco para pre-aprobación (consentimiento expreso)| Customer    |

---

## 3. Modelo de dominio

### 3.1 Aggregate `Customer` (raíz)

```
Customer
 ├─ id, type: enum (INDIVIDUAL, LEGAL_ENTITY, HOUSEHOLD)
 ├─ displayName
 ├─ status: enum (DRAFT, ACTIVE, SUSPENDED, CLOSED)
 ├─ keycloakUserId?                  // mapping con Keycloak realm
 ├─ kyc: KycState
 ├─ rgpdConsents: List<Consent>
 ├─ createdAt, updatedAt
 │
 ├─ individual: IndividualProfile?   // si type=INDIVIDUAL
 ├─ legalEntity: LegalEntityProfile? // si type=LEGAL_ENTITY
 └─ household: HouseholdProfile?     // si type=HOUSEHOLD
```

### 3.2 Sub-entidades

```
IndividualProfile
 ├─ nif, firstName, lastName, birthDate, nationality, residenceCountry
 ├─ taxResidence, civilStatus
 ├─ phone, email
 ├─ address: PostalAddress (link a Areas.zoneId opcional)
 └─ professional: ProfessionalProfile
     ├─ contractType (INDEFINITE, TEMPORARY, FREELANCE, CIVIL_SERVANT, UNEMPLOYED, RETIRED)
     ├─ employer, jobTitle, seniorityMonths
     ├─ sector, isITSector: Boolean

LegalEntityProfile
 ├─ cif, legalName, tradeName, legalForm (SL, SA, SCP, SLU, SOCIMI, ...)
 ├─ regMercantilNumber, foundedAt, sector (CNAE)
 ├─ representativeNif, address
 └─ ubo: List<UltimateBeneficialOwner>

HouseholdProfile
 ├─ members: List<HouseholdMember>     // ref a IndividualProfile + role (TITULAR, COTITULAR, DEPENDENT)
 ├─ relationship (MARRIAGE, REGISTERED_PARTNERSHIP, FAMILY, OTHER)
 ├─ dependentsCount
 └─ aggregatedFinancials: FinancialSnapshot   // suma neta titulares
```

### 3.3 Perfil financiero (`FinancialSnapshot`)

```
FinancialSnapshot
 ├─ asOf: LocalDate
 ├─ netIncomeMonthly, payments (12 | 14)
 ├─ grossIncomeAnnual
 ├─ otherDebtMonthly, cirbeFlag: Boolean
 ├─ ownFunds (líquido disponible)
 ├─ existingProperties: Integer
 ├─ rentalIncomeMonthly
 ├─ confidence (0..1, función de docs verificadas)
 └─ computed:
     ├─ maxAffordablePaymentBdE  // 35 % netIncome - otherDebt
     ├─ maxAffordablePaymentInternal // 30 %
     ├─ savingsRunwayMonths
     └─ targetTicketPrice (orientativo)
```

### 3.4 KYC

```
KycState
 ├─ status: enum (PENDING, VERIFIED, REJECTED, EXPIRED)
 ├─ provider: enum (IDNOW, ELECTRONIC_IDENTIFICATION, ONFIDO, MANUAL)
 ├─ idDocumentType, idDocumentNumber (cifrado)
 ├─ checks: { documentAuthentic, livenessOk, sanctionsClean, pepFlag, addressVerified }
 ├─ score (0..100)
 ├─ verifiedAt, expiresAt
 └─ providerRef
```

### 3.5 Preferencias y búsquedas

```
SearchPreference
 ├─ id, customerId
 ├─ operationType: SALE | RENT | RENT_TO_OWN | EXCHANGE
 ├─ propertyTypes: Set<PropertyType>
 ├─ priceMin, priceMax, surfaceMin, rooms, bathrooms
 ├─ zoneIds: Set<UUID>                 // ref Areas
 ├─ requiresFiber, maxRiskOccupation
 ├─ proximityKmTo (POI / hospital / AVE)
 ├─ alertChannel: NONE | EMAIL | PUSH | BOTH
 ├─ alertFrequency: REALTIME | DAILY | WEEKLY
 └─ active
```

### 3.6 Documentos

```
DocumentRef
 ├─ id, customerId, kind (PAYSLIP, IRPF, WORK_HISTORY, MORTGAGE_RECEIPT, ID_DOC, BANK_STATEMENT)
 ├─ filename, mimeType, sizeBytes
 ├─ storageUri (S3, server-side encrypted)
 ├─ ocrParsed: JSONB (campos extraídos)
 ├─ validationStatus: PENDING | VALID | INVALID
 └─ uploadedAt, expiresAt
```

### 3.7 Invariantes

- `Customer.type = INDIVIDUAL` ⇒ `individual` no nulo, otros nulos.
- `Customer.type = HOUSEHOLD` ⇒ ≥ 1 titular, ningún titular menor de edad.
- `KycState.status = VERIFIED` requerido para `Preapproval` (módulo banks).
- `FinancialSnapshot.confidence` depende del % de campos cubiertos por docs verificados.

---

## 4. Esquema PostgreSQL (`customers`)

```sql
CREATE SCHEMA customers;
SET search_path TO customers;

CREATE TABLE customers (
    id              UUID PRIMARY KEY,
    tenant_id       UUID NOT NULL,
    type            VARCHAR(20) NOT NULL,
    display_name    VARCHAR(160) NOT NULL,
    status          VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
    keycloak_user_id VARCHAR(64),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      VARCHAR(64) NOT NULL,
    updated_by      VARCHAR(64) NOT NULL,
    version         BIGINT NOT NULL DEFAULT 0,
    deleted_at      TIMESTAMPTZ
);
CREATE INDEX idx_customers_kc ON customers(keycloak_user_id);

-- Persona física. Datos PII cifrados (pgcrypto + envelope con Vault).
CREATE TABLE individual_profiles (
    customer_id     UUID PRIMARY KEY REFERENCES customers(id),
    nif_encrypted   BYTEA NOT NULL,
    nif_hash        VARCHAR(64) NOT NULL UNIQUE,    -- HMAC-SHA256 con pepper
    first_name      VARCHAR(80) NOT NULL,
    last_name       VARCHAR(160) NOT NULL,
    birth_date      DATE NOT NULL,
    nationality     CHAR(2) NOT NULL DEFAULT 'ES',
    residence_country CHAR(2) NOT NULL DEFAULT 'ES',
    tax_residence   CHAR(2) NOT NULL DEFAULT 'ES',
    civil_status    VARCHAR(20),
    phone_encrypted BYTEA,
    email_encrypted BYTEA,
    email_hash      VARCHAR(64) UNIQUE,
    address         JSONB,
    zone_id         UUID,            -- ref Areas
    professional    JSONB,
    CONSTRAINT chk_age_adult CHECK (birth_date <= now() - INTERVAL '18 years')
);

CREATE TABLE legal_entity_profiles (
    customer_id          UUID PRIMARY KEY REFERENCES customers(id),
    cif                  VARCHAR(12) NOT NULL UNIQUE,
    legal_name           VARCHAR(200) NOT NULL,
    trade_name           VARCHAR(200),
    legal_form           VARCHAR(20) NOT NULL,
    reg_mercantil_number VARCHAR(40),
    founded_at           DATE,
    cnae                 VARCHAR(6),
    representative_nif_h VARCHAR(64),
    address              JSONB NOT NULL,
    ubo                  JSONB NOT NULL DEFAULT '[]'
);

CREATE TABLE households (
    customer_id      UUID PRIMARY KEY REFERENCES customers(id),
    relationship     VARCHAR(40) NOT NULL,
    dependents_count INTEGER NOT NULL DEFAULT 0
);
CREATE TABLE household_members (
    household_id     UUID NOT NULL REFERENCES households(customer_id) ON DELETE CASCADE,
    individual_id    UUID NOT NULL REFERENCES individual_profiles(customer_id),
    role             VARCHAR(20) NOT NULL,
    ownership_pct    NUMERIC(5,2) NOT NULL,
    PRIMARY KEY (household_id, individual_id),
    CHECK (ownership_pct BETWEEN 0 AND 100)
);

CREATE TABLE financial_snapshots (
    id               UUID PRIMARY KEY,
    customer_id      UUID NOT NULL REFERENCES customers(id),
    as_of            DATE NOT NULL,
    net_income       NUMERIC(12,2) NOT NULL,
    payments         SMALLINT NOT NULL DEFAULT 12,
    gross_income_yr  NUMERIC(12,2),
    other_debt       NUMERIC(12,2) NOT NULL DEFAULT 0,
    cirbe_flag       BOOLEAN NOT NULL DEFAULT FALSE,
    own_funds        NUMERIC(14,2) NOT NULL DEFAULT 0,
    existing_props   INTEGER NOT NULL DEFAULT 0,
    rental_income    NUMERIC(12,2) NOT NULL DEFAULT 0,
    confidence       NUMERIC(4,3) NOT NULL DEFAULT 0,
    computed         JSONB NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (customer_id, as_of)
);

CREATE TABLE kyc_states (
    customer_id      UUID PRIMARY KEY REFERENCES customers(id),
    status           VARCHAR(16) NOT NULL,
    provider         VARCHAR(40) NOT NULL,
    id_doc_type      VARCHAR(16),
    id_doc_number_h  VARCHAR(64),
    checks           JSONB NOT NULL DEFAULT '{}',
    score            INTEGER,
    verified_at      TIMESTAMPTZ,
    expires_at       TIMESTAMPTZ,
    provider_ref     VARCHAR(120)
);

CREATE TABLE search_preferences (
    id               UUID PRIMARY KEY,
    customer_id      UUID NOT NULL REFERENCES customers(id),
    operation_type   VARCHAR(16) NOT NULL,
    property_types   TEXT[] NOT NULL,
    price_min        NUMERIC(14,2),
    price_max        NUMERIC(14,2),
    surface_min      INTEGER,
    rooms_min        SMALLINT,
    bathrooms_min    SMALLINT,
    zone_ids         UUID[] NOT NULL DEFAULT '{}',
    requires_fiber   BOOLEAN NOT NULL DEFAULT FALSE,
    max_risk_occup   INTEGER,
    alert_channel    VARCHAR(8) NOT NULL DEFAULT 'NONE',
    alert_frequency  VARCHAR(10) NOT NULL DEFAULT 'WEEKLY',
    active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_pref_customer ON search_preferences(customer_id);
CREATE INDEX idx_pref_zones    ON search_preferences USING gin (zone_ids);

CREATE TABLE documents (
    id                UUID PRIMARY KEY,
    customer_id       UUID NOT NULL REFERENCES customers(id),
    kind              VARCHAR(32) NOT NULL,
    filename          VARCHAR(255) NOT NULL,
    mime_type         VARCHAR(80) NOT NULL,
    size_bytes        BIGINT NOT NULL,
    storage_uri       TEXT NOT NULL,
    sha256            CHAR(64) NOT NULL,
    ocr_parsed        JSONB,
    validation_status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    uploaded_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at        TIMESTAMPTZ
);

CREATE TABLE rgpd_consents (
    id           UUID PRIMARY KEY,
    customer_id  UUID NOT NULL REFERENCES customers(id),
    purpose      VARCHAR(80) NOT NULL,         -- marketing, profiling, share_with_bank, ...
    granted      BOOLEAN NOT NULL,
    granted_at   TIMESTAMPTZ NOT NULL,
    revoked_at   TIMESTAMPTZ,
    legal_basis  VARCHAR(40) NOT NULL,         -- art.6.1.a..f RGPD
    evidence     JSONB                         -- IP, user-agent, form version
);

CREATE TABLE outbox_event (
    id UUID PRIMARY KEY, aggregate VARCHAR(64), aggregate_id UUID,
    type VARCHAR(120), payload JSONB, created_at TIMESTAMPTZ DEFAULT now(),
    published_at TIMESTAMPTZ
);

ALTER TABLE individual_profiles ENABLE ROW LEVEL SECURITY;
CREATE POLICY ip_tenant ON individual_profiles
   USING (EXISTS (SELECT 1 FROM customers c
                  WHERE c.id = individual_profiles.customer_id
                    AND c.tenant_id = current_setting('app.tenant_id')::uuid));
```

---

## 5. API REST (`/api/v1`)

Base: `http://customers:8083/api/v1`. JWT, Problem Details.

| Método | Path                                          | Roles                  |
|--------|-----------------------------------------------|------------------------|
| POST   | `/customers/individuals`                      | público (signup)       |
| POST   | `/customers/legal-entities`                   | ADMIN, AGENT           |
| POST   | `/customers/households`                       | CUSTOMER               |
| GET    | `/customers/{id}`                             | self, AGENT, ADMIN     |
| PATCH  | `/customers/{id}`                             | self, ADMIN            |
| DELETE | `/customers/{id}`                             | self (RGPD), ADMIN     |
| POST   | `/customers/{id}/household-members`           | self                   |
| POST   | `/customers/{id}/financial-snapshots`         | self                   |
| GET    | `/customers/{id}/financial-profile`           | self, BANK_OFFICER, AGENT |
| POST   | `/customers/{id}/documents`  (multipart)      | self                   |
| GET    | `/customers/{id}/documents`                   | self, AGENT            |
| POST   | `/customers/{id}/kyc/start`                   | self                   |
| POST   | `/customers/{id}/kyc/callback`                | SYSTEM (provider)      |
| GET    | `/customers/{id}/kyc`                         | self, BANK_OFFICER     |
| POST   | `/customers/{id}/search-preferences`          | self                   |
| GET    | `/customers/{id}/search-preferences`          | self                   |
| POST   | `/customers/{id}/consents`                    | self                   |
| GET    | `/customers/{id}/consents`                    | self                   |
| POST   | `/customers/{id}/rgpd/export`                 | self                   |
| POST   | `/customers/{id}/rgpd/erasure`                | self                   |
| GET    | `/customers/{id}/affordability?propertyId=`   | self                   |
| POST   | `/customers/{id}/share-with-bank`             | self                   |

### 5.1 `GET /customers/{id}/financial-profile` — DTO consumido por `banks`

```json
{
  "customerId": "0190c1...-uuid",
  "type": "INDIVIDUAL",
  "asOf": "2026-05-01",
  "borrower": {
    "age": 45,
    "contractType": "INDEFINITE",
    "seniorityMonths": 38,
    "sector": "IT",
    "netIncomeMonthly": 2585,
    "payments": 12,
    "otherDebtMonthly": 0,
    "dependents": 1,
    "ownFunds": 20000,
    "cirbeFlag": false,
    "hasGuarantor": false
  },
  "kyc": { "status": "VERIFIED", "score": 92, "expiresAt": "2027-05-01" },
  "consents": { "shareWithBank": true, "profiling": false },
  "computed": {
    "maxAffordablePaymentBdE": 1034.00,
    "maxAffordablePaymentInternal": 775.50,
    "targetTicketPrice": 180000,
    "savingsRunwayMonths": 7.7
  }
}
```

### 5.2 `GET /customers/{id}/affordability?propertyId=`

Compone con `products` (precio, gastos) + `banks` (productos disponibles) y devuelve **lista de
opciones financieras posibles**, ordenadas por `approvabilityScore`.

---

## 6. Eventos publicados

| Topic                                | Tipo                                              |
|--------------------------------------|---------------------------------------------------|
| `magenta.customers.profile.v1`       | `CustomerCreated`, `ProfileUpdated`, `FinancialSnapshotPublished` |
| `magenta.customers.kyc.v1`           | `KycVerified`, `KycRejected`, `KycExpired`        |
| `magenta.customers.preferences.v1`   | `SearchPreferenceCreated`, `SearchPreferenceDeleted` |
| `magenta.customers.rgpd.v1`          | `ConsentGranted`, `ConsentRevoked`, `ErasureRequested` |

Envelope CloudEvents + Avro. NO incluir PII en payload: solo IDs y campos no sensibles
(p. ej. `kycScore`, `incomeBand`). Si el consumidor necesita PII, llama API con su token.

---

## 7. Eventos consumidos

| Origen    | Topic                                  | Acción                                                   |
|-----------|----------------------------------------|----------------------------------------------------------|
| products  | `magenta.products.property.v1`         | Matching de búsquedas guardadas → alertas                |
| products  | `magenta.products.transaction.v1`      | Marcar histórico de adquisiciones del cliente            |
| banks     | `magenta.banks.preapproval.v1`         | Actualizar timeline del cliente, notificar               |
| areas     | `magenta.areas.zone.v1`                | Reescritura de `zone_id` deprecados                      |

---

## 8. Integraciones síncronas y externas

- **Keycloak Admin API**: alta/baja de usuarios, asignación de roles.
- **Proveedor KYC** (IDNow / Electronic IDentification / Onfido): SDK web + callback firmado HMAC.
- **OCR**: Tesseract self-hosted o AWS Textract (configurable) para nóminas e IRPF.
- **S3 / MinIO**: almacenamiento de documentos con cifrado server-side (KMS).
- **AEAT** (opcional, consentimiento): verificación de datos fiscales (servicio CSV).
- `areas` y `banks` vía RestClient + Resilience4j.

---

## 9. Reglas de cálculo de capacidad

```
maxPaymentBdE = max(0, netIncome * 0.40 - otherDebtMonthly)   // límite duro BdE
maxPaymentInternal = max(0, netIncome * 0.30 - otherDebtMonthly)   // política Magenta prudente

targetTicket = capitalForPayment(maxPaymentInternal, term=300, tin=3.5%) / LTV_assumed
```

Confidence:

```
confidence = 0.4 * (docsVerifiedScore)        // nóminas, IRPF, vida laboral
           + 0.3 * (kycVerified ? 1 : 0)
           + 0.2 * (hasBankStatements ? 1 : 0)
           + 0.1 * (dataCompletenessScore)
```

---

## 10. Frontend (libs/domain/customers)

- `SignupWizardComponent` (individual / household / company), validación NIF/CIF reactive.
- `FinancialProfilePage` con sliders y feedback en vivo (cuota máx., rango ticket).
- `DocumentsUploadComponent` con drag-and-drop, progreso, preview OCR.
- `KycFlowComponent` (iframe / SDK del proveedor).
- `SearchPreferencesEditor` con selector de zonas (consume `Areas.search`).
- `ConsentCenterComponent` (RGPD).
- NgRx feature `customerFeature` con selectores `selectMe`, `selectFinancialProfile`.

---

## 11. Seguridad y privacidad

- **PII cifrada a nivel campo** (NIF, email, teléfono, IDDoc) con envelope encryption (KEK en Vault).
- Hash determinista (HMAC-SHA256 + pepper) para columnas búsqueda única (`nif_hash`, `email_hash`).
- **RGPD**:
  - Endpoint `rgpd/export` genera JSON+PDF en S3 con URL firmada (24 h).
  - `rgpd/erasure` aplica **crypto-shredding** (rotación de DEK) + tombstone.
  - Logs anonimizados, sin PII en trazas.
- Consentimiento granular obligatorio para `share-with-bank`, `profiling`, `marketing`.
- Audit log inmutable de toda lectura de campos sensibles.
- MFA TOTP recomendado para clientes con financiero relleno.

---

## 12. Performance

- Caffeine local TTL 60 s para `getFinancialProfile(id)`.
- Búsqueda de matching offline (batch nocturno) escribe en `customer_matches` consumido por `products`.
- SLO: p95 `/financial-profile` < 120 ms; `/affordability` < 350 ms.

---

## 13. Configuración

```yaml
spring:
  application: { name: magenta-customers }
  datasource:
    url: jdbc:postgresql://postgres:5432/customers_db?currentSchema=customers
server: { port: 8083 }
magenta:
  pii:
    vault-key-name: customers-dek
  kyc:
    provider: IDNOW
    callback-secret: ${KYC_HMAC_SECRET}
  storage:
    s3-bucket: magenta-customers-docs
    kms-key-arn: ${S3_KMS_ARN}
  clients:
    areas:  http://areas:8081
    banks:  http://banks:8082
    products: http://products:8084
```

---

## 14. Testing

- Unit: validadores NIF (módulo 23), CIF (letra control), motor de capacidad.
- Testcontainers Postgres + LocalStack (S3, KMS) + Wiremock (KYC, banks, products).
- Pact: stubs `customers-stubs:1.x` consumidos por `banks` y `products`.
- Pruebas RGPD: export, erasure idempotente, no-recuperabilidad post crypto-shred.

---

## 15. Checklist

- [x] Migraciones Flyway con pgcrypto y RLS.
- [x] Servicio `PiiCrypto` con Vault transit.
- [x] `KycCallbackController` con verificación HMAC y replay-protection.
- [ ] Job `MatchEngine` nightly: cruza `search_preferences` × eventos `property.v1` recientes.
- [x] Endpoints RGPD export/erasure.
- [ ] OpenAPI spec publicada + stubs `customers-stubs:1.x` para consumidores.
- [x] Helm chart `charts/customers/` (deployment, service, hpa, pdb, networkpolicy).
