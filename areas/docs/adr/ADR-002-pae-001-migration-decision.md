# ADR-002 — PAE-001: Decisión de migración de stack (cierre formal)

**Estado:** Aceptado  
**Fecha:** 2026-05-30  
**Autores:** Equipo Magenta Platform  
**Revisado por:** Arquitectura  
**Relacionado con:** ADR-001-stack-versions.md

---

## Contexto

ADR-001 fijó el stack de la versión 1.0 en Java 21 / Spring Boot 3.3 / PostgreSQL 16 /
Kafka 3.7 / Redis 7.2 y creó el Plan de Actualización de Stack **PAE-001** para migrar a
las versiones recomendadas por `stack-tech_spec.md` en una versión futura.

Al revisar el estado del repositorio en la fecha de este ADR, se constatan dos hechos:

1. **Infraestructura local ya migrada (parcialmente):** El `docker-compose.yml` del módulo
   `areas` ya usa PostgreSQL 18 + PostGIS 3.5, Redis 8 y Kafka KRaft (Confluent 8.0.0), en
   línea con el baseline 30/05/2026. El comentario en el archivo lo refleja como
   "Decisión PAE-001 (parcial)".

2. **Código de aplicación pendiente:** Las dependencias Maven (`pom.xml`), el `Dockerfile`
   (imagen base `eclipse-temurin:21-jdk-alpine` → `gcr.io/distroless/java21-debian12`) y
   el código fuente continúan en Java 21 / Spring Boot 3.3, dado que la migración completa
   requiere coordinar todos los módulos de la plataforma (`banks`, `customers`, `products`,
   `servicios`) para evitar incompatibilidades cruzadas.

---

## Decisión

**Cierre del ítem PAE-001 como decisión documentada; migración de código diferida a v2.0.**

| Ámbito | Estado PAE-001 |
|--------|---------------|
| Infraestructura local (docker-compose) | ✅ Migrada a baseline (PG 18, Redis 8, Kafka KRaft) |
| Código aplicación — Java | ⏳ Permanece en Java 21 LTS hasta v2.0 |
| Código aplicación — Spring Boot | ⏳ Permanece en 3.3.x hasta v2.0 |
| Código aplicación — PostgreSQL driver/Flyway | ⏳ Compatibilidad PG 18 verificada; no requiere cambios |
| Código aplicación — Kafka client | ⏳ Kafka client 3.7 compatible con KRaft broker 8.0.0 |
| Código aplicación — Redis client | ⏳ Lettuce (Spring Data Redis) compatible con Redis 8 |

### Hoja de ruta v2.0 (objetivo Q4 2026)

| Componente | De | A |
|---|---|---|
| Java | 21 LTS | 25 LTS |
| Spring Boot | 3.3.x | 4.0.6+ |
| Spring Data JPA | 3.3.x | 4.x |
| Spring Security | 6.3.x | 7.x |
| PostgreSQL (driver) | 42.7.x | 42.8.x+ |
| Kafka client | 3.7 | 4.3.x |
| Redis client (Lettuce) | 6.x | 7.x |
| Kubernetes | 1.29+ | 1.36+ |

### Dependencias de la migración v2.0

- **Prerequisito 1:** Todos los módulos (`banks`, `customers`, `products`, `servicios`)
  deben migrar de forma coordinada; no se permite migración parcial por módulo.
- **Prerequisito 2:** Spring Boot 4.0 introduce cambios incompatibles en autoconfiguración,
  Security HTTP DSL y Actuator. Requiere rama de migración dedicada con cobertura de tests ≥ 80%.
- **Prerequisito 3:** Java 25 elimina APIs marcadas como `@Deprecated(forRemoval=true)` en
  Java 21. Requiere análisis de dependencias transitivas.

---

## Consecuencias

- **Positivo:** La infraestructura local ya está alineada con el baseline 2026, lo que
  permite desarrollar y probar contra las versiones finales de destino.
- **Positivo:** El código en Java 21 / Spring Boot 3.3 sigue recibiendo parches de seguridad
  (Spring Boot 3.3 EoL: agosto 2025 — **ACCIÓN REQUERIDA:** evaluar upgrade a 3.4.x / 3.5.x
  como paso intermedio antes de v2.0).
- **Negativo:** La divergencia entre la versión de infra (docker-compose) y el código impone
  que los tests de integración deben validar compatibilidad cruzada en CI.
- **Restricción:** Hasta que se complete v2.0, ningún módulo debe introducir APIs de
  Java 25 / Spring Boot 4 en el código de producción.

---

## Registro de compatibilidad verificada (2026-05-30)

| Versión infra | Versión cliente (código) | Estado |
|---|---|---|
| PostgreSQL 18.x | JDBC driver 42.7.x | ✅ Compatible — PG 18 mantiene protocolo wire v3 |
| Redis 8.x | Lettuce 6.x (Spring Data Redis 3.3.x) | ✅ Compatible — RESP2/RESP3 soportados |
| Kafka KRaft 8.0.0 | kafka-clients 3.7.x | ✅ Compatible — protocolo Kafka estable; sin ZooKeeper API |
| PostGIS 3.5 | Hibernate Spatial + JTS 1.19 | ✅ Compatible — WKB/WKT sin cambios |
