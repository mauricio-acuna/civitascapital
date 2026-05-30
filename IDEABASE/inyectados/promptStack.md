# Prompt maestro — Generación de especificación de stack técnico Angular + Spring + PostgreSQL

## Uso

Copia el bloque de prompt siguiente en un modelo con navegación web habilitada. Sustituye las variables entre `{{...}}` si el proyecto tiene requisitos concretos. El prompt obliga a distinguir baseline obligatoria de componentes opcionales y evita diseñar una arquitectura sobredimensionada.

---

## Prompt

Actúa como un arquitecto principal de software, cloud y seguridad con experiencia real en plataformas Java empresariales. Produce una **especificación técnica de producción extremadamente detallada**, en español, para una aplicación web moderna con los siguientes condicionantes:

### Contexto base

- Frontend: **Angular**.
- Backend: **Java + Spring Boot**.
- Base de datos relacional principal: **PostgreSQL**.
- Tipo de negocio/dominio: `{{describir dominio o indicar "genérico empresarial"}}`.
- Cloud objetivo: `{{AWS / Azure / GCP / cloud-agnostic}}`.
- Requisitos regulatorios: `{{GDPR, PCI DSS, banca, salud, ninguno especificado}}`.
- Criticidad: `{{media / alta / misión crítica}}`.
- Carga inicial y crecimiento: `{{usuarios, RPS, volumen de datos, o "por determinar"}}`.
- RPO/RTO requeridos: `{{valores o "proponer baseline"}}`.
- Modelo de identidad: `{{IdP corporativo/OIDC/por determinar}}`.

### Regla de actualidad y fuentes

Antes de decidir versiones, navega y verifica en **fuentes oficiales** las versiones soportadas o estables vigentes a la fecha actual de:

- Angular y su tabla de compatibilidad con Node.js/TypeScript/RxJS.
- Java LTS y Spring Boot/Spring Framework.
- PostgreSQL y sus releases de seguridad soportadas.
- Kafka, RabbitMQ y Redis si se recomiendan.
- Kubernetes si se despliega sobre Kubernetes.
- OpenTelemetry/Spring/Micrometer para observabilidad.
- OWASP ASVS, OWASP Top 10 y OWASP API Security Top 10.

Indica la **fecha de baseline**, incluye una tabla de versiones y enlaza las referencias oficiales. No inventes versiones ni presentes una release no-LTS de Java como elección por defecto para producción.

### Regla de arquitectura: evita el sobre-diseño

No asumas que “estándar empresarial” significa incluir todos los middlewares desde el día uno.

Debes:
- Recomendar **monolito modular con arquitectura hexagonal** como punto de partida por defecto, salvo que los requisitos aportados justifiquen microservicios.
- Definir cuándo extraer microservicios y qué evidencia debe existir.
- Tratar PostgreSQL como **system of record**.
- Incluir Redis sólo para caché, rate limiting, estado efímero u otro uso explícitamente justificado.
- Incluir Kafka cuando haya eventos durables, replay, integraciones desacopladas, streaming o CDC.
- Incluir RabbitMQ únicamente cuando work queues, routing AMQP, prioridades o semántica de cola lo hagan preferible. Explica claramente que Kafka y RabbitMQ no deben incorporarse simultáneamente sin una razón documentada.
- Evitar “exactly once” como promesa end-to-end: diseña at-least-once con idempotencia.

### Alcance obligatorio del documento

Genera un documento Markdown denominado `stack-tech_spec.md` con, como mínimo, estas secciones y nivel de detalle:

1. **Propósito, alcance y supuestos.**
   - Objetivos no funcionales medibles: disponibilidad, latencia p95/p99, error rate, RPO, RTO, seguridad, despliegue y observabilidad.
   - Riesgos y requisitos aún por confirmar.

2. **Baseline tecnológica.**
   - Tabla completa por capas: Angular, Node/TypeScript, Java LTS, Spring Boot, Spring Security, persistencia, Flyway, PostgreSQL, Redis opcional, Kafka opcional/recomendado por caso, RabbitMQ opcional, Resilience4j, gateway, contenedores, Kubernetes, Helm, IaC, GitOps, OpenTelemetry, Prometheus, Grafana, logs/traces, testing y seguridad de supply chain.
   - Versiones verificadas con fuentes oficiales y política de patching.

3. **Arquitectura lógica y de despliegue.**
   - Diagrama Mermaid de contenedores.
   - Bounded contexts de ejemplo y ownership.
   - Decisión monolito modular versus microservicios.
   - Criterios verificables para extraer servicios.

4. **Frontend Angular.**
   - Arquitectura standalone y feature-first.
   - Routing/lazy loading, signals/estado, generación de cliente API, errores y correlación.
   - Rendimiento, bundle budgets, accesibilidad WCAG.
   - Seguridad frontend: OIDC Authorization Code + PKCE, alternativa BFF, CSP, Trusted Types, AOT, CSRF/cookies, CORS, headers, XSS y gestión de tokens.
   - Estructura de carpetas.

5. **Backend Java/Spring.**
   - Componentes concretos de Spring.
   - Arquitectura hexagonal/ports and adapters y Clean Architecture.
   - Estructura de paquetes por dominio.
   - Regla de dependencias.
   - API REST contract-first/OpenAPI, versionado, Problem Details, paginación, idempotency keys, optimistic concurrency.
   - Configuración orientativa segura.

