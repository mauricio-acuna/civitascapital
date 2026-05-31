# Civitas Capital

**Civitas Capital** es una plataforma SaaS inmobiliaria-financiera diseñada para el mercado español que conecta inventario, zonas, clientes, financiación bancaria y servicios de cierre en una arquitectura moderna de microservicios.

La idea central es sencilla: ayudar a una persona, familia, agente o inversor a tomar mejores decisiones inmobiliarias combinando datos de mercado, solvencia, financiación, disponibilidad real de inmuebles y servicios asociados como tasación, seguros, gestoría, mediación hipotecaria o búsqueda profesional.

## Por qué es interesante

El mercado inmobiliario no falla por falta de portales; falla porque las decisiones se toman con información fragmentada. Un comprador mira pisos en una web, calcula la hipoteca en otra, pregunta a bancos por separado, compara barrios manualmente y acaba contratando servicios externos sin trazabilidad.

Civitas Capital plantea una experiencia integrada:

- Saber si una vivienda es financiable antes de enamorarse de ella.
- Comparar zonas por precio, servicios, riesgo, conectividad y demanda.
- Calcular capacidad hipotecaria con reglas prudentes de esfuerzo y endeudamiento.
- Gestionar clientes, KYC, documentación y consentimientos RGPD.
- Orquestar servicios de valor añadido alrededor de cada operación.
- Alimentar índices de precio por zona a partir de transacciones reales.

## Propuesta de valor

Civitas Capital no es solo un catálogo de inmuebles. Es una capa de inteligencia operativa para operaciones de compra, alquiler, financiación y servicios inmobiliarios.

Sus puntos fuertes:

- **Arquitectura de producto seria**: microservicios Spring Boot, DDD ligero, arquitectura hexagonal, eventos de dominio y separación estricta por bounded context.
- **Especialización en España**: zonas INE/Catastro, reglas de esfuerzo bancario, RGPD, KYC, financiación 90+5+5, alquiler con seguro de impago, notaría, gestoría e impuestos.
- **Modelo multi-tenant**: pensado para agencias, brokers, franquicias, promotoras, servicers, bancos o marketplaces B2B.
- **Datos geográficos enriquecidos**: jerarquía de zonas, índices €/m2, servicios, fibra, riesgo, demanda y comparación territorial.
- **Financiación integrada**: simulaciones hipotecarias, TAE, elegibilidad, preaprobaciones, LTV y productos bancarios preferentes.
- **Workflow monetizable**: contratación de servicios, pagos, partners, entregables, SLA y facturación.
- **Seguridad y auditoría desde el diseño**: Keycloak, JWT, RLS por tenant, outbox, idempotencia, trazabilidad y documentación técnica.

## Módulos principales

| Módulo | Puerto | Responsabilidad |
| --- | ---: | --- |
| `areas` | 8081 | Geografía, zonas, índices de precio, demanda y enriquecimiento territorial |
| `banks` | 8082 | Bancos, productos hipotecarios, simulaciones, preaprobaciones y tasaciones |
| `customers` | 8083 | Identidad, KYC, perfil financiero, documentación, preferencias y RGPD |
| `products` | 8084 | Catálogo de inmuebles, operaciones, leads, visitas, favoritos y transacciones |
| `servicios` | 8085 | Catálogo de servicios, workflows, partners, pagos, entregables y SLA |

## Arquitectura

Cada módulo es un microservicio autónomo con su propio esquema PostgreSQL. La comunicación entre módulos se realiza mediante REST para consultas síncronas y Kafka para eventos de dominio.

```text
                 Angular SPA / BFF
                       |
                  API Gateway
                       |
      +----------+-----+------+----------+
      |          |            |          |
   areas      banks      customers   products
      \          |            |          /
       \---------+--- Kafka --+---------/
                     |
                 servicios
```

Principios internos:

- Dominio sin dependencias de Spring/JPA/Kafka.
- Casos de uso en capa `application`.
- Adaptadores REST, Kafka, persistencia y clientes externos en `infrastructure`.
- Eventos de dominio publicados mediante Outbox Pattern.
- Consumidores idempotentes.
- APIs versionadas bajo `/api/v1`.
- Errores en formato Problem Details.
- Multi-tenancy con `tenant_id` y Row Level Security.

## Stack técnico

- Java 21/25 según módulo
- Spring Boot 3.x/4.x según evolución del módulo
- Maven
- PostgreSQL + PostGIS
- Flyway
- Kafka KRaft
- Redis
- Keycloak OIDC/JWT
- MapStruct
- Resilience4j
- OpenTelemetry, Prometheus y Grafana
- Docker, Helm y Kubernetes
- JUnit 5, Testcontainers, ArchUnit y WireMock
- Camunda/Zeebe en `servicios`
- OpenSearch en `products`

## Funcionalidades destacadas

### Decisión inmobiliaria asistida

Civitas Capital permite comparar alquiler frente a compra, analizar zonas, estimar capacidad financiera y detectar oportunidades según precio, riesgo, financiación y demanda.

### Motor geográfico y de mercado

