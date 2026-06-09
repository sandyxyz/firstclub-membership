package com.firstclub.membership.service;

import com.firstclub.membership.domain.Benefit;
import com.firstclub.membership.domain.BenefitType;
import com.firstclub.membership.domain.UserMembership;
import com.firstclub.membership.dto.CheckoutBenefitsRequest;
import com.firstclub.membership.dto.CheckoutBenefitsResponse;
import com.firstclub.membership.exception.ValidationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CheckoutBenefitService {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final MembershipService membershipService;

    public CheckoutBenefitService(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    public CheckoutBenefitsResponse calculate(String userId, CheckoutBenefitsRequest request) {
        if (request == null || request.orderValue() == null) {
            throw new ValidationException("orderValue is required");
        }
        if (request.orderValue().signum() < 0) {
            throw new ValidationException("orderValue cannot be negative");
        }

        UserMembership membership = membershipService.getActiveMembership(userId);
        boolean freeDelivery = request.deliveryEligible() && membership.tier().benefits().stream()
                .anyMatch(benefit -> benefit.type() == BenefitType.FREE_DELIVERY);
        int discountPercent = request.selectedItemsEligible()
                ? membership.tier().benefits().stream()
                        .filter(benefit -> benefit.type() == BenefitType.EXTRA_DISCOUNT_PERCENT)
                        .mapToInt(Benefit::value)
                        .max()
                        .orElse(0)
                : 0;
        BigDecimal discountAmount = request.orderValue()
                .multiply(BigDecimal.valueOf(discountPercent))
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
        BigDecimal payableOrderValue = request.orderValue()
                .subtract(discountAmount)
                .setScale(2, RoundingMode.HALF_UP);

        return new CheckoutBenefitsResponse(
                userId,
                membership.tier().id(),
                freeDelivery,
                discountPercent,
                discountAmount,
                payableOrderValue,
                membership.tier().benefits()
        );
    }
}
