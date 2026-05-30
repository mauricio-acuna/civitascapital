# Módulo `servicios` — Especificación técnica (autocontenida)

> Plataforma **Magenta** · Bounded Context: **Servicios añadidos y workflows**
> Stack: Java 21 · Spring Boot 3.3 · PostgreSQL 16 · Angular 17 · Kafka 3.7 · Camunda 8 (Zeebe)
> Puerto local: **8085** · BD: `services_db` · Esquema: `services`
>
> Documento autocontenido. Contratos cross-cutting en `../ARCHITECTURE.md`.

---

## 1. Misión del módulo

Catálogo y **orquestación** de todos los servicios añadidos que rodean la operación inmobiliaria.
Es el módulo que **compone** los demás (`areas`, `banks`, `customers`, `products`) para entregar
experiencias completas al cliente final mediante **workflows** versionados.

Servicios cubiertos (catálogo inicial):

| Código             | Descripción                                                                 |
|--------------------|------------------------------------------------------------------------------|
| `FIRST_HOME_AID`   | Ayuda integral a primer comprador (incluye 90+5+5, ICO, ayudas autonómicas) |
| `MORTGAGE_BROKER`  | Mediación hipotecaria multi-banco                                            |
| `BRIDGE_LOAN`      | Crédito puente para compra antes de venta                                    |
| `RENT_DEFAULT_INSURANCE` | Seguro de impago de alquiler (Mapfre, AXA, Mutua de Propietarios, ARAG)|
| `HOME_INSURANCE`   | Seguro de hogar (vinculable a hipoteca)                                      |
| `LIFE_INSURANCE`   | Seguro de vida (vinculable a hipoteca)                                       |
| `RENT_GUARANTEE`   | Aval bancario o fianza para alquiler                                         |
| `APPRAISAL`        | Tasación ECO 805/2003                                                        |
| `PROFILE_SEARCH`   | Búsqueda profesional de perfil inquilino                                     |
| `PROPERTY_SEARCH`  | Personal shopper inmobiliario                                                |
| `LEGAL_REVIEW`     | Revisión jurídica de contratos, nota simple, cargas                          |
| `TECHNICAL_REPORT` | ITE / informe técnico / certificado energético                               |
| `RENOVATION_QUOTE` | Presupuesto de reforma con partners                                          |
| `MOVING`           | Mudanza con socios certificados                                              |
| `UTILITIES_SETUP`  | Alta de luz, agua, gas, internet                                             |
| `TAX_ADVISORY`     | Asesoría fiscal de la operación (IRPF, plusvalía, ITP/IVA)                   |
| `NOTARY_GESTORIA`  | Coordinación notaría + gestoría                                              |

Este módulo NO duplica fuentes de verdad: orquesta y compone.

---

## 2. Casos de uso principales

| ID    | Caso de uso                                                          | Actor                  |
|-------|----------------------------------------------------------------------|------------------------|
| UC-S1 | Listar catálogo de servicios disponibles                             | público                |
| UC-S2 | Cotizar un servicio (precio, plazos)                                 | público                |
| UC-S3 | Contratar servicio (genera workflow Camunda)                         | CUSTOMER               |
| UC-S4 | Avanzar/abandonar workflow (tareas humanas y automáticas)            | CUSTOMER, AGENT, SYSTEM|
| UC-S5 | Comparar paquetes (ej. "First Home Aid Premium" vs "Standard")       | CUSTOMER               |
| UC-S6 | Recibir alertas/reminders del workflow                               | CUSTOMER               |
| UC-S7 | Renovar/cancelar póliza de seguro                                    | CUSTOMER               |
| UC-S8 | Búsqueda profesional de inquilino → entrega de shortlist             | OWNER, AGENT           |
| UC-S9 | Liquidación / facturación a partners (Stripe Connect / SEPA B2B)     | SYSTEM, ADMIN          |
| UC-S10| Dashboard de SLA y satisfacción por proveedor                        | ADMIN                  |

---

## 3. Modelo de dominio

