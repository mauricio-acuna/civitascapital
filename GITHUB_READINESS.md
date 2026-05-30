# GitHub Readiness — Civitas Capital

## Estado actual

El proyecto todavía **no está listo para subirse públicamente como repositorio final**.

Sí está listo para una fase privada de preparación.

## Razones

- No hay repositorio Git inicializado en esta carpeta.
- Existen artefactos `target/` compilados que no deberían subirse.
- Hay documentación interna extensa con nombres históricos.
- Algunos módulos tienen pendientes técnicos importantes.
- `banks` y `products` necesitan revisión antes de presentarse como estables.
- La marca pública ya cambió a Civitas Capital, pero el código mantiene namespaces históricos.

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

- Crear `.gitignore`.
- Revisar secretos y credenciales.
- Eliminar o ignorar `target/`.
- Añadir instrucciones de arranque.
- Añadir estado real por módulo.
- Preparar licencia o dejar explícito "proprietary".
- Crear rama `main`.
- Hacer primer commit limpio.
- Decidir si el repositorio será privado.

## Recomendación

Subir inicialmente como repositorio **privado**.

Mensaje recomendado:

> Civitas Capital is an investor-ready real estate finance SaaS prototype focused on buyer qualification, property affordability and service monetization for agencies, brokers and developers.

