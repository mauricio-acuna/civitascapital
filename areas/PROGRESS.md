# areas â€” Estado de implementaciÃ³n

> Plataforma Magenta Â· MÃ³dulo `areas` Â· Puerto 8081
> Stack: Java 21 Â· Spring Boot 3.3 Â· PostgreSQL 16+PostGIS Â· Kafka 3.7 Â· Keycloak 24
> Ãšltima actualizaciÃ³n: 2026-05-30 â€” SesiÃ³n 2 (cascada P2â†’P6: Fase 9 auditada âœ…, processed_message migraciÃ³n, health groups, ARCHITECTURE.md actualizado)

## Leyenda de estados

| SÃ­mbolo | Significado                                           |
|---------|-------------------------------------------------------|
| â¬œ       | Pendiente â€” no empezado                               |
| ðŸ”¨       | En construcciÃ³n â€” ficheros parciales, puede compilar  |
| âœ…       | Terminado â€” cÃ³digo completo, compila sin errores      |
| ðŸ§ª       | Probado â€” tests unitarios/integraciÃ³n pasando         |
| ðŸš€       | Validado en runtime (docker-compose local OK)         |

---

## Fase 1 â€” Estructura Maven + Arranque

| # | Artefacto | Estado | Fichero(s) |
|---|-----------|--------|------------|
| 1.1 | `pom.xml` (Spring Boot 3.3, PostGIS, Kafka, MapStruct, Bucket4j, etc.) | âœ… | `pom.xml` |
| 1.2 | Clase principal `MagentaAreasApplication` | âœ… | `src/main/java/com/magenta/areas/MagentaAreasApplication.java` |
| 1.3 | `application.yml` | âœ… | `src/main/resources/application.yml` |
| 1.4 | `application-local.yml` (Docker Compose overrides) | âœ… | `src/main/resources/application-local.yml` |

---

## Fase 2 â€” Dominio puro (sin Spring/JPA)

| # | Artefacto | Estado | Fichero(s) |
|---|-----------|--------|------------|
| 2.1 | `ZoneType` enum (COUNTRY â†’ BUILDING) | âœ… | `domain/model/ZoneType.java` |
| 2.2 | `ZoneStatus` enum (ACTIVE, DEPRECATED) | âœ… | `domain/model/ZoneStatus.java` |
| 2.3 | `GeoPoint` value object | âœ… | `domain/model/GeoPoint.java` |
| 2.4 | `Money` value object | âœ… | `domain/model/Money.java` |
| 2.5 | `Zone` aggregate root | âœ… | `domain/model/Zone.java` |
| 2.6 | `PriceIndex` entity | âœ… | `domain/model/PriceIndex.java` |
| 2.7 | `ZoneEnrichment` entity | âœ… | `domain/model/ZoneEnrichment.java` |
| 2.8 | `ZoneDemandSnapshot` entity | âœ… | `domain/model/ZoneDemandSnapshot.java` |
| 2.9 | `PropertyType` enum | âœ… | `domain/model/PropertyType.java` |
| 2.10 | `OperationType` enum (SALE, RENT) | âœ… | `domain/model/OperationType.java` |
| 2.11 | `HospitalKind` enum | âœ… | `domain/model/HospitalKind.java` |
| 2.12 | `DepopulationRisk` enum | âœ… | `domain/model/DepopulationRisk.java` |
| 2.13 | `SourceRef` enum | âœ… | `domain/model/SourceRef.java` |

---

## Fase 3 â€” Eventos de dominio

| # | Artefacto | Estado | Fichero(s) |
|---|-----------|--------|------------|
| 3.1 | `ZoneCreated` | âœ… | `domain/event/ZoneCreated.java` |
| 3.2 | `ZoneUpdated` | âœ… | `domain/event/ZoneUpdated.java` |
| 3.3 | `ZoneDeprecated` | âœ… | `domain/event/ZoneDeprecated.java` |
| 3.4 | `PriceIndexPublished` | âœ… | `domain/event/PriceIndexPublished.java` |
| 3.5 | `ZoneEnrichmentUpdated` | âœ… | `domain/event/ZoneEnrichmentUpdated.java` |
| 3.6 | `DomainEvent` base record | âœ… | `domain/event/DomainEvent.java` |

