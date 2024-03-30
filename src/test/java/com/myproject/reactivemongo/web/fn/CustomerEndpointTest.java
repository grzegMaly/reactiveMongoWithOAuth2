package com.myproject.reactivemongo.web.fn;

import com.myproject.reactivemongo.domain.Customer;
import com.myproject.reactivemongo.model.CustomerDTO;
import com.myproject.reactivemongo.services.CustomerServiceImplTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
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
public class CustomerEndpointTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    @Order(1)
    void testListCustomers() {

        webTestClient.mutateWith(mockOAuth2Login())
                .get().uri(CustomerRouterConfig.CUSTOMER_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-type", "application/json")
                .expectBody().jsonPath("$.size()").value(greaterThan(1));
    }

    @Test
    @Order(2)
    void testGetCustomerByName() {

        final String CUSTOMER_NAME = "TEST";
        CustomerDTO testDto = getSavedTestCustomer();
        testDto.setCustomerName(CUSTOMER_NAME);

        webTestClient.mutateWith(mockOAuth2Login())
                .put().uri(CustomerRouterConfig.CUSTOMER_PATH_ID, testDto.getId())
                .body(Mono.just(testDto), CustomerDTO.class)
                .exchange();

        webTestClient.mutateWith(mockOAuth2Login())
                 .get().uri(UriComponentsBuilder
                .fromPath(CustomerRouterConfig.CUSTOMER_PATH)
                .queryParam("customerName", CUSTOMER_NAME).build().toUri())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-type", "application/json")
                .expectBody().jsonPath("$.size()").value(equalTo(1));
    }

    @Test
    @Order(3)
    void testGetCustomerById() {

        CustomerDTO testDto = getSavedTestCustomer();

        webTestClient.mutateWith(mockOAuth2Login())
                .get().uri(CustomerRouterConfig.CUSTOMER_PATH_ID, testDto.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerDTO.class);
    }

    @Test
    @Order(4)
    void testGetCustomerByIdNotFound() {

        webTestClient.mutateWith(mockOAuth2Login())
                .get().uri(CustomerRouterConfig.CUSTOMER_PATH_ID, 999)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(CustomerDTO.class);
    }

    @Test
    @Order(5)
    void testCreateCustomer() {

        Customer testCustomer = CustomerServiceImplTest.getTestCustomer();

        webTestClient.mutateWith(mockOAuth2Login())
                .post().uri(CustomerRouterConfig.CUSTOMER_PATH)
                .body(Mono.just(testCustomer), CustomerDTO.class)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("location");
    }

    @Test
    @Order(6)
    void testCreateCustomerBadData() {

        Customer testCustomer = CustomerServiceImplTest.getTestCustomer();
        testCustomer.setCustomerName("");

        webTestClient.mutateWith(mockOAuth2Login())
                .post().uri(CustomerRouterConfig.CUSTOMER_PATH)
                .body(Mono.just(testCustomer), CustomerDTO.class)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @Order(7)
    void testUpdateCustomerByIdFound() {

        final String NEW_NAME = "New Name";
        CustomerDTO testCustomer = getSavedTestCustomer();
        testCustomer.setCustomerName(NEW_NAME);

        webTestClient.mutateWith(mockOAuth2Login())
                .put().uri(CustomerRouterConfig.CUSTOMER_PATH_ID, testCustomer.getId())
                .body(Mono.just(testCustomer), CustomerDTO.class)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @Order(8)
    void testUpdateCustomerByIdNotFound() {

        Customer testCustomer = CustomerServiceImplTest.getTestCustomer();

        webTestClient.mutateWith(mockOAuth2Login())
                .put().uri(CustomerRouterConfig.CUSTOMER_PATH_ID, 999)
                .body(Mono.just(testCustomer), CustomerDTO.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(9)
    void testUpdateCustomerBadData() {

        CustomerDTO testCustomer = getSavedTestCustomer();
        testCustomer.setCustomerName("");

        webTestClient.mutateWith(mockOAuth2Login())
                .put().uri(CustomerRouterConfig.CUSTOMER_PATH_ID, testCustomer.getId())
                .body(Mono.just(testCustomer), CustomerDTO.class)
                .exchange()
                .expectStatus().isBadRequest();
    }


    @Test
    @Order(10)
    void testPatchCustomerByIdFound() {

        CustomerDTO testCustomer = getSavedTestCustomer();
        Customer tempCustomer = Customer.builder().customerName("New Name").build();

        webTestClient.mutateWith(mockOAuth2Login())
                .patch().uri(CustomerRouterConfig.CUSTOMER_PATH_ID, testCustomer.getId())
                .body(Mono.just(tempCustomer), CustomerDTO.class)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @Order(11)
    void testPatchCustomerByIdNotFound() {
        CustomerDTO testCustomer = getSavedTestCustomer();
        Customer tempCustomer = Customer.builder().customerName("New Name").build();

        webTestClient.mutateWith(mockOAuth2Login())
                .patch().uri(CustomerRouterConfig.CUSTOMER_PATH_ID, 999)
                .body(Mono.just(tempCustomer), CustomerDTO.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(12)
    void testPatchCustomerBadData() {

        CustomerDTO testCustomer = getSavedTestCustomer();
        Customer tempCustomer = Customer.builder().customerName("").build();

        webTestClient.mutateWith(mockOAuth2Login())
                .patch().uri(CustomerRouterConfig.CUSTOMER_PATH_ID, testCustomer.getId())
                .body(Mono.just(tempCustomer), CustomerDTO.class)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @Order(13)
    void testDeleteCustomerByIdFound() {

        CustomerDTO testCustomer = getSavedTestCustomer();

        webTestClient.mutateWith(mockOAuth2Login())
                .delete().uri(CustomerRouterConfig.CUSTOMER_PATH_ID, testCustomer.getId())
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @Order(14)
    void testDeleteCustomerByIdNotFound() {

        webTestClient.mutateWith(mockOAuth2Login())
                .delete().uri(CustomerRouterConfig.CUSTOMER_PATH_ID, 999)
                .exchange()
                .expectStatus().isNotFound();
    }

    public CustomerDTO getSavedTestCustomer() {

        FluxExchangeResult<CustomerDTO> customerDTOFluxExchangeResult = webTestClient.mutateWith(mockOAuth2Login())
                .post()
                .uri(CustomerRouterConfig.CUSTOMER_PATH)
                .body(Mono.just(CustomerServiceImplTest.getTestCustomer()), CustomerDTO.class)
                .header("Content-type", "application/json")
                .exchange()
                .returnResult(CustomerDTO.class);

        List<String> location = customerDTOFluxExchangeResult.getRequestHeaders().get("Location");

        return webTestClient.mutateWith(mockOAuth2Login())
                .get().uri(CustomerRouterConfig.CUSTOMER_PATH)
                .exchange().returnResult(CustomerDTO.class).getResponseBody().blockFirst();
    }
}
