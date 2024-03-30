package com.myproject.reactivemongo.services;

import com.myproject.reactivemongo.domain.Beer;
import com.myproject.reactivemongo.mappers.BeerMapper;
import com.myproject.reactivemongo.mappers.BeerMapperImpl;
import com.myproject.reactivemongo.model.BeerDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
public class  BeerServiceImplTest {

    @Autowired
    BeerService beerService;

    @Autowired
    BeerMapper beerMapper;

    BeerDTO beerDTO;

    @BeforeEach
    void setUp() {

        beerDTO = beerMapper.beerToBeerDto(getTestBeer());
    }

    @Test
    void testFindFirstByBeerName() {

        BeerDTO savedDto = getSavedBeerDto();
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        Mono<BeerDTO> foundDto = beerService.findFirstByBeerName(savedDto.getBeerName());

        foundDto.subscribe(dto -> {
            System.out.println(dto.toString());
            atomicBoolean.set(true);
        });

        await().untilTrue(atomicBoolean);
    }

    @Test
    void testFindFirstByBeerStyle() {

        BeerDTO savedDto = getSavedBeerDto();
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        System.out.println(savedDto.toString());

        Flux<BeerDTO> foundDto = beerService.findByBeerStyle(savedDto.getBeerStyle());

        foundDto.subscribe(dto -> {
            System.out.println(dto.toString());
            atomicBoolean.set(true);
        });

        await().untilTrue(atomicBoolean);
    }

    @Test
    @DisplayName("Test Save Beer Using Subscriber")
    void saveBeerUseSubscriber() {

        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        AtomicReference<BeerDTO> atomicDto = new AtomicReference<>();

        Mono<BeerDTO> savedMono = beerService.saveBeer(Mono.just(beerDTO));

        savedMono.subscribe(savedDto -> {
            System.out.println(savedDto.getId());
            atomicBoolean.set(true);
            atomicDto.set(savedDto);
        });

        await().untilTrue(atomicBoolean);

        BeerDTO persistedDto = atomicDto.get();
        assertThat(persistedDto).isNotNull();
        assertThat(persistedDto.getId()).isNotNull();
    }


    @Test
    @DisplayName("Test Save Beer Using Block")
    void saveBeerUseBlock() {

        BeerDTO savedDto = beerService.saveBeer(Mono.just(beerDTO)).block();

        assertThat(savedDto).isNotNull();
        assertThat(savedDto.getId()).isNotNull();
    }

    @Test
    @DisplayName("Test Update Beer Using Block")
    void testUpdateBlocking() {

        final String newName = "New Beer Name";
        BeerDTO savedBeerDto = getSavedBeerDto();
        savedBeerDto.setBeerName(newName);

        BeerDTO updatedDto = beerService.updateBeer(savedBeerDto.getId(), savedBeerDto).block();

        BeerDTO fetchedDto = beerService.getById(updatedDto.getId()).block();
        assertThat(fetchedDto.getBeerName()).isEqualTo(newName);
        assertThat(fetchedDto.getId()).isEqualTo(savedBeerDto.getId());
    }

    @Test
    @DisplayName("Test Update Using Reactive Streams")
    void testUpdateStreaming() {

        final String newName = "New Beer Name";
        AtomicReference<BeerDTO> atomicDto = new AtomicReference<>();

        beerService.saveBeer(Mono.just(getTestBeerDto()))
                .map(savedBeerDto -> {
                    savedBeerDto.setBeerName(newName);
                    return savedBeerDto;
                }).flatMap(beerService::saveBeer)
                .flatMap(savedUpdatedBeer -> beerService.getById(savedUpdatedBeer.getId()))
                .subscribe(atomicDto::set);

        await().until(() -> atomicDto.get() != null);
        assertThat(atomicDto.get().getBeerName()).isEqualTo(newName);
    }

    @Test
    void testDeleteBeer() {

        BeerDTO beerToDelete = getSavedBeerDto();

        beerService.deleteBeerById(beerToDelete.getId()).block();

        Mono<BeerDTO> expectedEmptyBeerMono = beerService.getById(beerToDelete.getId());

        BeerDTO emptyBear = expectedEmptyBeerMono.block();

        assertThat(emptyBear).isNull();
    }

    public BeerDTO getSavedBeerDto() {
        return beerService.saveBeer(Mono.just(getTestBeerDto())).block();
    }

    public static BeerDTO getTestBeerDto() {
        return new BeerMapperImpl().beerToBeerDto(getTestBeer());
    }

    public static Beer getTestBeer() {

        return Beer.builder()
                .beerName("Space Dust")
                .beerStyle("IPA")
                .price(BigDecimal.TEN)
                .quantityOnHand(12)
                .upc("123456")
                .build();
    }
}