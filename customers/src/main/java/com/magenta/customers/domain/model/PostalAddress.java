package com.magenta.customers.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PostalAddress {
    String street;
    String number;
    String floor;
    String postalCode;
    String city;
    String province;
    String countryCode;
    java.util.UUID zoneId;
}
