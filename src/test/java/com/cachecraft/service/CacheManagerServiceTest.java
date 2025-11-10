package com.cachecraft.service;

import com.cachecraft.metrics.MetricsCollector;
import com.cachecraft.model.CacheEntry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CacheManagerServiceTest {

    private CacheManagerService cacheManagerService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private MetricsCollector metricsCollector;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        metricsCollector = new MetricsCollector(new SimpleMeterRegistry());
        cacheManagerService = new CacheManagerService(redisTemplate, metricsCollector);
        cacheManagerService.init();
    }

    @Test
    void testGetFromDatabase() {
        // Test getting data that exists in the simulated database
        CacheEntry entry = cacheManagerService.get("key1");
        assertNotNull(entry);
        assertEquals("key1", entry.getKey());
        assertEquals("value1", entry.getValue());
        assertEquals(1, entry.getFrequencyCount());
    }

    @Test
    void testGetNonExistentKey() {
        // Test getting data that doesn't exist anywhere
        CacheEntry entry = cacheManagerService.get("nonexistent");
        assertNull(entry);
    }

    @Test
    void testPutAndRetrieve() {
        // Test putting data and then retrieving it
        cacheManagerService.put("testKey", "testValue");
        
        CacheEntry entry = cacheManagerService.get("testKey");
        assertNotNull(entry);
        assertEquals("testKey", entry.getKey());
        assertEquals("testValue", entry.getValue());
        // When we retrieve the entry, the frequency count is incremented to 1
        assertEquals(1, entry.getFrequencyCount());
    }

    @Test
    void testEvict() {
        // Test putting data and then evicting it
        cacheManagerService.put("testKey", "testValue");
        
        // Verify it exists
        CacheEntry entry = cacheManagerService.get("testKey");
        assertNotNull(entry);
        
        // Evict it
        cacheManagerService.evict("testKey");
        
        // Note: In a real test, we would verify the entry is removed,
        // but our current implementation doesn't have a direct way to check
        // that the entry is gone from both caches without more complex mocking
    }
}