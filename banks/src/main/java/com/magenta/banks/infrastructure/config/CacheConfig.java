package com.magenta.banks.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Value("${magenta.cache.products-ttl-minutes:30}")
    private int productsTtlMinutes;

    public static final String CACHE_PRODUCTS  = "products";
    public static final String CACHE_BANKS     = "banks";

    /**
     * Caffeine para productos (TTL 30 min) — datos que cambian poco.
     * Redis se usa para simulaciones determinísticas (configurado por Spring Boot auto-config).
     */
    @Bean
    @Primary
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(CACHE_PRODUCTS, CACHE_BANKS);
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(productsTtlMinutes, TimeUnit.MINUTES)
                .maximumSize(1000));
        return manager;
    }
}
