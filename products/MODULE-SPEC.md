# Módulo `products` — Especificación técnica (autocontenida)

> Plataforma **Magenta** · Bounded Context: **Catálogo de inmuebles y transacciones**
> Stack: Java 21 · Spring Boot 3.3 · PostgreSQL 16 + OpenSearch 2.13 · Angular 17 · Kafka 3.7
> Puerto local: **8084** · BD: `products_db` · Esquema: `products`
>
> Documento autocontenido. Contratos cross-cutting en `../ARCHITECTURE.md`.

---

## 1. Misión del módulo

Catálogo y ciclo de vida de los **inmuebles** y sus **operaciones** (venta, alquiler, alquiler con
opción a compra, permuta). Tipologías cubiertas:

`FLAT`, `APARTMENT`, `STUDIO`, `HOUSE`, `VILLA`, `TOWNHOUSE`, `DUPLEX`, `LOFT`, `PENTHOUSE`,
`COUNTRY_HOUSE`, `RUSTIC_LAND`, `URBAN_LAND`, `PARKING`, `STORAGE_ROOM`,
`COMMERCIAL_PREMISES`, `OFFICE`, `WAREHOUSE`, `HOTEL`, `BUILDING`.

Funciones clave:

- Alta, edición, publicación, despublicación y archivado de inmuebles y sus operaciones.
- Galería multimedia (fotos, planos, virtual-tour 360, video).
- **Búsqueda avanzada** geográfica y facetada (OpenSearch), con filtros por zona, precio,
  superficie, dormitorios, eficiencia energética, accesibilidad, financiación disponible.
- **Matching** con `customer.search-preferences` (entrante por evento).
- Registro de **transacciones** cerradas (input para `areas.price-index`).
- Gestión de favoritos, vistas, leads y agenda de visitas.
- Integración con `banks` para badge "**Financiable 90+5+5**" y con `servicios` para tasación,
  seguros, búsqueda profesional.

Este módulo es **fuente de verdad** del inventario inmobiliario.

---

## 2. Casos de uso principales

| ID    | Caso de uso                                                          | Actor             |
|-------|----------------------------------------------------------------------|-------------------|
| UC-P1 | Alta de inmueble con georreferenciación (lat/lng → zoneId)          | AGENT             |
| UC-P2 | Publicar operación (SALE/RENT/...) con precio y condiciones         | AGENT             |
| UC-P3 | Subida masiva de fotos con generación de thumbnails y watermark      | AGENT             |
| UC-P4 | Búsqueda facetada con filtros + ordenación + paginación              | público           |
| UC-P5 | Búsqueda por mapa (bounding box / polígono / radio)                  | público           |
| UC-P6 | Marcar favorito, registrar vista, abrir lead                        | CUSTOMER          |
| UC-P7 | Agendar visita (slots del agente)                                    | CUSTOMER          |
| UC-P8 | Recibir feasibility de bancos y mostrar badge                        | sistema           |
| UC-P9 | Registrar transacción cerrada (precio final, fecha, comprador)       | AGENT, ADMIN      |
| UC-P10| Importar feeds externos (Idealista/Fotocasa partners) vía CSV/XML    | ADMIN             |
| UC-P11| Sincronizar índice OpenSearch desde Postgres                         | sistema           |
| UC-P12| Reportar inmueble (ocupado, dato erróneo, fraude)                    | público           |

---

## 3. Modelo de dominio

### 3.1 Aggregate `Property` (raíz)

