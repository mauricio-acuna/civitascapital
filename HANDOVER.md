# MAGENTA PLATFORM — Handover Document v1.0

> **Fecha**: 2026-05-30  
> **Propósito**: Documento de traspaso completo para que un agente de IA (u otro equipo) pueda
> continuar el desarrollo, desplegar en producción, superar auditorías y presentar la documentación.  
> **Fuente de verdad**: Este documento refleja el estado real del código en disco a 30/05/2026.
> Leer junto a `ARCHITECTURE.md` de cada módulo (son copias del mismo documento canónico).

---

## 0. Cómo usar este documento

```
LECTURA RECOMENDADA:
  1. §1  → Visión y arquitectura global
  2. §2  → Stack fijado (no cambiar sin ADR)
  3. §3  → Estado real de cada módulo (✅ / ⬜)
  4. §4  → Plan de cierre: qué falta para v1.0
  5. §5  → Guía de producción paso a paso
  6. §6  → Checklist de auditoría (OWASP / RGPD / BdE)
  7. §7  → Índice de documentación
  8. §8  → Reglas operativas para el agente continuador
```

---

## 1. Visión y arquitectura global

### 1.1 Qué es Magenta

Plataforma SaaS inmobiliaria española con tres flujos:
- **Adquisición**: compra / hipoteca / permuta / 90+5+5.
- **Alquiler**: residencial y comercial con cobertura de impagos.
- **Decisión asistida**: comparativa alquiler-vs-compra, simulación hipotecaria, capacidad crediticia (≤35% esfuerzo BdE, ≤40% endeudamiento).

### 1.2 Bounded contexts (módulos)

| Módulo     | Contexto                      | Carpeta raíz           | Puerto dev | BD         | Esquema    |
|------------|-------------------------------|------------------------|------------|------------|------------|
| `areas`    | Geografía y mercado por zona  | `c:\t\Magenta\areas\`  | 8081       | areas_db   | areas      |
| `banks`    | Entidades financieras/crédito | `c:\t\Magenta\banks\`  | 8082       | banks_db   | banks      |
| `customers`| Identidad, perfil, solvencia  | `c:\t\Magenta\customers\` | 8083    | customers_db | customers |
| `products` | Catálogo de inmuebles         | `c:\t\Magenta\products\` | 8084     | products_db | products  |
| `servicios`| Servicios y workflows         | `c:\t\Magenta\servicios\` | 8085    | services_db | servicios |

> **Regla absoluta**: ningún módulo accede a tablas de otro módulo. Comunicación = REST síncrono O eventos Kafka.

### 1.3 Diagrama de comunicación

```
                   ┌─────────────────────────────┐
                   │   Angular SPA (BFF Web)      │
                   └──────────────┬───────────────┘
                                  │ HTTPS + JWT (RS256)
                   ┌──────────────▼───────────────┐
                   │  API Gateway (Spring Cloud    │
                   │  Gateway + WAF + rate-limit)  │
                   └──┬────────┬────────┬─────────┘
                      │        │        │
            ┌─────────▼┐  ┌───▼────┐  ┌▼──────────┐  ┌──────────┐
            │  areas   │  │ banks  │  │ customers  │  │ products │
            │  :8081   │  │ :8082  │  │  :8083     │  │  :8084   │
            └──────────┘  └────────┘  └────────────┘  └──────────┘
                     ▲         ▲            ▲               ▲
                     └─────────┴── Kafka ───┴───────────────┘
                                   (KRaft)
                                      ▲
                              ┌───────┴──────┐
                              │  servicios   │
                              │   :8085      │
                              └──────────────┘
```

### 1.4 Package raíz por módulo

```
com.magenta.areas.*
com.magenta.banks.*
com.magenta.customers.*
com.magenta.products.*
com.magenta.servicios.*
```

### 1.5 Estructura interna estándar (hexagonal + DDD)

```
src/main/java/com/magenta/<modulo>/
 ├─ application/              # Casos de uso (@Service, un método execute())
 ├─ domain/
 │   ├─ model/                # Aggregate Roots, Value Objects, Enums
 │   ├─ event/                # DomainEvents (sin Spring/JPA/Kafka)
 │   ├─ port/in/              # Interfaces de entrada (use case commands)
 │   └─ port/out/             # Interfaces de salida (repos, clients, publishers)
 ├─ infrastructure/
 │   ├─ adapter/in/web/       # @RestController + DTOs + MapStruct mappers
 │   ├─ adapter/in/kafka/     # @KafkaListener consumers
 │   ├─ adapter/out/persistence/ # @Entity + Spring Data JPA repos + adapters
 │   ├─ adapter/out/kafka/    # KafkaTemplate producers + OutboxPoller
 │   ├─ adapter/out/client/   # RestClient hacia otros módulos
 │   └─ config/               # @Configuration beans
 └─ MagentaXApplication.java
