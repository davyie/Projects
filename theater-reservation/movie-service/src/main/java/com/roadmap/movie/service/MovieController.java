package com.roadmap.movie.service;

import dto.MovieDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/movies")
public class MovieController {
    
    private MovieService movieService;
    
    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    /**
     * Test endpoint
     * @return
     */
    @GetMapping("/hello")
    public ResponseEntity<String> helloWorld() {
        return ResponseEntity.status(200).body("Hello World from movie service!");
    }

    @GetMapping("/all")
    public Flux<MovieDTO> getAllMovies() {
        return movieService.getAllMovies();
    }

    @PostMapping("/add")
    public Mono<MovieDTO> addMovie(@RequestBody MovieDTO dto) {
        return movieService.addMovie(dto);
    }

    @PutMapping("/update/{movieId}")
    public Mono<MovieDTO> updateMovie(@PathVariable Long movieId, @RequestBody MovieDTO dto){
        return movieService.updateMovie(movieId, dto);
    }

    @DeleteMapping("/delete/{movieId}")
    public Mono<Void> deleteMovie(@PathVariable Long movieId) {
        return movieService.deleteMovie(movieId);
    }

    @PostMapping("/message")
    public ResponseEntity<String> sendMessage() {
        movieService.sendMessage();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
