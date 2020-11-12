package com.annakhuseinova.service;

import com.annakhuseinova.dto.Movie;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoviesRestClientTest {

    private MoviesRestClient moviesRestClient;
    private WebClient webClient;

    @BeforeEach
    void setUp() {

        String baseUrl = "http://localhost:8081";
        webClient = WebClient.create(baseUrl);
        moviesRestClient = new MoviesRestClient(webClient);
    }

    @Test
    void retrieveAllMovies() {
        List<Movie> movieList = moviesRestClient.retrieveAllMovies();
        System.out.println(movieList);
        assertTrue(movieList.size() > 0);
    }

    @Test
    void retrieveMovieById() {
        Integer movieId = 1;
        Movie movie = moviesRestClient.retrieveMovieById(movieId);
        assertEquals("Batman Begins", movie.getName());
    }

    @Test
    void retrieveMovieById_notFound() {
        assertThrows(WebClientResponseException.class, ()-> {
            moviesRestClient.retrieveMovieById(100);
        });
    }

    @Test
    void retrieveMovieByName() {
        String movieName = "Avengers";
        List<Movie> movieList =  moviesRestClient.retrieveMoviesByName(movieName);
        assertEquals(4, movieList.size());
    }

    @Test
    void retrieveMovieByName_not_found() {
        String movieName = "ABC";
        assertThrows(WebClientResponseException.class, ()-> {
           moviesRestClient.retrieveMoviesByName(movieName);
        });
    }

    @Test
    void retrieveMovieByYear() {
        Integer year = 2012;
        List<Movie> moviesList = moviesRestClient.retrieveMoviesByYear(year);
        assertEquals(2, moviesList.size());
    }

    @Test
    void retrieveMovieByYear_404_response() {
        Integer year = 1950;
        assertThrows(WebClientResponseException.class, ()-> {
            moviesRestClient.retrieveMoviesByYear(year);
        });
    }


    @Test
    void addNewMovie() {
        //given
        String batmanBeginsCrew = "Tom Hanks, Tim Allen";
        Movie toyStory = Movie.builder()
                .cast(batmanBeginsCrew)
                .name("ToyStory")
                .releaseDate(LocalDate.of(2020,3,2))
                .year(2020)
                .build();
        //when
        Movie movie = moviesRestClient.addMovie(toyStory);

        //then
        assertTrue(movie.getMovieId() != null);

    }

    @Test
    void updateMovie() {

        //given
        String darkNightRisesCrew = "Tom Hardy";
        Movie darkNightRises = new Movie();
        darkNightRises.setCast(darkNightRisesCrew);
        Integer movieId = 3;

        //when
        Movie updatedMovie = moviesRestClient.updateMovie(movieId, darkNightRises);

        //then
        String updatedCastName = "Christian Bale, Heath Ledger , Michael Caine, Tom Hardy";
        assertTrue(updatedMovie.getCast().contains(darkNightRisesCrew));
    }
}