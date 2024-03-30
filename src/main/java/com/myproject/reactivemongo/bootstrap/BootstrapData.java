package com.myproject.reactivemongo.bootstrap;

import com.myproject.reactivemongo.domain.Beer;
import com.myproject.reactivemongo.domain.Customer;
import com.myproject.reactivemongo.repositories.BeerRepository;
import com.myproject.reactivemongo.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class BootstrapData implements CommandLineRunner {

    private final BeerRepository beerRepository;
    private final CustomerRepository  customerRepository;

    @Override
    public void run(String... args) throws Exception {

        beerRepository.deleteAll()
                .doOnSuccess(success -> loadBeerData()).subscribe();

        customerRepository.deleteAll()
                .doOnSuccess(success -> loadCustomerData()).subscribe();
    }

    private void loadCustomerData() {

        customerRepository.count().subscribe(count -> {

            if (count == 0) {

                Customer customer1 = Customer.builder().customerName("Mike").build();
                Customer customer2 = Customer.builder().customerName("Tom").build();
                Customer customer3 = Customer.builder().customerName("Bob").build();

                customerRepository.save(customer1).subscribe();
                customerRepository.save(customer2).subscribe();
                customerRepository.save(customer3).subscribe();
            }
        });
    }

    private void loadBeerData() {

        beerRepository.count().subscribe(count -> {

            if (count == 0) {

                Beer beer1 = Beer.builder()
                        .beerName("Galaxy Cat")
                        .beerStyle("Pale Ale")
                        .upc("12345")
                        .price(new BigDecimal("12.99"))
                        .quantityOnHand(1234)
                        .build();

                Beer beer2 = Beer.builder()
                        .beerName("Crank")
                        .beerStyle("Pale Ale")
                        .upc("12345")
                        .price(new BigDecimal("14.05"))
                        .quantityOnHand(1234)
                        .build();

                Beer beer3 = Beer.builder()
                        .beerName("Sunshine City")
                        .beerStyle("Ipa")
                        .upc("12345")
                        .price(new BigDecimal("21.37"))
                        .quantityOnHand(1234)
                        .build();

                beerRepository.save(beer1).subscribe();
                beerRepository.save(beer2).subscribe();
                beerRepository.save(beer3).subscribe();
            }
        });
    }
}
