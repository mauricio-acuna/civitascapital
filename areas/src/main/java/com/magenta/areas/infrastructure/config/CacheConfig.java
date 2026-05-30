package com.magenta.areas.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${magenta.cache.zone-ttl-seconds:600}")
    private long zoneTtlSeconds;

    @Value("${magenta.cache.compare-ttl-seconds:3600}")
    private long compareTtlSeconds;

    /** Cache Caffeine local — para getZone y getAncestors (TTL 10 min). */
    @Bean
    @Primary
    public CacheManager caffeineCacheManager() {
        CaffeineCache zones = new CaffeineCache("zones",
                Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(zoneTtlSeconds))
                        .maximumSize(10_000).build());
        CaffeineCache ancestors = new CaffeineCache("zoneAncestors",
                Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(zoneTtlSeconds))
                        .maximumSize(10_000).build());

        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(zones, ancestors));
        return manager;
    }

    /** Cache Redis — para compare (TTL 1 h). */
    @Bean
    public CacheManager redisCacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(compareTtlSeconds))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .withCacheConfiguration("zoneComparisons", config)
                .build();
    }
}
