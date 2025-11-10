package com.cachecraft.service;

import com.cachecraft.metrics.MetricsCollector;
import com.cachecraft.model.CacheEntry;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class CacheManagerService {

    private static final Logger logger = LoggerFactory.getLogger(CacheManagerService.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final MetricsCollector metricsCollector;
    
    // Caffeine cache
    private Cache<String, CacheEntry> caffeineCache;
    
    // Simulated database
    private Map<String, String> database;

    @Autowired
    public CacheManagerService(RedisTemplate<String, Object> redisTemplate, MetricsCollector metricsCollector) {
        this.redisTemplate = redisTemplate;
        this.metricsCollector = metricsCollector;
    }

    @PostConstruct
    public void init() {
        // Initialize Caffeine cache
        this.caffeineCache = Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(500)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
        
        // Initialize simulated database
        this.database = new HashMap<>();
        this.database.put("key1", "value1");
        this.database.put("key2", "value2");
        this.database.put("key3", "value3");
    }

    public CacheEntry get(String key) {
        // 1. Check Caffeine cache first
        CacheEntry entry = caffeineCache.getIfPresent(key);
        if (entry != null) {
            // Cache hit in Caffeine
            metricsCollector.recordHit();
            entry.setFrequencyCount(entry.getFrequencyCount() + 1);
            caffeineCache.put(key, entry);
            logger.info("Cache hit in Caffeine for key: {}", key);
            return entry;
        }

        // 2. Check Redis cache
        try {
            String value = (String) redisTemplate.opsForValue().get(key);
            if (value != null) {
                // Cache hit in Redis
                metricsCollector.recordHit();
                entry = new CacheEntry(key, value, LocalDateTime.now(), 1);
                // Update both caches
                caffeineCache.put(key, entry);
                logger.info("Cache hit in Redis for key: {}", key);
                return entry;
            }
        } catch (Exception e) {
            logger.warn("Redis is not available, skipping Redis cache check", e);
        }

        // 3. Cache miss - fetch from database
        metricsCollector.recordMiss();
        String dbValue = database.get(key);
        if (dbValue != null) {
            entry = new CacheEntry(key, dbValue, LocalDateTime.now(), 1);
            // Store in both caches
            caffeineCache.put(key, entry);
            try {
                redisTemplate.opsForValue().set(key, dbValue);
            } catch (Exception e) {
                logger.warn("Redis is not available, skipping Redis cache update", e);
            }
            logger.info("Cache miss, fetched from database for key: {}", key);
            return entry;
        }

        // Not found anywhere
        logger.info("Key not found in any data source: {}", key);
        return null;
    }

    public void put(String key, String value) {
        CacheEntry entry = new CacheEntry(key, value, LocalDateTime.now(), 0);
        // Store in both caches
        caffeineCache.put(key, entry);
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            logger.warn("Redis is not available, skipping Redis cache update", e);
        }
        logger.info("Stored value in cache for key: {}", key);
    }

    public void evict(String key) {
        // Remove from both caches
        caffeineCache.invalidate(key);
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            logger.warn("Redis is not available, skipping Redis cache eviction", e);
        }
        metricsCollector.recordEviction();
        logger.info("Evicted key from cache: {}", key);
    }

    public void clearAll() {
        // Clear both caches
        caffeineCache.invalidateAll();
        try {
            redisTemplate.getConnectionFactory().getConnection().flushAll();
        } catch (Exception e) {
            logger.warn("Redis is not available, skipping Redis cache clear", e);
        }
        logger.info("Cleared all cache entries");
    }

    public long getCaffeineCacheSize() {
        return caffeineCache.estimatedSize();
    }
}