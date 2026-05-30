# AI HANDOFF — Módulo `servicios` de Magenta Platform

> **Instrucciones para la IA que recibe este fichero**
> Lee este documento de arriba abajo antes de tocar ningún fichero.
> Contiene el estado exacto del proyecto a 2026-05-30 (109 ficheros Java, 4 sesiones completadas),
> la lista priorizada de trabajo pendiente, los requisitos para poner en producción,
> los criterios de auditoría y la documentación que debe existir al cierre.
> El tracker operativo es `PENDIENTES.md` — actualízalo en cada sesión.

---

## 1. IDENTIDAD DEL PROYECTO

| Campo | Valor |
|-------|-------|
| Nombre | `magenta-servicios` |
| Bounded context | Servicios (marketplace de servicios inmobiliarios) |
| Plataforma | Magenta — plataforma SaaS multi-tenant para el sector inmobiliario |
| Repositorio raíz | `c:\t\Magenta\servicios\` |
| Puerto HTTP | 8085 |
| Paquete base Java | `com.magenta.servicios` |
| Spec de referencia | `C:\t\Magenta\IDEABASE\inyectados\stack-tech_spec.md` (baseline 2026-05-30) |
| Módulo spec local | `c:\t\Magenta\servicios\MODULE-SPEC.md` (cabecera desactualizada — ver §11) |
| Arquitectura doc | `c:\t\Magenta\servicios\ARCHITECTURE.md` (obsoleto — ver §11) |

---

## 2. TECH STACK — VERSIONES EXACTAS

```
Java            25 LTS  (compilador source/target=25)
Spring Boot     3.5.2   (parent POM)  — NO subir a 4.x hasta ADR-001 autoriza
PostgreSQL      18      (imagen docker: postgres:18)
Redis           8.0     (imagen docker: redis:8.0-alpine)
Kafka           4 KRaft (Confluent CP 8.0, sin ZooKeeper, CLUSTER_ID fijo: MkU3OEVBNTcwNTJENDM2Qk)
Camunda/Zeebe   8.7.0   (spring-boot-starter-camunda-sdk:8.7.0)
Keycloak        26      (OIDC/OAuth2, realm: magenta)
Stripe SDK      25.4.0
OpenPDF         2.0.3   (facturación)
Flyway          10.22.0
Resilience4j    2.3.0
MapStruct       1.6.3.Final
Testcontainers  1.21.1
ArchUnit        1.3.0
SpringDoc       2.8.5   (OpenAPI)
hypersistence   hibernate-types-60:2.21.1  (ListArrayType para TEXT[]/UUID[])
Spring Cloud    2024.0.2 (Vault config)
Pact JVM        4.6.14
```

### Variables de entorno requeridas en producción

```
DB_USERNAME          contraseña → Vault secret/servicios/db
DB_PASSWORD          contraseña → Vault secret/servicios/db
REDIS_HOST           redis:6379
KAFKA_BOOTSTRAP_SERVERS  kafka:9092
KEYCLOAK_JWKS_URI    http://keycloak:8080/realms/magenta/protocol/openid-connect/certs
ZEEBE_GATEWAY_ADDRESS zeebe:26500
STRIPE_SECRET_KEY    sk_live_... → Vault
STRIPE_WEBHOOK_SECRET whsec_... → Vault
CORS_ALLOWED_ORIGINS https://app.magenta.es
TRACE_SAMPLE_RATE    0.1  (prod) | 1.0  (dev)
OTEL_EXPORTER_OTLP_ENDPOINT http://otel-collector:4318/v1/traces
SPRING_CLOUD_VAULT_URI  http://vault:8200
SPRING_CLOUD_VAULT_TOKEN → Vault AppRole
```

---

## 3. ARQUITECTURA — REGLAS INVARIANTES

### Estructura de paquetes
```
com.magenta.servicios
├── domain/
│   ├── model/          ← aggregates, value objects, enums — CERO dependencias Spring/JPA/Kafka
│   ├── event/          ← domain events (Java records)
│   └── port/out/       ← interfaces que la infraestructura implementa
├── application/
│   └── usecase/        ← casos de uso @Transactional — solo dependen de domain
└── infrastructure/
    └── adapter/
        ├── in/web/     ← @RestController
        ├── in/camunda/ ← @JobWorker (Zeebe)
        ├── in/kafka/   ← @KafkaListener
        └── out/
            ├── persistence/ ← JPA entities + Spring Data + adapters
            ├── client/      ← RestClient + Resilience4j
            ├── invoice/     ← OpenPDF
            └── kafka/       ← Outbox relay
    └── config/         ← Spring config beans (SecurityConfig, RedisConfig, TenantFilter…)