```
Property
 ├─ id, reference (slug humano, único por tenant)
 ├─ type: PropertyType
 ├─ subtype: String?               // "loft estilo industrial", "casa de pueblo"
 ├─ status: enum (DRAFT, ACTIVE, RESERVED, SOLD, RENTED, ARCHIVED)
 ├─ ownership: OwnerInfo (privado, banco, fondo, promotor)
 ├─ location: Location
 │    ├─ address (calle, número, planta, puerta — número CIFRADO si DRAFT)
 │    ├─ postalCode
 │    ├─ coordinates: GeoPoint
 │    ├─ zoneId (ref Areas)
 │    └─ visibility: enum (EXACT, NEIGHBORHOOD_ONLY, MUNICIPALITY_ONLY)
 ├─ surface: Surface
 │    ├─ builtSqm, usefulSqm, plotSqm?
 ├─ layout: { rooms, bathrooms, terraces, parkingSpots, storageRooms, floor, hasElevator }
 ├─ condition: enum (NEW_BUILD, GOOD, NEEDS_REFORM, TO_DEMOLISH)
 ├─ buildYear, lastRenovationYear
 ├─ energyRating: { consumption: A..G + value kWh/m²·yr, emissions: A..G + value kgCO2/m²·yr,
 │                   certificateNumber, validUntil }
 ├─ features: Set<Feature>   // AC, heating, pool, garden, fiber, security, accessibility...
 ├─ orientation: Set<enum>   // N, NE, E, ...
 ├─ ite: { hasIte, result (FAVORABLE|UNFAVORABLE), issuedAt, nextDueAt }
 ├─ tags: Set<String>        // "luminoso", "obra-nueva", "100%-financiable"
 ├─ media: List<MediaAsset>
 ├─ documents: List<DocumentRef>  (nota simple, planos, escritura previa)
 ├─ operations: List<Operation>   (al menos una para publicar)
 └─ financing: FinancingHint      // calculado por evento de banks
     ├─ feasibleBankProductIds: Set<UUID>
     ├─ best90_5_5ProductId?
     └─ lastEvaluatedAt
```

### 3.2 Entidades hijas

```
Operation
 ├─ id, propertyId
 ├─ type: SALE | RENT | RENT_TO_OWN | EXCHANGE
 ├─ price: Money               // venta o alquiler/mes
 ├─ depositMonths?             // alquiler
 ├─ minContractMonths?
 ├─ rentToOwn: { optionFeePct, optionExerciseMonths, salePrice }?
 ├─ exchangeWishes: { types, zoneIds, maxDelta }?  // permuta
 ├─ negotiable: Boolean
 ├─ availableFrom: Date
 ├─ commissionPct?            // honorarios mediación
 ├─ status: enum (DRAFT, ACTIVE, RESERVED, CLOSED)
 ├─ exclusivity: Boolean
 └─ publishedAt

MediaAsset
 ├─ id, kind (PHOTO, FLOORPLAN, VIDEO, VIRTUAL_TOUR, AERIAL)
 ├─ storageUri, mimeType, sizeBytes, width, height
 ├─ aiTags: List<String>   // generadas por modelo de vision
 ├─ order, isCover
 └─ uploadedAt

Lead
 ├─ id, propertyId, operationId, customerId?, anonymousContact?
 ├─ source (WEB, MOBILE, PORTAL_PARTNER, AGENT_BACKOFFICE)
 ├─ message, status (NEW, CONTACTED, QUALIFIED, LOST, WON)
 └─ assignedAgentId

Visit
 ├─ id, propertyId, customerId, agentId
 ├─ slotStart, slotEnd, mode (IN_PERSON, VIDEO_CALL)
 ├─ status (REQUESTED, CONFIRMED, COMPLETED, CANCELLED, NO_SHOW)
 └─ feedback (rating, notes)

Favorite
 ├─ customerId, propertyId, createdAt

PropertyView
 ├─ id, propertyId, userId?, anonId, at, channel, referrer
 (tabla particionada por mes, sólo analítica)

Transaction
 ├─ id, propertyId, operationId, type (SALE|RENT|...)
 ├─ finalPrice, currency, surfaceSqm, pricePerSqm
 ├─ buyerCustomerId, sellerCustomerId
 ├─ bankProductId?, mortgageAmount?, ltv?
 ├─ closedAt, deedNotaryProtocol?
 └─ source (PLATFORM | IMPORTED)
```

