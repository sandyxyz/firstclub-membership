package com.firstclub.membership;

import com.firstclub.membership.config.MembershipCatalogProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(MembershipCatalogProperties.class)
public class MembershipProgramApplication {
    public static void main(String[] args) {
        SpringApplication.run(MembershipProgramApplication.class, args);
    }
}
