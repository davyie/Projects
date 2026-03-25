package com.roadmap.movie.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("movies")
public class Movie {
    @Id
    private Integer id;
    private String name;
    private Long screenTime;
    private String description;

    public Movie (String name, Long screenTime, String description) {
        this.name = name;
        this.screenTime = screenTime;
        this.description = description;
    }

    @Override
    public String toString() {
        return "Movie: " + this.name + " ," + this.screenTime + " ," + this.description;
    }
}
