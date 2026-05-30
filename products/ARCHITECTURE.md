# Magenta Platform — Arquitectura de Referencia (v1.0)

> Documento canónico, **autocontenido**, compartido por todos los módulos del sistema.
> Cualquier módulo (`areas`, `banks`, `customers`, `products`, `servicios`) DEBE cumplir los
> contratos definidos aquí. Cualquier divergencia se documenta como ADR (Architecture Decision Record).

---

## 1. Visión de negocio

**Magenta** es una plataforma SaaS para el mercado inmobiliario español orientada a tres flujos:

1. **Adquisición** de vivienda (compra / hipoteca / alquiler con opción a compra / permuta).
2. **Alquiler** residencial y comercial con cobertura de impagos.
3. **Decisión asistida**: comparativas alquiler-vs-compra, momento óptimo de compra,
   simulación 90 + 5 + 5 (esquema promotor + banco tipo Santander/CaixaBank),
   evaluación de capacidad hipotecaria contra reglas del Banco de España
   (≤ 35 % esfuerzo, ≤ 40 % endeudamiento total).

Los inmuebles se publican en **áreas** geográficas (CCAA → provincia → municipio → barrio → urbanización),
financiados por **bancos** (incluyendo líneas preferentes tipo *GoHipoteca*, *Hipoteca Joven Santander*,
*Open Bank*, *MyInvestor*), comprados/alquilados por **clientes** (persona física, persona jurídica,
unidad familiar), y soportados por un catálogo de **servicios** (mediación, tasación,
seguro impago, búsqueda de perfil, peritaje, gestoría).

---

## 2. Mapa de módulos (Bounded Contexts)

| Módulo       | Bounded Context                  | Carpeta       | Puerto local (dev) |
|--------------|----------------------------------|---------------|--------------------|
| Areas        | Geografía y mercado por zona     | `areas/`      | 8081               |
| Banks        | Entidades financieras y crédito  | `banks/`      | 8082               |
| Customers    | Identidad, perfil y solvencia    | `customers/`  | 8083               |
| Products     | Catálogo de inmuebles            | `products/`   | 8084               |
| Servicios    | Servicios añadidos y workflows   | `servicios/`  | 8085               |

Cada módulo es un **microservicio Spring Boot autónomo** con su **esquema PostgreSQL propio**
(`areas_db`, `banks_db`, `customers_db`, `products_db`, `services_db`) sobre una misma instancia o cluster.
**Prohibido** acceder a tablas de otro módulo: la comunicación se hace por **REST sincronía**
(consultas de baja latencia) o **eventos Kafka** (cambios de estado y proyecciones).

```
                       ┌──────────────────────────┐
                       │   Angular SPA (BFF Web)  │
                       └────────────┬─────────────┘
                                    │ HTTPS + JWT
                       ┌────────────▼─────────────┐
                       │   API Gateway (Spring    │
                       │   Cloud Gateway + WAF)   │
                       └─┬───────┬───────┬──────┬─┘
                         │       │       │      │
                ┌────────▼┐ ┌────▼───┐ ┌─▼────┐ ┌▼────────┐
                │ areas   │ │ banks  │ │ cust │ │products │
                └─────────┘ └────────┘ └──────┘ └─────────┘
                                ▲ Kafka (events)  ▲
                                └──────servicios──┘
```

---

## 3. Stack tecnológico (versiones fijadas)

