# Módulo `customers` — Estado de construcción

> Fecha inicio: 2026-05-30  
> Última actualización: 2026-05-30  
> IA activa: GitHub Copilot (Claude Sonnet 4.6)

## Leyenda de estados

| Símbolo | Estado |
|---------|--------|
| `[ ]` | Pendiente |
| `[~]` | En progreso |
| `[x]` | Implementado (sin tests) |
| `[T]` | Con tests unitarios |
| `[✓]` | Completo (implementado + tests + revisado) |

---

## Fase 1 — Andamiaje del proyecto

| Estado | Item | Fichero(s) clave |
|--------|------|-----------------|
| `[✓]` | PROGRESS.md (este fichero) | `PROGRESS.md` |
| `[x]` | `pom.xml` con todas las dependencias | `pom.xml` |
| `[x]` | Clase principal `MagentaCustomersApplication` | `src/main/java/.../MagentaCustomersApplication.java` |
| `[x]` | `application.yml` + `application-docker.yml` | `src/main/resources/application.yml` |
| `[x]` | `SecurityConfig` — Keycloak JWT RS256 | `infrastructure/config/SecurityConfig.java` |
| `[x]` | `MultiTenantFilter` — inyección `app.tenant_id` | `infrastructure/config/MultiTenantFilter.java` |
| `[x]` | `GlobalExceptionHandler` — RFC 7807 Problem Details | `infrastructure/adapter/in/web/GlobalExceptionHandler.java` |
| `[x]` | `OpenApiConfig` — springdoc | `infrastructure/config/OpenApiConfig.java` |
| `[x]` | `AwsConfig` — beans S3Client + S3Presigner | `infrastructure/config/AwsConfig.java` |
| `[x]` | `KafkaConfig` — bean KafkaTemplate | `infrastructure/config/KafkaConfig.java` |
| `[x]` | `RestClientConfig` — bean RestClient.Builder | `infrastructure/config/RestClientConfig.java` |

---

## Fase 2 — Dominio (paquete `domain/`)

### Modelo de dominio

| Estado | Clase | Fichero |
|--------|-------|---------|
| `[x]` | `Customer` (aggregate root) | `domain/model/Customer.java` |
| `[x]` | `CustomerType` enum | `domain/model/CustomerType.java` |
| `[x]` | `CustomerStatus` enum | `domain/model/CustomerStatus.java` |
| `[x]` | `IndividualProfile` | `domain/model/IndividualProfile.java` |
| `[x]` | `ProfessionalProfile` | `domain/model/ProfessionalProfile.java` |
| `[x]` | `ContractType` enum | `domain/model/ContractType.java` |
| `[x]` | `LegalEntityProfile` | `domain/model/LegalEntityProfile.java` |
| `[x]` | `UltimateBeneficialOwner` | `domain/model/UltimateBeneficialOwner.java` |
| `[x]` | `HouseholdProfile` | `domain/model/HouseholdProfile.java` |
| `[x]` | `HouseholdMember` | `domain/model/HouseholdMember.java` |
| `[x]` | `HouseholdRole` enum | `domain/model/HouseholdRole.java` |
| `[x]` | `FinancialSnapshot` | `domain/model/FinancialSnapshot.java` |
| `[x]` | `ComputedAffordability` | `domain/model/ComputedAffordability.java` |
| `[x]` | `KycState` | `domain/model/KycState.java` |
| `[x]` | `KycStatus` enum | `domain/model/KycStatus.java` |
| `[x]` | `KycProvider` enum | `domain/model/KycProvider.java` |
| `[x]` | `SearchPreference` | `domain/model/SearchPreference.java` |
| `[x]` | `OperationType` enum | `domain/model/OperationType.java` |
| `[x]` | `AlertChannel` enum | `domain/model/AlertChannel.java` |
| `[x]` | `AlertFrequency` enum | `domain/model/AlertFrequency.java` |
| `[x]` | `DocumentRef` | `domain/model/DocumentRef.java` |
| `[x]` | `DocumentKind` enum | `domain/model/DocumentKind.java` |
| `[x]` | `ValidationStatus` enum | `domain/model/ValidationStatus.java` |
| `[x]` | `Consent` (RGPD) | `domain/model/Consent.java` |
| `[x]` | `PostalAddress` VO | `domain/model/PostalAddress.java` |

