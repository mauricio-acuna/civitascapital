# Módulo `banks` — Especificación técnica (autocontenida)

> Plataforma **Magenta** · Bounded Context: **Entidades financieras y crédito**
> Stack: Java 21 · Spring Boot 3.3 · PostgreSQL 16 · Angular 17 · Kafka 3.7 · Keycloak 24
> Puerto local: **8082** · BD: `banks_db` · Esquema: `banks`
>
> Documento autocontenido. Contratos cross-cutting en `../ARCHITECTURE.md`.

---

## 1. Misión del módulo

Gestionar **entidades financieras**, sus **productos de crédito** y **operaciones** asociadas a
los flujos inmobiliarios de Magenta, con foco especial en:

- **Líneas preferentes 90 + 5 + 5** y **financiación al 100 %** (GoHipoteca, Hipoteca Joven
  Santander, MyInvestor, Open Bank, ImaginBank, BBVA Hipoteca Open, CaixaBank HipotecaNow,
  ING Hipoteca Naranja, Bankinter, EVO, Abanca, Cajamar, Kutxabank).
- **Simulación de cuota e interés** (TIN fijo, TIN variable Euríbor + diferencial, mixto).
- **Comprobación de elegibilidad** y **pre-aprobación** contra reglas internas + datos del cliente
  (módulo `customers`).
- **Calificación bancaria** (rating interno por entidad y por producto).
- **Tasaciones** vinculadas (ECO/805 2003) y su efecto en LTV.

Este módulo es **fuente de verdad** para cualquier dato bancario, condiciones, ratios y
pre-aprobaciones que el resto consuma.

---

## 2. Casos de uso principales

| ID    | Caso de uso                                                | Actor                  |
|-------|------------------------------------------------------------|------------------------|
| UC-B1 | Listar bancos activos y sus líneas preferentes             | Cualquiera             |
| UC-B2 | Buscar productos por % financiación / edad / ticket        | Customer, Agent        |
| UC-B3 | Simular cuota e ingresos requeridos para un inmueble       | Customer, Agent        |
| UC-B4 | Simular esquema 90 + 5 + 5 + IVA + AJD                     | Customer, Agent        |
| UC-B5 | Solicitar pre-aprobación (consume `Customer`)              | Customer               |
| UC-B6 | Marcar inmueble como "financiable" por X bancos            | Sistema                |
| UC-B7 | Comparar 2-N productos hipotecarios                        | Customer               |
| UC-B8 | Calcular TAE oficial (Circular BdE 5/2012)                 | Sistema                |
| UC-B9 | Publicar evento de cambio de condiciones de producto       | Sistema                |
| UC-B10| Registrar tasación y recalcular LTV                        | Bank Officer, System   |

---

## 3. Modelo de dominio

### 3.1 Aggregate `Bank`

```
Bank
 ├─ id, code (BIC), name, brand, country
 ├─ bdeRegistryNumber
 ├─ rating: enum (AAA..D)  // S&P/Moody's mapping
 ├─ logoUrl, websiteUrl
 ├─ active: Boolean
 └─ contactChannels: List<ContactChannel>
```

### 3.2 Aggregate `LoanProduct`

```
LoanProduct
 ├─ id, bankId, sku, name
 ├─ category: enum (MORTGAGE, BRIDGE, PERSONAL_FOR_DOWNPAYMENT, RENT_GUARANTEE_LOAN,
 │                  FIRST_HOME_AID, GREEN_RENOVATION)
 ├─ rateType: enum (FIXED, VARIABLE_EURIBOR, MIXED)
 ├─ tin: { initialPct, indexReference, marginPct, fixedYears? }
 ├─ ltvMaxPct, ltcMaxPct, ticketMin, ticketMax
 ├─ termMinMonths, termMaxMonths
 ├─ eligibility: EligibilityRules
 ├─ bundling: List<Cross-Sell> (seguro hogar, vida, nómina, plan pensiones, tarjeta)
 ├─ feeOpeningPct, feeStudyPct, feeEarlyRepaymentPct
 ├─ scheme: enum (STANDARD, NINETY_FIVE_FIVE)   // 90+5+5 marcado
 ├─ promoCode?: String                          // ej. GOHIPOTECA, HIPOTECA_JOVEN
 ├─ validFrom, validTo
 └─ status: enum (DRAFT, ACTIVE, DEPRECATED)
```

