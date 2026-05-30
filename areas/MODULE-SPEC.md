# Módulo `areas` — Especificación técnica (autocontenida)

> Plataforma **Magenta** · Bounded Context: **Geografía y mercado por zona**
> Stack v1.0 (ADR-001): Java 21 · Spring Boot 3.3 · PostgreSQL 16 · Angular 17 · Kafka 3.7 · Keycloak 24
> Stack destino (PAE-001 → v2.0 Q4 2026): Java 25 · Spring Boot 4.0.6 · PG 18.4 · Kafka 4.3 KRaft · Redis 8
> Puerto local: **8081** · BD: `areas_db` · Esquema: `areas`
>
> Este documento es suficiente para implementar el módulo sin abrir el resto del repositorio.
> Lo que NO esté aquí, se especifica en `../ARCHITECTURE.md` (contratos cross-cutting).
> Decisiones de stack: `docs/adr/ADR-001-stack-versions.md` y `docs/adr/ADR-002-pae-001-migration-decision.md`.

---

## 1. Misión del módulo

Modelar y servir la **jerarquía geográfica** donde se ofertan productos (inmuebles) y servicios:

```
País → Comunidad Autónoma → Provincia → Comarca → Municipio → Distrito → Barrio → Urbanización → Calle → Portal
```

Y enriquecerla con **datos de mercado** que el resto de módulos consume:

- Índice de precio €/m² (venta y alquiler) por zona y tipología.
- Tendencia interanual (%) y stock disponible.
- Cobertura de fibra (FTTH) y servicios básicos (hospital, AVE, autovía).
- Riesgo (ocupación, despoblación, ITE desfavorable).
- Demanda relativa (búsquedas, leads) por zona.

Este módulo es **la fuente de verdad** para todo lo que tenga `zoneId`, `postalCode` o coordenadas.

---

## 2. Casos de uso principales

| ID    | Caso de uso                                              | Actor          |
|-------|----------------------------------------------------------|----------------|
| UC-A1 | Buscar zonas por texto autocompletado                    | Cualquiera     |
| UC-A2 | Consultar jerarquía completa de una zona                 | Cualquiera     |
| UC-A3 | Resolver punto (lat, lng) → zona                         | Sistema        |
| UC-A4 | Obtener índice de precio €/m² por zona/tipología         | Cualquiera     |
| UC-A5 | Consultar enriquecimientos (fibra, hospital, tren)       | Cualquiera     |
| UC-A6 | Comparar 2-5 zonas (precio, servicios, riesgo)           | Customer       |
| UC-A7 | Importar polígonos GeoJSON oficiales (INE, Catastro)     | Admin          |
| UC-A8 | Recalcular índices a partir de transacciones cerradas    | Sistema (cron) |
| UC-A9 | Publicar evento de cambio de índice / zona               | Sistema        |

---

## 3. Modelo de dominio

### 3.1 Aggregate `Zone` (raíz)

```
Zone
 ├─ id: UUID
 ├─ code: String (canónico, ej. "ES-AN-GR-18000-FUENTE-VAQUEROS")
 ├─ name: String
 ├─ type: ZoneType (COUNTRY, REGION, PROVINCE, COUNTY, MUNICIPALITY, DISTRICT,
 │                  NEIGHBORHOOD, URBANIZATION, STREET, BUILDING)
 ├─ parentId: UUID?
 ├─ ineCode: String? (código INE oficial)
 ├─ postalCodes: Set<String>
 ├─ centroid: GeoPoint
 ├─ boundary: Geometry (PostGIS MULTIPOLYGON, SRID 4326)
 ├─ population: Integer?
 ├─ areaKm2: BigDecimal?
 ├─ status: ZoneStatus (ACTIVE, DEPRECATED)
 └─ tags: Set<String>
```

### 3.2 Entidades de mercado

