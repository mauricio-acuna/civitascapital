# BUILD STATUS — Módulo `products`

> Documento de seguimiento para continuidad entre sesiones de IA.
> Actualizar este fichero en cada sesión antes de cerrar.
> Última actualización: 2026-05-30 — Sesión 3: Alineación con IDEABASE/inyectados

---

## Leyenda de estados

| Símbolo | Estado |
|---------|--------|
| `[ ]`   | Pendiente — no iniciado |
| `[~]`   | En progreso |
| `[T]`   | Implementado + tests unitarios pasando |
| `[OK]`  | Terminado (impl + tests + revisión) |

---

## BLOQUE 1 — Scaffold del proyecto

| ID  | Tarea | Estado | Notas |
|-----|-------|--------|-------|
| B1-01 | `pom.xml` Maven (Java 21, Spring Boot 3.3, dependencias) | `[OK]` | `products/pom.xml` |
| B1-02 | Estructura de directorios hexagonal | `[OK]` | `src/main/java/com/magenta/products/...` |
| B1-03 | `MagentaProductsApplication.java` | `[OK]` | Puerto 8084 |
| B1-04 | `application.yml` + `application-test.yml` | `[OK]` | Datasource, Redis, OpenSearch, Kafka, clientes |

---

## BLOQUE 2 — Dominio

| ID  | Tarea | Estado | Notas |
|-----|-------|--------|-------|
| B2-01 | Enums: `PropertyType`, `PropertyStatus`, `OperationType`, `OperationStatus`, `MediaKind`, `LeadStatus`, `VisitStatus`, `VisitMode`, `PropertyCondition`, `EnergyLetter`, `Orientation`, `Visibility`, `TransactionSource` | `[OK]` | `domain/model/` |
| B2-02 | Value Objects: `Money`, `GeoPoint`, `Surface`, `Layout`, `EnergyRating`, `OwnerInfo`, `Location`, `FinancingHint`, `IteInfo` | `[OK]` | `domain/model/` |
| B2-03 | Aggregate `Property` (raíz) + invariantes de publicación | `[OK]` | `domain/model/Property.java` |
| B2-04 | Entidad `Operation` | `[OK]` | `domain/model/Operation.java` |
| B2-05 | Entidad `MediaAsset` | `[OK]` | `domain/model/MediaAsset.java` |
| B2-06 | Entidad `Lead` | `[OK]` | `domain/model/Lead.java` |
| B2-07 | Entidad `Visit` | `[OK]` | `domain/model/Visit.java` |
| B2-08 | Entidad `Favorite` | `[OK]` | `domain/model/Favorite.java` |
| B2-09 | Entidad `Transaction` | `[OK]` | `domain/model/Transaction.java` |
| B2-10 | Entidad `PropertyView` (analítica) | `[OK]` | `domain/model/PropertyView.java` |
| B2-11 | Eventos de dominio (`PropertyCreated`, `PropertyPublished`, etc.) — incluye `VisitRequested/Confirmed/Completed`, `LeadQualified/Converted/Lost` | `[OK]` | `domain/event/` — todos creados en sesión 3 |
| B2-12 | Puertos inbound: `PropertyUseCase`, `SearchUseCase`, etc. | `[ ]` | directorio `domain/port/in/` **no existe** |
| B2-13 | Puertos outbound: `PropertyRepository`, `SearchPort`, `EventPublisherPort`, `MediaStoragePort`, `ZoneResolverPort`, `FinancingPort` | `[OK]` | `domain/port/out/` |

---

## BLOQUE 3 — Aplicación (casos de uso)

| ID  | Tarea | Estado | Notas |
|-----|-------|--------|-------|
| B3-01 | `CreatePropertyUseCase` (UC-P1) | `[OK]` | `application/` |
| B3-02 | `PublishPropertyUseCase` (UC-P2) — valida invariantes | `[OK]` | `application/` |
| B3-03 | `UploadMediaUseCase` (UC-P3) | `[OK]` | `application/` |
| B3-04 | `SearchPropertiesUseCase` (UC-P4, UC-P5) | `[~]` | `MatchAffordablePropertiesUseCase` añadido para MVP financiero; falta búsqueda facetada completa |
| B3-05 | `FavoriteUseCase` + `RegisterViewUseCase` + `CreateLeadUseCase` (UC-P6) | `[~]` | `FavoriteUseCase` y `CreateLeadUseCase` ✓; `RegisterViewUseCase` **falta** |
| B3-06 | `ScheduleVisitUseCase` (UC-P7) | `[OK]` | `application/` |
| B3-07 | `UpdateFinancingUseCase` (UC-P8) | `[OK]` | `application/` |
| B3-08 | `RegisterTransactionUseCase` (UC-P9) | `[OK]` | `application/` |
| B3-09 | `ImportFeedUseCase` (UC-P10) | `[ ]` | Pendiente: parser CSV/XML |
| B3-10 | `SyncSearchIndexUseCase` (UC-P11) | `[ ]` | fichero **no existe** |
| B3-11 | `ReportPropertyUseCase` (UC-P12) | `[ ]` | fichero **no existe** |
| B3-12 | `ArchivePropertyUseCase` | `[OK]` | `application/` |
| B3-13 | `MatchingUseCase` (consume preferences event) | `[~]` | motor puro `PropertyAffordabilityMatchService` añadido; falta consumer/event flow |