### Eventos de dominio

| Estado | Evento | Fichero |
|--------|--------|---------|
| `[x]` | `DomainEvent` (interfaz base) | `domain/event/DomainEvent.java` |
| `[x]` | `CustomerCreated` | `domain/event/CustomerCreated.java` |
| `[x]` | `ProfileUpdated` | `domain/event/ProfileUpdated.java` |
| `[x]` | `FinancialSnapshotPublished` | `domain/event/FinancialSnapshotPublished.java` |
| `[x]` | `KycVerified` | `domain/event/KycVerified.java` |
| `[x]` | `KycRejected` | `domain/event/KycRejected.java` |
| `[x]` | `KycExpired` | `domain/event/KycExpired.java` |
| `[x]` | `SearchPreferenceCreated` | `domain/event/SearchPreferenceCreated.java` |
| `[x]` | `SearchPreferenceDeleted` | `domain/event/SearchPreferenceDeleted.java` |
| `[x]` | `ConsentGranted` | `domain/event/ConsentGranted.java` |
| `[x]` | `ConsentRevoked` | `domain/event/ConsentRevoked.java` |
| `[x]` | `ErasureRequested` | `domain/event/ErasureRequested.java` |

### Puertos (interfaces)

| Estado | Puerto | Fichero |
|--------|--------|---------|
| `[x]` | `CustomerRepository` (outbound) | `domain/port/out/CustomerRepository.java` |
| `[x]` | `FinancialSnapshotRepository` (outbound) | `domain/port/out/FinancialSnapshotRepository.java` |
| `[x]` | `DocumentRepository` (outbound) | `domain/port/out/DocumentRepository.java` |
| `[x]` | `SearchPreferenceRepository` (outbound) | `domain/port/out/SearchPreferenceRepository.java` |
| `[x]` | `ConsentRepository` (outbound) | `domain/port/out/ConsentRepository.java` |
| `[x]` | `EventPublisher` (outbound) | `domain/port/out/EventPublisher.java` |
| `[x]` | `PiiCryptoPort` (outbound) | `domain/port/out/PiiCryptoPort.java` |
| `[x]` | `DocumentStoragePort` (outbound) | `domain/port/out/DocumentStoragePort.java` |
| `[x]` | `KycProviderPort` (outbound) | `domain/port/out/KycProviderPort.java` |
| `[x]` | `KeycloakPort` (outbound) | `domain/port/out/KeycloakPort.java` |
| `[x]` | `AffordabilityPort` (inbound) | `domain/port/in/AffordabilityPort.java` |

---

## Fase 3 — Infraestructura: Persistencia

| Estado | Item | Fichero |
|--------|------|---------|
| `[x]` | Flyway V1 — schema completo con RLS | `resources/db/migration/V202405300001__init_customers_schema.sql` |
| `[x]` | `CustomerJpaEntity` | `infrastructure/adapter/out/persistence/entity/CustomerJpaEntity.java` |
| `[x]` | `IndividualProfileJpaEntity` | `infrastructure/adapter/out/persistence/entity/IndividualProfileJpaEntity.java` |
| `[x]` | `LegalEntityProfileJpaEntity` | `infrastructure/adapter/out/persistence/entity/LegalEntityProfileJpaEntity.java` |
| `[x]` | `HouseholdJpaEntity` | `infrastructure/adapter/out/persistence/entity/HouseholdJpaEntity.java` |
| `[x]` | `FinancialSnapshotJpaEntity` | `infrastructure/adapter/out/persistence/entity/FinancialSnapshotJpaEntity.java` |
| `[x]` | `KycStateJpaEntity` | `infrastructure/adapter/out/persistence/entity/KycStateJpaEntity.java` |
| `[x]` | `SearchPreferenceJpaEntity` | `infrastructure/adapter/out/persistence/entity/SearchPreferenceJpaEntity.java` |
| `[x]` | `DocumentJpaEntity` | `infrastructure/adapter/out/persistence/entity/DocumentJpaEntity.java` |
| `[x]` | `ConsentJpaEntity` | `infrastructure/adapter/out/persistence/entity/ConsentJpaEntity.java` |
| `[x]` | `OutboxEventJpaEntity` | `infrastructure/adapter/out/persistence/entity/OutboxEventJpaEntity.java` |
| `[x]` | Spring Data repos (JPA) | `infrastructure/adapter/out/persistence/jpa/*.java` |
| `[x]` | `CustomerRepositoryAdapter` | `infrastructure/adapter/out/persistence/CustomerRepositoryAdapter.java` |
| `[x]` | `FinancialSnapshotRepositoryAdapter` | `infrastructure/adapter/out/persistence/FinancialSnapshotRepositoryAdapter.java` |
| `[x]` | `DocumentRepositoryAdapter` | `infrastructure/adapter/out/persistence/DocumentRepositoryAdapter.java` |
| `[x]` | `SearchPreferenceRepositoryAdapter` | `infrastructure/adapter/out/persistence/SearchPreferenceRepositoryAdapter.java` |
| `[x]` | `ConsentRepositoryAdapter` | `infrastructure/adapter/out/persistence/ConsentRepositoryAdapter.java` |

