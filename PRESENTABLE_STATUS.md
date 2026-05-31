# Estado de Presentabilidad — Civitas Capital

## Veredicto actual

**Ya está subido como repositorio público de trabajo, pero no lo presentaría todavía como producto final.**

Sí está bastante más cerca de ser presentable como repositorio privado para trabajo, demo e inversores técnicos.

## Avances realizados

- Marca pública cambiada a **Civitas Capital**.
- Posicionamiento comercial enfocado en **Civitas Pro**.
- README actualizado.
- Pitch para inversores actualizado.
- Veredicto crítico tipo inversor creado.
- Alcance del MVP definido.
- Roadmap de 90 días creado.
- Guion de demo creado.
- Dataset sintético creado.
- Demo HTML local creada.
- Caso de uso `SimulateNinetyFiveFiveUseCase` añadido en `banks`.
- Servicio de desglose `NinetyFiveFiveBreakdownService` añadido con tests unitarios.
- Endpoint `POST /api/v1/simulations/90-5-5` añadido en `banks`.
- Caso de uso `MarkPropertyFinanciableUseCase` añadido en `banks` para alimentar badges de financiación en inmuebles.
- Datos semilla de `banks` añadidos: 13 entidades y 13 productos hipotecarios sintéticos 90+5+5 para demo y sandbox.
- `LoanProductController` añadido en `banks`: permite buscar productos hipotecarios activos y ver detalle por ID.
- Motor `PropertyAffordabilityMatchService`, caso de uso `MatchAffordablePropertiesUseCase` y endpoint `POST /api/v1/property-matches/affordability` añadidos en `products`.
- `GET /api/v1/properties` y `GET /api/v1/properties/{id}` ahora devuelven catálogo real desde persistencia, con filtros básicos.
- `products` aplica tenant scope en búsquedas/detalle de inmuebles: activa `tenantFilter` y `app.tenant_id` para RLS; los endpoints públicos aceptan `X-Tenant-Id` para demo/sandbox.
- `products/Dockerfile` añadido con build Maven Java 21 y runtime Distroless nonroot.
- `banks/Dockerfile` corregido: ya no depende de `mvnw` inexistente y usa build Maven + runtime Distroless Java 25.
- Realm Keycloak demo añadido para `banks` y `products`: roles, clientes, usuarios demo y claim `tenant_id`.
- `products` corrige extracción de roles Keycloak desde `realm_access.roles`, alineado con `banks`.
- Endpoints públicos de catálogo en `banks` aceptan `X-Tenant-Id`, alineados con la demo multi-tenant.
- `banks` expone Euribor 12M (`GET /api/v1/indices/euribor`) con persistencia JPA y seed demo para sandbox.
- `banks` añade persistencia JPA para preaprobaciones e historial append-only (`preapprovals` + `preapproval_events`).
- `banks` añade persistencia y lectura REST de tasaciones: detalle, búsqueda por inmueble/cliente y última tasación vigente.
- `banks` corrige idempotencia Kafka: `processed_event` pasa a PK `(consumer_name, event_id)` y se añade `ProcessedEventService`.
- `banks` añade idempotencia HTTP con `Idempotency-Key` para simulaciones, comparativas, preaprobaciones y tasaciones.
- Checklist GitHub creado.
- `.gitignore` añadido para evitar subir `target/` y otros artefactos.
- JDK 21 y JDK 25 instalados localmente bajo `.jdks/` e ignorados por Git.
- `banks` compila y pasa su suite: 40 tests verdes con Java 25, incluyendo reglas ArchUnit.
- `products` compila y pasa su suite: 20 tests verdes con Java 21, incluyendo reglas ArchUnit y reporte JaCoCo.
- `banks` refuerza límites hexagonales: los puertos de dominio ya no dependen de Spring Data y los servicios transversales salen de `adapter.in`.
- CI de GitHub Actions añadido para validar `products` con Java 21 y `banks` con Java 25 en cada push/PR.

## Bloqueos antes de subir públicamente

### 1. Validación parcial del entorno Java

El entorno local ya tiene JDK 21 y JDK 25 instalados y probados para:

- `products`: Java 21
- `banks`: Java 25

Queda validar:

- `areas`: Java 21
- `customers`: Java 25
- `servicios`: Java 25

### 2. Maven verificado parcialmente

Maven ya validó `banks` y `products`. Faltan `areas`, `customers` y `servicios`.

### 3. Artefactos generados

Existen carpetas `target/` en:

- `areas/target`
- `banks/target`
- `customers/target`
- `products/target`
- `servicios/target`

Ya están ignoradas por `.gitignore`, pero conviene no incluirlas en el primer commit.

### 4. Marca interna pendiente

El código conserva nombres internos históricos (`com.magenta.*`, topics `magenta.*`, etc.). No bloquea una demo privada, pero sí debe explicarse.

### 5. Módulos incompletos

`banks` y `products` tienen pendientes relevantes según la propia documentación del proyecto. No conviene presentarlos como terminados.

## Qué sí se puede enseñar ya

- Narrativa de producto.
- Pitch inversor.
- Demo HTML local.
- Dataset sintético.
- Roadmap 90 días.
- Arquitectura backend como base avanzada en construcción.

## Próximo paso recomendado

1. Validar build de `areas`, `customers` y `servicios`.
2. Arreglar los fallos mínimos que bloqueen demo integrada.
3. Completar endpoints y consumers estrictamente necesarios para el flujo Civitas Pro.
4. Mantener el repositorio privado antes de una revisión externa seria.
5. Preparar una demo guiada con datos sintéticos y métricas claras.

## Decisión

**No está listo para publicación pública final.**

**Sí puede usarse como repositorio de trabajo y base de demo técnica, siempre explicando que es work-in-progress.**