---

## BLOQUE 4 — Infraestructura: Persistencia

| ID  | Tarea | Estado | Notas |
|-----|-------|--------|-------|
| B4-01 | `PropertyJpaEntity` + mapper | `[OK]` | `infrastructure/adapter/out/persistence/` |
| B4-02 | `OperationJpaEntity` + mapper | `[OK]` | |
| B4-03 | `MediaAssetJpaEntity` + mapper | `[OK]` | |
| B4-04 | `LeadJpaEntity` + mapper | `[ ]` | fichero **no existe** |
| B4-05 | `VisitJpaEntity` + mapper | `[ ]` | fichero **no existe** |
| B4-06 | `FavoriteJpaEntity` + mapper | `[ ]` | fichero **no existe** |
| B4-07 | `TransactionJpaEntity` + mapper | `[ ]` | fichero **no existe** |
| B4-08 | `OutboxEventJpaEntity` + `OutboxEventRepository` | `[OK]` | |
| B4-09 | `PropertyJpaRepository` (Spring Data, consultas JPQL) | `[OK]` | |
| B4-10 | `PropertyRepositoryAdapter` (implementa puerto outbound) | `[OK]` | |
| B4-11 | Multi-tenant filter `@Filter("tenant")` en entidades | `[OK]` | |
| B4-12 | `TenantInterceptor` (inyecta `app.tenant_id` en conexión PG) | `[ ]` | fichero **no existe** |

---

## BLOQUE 5 — Infraestructura: Flyway

| ID  | Tarea | Estado | Notas |
|-----|-------|--------|-------|
| B5-01 | `V202605300001__init_schema.sql` — tablas principales | `[OK]` | PostGIS, tsvector, GIST exclusion |
| B5-02 | `V202605300002__rls_policies.sql` — RLS policies | `[OK]` | |
| B5-03 | `V<ts>__indexes.sql` — índices adicionales GIN/GIST | `[ ]` | fichero **no existe** (los básicos están en init_schema) |
| B5-04 | `V202605300003__outbox.sql` — tabla outbox + processed_event | `[OK]` | existe como V...0003 (no 0004) |
| B5-05 | Partición `property_views` — procedimiento auto-particionado | `[ ]` | Pendiente: pg_partman o cron job |

---

## BLOQUE 6 — Infraestructura: Web (REST)

| ID  | Tarea | Estado | Notas |
|-----|-------|--------|-------|
| B6-01 | `PropertyController` (CRUD + publish + archive) | `[OK]` | `adapter/in/web/` |
| B6-02 | `OperationController` | `[ ]` | **no existe** |
| B6-03 | `MediaController` (multipart upload) | `[ ]` | **no existe** |
| B6-04 | `SearchController` (`/search`, `/search/map`) | `[~]` | `PropertyMatchController` añadido para matching financiero MVP; falta búsqueda facetada/mapa |
| B6-05 | `LeadController` | `[ ]` | **no existe** |
| B6-06 | `VisitController` | `[ ]` | **no existe** |
| B6-07 | `FavoriteController` | `[ ]` | **no existe** |
| B6-08 | `TransactionController` | `[ ]` | **no existe** |
| B6-09 | `ImportController` | `[ ]` | **no existe** |
| B6-10 | DTOs request/response + MapStruct mappers | `[~]` | solo DTOs de `PropertyController` |
| B6-11 | `GlobalExceptionHandler` (RFC 7807 ProblemDetails) | `[OK]` | ✓ |
| B6-12 | `ReportController` (CAPTCHA + rate-limit) | `[ ]` | **no existe** |

---

## BLOQUE 7 — Infraestructura: Kafka

| ID  | Tarea | Estado | Notas |
|-----|-------|--------|-------|
| B7-01 | `PropertyEventProducer` (Outbox → Kafka) | `[OK]` | `adapter/out/kafka/` |
| B7-02 | `OutboxPollingPublisher` (scheduler transaccional) | `[OK]` | |
| B7-03 | `ZoneChangedConsumer` (`magenta.areas.zone.v1`) | `[ ]` | **no existe** |
| B7-04 | `PriceIndexConsumer` (`magenta.areas.price-index.v1`) | `[ ]` | **no existe** |
| B7-05 | `BankProductConsumer` (`magenta.banks.product.v1`) | `[OK]` | ✓ |
| B7-06 | `PreapprovalConsumer` (`magenta.banks.preapproval.v1`) | `[ ]` | **no existe** |
| B7-07 | `CustomerPreferencesConsumer` (`magenta.customers.preferences.v1`) | `[ ]` | **no existe** |
| B7-08 | `CustomerProfileConsumer` (`magenta.customers.profile.v1`) | `[ ]` | **no existe** |
| B7-09 | `ServiciosWorkflowConsumer` (`magenta.servicios.workflow.v1`) | `[ ]` | **no existe** |
| B7-10 | Idempotencia consumidores (`processed_event` table) | `[OK]` | |

