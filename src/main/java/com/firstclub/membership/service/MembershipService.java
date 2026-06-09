package com.firstclub.membership.service;

import com.firstclub.membership.domain.MembershipPlan;
import com.firstclub.membership.domain.MembershipStatus;
import com.firstclub.membership.domain.MembershipTier;
import com.firstclub.membership.domain.UserMembership;
import com.firstclub.membership.dto.TierApplicationResponse;
import com.firstclub.membership.dto.TierEvaluationRequest;
import com.firstclub.membership.dto.TierEvaluationResponse;
import com.firstclub.membership.exception.ConflictException;
import com.firstclub.membership.exception.NotFoundException;
import com.firstclub.membership.exception.ValidationException;
import com.firstclub.membership.repository.CatalogRepository;
import com.firstclub.membership.repository.UserMembershipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class MembershipService {
    private final CatalogRepository catalogRepository;
    private final UserMembershipRepository membershipRepository;
    private final TierEvaluationService tierEvaluationService;
    private final Clock clock;
    private final Map<String, ReentrantLock> userLocks = new ConcurrentHashMap<>();

    @Autowired
    public MembershipService(
            CatalogRepository catalogRepository,
            UserMembershipRepository membershipRepository,
            TierEvaluationService tierEvaluationService
    ) {
        this(catalogRepository, membershipRepository, tierEvaluationService, Clock.systemUTC());
    }

    MembershipService(
            CatalogRepository catalogRepository,
            UserMembershipRepository membershipRepository,
            TierEvaluationService tierEvaluationService,
            Clock clock
    ) {
        this.catalogRepository = catalogRepository;
        this.membershipRepository = membershipRepository;
        this.tierEvaluationService = tierEvaluationService;
        this.clock = clock;
    }

    public UserMembership subscribe(String userId, String planId, String tierId) {
        validateUserId(userId);
        validateRequiredId(planId, "planId");
        validateRequiredId(tierId, "tierId");
        return withUserLock(userId, () -> {
            Instant now = clock.instant();
            UserMembership existing = membershipRepository.findByUserId(userId).orElse(null);
            if (existing != null && existing.isActive(now)) {
                throw new ConflictException("User already has an active membership");
            }
            MembershipPlan plan = catalogRepository.getPlan(planId);
            MembershipTier tier = catalogRepository.getTier(tierId);
            Instant expiresAt = now.atZone(ZoneOffset.UTC).plusMonths(plan.durationMonths()).toInstant();
            UserMembership membership = new UserMembership(
                    userId,
                    plan,
                    tier,
                    now,
                    expiresAt,
                    MembershipStatus.ACTIVE,
                    existing == null ? 0 : existing.version() + 1
            );
            return existing == null
                    ? membershipRepository.saveNew(membership)
                    : membershipRepository.replace(existing, membership);
        });
    }

    public UserMembership changeTier(String userId, String tierId) {
        validateUserId(userId);
        validateRequiredId(tierId, "tierId");
        return withUserLock(userId, () -> {
            UserMembership current = activeMembership(userId);
            MembershipTier nextTier = catalogRepository.getTier(tierId);
            if (Objects.equals(current.tier().id(), nextTier.id())) {
                throw new ValidationException("User is already on tier " + nextTier.name());
            }
            return membershipRepository.save(current.withTier(nextTier));
        });
    }

    public UserMembership cancel(String userId) {
        validateUserId(userId);
        return withUserLock(userId, () -> {
            UserMembership current = activeMembership(userId);
            return membershipRepository.save(current.cancelled());
        });
    }

    public TierApplicationResponse evaluateAndApplyTier(String userId, TierEvaluationRequest request) {
        validateUserId(userId);
        return withUserLock(userId, () -> {
            UserMembership current = activeMembership(userId);
            TierEvaluationResponse evaluation = tierEvaluationService.evaluate(request);
            MembershipTier evaluatedTier = evaluation.eligibleTier();
            if (Objects.equals(current.tier().id(), evaluatedTier.id())) {
                return new TierApplicationResponse(current, evaluatedTier, false, evaluation.reason());
            }
            UserMembership updated = membershipRepository.save(current.withTier(evaluatedTier));
            return new TierApplicationResponse(updated, evaluatedTier, true, evaluation.reason());
        });
    }

    public UserMembership getActiveMembership(String userId) {
        validateUserId(userId);
        return activeMembership(userId);
    }

    public UserMembership getMembership(String userId) {
        validateUserId(userId);
        return membershipRepository.findByUserId(userId)
                .map(this::expireIfNeeded)
                .orElseThrow(() -> new NotFoundException("Membership not found for user " + userId));
    }

    private UserMembership activeMembership(String userId) {
        UserMembership membership = getMembership(userId);
        if (!membership.isActive(clock.instant())) {
            throw new ConflictException("Membership is not active");
        }
        return membership;
    }

    private UserMembership expireIfNeeded(UserMembership membership) {
        if (membership.status() == MembershipStatus.ACTIVE && !membership.expiresAt().isAfter(clock.instant())) {
            UserMembership expired = new UserMembership(
                    membership.userId(),
                    membership.plan(),
                    membership.tier(),
                    membership.subscribedAt(),
                    membership.expiresAt(),
                    MembershipStatus.EXPIRED,
                    membership.version() + 1
            );
            return membershipRepository.save(expired);
        }
        return membership;
    }

    private void validateUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new ValidationException("userId is required");
        }
    }

    private void validateRequiredId(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " is required");
        }
    }

    private <T> T withUserLock(String userId, LockedOperation<T> operation) {
        ReentrantLock lock = userLocks.computeIfAbsent(userId, ignored -> new ReentrantLock());
        lock.lock();
        try {
            return operation.execute();
        } finally {
            lock.unlock();
        }
    }

    @FunctionalInterface
    private interface LockedOperation<T> {
        T execute();
    }
}
