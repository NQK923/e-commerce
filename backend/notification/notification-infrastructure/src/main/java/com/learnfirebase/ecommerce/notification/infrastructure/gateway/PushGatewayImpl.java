package com.learnfirebase.ecommerce.notification.infrastructure.gateway;
 
import org.springframework.stereotype.Component;
 
import com.learnfirebase.ecommerce.notification.application.port.out.PushGateway;
 
import lombok.extern.slf4j.Slf4j;
 
/**
 * Mock implementation of PushGateway for demo and local development purposes.
 * Does not connect to a real push provider.
 */
@Slf4j
@Component
public class PushGatewayImpl implements PushGateway {
    @Override
    public String send(String recipient, String body) {
        log.info("[DEMO PUSH] Recipient: {}, Body: {}", recipient, body);
        return "push-" + recipient;
    }
}