---

## Fase 4 â€” Puertos (interfaces hexagonales)

| # | Artefacto | Estado | Fichero(s) |
|---|-----------|--------|------------|
| 4.1 | `SearchZonesPort` (in) | âœ… | `domain/port/in/SearchZonesPort.java` |
| 4.2 | `GetZonePort` (in) | âœ… | `domain/port/in/GetZonePort.java` |
| 4.3 | `ResolvePointPort` (in) | âœ… | `domain/port/in/ResolvePointPort.java` |
| 4.4 | `CompareZonesPort` (in) | âœ… | `domain/port/in/CompareZonesPort.java` |
| 4.5 | `RecomputePriceIndexPort` (in) | âœ… | `domain/port/in/RecomputePriceIndexPort.java` |
| 4.6 | `ImportGeoJsonPort` (in) | âœ… | `domain/port/in/ImportGeoJsonPort.java` |
| 4.7 | `CreateZonePort` (in) | âœ… | `domain/port/in/CreateZonePort.java` |
| 4.8 | `UpdateZonePort` (in) | âœ… | `domain/port/in/UpdateZonePort.java` |
| 4.9 | `DeleteZonePort` (in) | âœ… | `domain/port/in/DeleteZonePort.java` |
| 4.10 | `GetEnrichmentPort` (in) | âœ… | `domain/port/in/GetEnrichmentPort.java` |
| 4.11 | `UpdateEnrichmentPort` (in) | âœ… | `domain/port/in/UpdateEnrichmentPort.java` |
| 4.12 | `ZoneRepositoryPort` (out) | âœ… | `domain/port/out/ZoneRepositoryPort.java` |
| 4.13 | `PriceIndexRepositoryPort` (out) | âœ… | `domain/port/out/PriceIndexRepositoryPort.java` |
| 4.14 | `EnrichmentRepositoryPort` (out) | âœ… | `domain/port/out/EnrichmentRepositoryPort.java` |
| 4.15 | `DemandRepositoryPort` (out) | âœ… | `domain/port/out/DemandRepositoryPort.java` |
| 4.16 | `OutboxPort` (out) | âœ… | `domain/port/out/OutboxPort.java` |

---

## Fase 5 â€” Casos de uso (application)

| # | Artefacto | Estado | Fichero(s) |
|---|-----------|--------|------------|
| 5.1 | `SearchZonesUseCase` (UC-A1) | âœ… | `application/SearchZonesUseCase.java` |
| 5.2 | `GetZoneUseCase` (UC-A2) | âœ… | `application/GetZoneUseCase.java` |
| 5.3 | `ResolvePointUseCase` (UC-A3) | âœ… | `application/ResolvePointUseCase.java` |
| 5.4 | `GetPriceIndexUseCase` (UC-A4) | âœ… | `application/GetPriceIndexUseCase.java` |
| 5.5 | `GetEnrichmentUseCase` (UC-A5) | âœ… | `application/GetEnrichmentUseCase.java` |
| 5.6 | `CompareZonesUseCase` (UC-A6) | âœ… | `application/CompareZonesUseCase.java` |
| 5.7 | `ImportGeoJsonUseCase` (UC-A7) | âœ… | `application/ImportGeoJsonUseCase.java` |
| 5.8 | `RecomputePriceIndexUseCase` (UC-A8) | âœ… | `application/RecomputePriceIndexUseCase.java` |
| 5.9 | `CreateZoneUseCase` | âœ… | `application/CreateZoneUseCase.java` |
| 5.10 | `UpdateZoneUseCase` | âœ… | `application/UpdateZoneUseCase.java` |
| 5.11 | `DeleteZoneUseCase` | âœ… | `application/DeleteZoneUseCase.java` |
| 5.12 | `UpdateEnrichmentUseCase` | âœ… | `application/UpdateEnrichmentUseCase.java` |