```
PriceIndex
 ├─ zoneId, propertyType, operationType (SALE|RENT), period (YYYY-MM)
 ├─ pricePerSqm: Money
 ├─ yoyDeltaPct, momDeltaPct
 ├─ sampleSize, confidence (0..1)
 └─ source: SourceRef (INTERNAL | IDEALISTA | FOTOCASA | INE | MITMA)

ZoneEnrichment
 ├─ zoneId
 ├─ fiberCoveragePct: Integer (0..100)
 ├─ hasHospital: Boolean
 ├─ hospitalKind: enum (NONE, PRIMARY_CARE, GENERAL, REFERENCE_UNIVERSITY)
 ├─ trainToHubMinutes: Integer?  (referencia configurable: Madrid/BCN)
 ├─ highwayDistanceKm: BigDecimal?
 ├─ supermarketsCount: Integer?
 ├─ riskOccupationScore: Integer (0..100)
 ├─ depopulationRisk: enum (LOW, MEDIUM, HIGH)
 └─ qualityOfLifeIndex: Integer (0..100)

ZoneDemandSnapshot
 ├─ zoneId, period
 ├─ searches, leads, viewedProperties, savedSearches
 └─ supplyDemandRatio: BigDecimal
```

### 3.3 Invariantes

- `parentId` debe pertenecer al `ZoneType` inmediatamente superior (validado por servicio).
- `boundary` debe contener al `centroid`.
- `PriceIndex.confidence < 0.5` no se publica al exterior.
- Un `Zone` con `status = DEPRECATED` puede consultarse pero no aceptar nuevos productos.

---

## 4. Esquema PostgreSQL (`areas`)

> Requiere extensiones `postgis`, `pg_trgm`, `unaccent`.