### 3.3 Invariantes

- Un `Property` no se publica (`ACTIVE`) sin: ≥ 3 fotos, ≥ 1 operación activa, `energyRating`
  declarado, `zoneId` resuelto.
- `Operation.price > 0` y coherente con `currency`.
- `Property.location.coordinates` debe caer dentro de `zone.boundary` (validación contra `areas`).
- Cambios de estado a `SOLD`/`RENTED` requieren `Transaction` asociada.
- Dirección exacta sólo visible a usuarios con lead aceptado o role AGENT/ADMIN.

---

## 4. Esquema PostgreSQL (`products`)

```sql
CREATE SCHEMA products;
SET search_path TO products;

CREATE TABLE properties (
    id              UUID PRIMARY KEY,
    tenant_id       UUID NOT NULL,
    reference       VARCHAR(80) NOT NULL,
    type            VARCHAR(32) NOT NULL,
    subtype         VARCHAR(80),
    status          VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
    owner_info      JSONB NOT NULL DEFAULT '{}',
    address         JSONB NOT NULL,
    address_exact_enc BYTEA,                 -- número/portal cifrados hasta publicar
    postal_code     VARCHAR(10) NOT NULL,
    coordinates     GEOGRAPHY(POINT, 4326) NOT NULL,
    zone_id         UUID NOT NULL,
    visibility      VARCHAR(24) NOT NULL DEFAULT 'NEIGHBORHOOD_ONLY',
    built_sqm       NUMERIC(8,2) NOT NULL,
    useful_sqm      NUMERIC(8,2),
    plot_sqm        NUMERIC(10,2),
    rooms           SMALLINT,
    bathrooms       SMALLINT,
    terraces        SMALLINT,
    parking_spots   SMALLINT,
    storage_rooms   SMALLINT,
    floor           SMALLINT,
    has_elevator    BOOLEAN,
    condition       VARCHAR(20),
    build_year      INTEGER,
    last_reno_year  INTEGER,
    energy          JSONB NOT NULL DEFAULT '{}',
    features        TEXT[] NOT NULL DEFAULT '{}',
    orientation     TEXT[] NOT NULL DEFAULT '{}',
    ite             JSONB,
    tags            TEXT[] NOT NULL DEFAULT '{}',
    financing       JSONB NOT NULL DEFAULT '{}',
    search_vector   tsvector GENERATED ALWAYS AS
                    (to_tsvector('spanish',
                       unaccent(coalesce(reference,'') || ' ' ||
                                coalesce(subtype,'') || ' ' ||
                                array_to_string(tags,' ')))) STORED,
    published_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      VARCHAR(64) NOT NULL,
    updated_by      VARCHAR(64) NOT NULL,
    version         BIGINT NOT NULL DEFAULT 0,
    deleted_at      TIMESTAMPTZ,
    UNIQUE (tenant_id, reference)
);
CREATE INDEX idx_prop_status   ON properties(status);
CREATE INDEX idx_prop_type     ON properties(type);
CREATE INDEX idx_prop_zone     ON properties(zone_id);
CREATE INDEX idx_prop_coords   ON properties USING gist (coordinates);
CREATE INDEX idx_prop_search   ON properties USING gin (search_vector);
CREATE INDEX idx_prop_features ON properties USING gin (features);

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
CREATE INDEX idx_op_property ON operations(property_id);
CREATE INDEX idx_op_type_status ON operations(type, status);

CREATE TABLE media_assets (
    id           UUID PRIMARY KEY,
    property_id  UUID NOT NULL REFERENCES properties(id) ON DELETE CASCADE,
    kind         VARCHAR(20) NOT NULL,
    storage_uri  TEXT NOT NULL,
    mime_type    VARCHAR(80) NOT NULL,
    size_bytes   BIGINT NOT NULL,
    width        INTEGER, height INTEGER,
    ai_tags      TEXT[] NOT NULL DEFAULT '{}',
    "order"      INTEGER NOT NULL DEFAULT 0,
    is_cover     BOOLEAN NOT NULL DEFAULT FALSE,
    uploaded_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE favorites (
    customer_id UUID NOT NULL,
    property_id UUID NOT NULL REFERENCES properties(id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (customer_id, property_id)
);

CREATE TABLE leads (
    id              UUID PRIMARY KEY,
    property_id     UUID NOT NULL REFERENCES properties(id),
    operation_id    UUID REFERENCES operations(id),
    customer_id     UUID,
    anon_contact    JSONB,
    source          VARCHAR(24) NOT NULL,
    message         TEXT,
    status          VARCHAR(16) NOT NULL DEFAULT 'NEW',
    assigned_agent  VARCHAR(64),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_lead_status ON leads(status, created_at DESC);

CREATE TABLE visits (
    id            UUID PRIMARY KEY,
    property_id   UUID NOT NULL REFERENCES properties(id),
    customer_id   UUID NOT NULL,
    agent_id      VARCHAR(64) NOT NULL,
    slot_start    TIMESTAMPTZ NOT NULL,
    slot_end      TIMESTAMPTZ NOT NULL,
    mode          VARCHAR(16) NOT NULL,
    status        VARCHAR(16) NOT NULL DEFAULT 'REQUESTED',
    feedback      JSONB,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    EXCLUDE USING gist (
        agent_id WITH =,
        tstzrange(slot_start, slot_end) WITH &&
    ) WHERE (status IN ('REQUESTED','CONFIRMED'))
);

CREATE TABLE property_views (
    id            UUID PRIMARY KEY,
    property_id   UUID NOT NULL,
    user_id       UUID,
    anon_id       UUID,
    at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    channel       VARCHAR(16),
    referrer      TEXT
) PARTITION BY RANGE (at);
CREATE TABLE property_views_2026_05 PARTITION OF property_views
    FOR VALUES FROM ('2026-05-01') TO ('2026-06-01');

CREATE TABLE transactions (
    id                  UUID PRIMARY KEY,
    tenant_id           UUID NOT NULL,
    property_id         UUID NOT NULL REFERENCES properties(id),
    operation_id        UUID NOT NULL REFERENCES operations(id),
    type                VARCHAR(16) NOT NULL,
    final_price         NUMERIC(14,2) NOT NULL,
    currency            CHAR(3) NOT NULL DEFAULT 'EUR',
    surface_sqm         NUMERIC(8,2) NOT NULL,
    price_per_sqm       NUMERIC(12,2) NOT NULL,
    buyer_customer_id   UUID,
    seller_customer_id  UUID,
    bank_product_id     UUID,
    mortgage_amount     NUMERIC(14,2),
    ltv                 NUMERIC(5,2),
    closed_at           DATE NOT NULL,
    deed_notary_proto   VARCHAR(120),
    source              VARCHAR(16) NOT NULL DEFAULT 'PLATFORM',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_tx_property ON transactions(property_id);
CREATE INDEX idx_tx_closed   ON transactions(closed_at);

CREATE TABLE outbox_event (
    id UUID PRIMARY KEY, aggregate VARCHAR(64), aggregate_id UUID,
    type VARCHAR(120), payload JSONB, created_at TIMESTAMPTZ DEFAULT now(),
    published_at TIMESTAMPTZ
);

ALTER TABLE properties ENABLE ROW LEVEL SECURITY;
CREATE POLICY prop_tenant ON properties
    USING (tenant_id = current_setting('app.tenant_id')::uuid);
```