---

## Fase 6 â€” Persistencia (infrastructure/adapter/out/persistence)

| # | Artefacto | Estado | Fichero(s) |
|---|-----------|--------|------------|
| 6.1 | Flyway `V202601011200__init.sql` (tablas + Ã­ndices + RLS) | âœ… | `resources/db/migration/V202601011200__init.sql` |
| 6.2 | `ZoneJpaEntity` | âœ… | `infrastructure/adapter/out/persistence/entity/ZoneJpaEntity.java` |
| 6.3 | `PriceIndexJpaEntity` | âœ… | `infrastructure/adapter/out/persistence/entity/PriceIndexJpaEntity.java` |
| 6.4 | `ZoneEnrichmentJpaEntity` | âœ… | `infrastructure/adapter/out/persistence/entity/ZoneEnrichmentJpaEntity.java` |
| 6.5 | `ZoneDemandSnapshotJpaEntity` | âœ… | `infrastructure/adapter/out/persistence/entity/ZoneDemandSnapshotJpaEntity.java` |
| 6.6 | `OutboxEventJpaEntity` | âœ… | `infrastructure/adapter/out/persistence/entity/OutboxEventJpaEntity.java` |
| 6.7 | `ZoneJpaRepository` (Spring Data) | âœ… | `infrastructure/adapter/out/persistence/ZoneJpaRepository.java` |
| 6.8 | `PriceIndexJpaRepository` | âœ… | `infrastructure/adapter/out/persistence/PriceIndexJpaRepository.java` |
| 6.9 | `ZoneEnrichmentJpaRepository` | âœ… | `infrastructure/adapter/out/persistence/ZoneEnrichmentJpaRepository.java` |
| 6.10 | `ZoneDemandJpaRepository` | âœ… | `infrastructure/adapter/out/persistence/ZoneDemandJpaRepository.java` |
| 6.11 | `OutboxJpaRepository` | âœ… | `infrastructure/adapter/out/persistence/OutboxJpaRepository.java` |
| 6.12 | `ZonePersistenceAdapter` (impl de ZoneRepositoryPort) | âœ… | `infrastructure/adapter/out/persistence/ZonePersistenceAdapter.java` |
| 6.13 | `PriceIndexPersistenceAdapter` | âœ… | `infrastructure/adapter/out/persistence/PriceIndexPersistenceAdapter.java` |
| 6.14 | `EnrichmentPersistenceAdapter` | âœ… | `infrastructure/adapter/out/persistence/EnrichmentPersistenceAdapter.java` |
| 6.15 | `OutboxPersistenceAdapter` | âœ… | `infrastructure/adapter/out/persistence/OutboxPersistenceAdapter.java` |
| 6.16 | `TenantFilter` Hibernate filter + interceptor | âœ… | `infrastructure/config/TenantFilterInterceptor.java` |

> **SESION2ciÃ³n 2026-05-30**: fichero creado en esta sesiÃ³n (estaba marcado âœ… pero no existÃ­a en disco).
| 6.17 | `DemandPersistenceAdapter` (impl de DemandRepositoryPort) | âœ… | `infrastructure/adapter/out/persistence/DemandPersistenceAdapter.java` |

---

## Fase 7 â€” Adaptadores REST (infrastructure/adapter/in/web)

