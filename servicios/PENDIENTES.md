# Módulo `servicios` — Registro de construcción

> **Cómo leer este fichero**
> - `[ ]` pendiente · `[~]` en progreso · `[x]` código escrito · `[T]` probado · `[DONE]` terminado (código + tests + revisión)
>
> Actualizar siempre en la misma sesión antes de parar.  
> Stack: **Java 25 · Spring Boot 3.5.2** · PostgreSQL 18 · Kafka 4 (KRaft) · Redis 8 · Camunda 8/Zeebe · Puerto 8085
> ADR: [ADR-001](docs/adr/ADR-001-spring-boot-4-upgrade-path.md) — camino a Spring Boot 4

---

## ALINEACIÓN CON `stack-tech_spec.md` (sesión 2026-05-30)

| Brecha | Gravedad | Estado |
|--------|----------|--------|
| JWT `realm_access.roles` no extraía roles de Keycloak (bug silencioso) | 🔴 Crítica | [x] FIXED |
| `spring.jpa.open-in-view` no estaba en `false` | 🟡 Importante | [x] FIXED |
| Kafka usaba ZooKeeper (deprecado en Kafka 4.x) | 🟡 Importante | [x] FIXED → KRaft |
| PostgreSQL 16 → 18.4 (spec baseline) | 🟡 Importante | [x] FIXED |
| Redis 7.2 → 8.0 (spec baseline) | 🟡 Importante | [x] FIXED |
| Java 21 → 25 LTS (spec baseline) | 🟡 Importante | [x] FIXED |
| Spring Boot 3.3.5 → 3.5.2 (última 3.x) | 🟡 Importante | [x] FIXED |
| `server.shutdown: graceful` ausente | 🟡 Importante | [x] FIXED |
| `server.forward-headers-strategy: framework` ausente | 🟡 Importante | [x] FIXED |
| Trace sampling 100% en prod | 🟡 Importante | [x] FIXED → `${TRACE_SAMPLE_RATE:0.1}` |
| `spring.mvc.problem-details.enabled` ausente (RFC 9457) | 🟡 Importante | [x] FIXED |
| Resilience4j: sin exponential backoff ni slowCallRate | 🟡 Importante | [x] FIXED |
| Helm securityContext incompleto (sin `readOnlyRootFilesystem`, `capabilities.drop`, `seccompProfile`) | 🟡 Importante | [x] FIXED |
| `GlobalExceptionHandler` (Problem Details) ausente | 🟠 Recomendado | [x] ADDED |
| CORS no configurado | 🟠 Recomendado | [x] ADDED (en SecurityConfig) |
| ArchUnit test hexagonal ausente | 🟠 Recomendado | [x] ADDED |
| Spring Boot 4.0.x (spec ideal) | 🟣 ADR | [~] ADR-001 creado — pendiente ecosistema |
| `PodDisruptionBudget` Helm ausente | 🟠 Recomendado | [x] ADDED |

---

## FASE 0 — Andamiaje del proyecto

| Estado | Ítem | Notas |
|--------|------|-------|
| [x] | `pom.xml` (Spring Boot 3.5.2, Java 25, todas las dependencias) | `servicios/pom.xml` — actualizado sesión 2 |
| [x] | `MagentaServiciosApplication.java` | Entry point |
| [x] | `application.yml` | Puerto 8085, BD, Zeebe, Redis, Kafka |
| [x] | `Dockerfile` multi-stage Distroless | `servicios/Dockerfile` |
| [x] | `docker-compose.yml` (Postgres, Kafka, Redis, Zeebe, Keycloak) | `servicios/docker-compose.yml` |

---

## FASE 1 — Dominio

### 1.1 Aggregates y entidades