### 3.1 Aggregate `ServiceDefinition` (catálogo)

```
ServiceDefinition
 ├─ id, code (ej. "RENT_DEFAULT_INSURANCE")
 ├─ name, description, category
 ├─ pricingModel: enum (FIXED, PERCENT_OF_PRICE, MONTHLY_SUBSCRIPTION, QUOTE_BASED)
 ├─ basePrice: Money?
 ├─ priceFormula: String?    // JSONata / SpEL  ej. "0.0035 * property.price"
 ├─ slaHours: Integer        // tiempo de respuesta comprometido
 ├─ partners: List<PartnerRef>  // proveedores activos
 ├─ workflowKey: String      // BPMN deployado en Camunda
 ├─ inputs: JSONSchema       // qué necesita para activarse
 ├─ outputs: JSONSchema      // qué entrega al cerrar
 ├─ requiresKyc: Boolean
 ├─ status: enum (ACTIVE, DEPRECATED)
 └─ valid for: Set<OperationType>  // SALE, RENT, ...
```

### 3.2 Aggregate `ServiceOrder` (instancia)

```
ServiceOrder
 ├─ id, serviceCode, customerId, propertyId?, operationId?, bankProductId?
 ├─ inputs: JSONB             // datos rellenados por el cliente
 ├─ priceQuoted: Money
 ├─ priceFinal: Money?
 ├─ status: enum (DRAFT, QUOTED, ACCEPTED, IN_PROGRESS, COMPLETED, CANCELLED, FAILED)
 ├─ workflowInstanceId: String  // Camunda
 ├─ partnerId?                   // asignado en runtime
 ├─ slaDueAt
 ├─ deliverables: List<Deliverable>
 ├─ payments: List<Payment>
 ├─ history: List<StatusChange>
 └─ createdAt, completedAt
```

### 3.3 Entidades de soporte

```
Partner
 ├─ id, code, name, kind (INSURER, BROKER, NOTARY, GESTORIA, APPRAISER, LAWYER, MOVER, UTILITY)
 ├─ services: Set<ServiceCode>
 ├─ coverageZoneIds: Set<UUID>   // áreas donde opera (ref Areas)
 ├─ commissionPct
 ├─ rating, npsScore
 ├─ active
 └─ contractRef

Deliverable
 ├─ id, orderId
 ├─ kind (DOCUMENT, REPORT, POLICY, INVOICE, SHORTLIST)
 ├─ storageUri, sha256, signedBy
 └─ issuedAt

Payment
 ├─ id, orderId
 ├─ direction: enum (INBOUND, OUTBOUND)    // cobro a cliente / pago a partner
 ├─ amount, currency, method (CARD, SEPA, WALLET)
 ├─ providerRef (Stripe pi_..., charge_...)
 ├─ status (PENDING, AUTHORIZED, CAPTURED, REFUNDED, FAILED)
 ├─ vatPct, invoiceNumber
 └─ at

ProfileSearchBrief         // específico de PROFILE_SEARCH
 ├─ orderId
 ├─ minIncomeMultiple (ej. 3x renta)
 ├─ contractType, maxDependents
 ├─ requiresGuarantor, petsAllowed
 ├─ targetMoveInDate
 └─ shortlist: List<CandidateRef>  (ref Customer ids con consentimiento)

PropertySearchBrief        // específico de PROPERTY_SEARCH (personal shopper)
 ├─ orderId
 ├─ targetTicket, zoneIds, propertyTypes, mustHaves, niceToHaves
 ├─ deadline
 └─ shortlist: List<PropertyRef>
```

### 3.4 Invariantes

- `ServiceOrder.status = ACCEPTED` requiere consentimiento RGPD `share-with-partner`.
- `RENT_DEFAULT_INSURANCE` exige `propertyId` con operación `RENT` activa y KYC del inquilino candidato.
- Workflows con tareas humanas tienen reasignación automática si SLA vence (escalado).
- `Payment.direction = OUTBOUND` requiere `Partner` activo con datos SEPA verificados.

---

## 4. Esquema PostgreSQL (`services`)

