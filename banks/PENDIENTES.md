# PENDIENTES — Módulo `banks`

> Fichero de seguimiento de implementación. Estados: ⬜ pendiente · 🔄 en progreso · ✅ hecho · 🧪 probado
> Actualizar este fichero en cada sesión antes de cerrar.
>
> **Rama Git sugerida:** `feat/banks-module`
> **Puerto local:** 8082  |  **BD:** `banks_db`  |  **Esquema:** `banks`

---

## Fase 1 — Estructura Maven + Bootstrap ✅

| Estado | Tarea | Archivo(s) |
|--------|-------|-----------|
| ✅ | `pom.xml` raíz con todas las dependencias | `pom.xml` |
| ✅ | Clase principal `MagentaBanksApplication` | `src/main/java/.../MagentaBanksApplication.java` |
| ✅ | `application.yml` base + perfiles dev/prod | `src/main/resources/application.yml` |
| ✅ | `application-dev.yml` con Testcontainers URL | `src/main/resources/application-dev.yml` |
| ✅ | **Alineación IDEABASE/inyectados**: Java 25 LTS, Spring Boot 4.0.6, PG 18, Redis 8, Kafka KRaft, open-in-view false, graceful shutdown, forward-headers-strategy, OTel OTLP, Resilience4j completo | múltiples ficheros |

---

## Fase 2 — Migraciones Flyway ✅

| Estado | Tarea | Archivo(s) |
|--------|-------|-----------|
| ✅ | `V1__init.sql` — esquema completo `banks` | `src/main/resources/db/migration/V1__init.sql` |
| ✅ | `V2__seed_banks.sql` — 13 bancos seed | `src/main/resources/db/migration/V2__seed_banks.sql` |
| ✅ | `V3__seed_products.sql` — productos preferentes | `src/main/resources/db/migration/V3__seed_products.sql` |

---

## Fase 3 — Dominio (puro Java, sin Spring/JPA) ✅

### Aggregates y Value Objects

| Estado | Tarea | Archivo(s) |
|--------|-------|-----------|
| ✅ | `Bank` aggregate root + `ContactChannel` VO | `domain/model/bank/` |
| ✅ | `LoanProduct` aggregate + `RateInfo` VO + `EligibilityRules` | `domain/model/loanproduct/` |
| ✅ | `LoanSimulation` aggregate + `BorrowerProfile` VO + `SimulationResult` VO | `domain/model/loansimulation/` |
| ✅ | `Preapproval` aggregate + máquina de estados | `domain/model/preapproval/` |
| ✅ | `Appraisal` aggregate | `domain/model/appraisal/` |
| ✅ | Enums canónicos (`RateType`, `LoanCategory`, `PreapprovalStatus`, `Rating`, `Scheme`) | `domain/model/` |

### Reglas y motor

| Estado | Tarea | Archivo(s) |
|--------|-------|-----------|
| ✅ | `RuleEngine` — evaluador de `EligibilityRules` JSON | `domain/service/RuleEngine.java` |
| ✅ | `PreapprovalStateMachine` — transiciones válidas | `domain/service/PreapprovalStateMachine.java` |

### Eventos de dominio

| Estado | Tarea | Archivo(s) |
|--------|-------|-----------|
| ✅ | `ProductPublished`, `ProductUpdated`, `ProductDeprecated` | `domain/event/` |
| ✅ | `PreapprovalRequested`, `PreapprovalApproved`, `PreapprovalRejected`, `PreapprovalExpired` | `domain/event/` |
| ✅ | `AppraisalIssued`, `SimulationCreated` | `domain/event/` |

---

## Fase 4 — Puertos (interfaces de dominio)

