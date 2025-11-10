package com.cachecraft.controller;

import com.cachecraft.metrics.MetricsCollector;
import com.cachecraft.model.CacheEntry;
import com.cachecraft.service.CacheManagerService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CacheControllerTest {

    private CacheController cacheController;

    @Mock
    private CacheManagerService cacheManagerService;

    private MetricsCollector metricsCollector;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        metricsCollector = new MetricsCollector(new SimpleMeterRegistry());
        cacheController = new CacheController(cacheManagerService, metricsCollector);
    }

    @Test
    void testGetDataFound() {
        // Test getting data that exists
        CacheEntry entry = new CacheEntry("testKey", "testValue", java.time.LocalDateTime.now(), 1);
        when(cacheManagerService.get("testKey")).thenReturn(entry);
        
        ResponseEntity<CacheEntry> response = cacheController.getData("testKey");
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("testKey", response.getBody().getKey());
    }

    @Test
    void testGetDataNotFound() {
        // Test getting data that doesn't exist
        when(cacheManagerService.get("nonexistent")).thenReturn(null);
        
        ResponseEntity<CacheEntry> response = cacheController.getData("nonexistent");
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testGetCacheStats() {
        // Test getting cache statistics
        ResponseEntity<Map<String, Object>> response = cacheController.getCacheStats();
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("hits"));
        assertTrue(response.getBody().containsKey("misses"));
        assertTrue(response.getBody().containsKey("evictions"));
        assertTrue(response.getBody().containsKey("memoryUsage"));
    }

    @Test
    void testClearCache() {
        // Test clearing cache
        ResponseEntity<String> response = cacheController.clearCache();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("All cache cleared successfully", response.getBody());
        verify(cacheManagerService, times(1)).clearAll();
    }
}