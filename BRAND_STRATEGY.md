# Civitas Capital — Estrategia de marca

## Decisión

El proyecto deja de presentarse públicamente como **Magenta** y pasa a llamarse **Civitas Capital**.

## Motivo

El nombre anterior resultaba poco adecuado para una propuesta inmobiliaria-financiera orientada a inversores, bancos, promotoras, brokers y clientes B2B. **Civitas Capital** transmite mejor:

- Seguridad institucional.
- Relación con ciudad, vivienda, territorio y comunidad.
- Capital, financiación, activos y retorno.
- Seriedad para conversaciones con inversores y partners financieros.

## Posicionamiento

> Civitas Capital cualifica compradores inmobiliarios, conecta financiación y convierte cada operación en un flujo trazable de cierre, servicios y capital.

## Arquitectura de producto

- **Civitas Pro**: SaaS para agencias, brokers y promotoras.
- **Civitas Finance**: simulación, solvencia, elegibilidad y preaprobación.
- **Civitas Areas**: datos de zona, precio, riesgo, demanda y calidad territorial.
- **Civitas Services**: marketplace de servicios de cierre.
- **Civitas Capital API**: datos y conectores para partners.

## Nota legal y de confianza

Debe evitarse el uso comercial de expresiones como "garantía total", "retorno garantizado" o "aprobación asegurada" salvo que exista base contractual, financiera y regulatoria para sostenerlo.

La promesa correcta es:

> Mayor claridad, trazabilidad y probabilidad de cierre mediante cualificación financiera temprana.

No:

> Garantía absoluta de financiación o retorno.

## Refactor técnico pendiente

El cambio aplicado es de **marca pública y documentación comercial**. Todavía existen nombres internos históricos en código, paquetes, topics y documentación técnica (`com.magenta.*`, `magenta.*`, etc.).

Renombrar esos elementos requiere una refactorización técnica separada porque afecta a:

- Paquetes Java.
- Topics Kafka.
- Schemas y migraciones.
- Configuración de Keycloak.
- Documentación de arquitectura.
- Contratos entre módulos.
- Tests y rutas de despliegue.

Recomendación: hacer esa refactorización solo después de estabilizar el MVP y decidir la marca final.