| # | Artefacto | Estado | Fichero(s) |
|---|-----------|--------|------------|
| 7.1 | `ZoneController` (todos los endpoints de /zones) | âœ… | `infrastructure/adapter/in/web/ZoneController.java` |
| 7.2 | `PriceIndexController` | âœ… | `infrastructure/adapter/in/web/PriceIndexController.java` |
| 7.3 | `EnrichmentController` | âœ… | `infrastructure/adapter/in/web/EnrichmentController.java` |
| 7.4 | `CompareController` | âœ… | `infrastructure/adapter/in/web/CompareController.java` |
| 7.5 | `DemandController` | âœ… | `infrastructure/adapter/in/web/DemandController.java` |
| 7.6 | `ZoneRequest` / `ZoneResponse` DTOs | âœ… | `infrastructure/adapter/in/web/dto/` |
| 7.7 | `ZoneDetailResponse` DTO | âœ… | `infrastructure/adapter/in/web/dto/ZoneDetailResponse.java` |
| 7.8 | `ZoneCompareResponse` DTO | âœ… | `infrastructure/adapter/in/web/dto/ZoneCompareResponse.java` |
| 7.9 | `PriceIndexResponse` DTO | âœ… | `infrastructure/adapter/in/web/dto/PriceIndexResponse.java` |
| 7.10 | `ZoneMapper` (MapStruct) | âœ… | `infrastructure/adapter/in/web/mapper/ZoneMapper.java` |
| 7.11 | `GlobalExceptionHandler` (**RFC 9457** `ProblemDetail`) | âœ… | `infrastructure/adapter/in/web/GlobalExceptionHandler.java` |

> **SESION2ciÃ³n 2026-05-30**: ZoneMapper creado en esta sesiÃ³n (estaba marcado âœ… pero no existÃ­a en disco). GlobalExceptionHandler docstring corregido RFC 7807 â†’ RFC 9457.

---

## Fase 8 â€” Kafka producers + consumers (infrastructure/adapter/in-out/kafka)

| # | Artefacto | Estado | Fichero(s) |
|---|-----------|--------|------------|
| 8.1 | `OutboxEventPublisher` (poller + produce Kafka) | âœ… | `infrastructure/adapter/out/kafka/OutboxEventPublisher.java` |
| 8.2 | `ZoneEventProducer` | âœ… | `infrastructure/adapter/out/kafka/ZoneEventProducer.java` |
| 8.3 | `PriceIndexEventProducer` | âœ… | `infrastructure/adapter/out/kafka/PriceIndexEventProducer.java` |

> **SESION2ciÃ³n 2026-05-30**: ZoneEventProducer y PriceIndexEventProducer creados en esta sesiÃ³n (estaban marcados âœ… pero no existÃ­an en disco).
| 8.4 | `TransactionConsumer` (magenta.products.transaction.v1) | âœ… | `infrastructure/adapter/in/kafka/TransactionConsumer.java` |
| 8.5 | `PropertyConsumer` (magenta.products.property.v1) | âœ… | `infrastructure/adapter/in/kafka/PropertyConsumer.java` |
| 8.6 | `ServiciosWorkflowConsumer` (magenta.servicios.workflow.v1) | âœ… | `infrastructure/adapter/in/kafka/ServiciosWorkflowConsumer.java` |
| 8.7 | `ProcessedEventRepository` (idempotencia) | âœ… | `infrastructure/adapter/in/kafka/ProcessedEventRepository.java` |

---

## Fase 9 â€” IngestiÃ³n externa (infrastructure/adapter/out/ingestion)

| # | Artefacto | Estado | Fichero(s) |
|---|-----------|--------|------------|
| 9.1 | `IneIngestionAdapter` (`@Scheduled` nocturno) | âœ… | `infrastructure/adapter/out/ingestion/ine/IneIngestionAdapter.java` |
| 9.2 | `CatastroIngestionAdapter` | âœ… | `infrastructure/adapter/out/ingestion/catastro/CatastroIngestionAdapter.java` |
| 9.3 | `CnmcFiberIngestionAdapter` | âœ… | `infrastructure/adapter/out/ingestion/cnmc/CnmcFiberIngestionAdapter.java` |
| 9.4 | `OsmPoiIngestionAdapter` (hospitales, estaciones) | âœ… | `infrastructure/adapter/out/ingestion/osm/OsmPoiIngestionAdapter.java` |

> **SESION2ciÃ³n 2026-06-01**: Los 4 adaptadores son implementaciones REALES (no stubs). Cada uno tiene `@Scheduled` con cron real, `RestClient` a la API externa y lÃ³gica de parsing + actualizaciÃ³n de dominio. Marcados âœ….