---

## BLOQUE 8 — Infraestructura: OpenSearch

| ID  | Tarea | Estado | Notas |
|-----|-------|--------|-------|
| B8-01 | `OpenSearchConfig` (cliente OS) | `[OK]` | `infrastructure/config/` |
| B8-02 | `PropertyIndexDocument` (mapping DTO) | `[OK]` | `adapter/out/search/` |
| B8-03 | `PropertySearchAdapter` (implementa `SearchPort`) | `[OK]` | |
| B8-04 | Index initializer (`properties_v1` mapping) | `[ ]` | **no existe** |
| B8-05 | `SearchQueryBuilder` (filtros + geo + facetas) | `[ ]` | **no existe** |
| B8-06 | `PropertyIndexerConsumer` (Kafka → OS, idempotente) | `[ ]` | **no existe** |

---

## BLOQUE 9 — Infraestructura: Clientes REST

| ID  | Tarea | Estado | Notas |
|-----|-------|--------|-------|
| B9-01 | `AreasClient` (`/zones/resolve`, boundary validation) | `[OK]` | `adapter/out/client/` |
| B9-02 | `BanksClient` (`/financing-feasibility`) | `[OK]` | |
| B9-03 | `CustomersClient` (consulta perfil) | `[ ]` | **no existe** |
| B9-04 | Resilience4j config (CB, Retry, Bulkhead, TimeLimiter 2s) | `[OK]` | ✓ en `application.yml` |
| B9-05 | Headers interceptor (`X-Tenant-Id`, `X-Request-Id`, etc.) | `[ ]` | **no existe** |

---

## BLOQUE 10 — Seguridad

| ID  | Tarea | Estado | Notas |
|-----|-------|--------|-------|
| B10-01 | `SecurityConfig` (JWT RS256 + JWKS Keycloak) | `[OK]` | |
| B10-02 | `@PreAuthorize` en controllers (RBAC) | `[OK]` | |
| B10-03 | `OwnershipCheck` (agente propietario o admin) | `[ ]` | **no existe** |
| B10-04 | Cifrado dirección exacta (AES-256-GCM) hasta publicación | `[ ]` | **no existe** — requiere `AddressEncryptionService` + BYTEA column |
| B10-05 | URL firmada S3 para fotos originales (5 min) | `[ ]` | **no existe** — requiere `S3MediaStorageAdapter` |
| B10-06 | Rate-limit reportes (5/h/IP) con Bucket4j | `[ ]` | **no existe** — depende de `ReportController` |
| B10-07 | Anti-scraping: paginación forzada, headers WAF | `[ ]` | **no existe** — depende de `SearchController` |

---

## BLOQUE 11 — Configuración / Observabilidad

| ID  | Tarea | Estado | Notas |
|-----|-------|--------|-------|
| B11-01 | `application.yml` completo | `[OK]` | |
| B11-02 | `logback-spring.xml` (JSON, campos obligatorios) | `[OK]` | |
| B11-03 | Micrometer + Prometheus (`/actuator/prometheus`) | `[OK]` | |
| B11-04 | OpenTelemetry auto-instrumentation config | `[OK]` | |
| B11-05 | `CacheConfig` (Caffeine local + Redis) | `[OK]` | |
| B11-06 | HikariCP config (30 read / 10 write) | `[OK]` | |

---

## BLOQUE 12 — OpenAPI + Contratos

| ID  | Tarea | Estado | Notas |
|-----|-------|--------|-------|
| B12-01 | Anotaciones SpringDoc OpenAPI 3.1 en controllers | `[~]` | solo `PropertyController` tiene anotaciones |
| B12-02 | `openapi.yaml` generado/guardado | `[ ]` | Requiere arrancar app |
| B12-03 | Pact stubs `products-stubs:1.x` | `[ ]` | Pendiente |

---

## BLOQUE B15 — ALINEACIÓN CON IDEABASE (stack-tech_spec.md)

> Revisión completa del spec en `C:\t\Magenta\IDEABASE\inyectados\stack-tech_spec.md`  
> Se identificaron 2 categorías: **cambios aplicados** y **divergencias de versiones** (requieren decisión).

### Cambios aplicados en esta sesión