### 3.3 Aggregate `LoanSimulation`

```
LoanSimulation
 ├─ id, customerId?, productId
 ├─ requestedAmount, surfaceSqm?, propertyType, operationType, zoneId
 ├─ termMonths
 ├─ borrowerProfile: { netIncomeMonthly, age, contractType, otherDebtMonthly,
 │                     dependents, ownFunds, hasGuarantor }
 ├─ taxes: { ivaPct, ajdPct, itpPct } // calculado por CCAA via Areas
 ├─ result: SimulationResult
 │    ├─ monthlyPayment (TIN fijo / variable hipotético)
 │    ├─ tae (RD 309/2019 / Circular BdE 5/2012)
 │    ├─ totalCost, totalInterest
 │    ├─ effortRatio  // cuota / ingreso neto
 │    ├─ debtRatio    // (cuota + otra deuda) / ingreso neto
 │    ├─ requiredOwnFunds   // 5+5+IVA+AJD+notaría+gestoría
 │    ├─ approvabilityScore (0..100)
 │    └─ verdict: APPROVABLE | TIGHT | REJECTABLE
 └─ createdAt
```

### 3.4 Aggregate `Preapproval`

```
Preapproval
 ├─ id, customerId, productId, propertyId?
 ├─ amount, term, ltv
 ├─ status: enum (REQUESTED, IN_REVIEW, APPROVED, REJECTED, EXPIRED)
 ├─ conditions: List<Condition>  (avalista, vinculaciones, máximos)
 ├─ expiresAt
 └─ history: List<StatusChange>
```

### 3.5 Aggregate `Appraisal` (tasación)

```
Appraisal
 ├─ id, propertyId, customerId, providerId (sociedad tasadora)
 ├─ regulation: ECO_805_2003
 ├─ marketValue, mortgageValue, surfaceSqm
 ├─ issuedAt, validUntil (6 meses)
 ├─ pdfUrl (S3 signed)
 └─ usedInPreapprovalIds
```

### 3.6 Reglas de elegibilidad (`EligibilityRules`)

Expresión declarativa serializable en JSON:

```json
{
  "all": [
    { "field": "borrower.age", "op": "<=", "value": 67, "atTermEnd": true },
    { "field": "borrower.contractType", "op": "IN", "value": ["INDEFINITE","CIVIL_SERVANT"] },
    { "field": "result.effortRatio", "op": "<=", "value": 0.35 },
    { "field": "result.debtRatio",   "op": "<=", "value": 0.40 },
    { "field": "ltv",                "op": "<=", "value": 0.90 },
    { "field": "borrower.cirbeFlag", "op": "==", "value": false }
  ]
}
```

Evaluadas con motor propio (`RuleEngine`) puro Java, sin dependencias externas (testeable).

### 3.7 Invariantes

- `LoanProduct.ltvMaxPct ≤ 100`.
- `LoanProduct.scheme = NINETY_FIVE_FIVE` ⇒ `ltvMaxPct ≥ 90`.
- `Preapproval.status` solo avanza por transiciones permitidas en máquina de estados.
- TAE calculada con método estándar UE (Directiva 2014/17/UE Anexo I).

---

## 4. Esquema PostgreSQL (`banks`)