---

## Fase 10 â€” ConfiguraciÃ³n Spring

| # | Artefacto | Estado | Fichero(s) |
|---|-----------|--------|------------|
| 10.1 | `SecurityConfig` (JWT RS256, Keycloak JWKS) | âœ… | `infrastructure/config/SecurityConfig.java` |
| 10.2 | `CacheConfig` (Caffeine + Redis) | âœ… | `infrastructure/config/CacheConfig.java` |
| 10.3 | `KafkaConfig` | âœ… | `infrastructure/config/KafkaConfig.java` |
| 10.4 | `RateLimitConfig` (Bucket4j) | âœ… | `infrastructure/config/RateLimitConfig.java` |
| 10.5 | `OpenApiConfig` (springdoc) | âœ… | `infrastructure/config/OpenApiConfig.java` |
| 10.6 | `ObservabilityConfig` (OTel, Micrometer) | âœ… | `infrastructure/config/ObservabilityConfig.java` |

---

## Fase 11 â€” Tests

| # | Artefacto | Estado | Fichero(s) |
|---|-----------|--------|------------|
| 11.1 | `ZoneDomainTest` (invariantes aggregate) | âœ… | `test/domain/model/ZoneDomainTest.java` |
| 11.2 | `PriceIndexRecomputeTest` (cÃ¡lculo Â§9.1) | âœ… | `test/domain/model/PriceIndexRecomputeTest.java` |
| 11.3 | `SearchZonesUseCaseTest` | âœ… | `test/application/SearchZonesUseCaseTest.java` |
| 11.4 | `CompareZonesUseCaseTest` | âœ… | `test/application/CompareZonesUseCaseTest.java` |
| 11.5 | `ZonePersistenceAdapterIT` (@DataJpaTest + Testcontainers PostGIS) | âœ… | `test/infrastructure/persistence/ZonePersistenceAdapterIT.java` |
| 11.6 | `ZoneControllerTest` (@WebMvcTest) | âœ… | `test/infrastructure/web/ZoneControllerTest.java` |
| 11.7 | Spring Cloud Contract stubs | âœ… | `test/resources/contracts/` (5 contratos: SearchZones, GetZoneById, GetZoneById_NotFound, GetLatestPriceIndex, CompareZones) |
| 11.8 | ArchUnit hexagonal rules | âœ… | `test/arch/HexagonalArchitectureTest.java` |

> âœ… Tests unitarios (11.1â€“11.4) implementados. Tests de integraciÃ³n (11.5â€“11.8) implementados â€” requieren Docker para ejecutarse.

---

## Fase 12 â€” Ops

| # | Artefacto | Estado | Fichero(s) |
|---|-----------|--------|------------|
| 12.1 | `Dockerfile` multi-stage Distroless | âœ… | `Dockerfile` |
| 12.2 | Helm chart `charts/areas/` (deployment + service + sa + hpa + pdb + netpol) | âœ… | `charts/areas/` |
| 12.3 | Grafana dashboard `areas-overview.json` | âœ… | `ops/grafana/areas-overview.json` |
| 12.4 | Alertas Prometheus `areas-alerts.yaml` | âœ… | `ops/prometheus/areas-alerts.yaml` |

---

---

## Fase 13 â€” AlineaciÃ³n stack-tech_spec.md (inyectados)

