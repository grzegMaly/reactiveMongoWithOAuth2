package com.myproject.reactivemongo.web.fn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.reactivemongo.model.CustomerDTO;
import com.myproject.reactivemongo.services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class CustomerHandler {

    private final CustomerService customerService;
    private final Validator validator;

    Function<ResponseStatusException, Mono<ServerResponse>> handleResponseStatusException = ex ->
            ServerResponse.status(ex.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(ex.getReason() != null ? ex.getReason() : "");

    private void validate(CustomerDTO customerDTO) {

        Errors errors = new BeanPropertyBindingResult(customerDTO, "customerDto");
        validator.validate(customerDTO, errors);

        if (errors.hasErrors()) {

            ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

            List<Map<String, String>> errorList = Stream.concat(
                    Stream.of(Collections.singletonMap("message", "Validate Failed")),
                    errors.getFieldErrors().stream()
                            .map(fieldError ->
                                    Collections.singletonMap(fieldError.getField(), fieldError.getDefaultMessage()))
            ).collect(Collectors.toList());

            try {

                String jsonErrorDetails = objectMapper.writeValueAsString(errorList);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, jsonErrorDetails);
            } catch (JsonProcessingException e) {

                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing JSON", e);
            }
        }
    }

    public Mono<ServerResponse> listCustomers(ServerRequest request) {

        Flux<CustomerDTO> flux;

        if (request.queryParam("customerName").isPresent()) {
            flux = customerService.findByCustomerName(request.queryParam("customerName").get());
        } else {
            flux = customerService.listCustomers();
        }

        return ServerResponse.ok()
                .body(flux, CustomerDTO.class);

    }

    public Mono<ServerResponse> getCustomerById(ServerRequest request) {

        return ServerResponse.ok()
                .body(customerService.getCustomerById(request.pathVariable("customerId"))
                                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND))),
                        CustomerDTO.class);
    }

    public Mono<ServerResponse> createNewCustomer(ServerRequest request) {

        return request.bodyToMono(CustomerDTO.class)
                .flatMap(customerDTO -> Mono.defer(() -> {
                    validate(customerDTO);
                    return customerService.saveCustomer(customerDTO);
                }))
                .flatMap(customerDTO -> ServerResponse
                        .created(UriComponentsBuilder
                                .fromPath(CustomerRouterConfig.CUSTOMER_PATH_ID)
                                .build(customerDTO.getId()))
                        .build())
                .onErrorResume(ResponseStatusException.class, handleResponseStatusException);
    }

    public Mono<ServerResponse> updateCustomerById(ServerRequest request) {

        return request.bodyToMono(CustomerDTO.class)
                .flatMap(customerDTO -> Mono.defer(() -> {
                    validate(customerDTO);
                    return customerService.updateCustomer(request.pathVariable("customerId"), customerDTO);
                }))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(savedDto -> ServerResponse.noContent().build())
                .onErrorResume(ResponseStatusException.class, handleResponseStatusException);
    }

    public Mono<ServerResponse> patchCustomerById(ServerRequest request) {

        return request.bodyToMono(CustomerDTO.class)
                .flatMap(customerDTO ->
                        customerService.patchCustomer(request.pathVariable("customerId"), customerDTO))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(savedDto -> ServerResponse.noContent().build())
                .onErrorResume(ResponseStatusException.class, handleResponseStatusException);
    }

    public Mono<ServerResponse> deleteCustomerById(ServerRequest request) {

        return customerService.getCustomerById(request.pathVariable("customerId"))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(customerDTO -> customerService.deleteCustomerById(customerDTO.getId()))
                .then(ServerResponse.noContent().build());
    }
}
