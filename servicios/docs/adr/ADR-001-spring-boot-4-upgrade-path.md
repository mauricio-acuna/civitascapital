# ADR-001: Versiones de runtime — Spring Boot 3.5 / Java 25 → camino a Spring Boot 4

**Estado:** Aceptado  
**Fecha:** 2026-05-30  
**Decisores:** Equipo Magenta – módulo `servicios`

---

## Contexto

El stack-tech_spec.md de referencia de la plataforma Magenta establece como baseline
**Java 25 LTS + Spring Boot 4.0.6+ (Spring Framework 7)** para proyectos nuevos a 30/05/2026.

El MODULE-SPEC.md del módulo `servicios` fue redactado cuando la baseline de la plataforma
era Java 21 / Spring Boot 3.3, y el módulo depende de:

| Dependencia crítica | Versión actual | Compatibilidad con SB 4.0 |
|---|---|---|
| `io.camunda.spring:spring-boot-starter-camunda-sdk` | 8.7.0 | Por verificar; Camunda aún no ha publicado starter oficial para SB 4.0.x a la fecha de este ADR |
| `resilience4j-spring-boot3` | 2.3.0 | Requiere artefacto `resilience4j-spring-boot4` aún no estable |
| `springdoc-openapi-starter-webmvc-ui` | 2.8.5 | Requiere springdoc 3.x para SB 4 |
| `spring-cloud-starter-vault-config` | 2024.0.2 | Requiere Spring Cloud compatible con SB 4 |

## Decisión

Para la Release 0.1.x del módulo `servicios`:

- **Java: 21 → 25** ✅ — cambio seguro, JDK 25 es totalmente retrocompatible; el código compilado a `--release 25` produce mejores optimizaciones de GC y Virtual Threads.
- **Spring Boot: 3.3.5 → 3.5.2** ✅ — actualización dentro de la rama 3.x; requiere Spring Cloud `2024.0.2`. Camunda, Resilience4j y springdoc son compatibles.
- **Spring Boot 4.0.x** ⏳ **PENDIENTE** — se realizará en Release 0.2.x cuando:
  1. Camunda publique `spring-boot-starter-camunda-sdk:8.x` oficial para SB 4.
  2. Resilience4j publique `resilience4j-spring-boot4` estable.
  3. springdoc-openapi publique 3.x para SB 4.
  4. Spring Cloud publique release compatible con SB 4.

## Consecuencias

- El módulo opera sobre la LTS más reciente de Java (25).
- Spring Boot 3.5.x es la última minor en la rama 3.x; recibe parches de seguridad mientras la comunidad migra a SB 4.
- El paso a SB 4 implica revisar: `@EnableJdbcRepositories` deprecations, cambios en Spring Security 7 y namespace de algunas anotaciones Spring MVC.
- Este ADR debe revisarse en cuanto Camunda SDK publique soporte oficial para Spring Boot 4.
