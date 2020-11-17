package com.annakhuseinova.service;

import com.annakhuseinova.constants.MoviesAppConstants;
import com.annakhuseinova.dto.Movie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static com.annakhuseinova.constants.MoviesAppConstants.*;

@Slf4j
public class MoviesRestClient {

    private WebClient webClient;

    public MoviesRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<Movie> retrieveAllMovies(){
       return webClient.get().uri(MoviesAppConstants.GET_ALL_MOVIES_V1)
                .retrieve()
                .bodyToFlux(Movie.class)
                .collectList()
                .block();
    }

    public Movie retrieveMovieById(Integer movieId){
        try {
            return webClient.get().uri(MoviesAppConstants.MOVIE_BY_ID_PATH_PARAM, movieId).retrieve()
                    .bodyToMono(Movie.class)
                    .block();
        } catch (WebClientResponseException e){
            log.error("WebClientResponseException in retrieveMovieById and the message is {}",
                    e.getResponseBodyAsString());
            throw e;
        } catch (Exception e){
            log.error("Exception is retrieveByMovieId and the message is {}", e.getMessage());
            throw e;
        }
    }

    public List<Movie> retrieveMoviesByName(String name){

        String retrieveByNameUrl = UriComponentsBuilder.fromUriString(MOVIE_BY_NAME_QUERY_PARAM_V1)
                .queryParam("movie_name", name).buildAndExpand()
                .toUriString();
        try {
            return webClient.get().uri(retrieveByNameUrl).retrieve().bodyToFlux(Movie.class).collectList().block();
        } catch (WebClientResponseException ex){
            log.error("WebCLientResponseException in retrieveMovieByName. Status code is {} and the message is {}",
                    ex.getRawStatusCode());
        } catch (Exception ex){
            log.error("Exception in retrieveMovieByName and the message is {}", ex);
        }
        return null;
    }

    public List<Movie> retrieveMoviesByYear(Integer year){
        String retrieveByNameUrl = UriComponentsBuilder.fromUriString(MOVIE_BY_YEAR_QUERY_PARAM_V1)
                .queryParam("year", year).buildAndExpand()
                .toUriString();
        try {
            return webClient.get().uri(retrieveByNameUrl).retrieve().bodyToFlux(Movie.class).collectList().block();
        } catch (WebClientResponseException ex){
            log.error("WebCLientResponseException in retrieveMovieByName. Status code is {} and the message is {}",
                    ex.getRawStatusCode());
        } catch (Exception ex){
            log.error("Exception in retrieveMovieByName and the message is {}", ex);
        }
        return null;
    }

    public Movie addMovie(Movie newMovie){
        try {
            return webClient.post().uri(ADD_MOVIE_V2).syncBody(newMovie).retrieve().bodyToMono(Movie.class).block();
        } catch (WebClientResponseException e) {
            log.error("WebCLientResponseException in addMovie. Status code is {} and the message is {}");
        } catch (Exception e){
            log.error("Generic exception in retrieveMovieByName. Status code is {} and the message is {}");
        }
        return null;
    }

    public Movie updateMovie(Integer movieId, Movie movie){
        return webClient.put().uri(MOVIE_BY_ID_PATH_PARAM).syncBody(movie).retrieve().bodyToMono(Movie.class)
                .block();
    }

    public String deleteMovie(Integer movieId){

        try {
            return webClient.delete().uri(MOVIE_BY_ID_PATH_PARAM, movieId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e){
            log.error("WebClientResponseException in delete movie. Status code is {} and the message is {}",
                    e.getStatusCode(), e.getRawStatusCode());
        } catch (Exception e){
            log.error("Exception in delete movie. The message is {}", e.getMessage());
        }
       return null;
    }

    public String deleteMovieByName(String movieName){

        String deleteMovieByNameURI = UriComponentsBuilder.fromUriString(MOVIE_BY_NAME_QUERY_PARAM_V1)
                .queryParam("movie_name", movieName)
                .buildAndExpand()
                .toUriString();
        try {
             webClient.delete().uri(deleteMovieByNameURI)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (WebClientResponseException ex){
            log.error("WebClientResponseException in deleteMovie. Status code is {} and the ");
            throw ex;
        } catch (Exception e){
            log.error("Exception in deleteMovie and the message is {}", e.getMessage());
        }
        return "Movie deleted successfully";
    }
}
