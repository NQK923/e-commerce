package com.learnfirebase.ecommerce.inventory.infrastructure.redis;

import java.util.List;
import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.inventory.application.port.out.InventoryRedisScriptPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryRedisScriptAdapter implements InventoryRedisScriptPort {
    private static final String AVAILABLE_KEY = "inventory:%s:%s:available";
    private static final String RESERVED_KEY = "inventory:%s:%s:reserved";

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean executeAtomicReserve(String inventoryId, Map<String, Integer> reservations) {
        String scriptText =
            "local availKey = KEYS[1]; " +
            "local delta = tonumber(ARGV[1]); " +
            "local current = tonumber(redis.call('get', availKey) or '0'); " +
            "if current + delta < 0 then return -1 end; " +
            "redis.call('incrby', availKey, delta); " +
            "return redis.call('get', availKey);";

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(scriptText);
        script.setResultType(Long.class);

        for (Map.Entry<String, Integer> entry : reservations.entrySet()) {
            String availKey = availableKey(inventoryId, entry.getKey());
            Long result = redisTemplate.execute(script, List.of(availKey), String.valueOf(entry.getValue()));
            if (result == null || result < 0) {
                log.warn("Inventory update failed for {} delta {}", entry.getKey(), entry.getValue());
                return false;
            }
        }
        return true;
    }

    @Override
    public int getAvailable(String inventoryId, String productId) {
        String value = redisTemplate.opsForValue().get(availableKey(inventoryId, productId));
        return value == null ? 0 : Integer.parseInt(value);
    }

    @Override
    public int getReserved(String inventoryId, String productId) {
        String value = redisTemplate.opsForValue().get(reservedKey(inventoryId, productId));
        return value == null ? 0 : Integer.parseInt(value);
    }

    private String availableKey(String inventoryId, String productId) {
        return String.format(AVAILABLE_KEY, inventoryId, productId);
    }

    private String reservedKey(String inventoryId, String productId) {
        return String.format(RESERVED_KEY, inventoryId, productId);
    }
}
