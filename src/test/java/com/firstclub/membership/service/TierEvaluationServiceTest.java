package com.firstclub.membership.service;

import com.firstclub.membership.domain.Benefit;
import com.firstclub.membership.domain.BenefitType;
import com.firstclub.membership.domain.CohortPolicy;
import com.firstclub.membership.domain.MembershipTier;
import com.firstclub.membership.domain.TierCriteria;
import com.firstclub.membership.dto.TierEvaluationRequest;
import com.firstclub.membership.repository.CatalogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TierEvaluationServiceTest {
    private TierEvaluationService service;

    @BeforeEach
    void setUp() {
        CatalogRepository catalog = new CatalogRepository();
        catalog.saveTier(new MembershipTier(
                "silver",
                "Silver",
                1,
                new TierCriteria(0, BigDecimal.ZERO, new CohortPolicy(Set.of())),
                List.of(new Benefit(BenefitType.FREE_DELIVERY, "Free delivery", 0))
        ));
        catalog.saveTier(new MembershipTier(
                "gold",
                "Gold",
                2,
                new TierCriteria(5, new BigDecimal("5000"), new CohortPolicy(Set.of("power-shopper"))),
                List.of(new Benefit(BenefitType.EXTRA_DISCOUNT_PERCENT, "Discount", 10))
        ));
        catalog.saveTier(new MembershipTier(
                "platinum",
                "Platinum",
                3,
                new TierCriteria(10, new BigDecimal("15000"), new CohortPolicy(Set.of("vip"))),
                List.of(new Benefit(BenefitType.PRIORITY_SUPPORT, "Priority support", 0))
        ));
        service = new TierEvaluationService(catalog);
    }

    @Test
    void returnsHighestQualifyingTier() {
        var response = service.evaluate(new TierEvaluationRequest(12, new BigDecimal("20000"), Set.of("vip")));

        assertThat(response.eligibleTier().id()).isEqualTo("platinum");
    }

    @Test
    void fallsBackToBaseTierWhenPremiumCohortDoesNotMatch() {
        var response = service.evaluate(new TierEvaluationRequest(12, new BigDecimal("20000"), Set.of("new-user")));

        assertThat(response.eligibleTier().id()).isEqualTo("silver");
    }
}
