package com.magenta.customers.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.util.List;

@Getter
@Builder
@With
public class HouseholdProfile {

    public enum Relationship {
        MARRIAGE, REGISTERED_PARTNERSHIP, FAMILY, OTHER
    }

    private final List<HouseholdMember> members;
    private final Relationship relationship;
    private final int dependentsCount;
    // Snapshot financiero agregado calculado a partir de los titulares
    private final FinancialSnapshot aggregatedFinancials;
}