```sql
CREATE SCHEMA areas;
SET search_path TO areas;

CREATE TABLE zones (
    id              UUID PRIMARY KEY,
    tenant_id       UUID NOT NULL,
    code            VARCHAR(120) NOT NULL UNIQUE,
    name            VARCHAR(160) NOT NULL,
    type            VARCHAR(20)  NOT NULL,
    parent_id       UUID REFERENCES zones(id),
    ine_code        VARCHAR(20),
    postal_codes    TEXT[] NOT NULL DEFAULT '{}',
    centroid        GEOGRAPHY(POINT, 4326) NOT NULL,
    boundary        GEOGRAPHY(MULTIPOLYGON, 4326),
    population      INTEGER,
    area_km2        NUMERIC(12,4),
    status          VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    tags            TEXT[] NOT NULL DEFAULT '{}',
    search_vector   tsvector GENERATED ALWAYS AS
                    (to_tsvector('spanish', unaccent(coalesce(name,'') || ' ' || coalesce(code,'')))) STORED,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      VARCHAR(64) NOT NULL,
    updated_by      VARCHAR(64) NOT NULL,
    version         BIGINT NOT NULL DEFAULT 0,
    deleted_at      TIMESTAMPTZ
);

CREATE INDEX idx_zones_parent      ON zones(parent_id);
CREATE INDEX idx_zones_type        ON zones(type);
CREATE INDEX idx_zones_postal      ON zones USING gin (postal_codes);
CREATE INDEX idx_zones_search      ON zones USING gin (search_vector);
CREATE INDEX idx_zones_centroid    ON zones USING gist (centroid);
CREATE INDEX idx_zones_boundary    ON zones USING gist (boundary);
CREATE INDEX idx_zones_tenant      ON zones(tenant_id);

CREATE TABLE price_indices (
    id                UUID PRIMARY KEY,
    tenant_id         UUID NOT NULL,
    zone_id           UUID NOT NULL REFERENCES zones(id),
    property_type     VARCHAR(24) NOT NULL,
    operation_type    VARCHAR(16) NOT NULL,
    period            DATE NOT NULL,                -- primer día del mes
    price_per_sqm     NUMERIC(12,2) NOT NULL,
    currency          CHAR(3) NOT NULL DEFAULT 'EUR',
    yoy_delta_pct     NUMERIC(6,3),
    mom_delta_pct     NUMERIC(6,3),
    sample_size       INTEGER NOT NULL,
    confidence        NUMERIC(4,3) NOT NULL,
    source            VARCHAR(24) NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (zone_id, property_type, operation_type, period, source)
) PARTITION BY RANGE (period);

CREATE TABLE price_indices_2026 PARTITION OF price_indices
    FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');

CREATE TABLE zone_enrichment (
    zone_id                  UUID PRIMARY KEY REFERENCES zones(id),
    tenant_id                UUID NOT NULL,
    fiber_coverage_pct       INTEGER CHECK (fiber_coverage_pct BETWEEN 0 AND 100),
    has_hospital             BOOLEAN NOT NULL DEFAULT FALSE,
    hospital_kind            VARCHAR(32) NOT NULL DEFAULT 'NONE',
    train_to_hub_minutes     INTEGER,
    highway_distance_km      NUMERIC(8,2),
    supermarkets_count       INTEGER,
    risk_occupation_score    INTEGER CHECK (risk_occupation_score BETWEEN 0 AND 100),
    depopulation_risk        VARCHAR(8) NOT NULL DEFAULT 'LOW',
    quality_of_life_index    INTEGER CHECK (quality_of_life_index BETWEEN 0 AND 100),
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE zone_demand_snapshots (
    id                  UUID PRIMARY KEY,
    zone_id             UUID NOT NULL REFERENCES zones(id),
    period              DATE NOT NULL,
    searches            INTEGER NOT NULL DEFAULT 0,
    leads               INTEGER NOT NULL DEFAULT 0,
    viewed_properties   INTEGER NOT NULL DEFAULT 0,
    saved_searches      INTEGER NOT NULL DEFAULT 0,
    supply_demand_ratio NUMERIC(8,4),
    UNIQUE (zone_id, period)
);

CREATE TABLE outbox_event (
    id           UUID PRIMARY KEY,
    aggregate    VARCHAR(64) NOT NULL,
    aggregate_id UUID NOT NULL,
    type         VARCHAR(120) NOT NULL,
    payload      JSONB NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at TIMESTAMPTZ
);

-- Row Level Security
ALTER TABLE zones ENABLE ROW LEVEL SECURITY;
CREATE POLICY zones_tenant_isolation ON zones
    USING (tenant_id = current_setting('app.tenant_id')::uuid);
```

---

## 5. API REST (`/api/v1`)

Base: `http://areas:8081/api/v1`. Todas las respuestas son JSON. Autenticación: Bearer JWT.
Errores en formato RFC 7807. Paginación estándar Spring Data.

| Método | Path                                          | Descripción                                | Roles                     |
|--------|-----------------------------------------------|--------------------------------------------|---------------------------|
| GET    | `/zones`                                      | Listar/filtrar zonas                       | público                   |
| GET    | `/zones/{id}`                                 | Detalle de zona (con padre y enriquecimiento) | público                |
| GET    | `/zones/{id}/children`                        | Zonas hijas                                | público                   |
| GET    | `/zones/{id}/ancestors`                       | Cadena hasta raíz                          | público                   |
| GET    | `/zones/search?q={text}&types=...&limit=10`   | Autocompletado (trigram + FTS)             | público                   |
| GET    | `/zones/resolve?lat={}&lng={}`                | Punto → zona más específica que lo contiene| público                   |
| GET    | `/zones/by-postal-code/{cp}`                  | Zonas asociadas a un CP                    | público                   |
| POST   | `/zones`                                      | Crear zona                                 | ADMIN                     |
| PUT    | `/zones/{id}`                                 | Actualizar zona                            | ADMIN                     |
| DELETE | `/zones/{id}`                                 | Baja lógica                                | ADMIN                     |
| POST   | `/zones/import`                               | Importar GeoJSON (multipart)               | ADMIN                     |
| GET    | `/price-indices?zoneId=&type=&op=&from=&to=`  | Serie temporal de €/m²                     | público                   |
| GET    | `/price-indices/latest?zoneId=&type=&op=`     | Último valor                               | público                   |
| POST   | `/price-indices/recompute`                    | Recalcular (admin / cron)                  | ADMIN, SYSTEM             |
| GET    | `/enrichment/{zoneId}`                        | Datos de fibra, hospital, transporte       | público                   |
| PATCH  | `/enrichment/{zoneId}`                        | Actualizar enriquecimientos                | ADMIN                     |
| GET    | `/compare?zoneIds=a,b,c`                      | Comparativa multi-zona                     | público (rate-limited)    |
| GET    | `/demand/{zoneId}?period=YYYY-MM`             | Snapshot de demanda                        | AGENT, ADMIN              |

