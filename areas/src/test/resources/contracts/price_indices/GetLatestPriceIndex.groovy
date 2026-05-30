package contracts.price_indices

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "GET /price-indices/latest — devuelve el índice de precio más reciente"

    request {
        method GET()
        url("/api/v1/price-indices/latest") {
            queryParameters {
                parameter("zoneId", $(consumer(regex('[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}')),
                                      producer("0190af11-5f33-7a9b-9f10-ce0a7c41c8b1")))
                parameter("type", "FLAT")
                parameter("op", "SALE")
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
        body(
            zoneId:       "0190af11-5f33-7a9b-9f10-ce0a7c41c8b1",
            propertyType: "FLAT",
            operationType:"SALE",
            period:       $(consumer(anyNonEmptyString()), producer("2026-05-01")),
            pricePerSqm:  $(consumer(anyPositiveInt()), producer(1250)),
            currency:     "EUR",
            confidence:   $(consumer(anyDouble()), producer(0.85))
        )
    }
}
