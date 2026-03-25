package com.roadmap.movie.core.ports.out;

import reactor.core.publisher.Mono;
import requests.Request;

public interface MessagePublisherPort {

    Mono<Void> publishMessage(Request request);
}
