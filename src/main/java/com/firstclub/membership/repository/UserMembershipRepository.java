package com.firstclub.membership.repository;

import com.firstclub.membership.domain.UserMembership;
import com.firstclub.membership.exception.ConflictException;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class UserMembershipRepository {
    private final ConcurrentMap<String, UserMembership> memberships = new ConcurrentHashMap<>();

    public Optional<UserMembership> findByUserId(String userId) {
        return Optional.ofNullable(memberships.get(userId));
    }

    public UserMembership saveNew(UserMembership membership) {
        UserMembership existing = memberships.putIfAbsent(membership.userId(), membership);
        if (existing != null) {
            throw new ConflictException("User already has a membership record");
        }
        return membership;
    }

    public UserMembership replace(UserMembership expected, UserMembership replacement) {
        boolean replaced = memberships.replace(expected.userId(), expected, replacement);
        if (!replaced) {
            throw new ConflictException("Membership was modified concurrently");
        }
        return replacement;
    }

    public UserMembership save(UserMembership membership) {
        UserMembership current = memberships.get(membership.userId());
        if (current == null) {
            memberships.put(membership.userId(), membership);
            return membership;
        }
        if (current.version() + 1 != membership.version()) {
            throw new ConflictException("Membership version conflict for user " + membership.userId());
        }
        boolean replaced = memberships.replace(membership.userId(), current, membership);
        if (!replaced) {
            throw new ConflictException("Membership was modified concurrently");
        }
        return membership;
    }
}
