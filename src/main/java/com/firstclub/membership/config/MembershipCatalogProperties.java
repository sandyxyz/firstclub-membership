package com.firstclub.membership.config;

import com.firstclub.membership.domain.BenefitType;
import com.firstclub.membership.domain.PlanType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@ConfigurationProperties(prefix = "membership.catalog")
public record MembershipCatalogProperties(List<PlanConfig> plans, List<TierConfig> tiers) {
    public record PlanConfig(
            String id,
            PlanType type,
            BigDecimal price,
            int durationMonths,
            String currency
    ) {
    }

    public record TierConfig(
            String id,
            String name,
            int rank,
            CriteriaConfig criteria,
            List<BenefitConfig> benefits
    ) {
    }

    public record CriteriaConfig(
            int minMonthlyOrders,
            BigDecimal minMonthlyOrderValue,
            Set<String> allowedCohorts
    ) {
    }

    public record BenefitConfig(BenefitType type, String description, int value) {
    }
}