---

## Fase 4 — Casos de uso (application/)

| Estado | Caso de uso | Clase | UC |
|--------|-------------|-------|----|
| `[x]` | Registro persona física | `RegisterIndividualUseCase` | UC-C1 |
| `[x]` | Alta persona jurídica | `RegisterLegalEntityUseCase` | UC-C2 |
| `[x]` | Crear unidad familiar | `CreateHouseholdUseCase` | UC-C3 |
| `[x]` | Guardar perfil financiero | `SaveFinancialSnapshotUseCase` | UC-C4 |
| `[x]` | Subir documentación | `UploadDocumentUseCase` | UC-C5 |
| `[x]` | Ejecutar KYC | `StartKycUseCase` + `ProcessKycCallbackUseCase` | UC-C6 |
| `[x]` | Guardar búsqueda + alerta | `SaveSearchPreferenceUseCase` | UC-C7 |
| `[x]` | Matching de inmuebles (evento) | `PropertyMatchingService` | UC-C8 |
| `[x]` | Derechos RGPD | `RgpdExportUseCase` + `RgpdErasureUseCase` | UC-C9 |
| `[x]` | Calcular capacidad hipotecaria | `AffordabilityService` | UC-C10 |
| `[x]` | Compartir perfil con banco | `ShareProfileWithBankUseCase` | UC-C11 |

---

## Fase 5 — REST API (infrastructure/adapter/in/web/)

| Estado | Controller | Endpoints cubiertos |
|--------|-----------|---------------------|
| `[x]` | `IndividualController` | POST /customers/individuals (público) |
| `[x]` | `CustomerController` | GET/{id}, PATCH/{id}, DELETE/{id}, POST /legal-entities, POST /households |
| `[x]` | `FinancialController` | POST/GET /{id}/financial-snapshots, GET /{id}/financial-profile, GET /{id}/affordability, POST /{id}/share-with-bank |
| `[x]` | `DocumentController` | POST/GET /{id}/documents |
| `[x]` | `KycController` | POST /{id}/kyc/start, POST /{id}/kyc/callback, GET /{id}/kyc |
| `[x]` | `SearchPreferenceController` | POST/GET /{id}/search-preferences |
| `[x]` | `ConsentController` | POST/DELETE/GET /{id}/consents |
| `[x]` | `RgpdController` | POST /{id}/rgpd/export, POST /{id}/rgpd/erasure |
| `[ ]` | `HouseholdMemberController` | POST /{id}/household-members (endpoint en spec §5, pendiente) |

---

## Fase 6 — Mensajería Kafka

| Estado | Item | Fichero |
|--------|------|---------|
| `[x]` | `OutboxEventPublisher` — transactional outbox | `infrastructure/adapter/out/kafka/OutboxEventPublisher.java` |
| `[x]` | `OutboxPollingJob` — scheduler de publicación | `infrastructure/adapter/out/kafka/OutboxPollingJob.java` |
| `[x]` | `PropertyEventConsumer` (products → matching) | `infrastructure/adapter/in/kafka/PropertyEventConsumer.java` |
| `[x]` | `PreapprovalEventConsumer` (banks → timeline) | `infrastructure/adapter/in/kafka/PreapprovalEventConsumer.java` |
| `[x]` | `ZoneEventConsumer` (areas → rewrite zone_id) | `infrastructure/adapter/in/kafka/ZoneEventConsumer.java` |
| `[ ]` | `TransactionEventConsumer` (products → histórico adquisiciones) | `infrastructure/adapter/in/kafka/TransactionEventConsumer.java` |