| # | Ãtem | Estado | Cambio |
|---|------|--------|--------|
| 13.1 | `DemandPersistenceAdapter` creado (componente faltante) | âœ… | `infrastructure/adapter/out/persistence/DemandPersistenceAdapter.java` |
| 13.2 | `application.yml`: `connection-timeout` 30000â†’2000, `ddl-auto: none`, `show-details: when-authorized` | âœ… | `application.yml` |
| 13.3 | `application.yml`: `forward-headers-strategy: framework` + `TRACE_SAMPLE_RATE:0.1` + `correlationId` en logs | âœ… | `application.yml` |
| 13.4 | `KafkaConfig`: `ExponentialBackOff` + `DeadLetterPublishingRecoverer` (â†’ DLT topics) | âœ… | `infrastructure/config/KafkaConfig.java` |
| 13.5 | Helm `deployment.yaml`: container `securityContext` (`allowPrivilegeEscalation: false`, `readOnlyRootFilesystem: true`, `capabilities drop ALL`, `seccompProfile RuntimeDefault`) + `/tmp` emptyDir | âœ… | `charts/areas/templates/deployment.yaml` |
| 13.6 | Helm `values.yaml`: `startupProbe` (15s inicial, period 5s, failureThreshold 24) | âœ… | `charts/areas/values.yaml` |
| 13.7 | Helm `_helpers.tpl` (fullname, labels, selectorLabels) | âœ… | `charts/areas/templates/_helpers.tpl` |
| 13.8 | Helm `service.yaml` (ClusterIP) | âœ… | `charts/areas/templates/service.yaml` |
| 13.9 | Helm `serviceaccount.yaml` (automountServiceAccountToken: false) | âœ… | `charts/areas/templates/serviceaccount.yaml` |
| 13.10 | Helm `hpa.yaml` (autoscaling/v2 CPU) | âœ… | `charts/areas/templates/hpa.yaml` |
| 13.11 | Helm `pdb.yaml` (minAvailable: 1) | âœ… | `charts/areas/templates/pdb.yaml` |
| 13.12 | Helm `networkpolicy.yaml` (ingress desde gateway/monitoring, egress a PG/Redis/Kafka/Keycloak/OTel/DNS) | âœ… | `charts/areas/templates/networkpolicy.yaml` |
| 13.13 | ADR-001: justificaciÃ³n de versiones Java 21/Boot 3.3/PG 16/Kafka 3.7 vs recomendaciÃ³n spec | âœ… | `docs/adr/ADR-001-stack-versions.md` |

> **Pendiente de ADR**: PAE-001 plan de actualizaciÃ³n a Java 25 / Spring Boot 4 / PG 18 / Kafka 4 (v2.0)

---

## Notas para continuar en otra sesiÃ³n

1. **CompilaciÃ³n**: `cd areas && mvn clean verify -DskipTests` â€” **paso obligatorio antes de seguir**.
2. **Tests unitarios + ArchUnit**: `mvn test` (no requieren Docker).
3. **Tests de integraciÃ³n**: `mvn verify -Pit` (requieren Docker; imagen `postgis/postgis:16-3.4`).
4. **Runtime local**: `docker compose up postgres kafka schema-registry redis keycloak -d && mvn spring-boot:run -Dspring-boot.run.profiles=local`
5. **Swagger**: `http://localhost:8081/swagger-ui.html`
6. **PrÃ³ximos pasos** (no hay â¬œ de cÃ³digo â€” todo implementado):
   - âœ… Verificar compilaciÃ³n limpia (`mvn clean verify -DskipTests`).
   - âœ… Verificar tests unitarios + ArchUnit (`mvn test`).
   - Ejecutar tests de integraciÃ³n (requieren Docker).
   - PAE-001: plan de actualizaciÃ³n Java 25 / Spring Boot 4 / PG 18 (v2.0) â€” ver `docs/adr/ADR-001-stack-versions.md`.
7. **Variables de entorno que requieren secreto** (no en git):
   - `DB_USER`, `DB_PASSWORD` â†’ Kubernetes Secret `areas-db-secret`
   - `KEYCLOAK_JWKS_URI` â†’ URL del realm Keycloak
   - `KAFKA_BOOTSTRAP_SERVERS`, `REDIS_HOST`, `OTLP_ENDPOINT`

---

## Fase 14 â€” Mejoras propuestas (revisiÃ³n 2026-05-30 vs `IDEABASE/inyectados/stack-tech_spec.md`)

> SÃ³lo propuestas. No tocar lo cerrado en Fases 1â€“13. Cada Ã­tem se promueve a tarea concreta
> antes de ejecutarse. Origen: revisiÃ³n cruzada con la baseline 30/05/2026.