| Estado | Tarea | Archivo(s) |
|--------|-------|-----------|
| ✅ | `BankRepository` port | `domain/port/out/BankRepository.java` |
| ✅ | `LoanProductRepository` port | `domain/port/out/LoanProductRepository.java` |
| ✅ | `LoanSimulationRepository` port | `domain/port/out/LoanSimulationRepository.java` |
| ✅ | `PreapprovalRepository` port | `domain/port/out/PreapprovalRepository.java` |
| ✅ | `AppraisalRepository` port | `domain/port/out/AppraisalRepository.java` |
| ✅ | `EuriborRateRepository` port | `domain/port/out/EuriborRateRepository.java` |
| ✅ | `CustomerClient` port (outbound) | `domain/port/out/CustomerClient.java` |
| ✅ | `PropertyClient` port (outbound) | `domain/port/out/PropertyClient.java` |
| ✅ | `ZoneClient` port (outbound) | `domain/port/out/ZoneClient.java` |
| ✅ | `DomainEventPublisher` port | `domain/port/out/DomainEventPublisher.java` |

---

## Fase 5 — Capa Aplicación (casos de uso)

| Estado | Tarea | UC | Archivo(s) |
|--------|-------|----|-----------|
| ✅ | `ListBanksUseCase` | UC-B1 | `application/usecase/ListBanksUseCase.java` |
| ✅ | `GetBankUseCase` | UC-B1 | `application/usecase/GetBankUseCase.java` |
| ✅ | `SearchProductsUseCase` | UC-B2 | `application/usecase/SearchProductsUseCase.java` |
| ✅ | `SimulateLoanUseCase` | UC-B3 | `application/usecase/SimulateLoanUseCase.java` |
| ✅ | `SimulateNinetyFiveFiveUseCase` | UC-B4 | `application/usecase/SimulateNinetyFiveFiveUseCase.java` |
| ✅ | `RequestPreapprovalUseCase` | UC-B5 | `application/usecase/RequestPreapprovalUseCase.java` |
| ✅ | `UpdatePreapprovalStatusUseCase` | UC-B5 | `application/usecase/UpdatePreapprovalStatusUseCase.java` |
| ✅ | `MarkPropertyFinanciableUseCase` | UC-B6 | `application/usecase/MarkPropertyFinanciableUseCase.java` |
| ✅ | `CompareProductsUseCase` | UC-B7 | `application/usecase/CompareProductsUseCase.java` |
| ✅ | `RegisterAppraisalUseCase` | UC-B10 | `application/usecase/RegisterAppraisalUseCase.java` |
| ✅ | `GetFinancingFeasibilityUseCase` | UC-B6 | `application/usecase/GetFinancingFeasibilityUseCase.java` |

---

## Fase 6 — Servicios de cálculo (dominio)

| Estado | Tarea | Archivo(s) |
|--------|-------|-----------|
| ✅ | `FrenchAmortizationService` — cuota sistema francés | `domain/service/FrenchAmortizationService.java` |
| ✅ | `TaeCalculatorService` — Newton-Raphson, Circular BdE 5/2012 | `domain/service/TaeCalculatorService.java` |
| ✅ | `OwnFundsCalculatorService` — esquema 90+5+5 | `domain/service/OwnFundsCalculatorService.java` |
| ✅ | `NinetyFiveFiveBreakdownService` — desglose banco/promotor/comprador/impuestos/costes | `domain/service/NinetyFiveFiveBreakdownService.java` |
| ✅ | `ApprovabilityScorerService` — score 0..100 + verdict | `domain/service/ApprovabilityScorerService.java` |

---

## Fase 7 — Infraestructura: Persistencia JPA