---

## Fase 7 — Clientes externos

| Estado | Cliente | Fichero |
|--------|---------|---------|
| `[x]` | `AreasRestClient` | `infrastructure/adapter/out/client/AreasRestClient.java` |
| `[x]` | `BanksRestClient` | `infrastructure/adapter/out/client/BanksRestClient.java` |
| `[ ]` | `ProductsRestClient` | `infrastructure/adapter/out/client/ProductsRestClient.java` |
| `[x]` | `KeycloakAdminAdapter` | `infrastructure/adapter/out/client/KeycloakAdminAdapter.java` |
| `[x]` | `KycProviderAdapter` (IDNow) | `infrastructure/adapter/out/client/KycProviderAdapter.java` |
| `[x]` | `VaultPiiCryptoAdapter` | `infrastructure/adapter/out/crypto/VaultPiiCryptoAdapter.java` |
| `[x]` | `S3DocumentStorageAdapter` | `infrastructure/adapter/out/storage/S3DocumentStorageAdapter.java` |

---

## Fase 8 — Tests

| Estado | Suite | Fichero |
|--------|-------|---------|
| `[x]` | Unit — `NifValidatorTest` | `test/.../domain/NifValidatorTest.java` |
| `[x]` | Unit — `CifValidatorTest` | `test/.../domain/CifValidatorTest.java` |
| `[x]` | Unit — `AffordabilityServiceTest` | `test/.../application/AffordabilityServiceTest.java` |
| `[ ]` | Unit — `ConfidenceCalculatorTest` | `test/.../application/ConfidenceCalculatorTest.java` |
| `[ ]` | Slice — `@DataJpaTest` CustomerRepo | `test/.../persistence/CustomerRepositoryTest.java` |
| `[ ]` | Integration — `RegisterIndividualIT` | `test/.../RegisterIndividualIT.java` |
| `[ ]` | Integration — `KycCallbackIT` | `test/.../KycCallbackIT.java` |
| `[ ]` | Integration — `RgpdErasureIT` | `test/.../RgpdErasureIT.java` |
| `[ ]` | Contract (Pact) — `CustomersPactTest` | `test/.../pact/CustomersPactTest.java` |
| `[x]` | ArchUnit — `HexagonalArchTest` | `test/.../arch/HexagonalArchTest.java` |

---

## Fase 9 — Operaciones

| Estado | Item | Fichero |
|--------|------|---------|
| `[x]` | `Dockerfile` multi-stage Distroless | `Dockerfile` |
| `[x]` | `docker-compose.yml` (dev local) | `docker-compose.yml` |
| `[x]` | Helm chart `charts/customers/` | `charts/customers/Chart.yaml` etc. |
| `[ ]` | Grafana dashboard JSON | `ops/grafana/customers-dashboard.json` |

---

## Notas de continuidad para siguiente sesión

### Última posición (2026-05-30 — Sesión 2)
- **Alineación**: Stack actualizado a baseline `inyectados/stack-tech_spec.md` (30/05/2026)
- **Pendientes inmediatos** (en orden de prioridad):
  1. `ProductsRestClient` — cliente Resilience4j hacia `products:8084`
  2. `TransactionEventConsumer` — consumer `magenta.products.transaction.v1`
  3. `HouseholdMemberController` — POST `/{id}/household-members`
  4. Tests integración Testcontainers (`RegisterIndividualIT`, `KycCallbackIT`, `RgpdErasureIT`)
  5. `ConfidenceCalculatorTest` (unit)
  6. `@DataJpaTest` `CustomerRepositoryTest`
  7. Pact consumer `CustomersPactTest`
  8. Grafana dashboard JSON (`ops/grafana/customers-dashboard.json`)

