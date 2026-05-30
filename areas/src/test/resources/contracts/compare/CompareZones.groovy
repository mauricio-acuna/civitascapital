package contracts.compare

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "GET /compare — comparativa multi-zona"

    request {
        method GET()
        url("/api/v1/compare") {
            queryParameters {
                parameter("zoneIds", $(
                    consumer("0190af11-5f33-7a9b-9f10-ce0a7c41c8b1,0190af12-5f33-7a9b-9f10-ce0a7c41c8b2"),
                    producer("0190af11-5f33-7a9b-9f10-ce0a7c41c8b1,0190af12-5f33-7a9b-9f10-ce0a7c41c8b2")
                ))
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
            zones: [
                [
                    id:               "0190af11-5f33-7a9b-9f10-ce0a7c41c8b1",
                    name:             $(consumer(anyNonEmptyString()), producer("Madrid")),
                    salePricePerSqm:  $(consumer(anyPositiveInt()), producer(3200)),
                    rentPricePerSqm:  $(consumer(anyDouble()),      producer(12.5))
                ],
                [
                    id:               "0190af12-5f33-7a9b-9f10-ce0a7c41c8b2",
                    name:             $(consumer(anyNonEmptyString()), producer("Barcelona")),
                    salePricePerSqm:  $(consumer(anyPositiveInt()), producer(4100)),
                    rentPricePerSqm:  $(consumer(anyDouble()),      producer(15.2))
                ]
            ]
        )
    }
}
