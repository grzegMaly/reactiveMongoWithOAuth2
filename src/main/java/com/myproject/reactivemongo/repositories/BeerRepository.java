package com.myproject.reactivemongo.repositories;

import com.myproject.reactivemongo.domain.Beer;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface BeerRepository extends ReactiveMongoRepository<Beer, String> {

    Mono<Beer> findFirstByBeerName(String beerName);
    Flux<Beer> findByBeerStyle(String beerStyle);
}