| Capa              | Tecnología                                      | Versión mínima |
|-------------------|--------------------------------------------------|----------------|
| Lenguaje BE       | Java                                             | 21 LTS         |
| Framework BE      | Spring Boot                                      | 3.3.x          |
| Build BE          | Maven                                            | 3.9.x          |
| Persistencia      | Spring Data JPA + Hibernate                      | 6.5.x          |
| Migraciones       | Flyway                                           | 10.x           |
| Base de datos     | PostgreSQL                                       | 16             |
| Búsqueda full-text| PostgreSQL `tsvector` (+ OpenSearch en `products`) | OS 2.13      |
| Mensajería        | Apache Kafka (Confluent / Redpanda)              | 3.7            |
| Cache             | Redis                                            | 7.2            |
| API Gateway       | Spring Cloud Gateway                             | 4.1            |
| Service Discovery | Spring Cloud + Kubernetes DNS                    | —              |
| Auth              | Keycloak (OIDC / OAuth2) + JWT RS256             | 24.x           |
| Frontend          | Angular                                          | 17 LTS         |
| UI Kit            | Angular Material + TailwindCSS                   | —              |
| State (FE)        | NgRx 17                                          | —              |
| Build FE          | Nx + Angular CLI                                 | 17.x           |
| Contenedores      | Docker (multi-stage, Distroless)                 | —              |
| Orquestación      | Kubernetes                                       | 1.29+          |
| IaC               | Terraform + Helm                                 | TF 1.7         |
| Observabilidad    | OpenTelemetry + Prometheus + Grafana + Loki + Tempo | —          |
| Trazas            | OpenTelemetry → Tempo (W3C Trace-Context)        | OTel 1.32      |
| CI/CD             | GitHub Actions / GitLab CI + ArgoCD              | —              |

---

## 4. Arquitectura interna de cada microservicio (Hexagonal + DDD ligero)

```
src/main/java/com/magenta/<modulo>/
 ├─ application/         # Casos de uso (servicios de aplicación, orquestación)
 ├─ domain/
 │   ├─ model/           # Entidades, VO, Aggregate Roots
 │   ├─ event/           # Eventos de dominio
 │   └─ port/            # Puertos (interfaces) inbound/outbound
 ├─ infrastructure/
 │   ├─ adapter/
 │   │   ├─ in/web/      # Controllers REST, DTOs, mappers
 │   │   ├─ in/kafka/    # Consumers
 │   │   ├─ out/persistence/   # Repos JPA, entidades persistentes
 │   │   ├─ out/kafka/   # Producers
 │   │   └─ out/client/  # Feign/RestClient hacia otros módulos
 │   ├─ config/          # Spring config, security, observability
 │   └─ migration/       # Flyway (V1__init.sql ...)
 └─ MagentaXApplication.java
```

Reglas duras:

- El paquete `domain` **no** importa nada de Spring ni JPA.
- Mapeo entre DTO ↔ Dominio con **MapStruct**.
- Cada caso de uso es una clase `@Service` con un único método público `execute(...)`.
- Toda escritura emite un **DomainEvent** publicado por Outbox Pattern (tabla `outbox_event`).

---

## 5. Contratos cross-cutting (compartidos por todos los módulos)

### 5.1 Autenticación / Autorización

- OIDC contra **Keycloak realm `magenta`**.
- Cada microservicio valida JWT RS256 con JWKS de Keycloak (`/.well-known/jwks.json`).
- Claims obligatorios en el token: `sub`, `email`, `preferred_username`, `realm_access.roles`,
  `customer_id` (cuando aplique), `tenant_id`.
- Roles canónicos:
  `ROLE_CUSTOMER`, `ROLE_AGENT`, `ROLE_BANK_OFFICER`, `ROLE_ADMIN`, `ROLE_SYSTEM`.
- Autorización a nivel método con `@PreAuthorize` y a nivel fila con políticas por `tenant_id`.

### 5.2 Multi-tenant

- Todas las tablas con datos de negocio incluyen `tenant_id UUID NOT NULL`.
- Interceptor JPA inyecta filtro Hibernate `@Filter("tenant")`.
- En PostgreSQL se aplica **RLS** (Row Level Security) como segunda barrera.

### 5.3 Formato de error (RFC 7807 Problem Details)

```json
{
  "type": "https://magenta.es/errors/validation",
  "title": "Validation failed",
  "status": 400,
  "detail": "field 'price' must be > 0",
  "instance": "/api/v1/products/123",
  "traceId": "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01",
  "errors": [{ "field": "price", "code": "min", "message": "must be > 0" }]
}
```

### 5.4 Paginación, filtrado y ordenación

Estándar **Spring Data** (`?page=0&size=20&sort=createdAt,desc`).
Respuesta envuelta:

```json
{
  "content": [...],
  "page": { "number": 0, "size": 20, "totalElements": 1342, "totalPages": 68 },
  "_links": { "self": "...", "next": "...", "prev": null }
}
```