| ID   | Cambio | Fichero | Estado |
|------|--------|---------|--------|
| B15-01 | Fix crítico §6.5: HTTP externo fuera de `@Transactional` en `PublishPropertyUseCase` — fases separadas: read-tx → HTTP → write-tx | `application/PublishPropertyUseCase.java` | `[OK]` |
| B15-02 | Fix crítico §6.5: mismo patrón en `UpdateFinancingUseCase` | `application/UpdateFinancingUseCase.java` | `[OK]` |
| B15-03 | Eventos dominio faltantes: `VisitConfirmed`, `VisitCompleted`, `LeadQualified`, `LeadConverted`, `LeadLost` | `domain/event/` | `[OK]` |
| B15-04 | Helm securityContext §9.5: `capabilities.drop: ["ALL"]`, `seccompProfile: RuntimeDefault`, `runAsUser: 10001`, `allowPrivilegeEscalation: false` | `charts/values.yaml` | `[OK]` |
| B15-05 | Helm: startup probe añadida §11.2 | `charts/templates/deployment.yaml` | `[OK]` |
| B15-06 | Helm: `automountServiceAccountToken: false` §9.5 | `charts/templates/deployment.yaml` | `[OK]` |
| B15-07 | Helm: `PodDisruptionBudget` §11.2 | `charts/templates/pdb.yaml` | `[OK]` |
| B15-08 | Helm: `NetworkPolicy` default-deny + allow required flows §9.5 | `charts/templates/networkpolicy.yaml` | `[OK]` |
| B15-09 | Resilience4j completo §10.2: `slidingWindowSize: 50`, `minimumNumberOfCalls: 20`, `slowCallDurationThreshold: 1500ms`, `waitDurationInOpenState: 30s`, `permittedNumberOfCallsInHalfOpenState: 5`, `bulkhead` por dependencia | `application.yml` | `[OK]` |
| B15-10 | Retry §10.1: sólo para excepciones idempotentes (`IOException`, `TimeoutException`), `exponentialBackoffMultiplier: 2` | `application.yml` | `[OK]` |
| B15-11 | Logging estructurado JSON §12.4: `logback-spring.xml` con `logstash-logback-encoder`, campos `traceId/spanId/correlationId/tenantId/userId` | `resources/logback-spring.xml` | `[OK]` |
| B15-12 | Dependencia `logstash-logback-encoder:8.0` añadida | `pom.xml` | `[OK]` |

### ⚠️ DIVERGENCIAS DE VERSIONES — Requieren decisión del equipo

El `ARCHITECTURE.md` del proyecto define versiones distintas a las del `stack-tech_spec.md` de IDEABASE.
**La IA NO ha cambiado las versiones automáticamente** para evitar romper decisiones de arquitectura ya tomadas.

| Componente | `ARCHITECTURE.md` / `pom.xml` actual | `stack-tech_spec.md` (IDEABASE) | Impacto del cambio |
|---|---|---|---|
| Java | 21 LTS | **25 LTS** | Cambio breaking: nueva LTS, Spring Boot 4 requerido |
| Spring Boot | 3.3.5 | **4.0.6+** | Cambio breaking: migración Jakarta EE 11, config cambia |
| Angular | 17 | **21** | FE fuera del módulo products; decisión independiente |
| PostgreSQL | 16 | **18.4+** | Imagen Docker + dialect; bajo riesgo funcional |
| Kafka | 3.7 (Zookeeper) | **4.3.x+ KRaft** | Cambio significativo: sin Zookeeper, nuevo docker-compose |
| Redis | 7.2 | **8.x** | Bajo riesgo; verificar compatibilidad Lettuce |
| Kubernetes | implícito 1.29+ | **1.36** | K8s API versions en Helm templates |

**Para aplicar el upgrade de versiones**: responde "aplica el upgrade de versiones a IDEABASE" y la siguiente sesión ejecutará el cambio en `pom.xml` y `docker-compose.yml`.

---

## BLOQUE 13 — Tests

| ID  | Tarea | Estado | Notas |
|-----|-------|--------|-------|
| B13-01 | `PropertyAggregateTest` (invariantes de publicación) | `[OK]` | |
| B13-02 | `PublishPropertyUseCaseTest` | `[OK]` | |
| B13-03 | `PropertyControllerTest` (`@WebMvcTest`) | `[ ]` | fichero **no existe** |
| B13-04 | `PropertyRepositoryTest` (`@DataJpaTest` + Testcontainers PG) | `[ ]` | Pendiente |
| B13-05 | `SearchAdapterTest` (Testcontainers OpenSearch) | `[ ]` | Pendiente |
| B13-06 | `ArchUnitTest` (reglas hexagonales) | `[OK]` | |
| B13-07 | Integration test Kafka (Testcontainers) | `[ ]` | Pendiente |
| B13-08 | E2E Playwright (buscar → favorito → visita) | `[ ]` | Pendiente (FE) |

---

## BLOQUE 14 — Helm / Docker

| ID  | Tarea | Estado | Notas |
|-----|-------|--------|-------|
| B14-01 | `Dockerfile` multi-stage Distroless | `[ ]` | fichero **no existe** |
| B14-02 | `charts/products/Chart.yaml` | `[OK]` | |
| B14-03 | `charts/products/values.yaml` | `[OK]` | |
| B14-04 | `charts/products/templates/deployment.yaml` | `[OK]` | |
| B14-05 | `charts/products/templates/service.yaml` | `[OK]` | |
| B14-06 | `charts/products/templates/configmap.yaml` | `[OK]` | |
| B14-07 | `charts/products/templates/hpa.yaml` | `[OK]` | |
| B14-08 | `docker-compose.yml` (dev local: PG+PostGIS, OS, Kafka, Redis) | `[OK]` | |