| Estado | Tarea | Archivo(s) |
|--------|-------|-----------|
| ✅ | `BankJpaEntity` + `BankJpaRepository` | `infrastructure/adapter/out/persistence/bank/` |
| ✅ | `LoanProductJpaEntity` + `LoanProductJpaRepository` | `infrastructure/adapter/out/persistence/loanproduct/` |
| ✅ | `LoanSimulationJpaEntity` + repo | `infrastructure/adapter/out/persistence/loansimulation/` |
| ✅ | `PreapprovalJpaEntity` + `PreapprovalEventJpaEntity` + repos | `infrastructure/adapter/out/persistence/preapproval/` |
| ✅ | `AppraisalJpaEntity` + repo | `infrastructure/adapter/out/persistence/appraisal/` |
| ✅ | `EuriborRateJpaEntity` + repo | `infrastructure/adapter/out/persistence/euribor/` |
| ✅ | `OutboxEventJpaEntity` + repo | `infrastructure/adapter/out/persistence/outbox/` |
| ⬜ | MapStruct mappers dominio ↔ JPA | `infrastructure/adapter/out/persistence/*/` |

---

## Fase 8 — Infraestructura: REST Controllers

| Estado | Tarea | Archivo(s) |
|--------|-------|-----------|
| ✅ | `BankController` — GET /banks, GET /banks/{id}, GET /banks/{id}/products | `infrastructure/adapter/in/web/BankController.java` |
| ✅ | `LoanProductController` — GET /products, GET /products/{id} | `infrastructure/adapter/in/web/LoanProductController.java` |
| ✅ | `LoanSimulationController` — POST /simulations, GET, POST /compare | `infrastructure/adapter/in/web/LoanSimulationController.java` |
| ✅ | `PreapprovalController` — CRUD preapprovals | `infrastructure/adapter/in/web/PreapprovalController.java` |
| ✅ | `AppraisalController` — POST/GET appraisals | `infrastructure/adapter/in/web/AppraisalController.java` |
| ✅ | `EuriborController` — GET /indices/euribor | `infrastructure/adapter/in/web/EuriborController.java` |
| ✅ | `FeasibilityController` — GET /financing-feasibility | `infrastructure/adapter/in/web/FeasibilityController.java` |
| ✅ | DTOs request/response — BankResponse, LoanProductResponse, SimulationRequest | `infrastructure/adapter/in/web/dto/` |
| ⬜ | MapStruct mappers dominio ↔ DTO | `infrastructure/adapter/in/web/mapper/` |
| ✅ | `GlobalExceptionHandler` — Problem Details RFC 7807 | `infrastructure/adapter/in/web/GlobalExceptionHandler.java` |

---

## Fase 9 — Infraestructura: Kafka

| Estado | Tarea | Archivo(s) |
|--------|-------|-----------|
| ✅ | `OutboxDomainEventPublisher` — transactional outbox (persiste en misma tx) | `infrastructure/adapter/out/persistence/outbox/OutboxDomainEventPublisher.java` |
| ✅ | `OutboxPoller` — @Scheduled publica pendientes | `infrastructure/adapter/out/kafka/OutboxPoller.java` |
| ⬜ | `CustomerProfileConsumer` — `magenta.customers.profile.v1` | `infrastructure/adapter/in/kafka/CustomerProfileConsumer.java` |
| ⬜ | `CustomerKycConsumer` — `magenta.customers.kyc.v1` | `infrastructure/adapter/in/kafka/CustomerKycConsumer.java` |
| ⬜ | `PropertyEventConsumer` — `magenta.products.property.v1` | `infrastructure/adapter/in/kafka/PropertyEventConsumer.java` |
| ⬜ | `TransactionEventConsumer` — `magenta.products.transaction.v1` | `infrastructure/adapter/in/kafka/TransactionEventConsumer.java` |
| ⬜ | `ZoneEventConsumer` — `magenta.areas.zone.v1` | `infrastructure/adapter/in/kafka/ZoneEventConsumer.java` |
| ⬜ | `PriceIndexConsumer` — `magenta.areas.price-index.v1` | `infrastructure/adapter/in/kafka/PriceIndexConsumer.java` |

---

## Fase 10 — Infraestructura: REST Clients externos