| Estado | Clase | Ruta |
|--------|-------|------|
| [x] | `ServiceDefinition` (aggregate root) | `domain/model/ServiceDefinition.java` |
| [x] | `ServiceOrder` (aggregate root) | `domain/model/ServiceOrder.java` |
| [x] | `Partner` | `domain/model/Partner.java` |
| [x] | `Deliverable` | `domain/model/Deliverable.java` |
| [x] | `Payment` | `domain/model/Payment.java` |
| [x] | `ProfileSearchBrief` | `domain/model/ProfileSearchBrief.java` |
| [x] | `PropertySearchBrief` | `domain/model/PropertySearchBrief.java` |
| [x] | `StatusChange` (history entry) | `domain/model/StatusChange.java` |

### 1.2 Value Objects y Enums

| Estado | Clase | Ruta |
|--------|-------|------|
| [x] | `ServiceCode` (enum, 17 servicios) | `domain/model/ServiceCode.java` |
| [x] | `OrderStatus` (enum) | `domain/model/OrderStatus.java` |
| [x] | `PricingModel` (enum) | `domain/model/PricingModel.java` |
| [x] | `PartnerKind` (enum) | `domain/model/PartnerKind.java` |
| [x] | `PaymentDirection` + `PaymentStatus` (enums) | `domain/model/Payment.java` |
| [x] | `DeliverableKind` (enum) | `domain/model/DeliverableKind.java` |

### 1.3 Eventos de dominio

| Estado | Clase | Ruta |
|--------|-------|------|
| [x] | `OrderCreatedEvent` | `domain/event/` |
| [x] | `OrderAcceptedEvent` | `domain/event/` |
| [x] | `OrderInProgressEvent` | `domain/event/` |
| [x] | `OrderCompletedEvent` | `domain/event/` |
| [x] | `OrderCancelledEvent` | `domain/event/` |
| [x] | `OrderFailedEvent` | `domain/event/` |
| [x] | `DeliverableIssuedEvent` | `domain/event/` |
| [x] | `PaymentCapturedEvent` | `domain/event/` |
| [x] | `PayoutSentEvent` | `domain/event/` |
| [x] | `PaymentRefundedEvent` | `domain/event/` |
| [x] | `SlaBreachedEvent` | `domain/event/` |

---

## FASE 2 — Puertos (interfaces)

| Estado | Puerto | Ruta |
|--------|--------|------|
| [x] | `ServiceCatalogRepository` | `domain/port/out/` |
| [x] | `ServiceOrderRepository` | `domain/port/out/` |
| [x] | `PartnerRepository` | `domain/port/out/` |
| [x] | `DeliverableRepository` | `domain/port/out/` |
| [x] | `DeliverablePortRepository` (puerto separado) | `domain/port/out/DeliverableRepository.java` — sesión 4 |
| [x] | `PaymentRepository` (puerto separado) | `domain/port/out/PaymentRepository.java` — sesión 4 |
| [x] | `EventPublisher` | `domain/port/out/` |
| [x] | `WorkflowPort` (Camunda) | `domain/port/out/` |
| [x] | `PaymentGatewayPort` (Stripe) | `domain/port/out/` |
| [x] | `NotificationPort` | `domain/port/out/` |
| [x] | `CustomerClient` (inbound sync) | `domain/port/out/` |
| [x] | `ProductClient` (inbound sync) | `domain/port/out/` |
| [x] | `BankClient` (inbound sync) | `domain/port/out/` |
| [x] | `AreaClient` (inbound sync) | `domain/port/out/` |

---

## FASE 3 — Base de datos

| Estado | Ítem | Fichero |
|--------|------|---------|
| [x] | Migración `V202506300001__init_schema.sql` (tablas, índices, RLS, outbox) | `db/migration/` |
| [x] | Migración `V202506300002__seed_catalog.sql` (17 ServiceDefinitions) | `db/migration/` |
| [ ] | Validación RLS con tests de integración | |

---

## FASE 4 — Adaptadores de persistencia JPA