### 5.1 DTO ejemplo — `ZoneDetailResponse`

```json
{
  "id": "0190af11-5f33-7a9b-9f10-ce0a7c41c8b1",
  "code": "ES-AN-GR-18340-FUENTE-VAQUEROS",
  "name": "Fuente Vaqueros",
  "type": "MUNICIPALITY",
  "parent": { "id": "...", "name": "Granada", "type": "PROVINCE" },
  "ineCode": "18083",
  "postalCodes": ["18340"],
  "centroid": { "lat": 37.219, "lng": -3.789 },
  "population": 4329,
  "areaKm2": 17.43,
  "enrichment": {
    "fiberCoveragePct": 96,
    "hasHospital": false,
    "hospitalKind": "NONE",
    "trainToHubMinutes": 35,
    "highwayDistanceKm": 3.2,
    "depopulationRisk": "LOW",
    "qualityOfLifeIndex": 71
  },
  "latestPrice": {
    "sale":  { "pricePerSqm": 980.50, "yoyDeltaPct": 3.4, "period": "2026-04" },
    "rent":  { "pricePerSqm": 6.40,   "yoyDeltaPct": 4.1, "period": "2026-04" }
  },
  "tags": ["metropolitana", "buena-fibra"],
  "createdAt": "2025-01-12T10:00:00Z",
  "updatedAt": "2026-05-29T08:22:11Z"
}
```

### 5.2 DTO ejemplo — `ZoneCompareResponse`

```json
{
  "zones": [
    { "id": "...", "name": "Tortosa",
      "salePricePerSqm": 980, "rentPricePerSqm": 6.1, "hospitalKind": "REFERENCE_UNIVERSITY",
      "fiberCoveragePct": 92, "trainToHubMinutes": 110, "qualityOfLifeIndex": 74 },
    { "id": "...", "name": "Balaguer",
      "salePricePerSqm": 720, "rentPricePerSqm": 4.9, "hospitalKind": "PRIMARY_CARE",
      "fiberCoveragePct": 88, "trainToHubMinutes": 195, "qualityOfLifeIndex": 65 }
  ]
}
```

### 5.3 OpenAPI

Generado con `springdoc-openapi`. Disponible en `/v3/api-docs` y `/swagger-ui.html`.
Contrato exportado a `docs/api/areas-openapi.yaml` y publicado en el Portal de APIs interno.

---

## 6. Eventos publicados (Kafka)

| Topic                          | Tipo CloudEvent                              | Trigger                          |
|--------------------------------|-----------------------------------------------|----------------------------------|
| `magenta.areas.zone.v1`        | `com.magenta.areas.ZoneCreated`               | POST `/zones`                    |
| `magenta.areas.zone.v1`        | `com.magenta.areas.ZoneUpdated`               | PUT/PATCH                        |
| `magenta.areas.zone.v1`        | `com.magenta.areas.ZoneDeprecated`            | DELETE                           |
| `magenta.areas.price-index.v1` | `com.magenta.areas.PriceIndexPublished`       | recompute / import nightly       |
| `magenta.areas.enrichment.v1`  | `com.magenta.areas.ZoneEnrichmentUpdated`     | PATCH `/enrichment/{id}`         |