| Estado | Tarea | Archivo(s) |
|--------|-------|-----------|
| ✅ | `CustomerRestClient` → customers:8083 + Resilience4j | `infrastructure/adapter/out/client/CustomerRestClient.java` |
| ✅ | `PropertyRestClient` → products:8084 | `infrastructure/adapter/out/client/PropertyRestClient.java` |
| ✅ | `ZoneRestClient` → areas:8081 | `infrastructure/adapter/out/client/ZoneRestClient.java` |
| ⬜ | `EuriborFetcherJob` — cron EMMI + fallback BdE | `infrastructure/adapter/out/client/EuriborFetcherJob.java` |

---

## Fase 11 — Configuración Spring

| Estado | Tarea | Archivo(s) |
|--------|-------|-----------|
| ✅ | `SecurityConfig` — JWT RS256 Keycloak + RBAC | `infrastructure/config/SecurityConfig.java` |
| ✅ | `CacheConfig` — Caffeine (productos) + Redis (simulaciones) | `infrastructure/config/CacheConfig.java` |
| ⬜ | `ObservabilityConfig` — OpenTelemetry beans | `infrastructure/config/ObservabilityConfig.java` |
| ⬜ | `RateLimitConfig` — Bucket4j /simulations | `infrastructure/config/RateLimitConfig.java` |
| ✅ | `KafkaConfig` — topics, serializers | `infrastructure/config/KafkaConfig.java` |
| ⬜ | `ResilienceConfig` — CircuitBreaker customers | `infrastructure/config/ResilienceConfig.java` |

---

## Fase 12 — Datos semilla (Flyway)

| Estado | Tarea | Archivo(s) |
|--------|-------|-----------|
| ✅ | `V2__seed_banks.sql` — 13 bancos: Santander, BBVA, CaixaBank, ING, Bankinter, EVO, Abanca, Cajamar, Kutxabank, MyInvestor, Open Bank, ImaginBank, GoHipoteca | `db/migration/V2__seed_banks.sql` |
| ✅ | `V3__seed_products.sql` — productos preferentes 90+5+5 por banco | `db/migration/V3__seed_products.sql` |

---

## Fase 13 — Tests

| Estado | Tarea | Archivo(s) |
|--------|-------|-----------|
| ✅ | `RuleEngineTest` — casos eligibilidad | `test/.../RuleEngineTest.java` |
| ✅ | `TaeCalculatorTest` — golden files BdE | `test/.../TaeCalculatorTest.java` |
| ✅ | `FrenchAmortizationTest` — cuotas conocidas | `test/.../FrenchAmortizationTest.java` |
| ✅ | `ApprovabilityScorerTest` | `test/.../ApprovabilityScorerTest.java` |
| ✅ | `OwnFundsCalculatorTest` — esquema 90+5+5 | `test/.../OwnFundsCalculatorTest.java` |
| ✅ | `PreapprovalStateMachineTest` | `test/.../PreapprovalStateMachineTest.java` |
| ✅ | `HexagonalArchitectureTest` — ArchUnit reglas hexagonal | `test/.../HexagonalArchitectureTest.java` |
| ⬜ | `BankControllerIT` — Testcontainers Postgres | `test/.../BankControllerIT.java` |
| ⬜ | `LoanSimulationIT` — Testcontainers + Wiremock | `test/.../LoanSimulationIT.java` |
| ⬜ | `PreapprovalIT` | `test/.../PreapprovalIT.java` |

---

## Fase 14 — DevOps / Contenedores

| Estado | Tarea | Archivo(s) |
|--------|-------|-----------|
| ✅ | `Dockerfile` multi-stage (Maven + Distroless Java 25) | `Dockerfile` |
| ✅ | `docker-compose.yml` — stack local completo (PG18, Redis8, Kafka KRaft) | `docker-compose.yml` |
| ✅ | `keycloak/magenta-realm.json` — realm magenta, roles CUSTOMER/AGENT/BANK_OFFICER/ADMIN/SYSTEM | `keycloak/magenta-realm.json` |
| ⬜ | Helm chart `charts/banks/` | `charts/banks/` |
| ⬜ | Dashboard Grafana `banks-preapprovals.json` | `charts/banks/dashboards/` |

