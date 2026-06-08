package com.firstclub.membership.domain;

import java.math.BigDecimal;

public record TierCriteria(int minMonthlyOrders, BigDecimal minMonthlyOrderValue, CohortPolicy cohortPolicy) {
}
