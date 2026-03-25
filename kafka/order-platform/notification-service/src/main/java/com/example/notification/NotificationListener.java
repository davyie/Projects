package com.example.notification;

import com.example.events.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    @KafkaListener(topics = "${kafka.topic.order-placed}", groupId = "${spring.kafka.consumer.group-id}")
    public void handle(OrderPlacedEvent event) {
        log.info("[NOTIFICATION] Received order | orderId={} | customer={}",
                event.getOrderId(), event.getCustomerId());

        sendConfirmationEmail(event);
    }

    private void sendConfirmationEmail(OrderPlacedEvent event) {
        // Simulated email — in production this would call an email provider (SendGrid, SES, etc.)
        log.info("""
                [NOTIFICATION] Sending confirmation email
                  To      : {}
                  Subject : Order Confirmation #{}
                  Body    : Hi {}, your order for {} x{} has been placed! Total: ${}
                """,
                event.getCustomerEmail(),
                event.getOrderId(),
                event.getCustomerId(),
                event.getProductId(),
                event.getQuantity(),
                event.getTotalPrice());
    }
}