```

**Reglas de dependencia (enforced por ArchUnit en cada módulo)**:
- `domain` no importa Spring, JPA, Kafka, ni Redis.
- `application` no importa `adapter.*`.
- `adapter.in.*` no importa `adapter.out.*` directamente.
- Todos los controllers en `adapter.in.web`.
- Todas las @Entity en `adapter.out.persistence`.

---

## 2. Stack tecnológico fijado

### 2.1 Stack `banks` (alineado con IDEABASE baseline 30/05/2026)

| Capa             | Tecnología                          | Versión          |
|------------------|-------------------------------------|------------------|
| Lenguaje         | Java                                | **25 LTS**       |
| Framework        | Spring Boot                         | **4.0.6**        |
| Build            | Maven                               | 3.9.x            |
| Persistencia     | Spring Data JPA + Hibernate         | 6.6.x            |
| Migraciones      | Flyway                              | 10.x             |
| Base de datos    | PostgreSQL                          | **18**           |
| Mensajería       | Kafka (Confluent KRaft)             | **8.0.0**        |
| Cache            | Redis                               | **8**            |
| Auth             | Keycloak OIDC + JWT RS256           | 24.x             |
| Resiliencia      | Resilience4j                        | 2.3.0            |
| Observabilidad   | OTel + Micrometer + Prometheus      | OTel 1.32        |
| Mapeo            | MapStruct + Lombok                  | 1.6.2 / 1.18.36  |
| Seguridad        | Spring Cloud Vault (AES-256-GCM)    | 4.2.0            |
| Rate limiting    | Bucket4j                            | 8.10.1           |
| Tests            | JUnit 5, Testcontainers, WireMock 4, ArchUnit 1.3 | —   |
| Contenedor       | Distroless `gcr.io/distroless/java25-debian12:nonroot` | — |

> Documento ADR: no existe aún para `banks` (sus versiones son el baseline); ver `areas/docs/adr/ADR-001-stack-versions.md` para entender la divergencia del resto de módulos.

### 2.2 Stack `areas` y módulos restantes (v1.0 — ADR-001)

| Capa             | Tecnología                | Versión pom.xml | Versión docker-compose |
|------------------|---------------------------|-----------------|------------------------|
| Java             | Java                      | **21 LTS**      | —                      |
| Framework        | Spring Boot               | **3.3.x**       | —                      |
| Base de datos    | PostgreSQL + PostGIS       | **16**          | **18-3.5** (PAE-001)   |
| Mensajería       | Kafka KRaft Confluent      | 3.7             | **8.0.0** (PAE-001)    |
| Cache            | Redis                     | 7.2             | **8** (PAE-001)        |

> **PAE-001 parcial (2026-05-30)**: `docker-compose.yml` de `areas` ya usa PG18/Redis8/Kafka KRaft.
> `pom.xml` permanece en Java 21/Boot 3.3 hasta hito v2.0 (Q4 2026/Q1 2027).
> Ver `areas/docs/adr/ADR-001-stack-versions.md §Enmienda PAE-001`.

---

## 3. Estado real de cada módulo

### 3.1 `banks` — 71/100 completado

**Completado (71 tareas):**
- ✅ Fase 1: pom.xml (Boot 4.0.6 / Java 25), MagentaBanksApplication, application.yml + application-dev.yml, alineación IDEABASE completa
- ✅ Fase 2 parcial: V1__init.sql (esquema completo con RLS, outbox, processed_event)
- ✅ Fase 3: Dominio completo — Bank, LoanProduct, LoanSimulation, Preapproval, Appraisal; 8 enums; BorrowerProfile, RateInfo, EligibilityRules, SimulationResult; RuleEngine, PreapprovalStateMachine; todos los eventos
- ✅ Fase 4: 10 puertos (repos + clients + publisher)
- ✅ Fase 5 parcial: 9/11 use cases (faltan SimulateNinetyFiveFiveUseCase, MarkPropertyFinanciableUseCase)
- ✅ Fase 6: FrenchAmortizationService, TaeCalculatorService (Newton-Raphson, Circular BdE 5/2012), OwnFundsCalculatorService, ApprovabilityScorerService
- ✅ Fase 7 parcial: Bank JPA, LoanProduct JPA, LoanSimulation JPA, Outbox JPA (4/8)
- ✅ Fase 8 parcial: BankController, LoanSimulationController, PreapprovalController, AppraisalController, FeasibilityController + DTOs + GlobalExceptionHandler (7/10)
- ✅ Fase 9 parcial: OutboxDomainEventPublisher, OutboxPoller (2/8)
- ✅ Fase 10 parcial: CustomerRestClient, PropertyRestClient, ZoneRestClient (3/4)
- ✅ Fase 11 parcial: SecurityConfig, CacheConfig, KafkaConfig (3/6)
- ✅ Fase 13 parcial: 6 tests unitarios dominio + HexagonalArchitectureTest (7/10)
- ✅ Fase 14 parcial: Dockerfile (Java 25 Distroless), docker-compose.yml (KRaft)

**Pendiente (29 tareas) — prioridad de desbloqueo:**

| Bloque | Tarea | Archivo esperado |
|--------|-------|-----------------|
| **A — Compilación (bloquean build)** | `PreapprovalJpaEntity` + `PreapprovalEventJpaEntity` + repos + adapter | `adapter/out/persistence/preapproval/` |
| A | `AppraisalJpaEntity` + repo + adapter | `adapter/out/persistence/appraisal/` |
| A | `EuriborRateJpaEntity` + repo + adapter | `adapter/out/persistence/euribor/` |
| A | `ObservabilityConfig` (OTel beans) | `infrastructure/config/ObservabilityConfig.java` |
| A | `RateLimitConfig` (Bucket4j /simulations 30 rpm/IP) | `infrastructure/config/RateLimitConfig.java` |
| A | `ResilienceConfig` (CB customers/products/areas) | `infrastructure/config/ResilienceConfig.java` |
| A | `LoanProductController` | `adapter/in/web/LoanProductController.java` |
| A | `EuriborController` (`GET /api/v1/indices/euribor`) | `adapter/in/web/EuriborController.java` |
| A | `SimulateNinetyFiveFiveUseCase` (UC-B4, caso de uso central) | `application/usecase/SimulateNinetyFiveFiveUseCase.java` |
| A | MapStruct mappers dominio↔JPA y dominio↔DTO | `persistence/*/mapper/`, `web/mapper/` |
| **B — Funcionalidad** | `EuriborFetcherJob` (EMMI + fallback BdE XML, cron con jitter) | `adapter/out/client/EuriborFetcherJob.java` |
| B | `CustomerProfileConsumer` (`magenta.customers.profile.v1`) | `adapter/in/kafka/CustomerProfileConsumer.java` |
| B | `CustomerKycConsumer` (`magenta.customers.kyc.v1`) | `adapter/in/kafka/CustomerKycConsumer.java` |
| B | `PropertyEventConsumer` (`magenta.products.property.v1`) | `adapter/in/kafka/PropertyEventConsumer.java` |
| B | `TransactionEventConsumer` (`magenta.products.transaction.v1`) | `adapter/in/kafka/TransactionEventConsumer.java` |
| B | `ZoneEventConsumer` (`magenta.areas.zone.v1`) | `adapter/in/kafka/ZoneEventConsumer.java` |
| B | `PriceIndexConsumer` (`magenta.areas.price-index.v1`) | `adapter/in/kafka/PriceIndexConsumer.java` |
| B | `MarkPropertyFinanciableUseCase` (UC-B6) | `application/usecase/MarkPropertyFinanciableUseCase.java` |
| **C — Datos/Entorno** | `V2__seed_banks.sql` (13 bancos: Santander, BBVA, CaixaBank, ING, Bankinter, EVO, Abanca, Cajamar, Kutxabank, MyInvestor, Open Bank, ImaginBank, GoHipoteca) | `db/migration/V2__seed_banks.sql` |
| C | `V3__seed_products.sql` (productos preferentes 90+5+5 por banco) | `db/migration/V3__seed_products.sql` |
| C | `keycloak/magenta-realm.json` (roles: CUSTOMER/AGENT/BANK_OFFICER/ADMIN/SYSTEM) | `keycloak/magenta-realm.json` |
| **D — Calidad/Ops** | `BankControllerIT` (Testcontainers PG) | `test/.../BankControllerIT.java` |
| D | `LoanSimulationIT` (Testcontainers + WireMock) | `test/.../LoanSimulationIT.java` |
| D | `PreapprovalIT` | `test/.../PreapprovalIT.java` |
| D | Helm chart `charts/banks/` (deployment, service, hpa, pdb, networkpolicy, serviceaccount) | `charts/banks/` |
| D | Dashboard Grafana `banks-preapprovals.json` | `charts/banks/dashboards/` |
| D | Tabla `processed_message` en V1 o V4 para consumers Kafka | SQL migration |
| D | `Idempotency-Key` en POST /simulations, /preapprovals, /appraisals | middleware/filter |
| D | Gate CI OWASP ZAP + Dependency-Check + Sonar | pipeline config |

### 3.2 `areas` — ~96/100 completado (4 stubs de ingesta pendientes)

**Completado:**
- ✅ Fases 1–8 completas: dominio, eventos, puertos, 12 use cases, JPA completo, controllers, Kafka producers/consumers, idempotencia
- ✅ TenantFilterInterceptor (creado 2026-05-30): extrae `tenant_id` del JWT, ejecuta `SET app.tenant_id` para RLS
- ✅ ZoneMapper MapStruct (creado 2026-05-30)
- ✅ ZoneEventProducer + PriceIndexEventProducer (creados 2026-05-30)
- ✅ Fase 9: 4 adaptadores de ingesta EXISTEN como stubs estructurados (IneIngestionAdapter, CatastroIngestionAdapter, CnmcFiberIngestionAdapter, OsmPoiIngestionAdapter) — tienen `@Scheduled` + `RestClient` declarado, sin lógica real
- ✅ Fase 10–12: configs, Dockerfile, Helm, Grafana, Prometheus
- ✅ Fase 13: alineación completa con stack-tech_spec.md
- ✅ docker-compose.yml PG18/Redis8/Kafka KRaft (creado 2026-05-30)
- ✅ ADR-001 + enmienda PAE-001

**Pendiente real (🔨 stubs o ⬜ no empezado):**

| Estado | Tarea | Archivo |
|--------|-------|---------|
| 🔨 | IneIngestionAdapter — lógica real contra API INE | `adapter/out/ingestion/ine/IneIngestionAdapter.java` |
| 🔨 | CatastroIngestionAdapter — lógica real contra INSPIRE/Catastro | `adapter/out/ingestion/catastro/CatastroIngestionAdapter.java` |
| 🔨 | CnmcFiberIngestionAdapter — API CNMC cobertura fibra | `adapter/out/ingestion/cnmc/CnmcFiberIngestionAdapter.java` |
| 🔨 | OsmPoiIngestionAdapter — Overpass API hospitales/estaciones | `adapter/out/ingestion/osm/OsmPoiIngestionAdapter.java` |
| ⬜ | Paginación cursor-based en `GET /zones?search=` y feeds price-index | ZoneController + SearchZonesUseCase |
| ⬜ | Tests BOLA: `PUT /zones/{id}` con tenant ajeno debe devolver 403 | test IT |
| ⬜ | Gate CI OWASP ZAP | pipeline |
| ⬜ | Verificar liveness probe NO consulta PG/Kafka/Redis | Helm values.yaml |

### 3.3 `customers`, `products`, `servicios`

> Estado desconocido para este agente. No se han inspeccionado en esta sesión.
> Asumir que siguen el mismo stack y convenciones que `areas` (Java 21 / Boot 3.3).
> **Acción requerida**: auditar `PROGRESS.md` o equivalente de cada módulo antes de continuar.

---

## 4. Plan de cierre para producción v1.0

### 4.1 Requisito mínimo para demo / UAT

Para que la plataforma sea funcionalmente demostrable se deben completar, **en este orden**:

```
PASO 1: banks — compilación limpia (Bloque A)
  1a. PreapprovalJpaEntity + AppraisalJpaEntity + EuriborRateJpaEntity + adapters
  1b. ObservabilityConfig + RateLimitConfig + ResilienceConfig
  1c. LoanProductController + EuriborController
  1d. SimulateNinetyFiveFiveUseCase (UC-B4 — core de negocio)
  1e. MapStruct mappers (JPA + DTO)
  → Verificar: mvn clean verify -DskipTests (banks)

PASO 2: banks — datos semilla
  2a. V2__seed_banks.sql (13 bancos)
  2b. V3__seed_products.sql (productos 90+5+5)

PASO 3: keycloak/magenta-realm.json
  Roles: CUSTOMER / AGENT / BANK_OFFICER / ADMIN / SYSTEM
  Clients: areas-service, banks-service, customers-service, products-service, servicios-service

PASO 4: areas — ingesta stubs → funcionales
  4a. IneIngestionAdapter (datos demográficos)
  4b. CatastroIngestionAdapter (geometrías municipios/barrios)
  4c. OsmPoiIngestionAdapter (hospitales, estaciones)

PASO 5: validación E2E local
  docker compose up (en cada módulo)
  Smoke test: POST /simulations, GET /zones, GET /banks
```

### 4.2 Requisito para producción (release gate)

```
☐ mvn clean verify (todos los módulos, incluye ArchUnit + IT con Testcontainers)
☐ Cobertura ≥ 80% líneas, ≥ 70% ramas en domain/ + application/
☐ 0 vulnerabilidades CRITICAL/HIGH en Dependency-Check
☐ 0 hallazgos HIGH en OWASP ZAP baseline
☐ Helm chart validado: helm lint + helm template | kubeval
☐ Imágenes firmadas con Cosign + SBOM SPDX generado
☐ Keycloak realm configurado (magenta-realm.json importado)
☐ Flyway migraciones sin errores en staging
☐ Smoke tests E2E (Karate o Postman Newman)
☐ SLO validados: p95 lectura <150ms, p95 escritura <400ms (k6/Gatling)
☐ Secretos en Vault (no en ConfigMaps/Secrets base64)
```

---

## 5. Guía de despliegue en producción

### 5.1 Pre-requisitos de infraestructura

```
Cluster Kubernetes ≥ 1.29
  - Namespace: magenta-prod
  - StorageClass para PVC PostgreSQL
  - Ingress controller (NGINX o Traefik)
  - cert-manager para TLS

PostgreSQL 18 (instancia compartida o cluster dedicado)
  - Bases de datos separadas: areas_db, banks_db, customers_db, products_db, services_db
  - Usuario por BD con GRANT mínimo
  - PostGIS 3.5 habilitado en areas_db: CREATE EXTENSION postgis; CREATE EXTENSION pg_trgm;

Kafka cluster KRaft (Confluent Platform 8.0.0 o Apache Kafka 4.x)
  - Topics pre-creados (o KAFKA_AUTO_CREATE_TOPICS_ENABLE=true en dev)
  - Schema Registry accesible
  - Topics obligatorios:
    magenta.areas.zone.v1
    magenta.areas.price-index.v1
    magenta.banks.product.v1
    magenta.banks.preapproval.v1
    magenta.customers.profile.v1
    magenta.customers.kyc.v1
    magenta.products.property.v1
    magenta.products.transaction.v1
    magenta.servicios.workflow.v1

Redis 8 (cluster o sentinel, no standalone en prod)

Keycloak 24 con realm magenta importado desde keycloak/magenta-realm.json

HashiCorp Vault (Spring Cloud Vault 4.2.0)
  - Monta secretos en /secret/magenta/<modulo>/
  - Campos: DB_PASSWORD, KAFKA_API_KEY, REDIS_PASSWORD, JWT_PRIVATE_KEY

OpenTelemetry Collector → Tempo + Prometheus + Loki
```

### 5.2 Orden de despliegue

```
1. PostgreSQL schemas (Flyway automático en startup de cada servicio)
2. Keycloak realm import
3. Kafka topics + Schema Registry schemas (Avro)
4. areas (sin dependencias de otros módulos)
5. customers (sin dependencias de otros módulos)
6. products (depende de areas vía REST/Kafka)
7. banks (depende de customers, products, areas)
8. servicios (depende de todos)
9. API Gateway
10. Angular SPA (configura OIDC_ISSUER_URL apuntando a Keycloak)
```

### 5.3 Variables de entorno obligatorias por módulo

**Comunes a todos los módulos:**

```
DB_USER=<usuario>
DB_PASSWORD=<vault>
KAFKA_BOOTSTRAP_SERVERS=kafka:9092
SCHEMA_REGISTRY_URL=http://schema-registry:8081
REDIS_HOST=redis
KEYCLOAK_JWKS_URI=https://keycloak/realms/magenta/protocol/openid-connect/certs
OTLP_ENDPOINT=http://otel-collector:4318/v1/traces
TRACE_SAMPLE_RATE=0.1
SPRING_PROFILES_ACTIVE=prod
```

**`banks` adicionales:**

```
VAULT_URI=https://vault:8200
VAULT_TOKEN=<vault>
EURIBOR_EMMI_API_KEY=<vault>
EURIBOR_BDE_FALLBACK_URL=https://www.bde.es/webbde/es/estadis/infoest/series/...
```

**`areas` adicionales:**

```
INE_API_BASE_URL=https://servicios.ine.es/wstempus/jsStatINE
CATASTRO_INSPIRE_URL=https://ovc.catastro.meh.es/OVCServicio/...
CNMC_API_BASE_URL=https://geoportal.cnmc.gob.es/...
OSM_OVERPASS_URL=https://overpass-api.de/api/interpreter
```

### 5.4 Helm despliegue

```bash
# Cada módulo tiene su Helm chart en charts/<modulo>/
# areas ya tiene Helm chart completo

helm upgrade --install areas ./charts/areas \
  --namespace magenta-prod \
  --set image.tag=<git-sha> \
  --set ingress.host=areas.magenta.es \
  --values charts/areas/values-prod.yaml

# banks (chart pendiente de crear — ver §3.1 Bloque D)
```

### 5.5 Flyway — consideraciones críticas

- Migraciones numeradas `V<YYYYMMDDHHmm>__<descripcion>.sql`.
- **NO modificar migraciones ya ejecutadas** en staging/prod.
- Si hay seed data en V2, V3: solo ejecutar una vez; proteger con `ON CONFLICT DO NOTHING`.
- `areas`: migración `V202601011200__init.sql` incluye tablas con RLS activo. PostgreSQL necesita que el rol de la app NO sea superuser para que RLS funcione.
- `banks`: la tabla `preapprovals` tiene RLS. Verificar que el rol `banks` tiene `GRANT SELECT, INSERT, UPDATE` y NO `BYPASSRLS`.

---

## 6. Checklist de auditoría

### 6.1 OWASP Top 10 2025

| Riesgo | Implementado | Verificación |
|--------|-------------|--------------|
| A01 Broken Access Control | RBAC con `@PreAuthorize`, RLS en PG, tenant isolation | Test: intento de acceso con tenant ajeno debe dar 403 |
| A02 Cryptographic Failures | TLS 1.3, AES-256-GCM via Vault (campos PII), BCrypt/Argon2 | Audit: ¿PII en logs? ¿HTTPS end-to-end? |
| A03 Injection | JPA parametrizado, Bean Validation, sin SQL nativo dinámico | ArchUnit: no `@NativeQuery` con concatenación |
| A04 Insecure Design | Hexagonal + DDD, threat modeling STRIDE, ADRs | Presentar ADR-001 (areas) |
| A05 Security Misconfiguration | Distroless, `securityContext` restricted en Helm, sin defaults inseguros | `helm template \| grep securityContext` |
| A06 Vulnerable Components | Dependabot + OWASP Dependency-Check CI | `mvn org.owasp:dependency-check-maven:check` |
| A07 Auth Failures | Keycloak MFA TOTP, rate-limit en `/login` (Bucket4j), JWT exp corto | Verificar `exp` < 15 min en tokens |
| A08 Integrity Failures | Cosign firma imagen, SBOM SPDX, Admission Controller Sigstore | Pipeline CI |
| A09 Logging Failures | JSON estructurado SLF4J, `tenantId`+`traceId` en cada log, sin PII | Grep: ¿aparece DNI/IBAN en logs? |
| A10 SSRF | Egress NetworkPolicy allowlist, validación URLs externas (Catastro, EMMI) | `kubectl get networkpolicy -n magenta-prod` |

### 6.2 OWASP API Security Top 10 2023

| Riesgo | Qué revisar en Magenta |
|--------|----------------------|
| API1 BOLA | `GET /preapprovals/{id}` → ¿valida que customerId en JWT == preapproval.customerId? Idem zonas, simulaciones |
| API2 Broken Auth | JWT validado en cada microservicio (no solo en gateway) — ver `SecurityConfig` de cada módulo |
| API3 Broken Object Property | DTOs no exponen campos internos (`version`, `tenantId` raw, `deletedAt`) |
| API4 Unrestricted Resource Consumption | Bucket4j activo en `/simulations` (30 rpm/IP, 300 rpm/user). Falta en banks (pendiente RateLimitConfig) |
| API5 Broken Function Level Auth | `POST /zones` (admin), `POST /preapprovals` (customer) — roles verificados con `@PreAuthorize` |
| API6 Unrestricted Access to Sensitive Business Flows | `POST /simulations` requiere Idempotency-Key (pendiente en banks) |
| API7 SSRF | (ver OWASP A10) |
| API8 Security Misconfiguration | (ver OWASP A05) |
| API9 Improper Inventory Management | OpenAPI 3.1 en `/v3/api-docs` por módulo, agregado en Gateway |
| API10 Unsafe Consumption of APIs | EuriborFetcherJob valida schema de respuesta EMMI antes de persistir |

### 6.3 RGPD / LOPD-GDD (España)

| Obligación | Estado en Magenta |
|------------|------------------|
| Minimización de datos | DTOs solo exponen campos necesarios; `@JsonIgnore` en campos internos |
| Consentimiento | Gestionado en `customers` (fuera de alcance de este módulo) |
| Derecho al olvido | **Pendiente**: tombstone Kafka + crypto-shredding con Vault (campo `deleted_at` existe en tablas, lógica de shredding no implementada) |
| Portabilidad | **Pendiente**: endpoint `GET /customers/{id}/export` (módulo customers) |
| PII en logs | Regla en `logback-spring.xml` para redactar DNI, IBAN, ingresos — **verificar** |
| Retención de datos | Logs: 90 días; datos de usuario: política en `customers`; ver contrato DPA |
| Transferencias fuera UE | APIs externas (INE, Catastro, CNMC, OSM): servidores en España/UE — **verificar** |
| DPO / Registro de actividades | Documento externo (no en código) |

### 6.4 Regulatorio Banco de España

| Requerimiento | Implementación |
|---------------|---------------|
| TAE Circular BdE 5/2012 (RD 309/2019) | `TaeCalculatorService` — Newton-Raphson, verificar con golden files |
| LTV máximo ≤ 80% (≤ 90% primera vivienda) | `EligibilityRules.ltvMaxPct` en LoanProduct + `ApprovabilityScorerService` |
| Esfuerzo ≤ 35% ingresos netos | `ApprovabilityScorerService.checkDebtRatio()` |
| Endeudamiento total ≤ 40% | `ApprovabilityScorerService.checkTotalDebt()` |
| Transparencia hipotecaria Ley 5/2019 | FEIN (Ficha Europea Info Normalizada) — **pendiente** generar PDF |
| Tasación ECO/805 2003 | `Appraisal` aggregate — registro de tasador, fecha, valor, metodología |

---

## 7. Índice de documentación

### 7.1 Documentos existentes en el repositorio

| Documento | Ubicación | Propósito |
|-----------|-----------|-----------|
| Arquitectura canónica | `banks/ARCHITECTURE.md` (copia en cada módulo raíz) | Contratos cross-cutting para todos los módulos |
| Spec módulo banks | `banks/MODULE-SPEC.md` | Casos de uso, modelo dominio, endpoints, topics |
| Pendientes banks | `banks/PENDIENTES.md` | Tracker de tareas con estado real |
| Progress areas | `areas/PROGRESS.md` | Tracker con estado real (auditado 2026-05-30) |
| ADR-001 areas | `areas/docs/adr/ADR-001-stack-versions.md` | Decisión versiones v1.0 + enmienda PAE-001 |
| docker-compose banks | `banks/docker-compose.yml` | Stack local PG18/Redis8/Kafka KRaft |
| docker-compose areas | `areas/docker-compose.yml` | Stack local PG18+PostGIS/Redis8/Kafka KRaft |
| Flyway banks | `banks/src/main/resources/db/migration/V1__init.sql` | Esquema completo banks con RLS |
| Flyway areas | `areas/src/main/resources/db/migration/V202601011200__init.sql` | Esquema completo areas con PostGIS + RLS |
| Grafana areas | `areas/ops/grafana/areas-overview.json` | Dashboard métricas negocio |
| Alertas areas | `areas/ops/prometheus/areas-alerts.yaml` | Alertas SLO |
| Helm areas | `areas/charts/areas/` | Chart completo con securityContext + NetworkPolicy |

### 7.2 Documentos pendientes de crear

| Documento | Ubicación sugerida | Responsable |
|-----------|--------------------|-------------|
| ADR-001 banks | `banks/docs/adr/ADR-001-stack-versions.md` | Arquitectura |
| ADR-002 banks | `banks/docs/adr/ADR-002-tae-algorithm.md` | Equipo banks (Newton-Raphson vs interpolación) |
| FEIN template | `banks/docs/legal/fein-template.md` | Legal + Equipo banks |
| Keycloak realm | `banks/keycloak/magenta-realm.json` | DevOps |
| Runbook producción | `ops/runbooks/` | SRE |
| DPA / RGPD registro | externo (Confluence/Notion) | DPO |
| Threat model STRIDE | `docs/security/threat-model.md` | Security |

### 7.3 APIs públicas (OpenAPI)

Una vez los servicios estén desplegados:

```
areas:    http://localhost:8081/v3/api-docs  → http://localhost:8081/swagger-ui.html
banks:    http://localhost:8082/v3/api-docs  → http://localhost:8082/swagger-ui.html
customers: http://localhost:8083/v3/api-docs
products:  http://localhost:8084/v3/api-docs
servicios: http://localhost:8085/v3/api-docs
Gateway agregado: http://gateway/docs
```

---

## 8. Reglas operativas para el agente continuador

### 8.1 Reglas absolutas (no negociables)

```
R1. El paquete domain.* NO importa Spring, JPA, Kafka, Redis, ni HTTP.
    → Enforced por HexagonalArchitectureTest (ArchUnit) en cada módulo.

R2. Toda escritura emite un DomainEvent publicado VIA OUTBOX TRANSACCIONAL.
    → Nunca dual-write directo a Kafka.
    → Flujo: use case → outbox.publish(event) → OutboxPoller → KafkaTemplate.

R3. Todos los consumers Kafka son IDEMPOTENTES.
    → Verificar event_id contra tabla processed_event antes de procesar.
    → banks: tabla aún pendiente (ver §3.1 Bloque D).

R4. Errores HTTP = org.springframework.http.ProblemDetail (RFC 9457).
    → No crear clases de error propias.
    → GlobalExceptionHandler en cada módulo.

R5. Multi-tenant: todas las tablas de negocio tienen tenant_id UUID NOT NULL.
    → Hibernate filter + PostgreSQL RLS = doble barrera.
    → TenantFilterInterceptor establece app.tenant_id en sesión PG.

R6. Seguridad: ningún endpoint sin autenticación salvo /actuator/health, /v3/api-docs.
    → SecurityConfig en cada módulo.

R7. No re-abrir tareas ✅ de fases completadas salvo bug confirmado.
    → Crear ADR si el cambio es arquitectónico.
    → Actualizar PENDIENTES.md / PROGRESS.md en cada sesión.

R8. Los mapeos dominio↔JPA y dominio↔DTO se hacen con MapStruct.
    → No toObject() / fromObject() manuales en adapters.

R9. Paginación cursor-based en colecciones grandes (> 1000 filas esperadas).
    → Offset pagination solo para colecciones pequeñas y catalogos.

R10. Liveness probe NO consulta PG / Kafka / Redis.
     → Solo readinessProbe verifica dependencias.
     → /actuator/health/liveness = proceso vivo únicamente.
```

### 8.2 Convenciones de código

```
- UUID v7 como PK (tiempo-ordenable)
- snake_case en SQL, camelCase en Java
- Tablas en plural (banks, loan_products, preapprovals)
- FKs: fk_<tabla>_<referencia> (ej: fk_preapprovals_bank)
- Índices: idx_<tabla>_<columna>
- Auditoría: created_at, updated_at (TIMESTAMPTZ), version BIGINT (optimistic lock)
- Soft delete: deleted_at TIMESTAMPTZ NULL
- Flyway: V<YYYYMMDDHHmm>__<descripcion_snake>.sql
- CloudEvents: specversion=1.0, source=/magenta/<modulo>, type=com.magenta.<modulo>.<EventName>
- Kafka key: <tenantId>:<aggregateId> (particionado por tenant)
```

### 8.3 Cómo compilar y testear cada módulo

```bash
# Compilar sin tests (rápido, para verificar errores de compilación)
cd c:\t\Magenta\banks
mvn clean compile -DskipTests

# Tests unitarios + ArchUnit (no requieren Docker)
mvn test

# Tests de integración (requieren Docker en el host)
mvn verify -Pit

# Todo junto
mvn clean verify

# areas (mismo patrón)
cd c:\t\Magenta\areas
mvn clean verify -DskipTests
mvn test
```

### 8.4 Cómo levantar el entorno local

```bash
# areas
cd c:\t\Magenta\areas
docker compose up -d
mvn spring-boot:run -Dspring-boot.run.profiles=local
# Swagger: http://localhost:8081/swagger-ui.html

# banks
cd c:\t\Magenta\banks
docker compose up -d
mvn spring-boot:run -Dspring-boot.run.profiles=dev
# Swagger: http://localhost:8082/swagger-ui.html
```

### 8.5 Checklist antes de cada sesión

```
[ ] Leer PENDIENTES.md (banks) o PROGRESS.md (areas) para conocer estado real
[ ] Verificar que los ficheros marcados ✅ realmente existen en disco antes de confiar
[ ] Ejecutar mvn clean compile -DskipTests para confirmar estado actual de compilación
[ ] No crear archivos que ya existen (buscar antes con file_search o grep_search)
[ ] Actualizar PENDIENTES.md / PROGRESS.md al cerrar la sesión
```

### 8.6 Decisiones arquitectónicas ya tomadas (no reabrir)

| Decisión | Referencia |
|----------|-----------|
| `areas` pom.xml permanece en Java 21 / Boot 3.3 hasta v2.0 | ADR-001 areas |
| docker-compose de `areas` usa PG18/Redis8/Kafka KRaft | ADR-001 §Enmienda PAE-001 |
| TAE calculado con Newton-Raphson, Circular BdE 5/2012 | TaeCalculatorService.java |
| Outbox transaccional (NO Debezium en v1.0) | OutboxPoller.java (polling @Scheduled) |
| MapStruct para todos los mapeos (no ModelMapper) | pom.xml annotationProcessorPaths |
| Distroless para imágenes de producción | Dockerfile cada módulo |
| KRaft sin Zookeeper en todos los módulos | docker-compose.yml cada módulo |
| RFC 9457 (ProblemDetail) para errores HTTP | GlobalExceptionHandler cada módulo |

---

## 9. Contacto y escalado

| Área | Responsable | Cuándo escalar |
|------|-------------|----------------|
| Dominio bancario (BdE, Euríbor, LTV) | Equipo Product (banca) | Duda sobre reglas de negocio |
| Arquitectura (ADR nuevo) | Arquitectura Platform | Cambio que afecta >1 módulo |
| RGPD / DPO | DPO designado | Nuevo flujo con PII |
| Seguridad | Security Champion | Vulnerabilidad o diseño de auth |
| Infraestructura | SRE/DevOps | Cambio en Kubernetes, Vault, Kafka |

---

*Documento generado automáticamente el 2026-05-30. Mantener actualizado en cada sesión de desarrollo.*
