# GitHub Readiness — Civitas Capital

## Estado actual

El proyecto ya está subido a GitHub como repositorio de trabajo.

Todavía **no está listo para presentarse públicamente como repositorio final**. Sí está entrando en una fase mucho más defendible para preparación privada, demo técnica y revisión de inversores.

## Razones

- `banks` y `products` ya compilan y pasan tests, pero `areas`, `customers` y `servicios` siguen pendientes de validación reciente.
- Hay documentación interna extensa con nombres históricos.
- Algunos módulos tienen pendientes técnicos importantes.
- La marca pública ya cambió a Civitas Capital, pero el código mantiene namespaces históricos.
- El repositorio debe pasar por una revisión de secretos, licencias y narrativa antes de enseñarse como activo comercial.

## Qué sí puede subirse en privado

- Documentación de producto.
- Arquitectura.
- Roadmap.
- MVP scope.
- Demo script.
- Código backend como base técnica en progreso.

## Qué no conviene hacer todavía

- Publicarlo como open source público.
- Presentarlo como producto terminado.
- Prometer preaprobación bancaria real.
- Prometer retorno garantizado.
- Subir artefactos compilados, credenciales o configuración local sensible.

## Checklist antes de GitHub

- `.gitignore` creado.
- `target/`, `.jdks/`, logs y notas personales ignorados.
- Primeros commits subidos a GitHub.
- CI añadido para `banks` y `products`.
- Revisar secretos y credenciales antes de hacerlo privado/compartirlo.
- Añadir instrucciones de arranque validadas por módulo.
- Actualizar estado real por módulo tras cada build.
- Preparar licencia o dejar explícito "proprietary".
- Decidir si el repositorio será privado.

## Recomendación

Subir inicialmente como repositorio **privado**.

Mensaje recomendado:

> Civitas Capital is an investor-ready real estate finance SaaS prototype focused on buyer qualification, property affordability and service monetization for agencies, brokers and developers.