| Estado | Clase | Ruta |
|--------|-------|------|
| [x] | `ServiceDefinitionJpaEntity` + mapper | `infrastructure/adapter/out/persistence/` |
| [x] | `ServiceOrderJpaEntity` + mapper | `infrastructure/adapter/out/persistence/` |
| [x] | `PartnerJpaEntity` + mapper | `infrastructure/adapter/out/persistence/PartnerJpaEntity.java` — sesión 3 |
| [x] | `DeliverableJpaEntity` + mapper | `infrastructure/adapter/out/persistence/DeliverableJpaEntity.java` — sesión 4 |
| [x] | `PaymentJpaEntity` + mapper | `infrastructure/adapter/out/persistence/PaymentJpaEntity.java` — sesión 4 |
| [x] | `OutboxEventJpaEntity` + `OutboxEventJpaRepository` | `infrastructure/adapter/out/persistence/` |
| [x] | `ServiceCatalogRepositoryAdapter` | `infrastructure/adapter/out/persistence/` |
| [x] | `ServiceOrderRepositoryAdapter` | `infrastructure/adapter/out/persistence/` |
| [x] | `PartnerRepositoryAdapter` | `infrastructure/adapter/out/persistence/PartnerRepositoryAdapter.java` — sesión 3 |
| [x] | `DeliverableRepositoryAdapter` | `infrastructure/adapter/out/persistence/DeliverableRepositoryAdapter.java` — sesión 4 |
| [x] | `PaymentRepositoryAdapter` | `infrastructure/adapter/out/persistence/PaymentRepositoryAdapter.java` — sesión 4 |

---

## FASE 5 — Casos de uso (Application layer)

| Estado | Caso de uso | UC | Clase |
|--------|-------------|----|-------|
| [x] | Listar catálogo | UC-S1 | `ListCatalogUseCase` |
| [x] | Cotizar servicio | UC-S2 | `QuoteServiceUseCase` |
| [x] | Crear orden (DRAFT→QUOTED) | UC-S3 | `CreateOrderUseCase` |
| [x] | Aceptar orden (→IN_PROGRESS + Camunda) | UC-S3 | `AcceptOrderUseCase` |
| [x] | Cancelar / actualizar inputs | UC-S4 | `CancelOrderUseCase` |
| [ ] | Obtener timeline de orden | UC-S4 | `GetOrderTimelineUseCase` — NO EXISTE |
| [x] | Comparar paquetes | UC-S5 | `ComparePackagesUseCase` |
| [ ] | Renovar / cancelar póliza | UC-S7 | `RenewPolicyUseCase` — NO EXISTE |
| [x] | Asignar partner (rule engine) | interna | `PartnerAssignmentService` |
| [x] | SLA scheduler (escalado) | UC-SLA | `SlaMonitorScheduler` |
| [ ] | Liquidación partners | UC-S9 | `PartnerPayoutUseCase` — NO EXISTE |
| [x] | Dashboard SLA | UC-S10 | `SlaDashboardQueryService` |

---

## FASE 6 — API REST (Controllers)

| Estado | Endpoint(s) | Controller |
|--------|-------------|------------|
| [x] | `GET /catalog`, `GET /catalog/{code}` | `CatalogController` |
| [x] | `POST /catalog/{code}/quote` | `CatalogController` |
| [x] | `POST /orders`, `GET /orders/{id}`, `PATCH /orders/{id}` | `OrdersController` |
| [x] | `POST /orders/{id}/accept` | `OrdersController` |
| [x] | `GET /orders?customerId=&status=` | `OrdersController` |
| [x] | `GET /orders/{id}/timeline` | `OrdersController` |
| [x] | `GET /orders/{id}/deliverables` | `OrdersController` |
| [x] | `GET /orders/{id}/payments` | `OrdersController` |
| [x] | `POST /partners`, `GET /partners`, `PATCH /partners/{id}` | `PartnersController` |
| [x] | `POST /webhooks/stripe` | `WebhookController` |
| [x] | `POST /webhooks/partners/{partnerCode}` | `WebhookController` |
| [x] | `GET /sla-dashboard` | `SlaDashboardController` |
| [ ] | OpenAPI spec (`springdoc-openapi`) | config |

---

