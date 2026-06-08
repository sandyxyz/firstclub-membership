package com.firstclub.membership.web;

import com.firstclub.membership.domain.MembershipPlan;
import com.firstclub.membership.domain.MembershipTier;
import com.firstclub.membership.repository.CatalogRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api")
public class CatalogController {
    private final CatalogRepository catalogRepository;

    public CatalogController(CatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    @GetMapping("/plans")
    public Collection<MembershipPlan> plans() {
        return catalogRepository.findPlans();
    }

    @GetMapping("/tiers")
    public Collection<MembershipTier> tiers() {
        return catalogRepository.findTiers();
    }
}