### 5.5 Versionado de API

- Path-based: `/api/v1/...`. Cambios breaking → `/api/v2/...`.
- Compatibilidad ascendente mínima de **6 meses** entre versiones.
- OpenAPI 3.1 publicado en `/v3/api-docs` y agregado por el Gateway en `/docs`.

### 5.6 Eventos de dominio (Kafka)

- Topic naming: `magenta.<modulo>.<aggregate>.<v1>` (ej. `magenta.products.property.v1`).
- Serialización: **CloudEvents 1.0 + Avro** registrados en **Schema Registry**.
- Envelope obligatorio:

```json
{
  "specversion": "1.0",
  "id": "uuid",
  "source": "/magenta/products",
  "type": "com.magenta.products.PropertyPublished",
  "time": "2026-05-30T10:15:00Z",
  "subject": "property:123e4567-...",
  "datacontenttype": "application/avro",
  "traceparent": "00-...-...-01",
  "tenantid": "uuid",
  "data": { ... }
}
```

- Patrón **Outbox** + **Debezium** o publisher transactional para garantizar entrega.
- Consumidores **idempotentes** (clave: `event.id` persistida en `processed_event`).

### 5.7 Eventos canónicos publicados por cada módulo

| Topic                                       | Productor   | Consumidores principales            |
|---------------------------------------------|-------------|--------------------------------------|
| `magenta.areas.zone.v1`                     | areas       | products, servicios                  |
| `magenta.areas.price-index.v1`              | areas       | products, servicios, banks           |
| `magenta.banks.product.v1`                  | banks       | servicios, products (badge "financiable") |
| `magenta.banks.preapproval.v1`              | banks       | customers, servicios                 |
| `magenta.customers.profile.v1`              | customers   | products (matching), servicios, banks |
| `magenta.customers.kyc.v1`                  | customers   | banks                                |
| `magenta.products.property.v1`              | products    | servicios, customers (favoritos), banks |
| `magenta.products.transaction.v1`           | products    | banks, servicios, customers          |
| `magenta.servicios.workflow.v1`             | servicios   | customers, products                  |

### 5.8 Comunicación síncrona entre módulos

- Cliente HTTP: **Spring `RestClient`** (Java 21) o **Spring Cloud OpenFeign**.
- Resiliencia: **Resilience4j** (CircuitBreaker, Retry exponencial, Bulkhead, TimeLimiter 2 s default).
- Cache de respuestas idempotentes en Redis con TTL declarado por endpoint.
- Cabeceras obligatorias en cada llamada interna:
  `X-Tenant-Id`, `X-Request-Id`, `traceparent`, `Authorization: Bearer <service-account-JWT>`.

### 5.9 Datos compartidos (Shared Kernel mínimo)

Publicados como librería Maven `com.magenta:magenta-shared-kernel:1.x` que incluye:

- VOs: `Money`, `Iban`, `Nif`, `GeoPoint`, `PostalCode`, `EnergyRating`.
- Enums: `Currency`, `PropertyType`, `OperationType` (`SALE`, `RENT`, `EXCHANGE`),
  `TenantType` (`B2C`, `B2B`).
- DTOs `Page<T>`, `ProblemDetail`, `CloudEventEnvelope`.
- Anotaciones `@TenantAware`, `@Idempotent`.

**Nada de lógica de negocio** en el shared kernel: solo tipos.

---

## 6. Persistencia — convenciones PostgreSQL

- Una BD por módulo, **un esquema lógico por módulo** (`areas`, `banks`, ...).
- Identificadores: `UUID v7` (ordenable temporalmente) como PK.
- Auditoría obligatoria en cada tabla: `created_at`, `updated_at`, `created_by`, `updated_by`, `version BIGINT`.
- Soft delete con `deleted_at TIMESTAMPTZ NULL`.
- Convención de nombres: `snake_case`, tablas en plural, FKs `fk_<tabla>_<ref>`.
- Migraciones Flyway numeradas `V<YYYYMMDDHHmm>__<descripcion>.sql`.
- Índices GIN para `tsvector` (búsqueda) y para columnas `jsonb`.
- Particionado por rango (`created_at`) en tablas de alto volumen (`property_view`, `audit_log`).
- Réplica de lectura PostgreSQL para queries pesadas (`spring.datasource.readonly`).

