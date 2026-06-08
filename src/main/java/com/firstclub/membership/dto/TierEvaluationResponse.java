package com.firstclub.membership.dto;

import com.firstclub.membership.domain.MembershipTier;

public record TierEvaluationResponse(MembershipTier eligibleTier, String reason) {
}
