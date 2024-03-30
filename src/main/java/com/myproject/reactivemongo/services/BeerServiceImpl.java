package com.myproject.reactivemongo.services;

import com.myproject.reactivemongo.mappers.BeerMapper;
import com.myproject.reactivemongo.model.BeerDTO;
import com.myproject.reactivemongo.repositories.BeerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BeerServiceImpl implements BeerService {

    private final BeerMapper beerMapper;
    private final BeerRepository beerRepository;

    @Override
    public Flux<BeerDTO> listBeers() {

        return beerRepository.findAll()
                .map(beerMapper::beerToBeerDto);
    }

    @Override
    public Mono<BeerDTO> getById(String beerId) {
        return beerRepository.findById(beerId)
                .map(beerMapper::beerToBeerDto);
    }

    @Override
    public Mono<BeerDTO> findFirstByBeerName(String beerName) {
        return beerRepository.findFirstByBeerName(beerName)
                .map(beerMapper::beerToBeerDto);
    }

    @Override
    public Flux<BeerDTO> findByBeerStyle(String beerStyle) {

        return beerRepository.findByBeerStyle(beerStyle)
                .map(beerMapper::beerToBeerDto);
    }

    @Override
    public Mono<BeerDTO> saveBeer(Mono<BeerDTO> beerDTO) {

        return beerDTO.map(beerMapper::beerDtoToBeer)
                .flatMap(beerRepository::save)
                .map(beerMapper::beerToBeerDto);
    }

    @Override
    public Mono<BeerDTO> saveBeer(BeerDTO beerDTO) {

        return beerRepository.save(beerMapper.beerDtoToBeer(beerDTO))
                .map(beerMapper::beerToBeerDto);
    }

    @Override
    public Mono<BeerDTO> updateBeer(String beerId, BeerDTO beerDTO) {

        return beerRepository.findById(beerId)
                .map(foundBeer -> {
                    foundBeer.setBeerName(beerDTO.getBeerName());
                    foundBeer.setBeerStyle(beerDTO.getBeerStyle());
                    foundBeer.setPrice(beerDTO.getPrice());
                    foundBeer.setQuantityOnHand(beerDTO.getQuantityOnHand());
                    foundBeer.setUpc(beerDTO.getUpc());

                    return foundBeer;
                }).flatMap(beerRepository::save)
                .map(beerMapper::beerToBeerDto);
    }

    @Override
    public Mono<BeerDTO> patchBeer(String beerId, BeerDTO beerDTO) {

        return beerRepository.findById(beerId)
                .map(foundBeer -> {

                    if (StringUtils.hasText(beerDTO.getBeerName())) {
                        foundBeer.setBeerName(beerDTO.getBeerName());
                    }

                    if (StringUtils.hasText(beerDTO.getBeerStyle())) {
                        foundBeer.setBeerStyle(beerDTO.getBeerStyle());
                    }

                    if (StringUtils.hasText(beerDTO.getUpc())) {
                        foundBeer.setUpc(beerDTO.getUpc());
                    }

                    if (beerDTO.getPrice() != null) {
                        foundBeer.setPrice(beerDTO.getPrice());
                    }

                    if (beerDTO.getQuantityOnHand() != null) {
                        foundBeer.setQuantityOnHand(beerDTO.getQuantityOnHand());
                    }

                    return foundBeer;
                }).flatMap(beerRepository::save)
                .map(beerMapper::beerToBeerDto);
    }

    @Override
    public Mono<Void> deleteBeerById(String beerId) {

        return beerRepository.deleteById(beerId);
    }
}
