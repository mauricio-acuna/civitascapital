package com.magenta.customers.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProfessionalProfile {
    ContractType contractType;
    String employer;
    String jobTitle;
    Integer seniorityMonths;
    String sector;
    boolean itSector;
}