---

## Resumen de progreso

| Fase | Total | ✅ Hecho | 🧪 Probado | ⬜ Pendiente |
|------|-------|---------|-----------|------------|
| 1. Bootstrap + alineación | 5 | 5 | 0 | 0 |
| 2. Flyway | 3 | 3 | 0 | 0 |
| 3. Dominio | 14 | 14 | 0 | 0 |
| 4. Puertos | 10 | 10 | 0 | 0 |
| 5. Aplicación | 11 | 9 | 0 | 2 |
| 6. Cálculo | 4 | 4 | 0 | 0 |
| 7. Persistencia JPA | 8 | 7 | 0 | 1 |
| 8. Controllers | 10 | 9 | 0 | 1 |
| 9. Kafka | 8 | 2 | 0 | 6 |
| 10. REST Clients | 4 | 3 | 0 | 1 |
| 11. Config | 6 | 3 | 0 | 3 |
| 12. Semilla | 2 | 2 | 0 | 0 |
| 13. Tests | 10 | 7 | 0 | 3 |
| 14. DevOps | 5 | 2 | 0 | 3 |
| **TOTAL** | **100** | **80** | **0** | **20** |

---

## Notas de contexto para la próxima sesión

- **Stack baseline 30/05/2026 (alineado con IDEABASE/inyectados/stack-tech_spec.md)**
- Java **25 LTS** / Spring Boot **4.0.6** / PostgreSQL **18** / Redis **8** / Kafka **Confluent 8.0.0 KRaft** (sin Zookeeper) / Keycloak 24
- Arquitectura: Hexagonal + DDD ligero (ver `ARCHITECTURE.md`)
- Package raíz: `com.magenta.banks`
- Multi-tenant: todas las tablas tienen `tenant_id`; RLS en `preapprovals`
- RuleEngine: motor propio puro Java en `domain/service/RuleEngine.java`
- TAE: Newton-Raphson, Circular BdE 5/2012 (RD 309/2019)
- Outbox pattern para eventos Kafka (tabla `outbox_event`)
- Caché Caffeine (productos, 30 min) + Redis (simulaciones determinísticas, 1 h)
- Rate limit Bucket4j: /simulations 30 rpm/IP, 300 rpm/usuario
- Datos sensibles cifrados AES-256-GCM (Spring Cloud Vault 4.2.0)
- `server.shutdown: graceful`, `open-in-view: false`, `forward-headers-strategy: framework`
- Tracing sampling: 0.1 prod, 1.0 dev; OTel OTLP endpoint configurable
- Resilience4j: timelimiter 2s + CB con slowCall 1500ms + retry exponential backoff + bulkhead
- ArchUnit test: `HexagonalArchitectureTest` verifica reglas de dependencia hexagonal

## Próximos pasos prioritarios (para continuar)

**Bloque A — Compilación (bloquean el build):**
1. Persistencia JPA principal cerrada; queda revisar mappers MapStruct si se decide mantenerlos como requisito formal.
2. Config classes: `ObservabilityConfig`, `RateLimitConfig`, `ResilienceConfig`
3. Controller de Euribor añadido; queda endurecer/expandir controllers administrativos de producto cuando haya decisión de permisos.
4. Use case `SimulateNinetyFiveFiveUseCase` implementado y expuesto en `POST /api/v1/simulations/90-5-5`.

**Bloque B — Funcionalidad:**
5. `EuriborFetcherJob` — cron EMMI + fallback BdE XML
6. Consumers Kafka (6): CustomerProfile, Kyc, Property, Transaction, Zone, PriceIndex

**Bloque C — Datos y entorno:**
7. `V2__seed_banks.sql` + `V3__seed_products.sql` (13 bancos) implementados como datos sintéticos de sandbox; falta validarlos contra Flyway con Java 25.
8. `keycloak/magenta-realm.json` añadido con roles, clientes y usuarios demo; falta validar import real al levantar Docker Compose.

