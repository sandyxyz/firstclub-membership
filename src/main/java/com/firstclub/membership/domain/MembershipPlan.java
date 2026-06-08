package com.firstclub.membership.domain;

import java.math.BigDecimal;

public record MembershipPlan(String id, PlanType type, BigDecimal price, int durationMonths, String currency) {
}