---

## PRÓXIMOS PASOS (para la siguiente sesión)

### ⚠️ DECISIÓN PENDIENTE:
- **¿Aplicar upgrade de versiones a IDEABASE?** (Java 25, Spring Boot 4, Kafka KRaft, PG 18, Redis 8)
  - Si **SÍ**: decir "aplica el upgrade de versiones a IDEABASE"
  - Si **NO**: continuar con Java 21 + Spring Boot 3.3

### PENDIENTES REALES (auditados 2026-05-30):

**ALTA PRIORIDAD — infraestructura de datos faltante:**
- B4-04/05/06/07: `LeadJpaEntity`, `VisitJpaEntity`, `FavoriteJpaEntity`, `TransactionJpaEntity` + adapters
- B4-12: `TenantInterceptor` (SET LOCAL `app.tenant_id`)
- B5-03: Migración de índices adicionales GIN/GIST
- `Flyway V...0005`: corregir schema `processed_event` a PK `(consumer_name, event_id)`

**MEDIA PRIORIDAD — casos de uso faltantes:**
- B3-04: `SearchPropertiesUseCase`
- B3-05: `RegisterViewUseCase` (parcial — faltan en FavoriteUseCase+CreateLeadUseCase ✓)
- B3-09: `ImportFeedUseCase` (CSV/XML)
- B3-10: `SyncSearchIndexUseCase`
- B3-11: `ReportPropertyUseCase`
- B3-13: `MatchingUseCase`
- B2-12: puertos inbound (`domain/port/in/`)

**MEDIA PRIORIDAD — controllers faltantes (8 de 12):**
- B6-02: `OperationController`
- B6-03: `MediaController` + `S3MediaStorageAdapter`
- B6-04: `SearchController` + `SearchQueryBuilder`
- B6-05: `LeadController`
- B6-06: `VisitController`
- B6-07: `FavoriteController`
- B6-08: `TransactionController`
- B6-12: `ReportController` (con Bucket4j rate-limit)

**MEDIA PRIORIDAD — Kafka consumers (6 de 7 faltan):**
- B7-03: `ZoneChangedConsumer`
- B7-04: `PriceIndexConsumer`
- B7-06: `PreapprovalConsumer`
- B7-07: `CustomerPreferencesConsumer`
- B7-08: `CustomerProfileConsumer`
- B7-09: `ServiciosWorkflowConsumer`

**MEDIA PRIORIDAD — OpenSearch:**
- B8-04: Index initializer / mapping creador
- B8-05: `SearchQueryBuilder` (filtros geo + facetas)
- B8-06: `PropertyIndexerConsumer`

**MEDIA PRIORIDAD — Seguridad (sin fichero):**
- B9-03: `CustomersClient`
- B9-05: Headers interceptor (`X-Tenant-Id`, `X-Request-Id`)
- B10-03: `OwnershipCheck`
- B10-04: Cifrado AES-256-GCM dirección (`AddressEncryptionService`)
- B10-05: S3 presigned URLs (`S3MediaStorageAdapter`)
- B10-06: Bucket4j rate-limit filter
- B10-07: Anti-scraping en `SearchController`

**BAJA PRIORIDAD:**
- B5-05: Auto-partición `property_views`
- B12-01: Anotaciones SpringDoc en el resto de controllers
- B12-02: Generar `openapi.yaml`
- B12-03: Pact stubs
- B13-03: `PropertyControllerTest`
- B13-04/05/07: Tests Testcontainers
- B14-01: `Dockerfile` multi-stage Distroless

---

## Convenciones del proyecto

- Paquete base: `com.magenta.products`
- Java 21, Spring Boot 3.3.x, Maven
- Arquitectura hexagonal (domain sin Spring/JPA)
- Puerto: 8084, BD: `products_db`, esquema: `products`
- UUID v7 como PK
- Flyway: `V<YYYYMMDDHHmm>__<descripcion>.sql`
- MapStruct para mapeos DTO ↔ Dominio ↔ JPA
- Todos los eventos via Outbox + CloudEvents 1.0
- Tests: JUnit 5, AssertJ, Mockito, Testcontainers

---

## BLOQUE B16 — MEJORAS PROPUESTAS (revisión 2026-05-30 vs `IDEABASE/inyectados/stack-tech_spec.md`)

> Propuestas no ejecutadas. Promover a tareas concretas antes de implementar.
> Sólo se añaden ítems nuevos; no se modifica nada de B1–B15.