```sql
CREATE SCHEMA banks;
SET search_path TO banks;

CREATE TABLE banks (
    id              UUID PRIMARY KEY,
    tenant_id       UUID NOT NULL,
    code            VARCHAR(11) NOT NULL UNIQUE,    -- BIC
    name            VARCHAR(160) NOT NULL,
    brand           VARCHAR(80),
    country         CHAR(2) NOT NULL DEFAULT 'ES',
    bde_registry_nr VARCHAR(20),
    rating          VARCHAR(4),
    logo_url        TEXT,
    website_url     TEXT,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      VARCHAR(64) NOT NULL,
    updated_by      VARCHAR(64) NOT NULL,
    version         BIGINT NOT NULL DEFAULT 0,
    deleted_at      TIMESTAMPTZ
);

CREATE TABLE loan_products (
    id                       UUID PRIMARY KEY,
    tenant_id                UUID NOT NULL,
    bank_id                  UUID NOT NULL REFERENCES banks(id),
    sku                      VARCHAR(80) NOT NULL,
    name                     VARCHAR(160) NOT NULL,
    category                 VARCHAR(40) NOT NULL,
    rate_type                VARCHAR(20) NOT NULL,
    tin_initial_pct          NUMERIC(6,4) NOT NULL,
    tin_index_reference      VARCHAR(20),
    tin_margin_pct           NUMERIC(6,4),
    tin_fixed_years          INTEGER,
    ltv_max_pct              NUMERIC(5,2) NOT NULL,
    ltc_max_pct              NUMERIC(5,2),
    ticket_min               NUMERIC(14,2) NOT NULL,
    ticket_max               NUMERIC(14,2) NOT NULL,
    term_min_months          INTEGER NOT NULL,
    term_max_months          INTEGER NOT NULL,
    eligibility              JSONB NOT NULL,
    bundling                 JSONB NOT NULL DEFAULT '[]',
    fee_opening_pct          NUMERIC(5,3) DEFAULT 0,
    fee_study_pct            NUMERIC(5,3) DEFAULT 0,
    fee_early_repayment_pct  NUMERIC(5,3) DEFAULT 0,
    scheme                   VARCHAR(24) NOT NULL DEFAULT 'STANDARD',
    promo_code               VARCHAR(40),
    valid_from               DATE NOT NULL,
    valid_to                 DATE,
    status                   VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    version                  BIGINT NOT NULL DEFAULT 0,
    UNIQUE (bank_id, sku)
);
CREATE INDEX idx_lp_scheme        ON loan_products(scheme);
CREATE INDEX idx_lp_promo         ON loan_products(promo_code);
CREATE INDEX idx_lp_category      ON loan_products(category);
CREATE INDEX idx_lp_ltv           ON loan_products(ltv_max_pct);
CREATE INDEX idx_lp_eligibility   ON loan_products USING gin (eligibility);

CREATE TABLE loan_simulations (
    id                  UUID PRIMARY KEY,
    tenant_id           UUID NOT NULL,
    customer_id         UUID,
    product_id          UUID NOT NULL REFERENCES loan_products(id),
    property_id         UUID,
    zone_id             UUID,
    requested_amount    NUMERIC(14,2) NOT NULL,
    term_months         INTEGER NOT NULL,
    borrower            JSONB NOT NULL,
    taxes               JSONB NOT NULL,
    result              JSONB NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_sim_customer ON loan_simulations(customer_id, created_at DESC);

CREATE TABLE preapprovals (
    id              UUID PRIMARY KEY,
    tenant_id       UUID NOT NULL,
    customer_id     UUID NOT NULL,
    product_id      UUID NOT NULL REFERENCES loan_products(id),
    property_id     UUID,
    amount          NUMERIC(14,2) NOT NULL,
    term_months     INTEGER NOT NULL,
    ltv             NUMERIC(5,2) NOT NULL,
    status          VARCHAR(16) NOT NULL,
    conditions      JSONB NOT NULL DEFAULT '[]',
    expires_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    version         BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_preap_customer ON preapprovals(customer_id);
CREATE INDEX idx_preap_status   ON preapprovals(status);

CREATE TABLE preapproval_events (
    id             UUID PRIMARY KEY,
    preapproval_id UUID NOT NULL REFERENCES preapprovals(id),
    from_status    VARCHAR(16),
    to_status      VARCHAR(16) NOT NULL,
    reason         TEXT,
    actor          VARCHAR(64) NOT NULL,
    at             TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE appraisals (
    id              UUID PRIMARY KEY,
    tenant_id       UUID NOT NULL,
    property_id     UUID NOT NULL,
    customer_id     UUID,
    provider_id     UUID NOT NULL,
    regulation      VARCHAR(20) NOT NULL DEFAULT 'ECO_805_2003',
    market_value    NUMERIC(14,2) NOT NULL,
    mortgage_value  NUMERIC(14,2) NOT NULL,
    surface_sqm     NUMERIC(8,2) NOT NULL,
    issued_at       DATE NOT NULL,
    valid_until     DATE NOT NULL,
    pdf_url         TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE euribor_rates (
    period          DATE PRIMARY KEY,
    rate_12m_pct    NUMERIC(6,4) NOT NULL,
    source          VARCHAR(20) NOT NULL DEFAULT 'EMMI'
);

CREATE TABLE outbox_event (
    id UUID PRIMARY KEY, aggregate VARCHAR(64), aggregate_id UUID,
    type VARCHAR(120), payload JSONB, created_at TIMESTAMPTZ DEFAULT now(),
    published_at TIMESTAMPTZ
);

ALTER TABLE preapprovals ENABLE ROW LEVEL SECURITY;
CREATE POLICY preap_tenant ON preapprovals
    USING (tenant_id = current_setting('app.tenant_id')::uuid);
```

