package contracts.zones

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "GET /zones/{id} — devuelve detalle de zona existente"

    request {
        method GET()
        url($(consumer(regex('/api/v1/zones/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}')),
               producer("/api/v1/zones/0190af11-5f33-7a9b-9f10-ce0a7c41c8b1")))
        headers {
            accept("application/json")
        }
    }

    response {
        status OK()
        headers {
            contentType("application/json")
        }
        body(
            id:   "0190af11-5f33-7a9b-9f10-ce0a7c41c8b1",
            code: $(consumer(anyNonEmptyString()), producer("ES-AN-GR-18340")),
            name: $(consumer(anyNonEmptyString()), producer("Fuente Vaqueros")),
            type: $(consumer(anyNonEmptyString()), producer("MUNICIPALITY"))
        )
    }
}
