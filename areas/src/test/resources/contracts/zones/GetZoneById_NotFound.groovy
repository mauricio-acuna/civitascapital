package contracts.zones

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "GET /zones/{id} — zona no encontrada devuelve 404 con ProblemDetail"

    request {
        method GET()
        url("/api/v1/zones/00000000-0000-0000-0000-000000000000")
        headers {
            accept("application/problem+json")
        }
    }

    response {
        status NOT_FOUND()
        headers {
            contentType("application/problem+json")
        }
        body(
            status: 404,
            title: $(consumer(anyNonEmptyString()), producer("Zone Not Found"))
        )
    }
}
