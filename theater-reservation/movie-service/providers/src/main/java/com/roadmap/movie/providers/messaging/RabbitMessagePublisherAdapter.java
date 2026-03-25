package com.roadmap.movie.providers.messaging;

import com.roadmap.movie.core.ports.out.MessagePublisherPort;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import requests.Request;

@Component
public class RabbitMessagePublisherAdapter implements MessagePublisherPort {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMessagePublisherAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public Mono<Void> publishMessage(Request request) {
        return Mono.fromRunnable(() -> {
            rabbitTemplate.convertAndSend(
                    "exchange",
                    "routingKey",
                    request
            );
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
