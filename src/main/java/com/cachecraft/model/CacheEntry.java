package com.cachecraft.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CacheEntry {
    private String key;
    private String value;
    private LocalDateTime timestamp;
    private int frequencyCount;
}