---

## 5. API REST (`/api/v1`)

Base: `http://banks:8082/api/v1`. JWT, Problem Details, paginación estándar.

| Método | Path                                                     | Roles                   |
|--------|----------------------------------------------------------|-------------------------|
| GET    | `/banks`                                                 | público                 |
| GET    | `/banks/{id}`                                            | público                 |
| GET    | `/banks/{id}/products`                                   | público                 |
| GET    | `/products?scheme=NINETY_FIVE_FIVE&ltvMin=90&age=...`    | público                 |
| GET    | `/products/{id}`                                         | público                 |
| POST   | `/products`                                              | ADMIN, BANK_OFFICER     |
| PUT    | `/products/{id}`                                         | ADMIN, BANK_OFFICER     |
| POST   | `/simulations`                                           | público (rate-limited)  |
| GET    | `/simulations/{id}`                                      | propietario / agent     |
| POST   | `/simulations/compare`                                   | público                 |
| POST   | `/preapprovals`                                          | CUSTOMER                |
| GET    | `/preapprovals/{id}`                                     | CUSTOMER, BANK_OFFICER  |
| PATCH  | `/preapprovals/{id}`                                     | BANK_OFFICER, SYSTEM    |
| GET    | `/preapprovals?customerId=...`                           | CUSTOMER, AGENT         |
| POST   | `/appraisals`                                            | BANK_OFFICER            |
| GET    | `/appraisals?propertyId=...`                             | CUSTOMER, AGENT, BANK_OFFICER |
| GET    | `/indices/euribor?from=&to=`                             | público                 |
| GET    | `/financing-feasibility?propertyId=&customerId=`         | CUSTOMER, AGENT         |

### 5.1 Request — `POST /simulations`

```json
{
  "productId": "0190b1...-uuid",
  "propertyId": "0190b2...-uuid",
  "requestedAmount": 159300,
  "propertyPrice": 177000,
  "surfaceSqm": 65,
  "propertyType": "FLAT",
  "operationType": "SALE",
  "zoneId": "0190af11-...",
  "termMonths": 360,
  "borrower": {
    "netIncomeMonthly": 2585,
    "payments": 12,
    "age": 45,
    "contractType": "INDEFINITE",
    "seniorityMonths": 38,
    "otherDebtMonthly": 0,
    "dependents": 1,
    "ownFunds": 20000,
    "hasGuarantor": false
  }
}
```

### 5.2 Response

