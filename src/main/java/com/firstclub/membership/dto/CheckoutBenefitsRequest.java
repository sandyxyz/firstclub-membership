package com.firstclub.membership.dto;

import java.math.BigDecimal;

public record CheckoutBenefitsRequest(
        BigDecimal orderValue,
        boolean deliveryEligible,
        boolean selectedItemsEligible
) {
}
