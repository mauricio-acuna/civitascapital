package contracts.zones

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "GET /zones/search — devuelve lista de zonas coincidentes"

    request {
        method GET()
        url("/api/v1/zones/search") {
            queryParameters {
                parameter("q", "Madrid")
                parameter("limit", "10")
            }
        }
        headers {
            accept("application/json")
        }
    }

    response {
        status OK()
        headers {
            contentType("application/json")
        }
        body([
            [
                id:   $(consumer(regex('[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}')),
                       producer(anyUuid())),
                code: $(consumer(anyNonEmptyString()), producer("ES-MD-MDC")),
                name: $(consumer(anyNonEmptyString()), producer("Madrid")),
                type: $(consumer(anyNonEmptyString()), producer("MUNICIPALITY"))
            ]
        ])
    }
}