```json
{
  "id": "0190b3...-uuid",
  "result": {
    "monthlyPayment": 715.42,
    "tae": 4.12,
    "tinApplied": 3.5,
    "totalCost": 257550,
    "totalInterest": 98250,
    "effortRatio": 0.2768,
    "debtRatio": 0.2768,
    "ltvComputed": 0.90,
    "taxes": { "ivaPct": 10.0, "ajdPct": 1.5, "itpPct": null },
    "requiredOwnFunds": 38055,
    "fundsGap": 18055,
    "approvabilityScore": 68,
    "verdict": "TIGHT",
    "warnings": [
      "Fondos propios insuficientes para escritura (faltan 18.055 €).",
      "Sin Hipoteca Joven: edad supera 35 años."
    ],
    "alternatives": [
      { "productId": "0190b4-...", "name": "Hipoteca Open BBVA", "ltvMax": 80, "fit": 0.61 }
    ]
  }
}
```

### 5.3 Endpoint clave — `GET /financing-feasibility`

Compone: producto + cliente + tasación + datos de zona (Areas). Devuelve lista de bancos
con verdict por cada uno. Lo consumen `products` y `servicios` para badges y workflows.

---

## 6. Eventos publicados

| Topic                              | Tipo                                          | Trigger                          |
|------------------------------------|-----------------------------------------------|----------------------------------|
| `magenta.banks.product.v1`         | `ProductPublished`, `ProductUpdated`, `ProductDeprecated` | CRUD producto       |
| `magenta.banks.preapproval.v1`     | `PreapprovalRequested`, `PreapprovalApproved`, `PreapprovalRejected`, `PreapprovalExpired` | máquina de estados |
| `magenta.banks.appraisal.v1`       | `AppraisalIssued`                             | POST `/appraisals`               |
| `magenta.banks.simulation.v1`      | `SimulationCreated` (sólo analítica)          | POST `/simulations`              |

Schema Avro `PreapprovalEvent` con `customerId`, `productId`, `propertyId`, `amount`, `ltv`,
`status`, `expiresAt`, `tenantId`.

---

## 7. Eventos consumidos

| Origen     | Topic                                  | Acción                                                       |
|------------|----------------------------------------|--------------------------------------------------------------|
| customers  | `magenta.customers.profile.v1`         | Actualizar caché local de perfil para simulaciones rápidas   |
| customers  | `magenta.customers.kyc.v1`             | Habilitar/inhabilitar pre-aprobaciones según KYC             |
| products   | `magenta.products.property.v1`         | Calcular y publicar feasibility (badge "financiable")        |
| products   | `magenta.products.transaction.v1`      | Cerrar pre-aprobaciones asociadas; liberar tasación si caducó |
| areas      | `magenta.areas.zone.v1`                | Cache local mínima para tipos impositivos por CCAA           |
| areas      | `magenta.areas.price-index.v1`         | Actualizar valor de mercado de referencia para LTV           |

---

## 8. Integraciones síncronas (outbound)

- `customers` → `GET /api/v1/customers/{id}/financial-profile` (RestClient + Resilience4j).
- `products` → `GET /api/v1/properties/{id}` (precio, superficie, tipo, zoneId).
- `areas`    → `GET /api/v1/zones/{id}` (CCAA → tipos AJD/ITP).
- **Euríbor**: feed nocturno EMMI (job cron) + fallback Banco de España (XML).
- **CIRBE**: integración opcional con scraper autorizado (sólo entornos prod con consentimiento).

---

## 9. Reglas de cálculo

### 9.1 Cuota (sistema francés)

$$ c = P \cdot \frac{i}{1 - (1+i)^{-n}} $$

con `i = TIN/12`, `n = termMonths`. Para `VARIABLE_EURIBOR` se simulan dos escenarios:
TIN inicial vigente y TIN estresado (`indexReference + margin + 100 bps`).

### 9.2 TAE

Resolver iterativamente (Newton-Raphson) la ecuación de equivalencia financiera
RD 309/2019, incluyendo: comisiones, seguros vinculados, periodo carencia, primer pago.

### 9.3 Fondos propios requeridos (esquema 90 + 5 + 5)

```
ownFundsRequired = price * (1 - LTV)        // 10 %
                 + price * 0.05              // aplazado promotor
                 + price * tax.iva           // 10 % obra nueva
                 + price * tax.ajd           // 1.5 % CCAA Catalunya
                 + price * 0.012             // notaría + registro + gestoría (aprox.)
```

