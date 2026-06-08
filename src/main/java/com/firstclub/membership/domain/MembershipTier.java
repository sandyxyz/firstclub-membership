package com.firstclub.membership.domain;

import java.util.List;

public record MembershipTier(
        String id,
        String name,
        int rank,
        TierCriteria criteria,
        List<Benefit> benefits
) {
}