---

## 5. OpenSearch — índice `properties_v1`

Mapping resumido:

```json
{
  "properties": {
    "id":            { "type": "keyword" },
    "tenantId":      { "type": "keyword" },
    "type":          { "type": "keyword" },
    "status":        { "type": "keyword" },
    "zone": {
      "properties": {
        "id":   { "type": "keyword" },
        "path": { "type": "keyword" }    // ["country","region","province","municipality"]
      }
    },
    "coordinates":   { "type": "geo_point" },
    "price":         { "type": "scaled_float", "scaling_factor": 100 },
    "operationType": { "type": "keyword" },
    "builtSqm":      { "type": "float" },
    "rooms":         { "type": "short" },
    "bathrooms":     { "type": "short" },
    "energy":        { "type": "keyword" },
    "features":      { "type": "keyword" },
    "tags":          { "type": "keyword" },
    "financing": {
      "properties": {
        "feasibleBankProductIds": { "type": "keyword" },
        "has90_5_5":              { "type": "boolean" }
      }
    },
    "text":          { "type": "text", "analyzer": "spanish" },
    "publishedAt":   { "type": "date" }
  }
}
```

Sincronización: **Outbox → Debezium → Kafka → Indexer service** (parte del propio módulo,
adapter `out/search/`). Sharding por provincia (`zone.path[2]`) en clusters grandes.

