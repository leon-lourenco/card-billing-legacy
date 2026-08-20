package com.cardbilling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CardBillingLegacyApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardBillingLegacyApplication.class, args);
    }
}