## FASE 7 — Kafka

| Estado | Ítem | Clase |
|--------|------|-------|
| [x] | Producer outbox relay (polling outbox → Kafka) | `OutboxRelayScheduler` |
| [x] | Consumer `magenta.banks.preapproval.v1` | `BanksPreapprovalConsumer` |
| [x] | Consumer `magenta.banks.appraisal.v1` | `BanksAppraisalConsumer` |
| [x] | Consumer `magenta.products.property.v1` | `ProductsPropertyConsumer` — sesión 3 |
| [x] | Consumer `magenta.products.transaction.v1` | `ProductsTransactionConsumer` |
| [x] | Consumer `magenta.customers.kyc.v1` | `CustomersKycConsumer` |
| [x] | Consumer `magenta.customers.preferences.v1` | `CustomersPreferencesConsumer` — sesión 3 |
| [x] | Consumer `magenta.areas.zone.v1` | `AreasZoneConsumer` |

---

## FASE 8 — Clientes HTTP externos

| Estado | Cliente | Clase |
|--------|---------|-------|
| [x] | `AreaRestClient` (Resilience4j) | `infrastructure/adapter/out/client/` |
| [x] | `BankRestClient` | `infrastructure/adapter/out/client/` |
| [x] | `CustomerRestClient` | `infrastructure/adapter/out/client/` |
| [x] | `ProductRestClient` | `infrastructure/adapter/out/client/` |
| [x] | `StripePaymentAdapter` | `infrastructure/adapter/out/client/` |
| [x] | `NotificationRestAdapter` | `infrastructure/adapter/out/client/NotificationRestAdapter.java` — sesión 3 |

---

## FASE 9 — Integración Stripe

| Estado | Ítem |
|--------|------|
| [x] | `StripePaymentAdapter` (PaymentIntent, captura) |
| [x] | `StripeConnectPayoutAdapter` (pago a partners) |
| [x] | `StripeWebhookVerifier` (firma HMAC + replay-protection) |
| [ ] | Tests con Stripe CLI mock |

---

## FASE 10 — Camunda 8 / Zeebe

### Job Workers (`@JobWorker`)

| Estado | Worker | Job type |
|--------|--------|----------|
| [x] | `ValidateInputsWorker` | `validateInputs` |
| [x] | `QuotePriceWorker` | `quotePrice` |
| [x] | `RequestPreapprovalWorker` | `requestPreapproval` — sesión 4 |
| [x] | `AssignPartnerWorker` | `assignPartner` |
| [x] | `ChargeCustomerWorker` | `chargeCustomer` |
| [x] | `PayoutPartnerWorker` | `payoutPartner` |
| [x] | `NotifyCustomerWorker` | `notifyCustomer` |
| [x] | `GenerateInvoiceWorker` | `generateInvoice` — sesión 4 |
| [x] | `VerifyDeliverableWorker` | `verifyDeliverable` — sesión 4 |
| [x] | `CloseOrderWorker` | `closeOrder` |

### BPMN Workflows

| Estado | Fichero | Proceso |
|--------|---------|---------|
| [x] | `bpmn/first-home-aid.bpmn` | FIRST_HOME_AID |
| [x] | `bpmn/mortgage-broker.bpmn` | MORTGAGE_BROKER |
| [x] | `bpmn/rent-default-insurance.bpmn` | RENT_DEFAULT_INSURANCE |
| [x] | `bpmn/appraisal.bpmn` | APPRAISAL |
| [x] | `bpmn/profile-search.bpmn` | PROFILE_SEARCH |
| [x] | `bpmn/property-search.bpmn` | PROPERTY_SEARCH |
| [x] | `bpmn/notary-gestoria.bpmn` | NOTARY_GESTORIA |

---

## FASE 11 — Generador de facturas

| Estado | Ítem |
|--------|------|
| [x] | `InvoiceGeneratorService` (OpenPDF) |
| [x] | `InvoiceNumberingService` (serie + año correlativa) |
| [ ] | Firma XAdES |
| [ ] | Tests unitarios generador |

