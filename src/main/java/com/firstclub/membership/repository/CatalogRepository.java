package com.firstclub.membership.repository;

import com.firstclub.membership.domain.MembershipPlan;
import com.firstclub.membership.domain.MembershipTier;
import com.firstclub.membership.exception.NotFoundException;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class CatalogRepository {
    private final Map<String, MembershipPlan> plans = new ConcurrentHashMap<>();
    private final Map<String, MembershipTier> tiers = new ConcurrentHashMap<>();

    public Collection<MembershipPlan> findPlans() {
        return plans.values().stream().sorted(Comparator.comparing(MembershipPlan::durationMonths)).toList();
    }

    public Collection<MembershipTier> findTiers() {
        return tiers.values().stream().sorted(Comparator.comparingInt(MembershipTier::rank)).toList();
    }

    public MembershipPlan getPlan(String planId) {
        MembershipPlan plan = plans.get(planId);
        if (plan == null) {
            throw new NotFoundException("Plan not found: " + planId);
        }
        return plan;
    }

    public MembershipTier getTier(String tierId) {
        MembershipTier tier = tiers.get(tierId);
        if (tier == null) {
            throw new NotFoundException("Tier not found: " + tierId);
        }
        return tier;
    }

    public void savePlan(MembershipPlan plan) {
        plans.put(plan.id(), plan);
    }

    public void saveTier(MembershipTier tier) {
        tiers.put(tier.id(), tier);
    }
}
