package com.myproject.reactivemongo.web.fn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.reactivemongo.model.BeerDTO;
import com.myproject.reactivemongo.services.BeerService;
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
public class BeerHandler {

    private final BeerService beerService;
    private final Validator validator;

    private static final Function<ResponseStatusException, Mono<ServerResponse>> handleResponseStatusException = ex ->
            ServerResponse.status(ex.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(ex.getReason() != null ? ex.getReason() : "");

    private void validate(BeerDTO beerDTO) {

        Errors errors = new BeanPropertyBindingResult(beerDTO, "beerDto");
        validator.validate(beerDTO, errors);

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


    public Mono<ServerResponse> listBeers(ServerRequest request) {

        Flux<BeerDTO> flux;

        if (request.queryParam("beerStyle").isPresent()) {
            flux = beerService.findByBeerStyle(request.queryParam("beerStyle").get());
        } else {
            flux = beerService.listBeers();
        }

        return ServerResponse.ok()
                .body(flux, BeerDTO.class);
    }

    public Mono<ServerResponse> getBeerById(ServerRequest request) {

        return ServerResponse.ok()
                .body(beerService.getById(request.pathVariable("beerId"))
                                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND))),
                        BeerDTO.class);
    }

    public Mono<ServerResponse> createNewBeer(ServerRequest request) {

        return request.bodyToMono(BeerDTO.class)
                .flatMap(beerDTO -> Mono.defer(() -> {
                    validate(beerDTO);
                    return beerService.saveBeer(beerDTO);
                }))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(savedDto -> ServerResponse.created(
                        UriComponentsBuilder.fromPath(BeerRouterConfig.BEER_PATH_ID)
                                .build(savedDto.getId())
                ).build())
                .onErrorResume(ResponseStatusException.class, handleResponseStatusException);

    }

    public Mono<ServerResponse> updateBeerById(ServerRequest request) {
        return request.bodyToMono(BeerDTO.class)
                .flatMap(beerDTO -> Mono.defer(() -> {
                    validate(beerDTO);
                    return beerService.updateBeer(request.pathVariable("beerId"), beerDTO);
                }))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(savedDto -> ServerResponse.noContent().build())
                .onErrorResume(ResponseStatusException.class, ex -> ServerResponse.status(ex.getStatusCode())
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ex.getReason() != null ? ex.getReason() : ""));
    }

    public Mono<ServerResponse> patchBeerById(ServerRequest request) {
        return request.bodyToMono(BeerDTO.class)
                .flatMap(beerDTO -> beerService.patchBeer(request.pathVariable("beerId"), beerDTO))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(updatedDto -> ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> deleteBeerById(ServerRequest request) {

        return beerService.getById(request.pathVariable("beerId"))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(beerDTO -> beerService.deleteBeerById(beerDTO.getId()))
                .then(ServerResponse.noContent().build());
    }
}