---

## 6. API REST (`/api/v1`)

| Método | Path                                            | Roles                  |
|--------|-------------------------------------------------|------------------------|
| GET    | `/properties`                                   | público (filtros)      |
| GET    | `/properties/{id}`                              | público                |
| POST   | `/properties`                                   | AGENT, ADMIN           |
| PATCH  | `/properties/{id}`                              | AGENT (owner), ADMIN   |
| POST   | `/properties/{id}/publish`                      | AGENT, ADMIN           |
| POST   | `/properties/{id}/archive`                      | AGENT, ADMIN           |
| POST   | `/properties/{id}/operations`                   | AGENT                  |
| PATCH  | `/operations/{id}`                              | AGENT                  |
| POST   | `/properties/{id}/media`  (multipart)           | AGENT                  |
| DELETE | `/media/{id}`                                   | AGENT                  |
| GET    | `/search` (facetas + geo)                       | público                |
| GET    | `/search/map?bbox=W,S,E,N&filters=...`          | público                |
| POST   | `/leads`                                        | público                |
| GET    | `/leads`                                        | AGENT, ADMIN           |
| PATCH  | `/leads/{id}`                                   | AGENT                  |
| POST   | `/visits`                                       | CUSTOMER               |
| PATCH  | `/visits/{id}`                                  | CUSTOMER, AGENT        |
| POST   | `/favorites/{propertyId}`                       | CUSTOMER               |
| DELETE | `/favorites/{propertyId}`                       | CUSTOMER               |
| GET    | `/favorites`                                    | CUSTOMER               |
| POST   | `/transactions`                                 | AGENT, ADMIN           |
| GET    | `/transactions?propertyId=`                     | AGENT, ADMIN           |
| POST   | `/properties/{id}/report`                       | público (captcha)      |
| POST   | `/imports/feed`  (multipart XML/CSV)            | ADMIN                  |

### 6.1 `GET /search`

Query params: `q`, `zoneIds[]`, `type`, `operation`, `priceMin/Max`, `surfaceMin/Max`,
`roomsMin`, `bathroomsMin`, `energyMin`, `features[]`, `has90_5_5`, `sort=price,asc|publishedAt,desc`,
`page`, `size`.

Respuesta incluye `aggregations` (facetas) y `_links` paginación.

### 6.2 `GET /properties/{id}` — DTO

