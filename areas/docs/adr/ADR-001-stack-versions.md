# ADR-001 — Versiones de stack para el módulo `areas` y plataforma Magenta

**Estado:** Aceptado  
**Fecha:** 2026-05-30  
**Autores:** Equipo Magenta Platform  
**Revisado por:** Arquitectura

---

## Contexto

El documento `stack-tech_spec.md` (baseline 30/05/2026) recomienda para proyectos nuevos:

| Componente | Recomendación `stack-tech_spec.md` | Decisión Magenta |
|---|---|---|
| Java | 25 LTS | **21 LTS** |
| Spring Boot | 4.0.6+ | **3.3.x** |
| PostgreSQL | 18.4+ | **16** |
| Apache Kafka | 4.3.x+ | **3.7** |
| Redis | 8.x | **7.2** |
| Angular | 21 | **17 LTS** |
| Kubernetes | 1.36 | **1.29+** |

La plataforma Magenta fue iniciada antes de que la especificación de referencia reflejara las versiones actuales de mayo de 2026. Las versiones están fijadas en `ARCHITECTURE.md` como contrato cross-cutting, compartidas por todos los bounded contexts (`areas`, `banks`, `customers`, `products`, `servicios`).

---

## Decisión

Mantener el stack fijado en `ARCHITECTURE.md` para la versión 1.0 de la plataforma.

**Razones:**

1. **Coherencia de plataforma**: Cambiar versiones en un único módulo rompe la homogeneidad de la plataforma. El contrato de versiones es cross-cutting.
2. **Spring Boot 3.3 + Java 21**: Java 21 es LTS activo con soporte estándar Oracle hasta septiembre 2026 y soporte extendido hasta 2031. Spring Boot 3.3 recibe mantenimiento activo. La migración a Boot 4 requiere alinear toda la plataforma (cambios en autoconfiguración, configuración de seguridad, Actuator, etc.).
3. **PostgreSQL 16**: Soportado activamente hasta noviembre 2028. PostGIS 3.4 tiene soporte estable. La actualización a PG 18 requiere validar extensiones (`postgis`, `pg_trgm`, `unaccent`) y realizar pruebas de regresión de las consultas nativas (`tsvector`, `ST_Covers`, CTEs recursivas).
4. **Kafka 3.7**: Versión estable con soporte activo. KRaft disponible en producción desde Kafka 3.3. Kafka 4.x elimina ZooKeeper completamente; la migración requiere un plan coordinado de actualización del cluster.
5. **Redis 7.2**: LTS activo. Las funcionalidades usadas (cache-aside, Bucket4j rate limiting) son compatibles. Redis 8.x no aporta cambios que justifiquen la actualización en v1.0.
6. **Angular 17 + Kubernetes 1.29**: En línea con el frontend Angular existente de la plataforma; versión K8s dentro de la ventana de soporte del proveedor cloud objetivo.

---

## Consecuencias

- Los módulos de la plataforma deben cumplir los mismos contratos de versión.
- Se crea un **Plan de Actualización de Stack** (PAE-001) para migrar a Java 25 / Spring Boot 4 / PostgreSQL 18 / Kafka 4 en la versión 2.0 de la plataforma (objetivo Q4 2026 / Q1 2027).
- Los CVEs que afecten a las versiones actuales se gestionan mediante parches de patch version (ej. Spring Boot 3.3.x, PostgreSQL 16.x). La política de parches es mensual o urgente ante CVE crítico.
- Las recomendaciones de `stack-tech_spec.md` se aplican en todos los demás aspectos (arquitectura, seguridad, observabilidad, patrones, K8s hardening) sin excepción.

---

## Alternativas consideradas

**Actualización inmediata a Java 25 / Spring Boot 4**: Descartado por el coste de coordinación cross-módulo y el riesgo de introducir regresiones justo antes de la release 1.0.

**Actualización solo del módulo `areas`**: Descartado porque crearía una heterogeneidad interna que dificulta el soporte operativo, las pipelines compartidas y el template de nuevos módulos.

---

## Enmienda PAE-001 — Decisión parcial 2026-05-30

**Estado**: Aplicado parcialmente.

Tras revisión de coherencia inter-módulo se aprueba la siguiente migración **solo de infraestructura local** (`docker-compose.yml`), manteniendo `pom.xml` en v1.0:

| Componente | docker-compose.yml (nuevo) | pom.xml (sin cambio) |
|---|---|---|
| PostgreSQL + PostGIS | `postgis/postgis:18-3.5-alpine` | `postgresql:42.7.x` (driver PG 16 compat) |
| Redis | `redis:8-alpine` | `spring-data-redis` (Spring Boot 3.3) |
| Kafka | `confluentinc/cp-kafka:8.0.0` (KRaft) | `spring-kafka` (Spring Boot 3.3) |
| Schema Registry | `confluentinc/cp-schema-registry:8.0.0` | `kafka-avro-serializer` (Confluent 8 compat) |

**Motivo**: los módulos `banks` y `customers` ya usan Kafka KRaft en su `docker-compose.yml` y el cluster Kafka compartido en desarrollo es KRaft. Mantener Kafka con ZooKeeper en `areas` rompería la inter-operabilidad del entorno local compartido.

**Ficheros modificados**: `docker-compose.yml` (creado).  
**Ficheros NO modificados**: `pom.xml`, `application.yml`, `Dockerfile`, Helm charts.

**Próximo hito PAE-001 completo**: v2.0 — objetivo Q4 2026 / Q1 2027 (actualización de `pom.xml` a Java 25 / Spring Boot 4.0.x + Dockerfile a `eclipse-temurin:25` + revisión de APIs que cambian en Boot 4).
