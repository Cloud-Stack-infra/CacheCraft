package com.cachecraft.controller;

import com.cachecraft.metrics.MetricsCollector;
import com.cachecraft.model.CacheEntry;
import com.cachecraft.service.CacheManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CacheController {

    private final CacheManagerService cacheManagerService;
    private final MetricsCollector metricsCollector;

    @Autowired
    public CacheController(CacheManagerService cacheManagerService, MetricsCollector metricsCollector) {
        this.cacheManagerService = cacheManagerService;
        this.metricsCollector = metricsCollector;
    }

    @GetMapping("/data/{key}")
    public ResponseEntity<CacheEntry> getData(@PathVariable String key) {
        CacheEntry entry = cacheManagerService.get(key);
        if (entry != null) {
            return ResponseEntity.ok(entry);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/data")
    public ResponseEntity<String> addData(@RequestBody Map<String, String> requestData) {
        String key = requestData.get("key");
        String value = requestData.get("value");
        
        if (key == null || value == null) {
            return ResponseEntity.badRequest().body("Key and value are required");
        }
        
        cacheManagerService.put(key, value);
        return ResponseEntity.ok("Data added successfully for key: " + key);
    }

    @GetMapping("/cache/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("hits", metricsCollector.getCacheHitCount());
        stats.put("misses", metricsCollector.getCacheMissCount());
        stats.put("evictions", metricsCollector.getCacheEvictionCount());
        stats.put("memoryUsage", metricsCollector.getMemoryUsage());
        return ResponseEntity.ok(stats);
    }

    @PutMapping("/cache/config")
    public ResponseEntity<String> updateTTLConfig(@RequestBody Map<String, Object> config) {
        // In a real implementation, this would update the TTL policy
        // For now, we'll just return a success message
        return ResponseEntity.ok("TTL policy updated successfully");
    }

    @DeleteMapping("/cache/clear")
    public ResponseEntity<String> clearCache() {
        cacheManagerService.clearAll();
        return ResponseEntity.ok("All cache cleared successfully");
    }
}