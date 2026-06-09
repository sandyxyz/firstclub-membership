package com.firstclub.membership.service;

import com.firstclub.membership.domain.MembershipTier;
import com.firstclub.membership.domain.TierCriteria;
import com.firstclub.membership.dto.TierEvaluationRequest;
import com.firstclub.membership.dto.TierEvaluationResponse;
import com.firstclub.membership.exception.ValidationException;
import com.firstclub.membership.repository.CatalogRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Set;

@Service
public class TierEvaluationService {
    private final CatalogRepository catalogRepository;

    public TierEvaluationService(CatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    public TierEvaluationResponse evaluate(TierEvaluationRequest request) {
        if (request == null) {
            throw new ValidationException("tier evaluation request is required");
        }
        BigDecimal orderValue = request.monthlyOrderValue() == null ? BigDecimal.ZERO : request.monthlyOrderValue();
        if (request.monthlyOrderCount() < 0 || orderValue.signum() < 0) {
            throw new ValidationException("monthly order count and value cannot be negative");
        }
        Set<String> cohorts = request.cohorts() == null ? Set.of() : request.cohorts();

        MembershipTier tier = catalogRepository.findTiers().stream()
                .filter(candidate -> qualifies(candidate.criteria(), request.monthlyOrderCount(), orderValue, cohorts))
                .max(Comparator.comparingInt(MembershipTier::rank))
                .orElseThrow(() -> new ValidationException("No configured tier matches the provided activity"));

        return new TierEvaluationResponse(
                tier,
                "Matched " + tier.name() + " using monthly order count, monthly order value, and cohort policy"
        );
    }

    private boolean qualifies(TierCriteria criteria, int orderCount, BigDecimal orderValue, Set<String> cohorts) {
        return orderCount >= criteria.minMonthlyOrders()
                && orderValue.compareTo(criteria.minMonthlyOrderValue()) >= 0
                && criteria.cohortPolicy().matches(cohorts);
    }
}