| ID | Estado | Ítem | Razón |
|----|--------|------|-------|
| B16-01 | `[ ]` | **Decisión bloqueante de versiones** (B15 ya la describe): cerrar PAE explicito o mantener Java 21 / Boot 3.3 / PG 16 / Kafka 3.7-ZK con justificación. Hoy `banks`, `customers` y `servicios` ya están en Java 25 / Boot 4 / PG 18 / Kafka KRaft → docker-compose y charts compartidos quedan en estado incoherente. | Coherencia inter-módulo |
| B16-02 | `[ ]` | **Cabecera `MODULE-SPEC.md` y `ARCHITECTURE.md` local desactualizadas** (Java 21 / Boot 3.3). Añadir bloque "Versiones vigentes vs plan IDEABASE" en la cabecera de ambos. | Drift documental |
| B16-03 | `[ ]` | **B6-11 — `GlobalExceptionHandler`**: migrar literal de "RFC 7807" a "RFC 9457" (Spring 6.1+ `ProblemDetail` ya cumple 9457). | Baseline §6 |
| B16-04 | `[ ]` | **Reconsiderar OpenSearch como dependencia día 1** (B8): la baseline recomienda PostgreSQL `tsvector` primero y OpenSearch sólo si se justifica. Dado el peso de B8-04/05/06 pendientes, evaluar entregar **fase 1 de búsqueda con tsvector + GIST geo + filtros JPQL** y posponer OpenSearch a fase 2. | Baseline §3 / §6 |
| B16-05 | `[ ]` | **Capa de búsqueda inexistente** (B3-04, B3-10, B6-04, B7-x, B8-04/05/06): bloqueante de UX. Si se decide B16-04 (tsvector primero), priorizar `SearchPropertiesUseCase` + `SearchController` + `SearchQueryBuilder` (Postgres) por encima del resto. | Producto |
| B16-06 | `[ ]` | **JPA adapters faltantes** (B4-04 a B4-07): Lead/Visit/Favorite/Transaction. Sin ellos no compila el flujo completo (UC-P6, UC-P7, UC-P9). | Bloqueante |
| B16-07 | `[ ]` | **`TenantInterceptor`** (B4-12): RLS PostgreSQL no funciona sin él (`SET LOCAL app.tenant_id = ...`). Crítico de seguridad multi-tenant. | Baseline §7 |
| B16-08 | `[ ]` | **Cifrado AES-256-GCM dirección exacta** (B10-04) hasta publicación: requisito explícito de `MODULE-SPEC.md`. Implementar con envelope encryption (KEK Vault, DEK por registro) + `address_encrypted BYTEA` + visibilidad enum. | Baseline §7.3 |
| B16-09 | `[ ]` | **URLs S3 firmadas TTL ≤ 5 min** (B10-05) para fotos originales; thumbnails públicos vía CDN. | Baseline §7 |
| B16-10 | `[ ]` | **Anti-scraping y rate-limit reportes** (B10-06/07): paginación cursor forzada en search público + Bucket4j 5 reportes/h/IP + WAF. | OWASP A04/A07 |
| B16-11 | `[ ]` | **Consumers Kafka pendientes** (B7-03/04/06/07/08/09 — 6 de 7). Sin ellos `products` no recibe `zone`, `price-index`, `preapproval`, `customer.preferences/profile`, `servicios.workflow`. | Funcional |
| B16-12 | `[ ]` | **Fix `processed_event` PK** = `(consumer_name, event_id)` (anotado en BUILD_STATUS pero no aplicado). Sin esta corrección la idempotencia se rompe si dos consumers procesan el mismo `event_id`. | Baseline §5 |
| B16-13 | `[ ]` | **`Idempotency-Key`** obligatoria en `POST /properties`, `POST /leads`, `POST /visits`, `POST /transactions`, `POST /reports`. | Baseline §6 |
| B16-14 | `[ ]` | **Cursor-based pagination** en `GET /search`, `GET /search/map`, `GET /properties` (hoy offset/Page Spring Data). | Baseline §6 |
| B16-15 | `[ ]` | **`Dockerfile`** (B14-01) inexistente: el módulo no es deployable. Multi-stage Distroless Java 21 (o 25 si B16-01 se aprueba). | Operación |
| B16-16 | `[ ]` | **`livenessProbe` Helm sin PG/Kafka/Redis/OS** (sólo readiness). Verificar `charts/products/templates/deployment.yaml`. | Baseline §11.2 |
| B16-17 | `[ ]` | **Visit `EXCLUDE GIST` constraint** anti-solapes por agente: confirmar que está en `V202605300001__init_schema.sql`; si no, añadir migración. | Producto |
| B16-18 | `[ ]` | **Auto-particionado `property_views`** (B5-05): pg_partman mensual. Sin ello la tabla crece linealmente y mata queries analíticas. | Performance |
| B16-19 | `[ ]` | Test BOLA (OWASP API1 2023): un `agent_A` no debe ver/mutar inmuebles de `agent_B` aunque compartan tenant; un `customer_A` no debe ver favoritos/leads de `customer_B`. | OWASP API1 |
| B16-20 | `[ ]` | Gate CI **OWASP Top 10 2025** + **API Top 10 2023** + ASVS **L2** (L3 en `POST /transactions` y `ReportController`). | Baseline §7 |

