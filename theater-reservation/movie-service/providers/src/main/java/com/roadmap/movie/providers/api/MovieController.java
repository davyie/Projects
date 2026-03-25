package com.roadmap.movie.providers.api;

import com.roadmap.movie.core.ports.in.MovieUseCase;
import dto.MovieDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import requests.Request;

@RestController
@RequestMapping("/movies")
public class MovieController {

    private final MovieUseCase movieUseCase;

    public MovieController(MovieUseCase movieUseCase) {
        this.movieUseCase = movieUseCase;
    }

    /**
     * Test endpoint
     * @return
     */
    @GetMapping("/hello")
    public ResponseEntity<String> helloWorld() {
        return ResponseEntity.status(200).body("Hello World from movie service!");
    }

    @GetMapping("/")
    public Flux<MovieDTO> getAllMovies() {
        return movieUseCase.getAllMovies();
    }

    @GetMapping("/{movieId}")
    public Mono<MovieDTO> getMovieById(@PathVariable Integer movieId) {
        return movieUseCase.getMovieById(movieId);
    }

    @PostMapping("/add")
    public Mono<MovieDTO> addMovie(@RequestBody MovieDTO dto) {
        return movieUseCase.addMovie(dto);
    }

    @PutMapping("/update/{movieId}")
    public Mono<MovieDTO> updateMovie(@PathVariable Integer movieId, @RequestBody MovieDTO dto){
        return movieUseCase.updateMovie(movieId, dto);
    }

    @DeleteMapping("/delete/{movieId}")
    public Mono<Void> deleteMovie(@PathVariable Integer movieId) {
        return movieUseCase.deleteMovie(movieId);
    }

    @PostMapping("/message")
    public Mono<Request> sendMessage() {
        return movieUseCase.sendMessage();
    }
}
