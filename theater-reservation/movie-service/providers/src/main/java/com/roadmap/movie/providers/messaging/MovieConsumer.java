package com.roadmap.movie.providers.messaging;

import com.roadmap.movie.core.mapper.MovieMapper;
import com.roadmap.movie.providers.database.MovieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MovieConsumer {

    private MovieRepository movieRepository;

    private Logger LOG = LoggerFactory.getLogger(MovieConsumer.class);

    private RabbitTemplate rabbitTemplate;

    // Mappers
    private MovieMapper movieMapper;
    private ObjectMapper objectMapper;

    public MovieConsumer(
                         MovieRepository movieRepository,
                         MovieMapper movieMapper,
                         ObjectMapper objectMapper,
                         RabbitTemplate rabbitTemplate) {
        this.movieRepository = movieRepository;
        this.movieMapper = movieMapper;
        this.objectMapper = objectMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

//    @RabbitListener(queues = "queue")
//    public Flux<Reply> receive(Request request) {
//        LOG.info(request.toString());
//        if (!request.getServices().contains(util.Service.MOVIE_SERVICE)){return null;}
//        switch (request.getRequestType()) {
//            case RequestType.GET_MOVIE_BY_ID -> {
//                return handleGetById(request.getCorrelationId(), (Integer) request.getPayload()).flux();
//            }
//            case RequestType.GET_ALL_MOVIES -> {
//                return handleGetAll(request.getCorrelationId());
//            }
//            default -> LOG.error("No RequestType matched with Movie Service");
//        }
//        return Flux.just(new Reply());
//    }

}