```sql
CREATE SCHEMA services;
SET search_path TO services;

CREATE TABLE service_definitions (
    id              UUID PRIMARY KEY,
    code            VARCHAR(40) NOT NULL UNIQUE,
    name            VARCHAR(160) NOT NULL,
    description     TEXT NOT NULL,
    category        VARCHAR(40) NOT NULL,
    pricing_model   VARCHAR(24) NOT NULL,
    base_price      NUMERIC(14,2),
    price_formula   TEXT,
    sla_hours       INTEGER NOT NULL DEFAULT 72,
    workflow_key    VARCHAR(80) NOT NULL,
    inputs_schema   JSONB NOT NULL,
    outputs_schema  JSONB NOT NULL,
    requires_kyc    BOOLEAN NOT NULL DEFAULT FALSE,
    valid_for       TEXT[] NOT NULL DEFAULT '{}',
    status          VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    version         BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE partners (
    id                  UUID PRIMARY KEY,
    tenant_id           UUID NOT NULL,
    code                VARCHAR(40) NOT NULL UNIQUE,
    name                VARCHAR(160) NOT NULL,
    kind                VARCHAR(20) NOT NULL,
    services            TEXT[] NOT NULL DEFAULT '{}',
    coverage_zone_ids   UUID[] NOT NULL DEFAULT '{}',
    commission_pct      NUMERIC(5,2),
    rating              NUMERIC(3,2),
    nps_score           SMALLINT,
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    contract_ref        VARCHAR(120),
    sepa_iban_enc       BYTEA,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_partner_kind     ON partners(kind);
CREATE INDEX idx_partner_services ON partners USING gin (services);
CREATE INDEX idx_partner_zones    ON partners USING gin (coverage_zone_ids);

CREATE TABLE service_orders (
    id                  UUID PRIMARY KEY,
    tenant_id           UUID NOT NULL,
    service_code        VARCHAR(40) NOT NULL,
    customer_id         UUID NOT NULL,
    property_id         UUID,
    operation_id        UUID,
    bank_product_id     UUID,
    inputs              JSONB NOT NULL,
    price_quoted        NUMERIC(14,2) NOT NULL,
    price_final         NUMERIC(14,2),
    currency            CHAR(3) NOT NULL DEFAULT 'EUR',
    status              VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
    workflow_instance_id VARCHAR(80),
    partner_id          UUID REFERENCES partners(id),
    sla_due_at          TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at        TIMESTAMPTZ,
    version             BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_so_customer ON service_orders(customer_id);
CREATE INDEX idx_so_status   ON service_orders(status, created_at DESC);
CREATE INDEX idx_so_workflow ON service_orders(workflow_instance_id);

CREATE TABLE service_order_events (
    id          UUID PRIMARY KEY,
    order_id    UUID NOT NULL REFERENCES service_orders(id),
    from_status VARCHAR(16),
    to_status   VARCHAR(16) NOT NULL,
    reason      TEXT,
    actor       VARCHAR(64) NOT NULL,
    payload     JSONB,
    at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE deliverables (
    id          UUID PRIMARY KEY,
    order_id    UUID NOT NULL REFERENCES service_orders(id),
    kind        VARCHAR(24) NOT NULL,
    storage_uri TEXT NOT NULL,
    sha256      CHAR(64) NOT NULL,
    signed_by   VARCHAR(160),
    issued_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE payments (
    id              UUID PRIMARY KEY,
    order_id        UUID NOT NULL REFERENCES service_orders(id),
    direction       VARCHAR(10) NOT NULL,
    amount          NUMERIC(14,2) NOT NULL,
    currency        CHAR(3) NOT NULL DEFAULT 'EUR',
    method          VARCHAR(16) NOT NULL,
    provider_ref    VARCHAR(120),
    status          VARCHAR(16) NOT NULL,
    vat_pct         NUMERIC(5,2) NOT NULL DEFAULT 21,
    invoice_number  VARCHAR(40),
    at              TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE profile_search_briefs (
    order_id              UUID PRIMARY KEY REFERENCES service_orders(id),
    min_income_multiple   NUMERIC(4,2) NOT NULL,
    contract_type         VARCHAR(20),
    max_dependents        SMALLINT,
    requires_guarantor    BOOLEAN,
    pets_allowed          BOOLEAN,
    target_move_in_date   DATE,
    shortlist             JSONB NOT NULL DEFAULT '[]'
);

CREATE TABLE property_search_briefs (
    order_id        UUID PRIMARY KEY REFERENCES service_orders(id),
    target_ticket   NUMERIC(14,2) NOT NULL,
    zone_ids        UUID[] NOT NULL DEFAULT '{}',
    property_types  TEXT[] NOT NULL DEFAULT '{}',
    must_haves      JSONB NOT NULL DEFAULT '[]',
    nice_to_haves   JSONB NOT NULL DEFAULT '[]',
    deadline        DATE,
    shortlist       JSONB NOT NULL DEFAULT '[]'
);

CREATE TABLE outbox_event (
    id UUID PRIMARY KEY, aggregate VARCHAR(64), aggregate_id UUID,
    type VARCHAR(120), payload JSONB, created_at TIMESTAMPTZ DEFAULT now(),
    published_at TIMESTAMPTZ
);

ALTER TABLE service_orders ENABLE ROW LEVEL SECURITY;
CREATE POLICY so_tenant ON service_orders
   USING (tenant_id = current_setting('app.tenant_id')::uuid);
```