Esquema Avro `ZoneEvent`:

```json
{
  "namespace": "com.magenta.areas.events",
  "type": "record",
  "name": "ZoneEvent",
  "fields": [
    { "name": "zoneId", "type": "string" },
    { "name": "code",   "type": "string" },
    { "name": "name",   "type": "string" },
    { "name": "type",   "type": "string" },
    { "name": "parentId", "type": ["null","string"], "default": null },
    { "name": "centroid", "type": { "type":"record", "name":"GeoPoint",
        "fields":[{"name":"lat","type":"double"},{"name":"lng","type":"double"}] } },
    { "name": "status", "type": "string" },
    { "name": "tenantId", "type": "string" }
  ]
}
```

Publicación con **Outbox + Debezium**. Garantía: **at-least-once**; consumidores deben ser idempotentes
(usar `event.id`).

---

## 7. Eventos consumidos

| Origen     | Topic                                  | Acción                                                       |
|------------|----------------------------------------|--------------------------------------------------------------|
| products   | `magenta.products.transaction.v1`      | Alimenta `price_indices` (recompute incremental)             |
| products   | `magenta.products.property.v1`         | Incrementa contadores de stock para `ZoneDemandSnapshot`     |
| servicios  | `magenta.servicios.workflow.v1`        | Incrementa `leads`/`searches` por zona                       |

---

## 8. Integraciones síncronas

Este módulo **no llama** a otros módulos en runtime crítico (es upstream puro).
Sí se integra con sistemas externos vía **adaptadores out**:

- **INE** (CSV / API) para códigos administrativos y población.
- **Catastro / Cartociudad** para polígonos y direcciones.
- **OpenStreetMap / Overpass** para POIs (hospitales, estaciones).
- **Operadores de fibra** (CNMC datos abiertos) para cobertura FTTH.

Cada conector se aísla en `infrastructure/adapter/out/ingestion/<source>/` con su propio
`@Scheduled` (nocturno) y test de contrato.

---

## 9. Reglas de cálculo

### 9.1 `PriceIndex.recompute(zoneId, period)`

1. Tomar transacciones cerradas del topic `magenta.products.transaction.v1` en la ventana.
2. Filtrar outliers (winsorización al 5/95 %).
3. Agrupar por `propertyType × operationType`.
4. `pricePerSqm = median(price / surface)`.
5. `confidence = clamp(sampleSize / 30, 0, 1)`.
6. Persistir e idempotente sobre `UNIQUE(zone, type, op, period, source='INTERNAL')`.
7. Emitir `PriceIndexPublished`.

### 9.2 `Zone.resolve(lat, lng)`

```sql
SELECT id, type, code
FROM zones
WHERE ST_Covers(boundary, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography)
  AND status = 'ACTIVE'
ORDER BY CASE type
   WHEN 'BUILDING'      THEN 1
   WHEN 'STREET'        THEN 2
   WHEN 'URBANIZATION'  THEN 3
   WHEN 'NEIGHBORHOOD'  THEN 4
   WHEN 'DISTRICT'      THEN 5
   WHEN 'MUNICIPALITY'  THEN 6
   ELSE 9
END
LIMIT 1;
```

---

## 10. Frontend (libs/domain/areas + features)

- `ZoneAutocompleteComponent` (signal-based, debounce 300 ms, llama `/zones/search`).
- `ZoneDetailPage` con mapa Leaflet + boundary GeoJSON.
- `ZoneComparePage` (gráfico €/m² con `ngx-charts`).
- `ZonePriceTrendChart` reusable.
- NgRx feature `areasFeature` con selectores: `selectZoneById`, `selectComparison`.
- HTTP service `AreasApi` con métodos tipados, generados desde OpenAPI con `openapi-generator-cli`.

