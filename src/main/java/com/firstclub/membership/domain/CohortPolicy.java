package com.firstclub.membership.domain;

import java.util.Set;

public record CohortPolicy(Set<String> allowedCohorts) {
    public boolean matches(Set<String> userCohorts) {
        return allowedCohorts == null || allowedCohorts.isEmpty() || userCohorts.stream().anyMatch(allowedCohorts::contains);
    }
}