El módulo `areas` modela desde país hasta portal y añade indicadores como precio por metro cuadrado, tendencia, stock, cobertura de fibra, servicios, riesgo de ocupación, despoblación y demanda relativa.

### Evaluación bancaria

El módulo `banks` cubre productos hipotecarios, simulación de cuotas, TAE, ratio de esfuerzo, ratio de endeudamiento, fondos propios requeridos, scoring de aprobabilidad y preaprobación.

### Cliente financiero y RGPD

El módulo `customers` mantiene la identidad, KYC, unidad familiar, perfil económico, documentación, preferencias de búsqueda y consentimientos.

### Inventario inmobiliario inteligente

El módulo `products` gestiona inmuebles, operaciones, multimedia, leads, visitas, favoritos, transacciones y financiación disponible.

### Marketplace de servicios

El módulo `servicios` orquesta tasaciones, mediación hipotecaria, seguros, búsqueda profesional, gestoría, mudanzas, suministros, asesoría fiscal y otros servicios monetizables.

## Estado del proyecto

El repositorio contiene una base backend amplia y orientada a producción. Hay módulos muy avanzados y otros en fase de cierre.

- `areas`: módulo muy avanzado, con dominio, API, persistencia, eventos, ingesta, Helm y observabilidad.
- `customers`: dominio, casos de uso, persistencia, REST, KYC, RGPD, documentación y eventos implementados en gran parte.
- `servicios`: módulo avanzado con workflows, catálogo, órdenes, partners, pagos, facturación y Camunda/Zeebe.
- `products`: base sólida de dominio, persistencia, búsqueda, eventos y casos de uso principales; build y tests verificados con Java 21.
- `banks`: dominio y reglas financieras fuertes, productos hipotecarios, simulación, preaprobaciones, tasaciones e idempotencia; build y tests verificados con Java 25.

Consulta `HANDOVER.md` y los `MODULE-SPEC.md` de cada módulo para el detalle exacto del estado.

## Estructura del repositorio

```text
.
├── areas/        # Geografía, zonas e indicadores de mercado
├── banks/        # Productos financieros, simulación y preaprobación
├── customers/    # Identidad, KYC, perfil financiero y RGPD
├── products/     # Inmuebles, operaciones, leads y transacciones
├── servicios/    # Servicios añadidos, workflows, partners y pagos
├── IDEABASE/     # Conceptos, prototipos y documentación de producto
└── HANDOVER.md   # Estado global y guía de continuidad
```

## Ejecución local

Cada módulo incluye su propio `docker-compose.yml` y `pom.xml`. De forma general:

```bash
cd areas
docker compose up -d
mvn test
mvn spring-boot:run
```

Repite el patrón para `banks`, `customers`, `products` o `servicios`, ajustando variables de entorno y dependencias externas cuando corresponda.

Validación actualmente verificada:

```bash
cd products && mvn test   # Java 21
cd banks && mvn test      # Java 25
```

## Documentación útil

- `START_HERE.md`: guía de lectura para entender el repositorio.
- `Veredicto.md`: análisis crítico tipo inversor/Shark Tank.
- `BRAND_STRATEGY.md`: estrategia de marca y posicionamiento de Civitas Capital.
- `HANDOVER.md`: visión global, estado real y plan de cierre.
- `MVP_SCOPE.md`: alcance del primer producto vendible, Civitas Pro.
- `ROADMAP_90_DIAS.md`: plan de ejecución para llegar a demo, pilotos y paquete inversor.
- `DEMO_SCRIPT.md`: guion de demo de 7 minutos para inversores y clientes.
- `GITHUB_READINESS.md`: checklist antes de subir el repositorio.
- `PRESENTABLE_STATUS.md`: estado actual de preparación y bloqueos antes de publicación.
- `demo/civitas-pro-demo.html`: demo visual local del flujo de cualificación.
- `areas/MODULE-SPEC.md`: especificación del motor geográfico.
- `banks/MODULE-SPEC.md`: especificación financiera.
- `customers/MODULE-SPEC.md`: especificación de identidad, KYC y solvencia.
- `products/MODULE-SPEC.md`: especificación del catálogo inmobiliario.
- `servicios/MODULE-SPEC.md`: especificación del marketplace de servicios.
- `PITCH_INVERSORES.md`: estrategia de producto, mercado, monetización y narrativa para inversores.

## Roadmap sugerido

1. Convertir el alcance en **Civitas Pro**, un MVP de cualificación financiera para agencias y brokers.
2. Crear una demo integrada con datos sintéticos.
3. Completar solo los endpoints necesarios para el flujo de demo.
4. Preparar pilotos con agencias, brokers hipotecarios o promotoras.
5. Medir conversión: lead inmobiliario, simulación financiera, contratación de servicio y preaprobación.
6. Cerrar después la deuda técnica de `products` y `banks` que bloquee escalabilidad.

## Licencia

Pendiente de definir. Si el objetivo es captar inversión o clientes B2B, conviene mantener el código privado hasta decidir una estrategia clara de propiedad intelectual, open-core o licensing comercial.
