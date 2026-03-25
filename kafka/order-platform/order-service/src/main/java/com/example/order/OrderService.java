package com.example.order;

import com.example.events.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @Value("${kafka.topic.order-placed}")
    private String orderPlacedTopic;

    public OrderService(KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public OrderPlacedEvent placeOrder(PlaceOrderRequest request) {
        OrderPlacedEvent event = new OrderPlacedEvent(
                request.customerId(),
                request.customerEmail(),
                request.productId(),
                request.quantity(),
                request.totalPrice()
        );

        // Use orderId as the key so all events for the same order go to the same partition
        CompletableFuture<SendResult<String, OrderPlacedEvent>> future =
                kafkaTemplate.send(orderPlacedTopic, event.getOrderId(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send event for order {}: {}", event.getOrderId(), ex.getMessage());
            } else {
                log.info("Order event sent | orderId={} | partition={} | offset={}",
                        event.getOrderId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });

        return event;
    }
}