---

## 5. Workflow engine — Camunda 8 / Zeebe

- Cada `ServiceDefinition.workflowKey` referencia un proceso BPMN deployado en Camunda 8.
- Procesos canónicos (carpeta `bpmn/`):
  - `first-home-aid.bpmn`
  - `mortgage-broker.bpmn`
  - `rent-default-insurance.bpmn`
  - `appraisal.bpmn`
  - `profile-search.bpmn`
  - `property-search.bpmn`
  - `notary-gestoria.bpmn`
- **Job workers** (Spring `@JobWorker`) implementan:
  `validateInputs`, `quotePrice`, `requestPreapproval` (a `banks`), `assignPartner`,
  `chargeCustomer` (Stripe), `payoutPartner` (SEPA via Stripe Connect), `notifyCustomer`,
  `generateInvoice`, `verifyDeliverable`, `closeOrder`.
- Tareas humanas (User Tasks) expuestas vía Camunda Tasklist y embebidas en backoffice.
- **Compensación** modelada (saga) para rollback de pagos cuando un proveedor cancela.

Ejemplo de `first-home-aid.bpmn` (conceptual):

```
Start
  └─ Service: collectCustomerProfile  (GET customers/{id}/financial-profile)
  └─ Service: collectPropertyData     (GET products/{id})
  └─ Service: getFinancingFeasibility (GET banks/financing-feasibility)
  └─ Gateway: ¿has90_5_5?
      ├─ Yes → Service: createPreapproval (POST banks/preapprovals)
      │        └─ User: clientReviewOffer  (Tasklist)
      │        └─ Service: scheduleAppraisal
      │        └─ Service: bundleInsurance (home + life)
      │        └─ Service: notaryGestoriaSubprocess
      │        └─ End: success
      └─ No  → Service: suggestAlternatives → End: alternative-path
```

---

## 6. API REST (`/api/v1`)

| Método | Path                                                | Roles                  |
|--------|-----------------------------------------------------|------------------------|
| GET    | `/catalog`                                          | público                |
| GET    | `/catalog/{code}`                                   | público                |
| POST   | `/catalog/{code}/quote`                             | público (rate-limited) |
| POST   | `/orders`                                           | CUSTOMER               |
| GET    | `/orders/{id}`                                      | self, AGENT, ADMIN     |
| PATCH  | `/orders/{id}` (cancelar, actualizar inputs)        | self                   |
| POST   | `/orders/{id}/accept`                               | self                   |
| GET    | `/orders?customerId=&status=`                       | self, AGENT, ADMIN     |
| GET    | `/orders/{id}/timeline`                             | self                   |
| GET    | `/orders/{id}/deliverables`                         | self                   |
| GET    | `/orders/{id}/payments`                             | self                   |
| POST   | `/partners`                                         | ADMIN                  |
| GET    | `/partners?service=&zoneId=`                        | ADMIN                  |
| PATCH  | `/partners/{id}`                                    | ADMIN                  |
| POST   | `/webhooks/stripe`                                  | público (firma)        |
| POST   | `/webhooks/partners/{partnerCode}`                  | público (firma)        |
| GET    | `/sla-dashboard`                                    | ADMIN                  |