```json
{
  "id": "0190d1...-uuid",
  "reference": "FV-2026-00417",
  "type": "FLAT",
  "status": "ACTIVE",
  "location": {
    "address": { "street": "C/ Real", "floor": "2", "door": "B", "exactHidden": true },
    "postalCode": "18340",
    "coordinates": { "lat": 37.219, "lng": -3.789 },
    "zone": { "id": "...", "name": "Fuente Vaqueros", "type": "MUNICIPALITY",
              "path": ["España","Andalucía","Granada","Fuente Vaqueros"] }
  },
  "surface": { "builtSqm": 78, "usefulSqm": 70 },
  "layout": { "rooms": 3, "bathrooms": 1, "floor": 2, "hasElevator": true },
  "condition": "GOOD",
  "buildYear": 1998,
  "energy": { "consumption": { "letter": "E", "value": 187 },
              "emissions":  { "letter": "E", "value": 38 } },
  "features": ["FIBER","HEATING","TERRACE","ELEVATOR"],
  "tags": ["luminoso","100%-financiable"],
  "operations": [
    { "id": "...", "type": "SALE", "price": 75000, "negotiable": true }
  ],
  "financing": {
    "has90_5_5": true,
    "feasibleBanks": [
      { "bankId": "...", "name": "Santander", "productSku": "HIPOTECA-90-5-5",
        "monthlyPayment": 305, "tae": 4.1 }
    ]
  },
  "media": [ { "id":"...","kind":"PHOTO","url":"https://cdn.../w800.jpg","isCover":true } ],
  "stats": { "views30d": 132, "favorites": 8, "leads": 3 }
}
```

---

## 7. Eventos publicados

| Topic                              | Tipo                                                              |
|------------------------------------|--------------------------------------------------------------------|
| `magenta.products.property.v1`     | `PropertyCreated`, `PropertyUpdated`, `PropertyPublished`, `PropertyArchived` |
| `magenta.products.operation.v1`    | `OperationPublished`, `OperationPriceChanged`, `OperationClosed`  |
| `magenta.products.lead.v1`         | `LeadCreated`, `LeadQualified`, `LeadConverted`, `LeadLost`       |
| `magenta.products.visit.v1`        | `VisitRequested`, `VisitConfirmed`, `VisitCompleted`              |
| `magenta.products.transaction.v1`  | `TransactionRegistered`                                            |

Payload `PropertyPublished` mínimo:

```json
{
  "propertyId": "uuid", "tenantId":"uuid", "zoneId":"uuid",
  "type":"FLAT", "operationType":"SALE",
  "price":75000, "builtSqm":78, "rooms":3,
  "coordinates":{"lat":37.219,"lng":-3.789},
  "features":["FIBER","ELEVATOR"], "energyLetter":"E"
}
```

---

## 8. Eventos consumidos

| Origen     | Topic                              | Acción                                                       |
|------------|------------------------------------|---------------------------------------------------------------|
| areas      | `magenta.areas.zone.v1`            | Reindexar properties cuya zona cambia de jerarquía/estatus    |
| areas      | `magenta.areas.price-index.v1`     | Calcular "below/above market" badge                           |
| banks      | `magenta.banks.product.v1`         | Re-evaluar `financing.feasibleBankProductIds`                 |
| banks      | `magenta.banks.preapproval.v1`     | Reservar inmueble si `APPROVED`; liberar si `EXPIRED`         |
| customers  | `magenta.customers.preferences.v1` | Disparar matching contra inventario activo (cola asíncrona)   |
| customers  | `magenta.customers.profile.v1`     | Cache mínima de afford-band para ranking personalizado         |
| servicios  | `magenta.servicios.workflow.v1`    | Estados de tasación / seguro afectan publicación              |

---

## 9. Integraciones síncronas

- `areas` → resolver `zoneId` desde coordenadas (`POST /zones/resolve`), validar boundary.
- `banks` → `GET /financing-feasibility?propertyId=` al publicar o cambiar precio.
- `customers` → consulta opcional al renderizar detalle (badge "encaja con tu perfil").
- **Catastro / Sede Electrónica** (opcional): metadatos catastrales por referencia.
- **CDN** (CloudFront/Fastly) para `cdn.magenta.es/properties/{id}/{w}.{format}`.
- **Image processing**: pipeline a Lambda / contenedor `sharp` para variantes y watermark.

