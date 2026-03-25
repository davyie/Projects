package com.example.analytics;

import com.example.events.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class AnalyticsListener {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsListener.class);

    private final AtomicInteger totalOrders = new AtomicInteger(0);
    private final AtomicReference<Double> totalRevenue = new AtomicReference<>(0.0);

    @KafkaListener(topics = "${kafka.topic.order-placed}", groupId = "${spring.kafka.consumer.group-id}")
    public void handle(OrderPlacedEvent event) {
        log.info("[ANALYTICS] Received order | orderId={} | productId={} | total=${}",
                event.getOrderId(), event.getProductId(), event.getTotalPrice());

        int orders = totalOrders.incrementAndGet();
        double revenue = totalRevenue.updateAndGet(r -> r + event.getTotalPrice());

        log.info("[ANALYTICS] Running totals | totalOrders={} | totalRevenue=${}",
                orders, String.format("%.2f", revenue));
    }
}
