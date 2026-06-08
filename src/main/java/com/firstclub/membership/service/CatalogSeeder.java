package com.firstclub.membership.service;

import com.firstclub.membership.domain.Benefit;
import com.firstclub.membership.domain.BenefitType;
import com.firstclub.membership.domain.CohortPolicy;
import com.firstclub.membership.domain.MembershipPlan;
import com.firstclub.membership.domain.MembershipTier;
import com.firstclub.membership.domain.PlanType;
import com.firstclub.membership.domain.TierCriteria;
import com.firstclub.membership.repository.CatalogRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Component
public class CatalogSeeder implements CommandLineRunner {
    private final CatalogRepository catalogRepository;

    public CatalogSeeder(CatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    @Override
    public void run(String... args) {
        catalogRepository.savePlan(new MembershipPlan("monthly", PlanType.MONTHLY, new BigDecimal("199.00"), 1, "INR"));
        catalogRepository.savePlan(new MembershipPlan("quarterly", PlanType.QUARTERLY, new BigDecimal("499.00"), 3, "INR"));
        catalogRepository.savePlan(new MembershipPlan("yearly", PlanType.YEARLY, new BigDecimal("1499.00"), 12, "INR"));

        catalogRepository.saveTier(new MembershipTier(
                "silver",
                "Silver",
                1,
                new TierCriteria(0, BigDecimal.ZERO, new CohortPolicy(Set.of())),
                List.of(
                        new Benefit(BenefitType.FREE_DELIVERY, "Free delivery on eligible orders", 0),
                        new Benefit(BenefitType.EXTRA_DISCOUNT_PERCENT, "Extra discount on selected categories", 5)
                )
        ));
        catalogRepository.saveTier(new MembershipTier(
                "gold",
                "Gold",
                2,
                new TierCriteria(5, new BigDecimal("5000"), new CohortPolicy(Set.of("power-shopper", "employee"))),
                List.of(
                        new Benefit(BenefitType.FREE_DELIVERY, "Free delivery on eligible orders", 0),
                        new Benefit(BenefitType.EXTRA_DISCOUNT_PERCENT, "Extra discount on selected categories", 10),
                        new Benefit(BenefitType.EARLY_SALE_ACCESS, "Early access to seasonal sales", 0)
                )
        ));
        catalogRepository.saveTier(new MembershipTier(
                "platinum",
                "Platinum",
                3,
                new TierCriteria(10, new BigDecimal("15000"), new CohortPolicy(Set.of("vip", "employee"))),
                List.of(
                        new Benefit(BenefitType.FREE_DELIVERY, "Free delivery with faster fulfilment", 0),
                        new Benefit(BenefitType.EXTRA_DISCOUNT_PERCENT, "Highest discount on selected categories", 15),
                        new Benefit(BenefitType.EXCLUSIVE_DEALS, "Access to exclusive member-only deals", 0),
                        new Benefit(BenefitType.PRIORITY_SUPPORT, "Priority support queue", 0),
                        new Benefit(BenefitType.EXCLUSIVE_COUPONS, "Monthly exclusive coupons", 0)
                )
        ));
    }
}