### 6.1 `POST /catalog/{code}/quote`

```json
// request
{
  "customerId": "uuid",
  "propertyId": "uuid",
  "operationId": "uuid",
  "extra": { "monthsCoverage": 12, "fraudClause": true }
}

// response
{
  "serviceCode": "RENT_DEFAULT_INSURANCE",
  "priceQuoted": 320.00,
  "currency": "EUR",
  "validUntil": "2026-06-29T00:00:00Z",
  "breakdown": [
    { "concept": "prima base (3.5% renta anual)", "amount": 252.00 },
    { "concept": "cobertura impago 12 meses", "amount": 50.00 },
    { "concept": "cláusula antiokupación", "amount": 18.00 }
  ],
  "slaHours": 48
}
```

### 6.2 `POST /orders`

Crea `DRAFT`, llama a `quote` interno, transiciona a `QUOTED`. El cliente confirma con `accept`,
arrancando el workflow Camunda.

---

## 7. Eventos publicados

| Topic                              | Tipo                                                              |
|------------------------------------|--------------------------------------------------------------------|
| `magenta.servicios.workflow.v1`    | `OrderCreated`, `OrderAccepted`, `OrderInProgress`, `OrderCompleted`, `OrderCancelled`, `OrderFailed` |
| `magenta.servicios.deliverable.v1` | `DeliverableIssued`                                                |
| `magenta.servicios.payment.v1`     | `PaymentCaptured`, `PayoutSent`, `PaymentRefunded`                 |
| `magenta.servicios.sla.v1`         | `SlaBreached`                                                      |

---

## 8. Eventos consumidos

| Origen     | Topic                              | Acción                                                            |
|------------|------------------------------------|--------------------------------------------------------------------|
| banks      | `magenta.banks.preapproval.v1`     | Avanza workflow `mortgage-broker` / `first-home-aid`               |
| banks      | `magenta.banks.appraisal.v1`       | Avanza workflow `appraisal`                                        |
| products   | `magenta.products.property.v1`     | Activa ofertas de servicios (cross-sell) por matching de zona/tipo |
| products   | `magenta.products.transaction.v1`  | Dispara `notary-gestoria` + `utilities-setup` automáticamente      |
| customers  | `magenta.customers.kyc.v1`         | Desbloquea servicios que `requiresKyc=true`                        |
| customers  | `magenta.customers.preferences.v1` | Refresca briefs activos de `property-search`                       |
| areas      | `magenta.areas.zone.v1`            | Reasigna partners cuando coverage cambia                           |

---

## 9. Integraciones síncronas y externas

- `customers`, `products`, `banks`, `areas` (RestClient + Resilience4j).
- **Stripe / Stripe Connect** para cobros y pagos a partners.
- **Aseguradoras** (Mapfre, AXA, Mutua, ARAG): API REST o sftp+xml según partner.
- **Notarías**: integración ANCERT / cita previa.
- **Email/SMS/Push**: SendGrid / Twilio / Firebase via servicio común `notification-service`.
- **eSignature**: Signaturit / DocuSign para firmas remotas de pólizas y mandatos.

---

## 10. Reglas y políticas

### 10.1 Asignación de partner

1. Filtrar partners activos con `services CONTAINS serviceCode`.
2. Filtrar por `coverage_zone_ids` que contenga `property.zoneId` (o ancestro).
3. Ordenar por: `slaCompliance30d desc`, `npsScore desc`, `commissionPct asc` (con pesos
   configurables en `magenta.partners.weights`).
