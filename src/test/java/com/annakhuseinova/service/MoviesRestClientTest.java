package com.annakhuseinova.service;

import com.annakhuseinova.dto.Movie;
import com.annakhuseinova.dto.MovieErrorResponse;
import com.github.jenspiegsa.wiremockextension.ConfigureWireMock;
import com.github.jenspiegsa.wiremockextension.InjectServer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.util.List;

import static com.annakhuseinova.constants.MoviesAppConstants.GET_ALL_MOVIES_V1;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(WireMockExtension.class)
class MoviesRestClientTest {

    private MoviesRestClient moviesRestClient;
    private WebClient webClient;

    @InjectServer
    private WireMockServer wireMockServer;

    @ConfigureWireMock
    Options options = wireMockConfig().port(8088).notifier(new ConsoleNotifier(true))
            .extensions(new ResponseTemplateTransformer(true));

    @BeforeEach
    void setUp() {
        int port = wireMockServer.port();
        String baseUrl = String.format("http://localhost:%s/", port);
        webClient = WebClient.create(baseUrl);
        moviesRestClient = new MoviesRestClient(webClient);
    }

    @Test
    void retrieveAllMovies() {

        // Given
        stubFor(get(urlPathEqualTo(GET_ALL_MOVIES_V1)).willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("all-movies.json")));

        List<Movie> movieList = moviesRestClient.retrieveAllMovies();
        System.out.println(movieList);
        assertTrue(movieList.size() > 0);
    }

    @Test
    void retrieveMovieById() {
        stubFor(get(urlPathEqualTo("/movieservice/v1/movie/1")).willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value()).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("movie.json")));

        Integer movieId = 1;
        Movie movie = moviesRestClient.retrieveMovieById(movieId);
        assertEquals("Batman Begins", movie.getName());
    }

    @Test
    void responseTemplating() {
        stubFor(get(urlPathEqualTo("/movieservice/v1/movie/1")).willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value()).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("movie.json")));

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

    @Test
    void deleteMovie() {

    }


    @Test
    void deleteMovie_notFound() {

        //given
        Integer movieId=100;

        //when
        assertThrows(MovieErrorResponse.class, ()-> moviesRestClient.deleteMovie(movieId));

    }
}