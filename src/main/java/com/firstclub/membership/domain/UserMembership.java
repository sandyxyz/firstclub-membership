package com.firstclub.membership.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.time.Instant;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class UserMembership {
    private final String userId;
    private final MembershipPlan plan;
    private final MembershipTier tier;
    private final Instant subscribedAt;
    private final Instant expiresAt;
    private final MembershipStatus status;
    private final long version;

    public UserMembership(
            String userId,
            MembershipPlan plan,
            MembershipTier tier,
            Instant subscribedAt,
            Instant expiresAt,
            MembershipStatus status,
            long version
    ) {
        this.userId = userId;
        this.plan = plan;
        this.tier = tier;
        this.subscribedAt = subscribedAt;
        this.expiresAt = expiresAt;
        this.status = status;
        this.version = version;
    }

    public String userId() {
        return userId;
    }

    public MembershipPlan plan() {
        return plan;
    }

    public MembershipTier tier() {
        return tier;
    }

    public Instant subscribedAt() {
        return subscribedAt;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public MembershipStatus status() {
        return status;
    }

    public long version() {
        return version;
    }

    public boolean isActive(Instant now) {
        return status == MembershipStatus.ACTIVE && expiresAt.isAfter(now);
    }

    public UserMembership withTier(MembershipTier nextTier) {
        return new UserMembership(userId, plan, nextTier, subscribedAt, expiresAt, status, version + 1);
    }

    public UserMembership cancelled() {
        return new UserMembership(userId, plan, tier, subscribedAt, expiresAt, MembershipStatus.CANCELLED, version + 1);
    }
}