```

### Reglas ArchUnit (verificadas en HexagonalArchitectureTest)
- `domain` NO depende de Spring/JPA/Hibernate/Kafka/Redis ni de `adapter`
- `application` NO depende de `adapter`
- `adapter/in/web` NO depende de `adapter/out`
- Sin dependencias cíclicas entre paquetes

### Patrón de persistencia
- Todos los JPA entities tienen getters/setters explícitos (sin Lombok en entidades)
- Arrays PostgreSQL → `@Type(ListArrayType.class)` de `com.vladmihalcea.hibernate.type.array`
- Optimistic locking → `@Version Long version` (NO en deliverables/payments que son append-only)
- Schema: `services` (explícito en `@Table(schema = "services")` Y en `spring.jpa.properties.hibernate.default_schema`)

### Patrón de escritura
- TODOS los writes de dominio pasan por `OutboxEventPublisher` con `PROPAGATION.MANDATORY`
- El `OutboxRelayScheduler` sondea la tabla `outbox_event` cada 5 s y publica en Kafka
- Nunca publicar directamente a Kafka desde un use case

### Multi-tenancy
- `TenantFilter` (OncePerRequestFilter, después de BearerTokenAuthenticationFilter) lee `tenant_id` del JWT y escribe en `TenantContext` (ThreadLocal)
- `TenantAwareDataSourceProxy` (DelegatingDataSource) emite `SELECT set_config('app.tenant_id', ?, false)` en cada `getConnection()`
- `TenantDataSourceConfig` (BeanPostProcessor) envuelve el bean `dataSource` auto-configurado
- La política RLS `so_tenant_isolation` en `service_orders` usa `current_setting('app.tenant_id', TRUE)::uuid`
- Sentinel de no-tenant: `00000000-0000-0000-0000-000000000000` (endpoints públicos)

---

## 4. INVENTARIO COMPLETO DE FICHEROS (109 Java)

### application/usecase/ (9 ficheros)
```
AcceptOrderUseCase.java
CancelOrderUseCase.java
ComparePackagesUseCase.java
CreateOrderUseCase.java
ListCatalogUseCase.java
PartnerAssignmentService.java
QuoteServiceUseCase.java
SlaDashboardQueryService.java
SlaMonitorScheduler.java
```
**FALTAN (crear):** `GetOrderTimelineUseCase`, `RenewPolicyUseCase`, `PartnerPayoutUseCase`

### domain/model/ (14 ficheros)
```
Deliverable.java, DeliverableKind.java, OrderStatus.java, Partner.java,
PartnerKind.java, Payment.java, PricingModel.java, ProfileSearchBrief.java,
PropertySearchBrief.java, ServiceCode.java (17 valores), ServiceDefinition.java,
ServiceDefinitionStatus.java, ServiceOrder.java, StatusChange.java
```

### domain/event/ (11 ficheros — todos Java records)
```
DeliverableIssuedEvent, OrderAcceptedEvent, OrderCancelledEvent, OrderCompletedEvent,
OrderCreatedEvent, OrderFailedEvent, OrderInProgressEvent, PaymentCapturedEvent,
PaymentRefundedEvent, PayoutSentEvent, SlaBreachedEvent
```

### domain/port/out/ (14 ficheros)
```
AreaClientPort, BankClientPort, CustomerClientPort, DeliverableRepository,
EventPublisher, NotificationPort, OutboxEventPublisher, PartnerRepository,
PaymentGatewayPort, PaymentRepository, ProductClientPort, ServiceCatalogRepository,
ServiceOrderRepository, WorkflowPort
```

### infrastructure/adapter/in/camunda/ (10 ficheros — COMPLETO)
```
AssignPartnerWorker (assignPartner)
ChargeCustomerWorker (chargeCustomer)
CloseOrderWorker (closeOrder) ← tiene TODO: call use case
GenerateInvoiceWorker (generateInvoice) ← nuevo, usa InvoiceGeneratorService
NotifyCustomerWorker (notifyCustomer)
PayoutPartnerWorker (payoutPartner)
QuotePriceWorker (quotePrice)
RequestPreapprovalWorker (requestPreapproval) ← nuevo, usa BankClientPort
ValidateInputsWorker (validateInputs) ← tiene TODO: JSON Schema validation
VerifyDeliverableWorker (verifyDeliverable) ← nuevo, usa DeliverableRepository
```

### infrastructure/adapter/in/kafka/ (7 ficheros — COMPLETO)
```
AreasZoneConsumer (magenta.areas.zone.v1)
BanksAppraisalConsumer (magenta.banks.appraisal.v1)
BanksPreapprovalConsumer (magenta.banks.preapproval.v1)
CustomersKycConsumer (magenta.customers.kyc.v1)
CustomersPreferencesConsumer (magenta.customers.preferences.v1) ← nuevo
ProductsPropertyConsumer (magenta.products.property.v1) ← nuevo
ProductsTransactionConsumer (magenta.products.transaction.v1)
```
Todos tienen `// TODO: verificar idempotencia en processed_message` (tabla pendiente S6)

### infrastructure/adapter/in/web/ (6 ficheros)
```
CatalogController, GlobalExceptionHandler, OrdersController,
PartnersController, SlaDashboardController, WebhookController
```
**FALTAN:** OpenApiConfig (springdoc bean)

### infrastructure/adapter/out/client/ (6 ficheros)
```
AreaRestClient, BankRestClient, CustomerRestClient,
NotificationRestAdapter, ProductRestClient, StripePaymentAdapter
```

### infrastructure/adapter/out/invoice/ (2 ficheros)
```
InvoiceGeneratorService (OpenPDF, genera PDF byte[])
InvoiceNumberingService (numeración atómica MAG-YYYY-NNNNN vía BD)
```
**FALTA:** firma XAdES (impacto fiscal en España)

### infrastructure/adapter/out/kafka/ (2 ficheros)
```
OutboxEventPublisherAdapter, OutboxRelayScheduler
```

### infrastructure/adapter/out/persistence/ (17 ficheros — COMPLETO)
```
DeliverableJpaEntity + DeliverableJpaRepository + DeliverableRepositoryAdapter
OutboxEventJpaEntity + OutboxEventJpaRepository
PartnerJpaEntity + PartnerJpaRepository + PartnerRepositoryAdapter
PaymentJpaEntity + PaymentJpaRepository + PaymentRepositoryAdapter
ServiceCatalogRepositoryAdapter
ServiceDefinitionJpaEntity + ServiceDefinitionJpaRepository
ServiceOrderJpaEntity + ServiceOrderJpaRepository + ServiceOrderRepositoryAdapter
```

