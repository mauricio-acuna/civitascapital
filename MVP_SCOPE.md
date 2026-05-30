# Civitas Pro — Alcance del MVP

## Objetivo

Construir el primer producto vendible de Civitas Capital:

> Cualificación financiera de compradores inmobiliarios para agencias, brokers hipotecarios y promotoras.

El MVP debe demostrar que una agencia puede ahorrar tiempo, filtrar mejor sus leads y derivar oportunidades financieras o servicios con mayor probabilidad de cierre.

## Usuario principal

**Agente inmobiliario o broker hipotecario** que recibe leads y necesita saber:

- Si el comprador puede comprar.
- Qué rango de precio es realista.
- Qué inmuebles encajan.
- Qué financiación puede explorar.
- Qué servicios se pueden activar.

## Flujo principal

1. El agente registra o importa un lead comprador.
2. Introduce ingresos, ahorros, deudas, edad, contrato y zona deseada.
3. El sistema calcula capacidad de compra.
4. El sistema muestra esfuerzo, endeudamiento y fondos propios necesarios.
5. El sistema cruza el perfil con inmuebles compatibles.
6. El sistema genera una simulación hipotecaria orientativa.
7. El sistema recomienda siguientes pasos: broker, tasación, gestoría, seguro o visita.
8. El agente obtiene una ficha de lead cualificado.

## Funciones incluidas

- Registro básico de comprador.
- Perfil financiero simplificado.
- Cálculo de capacidad de compra.
- Simulación de cuota hipotecaria.
- Clasificación: `VIABLE`, `AJUSTADO`, `NO VIABLE`.
- Matching simple con inmuebles.
- Recomendación de servicios.
- Export o vista resumen para agente.
- Datos demo precargados.

## Funciones excluidas por ahora

- Integración bancaria real.
- Preaprobación oficial.
- Scoring crediticio regulado.
- KYC completo.
- OCR documental.
- Portal inmobiliario público completo.
- Marketplace abierto de partners.
- Integración con Catastro/INE real en tiempo real.
- App móvil.

## Por qué se excluyen

Porque no son necesarias para validar el primer negocio. El MVP debe probar si una agencia o broker pagaría por cualificar leads y acelerar cierres.

## Métricas de éxito

- Leads cualificados por semana.
- Porcentaje de leads viables.
- Tiempo medio de cualificación.
- Derivaciones a broker o servicio.
- Conversión a visita.
- Conversión a solicitud hipotecaria.
- Ingreso potencial por lead o agencia.

## Demo mínima

Datos sintéticos:

- 10 compradores.
- 25 inmuebles.
- 5 zonas.
- 6 productos hipotecarios orientativos.
- 5 servicios de cierre.

Escenario estrella:

> Una familia con 4.200 EUR netos mensuales, 55.000 EUR de ahorros y 250 EUR de deuda mensual busca vivienda. Civitas Pro calcula el rango realista, descarta inmuebles inviables, recomienda 3 viviendas compatibles y sugiere mediación hipotecaria + tasación.

