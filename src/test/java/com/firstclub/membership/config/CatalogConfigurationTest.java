package com.firstclub.membership.config;

import com.firstclub.membership.repository.CatalogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CatalogConfigurationTest {
    @Autowired
    private CatalogRepository catalogRepository;

    @Test
    void loadsPlansAndTiersFromApplicationYaml() {
        assertThat(catalogRepository.findPlans()).hasSize(3);
        assertThat(catalogRepository.findTiers())
                .extracting("id")
                .containsExactly("silver", "gold", "platinum");
        assertThat(catalogRepository.getTier("platinum").benefits()).hasSize(5);
    }
}
