# FirstClub Membership Program

Spring Boot backend for a configurable membership program with plans, tiers, tier-based benefits, subscription lifecycle actions, and rule-based tier evaluation.

## What is included

- Monthly, quarterly, and yearly plans seeded at startup.
- Silver, Gold, and Platinum tiers with configurable criteria and benefits.
- APIs to list plans and tiers, subscribe, upgrade or downgrade tier, cancel, and fetch current membership.
- Tier evaluation based on monthly order count, monthly order value, and cohort membership.
- Per-user locking plus optimistic version checks around membership mutation paths.
- Focused tests for subscription lifecycle, tier evaluation, and concurrent subscription attempts.

## Run

With Maven:

```powershell
mvn spring-boot:run
```

Without Maven on PATH, this workspace includes a local-cache helper:

```powershell
.\build.ps1 run
```

The service starts on `http://localhost:8080`.

## Test

With Maven:

```powershell
mvn test
```

Or using the local-cache helper:

```powershell
.\build.ps1 test
```

## Demo APIs

```powershell
Invoke-RestMethod http://localhost:8080/api/plans
Invoke-RestMethod http://localhost:8080/api/tiers
```

```powershell
Invoke-RestMethod -Method Post http://localhost:8080/api/memberships/subscribe `
  -ContentType 'application/json' `
  -Body '{"userId":"user-101","planId":"monthly","tierId":"silver"}'
```

```powershell
Invoke-RestMethod -Method Post http://localhost:8080/api/memberships/user-101/change-tier `
  -ContentType 'application/json' `
  -Body '{"tierId":"gold"}'
```

```powershell
Invoke-RestMethod http://localhost:8080/api/memberships/user-101
```

```powershell
Invoke-RestMethod -Method Post http://localhost:8080/api/memberships/user-101/evaluate-tier `
  -ContentType 'application/json' `
  -Body '{"monthlyOrderCount":12,"monthlyOrderValue":20000,"cohorts":["vip"]}'
```

```powershell
Invoke-RestMethod -Method Post http://localhost:8080/api/memberships/user-101/cancel
```

## Extension points

- Replace `UserMembershipRepository` with a database repository and keep service contracts unchanged.
- Add payment orchestration before `saveNew` in `MembershipService.subscribe`.
- Add new `BenefitType` values without changing subscription lifecycle logic.
- Add new tier criteria by expanding `TierCriteria` and `TierEvaluationService`.