---

## 7. Seguridad (OWASP ASVS L2 + OWASP Top 10 2021)

| Riesgo                       | Mitigación                                                       |
|------------------------------|-------------------------------------------------------------------|
| A01 Broken Access Control    | OAuth2 + RBAC + RLS + tests de autorización por endpoint         |
| A02 Cryptographic Failures   | TLS 1.3 en tránsito, AES-256-GCM en reposo, BCrypt/Argon2 para hash |
| A03 Injection                | JPA parametrizado + Bean Validation + bloqueo de SQL nativo dinámico |
| A04 Insecure Design          | Threat modeling STRIDE por feature, ADRs firmados                 |
| A05 Security Misconfiguration| CIS Benchmarks, imágenes Distroless, `securityContext` en K8s     |
| A06 Vulnerable Components    | Dependabot + Snyk + OWASP Dependency-Check en CI                 |
| A07 Auth Failures            | Keycloak + MFA TOTP + rate-limit en `/login` (Bucket4j)           |
| A08 Integrity Failures       | Imágenes firmadas con Cosign, SBOM SPDX, verificación en admission |
| A09 Logging Failures         | Logs JSON estructurados, retención 90 d, sin PII en logs          |
| A10 SSRF                     | Egress allowlist, validación de URLs externas                     |

**PII** (DNI, IBAN, ingresos, score) cifrada a nivel campo con **Spring Cloud Vault**.
Cumplimiento **RGPD**: derecho al olvido implementado como tombstone + crypto-shredding.

---

## 8. Performance & Escalabilidad

- Stateless: ninguna sesión en memoria; idempotencia con `Idempotency-Key`.
- HPA en Kubernetes por CPU (target 60 %) y por mensaje pendiente en Kafka.
- Connection pool: HikariCP, `maximumPoolSize = 10 * vCPU`.
- Caching:
  - **Caffeine** local (LRU, 5 min) para datos casi inmutables (catálogos).
  - **Redis** distribuido para perfiles y resultados de búsqueda.
- Búsqueda de inmuebles: índice **OpenSearch** con sharding por provincia.
- Latencia objetivo (SLO):
  - p95 API lectura < **150 ms**.
  - p95 API escritura < **400 ms**.
  - Disponibilidad ≥ **99.9 %** mensual.
- Throughput de referencia: 2 000 req/s por módulo en horario pico.

---

## 9. Observabilidad

- **Logs**: SLF4J + Logback con encoder JSON; campos obligatorios `timestamp`, `level`, `logger`,
  `traceId`, `spanId`, `tenantId`, `userId`, `service`, `version`.
- **Métricas**: Micrometer → Prometheus. KPIs mínimos:
  `http_server_requests_seconds`, `jvm_memory_used_bytes`, `kafka_consumer_lag`,
  `magenta_<modulo>_business_event_total`.
- **Trazas**: OpenTelemetry SDK auto + manual en casos de uso; export OTLP a Tempo.
- **Dashboards Grafana** versionados como JSON en `ops/grafana/`.
- **Alerting**: Alertmanager → PagerDuty. SLO error budget 0.1 % mensual.

---

## 10. Estrategia de testing

| Nivel         | Herramienta                          | Cobertura objetivo |
|---------------|--------------------------------------|--------------------|
| Unit          | JUnit 5 + AssertJ + Mockito          | ≥ 80 % líneas, ≥ 70 % ramas |
| Arquitectura  | ArchUnit                             | reglas hexagonales |
| Slice         | `@DataJpaTest`, `@WebMvcTest`        | —                  |
| Integración   | Testcontainers (PostgreSQL, Kafka, Redis) | flujos críticos |
| Contrato      | Spring Cloud Contract / Pact         | productor ↔ consumidor |
| E2E           | Playwright (Angular) + Karate (API)  | smoke + flujos críticos |
| Carga         | k6 / Gatling                         | SLO p95            |
| Seguridad     | OWASP ZAP baseline en CI             | sin highs          |

Frontend Angular: Jest + Testing Library + Cypress (component + e2e).

---

