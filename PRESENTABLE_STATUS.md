# Estado de Presentabilidad — Civitas Capital

## Veredicto actual

**Todavía no lo subiría como repositorio público.**

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
- Checklist GitHub creado.
- `.gitignore` añadido para evitar subir `target/` y otros artefactos.

## Bloqueos antes de subir públicamente

### 1. Entorno Java no compatible

El entorno actual reporta:

```text
java version "1.8.0_411"
```

Pero los módulos requieren:

- `areas`: Java 21
- `products`: Java 21
- `banks`: Java 25
- `customers`: Java 25
- `servicios`: Java 25

Antes de validar build hay que instalar/configurar JDK 21 y JDK 25 o alinear temporalmente los módulos.

### 2. Maven no verificado completamente

`mvn` existe en `C:\j\maven\bin\mvn`, pero no se ha podido usar todavía para validar compilación real de los módulos.

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

1. Instalar/configurar Java 21 y Java 25.
2. Validar build módulo por módulo.
3. Arreglar los fallos mínimos que bloqueen demo.
4. Inicializar Git.
5. Hacer primer commit limpio sin `target/`.
6. Subir a repositorio privado.

## Decisión

**No está listo para publicación pública.**

**Sí puede pasar a fase de repositorio privado cuando se valide el estado de build o se acepte explícitamente subirlo como work-in-progress.**
