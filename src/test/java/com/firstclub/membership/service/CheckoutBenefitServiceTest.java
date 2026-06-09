package com.firstclub.membership.service;

import com.firstclub.membership.domain.Benefit;
import com.firstclub.membership.domain.BenefitType;
import com.firstclub.membership.domain.CohortPolicy;
import com.firstclub.membership.domain.MembershipPlan;
import com.firstclub.membership.domain.MembershipTier;
import com.firstclub.membership.domain.PlanType;
import com.firstclub.membership.domain.TierCriteria;
import com.firstclub.membership.dto.CheckoutBenefitsRequest;
import com.firstclub.membership.repository.CatalogRepository;
import com.firstclub.membership.repository.UserMembershipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CheckoutBenefitServiceTest {
    private CheckoutBenefitService service;

    @BeforeEach
    void setUp() {
        CatalogRepository catalog = new CatalogRepository();
        catalog.savePlan(new MembershipPlan("monthly", PlanType.MONTHLY, new BigDecimal("199.00"), 1, "INR"));
        catalog.saveTier(new MembershipTier(
                "gold",
                "Gold",
                2,
                new TierCriteria(5, new BigDecimal("5000"), new CohortPolicy(Set.of("power-shopper"))),
                List.of(
                        new Benefit(BenefitType.FREE_DELIVERY, "Free delivery", 0),
                        new Benefit(BenefitType.EXTRA_DISCOUNT_PERCENT, "Discount", 10)
                )
        ));
        TierEvaluationService tierEvaluationService = new TierEvaluationService(catalog);
        MembershipService membershipService = new MembershipService(
                catalog,
                new UserMembershipRepository(),
                tierEvaluationService,
                Clock.fixed(Instant.parse("2026-06-08T08:00:00Z"), ZoneOffset.UTC)
        );
        membershipService.subscribe("user-1", "monthly", "gold");
        service = new CheckoutBenefitService(membershipService);
    }

    @Test
    void appliesDeliveryAndItemBenefitsWhenCheckoutIsEligible() {
        var response = service.calculate(
                "user-1",
                new CheckoutBenefitsRequest(new BigDecimal("2500.00"), true, true)
        );

        assertThat(response.freeDelivery()).isTrue();
        assertThat(response.discountPercent()).isEqualTo(10);
        assertThat(response.discountAmount()).isEqualByComparingTo("250.00");
        assertThat(response.payableOrderValue()).isEqualByComparingTo("2250.00");
    }

    @Test
    void doesNotApplyBenefitsToIneligibleCheckoutComponents() {
        var response = service.calculate(
                "user-1",
                new CheckoutBenefitsRequest(new BigDecimal("2500.00"), false, false)
        );

        assertThat(response.freeDelivery()).isFalse();
        assertThat(response.discountPercent()).isZero();
        assertThat(response.discountAmount()).isEqualByComparingTo("0.00");
        assertThat(response.payableOrderValue()).isEqualByComparingTo("2500.00");
    }
}