---

## FASE 12 — Seguridad

| Estado | Ítem |
|--------|------|
| [x] | `SecurityConfig` (JWT RS256, Keycloak JWKS, CORS) — bug JWT corregido sesión 2 |
| [x] | `TenantFilter` + `TenantContext` + `TenantAwareDataSourceProxy` + `TenantDataSourceConfig` — sesión 3 (RLS multi-tenant) |
| [x] | Webhook HMAC verifier + replay-protection (`StripeWebhookFilter`) |
| [ ] | URLs firmadas para `Deliverable.storageUri` (TTL 5 min) — NO EXISTE |
| [ ] | Vault config para IBAN cifrado |
| [ ] | Tests de autorización por endpoint |

---

## FASE 13 — Configuración infraestructura Spring

| Estado | Ítem |
|--------|------|
| [x] | `RedisConfig` (TTL catalog 10 min, quote 15 min) |
| [ ] | `KafkaConfig` (producers, consumers, CloudEvents envelope) — NO EXISTE |
| [ ] | `Resilience4jConfig` bean (CB + Retry + Bulkhead + TimeLimiter 2 s) — configurado vía application.yml, bean explícito pendiente |
| [ ] | `ObservabilityConfig` (Micrometer + OTEL + métricas de negocio) — NO EXISTE |
| [ ] | OpenAPI `springdoc` config |

---

## FASE 14 — Testing

| Estado | Test | Herramienta |
|--------|------|-------------|
| [x] | `PartnerAssignmentServiceTest` | JUnit 5 + AssertJ |
| [ ] | `PriceFormulaEvaluatorTest` | JUnit 5 + AssertJ |
| [x] | `ServiceOrderStateMachineTest` | JUnit 5 |
| [x] | `HexagonalArchitectureTest` | ArchUnit JUnit 5 — añadido sesión 2 |
| [ ] | `CatalogControllerIT` | Testcontainers (Postgres) — pendiente |
| [ ] | `OrdersControllerIT` | Testcontainers (Postgres + Zeebe) — pendiente |
| [ ] | `StripeWebhookIT` | Wiremock — pendiente |
| [ ] | `FirstHomeAidBpmnTest` | zeebe-process-test — pendiente |
| [ ] | `AppraisalBpmnTest` | zeebe-process-test — pendiente |
| [ ] | Contract tests Pact (customers, products, banks) | Pact JVM — pendiente |
| [ ] | E2E Playwright (cotizar→pagar→deliverable) | Playwright — pendiente |

---

## FASE 15 — DevOps

| Estado | Ítem | Ruta |
|--------|------|------|
| [x] | `Dockerfile` multi-stage Distroless | `servicios/Dockerfile` |
| [x] | `docker-compose.yml` (Postgres, Kafka, Redis, Zeebe, Keycloak) | `servicios/docker-compose.yml` |
| [x] | Helm chart `charts/servicios/` | `charts/servicios/Chart.yaml`, `values.yaml`, `templates/` |
| [x] | `ops/grafana/services-sla.json` | Grafana dashboard |

---

## CHECKLIST FINAL (del MODULE-SPEC.md)

| Estado | Ítem |
|--------|------|
| [x] | Migraciones Flyway con RLS y outbox |
| [x] | Catálogo seed para los 17 servicios |
| [x] | BPMN deployados en Camunda (`bpmn/*.bpmn`) |
| [x] | Job workers Spring (`@JobWorker`) — 7 de 10 creados |
| [ ] | Job workers testados |
| [x] | Integración Stripe Connect (cobros + payouts) |
| [x] | Webhooks firmados y antirreplay |
| [x] | Generador de facturas PDF + numeración correlativa |
| [ ] | OpenAPI springdoc config bean |
| [x] | Dashboard Grafana `services-sla.json` |
| [x] | Helm chart `charts/servicios/` |
| [x] | TenantFilter (RLS multi-tenant) — sesión 3 |
| [x] | PartnerJpaEntity + PartnerRepositoryAdapter — sesión 3 |
| [x] | NotificationRestAdapter — sesión 3 |
| [x] | Consumers Kafka: `products.property` + `customers.preferences` — sesión 3 |
| [x] | Workers Camunda: `requestPreapproval`, `generateInvoice`, `verifyDeliverable` — sesión 4 |
| [ ] | Use cases: `GetOrderTimeline`, `RenewPolicy`, `PartnerPayout` |
| [ ] | Tests integración Testcontainers |
| [ ] | Tests BPMN (zeebe-process-test) |
| [ ] | Vault config + cifrado IBAN |