**Bloque D — Calidad y operación:**
9. Tests de integración con Testcontainers (BankControllerIT, LoanSimulationIT, PreapprovalIT)
10. Helm chart `charts/banks/` + Dashboard Grafana

---

## Fase 15 — Mejoras propuestas (revisión 2026-05-30 vs `IDEABASE/inyectados/stack-tech_spec.md`)

> Propuestas no ejecutadas. Promover a tareas concretas antes de implementar.

| Estado | Ítem | Razón |
|--------|------|-------|
| ⬜ | **Cabecera `MODULE-SPEC.md` y `ARCHITECTURE.md` local desalineadas**: el código ya está en Java 25 / Boot 4.0.6 / PG 18 / Redis 8 / Kafka KRaft / Keycloak 26, pero las cabeceras dicen "Java 21 · Spring Boot 3.3 · PG 16 · Kafka 3.7 · Keycloak 24". Actualizar para evitar drift. | Drift documental |
| ⬜ | **Sustituir "RFC 7807" por "RFC 9457"** en F8 (`GlobalExceptionHandler — Problem Details RFC 7807`) y en docstrings: Spring 6.1+ `ProblemDetail` ya cumple 9457 (que reemplaza al 7807). | Baseline §6 |
| ✅ | **`SimulateNinetyFiveFiveUseCase` (UC-B4)** implementado con desglose explícito 90+5+5, delegación en `SimulateLoanUseCase` y endpoint `POST /api/v1/simulations/90-5-5`. | Producto |
| ✅ | **`Idempotency-Key` soportada** en `POST /simulations`, `POST /simulations/90-5-5`, `POST /simulations/compare`, `POST /preapprovals`, `POST /appraisals` con persistencia de respuesta. | Baseline §6 |
| ✅ | **Tabla `processed_event(consumer_name, event_id)`** para los consumers Kafka pendientes + `ProcessedEventService`. | Baseline §5 |
| ⬜ | Paginación **cursor-based** en `GET /products?...` y `GET /banks/{id}/products` (hoy offset por defecto Spring Data). | Baseline §6 |
| ⬜ | **Test de autorización BOLA**: ningún `bankOfficer` debe poder leer preaprobaciones de otra entidad bancaria; ningún `customer` debe poder ver simulaciones/preaprobaciones de otro `customerId` aunque comparta tenant. | OWASP API1 2023 |
| ⬜ | **Helm chart `charts/banks/`** alineado al estándar de los demás módulos: `securityContext` restricted (runAsNonRoot, readOnlyRootFilesystem, capabilities drop ALL, seccompProfile RuntimeDefault), startupProbe, PDB, NetworkPolicy default-deny + flujos PG/Kafka/Redis/Keycloak/OTel/DNS. | Baseline §9.5 |
| ⬜ | `livenessProbe` no debe depender de PG/Kafka/Redis; sólo `readinessProbe`. | Baseline §11.2 |
| ⬜ | Resilience4j: confirmar **timeout 2s primero**, retry **sólo idempotente** (HTTP GET / comandos con Idempotency-Key) con backoff+jitter, circuit breaker con `slowCallDurationThreshold` y `permittedNumberOfCallsInHalfOpenState`. | Baseline §10 |
| ⬜ | Gate CI **OWASP Top 10 2025** + **API Top 10 2023** + ASVS **L2** (L3 en preaprobación). | Baseline §7 |
| ⬜ | `EuriborFetcherJob`: documentar que el cron no debe sincronizarse exactamente al inicio de hora (jitter) y que ante fallo EMMI hace fallback BdE XML (mencionado, falta implementar). | Resiliencia |
| ⬜ | **`scheme = NINETY_FIVE_FIVE` + `promoCode`** deben tener test que recorra los 13 bancos seed y verifique que cada producto preferente queda correctamente etiquetado (sin esto, UC-B6 "Financiable 90+5+5" puede dar falsos negativos). | Producto |