### Decisiones tomadas
1. UUID v7 vía `UuidCreator.getTimeOrderedEpoch()` (librería `uuid-creator`).
2. `PiiCryptoPort` usa Vault Transit API via `spring-cloud-vault`.
3. Outbox polling cada 5 s con `@Scheduled`, no Debezium (simplificación fase inicial).
4. KYC callback valida HMAC-SHA256 `X-KYC-Signature` header.
5. `FinancialSnapshot.confidence` calculado en `ConfidenceCalculator` service.
6. `maxPaymentBdE` = max(0, netIncome × 0.40 − otherDebt); `maxPaymentInternal` = 0.30.
7. MapStruct para todos los mapeos DTO ↔ Domain ↔ JPA entity.

### Dependencias externas requeridas (no mockeadas en dev)
- PostgreSQL **18** en `localhost:5432`
- Keycloak **26** en `localhost:8080`
- Vault en `localhost:8200`
- S3/MinIO en `localhost:9000`
- Kafka **4.3** en `localhost:9092`

### Comandos útiles
```bash
# Compilar + tests unitarios
mvn test -pl customers

# Levantar infraestructura dev
docker-compose -f docker-compose.yml up -d postgres kafka vault minio keycloak

# Build imagen
docker build -t magenta/customers:latest .

# Flyway info
mvn flyway:info -pl customers
```

---

## Fase 10 — Mejoras propuestas (revisión 2026-05-30 vs `IDEABASE/inyectados/stack-tech_spec.md`)

> Propuestas no ejecutadas. Promover a tareas concretas antes de implementar.

| Estado | Ítem | Razón |
|--------|------|-------|
| `[ ]` | **`ARCHITECTURE.md` local** sigue siendo v1.0 con stack viejo (Java 21 / Boot 3.3 / PG 16) aunque la `MODULE-SPEC.md` y el código ya están alineados a Java 25 / Boot 4.0 / PG 18 / Kafka 4.3 / Keycloak 26. Sustituir o marcar como obsoleto remitiendo a un `ARCHITECTURE.md` canónico. | Drift documental |
| `[ ]` | **F1 — sustituir "RFC 7807" por "RFC 9457"** en `GlobalExceptionHandler` (Spring 6.1+ `ProblemDetail` ya cumple 9457). | Baseline §6 |
| `[ ]` | **Crypto-shredding RGPD**: confirmar que `RgpdErasureUseCase` rota la DEK por registro (no sólo borra fila ni anonimiza); hash determinista (HMAC-SHA256 con pepper) en `nif_hash` para que la búsqueda siga funcionando tras shredding. | Baseline §7.3 |
| `[ ]` | **PII en logs**: añadir masker explícito en `logback-spring.xml` para `nif`, `iban`, `email`, `phone`, `score`, `income` (regex MDC filter). Verificar que no hay `log.info("customer={}", customer)` con toString ingenuo. | Baseline §7.3 |
| `[ ]` | **OWASP API1 BOLA**: tests de autorización con dos `customerId` del mismo tenant — `customer_A` no debe poder leer ni mutar `customer_B` aunque comparta `tenant_id`. Cobertura por endpoint, no sólo por servicio. | OWASP API1 2023 |
| `[ ]` | **`Idempotency-Key`** obligatoria en `POST /customers/individuals`, `POST /financial-snapshots`, `POST /kyc/start`, `POST /share-with-bank`. Persistir respuesta y reproducirla ante reintento. | Baseline §6 |
| `[ ]` | **Tabla `processed_message(consumer_name, event_id)`** ya implícita en consumers, dejarla explícita en migración (V…___processed_message.sql) para que la idempotencia sea visible. | Baseline §5 |
| `[ ]` | Paginación **cursor-based** en `GET /search-preferences`, `GET /documents`, `GET /consents` y `GET /financial-snapshots`. | Baseline §6 |
| `[ ]` | Validar **`open-in-view: false`**, `forward-headers-strategy: framework`, `server.shutdown: graceful`, `spring.mvc.problem-details.enabled: true`. | Baseline §11 |
| `[ ]` | Helm chart `charts/customers/`: `securityContext` restricted completo, startupProbe, PDB, NetworkPolicy default-deny, **liveness sin PG/Kafka/Redis**. | Baseline §9.5 / §11.2 |
| `[ ]` | KYC callback: además del HMAC-SHA256, **replay-protection** con `jti` + ventana 5 min (no sólo firma) y tamaño máximo de payload. | Baseline §7.5 |
| `[ ]` | `S3DocumentStorageAdapter`: URL pre-firmada con TTL ≤ 5 min y verificación de propiedad por `customerId` en cada `GET /documents/{id}/download`. | Baseline §7 |
| `[ ]` | Gate CI **OWASP Top 10 2025** + **API Top 10 2023** + ASVS **L2** (L3 en KYC y RGPD). | Baseline §7 |

