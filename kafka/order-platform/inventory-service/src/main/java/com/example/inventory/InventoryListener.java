package com.example.inventory;

import com.example.events.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InventoryListener {

    private static final Logger log = LoggerFactory.getLogger(InventoryListener.class);

    // Simulated in-memory stock: productId -> stock count
    private final Map<String, Integer> stock = new ConcurrentHashMap<>(Map.of(
            "PROD-001", 100,
            "PROD-002", 50,
            "PROD-003", 200
    ));

    @KafkaListener(topics = "${kafka.topic.order-placed}", groupId = "${spring.kafka.consumer.group-id}")
    public void handle(OrderPlacedEvent event) {
        log.info("[INVENTORY] Received order | orderId={} | productId={} | quantity={}",
                event.getOrderId(), event.getProductId(), event.getQuantity());

        stock.merge(event.getProductId(), event.getQuantity(), (current, qty) -> {
            int updated = current - qty;
            if (updated < 0) {
                log.warn("[INVENTORY] Stock went negative for product {} — may need restock!", event.getProductId());
            }
            log.info("[INVENTORY] Stock updated | productId={} | before={} | after={}",
                    event.getProductId(), current, updated);
            return updated;
        });

        // New product not in our map yet
        stock.putIfAbsent(event.getProductId(), -event.getQuantity());
    }
}
