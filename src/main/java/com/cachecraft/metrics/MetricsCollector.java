package com.cachecraft.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

@Component
public class MetricsCollector {

    private final MeterRegistry meterRegistry;
    
    // Cache metrics
    private final Counter cacheHits;
    private final Counter cacheMisses;
    private final Counter cacheEvictions;
    
    // Timing metrics
    private final Timer cacheOperationTimer;
    
    // Memory usage simulation
    private final AtomicLong memoryUsage;

    @Autowired
    public MetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.memoryUsage = new AtomicLong(0);
        
        // Initialize counters
        this.cacheHits = Counter.builder("cache.hits")
                .description("Number of cache hits")
                .register(meterRegistry);
        
        this.cacheMisses = Counter.builder("cache.misses")
                .description("Number of cache misses")
                .register(meterRegistry);
        
        this.cacheEvictions = Counter.builder("cache.evictions")
                .description("Number of cache evictions")
                .register(meterRegistry);
        
        // Initialize timer
        this.cacheOperationTimer = Timer.builder("cache.operations")
                .description("Timing of cache operations")
                .register(meterRegistry);
        
        // Register gauge for memory usage using the registry method
        meterRegistry.gauge("cache.memory.usage", memoryUsage, AtomicLong::doubleValue);
    }

    @PostConstruct
    public void init() {
        // Set initial memory usage
        memoryUsage.set(1024); // Initial memory in KB
    }

    public void recordHit() {
        cacheHits.increment();
        memoryUsage.incrementAndGet(); // Simple simulation
    }

    public void recordMiss() {
        cacheMisses.increment();
    }

    public void recordEviction() {
        cacheEvictions.increment();
        memoryUsage.decrementAndGet(); // Simple simulation
    }

    public <T> T recordOperationTime(Supplier<T> operation) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            return operation.get();
        } finally {
            sample.stop(cacheOperationTimer);
        }
    }

    public void recordOperationTime(Runnable operation) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            operation.run();
        } finally {
            sample.stop(cacheOperationTimer);
        }
    }

    // Getters for metrics
    public double getCacheHitCount() {
        return cacheHits.count();
    }

    public double getCacheMissCount() {
        return cacheMisses.count();
    }

    public double getCacheEvictionCount() {
        return cacheEvictions.count();
    }

    public double getMemoryUsage() {
        return memoryUsage.get();
    }
}