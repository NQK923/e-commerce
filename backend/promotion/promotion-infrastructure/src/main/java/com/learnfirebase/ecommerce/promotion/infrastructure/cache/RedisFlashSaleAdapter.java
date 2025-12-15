package com.learnfirebase.ecommerce.promotion.infrastructure.cache;

import java.util.Collections;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.promotion.application.port.out.FlashSaleCachePort;
import com.learnfirebase.ecommerce.promotion.domain.model.FlashSaleId;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisFlashSaleAdapter implements FlashSaleCachePort {

    private final StringRedisTemplate redisTemplate;
    
    private static final String STOCK_KEY_PREFIX = "flashsale:%s:stock";

    @Override
    public void setStock(FlashSaleId id, int quantity) {
        String key = String.format(STOCK_KEY_PREFIX, id.getValue());
        redisTemplate.opsForValue().set(key, String.valueOf(quantity));
    }

    @Override
    public boolean decrementStock(FlashSaleId id, int quantity) {
        String key = String.format(STOCK_KEY_PREFIX, id.getValue());
        
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
    public Integer getStock(FlashSaleId id) {
        String key = String.format(STOCK_KEY_PREFIX, id.getValue());
        String val = redisTemplate.opsForValue().get(key);
        return val != null ? Integer.parseInt(val) : null;
    }
}
