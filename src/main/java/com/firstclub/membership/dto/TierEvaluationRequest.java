package com.firstclub.membership.dto;

import java.math.BigDecimal;
import java.util.Set;

public record TierEvaluationRequest(int monthlyOrderCount, BigDecimal monthlyOrderValue, Set<String> cohorts) {
}
