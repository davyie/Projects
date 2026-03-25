package com.roadmap.movie.config;

import com.roadmap.movie.providers.database.MovieRepository;
import dto.MovieDTO;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import replys.Reply;
import replys.ReplyType;
import requests.Request;
import requests.RequestType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.function.Function;

@Configuration
public class RabbitMQConfig {

    private String exchange = "exchange";
    private String queue = "queue";
    private String routingKey = "routingKey";

    private String replyQueue = "replyQueue";
    private String replyExchange = "replyExchange";
    private String replyRoutingKey = "replyRoutingKey";

    private MovieRepository movieRepository;
    private ObjectMapper objectMapper;

    public RabbitMQConfig(MovieRepository movieRepository,
                          ObjectMapper objectMapper) {
        this.movieRepository = movieRepository;
        this.objectMapper = objectMapper;
    }


    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public Queue queue() {
        return new Queue(queue);
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(exchange);
    }

    @Bean
    public Binding queueBinding() {
        return BindingBuilder.bind(queue()).to(directExchange()).with(routingKey);
    }

    @Bean
    public Queue replyQueue() {
        return new Queue(replyQueue);
    }

    @Bean
    public DirectExchange replyExchange() {
        return new DirectExchange(replyExchange);
    }

    @Bean
    public Binding replyBinding() {
        return BindingBuilder.bind(replyQueue()).to(replyExchange()).with(replyRoutingKey);
    }

    @Bean
    public Function<Flux<Request>, Flux<Reply>> movieProcessor() {
        return requestFlux -> requestFlux.flatMap(request -> {
            if (!request.getServices().contains(util.Service.MOVIE_SERVICE)) {
                return Mono.empty();
            }

            return switch (request.getRequestType()) {
                case RequestType.GET_MOVIE_BY_ID -> handleGetById(request.getCorrelationId(), (Integer) request.getPayload());
                case RequestType.GET_ALL_MOVIES -> handleGetAll(request.getCorrelationId());
                default -> Mono.just(new Reply());
            };
        });
    }

    @Transactional
    private Mono<Reply> handleGetById(String correlationId, Integer id) {
        return movieRepository.findById(id)
                .map(m -> {
                    Reply reply = new Reply();
                    reply.setCorrelationId(correlationId);
                    reply.setMessage("This is message from MovieConsumer, handleGetById");
                    reply.setReplyType(ReplyType.GET_MOVIE_BY_ID);
                    reply.setPayload(objectMapper.convertValue(m, MovieDTO.class));
                    return reply;
                });
    }

    @Transactional
    private Flux<Reply> handleGetAll(String correlationId) {
        return movieRepository.findAll()
                .map(m -> {
                    Reply reply = new Reply();
                    reply.setCorrelationId(correlationId);
                    reply.setMessage("This is the reply from MovieConsumer to fetch all movies.");
                    reply.setReplyType(ReplyType.GET_ALL_MOVIES);
                    reply.setPayload(m);
                    return reply;
                });
    }
}