---

## 11. Seguridad específica

- Endpoints públicos de lectura cacheados en CDN con `Cache-Control: public, max-age=300`.
- `POST/PUT/PATCH/DELETE` requieren `ROLE_ADMIN` + `tenant_id` coincidente con token.
- Importación de GeoJSON valida tamaño (≤ 100 MB), tipo MIME, y verifica firma SHA-256 si viene de proveedor.
- Rate limit (Bucket4j): `/zones/search` → 60 rpm / IP; `/compare` → 30 rpm / IP.

---

## 12. Performance

- Cache Caffeine local TTL 10 min para `getZone(id)` y `getAncestors(id)`.
- Cache Redis TTL 1 h para `compare(zoneIds)` (clave: sha1 de ids ordenados).
- `latest_price_view` materializada y refrescada por job nightly + on-demand.
- Búsqueda fuzzy con `pg_trgm` (`name % :q`) combinada con `tsvector` ranking.
- SLO: p95 lectura < 120 ms; p95 escritura < 350 ms.

---

## 13. Configuración Spring (`application.yml`)

```yaml
spring:
  application:
    name: magenta-areas
  datasource:
    url: jdbc:postgresql://postgres:5432/areas_db?currentSchema=areas
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
  jpa:
    open-in-view: false
    properties:
      hibernate.jdbc.batch_size: 50
      hibernate.dialect: org.hibernate.dialect.PostgreSQLDialect
  flyway:
    schemas: areas
    locations: classpath:db/migration
  kafka:
    bootstrap-servers: kafka:9092
    properties:
      schema.registry.url: http://schema-registry:8081
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: ${KEYCLOAK_JWKS_URI}
server:
  port: 8081
management:
  endpoints.web.exposure.include: health,info,prometheus
  tracing.sampling.probability: 1.0
  otlp.tracing.endpoint: http://tempo:4317
magenta:
  cache:
    zone-ttl-seconds: 600
    compare-ttl-seconds: 3600
  ingestion:
    ine:    { schedule: "0 0 3 * * *" }
    fiber:  { schedule: "0 30 3 * * SUN" }
```

---

## 14. Testing

- Unit (JUnit 5) sobre servicios de dominio (cálculo de índices, resolución espacial).
- `@DataJpaTest` con **Testcontainers** PostgreSQL **PostGIS** image
  (`postgis/postgis:16-3.4`).
- Tests de contrato (Spring Cloud Contract) para cada endpoint publicado en sección 5;
  stubs publicados como `com.magenta:areas-stubs:1.x`.
- E2E con Karate sobre docker-compose local.
- Carga (k6): 500 rps mixtos sobre `/zones/search` y `/zones/{id}`, p95 < 200 ms.

---

## 15. Build & Run local

```bash
mvn -pl areas clean verify
docker compose -f ../docker-compose.yml up postgres kafka schema-registry redis keycloak -d
mvn -pl areas spring-boot:run
# Swagger en http://localhost:8081/swagger-ui.html
```

---

## 16. Checklist de implementación

- [ ] Migración Flyway V202601011200__init.sql con extensiones y tablas.
- [ ] Entidades JPA + repos + filtros multi-tenant.
- [ ] Casos de uso `SearchZones`, `ResolvePoint`, `CompareZones`, `RecomputePriceIndex`.
- [ ] Adaptadores REST + DTOs + MapStruct.
- [ ] Consumidores Kafka `TransactionConsumer`, `PropertyConsumer`.
- [ ] Productores Kafka vía Outbox + Debezium.
- [ ] Ingesta nocturna INE / Catastro / CNMC.
- [ ] OpenAPI + stubs publicados.
- [ ] Dashboards Grafana (`areas-overview.json`).
- [ ] Helm chart `charts/areas/`.