Para 2ª mano: cambiar `iva + ajd` por `itp` (variable por CCAA, ej. 10 % Catalunya, 7 % Madrid).

### 9.4 Approvability score

Composición lineal ponderada (suma 100):

| Factor             | Peso |
|--------------------|------|
| effortRatio        | 30   |
| debtRatio          | 20   |
| ownFunds gap       | 20   |
| Contract type      | 10   |
| Seniority          | 5    |
| Age vs term end    | 5    |
| Dependents         | 5    |
| CIRBE clean        | 5    |

Verdict:
- ≥ 75 → `APPROVABLE`
- 50-74 → `TIGHT`
- < 50 → `REJECTABLE`

### 9.5 Máquina de estados `Preapproval`

```
REQUESTED ──> IN_REVIEW ──> APPROVED ──> EXPIRED (auto a 90 d)
   │             │
   │             └──> REJECTED
   └──> REJECTED (validación previa)
```

Transiciones registradas en `preapproval_events`.

---

## 10. Frontend (libs/domain/banks)

- `LoanSimulatorComponent` (reactive forms + signals), preserva estado en NgRx.
- `MortgageComparatorComponent` (cards lado a lado).
- `Ninety5FiveBadgeComponent` (icono cuando `scheme = NINETY_FIVE_FIVE`).
- `PreapprovalWizardComponent` (stepper 4 pasos).
- Servicio `BanksApi` generado de OpenAPI.

---

## 11. Seguridad

- `POST /preapprovals` requiere `ROLE_CUSTOMER` y `customer_id` del token == path/body.
- Datos sensibles (`borrower.netIncome`, `cirbeFlag`) cifrados con **AES-256-GCM** (Spring Cloud Vault).
- Rate limit Bucket4j: `/simulations` 30 rpm/IP, 300 rpm/usuario.
- Auditoría inmutable en tabla `preapproval_events` (append-only; trigger BEFORE UPDATE → RAISE).

---

## 12. Performance

- Cache Caffeine de productos por id (TTL 30 min).
- Cache Redis de simulaciones determinísticas por hash(request) (TTL 1 h).
- Índices GIN sobre `eligibility` jsonb para filtros declarativos.
- SLO: p95 `/simulations` < 250 ms; `/financing-feasibility` < 400 ms.

---

## 13. Configuración (`application.yml`)

```yaml
spring:
  application: { name: magenta-banks }
  datasource:
    url: jdbc:postgresql://postgres:5432/banks_db?currentSchema=banks
  flyway: { schemas: banks }
server: { port: 8082 }
magenta:
  euribor:
    feed-url: https://www.emmi-benchmarks.eu/api/euribor
    cron: "0 30 6 * * MON-FRI"
  clients:
    customers: http://customers:8083
    products:  http://products:8084
    areas:     http://areas:8081
  resilience4j:
    circuitbreaker.instances.customers.failureRateThreshold: 50
```

---

## 14. Testing

- Unit: motor de reglas, cálculo de cuota, TAE (golden file con casos del Banco de España).
- Testcontainers Postgres + Kafka + Wiremock (customers, products, areas).
- Contract tests: stubs `banks-stubs:1.x` consumidos por `products`, `customers`, `servicios`.
- k6: `/simulations` 1000 rps p95 < 300 ms.

---

## 15. Checklist de implementación

- [ ] Catálogo seed con los 13 bancos mencionados y sus productos preferentes activos.
- [ ] Cargador semilla `landco_santander_90_5_5.sql` con SKU específico.
- [ ] `RuleEngine` evaluador de `eligibility` JSON.
- [ ] Servicio `TaeCalculator` con tests dorados.
- [ ] Job `EuriborFetcher` con fallback BdE.
- [ ] Outbox + producer Kafka.
- [ ] Helm chart `charts/banks/`.
- [ ] Dashboard Grafana `banks-preapprovals.json`.