### infrastructure/config/ (7 ficheros)
```
RedisConfig, SecurityConfig, StripeWebhookFilter,
TenantAwareDataSourceProxy, TenantContext, TenantDataSourceConfig, TenantFilter
```
**FALTAN:** `KafkaConfig`, `ObservabilityConfig`, `Resilience4jConfig` (bean explícito), `OpenApiConfig`

### test/ (3 ficheros)
```
architecture/HexagonalArchitectureTest (ArchUnit)
domain/PartnerAssignmentServiceTest
domain/ServiceOrderStateMachineTest
```
**FALTAN:** 8 tests descritos en §7

---

## 5. TRABAJO PENDIENTE — PRIORIZADO

### P1 — BLOQUEANTES de producción (implementar antes de cualquier despliegue)

#### P1-A: Tabla `processed_message` + idempotencia Kafka (S6)
Crear migración `V202506300003__processed_message.sql`:
```sql
CREATE TABLE services.processed_message (
    consumer_name VARCHAR(80)  NOT NULL,
    event_id      VARCHAR(120) NOT NULL,
    processed_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (consumer_name, event_id)
);
CREATE INDEX idx_pm_processed_at ON services.processed_message(processed_at);
```
Actualizar los 7 consumers Kafka para verificar idempotencia:
```java
// Patrón en cada consumer antes de procesar:
try {
    jdbcTemplate.update(
        "INSERT INTO services.processed_message(consumer_name,event_id) VALUES(?,?)",
        CONSUMER_NAME, eventId);
} catch (DuplicateKeyException e) {
    log.info("Evento ya procesado, ignorando: {}", eventId);
    return;
}
```

