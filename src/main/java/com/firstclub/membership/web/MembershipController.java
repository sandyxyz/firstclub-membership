package com.firstclub.membership.web;

import com.firstclub.membership.domain.UserMembership;
import com.firstclub.membership.dto.ChangeTierRequest;
import com.firstclub.membership.dto.SubscriptionRequest;
import com.firstclub.membership.dto.TierEvaluationRequest;
import com.firstclub.membership.dto.TierEvaluationResponse;
import com.firstclub.membership.exception.ValidationException;
import com.firstclub.membership.service.MembershipService;
import com.firstclub.membership.service.TierEvaluationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/memberships")
public class MembershipController {
    private final MembershipService membershipService;
    private final TierEvaluationService tierEvaluationService;

    public MembershipController(MembershipService membershipService, TierEvaluationService tierEvaluationService) {
        this.membershipService = membershipService;
        this.tierEvaluationService = tierEvaluationService;
    }

    @PostMapping("/subscribe")
    @ResponseStatus(HttpStatus.CREATED)
    public UserMembership subscribe(@RequestBody SubscriptionRequest request) {
        if (request == null) {
            throw new ValidationException("subscription request is required");
        }
        return membershipService.subscribe(request.userId(), request.planId(), request.tierId());
    }

    @GetMapping("/{userId}")
    public UserMembership currentMembership(@PathVariable String userId) {
        return membershipService.getMembership(userId);
    }

    @PostMapping("/{userId}/change-tier")
    public UserMembership changeTier(@PathVariable String userId, @RequestBody ChangeTierRequest request) {
        if (request == null || request.tierId() == null || request.tierId().isBlank()) {
            throw new ValidationException("tierId is required");
        }
        return membershipService.changeTier(userId, request.tierId());
    }

    @PostMapping("/{userId}/cancel")
    public UserMembership cancel(@PathVariable String userId) {
        return membershipService.cancel(userId);
    }

    @PostMapping("/{userId}/evaluate-tier")
    public TierEvaluationResponse evaluateTier(@PathVariable String userId, @RequestBody TierEvaluationRequest request) {
        return tierEvaluationService.evaluate(request);
    }
}
