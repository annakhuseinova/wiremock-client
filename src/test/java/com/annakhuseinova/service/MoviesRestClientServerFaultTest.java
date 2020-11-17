package com.annakhuseinova.service;

import com.github.jenspiegsa.wiremockextension.ConfigureWireMock;
import com.github.jenspiegsa.wiremockextension.InjectServer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.Fault;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import com.github.tomakehurst.wiremock.core.Options;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(WireMockExtension.class)
public class MoviesRestClientServerFaultTest {

    private MoviesRestClient moviesRestClient;
    private WebClient webClient;

    @InjectServer
    private WireMockServer wireMockServer;

    @ConfigureWireMock
    private Options options = wireMockConfig().port(8088).notifier(new ConsoleNotifier(true))
            .extensions(new ResponseTemplateTransformer(true));

    private TcpClient tcpClient = TcpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .doOnConnected(connection -> connection.addHandlerLast(new ReadTimeoutHandler(5))
            .addHandlerLast(new WriteTimeoutHandler(5)));

    @BeforeEach
    void setUp() {
        int port = wireMockServer.port();
        String baseUrl = String.format("http://localhost:%s", port);
        //webClient = WebClient.create(baseUrl);
        webClient = WebClient.builder().clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                .baseUrl(baseUrl)
                .build();
        moviesRestClient = new MoviesRestClient(webClient);
    }

    @Test
    void retrieveAllMovies_return500Status() {
        stubFor(get(anyUrl()).willReturn(serverError()));
        assertThrows(WebClientResponseException.class, ()->{
            moviesRestClient.retrieveAllMovies();
        });
    }

    @Test
    void retrieveAllMovies_503_serviceUnavailable() {
        stubFor(get(anyUrl()).willReturn(serverError().withStatus(HttpStatus.SERVICE_UNAVAILABLE.value())
                .withBody("Service Unavailable")));
        WebClientResponseException exception = assertThrows(WebClientResponseException.class, ()-> {
            moviesRestClient.retrieveAllMovies();
        });
        assertEquals("Service Unavailable", exception.getMessage());
    }

    @Test
    void retrieveAllMovies_FaultResponse() {
        // Stubbing for premature connection close.
        stubFor(get(anyUrl()).willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));
        WebClientResponseException exception = assertThrows(WebClientResponseException.class, ()-> {
            moviesRestClient.retrieveAllMovies();
        });
        assertEquals("Service Unavailable", exception.getMessage());
    }

    @Test
    void retrieveAllMovies_fixedDelay() {
        stubFor(get(anyUrl()).willReturn(ok().withFixedDelay(10000)));
        assertThrows(WebClientResponseException.class, ()-> {
           moviesRestClient.retrieveAllMovies();
        });
    }

    @Test
    void retrieveAllMovies_randomDelay() {
        stubFor(get(anyUrl()).willReturn(ok().withUniformRandomDelay(6000,10000)));
        assertThrows(WebClientResponseException.class, ()-> {
            moviesRestClient.retrieveAllMovies();
        });
    }
}
