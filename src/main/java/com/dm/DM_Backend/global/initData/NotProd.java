package com.dm.DM_Backend.global.initData;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!prod")
public class NotProd {
    @Bean
    public ApplicationRunner applicationRunner(NotProdService notProdService) {
        return args -> notProdService.initDummyData();
    }
}