4. Round-robin con weighting para evitar starvation.

### 10.2 SLA y escalado

- Job `@Scheduled(fixedRate=60s)` revisa `service_orders` con `sla_due_at < now()` y status no final.
- Acciones: reasignar, notificar admin, abrir incidencia (PagerDuty), publicar `SlaBreached`.

### 10.3 Facturación

- `Payment.direction=INBOUND` → emite factura simplificada (B2C) o completa (B2B) con
  numeración correlativa por serie y año. Generador PDF (OpenPDF) firmado con XAdES.
- `Payment.direction=OUTBOUND` → consolidado mensual SEPA B2B (XML pain.001) al partner.

---

## 11. Frontend (libs/domain/servicios)

- `ServicesCatalogPage` filtrable por categoría/operación.
- `ServiceQuoteWizard` adaptativo según `inputs_schema` (JSON Schema → form).
- `OrderTimelineComponent` mostrando workflow Camunda (steps, tareas humanas pendientes).
- `OrderDeliverablesComponent` (descarga firmada, verificación SHA256).
- `PaymentCheckoutComponent` (Stripe Elements).
- `PartnerBackofficeApp` (subapp en `web-backoffice`) para gestión de tareas humanas.
- `SlaDashboardComponent` con métricas en vivo (websocket / SSE).

---

## 12. Seguridad

- Webhooks (Stripe, partners) con verificación HMAC y replay-protection (jti + ventana 5 min).
- Acceso a `Deliverable.storageUri` siempre via URL firmada (TTL 5 min) y log auditable.
- Datos sensibles del partner (IBAN) cifrados con Vault.
- Roles refinados: `ROLE_PARTNER_OPERATOR` limitado a sus propios `service_orders`.

---

## 13. Performance & escalabilidad

- Camunda 8 cluster con 3 brokers, 1 partition por proceso pesado.
- HPA basado en backlog de jobs Zeebe + CPU.
- SLO: `/quote` p95 < 250 ms; `/orders` p95 < 350 ms; e2e workflow simple (`appraisal`) < 24 h
  laborables p90.
- Caché Redis para `catalog` (TTL 10 min) y `quote` determinístico (TTL 15 min).

---

## 14. Configuración

```yaml
spring:
  application: { name: magenta-servicios }
  datasource:
    url: jdbc:postgresql://postgres:5432/services_db?currentSchema=services
server: { port: 8085 }
zeebe:
  client:
    broker.gateway-address: zeebe:26500
    security.plaintext: true
magenta:
  stripe:
    api-key: ${STRIPE_API_KEY}
    webhook-secret: ${STRIPE_WEBHOOK_SECRET}
  clients:
    areas:     http://areas:8081
    banks:     http://banks:8082
    customers: http://customers:8083
    products:  http://products:8084
  partners:
    weights: { sla: 0.5, nps: 0.3, cost: 0.2 }
```

---

## 15. Testing

- Unit para `RuleEngine` de asignación de partners y motor de pricing (`priceFormula`).
- Testcontainers: Postgres + Zeebe + Wiremock (Stripe, banks, customers, products).
- Pruebas BPMN con `zeebe-process-test`.
- Contract tests Pact (consumidores: este módulo) sobre `customers`, `products`, `banks`.
- E2E Playwright: flujo "cotizar → pagar → recibir deliverable".

---

## 16. Checklist

- [ ] Migraciones Flyway con RLS y outbox.
- [ ] Catálogo seed para los 17 servicios mencionados.
- [ ] BPMN deployados en Camunda (`bpmn/*.bpmn`).
- [ ] Job workers Spring (`@JobWorker`) implementados y testados.
- [ ] Integración Stripe Connect (cobros + payouts).
- [ ] Webhooks firmados y antirreplay.
- [ ] Generador de facturas PDF + numeración correlativa.
- [ ] OpenAPI + stubs.
- [ ] Dashboard Grafana `services-sla.json`.
- [ ] Helm chart `charts/servicios/`.