---

## Registro de sesiones

| Fecha | Sesión | Qué se hizo | Próximo paso |
|-------|--------|-------------|--------------|
| 2026-05-30 | 1 | Creado PENDIENTES.md, pom.xml, dominio completo (7 aggregates, 11 eventos, 5 enums), puertos, migraciones Flyway V1+V2, adaptadores JPA (ServiceDefinition+ServiceOrder+Outbox), casos de uso (ListCatalog, Quote, CreateOrder, AcceptOrder, Cancel, Compare, PartnerAssignment, SlaMonitor, SlaDashboard), controllers REST (Catalog, Orders, Partners, Webhooks, SlaDashboard), Outbox relay + 5 Kafka consumers, 4 RestClients, Stripe adapter + HMAC filter, 7 Camunda @JobWorkers, 7 BPMNs, generador PDF + numeración, SecurityConfig, RedisConfig, application.yml, 2 tests unitarios, Dockerfile, docker-compose.yml, Helm chart, Grafana dashboard | Alineación stack-tech_spec + completar pendientes |
| 2026-05-30 | 2 | Alineación completa con stack-tech_spec.md: bug JWT corregido, Java 21→25, Spring Boot 3.3→3.5.2, PG 16→18, Redis 7→8, Kafka ZK→KRaft, open-in-view:false, graceful shutdown, forward-headers, trace sampling, problem-details RFC 9457, Resilience4j mejorado, Helm securityContext completo, PodDisruptionBudget, GlobalExceptionHandler, CORS, ArchUnit test, ADR-001; auditoría real del filesystem y corrección de estados en PENDIENTES | Sesión 3: PartnerJpaEntity+Adapter, NotificationRestAdapter, TenantFilter, workers faltantes, consumers faltantes, use cases faltantes, OpenAPI config, ObservabilityConfig |
| 2026-05-30 | 4 | **S2** `DeliverableRepository` port + `DeliverableJpaEntity` + `DeliverableJpaRepository` + `DeliverableRepositoryAdapter`. **S3** `PaymentRepository` port + `PaymentJpaEntity` + `PaymentJpaRepository` + `PaymentRepositoryAdapter`. **S8** `RequestPreapprovalWorker` (calls BankClientPort.createPreapproval) + `GenerateInvoiceWorker` (InvoiceGeneratorService + InvoiceNumberingService + guarda Deliverable INVOICE) + `VerifyDeliverableWorker` (valida existencia, orderId, kind y sha256). Los 10 workers Camunda ya están completos. | Siguiente: S7 GetOrderTimeline+RenewPolicy+PartnerPayout, S18 KafkaConfig+ObservabilityConfig, S19 OpenAPI config, S16 MODULE-SPEC.md header, S20 Testcontainers IT |

---

## FASE 16 — MEJORAS PROPUESTAS (revisión 2026-05-30 vs `IDEABASE/inyectados/stack-tech_spec.md`)

> Propuestas no ejecutadas. Promover a tareas concretas antes de implementar.
> No se modifica nada de FASE 0–15 ni del registro de sesiones.