## 11. CI/CD y despliegue

- **Trunk-based** + Pull Requests con revisión obligatoria.
- Pipeline por módulo:
  1. `mvn verify` (unit + integration + ArchUnit)
  2. SAST (Sonar, Snyk) y SCA (Dependency-Check)
  3. Build imagen Docker multi-stage, **Distroless**, SBOM + firma Cosign
  4. Push a registry privado (Harbor)
  5. ArgoCD sincroniza Helm chart al cluster
- Estrategia de despliegue: **blue-green** (gateway) + **canary 5-25-100 %** en módulos backend.
- Feature flags con **Unleash**.
- Migraciones Flyway ejecutadas como `Job` previo al rollout.

---

## 12. Frontend Angular (transversal)

Repositorio monorepo Nx con apps y libs:

```
apps/
  web-portal/        # SPA pública (búsqueda inmuebles, comparador alquiler-vs-compra)
  web-backoffice/    # Gestión interna (agentes, admin, banca)
libs/
  shared/ui/         # Componentes base (Material wrappers)
  shared/data-access/# Servicios HTTP, NgRx feature states
  shared/util/       # Pipes, formatters (Money, Nif, PostalCode)
  domain/areas/
  domain/banks/
  domain/customers/
  domain/products/
  domain/servicios/
```

Reglas:

- Standalone components, Signals API.
- HTTP via interceptors: `AuthInterceptor`, `TenantInterceptor`, `TraceInterceptor`, `ErrorInterceptor`.
- i18n con `@angular/localize` (ES, CA, EN).
- A11y nivel **WCAG 2.2 AA**.
- Lazy-loaded feature modules por dominio.
- Build con presupuestos (`initial < 350 KB gz`, `lazy < 200 KB gz`).

---

## 13. Entornos

| Entorno  | Propósito        | Datos              | URL pública                       |
|----------|------------------|--------------------|------------------------------------|
| local    | Dev individual   | Docker Compose     | `http://localhost:4200`            |
| dev      | Integración cont.| Sintéticos         | `https://dev.magenta.internal`     |
| staging  | UAT + carga      | Anonimización RGPD | `https://stg.magenta.es`           |
| prod     | Producción       | Reales             | `https://app.magenta.es`           |

`docker-compose.yml` en raíz levanta: PostgreSQL 16, Kafka + Zookeeper, Schema Registry,
Redis, Keycloak, los 5 microservicios y el gateway.

---

## 14. Glosario de dominio

- **Inmueble (Property)**: unidad inmobiliaria comercializable (vivienda, local, nave, parking, terreno).
- **Operación (Operation)**: oferta sobre un inmueble (`SALE`, `RENT`, `RENT_TO_OWN`, `EXCHANGE`).
- **Zona (Zone)**: agrupación geográfica jerárquica.
- **Índice de precio (Price Index)**: €/m² calculado por zona y tipología, ventana móvil 90 días.
- **Cliente (Customer)**: titular potencial (persona física, jurídica o unidad familiar).
- **Capacidad hipotecaria**: cuota máxima admisible aplicando reglas BdE.
- **Esquema 90 + 5 + 5**: 90 % hipoteca + 5 % entrada + 5 % aplazado promotor.
- **Línea preferente**: producto bancario con LTV ≥ 90 % (GoHipoteca, Hipoteca Joven, etc.).
- **Servicio**: workflow contratable (tasación, seguro impago, búsqueda, gestoría).

---

## 15. ADRs

Carpeta `docs/adr/` con plantilla MADR. Cambios estructurales requieren ADR aprobado.

---

## 16. Cómo trabaja un módulo en paralelo

Cada módulo tiene su **`MODULE-SPEC.md`** autocontenido. Un IDE/Sonnet trabajando en `products/`
**no necesita** abrir `banks/` para implementar; consume:

1. Los **contratos REST** publicados aquí y en el spec del módulo destino.
2. Los **eventos Kafka** documentados aquí (sección 5.7).
3. La librería compartida `magenta-shared-kernel`.
4. Mocks Spring Cloud Contract publicados por cada módulo productor.

Cualquier necesidad nueva entre módulos se negocia vía PR sobre este documento + ADR.