---

## 10. Búsqueda y ranking

- Score base OpenSearch (BM25 sobre `text`) + boosting por: `featuredUntil`, `freshness`,
  `has90_5_5`, `priceVsMarketDelta` (más barato → boost), `qualityOfLifeIndex` (de areas).
- Para usuarios autenticados: re-ranking en cliente con `customer.affordability` (penaliza
  precios fuera de rango).
- "Listings near me": `geo_distance` con `pin` del usuario.

---

## 11. Frontend (libs/domain/products)

- `PropertySearchPage` con filtros laterales (signals), facetas dinámicas y URL persistida.
- `PropertyMapView` (Leaflet + clustering, marker per cluster, side panel).
- `PropertyDetailPage` con galería, plano, video, virtual tour iframe, financiación.
- `PropertyFormWizard` para alta (AGENT) en 5 pasos.
- `FavoritesPage`, `VisitsCalendar` con date-fns.
- `Financing90_5_5Badge` reutilizable (consume `banks` via servicio cacheado).

---

## 12. Seguridad

- Dirección exacta (número, planta, puerta) **cifrada en BD** hasta `PropertyPublished`; en estado
  publicado se muestra sólo `street + zona`, salvo a `AGENT` o `CUSTOMER` con lead aceptado.
- Endpoints de creación/edición: `ROLE_AGENT` con `ownership_check` (propietario o admin).
- Reportar inmueble con CAPTCHA + rate limit (5/h/IP).
- Watermark obligatorio en fotos públicas; original sin marca sólo accesible vía URL firmada
  (S3 presigned, 5 min) para el agente.
- Anti-scraping: respuestas paginadas, `Crawl-Delay` + WAF, fingerprinting de feeds anómalos.

---

## 13. Performance

- Lectura `/properties/{id}`: Caffeine local 60 s + Redis 5 min.
- Búsqueda: OpenSearch dedicado, **3 nodos** mínimo, replica factor 1.
- Imágenes servidas por CDN con `Cache-Control: public, max-age=31536000, immutable`.
- Conexiones HikariCP 30 (lectura) / 10 (escritura).
- SLO: p95 `/search` < 250 ms; `/properties/{id}` < 120 ms.

---

## 14. Configuración

```yaml
spring:
  application: { name: magenta-products }
  datasource:
    url: jdbc:postgresql://postgres:5432/products_db?currentSchema=products
server: { port: 8084 }
magenta:
  search:
    opensearch-uri: http://opensearch:9200
    index: properties_v1
  media:
    s3-bucket: magenta-properties-media
    cdn-base: https://cdn.magenta.es
  clients:
    areas:     http://areas:8081
    banks:     http://banks:8082
    customers: http://customers:8083
```

---

## 15. Testing

- Unit + ArchUnit + Bean Validation tests para invariantes de publicación.
- Testcontainers: Postgres + PostGIS + OpenSearch + Kafka + LocalStack S3.
- Pact: stubs `products-stubs:1.x` (banks y customers son consumidores).
- E2E Playwright: flujo "buscar → favorito → solicitar visita".
- Carga k6: 3 000 rps en `/search` con p95 < 300 ms; 500 rps en `/properties/{id}`.

---

## 16. Checklist

- [ ] Migraciones Flyway con PostGIS + tsvector + EXCLUDE GIST visits.
- [ ] Entidades JPA con `@Filter` multi-tenant + RLS.
- [ ] Servicio `PropertyPublisher` con reglas de invariantes.
- [ ] Indexer Kafka → OpenSearch (idempotente).
- [ ] Pipeline media (thumbs, watermark, AI tagging opcional).
- [ ] Outbox + Debezium configurado.
- [ ] OpenAPI + stubs.
- [ ] Helm chart `charts/products/`.