| Estado | Ítem | Razón |
|--------|------|-------|
| [ ] | **Cabecera `MODULE-SPEC.md` desactualizada**: dice "Java 21 · Spring Boot 3.3 · PostgreSQL 16 · Angular 17 · Kafka 3.7" pero el código ya está en Java 25 / Boot 3.5.2 / PG 18 / Redis 8 / Kafka 4 KRaft. Actualizar cabecera (ya hecho en este tracker, falta en spec). | Drift documental |
| [ ] | **`ARCHITECTURE.md` local** sigue siendo v1.0 con stack viejo. Sustituir o anotar "obsoleto, ver tracker". | Drift documental |
| [ ] | **Camunda 8 / Zeebe — justificación explícita**: la baseline marca workflow engine como *opcional* ("introducir cuando los workflows visibles superen 3 estados con tareas humanas y SLA explícito"). Aquí los 17 servicios + tareas humanas + SLA lo justifican; añadir párrafo en ADR-001 (o ADR nuevo) que lo deje por escrito. Si algún servicio se simplifica en fases tempranas, considerar máquina de estados en BD para esos. | Baseline §3 |
| [ ] | **`Idempotency-Key`** obligatoria en `POST /orders`, `POST /catalog/{code}/quote`, `POST /partners`, `POST /webhooks/stripe`, `POST /webhooks/partners/{partnerCode}` (estos últimos por idempotencia de proveedor). | Baseline §6 |
| [ ] | **Tabla `processed_message(consumer_name, event_id)`** explícita en migración (V…__processed_message.sql) para los 7 consumers Kafka. Sin esto la garantía at-least-once + idempotencia es invisible. | Baseline §5 |
| [ ] | **Cursor-based pagination** en `GET /orders`, `GET /partners`, `GET /sla-dashboard`. | Baseline §6 |
| [ ] | **OWASP API1 BOLA**: tests de autorización por `customerId` ajeno y `partnerId` ajeno (un `partner_A` no debe ver órdenes de `partner_B` aunque compartan tenant). | OWASP API1 |
| [ ] | **F12 — `TenantFilter`** + RLS Postgres por `tenant_id`: bloqueante para multi-tenant seguro. | Baseline §7 |
| [ ] | **F12 — Vault config + IBAN partner cifrado** (envelope encryption KEK Vault + DEK por registro). Sin esto, los IBAN de payouts viajan en claro en BD. | Baseline §7.3 |
| [ ] | **F12 — URLs firmadas para `Deliverable.storageUri`** (TTL ≤ 5 min) y verificación de propiedad por `customerId`/`partnerId`. | Baseline §7 |
| [ ] | **PII y datos sensibles en logs**: masker en `logback-spring.xml` para `iban`, `email`, `phone`, `nif`, `customerId` (truncar). | Baseline §7.3 |
| [ ] | **Helm `livenessProbe` sin PG / Kafka / Zeebe / Redis** (sólo readiness). Verificar template. | Baseline §11.2 |
| [ ] | **Resilience4j**: confirmar `timelimiter` 2s antes que `circuitbreaker`; retry **sólo idempotente** o protegido por Idempotency-Key (no en webhooks Stripe entrantes). | Baseline §10 |
| [ ] | **Stripe webhook**: además del HMAC, validar `Stripe-Signature` timestamp (ventana 5 min) y reusar `StripeWebhookFilter` para webhooks de partners (mismo contrato). | Baseline §7.5 |
| [ ] | **F11 — Firma XAdES de facturas** y test del generador (impacto fiscal). | Producto |
| [ ] | **F14 — `FirstHomeAidBpmnTest` y `AppraisalBpmnTest`** (zeebe-process-test): los workflows críticos no están probados; si Zeebe versiona BPMN incorrectamente, no se detecta hasta producción. | Calidad |
| [ ] | Gate CI **OWASP Top 10 2025** + **API Top 10 2023** + ASVS **L2** (L3 en pagos y partner payouts). | Baseline §7 |
| [ ] | **Boot 4 (ADR-001)**: cuando ecosistema Camunda 8 / Zeebe + Stripe + Spring Cloud sean compatibles, ejecutar plan de upgrade. Hoy queda en 3.5.2. | Baseline §3 |

