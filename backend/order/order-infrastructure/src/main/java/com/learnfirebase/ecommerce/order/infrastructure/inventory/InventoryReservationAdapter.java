package com.learnfirebase.ecommerce.order.infrastructure.inventory;

import java.util.Collections;
import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.order.application.port.out.InventoryReservationPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InventoryReservationAdapter implements InventoryReservationPort {

    private final StringRedisTemplate redisTemplate;
    
    private static final String FLASH_SALE_STOCK_KEY_PREFIX = "flashsale:%s:stock";

    @Override
    public boolean reserve(String orderId, Map<String, Integer> productQuantities) {
        // Placeholder implementation; integrate with Redis or inventory service
        return true;
    }

    @Override
    public boolean reserveFlashSale(String orderId, String flashSaleId, int quantity) {
        String key = String.format(FLASH_SALE_STOCK_KEY_PREFIX, flashSaleId);
        
        String scriptText = 
            "local current = tonumber(redis.call('get', KEYS[1])); " +
            "if current ~= nil and current >= tonumber(ARGV[1]) then " +
            "   return redis.call('decrby', KEYS[1], ARGV[1]); " +
            "else " +
            "   return -1; " +
            "end";
            
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(scriptText);
        script.setResultType(Long.class);
        
                Long result = redisTemplate.execute(script, Collections.singletonList(key), String.valueOf(quantity));
        
                
        
                return result != null && result >= 0;
        
            }
        
        
        
            @Override
        
            public void releaseFlashSale(String flashSaleId, int quantity) {
        
                String key = String.format(FLASH_SALE_STOCK_KEY_PREFIX, flashSaleId);
        
                redisTemplate.opsForValue().increment(key, quantity);
        
            }
        
        }
        
        