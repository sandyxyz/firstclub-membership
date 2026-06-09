package com.firstclub.membership.service;

import com.firstclub.membership.config.MembershipCatalogProperties;
import com.firstclub.membership.domain.Benefit;
import com.firstclub.membership.domain.CohortPolicy;
import com.firstclub.membership.domain.MembershipPlan;
import com.firstclub.membership.domain.MembershipTier;
import com.firstclub.membership.domain.TierCriteria;
import com.firstclub.membership.repository.CatalogRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CatalogSeeder implements CommandLineRunner {
    private final CatalogRepository catalogRepository;
    private final MembershipCatalogProperties properties;

    public CatalogSeeder(CatalogRepository catalogRepository, MembershipCatalogProperties properties) {
        this.catalogRepository = catalogRepository;
        this.properties = properties;
    }

    @Override
    public void run(String... args) {
        properties.plans().forEach(plan -> catalogRepository.savePlan(new MembershipPlan(
                plan.id(),
                plan.type(),
                plan.price(),
                plan.durationMonths(),
                plan.currency()
        )));

        properties.tiers().forEach(tier -> catalogRepository.saveTier(new MembershipTier(
                tier.id(),
                tier.name(),
                tier.rank(),
                new TierCriteria(
                        tier.criteria().minMonthlyOrders(),
                        tier.criteria().minMonthlyOrderValue(),
                        new CohortPolicy(tier.criteria().allowedCohorts() == null
                                ? Set.of()
                                : Set.copyOf(tier.criteria().allowedCohorts()))
                ),
                tier.benefits().stream()
                        .map(benefit -> new Benefit(benefit.type(), benefit.description(), benefit.value()))
                        .toList()
        )));
    }
}