#### P1-B: `Idempotency-Key` en POST endpoints (S13)
Crear `IdempotencyFilter` o interceptor que:
1. Lee cabecera `Idempotency-Key: <uuid>` (obligatoria en POST /orders, /catalog/*/quote, /partners, /webhooks/stripe, /webhooks/partners/*)
2. Busca en Redis TTL 24h: si existe, devuelve la respuesta cacheada (304 + Location header)
3. Si no existe, ejecuta el handler, almacena respuesta en Redis con la key
4. Devuelve 400 si la key falta en endpoints que la requieren

#### P1-C: `KafkaConfig` bean explícito (S18)
Fichero: `infrastructure/config/KafkaConfig.java`
```java
@Configuration
public class KafkaConfig {
    // ProducerFactory con acks=all, enable.idempotence=true, compression.type=snappy
    // ConsumerFactory con isolation.level=read_committed (exactamente-una-vez semántica)
    // KafkaTemplate<String, String>
    // DeadLetterPublishingRecoverer → topic magenta.servicios.dlq.v1
    // SeekToCurrentErrorHandler con backoff 3x1000ms
    // Envelope CloudEvents: content-type: application/cloudevents+json
}
```
Tópicos producidos (crear si no existen, replication-factor=3 en prod):
```
magenta.servicios.workflow.v1
magenta.servicios.deliverable.v1
magenta.servicios.payment.v1
magenta.servicios.sla.v1
magenta.servicios.dlq.v1  ← nuevo DLQ
```

#### P1-D: `ObservabilityConfig` (S18)
Fichero: `infrastructure/config/ObservabilityConfig.java`
```java
@Configuration
public class ObservabilityConfig {
    // Counter: servicios.orders.created / .completed / .cancelled / .failed (by serviceCode, tenant)
    // Gauge: servicios.sla.breached.active (órdenes con SLA roto activas)
    // Timer: servicios.order.duration (DRAFT→COMPLETED, percentiles 0.5, 0.95, 0.99)
    // Counter: servicios.payment.captured / .refunded (by serviceCode)
    // @Observed en use cases críticos
}
```

#### P1-E: Use cases faltantes (S7)
**`GetOrderTimelineUseCase`** — `application/usecase/GetOrderTimelineUseCase.java`
- Lee `service_order_events` (tabla de historial) por `order_id`
- Retorna `List<StatusChange>` ordenado por `at ASC`
- Llamado desde `OrdersController.getTimeline()`

**`RenewPolicyUseCase`** — `application/usecase/RenewPolicyUseCase.java`
- Aplica a ServiceCode: RENT_DEFAULT_INSURANCE
- Crea nueva `ServiceOrder` clonando inputs de la anterior con `status=DRAFT`
- Emite `OrderCreatedEvent` vía Outbox
- Endpoint: `POST /orders/{id}/renew`

**`PartnerPayoutUseCase`** — `application/usecase/PartnerPayoutUseCase.java`
- Busca pagos CAPTURED con `direction=INBOUND` y `invoice_number` asignado sin payout
- Para cada partner, calcula `amount * commissionPct / 100`
- Llama `PaymentGatewayPort.createPayout(partnerId, amount, sepaIban)`
- Emite `PayoutSentEvent` vía Outbox
- Scheduler semanal (lunes 06:00 UTC) o endpoint admin `POST /admin/partner-payouts`

### P2 — Seguridad (requeridas para pasar auditoría)

#### P2-A: Vault config + cifrado IBAN (S10)
Fichero: `infrastructure/config/VaultConfig.java`
- `spring-cloud-starter-vault-config` ya está en pom.xml
- `bootstrap.yml` con AppRole auth: `spring.cloud.vault.authentication=APPROLE`
- Cifrado envelope: KEK en Vault Transit engine, DEK por registro en `partner.sepa_iban_enc`
- `PartnerIbanEncryptionService`: `encrypt(String plainIban) → byte[]`, `decrypt(byte[] enc) → String`
- Integrar en `PartnerRepositoryAdapter.toEntity()` y `toDomain()`
- NUNCA loguear IBANs — ver P2-D

#### P2-B: URLs firmadas para Deliverable.storageUri (S11)
- Sustituir URIs `invoice://...` y `file://...` por URLs prefirmadas con TTL 5 min
- Integrar con AWS S3 / GCS / MinIO según infraestructura destino
- `SignedUrlService`: `generateUploadUrl(key, ttlMinutes) → String`, `generateDownloadUrl(key, ttlMinutes) → String`
- Verificar `customerId` o `partnerId` antes de generar URL (BOLA check)
- `GenerateInvoiceWorker` debe subir el PDF y almacenar la URL firmada en `Deliverable.storageUri`

#### P2-C: Tests BOLA / autorización (S12)
- `OrdersController`: un customerId ajeno no puede ver órdenes de otro, aunque comparta tenant
- `PartnersController`: un partner no puede ver datos de otro partner
- Patrón de verificación:
  ```java
  @PreAuthorize("hasRole('ADMIN') or #customerId == authentication.token.claims['sub']")
  ```
- Test mínimo por endpoint sensible: 401 (sin JWT), 403 (JWT válido pero sin permiso), 200 (permiso correcto)

#### P2-D: Logback PII masker (S15)
Fichero: `src/main/resources/logback-spring.xml`
- Convertidor que reemplaza con `[REDACTED]` los valores de:
  - `iban`, `sepa_iban`, `nif`, `nie`, `email`, `phone`, `customerId` (en JSON de logs)
- Activar en todos los profiles incluyendo dev

### P3 — Calidad (tests necesarios para CI gate)

#### P3-A: Tests de integración Testcontainers (S20)
**`CatalogControllerIT`** (`src/test/java/.../integration/`)
- PostgreSQL 18 via Testcontainers + Flyway auto-apply
- `GET /api/v1/catalog` → 200 + lista de 17 ServiceDefinitions
- `POST /api/v1/catalog/APPRAISAL/quote` con inputs válidos → 200 + precio
- `GET /api/v1/catalog/UNKNOWN_CODE` → 404 ProblemDetail

**`OrdersControllerIT`**
- PostgreSQL + Kafka (Testcontainers) + MockMvc
- JWT real generado con `jose4j` o `nimbus-jose-jwt` con claim `tenant_id`
- Flujo completo: POST /orders → 201, GET /orders/{id} → 200, PATCH /orders/{id} → 200
- Verificar que RLS devuelve 0 filas con `tenant_id` diferente

**`StripeWebhookIT`**
- WireMock del endpoint Stripe
- POST /api/v1/webhooks/stripe con body firmado (calcular HMAC-SHA256 en el test)
- Verificar 200 con firma válida, 400 con firma inválida, 400 con replay (mismo `Stripe-Signature`)

#### P3-B: Tests BPMN (S21)
**`FirstHomeAidBpmnTest`** — `zeebe-process-test:8.7.0`
- Deploy `bpmn/first-home-aid.bpmn` en motor embebido
- Mockear todos los job workers
- Ejecutar instancia con variables de entrada válidas
- Verificar que todos los service tasks se completan y el proceso termina en el end event esperado

**`AppraisalBpmnTest`** — similar para `bpmn/appraisal.bpmn`

#### P3-C: Tests de fórmulas de precio (S22)
**`PriceFormulaEvaluatorTest`**
- Cubrir los 17 ServiceCode con distintos inputs (precio fijo, % comisión, fórmula SpEL)
- Verificar que `QuoteServiceUseCase` devuelve el precio correcto
- Casos borde: inputs null, moneda distinta, SLA overdue

### P4 — Infraestructura / DevOps

#### P4-A: `OpenApiConfig` (S19)
Fichero: `infrastructure/config/OpenApiConfig.java`
```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info().title("Magenta Servicios API").version("1.0.0")
                .description("Marketplace de servicios inmobiliarios"))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth",
                    new SecurityScheme().type(HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}
```
Anotar todos los controllers con `@Tag`, endpoints con `@Operation`, DTOs con `@Schema`.

#### P4-B: Migración `invoice_sequences` (falta en Flyway)
La tabla `invoice_sequences` que usa `InvoiceNumberingService` no está en ninguna migración.
Añadir a `V202506300001__init_schema.sql` o crear `V202506300004__invoice_sequences.sql`:
```sql
CREATE TABLE services.invoice_sequences (
    series      VARCHAR(10) NOT NULL,
    year        INTEGER     NOT NULL,
    last_number INTEGER     NOT NULL DEFAULT 0,
    PRIMARY KEY (series, year)
);
```

#### P4-C: Pipeline CI/CD (pendiente)
Fichero: `.github/workflows/ci.yml` o equivalente
Pasos obligatorios:
1. `mvn verify` — compila + tests unitarios + ArchUnit
2. `mvn verify -Pintegration-tests` — Testcontainers IT
3. OWASP Dependency Check (`mvn org.owasp:dependency-check-maven:check`)
4. Trivy scan imagen Docker
5. `mvn spotbugs:check` o `semgrep --config=auto`
6. Build imagen → push a registry
7. `helm upgrade --install` en cluster staging con `--atomic --timeout 5m`
8. Smoke test: `GET /actuator/health` → 200

#### P4-D: Firma XAdES facturas (S23)
- Dependencia: `xades4j` o `DSS (Digital Signature Service)` de la CE
- Certificado: certificado de empresa (`PKCS12`) en Vault PKI
- `InvoiceSignerService`: `sign(byte[] pdf, X509Certificate cert, PrivateKey key) → byte[]`
- Integrar en `GenerateInvoiceWorker` después de `generateInvoicePdf()`
- Obligatorio para facturas con validez fiscal en España (AEAT)

#### P4-E: Pact contract tests (S24)
Fichero: `src/test/java/.../contract/`
- Consumer Pact para `BankRestClient`: define contratos de `POST /api/v1/preapprovals`, `GET /api/v1/financing-feasibility`
- Consumer Pact para `CustomerRestClient`: contrato de `GET /api/v1/customers/{id}/profile`
- Consumer Pact para `ProductRestClient`: contrato de `GET /api/v1/properties/{id}`
- Publicar en Pact Broker al final del CI

#### P4-F: Cursor pagination (S14)
`GET /orders`, `GET /partners`, `GET /sla-dashboard` usan `Pageable` (offset).
Para producción con grandes volúmenes, implementar cursor-based:
- Añadir `cursor` (opaque base64 del último id+timestamp) en la respuesta
- `ServiceOrderRepository.findAfterCursor(String cursor, int limit, UUID tenantId)`
- Romper compatibilidad controlada via header `Accept: application/vnd.magenta.v2+json`

---

## 6. CHECKLIST DE PRODUCCIÓN

### Infraestructura
```
[ ] PostgreSQL 18 con SSL obligatorio (sslmode=require), backups PITR 30 días
[ ] Redis 8.0 con autenticación (requirepass) y TLS
[ ] Kafka 4 KRaft: 3 brokers, replication.factor=3, min.insync.replicas=2
[ ] Zeebe 8.7.0: 3 nodos, exportador Elasticsearch/OpenSearch habilitado
[ ] Keycloak 26: realm magenta, cliente servicios-api con RS256, JWKS rotación automática
[ ] Vault: AppRole auth, Transit engine para IBAN, PKI para certs factura XAdES
[ ] Object storage (S3/GCS/MinIO) para Deliverables con lifecycle 7 años (fiscal)
[ ] OTel Collector → Jaeger/Tempo para trazas, Prometheus para métricas, Loki para logs
```

### Aplicación
```
[ ] Variables de entorno inyectadas desde Vault (no en secretos K8s en claro)
[ ] JVM flags: --add-opens (Java 25 + Hibernate 6), -XX:MaxRAMPercentage=75
[ ] Readiness probe: /actuator/health/readiness (espera BD + Kafka + Redis ready)
[ ] Liveness probe: /actuator/health/liveness (solo JVM + app, NO dependencias externas)
[ ] PodDisruptionBudget minAvailable=1 (ya en Helm)
[ ] HPA: CPU 70% → escalar hasta 5 réplicas (añadir en Helm values.yaml)
[ ] NetworkPolicy: only ingress from gateway, egress to postgres/kafka/redis/zeebe/keycloak
[ ] Distroless runtime no-root (ya en Dockerfile: uid=10001)
[ ] readOnlyRootFilesystem=true (ya en Helm) → montar /tmp como emptyDir
```

### Datos
```
[ ] Migración Flyway ejecutada en prod antes del despliegue (baselineOnMigrate=false)
[ ] Seed de ServiceDefinitions (V202506300002) verificado
[ ] Tabla invoice_sequences creada (P4-B)
[ ] Tabla processed_message creada (P1-A)
[ ] RLS activado y verificado en service_orders: test manual SELECT con tenant_id incorrecto
[ ] Backup pre-despliegue automatizado en pipeline
[ ] GDPR: política de retención de datos — service_orders con deleted_at > 5 años → purga
```

### Seguridad pre-go-live
```
[ ] Vault IBAN encryption operativo (P2-A)
[ ] Stripe webhook secret rotado y configurado (no el de staging)
[ ] Keycloak JWKS con RSA 2048+ rotación trimestral
[ ] CORS only permite https://app.magenta.es en prod
[ ] CSP headers añadidos (via gateway o filtro Spring)
[ ] Dependencias sin CVE High/Critical (OWASP Dependency Check gate en CI)
[ ] Imagen Docker sin CVE High/Critical (Trivy scan gate en CI)
[ ] Pruebas de penetración básicas (OWASP ZAP o similar) antes de launch
```

---

## 7. CRITERIOS DE AUDITORÍA

### OWASP API Security Top 10 (2023)

| ID | Riesgo | Estado actual | Acción requerida |
|----|--------|---------------|------------------|
| API1 | BOLA | ⚠️ Parcial | Implementar P2-C: tests + @PreAuthorize por customerId/partnerId |
| API2 | Broken Authentication | ✅ JWT RS256 Keycloak | Verificar expiración (max 1h), refresh token rotation |
| API3 | Broken Object Property Level Auth | ⚠️ Sin revisar | Auditar qué campos devuelve cada endpoint a qué rol |
| API4 | Unrestricted Resource Consumption | ⚠️ Sin rate limit | Añadir `spring-boot-starter-actuator` rate limit o Envoy ratelimit en gateway |
| API5 | Broken Function Level Auth | ✅ @EnableMethodSecurity | Verificar que admin endpoints tienen @PreAuthorize("hasRole('ADMIN')") |
| API6 | Unrestricted Access to Sensitive Business Flows | ⚠️ Sin Idempotency-Key | Implementar P1-B |
| API7 | SSRF | ✅ No hay URLs externas dinámicas | Mantener — no introducir endpoints que fetch URLs de usuario |
| API8 | Security Misconfiguration | ⚠️ Sin headers CSP/HSTS | Añadir SecurityHeadersFilter o configurar en ingress |
| API9 | Improper Inventory Management | ⚠️ Sin OpenAPI completa | Implementar P4-A + publicar en dev portal |
| API10 | Unsafe Consumption of APIs | ✅ Resilience4j en todos los clientes | Verificar timeouts < 2s en todos los RestClient |

### OWASP Top 10 (2021) — Aplicación

| ID | Riesgo | Estado | Acción |
|----|--------|--------|--------|
| A01 | Broken Access Control | ⚠️ | P2-C tests BOLA |
| A02 | Crypto Failures | ⚠️ IBAN en claro | P2-A Vault encryption |
| A03 | Injection | ✅ JPA parameterizado, native queries con :param | Mantener |
| A04 | Insecure Design | ✅ Hexagonal, ports & adapters | Mantener |
| A05 | Security Misconfiguration | ⚠️ | Headers HTTP, Vault secrets |
| A06 | Vulnerable Components | ⚠️ No gateado | P4-C OWASP Dependency Check en CI |
| A07 | Auth/Auth Failures | ✅ | JWT fijo, CORS configurado |
| A08 | Integrity Failures | ✅ Outbox + Kafka idempotente | P1-A tabla processed_message |
| A09 | Logging Failures | ⚠️ PII en logs | P2-D Logback masker |
| A10 | SSRF | ✅ | Mantener |

### ASVS L2 — Application Security Verification Standard (mínimo requerido)

```
Verificar:
[V1]  Arquitectura → Hexagonal ✅, threat model pendiente
[V2]  Autenticación → JWT RS256 ✅, MFA en Keycloak (configurar en realm)
[V3]  Gestión de sesiones → Stateless ✅
[V4]  Control de acceso → @PreAuthorize ✅, BOLA tests pendientes
[V5]  Validación → Bean Validation en DTOs, JSON Schema en ValidateInputsWorker (TODO)
[V6]  Criptografía → IBAN en claro ⚠️ (P2-A), TLS en tránsito pendiente
[V7]  Manejo de errores → GlobalExceptionHandler ✅, PII en logs ⚠️ (P2-D)
[V8]  Protección de datos → GDPR retención ⚠️, PII masker ⚠️
[V9]  Comunicación → HTTPS obligatorio en prod, HSTS pendiente
[V10] Código malicioso → Dependency Check pendiente (P4-C)
[V11] Lógica de negocio → Idempotency-Key pendiente (P1-B)
[V12] Ficheros → Deliverable URLs firmadas pendiente (P2-B)
[V13] API y servicios web → OpenAPI pendiente (P4-A), rate limit pendiente
[V14] Configuración → Vault secrets ✅ (parcial), secrets en env vars ⚠️ (mover a Vault)
```

### PCI-DSS v4.0 (aplica por integración Stripe Connect)

```
Req 6.4.1 → Patch de componentes vulnerables → P4-C CI gate
Req 6.4.3 → Scripts de pago (no aplica — Stripe.js en frontend)
Req 7     → Control de acceso → @PreAuthorize + tenant isolation ✅
Req 8     → Gestión de credenciales → Vault ✅ (cuando P2-A complete)
Req 10    → Logging → sin PII ⚠️ → P2-D
Req 11.3  → Penetration testing → pendiente
Req 12.3  → Inventario de dependencias → SBOM (mvn cyclonedx:makeAggregateBom)
```

### RGPD / GDPR (aplica a datos de clientes españoles)

```
Categorías de datos personales procesados:
- Nombres, NIF/NIE, email, teléfono → en service_orders.inputs (JSONB)
- IBAN → en partners.sepa_iban_enc
- Historial de estado → service_order_events

Medidas requeridas:
[ ] Política retención: service_orders.deleted_at + purga automática a 5 años (fiscal)
[ ] Endpoint RTBF (Right to be Forgotten): DELETE /api/v1/customers/{id}/data
    → anonimizar inputs en service_orders, eliminar datos en profile_search_briefs
[ ] Registro de actividades de tratamiento (art. 30 RGPD) — documento externo
[ ] DPA firmado con Stripe, AWS/GCS, Vault operator
[ ] DPIA si procesáis datos de menores o datos financieros a gran escala
[ ] Consentimiento explícito en frontend antes de crear orders (fuera de este módulo)
```

### Ley 58/2003 General Tributaria (facturas electrónicas — España)

```
[ ] Facturas deben ser ilegibles = firmadas digitalmente (XAdES-BES mínimo) → P4-D
[ ] Serie y número correlativo sin huecos → InvoiceNumberingService ✅ (atómica en BD)
[ ] Conservación 5 años (object storage con lifecycle) → pendiente
[ ] Formato SII (Suministro Inmediato de Información) a AEAT si facturación > 6M€/año
[ ] Facturae 3.2.x si clientes son empresas con VeriFactu obligatorio (2025+)
```

---

## 8. DOCUMENTACIÓN QUE DEBE EXISTIR AL CIERRE

### Documentación técnica (en el repositorio)
```
docs/
├── adr/
│   ├── ADR-001-spring-boot-4-upgrade-path.md  ✅ existe
│   ├── ADR-002-camunda-8-decision.md          [ ] crear — justifica Zeebe vs máquina estados
│   └── ADR-003-stripe-connect-payout.md       [ ] crear — documenta flujo de liquidación
├── api/
│   └── openapi.yaml                           [ ] generado desde SpringDoc (mvn generate)
├── runbooks/
│   ├── runbook-incident-order-stuck.md        [ ] crear
│   ├── runbook-kafka-lag.md                   [ ] crear
│   └── runbook-sla-breach-escalation.md       [ ] crear
└── architecture/
    └── context-map.md                         [ ] crear — relaciones con otros BCs Magenta
```

### MODULE-SPEC.md — actualizar cabecera (S16, drift documental)
Línea actual: `Java 21 · Spring Boot 3.3 · PostgreSQL 16 · Angular 17 · Kafka 3.7`
Debe decir: `Java 25 · Spring Boot 3.5.2 · PostgreSQL 18 · Kafka 4 KRaft · Redis 8`

### ARCHITECTURE.md — marcar obsoleto (S17)
Añadir al inicio:
```markdown
> ⚠️ OBSOLETO — Este documento es la versión v1.0 original.
> El estado actual se refleja en PENDIENTES.md y AI-HANDOFF.md.
> Ver docs/adr/ para decisiones de arquitectura vigentes.
```

### README.md — debe existir con
```markdown
# magenta-servicios
## Quick start (docker compose up)
## Variables de entorno
## Endpoints principales
## Estructura de paquetes
## Ejecutar tests
## Deploy Helm
```

### Documentación de auditoría externa (entregar a auditores)
```
[ ] Threat model (STRIDE) del módulo servicios
[ ] Diagrama de flujo de datos con clasificación PII
[ ] Inventario de dependencias (SBOM CycloneDX)
[ ] Informe Trivy de vulnerabilidades con mitigaciones
[ ] Evidencia de penetration testing
[ ] Registro de actividades de tratamiento RGPD art.30
[ ] Certificado SSL/TLS del endpoint de producción
[ ] Evidencia de Vault en producción (no secretos en env vars)
```

---

## 9. BUGS CONOCIDOS Y TECHNICAL DEBT

### Bugs pendientes de fix
1. **`CloseOrderWorker`** — tiene `// TODO: call use case` → no actualiza el estado de la orden en BD al cerrar. Debe llamar a un use case que transite a `COMPLETED`.
2. **`ValidateInputsWorker`** — tiene `// TODO: JSON Schema validation` → actualmente no valida los inputs. Debe validar el campo `inputs_schema` del `ServiceDefinition` correspondiente.
3. **`ProductsTransactionConsumer`** — tiene `// TODO: auto-create NOTARY_GESTORIA + UTILITIES_SETUP orders` → no crea las órdenes de servicio encadenadas cuando se recibe una transacción de producto.
4. **`GenerateInvoiceWorker`** — `storageUri = "invoice://{invoiceNumber}"` es un placeholder. Debe sustituirse por URL firmada de object storage (P2-B).
5. **Kafka consumers** (los 7) — sin verificación de idempotencia. Procesar dos veces el mismo evento puede duplicar órdenes o pagos (P1-A).

### Technical debt
- `ServiceOrderJpaEntity` — el campo `inputs` es `String` (JSON serializado a mano). Debería ser `@JdbcTypeCode(SqlTypes.JSON)` de Hibernate 6 con deserialización automática.
- `StripePaymentAdapter` — sin retry en `capturePayment()` (Stripe acepta idempotent retries con `IdempotencyKey`).
- Los 7 BPMN tienen tasks de tipo `zeebe:taskDefinition` sin definir la variable `errorCode` en los boundary events de error. Si un worker lanza `ZeebeBpmnError`, el proceso puede quedar en estado indefinido.
- `InvoiceGeneratorService` — usa `FontFactory.getFont()` sin cache, costoso. Cachear en `@PostConstruct`.
- `PartnerAssignmentService` — selección de partner es `findFirst()` (orden no determinista). Implementar rule engine con scoring por `rating`, `commissionPct` y disponibilidad de zona.

---

## 10. PATRONES DE CÓDIGO — CÓMO CODIFICAR EN ESTE PROYECTO

### Crear un nuevo use case
```java
// application/usecase/NuevoUseCase.java
@Component
@Transactional
public class NuevoUseCase {
    private final ServiceOrderRepository orderRepo;
    private final OutboxEventPublisher outbox;
    // Inyectar solo puertos de dominio, nunca JPA ni Spring directamente
    
    public Result execute(Command cmd) {
        ServiceOrder order = orderRepo.findByIdAndTenantId(cmd.orderId(), cmd.tenantId())
            .orElseThrow(() -> new NoSuchElementException("Order not found"));
        // lógica de dominio
        outbox.publish(new SomeDomainEvent(...)); // PROPAGATION.MANDATORY
        return orderRepo.save(order);
    }
}
```

### Crear un nuevo JPA adapter
```java
// infrastructure/adapter/out/persistence/XxxJpaEntity.java
@Entity @Table(name = "xxx", schema = "services")
public class XxxJpaEntity {
    // arrays PG: @Type(ListArrayType.class) @Column(columnDefinition = "TEXT[]")
    // temporal: Instant (no LocalDateTime — siempre UTC)
    // optimistic lock: @Version Long version (excepto tablas append-only)
}
// infrastructure/adapter/out/persistence/XxxRepositoryAdapter.java
@Component
public class XxxRepositoryAdapter implements XxxRepository {
    // mapeo domain↔entity en métodos privados toEntity()/toDomain()
}
```

### Crear un nuevo Kafka consumer
```java
@KafkaListener(topics = "magenta.xxx.v1",
               groupId = "${spring.kafka.consumer.group-id}",
               containerFactory = "kafkaListenerContainerFactory")
public void consume(ConsumerRecord<String, String> record) {
    // 1. Parsear JSON con ObjectMapper
    // 2. Verificar idempotencia: INSERT INTO processed_message (P1-A)
    // 3. Lógica de negocio
    // 4. Re-throw RuntimeException para DLT si falla
}
```

### Crear un nuevo job worker Camunda
```java
@JobWorker(type = "nuevoJobType")
public Map<String, Object> nuevoJob(@Variable String orderId,
                                    @Variable(required = false) String opcional) {
    // inyectar solo domain ports o infrastructure services
    // retornar Map con variables Zeebe de salida
    // en caso de error recuperable: throw new ZeebeBpmnError("errorCode", "message")
    // en caso de error técnico: dejar que se reintente (Zeebe backoff)
}
```

### Errores HTTP
```java
// Usar siempre las excepciones mapeadas en GlobalExceptionHandler:
throw new NoSuchElementException("Order not found");         // → 404
throw new IllegalArgumentException("Invalid status");        // → 400
throw new IllegalStateException("Order already completed");  // → 409
throw new SecurityException("Access denied");                // → 403
// La respuesta es application/problem+json (RFC 9457)
```

---

## 11. DEUDA DOCUMENTAL ESPECÍFICA

### MODULE-SPEC.md — cabecera a actualizar
Reemplazar en la primera sección:
- `Java 21` → `Java 25 LTS`
- `Spring Boot 3.3` → `Spring Boot 3.5.2`
- `PostgreSQL 16` → `PostgreSQL 18`
- `Kafka 3.7` → `Kafka 4 KRaft (Confluent CP 8.0)`
- Añadir: `Redis 8.0`, `Camunda 8 / Zeebe 8.7.0`

### ADR-002 — Camunda 8 decision
Crear `docs/adr/ADR-002-camunda-8-zeebe.md` con:
- Contexto: 17 servicios, tareas humanas, SLA explícito, auditabilidad de cada paso
- Decisión: Camunda 8 Zeebe
- Alternativas consideradas: Spring State Machine, máquina de estados en BD, Temporal.io
- Consecuencias: BPMN desplegables, workers Java, Zeebe en cluster K8s
- Deuda: actualizar a 8.8+ cuando salga, plan test BPMN tests

### Context map
Crear `docs/architecture/context-map.md` con los bounded contexts de Magenta que se integran con `servicios`:
```
servicios ←→ banks     (preapproval, appraisal via Kafka + REST sync)
servicios ←→ products  (property, transaction via Kafka)
servicios ←→ customers (kyc, preferences via Kafka + REST sync)
servicios ←→ areas     (zone via Kafka + REST sync)
servicios ──→ payments (Stripe Connect, no BC propio)
servicios ──→ notifications (notificaciones push/email, REST fire-and-forget)
```

---

## 12. ORDEN DE EJECUCIÓN RECOMENDADO PARA CERRAR EL PROYECTO

```
Fase A — Completar código faltante (sin tests):
  1. P4-B: migración invoice_sequences (5 min)
  2. P1-A: migración processed_message + idempotencia en 7 consumers (1h)
  3. P1-E: GetOrderTimelineUseCase + RenewPolicyUseCase + PartnerPayoutUseCase (2h)
  4. P4-A: OpenApiConfig + anotaciones @Operation en controllers (1h)
  5. P1-C: KafkaConfig bean + DLQ (1h)
  6. P1-D: ObservabilityConfig + métricas de negocio (1h)
  7. Fixes bugs: CloseOrderWorker, ValidateInputsWorker JSON Schema (1h)

Fase B — Seguridad (sin tests):
  8.  P2-D: Logback PII masker (30 min)
  9.  P1-B: IdempotencyFilter (1h)
  10. P2-A: VaultConfig + PartnerIbanEncryptionService (2h)
  11. P2-B: SignedUrlService para Deliverables (2h)
  12. P2-C: @PreAuthorize BOLA checks en controllers (1h)

Fase C — Tests:
  13. P3-C: PriceFormulaEvaluatorTest (30 min)
  14. P3-A: CatalogControllerIT + OrdersControllerIT + StripeWebhookIT (4h)
  15. P3-B: FirstHomeAidBpmnTest + AppraisalBpmnTest (2h)
  16. P2-C: AuthorizationTests (BOLA) (2h)
  17. P4-E: Pact consumer contracts (2h)

Fase D — DevOps:
  18. P4-C: CI/CD pipeline YAML (2h)
  19. P4-D: XAdES invoice signing (4h — requiere certificado)
  20. P4-F: Cursor pagination (2h)

Fase E — Documentación:
  21. README.md
  22. MODULE-SPEC.md cabecera (S16)
  23. ARCHITECTURE.md nota obsoleto (S17)
  24. ADR-002, ADR-003
  25. Context map
  26. Runbooks
  27. SBOM: mvn cyclonedx:makeAggregateBom
```

**Estimación total:** ~35-40 horas de codificación efectiva para una IA sin interrupciones.

---

## 13. CHECKSUMS DE INTEGRIDAD

Verificar que estos ficheros existen y son no-vacíos antes de empezar:
```powershell
# Ejecutar desde c:\t\Magenta\servicios
Get-ChildItem -Recurse -File src -Include "*.java" | Measure-Object | Select Count
# Resultado esperado: 109

Get-ChildItem src\main\resources\db\migration | Select Name
# Esperado: V202506300001__init_schema.sql, V202506300002__seed_catalog.sql

Get-ChildItem src\main\resources\bpmn | Select Name
# Esperado: 7 ficheros .bpmn

Get-ChildItem charts\servicios\templates | Select Name
# Esperado: deployment.yaml, service.yaml, ingress.yaml, hpa.yaml, pdb.yaml (mínimo)
```

---

*Documento generado automáticamente a 2026-05-30. Actualizar junto con PENDIENTES.md.*
