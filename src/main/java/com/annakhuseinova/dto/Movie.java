package com.annakhuseinova.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Movie {

    private String cast;
    private Long movieId;
    private String name;
    private LocalDate releaseDate;
    private Integer year;
}
