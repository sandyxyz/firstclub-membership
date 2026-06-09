package com.firstclub.membership.dto;

import com.firstclub.membership.domain.Benefit;

import java.math.BigDecimal;
import java.util.List;

public record CheckoutBenefitsResponse(
        String userId,
        String tierId,
        boolean freeDelivery,
        int discountPercent,
        BigDecimal discountAmount,
        BigDecimal payableOrderValue,
        List<Benefit> availableBenefits
) {
}