---

## Fase 11 — Cierre, producción y auditoría (entregables v1.0)

> Documento global: `c:\t\Magenta\HANDOVER.md` cubre la plataforma. Esta sección recoge el **delta específico de `customers`** que otra IA u otro equipo necesita para cerrar el módulo, desplegar en producción y superar auditorías RGPD / OWASP / BdE. Promover cada ítem a tarea ejecutable antes de implementar.

### 11.1 Runbook de despliegue producción

| Estado | Ítem | Detalle |
|--------|------|---------|
| `[ ]` | **Orden Flyway** verificado | `V1__init` → `V2__pii_encryption` → `V…__processed_message`. Ejecutar `flyway info` antes de `migrate`; abortar si hay `pending` no esperado. |
| `[ ]` | **Secrets en Vault** | `customers/db`, `customers/keycloak`, `customers/kyc-provider`, `customers/s3`, `customers/kek-pii` (KEK rotación 90 días). Documentar paths exactos. |
| `[ ]` | **Helm values prod** | `charts/customers/values-prod.yaml`: réplicas ≥ 2, HPA CPU 70 %, PDB minAvailable=1, resources requests/limits, `livenessProbe` sin dependencias, `readinessProbe` con dependencias, `startupProbe` 60 s. |
| `[ ]` | **Smoke tests post-deploy** | `GET /actuator/health/readiness` 200 · `POST /customers/individuals` (con `Idempotency-Key`) 201 · `POST /kyc/start` con webhook simulado · `GET /rgpd/export` URL S3 firmada. |
| `[ ]` | **Plan de rollback** | Helm rollback + Flyway `undo` solo si la migración tiene `U…` declarada (PII no se rolea: documentarlo). |
| `[ ]` | **Carga inicial** | Sin seed productivo. Sandbox: `dev/seed.sql` con 5 perfiles INDIVIDUAL + 2 HOUSEHOLD + 1 LEGAL_ENTITY. |

### 11.2 Checklist de auditoría

| Estado | Categoría | Evidencia exigida |
|--------|-----------|-------------------|
| `[ ]` | **OWASP ASVS L2** (L3 en KYC y RGPD) | Reporte ZAP / Semgrep / Snyk en CI; ASVS checklist firmada. |
| `[ ]` | **OWASP API Top 10 2023** | Tests BOLA (API1) por `customerId` en cada endpoint; tests BFLA (API5); tests rate-limit (API4). |
| `[ ]` | **OWASP Top 10 2025** | A01 control acceso (RLS + Keycloak), A02 cripto (envelope encryption), A03 inyección (PreparedStatement + Bean Validation), A07 ident/auth, A08 integridad (firmas HMAC KYC). |
| `[ ]` | **RGPD Art. 17 (derecho al olvido)** | Procedimiento `RgpdErasureUseCase` con crypto-shredding (rotación DEK por registro). Test de reproducibilidad: tras shredding, `GET /customers/{id}` → 404 y datos no descifrables. |
| `[ ]` | **RGPD Art. 20 (portabilidad)** | `POST /rgpd/export` genera ZIP con JSON + PDF + documentos S3, URL firmada 24 h, registrado en `audit_log`. |
| `[ ]` | **RGPD Art. 30 (registro de tratamientos)** | `docs/rgpd/registro-tratamientos.md` con finalidades, base jurídica, plazos, encargados, transferencias. |
| `[ ]` | **Inventario PII por columna** | Tabla en `docs/rgpd/pii-inventory.md`: `customers.nif (PII, cifrada)`, `customers.email (PII, cifrada)`, `financial_snapshots.net_income (sensible, cifrada)`, etc. |
| `[ ]` | **Masking PII en logs** | Logback regex masker validado por test (`LogMaskerTest`) — no debe loguear NIF/IBAN/email/phone/score/income en claro. |
| `[ ]` | **Replay-protection KYC** | HMAC + `jti` único + ventana 5 min. Test: callback duplicado → 409. |
| `[ ]` | **TLS 1.3 obligatorio + HSTS preload** | Verificado en ingress + headers `Strict-Transport-Security: max-age=63072000; includeSubDomains; preload`. |
| `[ ]` | **Pista de auditoría** | `audit_log` con `who`, `when`, `what`, `tenant_id`, `correlation_id`. Retención ≥ 6 años (LSSI/LOPDGDD). |

