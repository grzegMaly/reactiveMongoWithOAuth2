package com.myproject.reactivemongo.web.fn;

import com.myproject.reactivemongo.domain.Beer;
import com.myproject.reactivemongo.model.BeerDTO;
import com.myproject.reactivemongo.services.BeerServiceImplTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOAuth2Login;

@SpringBootTest
@AutoConfigureWebTestClient
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BeerEndpointTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    @Order(1)
    void testListBeers() {
        webTestClient.mutateWith(mockOAuth2Login())
                .get().uri(BeerRouterConfig.BEER_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-type", "application/json")
                .expectBody().jsonPath("$.size()").value(greaterThan(1));
    }

    @Test
    @Order(1)
    void testListBeersByStyle() {

        final String BEER_STYLE = "TEST";
        BeerDTO testDto = getSavedTestBeer();
        testDto.setBeerStyle(BEER_STYLE);

        webTestClient.mutateWith(mockOAuth2Login())
                .post().uri(BeerRouterConfig.BEER_PATH)
                .body(Mono.just(testDto), BeerDTO.class)
                .header("Content-type", "application/json")
                .exchange();

        webTestClient.mutateWith(mockOAuth2Login())
                .get().uri(UriComponentsBuilder
                        .fromPath(BeerRouterConfig.BEER_PATH)
                        .queryParam("beerStyle", BEER_STYLE).build().toUri())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-type", "application/json")
                .expectBody().jsonPath("$.size()").value(equalTo(1));
    }

    @Test
    @Order(2)
    void testGetById() {
        BeerDTO beerDTO = getSavedTestBeer();

        webTestClient.mutateWith(mockOAuth2Login())
                .get().uri(BeerRouterConfig.BEER_PATH_ID, beerDTO.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-type", "application/json")
                .expectBody(BeerDTO.class);
    }

    @Test
    @Order(3)
    void testGetByIdNotFound() {
        webTestClient.mutateWith(mockOAuth2Login())
                .get().uri(BeerRouterConfig.BEER_PATH_ID, 999)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(4)
    void testCreateBeer() {

        Beer testBeer = BeerServiceImplTest.getTestBeer();

        webTestClient.mutateWith(mockOAuth2Login())
                .post().uri(BeerRouterConfig.BEER_PATH)
                .body(Mono.just(testBeer), BeerDTO.class)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("location");
    }

    @Test
    @Order(5)
    void testCreateBeerBadData() {

        Beer testBeer = BeerServiceImplTest.getTestBeer();
        testBeer.setBeerName("");

        webTestClient.mutateWith(mockOAuth2Login())
                .post().uri(BeerRouterConfig.BEER_PATH)
                .body(Mono.just(testBeer), BeerDTO.class)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @Order(6)
    void testPatchIdFound() {

        BeerDTO beerDTO = getSavedTestBeer();
        BeerDTO testBeer = BeerDTO.builder().beerName("Papieżowe").build();

        webTestClient.mutateWith(mockOAuth2Login())
                .patch()
                .uri(BeerRouterConfig.BEER_PATH_ID, beerDTO.getId())
                .body(Mono.just(testBeer), BeerDTO.class)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @Order(7)
    void testPatchIdNotFound() {

        webTestClient.mutateWith(mockOAuth2Login())
                .patch().uri(BeerRouterConfig.BEER_PATH_ID, 999)
                .body(Mono.just(BeerServiceImplTest.getTestBeer()), BeerDTO.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(8)
    void testDeleteIdFound() {

        BeerDTO beerDTO = getSavedTestBeer();

        webTestClient.mutateWith(mockOAuth2Login())
                .delete().uri(BeerRouterConfig.BEER_PATH_ID, beerDTO.getId())
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @Order(9)
    void testDeleteIdNotFound() {

        webTestClient.mutateWith(mockOAuth2Login())
                .delete().uri(BeerRouterConfig.BEER_PATH_ID, 999)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(10)
    void testUpdateBeerBadRequest() {

        BeerDTO testBeer = getSavedTestBeer();
        testBeer.setBeerName("");

        webTestClient.mutateWith(mockOAuth2Login())
                .put().uri(BeerRouterConfig.BEER_PATH_ID, testBeer.getId())
                .body(Mono.just(testBeer), BeerDTO.class)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @Order(11)
    void testUpdateBeerIdNotFound() {

        webTestClient.mutateWith(mockOAuth2Login())
                .put().uri(BeerRouterConfig.BEER_PATH_ID, 999)
                .body(Mono.just(BeerServiceImplTest.getTestBeer()), BeerDTO.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(12)
    void testUpdateBeer() {

        BeerDTO testBeer = getSavedTestBeer();

        webTestClient.mutateWith(mockOAuth2Login())
                .put().uri(BeerRouterConfig.BEER_PATH_ID, testBeer.getId())
                .body(Mono.just(testBeer), BeerDTO.class)
                .exchange()
                .expectStatus().isNoContent();
    }

    public BeerDTO getSavedTestBeer() {

        FluxExchangeResult<BeerDTO> beerDTOFluxExchangeResult = webTestClient.mutateWith(mockOAuth2Login())
                .post().uri(BeerRouterConfig.BEER_PATH)
                .body(Mono.just(BeerServiceImplTest.getTestBeer()), BeerDTO.class)
                .header("Content-Type", "application/json")
                .exchange()
                .returnResult(BeerDTO.class);

        List<String> location = beerDTOFluxExchangeResult.getResponseHeaders().get("Location");

        return webTestClient.mutateWith(mockOAuth2Login())
                  .get().uri(BeerRouterConfig.BEER_PATH)
                .exchange().returnResult(BeerDTO.class).getResponseBody().blockFirst();
    }
}