---

## BLOQUE B17 — CIERRE, PRODUCCIÓN Y AUDITORÍA (entregables v1.0)

> Documento global: `c:\t\Magenta\HANDOVER.md` cubre la plataforma. Esta sección recoge el **delta específico de `products`** que otra IA u otro equipo necesita para cerrar el módulo, desplegar en producción y superar auditorías OWASP / RGPD (PII en leads/visitas) / antifraude. Promover cada ítem a tarea ejecutable antes de implementar.
>
> ⚠️ **PRECONDICIÓN**: B16-01 (decisión de versiones) debe estar cerrada antes de iniciar este bloque, o quedará desactualizado el día que se haga el upgrade.

### B17.1 Runbook de despliegue producción

| ID | Estado | Ítem | Detalle |
|----|--------|------|---------|
| B17-01 | `[ ]` | **Dockerfile** (B14-01 / B16-15) | Multi-stage Distroless Java 21 (o 25 según B16-01). `USER nonroot:nonroot`, `HEALTHCHECK` ausente (lo provee K8s). |
| B17-02 | `[ ]` | **Orden Flyway** verificado | `V202605300001__init_schema` → migración fix `processed_event PK` (B16-12) → migración `EXCLUDE GIST` visit (B16-17) → `pg_partman` `property_views` (B16-18). |
| B17-03 | `[ ]` | **Secrets en Vault** | `products/db`, `products/s3`, `products/kek-address` (KEK envelope direcciones), `products/cdn-signing-key`, `products/keycloak`. |
| B17-04 | `[ ]` | **Helm values prod** | `charts/products/values-prod.yaml`: réplicas ≥ 3 (alto tráfico de búsqueda), HPA CPU 65 %, PDB, `livenessProbe` sin PG/OS/Kafka/Redis (B16-16), `startupProbe` 90 s, resources requests CPU 500 m / mem 1 Gi. |
| B17-05 | `[ ]` | **OpenSearch / tsvector decisión** (B16-04) | Si fase 1 = Postgres FTS: índice `tsvector` español + GIST geo. Si OpenSearch: `OpenSearchIndexInitializer` con índice `properties_v1` y alias `properties`. |
| B17-06 | `[ ]` | **CDN warm-up** | Pre-poblar CDN con thumbnails de top-1000 inmuebles antes de cutover (`POST /admin/cdn/warmup`). |
| B17-07 | `[ ]` | **Smoke tests post-deploy** | `GET /actuator/health/readiness` 200 · `POST /properties` (con `Idempotency-Key`, MEDIA pendiente OK) 201 · `GET /search?bbox=...` con resultados · `POST /zones/resolve` round-trip a `areas`. |
| B17-08 | `[ ]` | **Rollback** | Helm rollback. Flyway: `pg_partman` y `EXCLUDE GIST` no se rolean automáticamente; documentar procedimiento manual. |
| B17-09 | `[ ]` | **Carga inicial** | `dev/seed.sql` con ≥ 50 inmuebles repartidos en zonas de `areas` para que el matching funcione en sandbox. |

### B17.2 Checklist de auditoría

| ID | Estado | Categoría | Evidencia exigida |
|----|--------|-----------|-------------------|
| B17-10 | `[ ]` | **OWASP ASVS L2** (L3 en `/transactions` y `/reports`) | Reporte ZAP / Semgrep / Snyk en CI; ASVS checklist firmada. |
| B17-11 | `[ ]` | **OWASP API Top 10 2023** | API1 BOLA (agente A vs B, customer A vs B), API4 rate-limit (Bucket4j), API5 BFLA, API8 misconfiguration. |
| B17-12 | `[ ]` | **OWASP Top 10 2025** | A01 control acceso (TenantInterceptor + RLS + Keycloak), A02 cripto (AES-256-GCM dirección), A03 inyección (PreparedStatement, no concatenar tsquery), A07 ident/auth, A09 logging (pista auditoría). |
| B17-13 | `[ ]` | **Anti-scraping** (B16-10) | Bucket4j 5 reportes/h/IP, paginación cursor forzada, rate-limit `/search` 60 rpm/IP, headers WAF (CloudFront/Fastly). |
| B17-14 | `[ ]` | **Cifrado dirección exacta** (B16-08) | Test: `address_encrypted` no descifrable sin DEK; `district`/`zoneId` sí visibles. Visibilidad enum `EXACT/DISTRICT/ZONE_ONLY`. |
| B17-15 | `[ ]` | **URLs S3 firmadas** (B16-09) | Test: URL caduca a los ≤ 5 min, no compartible cross-property. |
| B17-16 | `[ ]` | **Multi-tenant aislamiento** | TenantInterceptor (B16-07) + RLS PostgreSQL (`SET LOCAL app.tenant_id`). Test: query directa con `tenant_id` ajeno → 0 filas. |
| B17-17 | `[ ]` | **PII leads/visitas** (RGPD) | `LeadFormSubmission.email/phone` cifrados; consentimiento explícito; retención 24 meses configurable. |
| B17-18 | `[ ]` | **Pista de auditoría** | `audit_log` para `Property.publish/unpublish`, `price.change`, `Transaction.close`. Retención ≥ 6 años. |
| B17-19 | `[ ]` | **TLS 1.3 + HSTS preload** | Verificado en ingress; headers `Strict-Transport-Security: max-age=63072000; includeSubDomains; preload`. |
| B17-20 | `[ ]` | **Idempotencia eventos** | Tras fix B16-12, test: replay de `magenta.areas.zone.v1` → 0 efectos secundarios duplicados. |