### 11.3 SLOs / SLIs

| SLI | SLO | Medición |
|-----|-----|----------|
| Disponibilidad `POST /customers/individuals` | 99.5 % mensual | Prometheus `http_server_requests_seconds_count{status!~"5.."}` |
| Latencia p95 `GET /customers/{id}` | < 250 ms | `http_server_requests_seconds{quantile="0.95"}` |
| Lag consumer `magenta.products.transaction.v1` | < 60 s | `kafka_consumergroup_lag` |
| Error rate KYC callback | < 0.1 % | `kyc_callback_errors_total / kyc_callback_total` |
| RPO BD | ≤ 5 min | WAL streaming a réplica + S3 backup horario |
| RTO | ≤ 30 min | Restore Helm + Flyway + restore PG desde snapshot |

### 11.4 Documentación a entregar (índice)

| Estado | Documento | Ubicación |
|--------|-----------|-----------|
| `[ ]` | OpenAPI 3.1 publicada | `customers/docs/api/openapi.yaml` (generada por springdoc) |
| `[ ]` | Diagrama C4 contexto + contenedor | `customers/docs/diagrams/c4-context.puml`, `c4-container.puml` |
| `[ ]` | Modelo de datos (ERD) | `customers/docs/diagrams/erd.png` (generado de Flyway) |
| `[ ]` | Registro de tratamientos RGPD | `customers/docs/rgpd/registro-tratamientos.md` |
| `[ ]` | Inventario PII | `customers/docs/rgpd/pii-inventory.md` |
| `[ ]` | DPIA (evaluación impacto) | `customers/docs/rgpd/dpia.md` |
| `[ ]` | ADRs | `customers/docs/adr/` (ya existe ADR-001 pendiente de poblar) |
| `[ ]` | Runbook on-call | `customers/docs/ops/runbook.md` (escenarios: KYC down, S3 down, PII KEK rotación, lag Kafka) |
| `[ ]` | Plan de pruebas E2E | `customers/docs/qa/e2e-plan.md` |

### 11.5 Riesgos y deuda técnica

| Riesgo | Impacto | Mitigación |
|--------|---------|------------|
| `ARCHITECTURE.md` local v1.0 obsoleto | Drift documental, decisiones erróneas | Marcar obsoleto y remitir a `HANDOVER.md` raíz. |
| Sin tests BOLA por `customerId` | OWASP API1 sin cobertura | Test suite por endpoint antes de v1.0. |
| KYC provider lock-in (IDNow/Onfido) | Coste cambio | Adaptador hexagonal ya aísla; documentar contrato. |
| KEK rotación manual | Pérdida de claves | Procedimiento Vault automatizado + test de descifrado tras rotación. |
| Sin `Idempotency-Key` en POST | Duplicados ante reintento | Filtro `IdempotencyFilter` + tabla `idempotency_keys`. |

### 11.6 Criterios de aceptación v1.0 (gate de cierre)

Todos `[x]` para declarar `customers` como **production-ready**:

- [ ] Fase 1–9 cerradas en este documento.
- [ ] Fase 10 (mejoras baseline) ≥ 90 % cerrada (los `[ ]` restantes con ADR justificando aplazamiento).
- [ ] Runbook §11.1 ejecutado con éxito en `staging`.
- [ ] Auditoría §11.2 firmada por responsable seguridad.
- [ ] SLOs §11.3 monitorizados en Grafana ≥ 7 días.
- [ ] Documentación §11.4 completa y revisada.
- [ ] `mvn clean verify` + ArchUnit + Pact + Testcontainers verde en CI.
- [ ] Pen-test externo sin findings críticos/altos.

