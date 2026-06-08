package com.firstclub.membership.service;

import com.firstclub.membership.domain.Benefit;
import com.firstclub.membership.domain.BenefitType;
import com.firstclub.membership.domain.CohortPolicy;
import com.firstclub.membership.domain.MembershipPlan;
import com.firstclub.membership.domain.MembershipStatus;
import com.firstclub.membership.domain.MembershipTier;
import com.firstclub.membership.domain.PlanType;
import com.firstclub.membership.domain.TierCriteria;
import com.firstclub.membership.domain.UserMembership;
import com.firstclub.membership.exception.ConflictException;
import com.firstclub.membership.repository.CatalogRepository;
import com.firstclub.membership.repository.UserMembershipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MembershipServiceTest {
    private MembershipService service;

    @BeforeEach
    void setUp() {
        CatalogRepository catalog = new CatalogRepository();
        catalog.savePlan(new MembershipPlan("monthly", PlanType.MONTHLY, new BigDecimal("199.00"), 1, "INR"));
        catalog.saveTier(new MembershipTier(
                "silver",
                "Silver",
                1,
                new TierCriteria(0, BigDecimal.ZERO, new CohortPolicy(Set.of())),
                List.of(new Benefit(BenefitType.FREE_DELIVERY, "Free delivery", 0))
        ));
        catalog.saveTier(new MembershipTier(
                "gold",
                "Gold",
                2,
                new TierCriteria(5, new BigDecimal("5000"), new CohortPolicy(Set.of("power-shopper"))),
                List.of(new Benefit(BenefitType.EXTRA_DISCOUNT_PERCENT, "Discount", 10))
        ));
        service = new MembershipService(
                catalog,
                new UserMembershipRepository(),
                Clock.fixed(Instant.parse("2026-06-08T08:00:00Z"), ZoneOffset.UTC)
        );
    }

    @Test
    void subscribesUserWithExpectedExpiry() {
        UserMembership membership = service.subscribe("user-1", "monthly", "silver");

        assertThat(membership.status()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(membership.expiresAt()).isEqualTo(Instant.parse("2026-07-08T08:00:00Z"));
        assertThat(membership.tier().id()).isEqualTo("silver");
    }

    @Test
    void rejectsDuplicateActiveSubscription() {
        service.subscribe("user-1", "monthly", "silver");

        assertThatThrownBy(() -> service.subscribe("user-1", "monthly", "silver"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("active membership");
    }

    @Test
    void changesTierAndIncrementsVersion() {
        service.subscribe("user-1", "monthly", "silver");

        UserMembership upgraded = service.changeTier("user-1", "gold");

        assertThat(upgraded.tier().id()).isEqualTo("gold");
        assertThat(upgraded.version()).isEqualTo(1);
    }

    @Test
    void serializesConcurrentSubscriptionsPerUser() throws InterruptedException {
        int attempts = 8;
        CountDownLatch ready = new CountDownLatch(attempts);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger conflicts = new AtomicInteger();
        var executor = Executors.newFixedThreadPool(attempts);

        for (int i = 0; i < attempts; i++) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    service.subscribe("user-1", "monthly", "silver");
                    success.incrementAndGet();
                } catch (ConflictException exception) {
                    conflicts.incrementAndGet();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        ready.await();
        start.countDown();
        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.sleep(10);
        }

        assertThat(success.get()).isEqualTo(1);
        assertThat(conflicts.get()).isEqualTo(attempts - 1);
    }
}