### B17.3 SLOs / SLIs

| SLI | SLO | Medición |
|-----|-----|----------|
| Disponibilidad `GET /search` | 99.9 % mensual | Prometheus |
| Latencia p95 `GET /search` (50 km radio) | < 400 ms | `http_server_requests_seconds{quantile="0.95"}` |
| Latencia p95 `POST /zones/resolve` (sync a `areas`) | < 200 ms | Lo mide cliente Resilience4j |
| Tasa de fallos `POST /zones/resolve` (CB abierto) | < 0.5 % | `resilience4j_circuitbreaker_calls_total{kind="failed"}` |
| Lag consumer `magenta.areas.price-index.v1` | < 5 min (badge no urgente) | `kafka_consumergroup_lag` |
| RPO BD | ≤ 5 min | WAL streaming + S3 horario |
| RTO | ≤ 30 min | Helm + Flyway + restore PG |

### B17.4 Documentación a entregar (índice)

| ID | Estado | Documento | Ubicación |
|----|--------|-----------|-----------|
| B17-21 | `[ ]` | OpenAPI 3.1 publicada | `products/docs/api/openapi.yaml` |
| B17-22 | `[ ]` | Diagrama C4 contexto + contenedor | `products/docs/diagrams/c4-context.puml`, `c4-container.puml` |
| B17-23 | `[ ]` | Modelo de datos (ERD) | `products/docs/diagrams/erd.png` |
| B17-24 | `[ ]` | Diagrama de estados Property | `products/docs/diagrams/property-state.puml` (DRAFT→PUBLISHED→RESERVED→SOLD/RENTED→ARCHIVED) |
| B17-25 | `[ ]` | ADRs | `products/docs/adr/` — ADR de upgrade versiones (B16-01), ADR de tsvector vs OpenSearch (B16-04). |
| B17-26 | `[ ]` | Runbook on-call | `products/docs/ops/runbook.md` (escenarios: areas down, S3 down, OS down, lag price-index, scraping detectado). |
| B17-27 | `[ ]` | Plan de pruebas E2E | `products/docs/qa/e2e-plan.md` |
| B17-28 | `[ ]` | Catálogo de eventos producidos/consumidos | `products/docs/events.md` (envelope CloudEvents + Avro + topic + key + retención). |

### B17.5 Riesgos y deuda técnica

| Riesgo | Impacto | Mitigación |
|--------|---------|------------|
| Decisión B16-01 sin cerrar | Incoherencia plataforma, charts compartidos rotos | Forzar decisión antes de cualquier PR funcional. |
| Sin Dockerfile (B17-01) | Módulo no deployable | Bloqueante v1.0. |
| Sin TenantInterceptor (B16-07) | Filtración cross-tenant | Bloqueante seguridad. |
| Sin AddressEncryption (B16-08) | Fuga dirección exacta de inmuebles privados | Bloqueante v1.0. |
| OpenSearch como dep día 1 | Coste operacional alto antes de tener tráfico | Empezar con tsvector (ADR). |
| `processed_event` PK errónea (B16-12) | Idempotencia rota | Migración fix antes de habilitar consumers. |
| Sin partición `property_views` | Query analítica degrada con tiempo | pg_partman antes de v1.0 o ADR aplazando. |
| Anti-scraping ausente | Coste DB + datos en claro a competencia | Bucket4j + WAF antes de open public search. |

### B17.6 Criterios de aceptación v1.0 (gate de cierre)

Todos `[x]` para declarar `products` como **production-ready**:

- [ ] B16-01 (versiones) cerrada con ADR.
- [ ] B16 ≥ 90 % cerrado (los `[ ]` restantes con ADR justificando aplazamiento).
- [ ] Bloques B1–B14 funcionales cerrados (capa búsqueda, controllers, JPA adapters, consumers, Dockerfile, Helm).
- [ ] Runbook §B17.1 ejecutado con éxito en `staging` (incluido cutover CDN warm-up).
- [ ] Auditoría §B17.2 firmada por responsable seguridad.
- [ ] SLOs §B17.3 monitorizados en Grafana ≥ 7 días.
- [ ] Documentación §B17.4 completa y revisada.
- [ ] `mvn clean verify` + ArchUnit + Pact + Testcontainers verde.
- [ ] Pen-test externo + prueba de carga (≥ 500 rps en `/search`) sin findings críticos/altos.