6. **Persistencia y PostgreSQL.**
   - Modelado, constraints, índices, datos personales, schema ownership.
   - Flyway y expand-contract.
   - Pooling, PgBouncer si procede.
   - HA, backups, PITR, réplicas, DR, cifrado y aislamiento de red.
   - Política de consultas y performance.

7. **Transaccionalidad y consistencia distribuida.**
   - Límites de transacción local, isolation levels y optimistic/pessimistic locking.
   - Explicar por qué el dual-write BD + broker es incorrecto.
   - Transactional Outbox con secuencia Mermaid.
   - Inbox/idempotent consumers, deduplicación, saga orquestada versus coreografiada.
   - CQRS ligero y event sourcing sólo bajo justificación.

8. **Redis.**
   - Casos de uso permitidos, TTL, invalidación, cache-aside, stampede protection.
   - Seguridad TLS/ACL/red privada/rotación/parcheo.
   - Métricas, límites y comportamiento si Redis falla.
   - Prohibición de usarlo como única verdad de negocio.

9. **Mensajería.**
   - Matriz comparativa Kafka versus RabbitMQ.
   - Kafka: tópicos, claves, particiones, orden, KRaft, replicación, acks, DLT, schema registry, retención, PII, ACL/TLS, consumer lag.
   - RabbitMQ si procede: quorum queues, exchanges, confirms, DLQ, prefetch, prioridades.
   - Modelo de evento versionado e idempotencia.

10. **Seguridad end-to-end.**
    - OWASP ASVS, Top 10 y API Security.
    - Threat modeling.
    - OIDC/OAuth2, MFA, RBAC/ABAC, BOLA/tenant isolation, auditoría.
    - TLS, WAF/API Gateway, rate limiting, CORS, SSRF, uploads, webhooks.
    - Secret manager/KMS, cifrado, rotación, masking y privacidad/GDPR si aplica.
    - Kubernetes hardening, Pod Security restricted, NetworkPolicies, RBAC, non-root/read-only/securityContext.
    - Supply chain: SAST, SCA, secrets scan, DAST, container/IaC scan, SBOM y firma.

11. **Resiliencia.**
    - Timeouts, retries únicamente idempotentes con exponential backoff+jitter, circuit breaker, bulkhead, rate limiter, fallback.
    - Ejemplo de configuración Resilience4j.
    - Failure modes y degradación controlada.

12. **Escalabilidad, clusterización y alta disponibilidad.**
    - Stateless pods, HPA, PDB, probes y resource limits.
    - Topología HA para Spring, PostgreSQL, Redis, Kafka, RabbitMQ opcional y observabilidad.
    - Multi-AZ, cuándo multi-region y coste de complejidad.
    - Tabla de escenarios de fallo y respuesta esperada.

13. **Observabilidad y monitoring.**
    - Métricas, logs estructurados y trazas con OpenTelemetry/OTLP.
    - Prometheus/Grafana/Alertmanager y opción Loki/Tempo o servicio managed.
    - RED/USE, métricas JVM, BD, cache, brokers y negocio.
    - SLO/error budgets, alertas accionables, runbooks, retención y masking.
    - Ejemplo JSON de log correlacionado.

14. **DevSecOps, IaC y CI/CD.**
    - Repositorio sugerido.
    - Trunk-based/PRs, artifacts inmutables, entornos y configuración.
    - Pipeline detallado de frontend y backend.
    - Helm/IaC/GitOps, canary/blue-green/rolling y rollback.
    - Quality gates y promotion de mismo artefacto firmado.

15. **Testing.**
    - Unit, architecture tests, integration con Testcontainers, contratos, E2E, seguridad, performance y resilience testing.
    - Definition of Done comprobable.

16. **Patrones y calidad.**
    - Hexagonal, Clean Architecture, DDD táctico mínimo, Adapter, Strategy, Anti-Corruption Layer, Strangler Fig, Outbox/Inbox, Saga, CQRS condicional, Feature Flags y BFF.
    - Explicar cuándo usar y cuándo no cada patrón.
    - ADRs obligatorios.

17. **Plan incremental de adopción.**
    - Foundations, MVP productivo, asincronía/eventos, escalado/servicios adicionales.
    - Qué no introducir hasta tener necesidad demostrada.

18. **Checklist production readiness.**

19. **Fuentes oficiales verificadas.**

20. **Decisión ejecutiva final**, corta y no ambigua.

### Calidad y estilo exigidos

- Escribe en español profesional y técnico.
- Sé específico: configura, compara y justifica; no escribas marketing.
- Corrige supuestos defectuosos: una arquitectura con más herramientas no es automáticamente más estándar ni mejor.
- Incluye tablas y bloques de configuración útiles, pero no generes una implementación completa.
- Diferencia claramente controles obligatorios, recomendados y opcionales.
- No uses tecnologías obsoletas ni dependencias abandonadas.
- Evita afirmar cumplimiento normativo sólo por incluir herramientas; describe controles y evidencias.
- Toda decisión que dependa de carga, regulación o cloud deberá quedar marcada como decisión a confirmar o ADR.

### Salida

Entrega únicamente el contenido Markdown completo de `stack-tech_spec.md`, listo para guardarse en repositorio y revisarse en una sesión de arquitectura.
