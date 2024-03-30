package com.myproject.reactivemongo.services;

import com.myproject.reactivemongo.model.BeerDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BeerService {

    Flux<BeerDTO> listBeers();
    Mono<BeerDTO> getById(String beerId);
    Mono<BeerDTO> findFirstByBeerName(String beerName);
    Flux<BeerDTO> findByBeerStyle(String beerStyle);
    Mono<BeerDTO> saveBeer(Mono<BeerDTO> beerDTO);
    Mono<BeerDTO> saveBeer(BeerDTO beerDTO);
    Mono<BeerDTO> updateBeer(String beerId, BeerDTO beerDTO);
    Mono<BeerDTO> patchBeer(String beerId, BeerDTO beerDTO);
    Mono<Void> deleteBeerById(String beerId);
}