| # | Estado | Ãtem | RazÃ³n |
|---|--------|------|-------|
| 14.1 | ðŸ”¨ | **PAE-001 â€” decisiÃ³n parcial 2026-05-30**: `docker-compose.yml` migrado a PG 18.4+PostGIS 3.5, Redis 8, Kafka 4.3 KRaft (Confluent 8.0.0), Schema Registry 8.0.0. `pom.xml` permanece en Java 21/Boot 3.3 (ADR-001 v1.0). ActualizaciÃ³n completa planificada para v2.0 (Q4 2026/Q1 2027). Ver ADR-001. | Coherencia inter-mÃ³dulo |
| 14.2 | âœ… | `MODULE-SPEC.md` ya tenÃ­a el bloque ADR-001/PAE-001. `ARCHITECTURE.md` actualizado con cabecera de versiones vigentes + referencias a ADR-001/ADR-002. | Drift documental |
| 14.3 | âœ… | `GlobalExceptionHandler`: ya usa `org.springframework.http.ProblemDetail` (Spring 6.1+, RFC 9457). Docstring corregido en esta sesiÃ³n. | Baseline Â§6 |
| 14.4 | âœ… | `docker-compose.yml` creado en esta sesiÃ³n con **KRaft** nativo (Confluent 8.0.0, sin ZooKeeper). CLUSTER_ID: `QWxwaGFBcmVhc0NsdXN0ZXI`. | Compatibilidad |
| 14.5 | âœ… | Helm `livenessProbe` ya apuntaba a `/actuator/health/liveness`. AÃ±adido en `application.yml`: `probes.enabled: true` + health groups explÃ­citos (`liveness: livenessState`; `readiness: readinessState,db,redis,kafka`). | Baseline Â§11.2 |
| 14.6 | âœ… | Fase 9 auditada 2026-06-01: los 4 adaptadores son implementaciones reales (`@Scheduled` + `RestClient` + parsing + persistencia). Estado corregido ðŸ”¨â†’âœ…. | Veracidad del tracker |
| 14.7 | âœ… | PaginaciÃ³n auditada 2026-06-01: `/zones/search` usa `?limit` (apropiado para autocompletar), `/price-indices` usa rango de fechas (apropiado para series temporales). NingÃºn endpoint usa `Page<>` offset que requiera conversiÃ³n. | Baseline Â§6 paginaciÃ³n |
| 14.8 | âœ… | **Bug corregido 2026-06-01**: `isAlreadyProcessed()` sÃ³lo filtraba por `event_id`, causando contaminaciÃ³n entre consumidores. Fix: migraciÃ³n `V202601011201__idempotency_consumer_scoping.sql` (aÃ±ade `consumer_name VARCHAR(120)`, PK compuesta `(consumer_name, event_id)`). `ProcessedEventRepository` actualizado a firma 3-arg. Los 3 consumers actualizados con sus nombres canÃ³nicos (`areas-transaction-consumer`, `areas-property-consumer`, `areas-servicios-consumer`). | Outbox + idempotencia |
| 14.9 | ✅ | **Gate CI implementado 2026-06-01**: OWASP `dependency-check-maven:10.0.4` (falla en CVSSv3 ≥ 7), JaCoCo 0.8.12 (≥ 70 % cobertura de líneas en dominio + application), Surefire (excluye `*IT`), Failsafe (integración). GitHub Actions workflow en `.github/workflows/areas-ci.yml` con 4 jobs: unit-tests, integration-tests, owasp-dependency-check, coverage-gate. | Baseline §7 |
| 14.10 | ✅ | **Tests BOLA implementados 2026-06-01**: `ZoneControllerBOLATest` (5 escenarios: 401 anónimo, 403 rol insuficiente, 404 tenant ajeno en PUT, 404 tenant ajeno en DELETE, 200 tenant correcto) + `EnrichmentControllerBOLATest` (3 escenarios). | Baseline §7 BOLA |





