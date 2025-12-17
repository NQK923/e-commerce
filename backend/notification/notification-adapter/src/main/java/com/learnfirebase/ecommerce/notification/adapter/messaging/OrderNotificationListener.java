package com.learnfirebase.ecommerce.notification.adapter.messaging;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnfirebase.ecommerce.notification.application.command.RecordNotificationCommand;
import com.learnfirebase.ecommerce.notification.application.port.in.RecordNotificationUseCase;
import com.learnfirebase.ecommerce.notification.domain.model.NotificationChannel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNotificationListener {

    private final RecordNotificationUseCase recordNotificationUseCase;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = {"OrderCreated", "OrderPaid", "OrderCancelled"}, groupId = "notification-service")
    public void handleOrderEvents(ConsumerRecord<String, String> record) {
        String topic = record.topic();
        String payload = record.value();
        try {
            EventEnvelope event = objectMapper.readValue(payload, EventEnvelope.class);
            if (event.userId == null) {
                log.debug("Skipping notification with missing userId for order {}", event.orderId);
                return;
            }
            String title;
            String body;
            switch (topic) {
                case "OrderCreated" -> {
                    title = "Đơn hàng đã tạo";
                    body = "Đơn hàng " + event.orderId + " đã được tạo.";
                }
                case "OrderPaid" -> {
                    title = "Thanh toán thành công";
                    body = "Đơn hàng " + event.orderId + " đã thanh toán thành công.";
                }
                case "OrderCancelled" -> {
                    title = "Đơn hàng bị hủy";
                    body = "Đơn hàng " + event.orderId + " đã bị hủy.";
                }
                default -> {
                    return;
                }
            }
            recordNotificationUseCase.record(RecordNotificationCommand.builder()
                .userId(event.userId)
                .title(title)
                .body(body)
                .channel(NotificationChannel.PUSH)
                .build());
        } catch (Exception e) {
            log.error("Failed to handle order event for notifications", e);
        }
    }

    private static class EventEnvelope {
        public String orderId;
        public String userId;
    }
}
