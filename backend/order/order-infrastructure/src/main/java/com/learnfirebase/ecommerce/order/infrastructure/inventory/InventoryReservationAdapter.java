package com.learnfirebase.ecommerce.order.infrastructure.inventory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.learnfirebase.ecommerce.order.application.port.out.InventoryReservationPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryReservationAdapter implements InventoryReservationPort {

    private static final String INVENTORY_ID = "DEFAULT_INVENTORY";
    private static final String AVAILABLE_KEY = "inventory:%s:%s:available";
    private static final String RESERVED_KEY = "inventory:%s:%s:reserved";
    private static final String RESERVATION_HASH = "inventory:reservation:%s";
    private static final String RESERVATION_EXPIRATIONS = "inventory:reservation:expirations";
    private static final long RESERVATION_TTL_SECONDS = 15 * 60;
    private static final String FLASH_SALE_STOCK_KEY_PREFIX = "flashsale:%s:stock";

    private final StringRedisTemplate redisTemplate;
    private final com.learnfirebase.ecommerce.order.application.port.out.OrderRepository orderRepository;

    @Override
    public boolean reserve(String orderId, Map<String, Integer> productQuantities) {
        List<String> reservedKeys = new ArrayList<>();
        try {
            for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
                String productId = entry.getKey();
                int qty = entry.getValue();
                if (qty <= 0) {
                    continue;
                }
                boolean success = adjustStockAtomically(productId, qty * -1);
                if (!success) {
                    rollback(reservedKeys);
                    return false;
                }
                reservedKeys.add(productId + ":" + qty);
                redisTemplate.opsForHash().put(reservationKey(orderId), productId, String.valueOf(qty));
            }
            if (!reservedKeys.isEmpty()) {
                long expiresAt = Instant.now().getEpochSecond() + RESERVATION_TTL_SECONDS;
                redisTemplate.opsForZSet().add(RESERVATION_EXPIRATIONS, orderId, expiresAt);
                redisTemplate.expire(reservationKey(orderId), java.time.Duration.ofSeconds(RESERVATION_TTL_SECONDS));
            }
            return true;
        } catch (Exception ex) {
            log.error("Failed to reserve inventory for order {}", orderId, ex);
            rollback(reservedKeys);
            return false;
        }
    }

    @Override
    public boolean reserveFlashSale(String orderId, String flashSaleId, int quantity) {
        String key = String.format(FLASH_SALE_STOCK_KEY_PREFIX, flashSaleId);

        String scriptText =
            "local current = tonumber(redis.call('get', KEYS[1]) or '-1'); " +
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

    @Override
    public void release(String orderId, Map<String, Integer> productQuantities) {
        Map<String, Integer> releaseMap = productQuantities;
        if (releaseMap == null || releaseMap.isEmpty()) {
            releaseMap = redisTemplate.<String, String>opsForHash().entries(reservationKey(orderId)).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Integer.parseInt(e.getValue())));
        }
        releaseMap.forEach((productId, qty) -> {
            if (qty <= 0) {
                return;
            }
            redisTemplate.opsForValue().increment(availableKey(productId), qty);
            decrementReserved(productId, qty);
        });
        cleanupReservation(orderId);
    }

    @Override
    public void confirm(String orderId) {
        Map<String, String> reservation = redisTemplate.<String, String>opsForHash().entries(reservationKey(orderId));
        reservation.forEach((productId, qtyStr) -> {
            int qty = Integer.parseInt(qtyStr);
            decrementReserved(productId, qty);
        });
        cleanupReservation(orderId);
    }

    @Override
    @Scheduled(fixedDelay = 60000)
    public void releaseExpiredReservations() {
        long now = Instant.now().getEpochSecond();
        var expired = redisTemplate.opsForZSet().rangeByScore(RESERVATION_EXPIRATIONS, 0, now);
        if (expired == null || expired.isEmpty()) {
            return;
        }
        List<String> expiredOrders = expired.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        for (String orderId : expiredOrders) {
            release(orderId, Collections.emptyMap());
            redisTemplate.opsForZSet().remove(RESERVATION_EXPIRATIONS, orderId);
            
            // Sync Order Status to CANCELLED in DB
            try {
                orderRepository.findById(new com.learnfirebase.ecommerce.order.domain.model.OrderId(orderId))
                    .ifPresent(order -> {
                        if (order.getStatus() == com.learnfirebase.ecommerce.order.domain.model.OrderStatus.PENDING) {
                            order.cancel("Stock reservation expired");
                            orderRepository.save(order);
                            log.info("Cancelled order {} due to stock reservation expiration", orderId);
                        }
                    });
            } catch (Exception e) {
                log.error("Failed to cancel order {} after stock release", orderId, e);
            }
            
            log.info("Released expired reservation for order {}", orderId);
        }
    }

    private boolean adjustStockAtomically(String productId, int delta) {
        String scriptText =
            "local availKey = KEYS[1]; " +
            "local reservedKey = KEYS[2]; " +
            "local delta = tonumber(ARGV[1]); " +
            "local current = tonumber(redis.call('get', availKey) or '0'); " +
            "if current + delta < 0 then return -1 end; " +
            "redis.call('incrby', availKey, delta); " +
            "redis.call('incrby', reservedKey, -delta); " +
            "return redis.call('get', availKey);";

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(scriptText);
        script.setResultType(Long.class);

        String availKey = availableKey(productId);
        String reservedKey = reservedKey(productId);

        Long result = redisTemplate.execute(script, List.of(availKey, reservedKey), String.valueOf(delta));
        return result != null && result >= 0;
    }

    private void rollback(List<String> reservedKeys) {
        for (String entry : reservedKeys) {
            String[] parts = entry.split(":");
            if (parts.length != 2) {
                continue;
            }
            String productId = parts[0];
            int qty = Integer.parseInt(parts[1]);
            redisTemplate.opsForValue().increment(availableKey(productId), qty);
            decrementReserved(productId, qty);
        }
    }

    private String availableKey(String productId) {
        return String.format(AVAILABLE_KEY, INVENTORY_ID, productId);
    }

    private String reservedKey(String productId) {
        return String.format(RESERVED_KEY, INVENTORY_ID, productId);
    }

    private String reservationKey(String orderId) {
        return String.format(RESERVATION_HASH, orderId);
    }

    private void cleanupReservation(String orderId) {
        redisTemplate.delete(reservationKey(orderId));
        redisTemplate.opsForZSet().remove(RESERVATION_EXPIRATIONS, orderId);
    }

    private void decrementReserved(String productId, int qty) {
        if (qty <= 0) {
            return;
        }
        String key = reservedKey(productId);
        
        String script = "local current = tonumber(redis.call('get', KEYS[1]) or '0'); " +
                       "local next = math.max(0, current - tonumber(ARGV[1])); " +
                       "redis.call('set', KEYS[1], next); " +
                       "return next;";
                       
        redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), 
            Collections.singletonList(key), String.valueOf(qty));
    }

    private long readLong(String key) {
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }
}
