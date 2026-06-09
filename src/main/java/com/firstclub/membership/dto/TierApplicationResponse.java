package com.firstclub.membership.dto;

import com.firstclub.membership.domain.MembershipTier;
import com.firstclub.membership.domain.UserMembership;

public record TierApplicationResponse(
        UserMembership membership,
        MembershipTier evaluatedTier,
        boolean changed,
        String reason
) {
}
