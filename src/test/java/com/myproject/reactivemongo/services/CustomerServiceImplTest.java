package com.myproject.reactivemongo.services;

import com.myproject.reactivemongo.domain.Customer;
import com.myproject.reactivemongo.mappers.CustomerMapper;
import com.myproject.reactivemongo.mappers.CustomerMapperImpl;
import com.myproject.reactivemongo.model.CustomerDTO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CustomerServiceImplTest {

    @Autowired
    CustomerService customerService;

    @Autowired
    CustomerMapper customerMapper;

    CustomerDTO customerDTO;

    @BeforeEach
    void setUp() {
        customerDTO = customerMapper.customerToCustomerDto(getTestCustomer());
    }

    @Test
    @Order(1)
    void testFindFirstByCustomerName() {

        CustomerDTO savedDto = getSavedCustomerDto();
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        Flux<CustomerDTO> foundDto = customerService.findByCustomerName(savedDto.getCustomerName());

        foundDto.subscribe(dto -> {
            System.out.println(dto.toString());
            atomicBoolean.set(true);
        });

        await().untilTrue(atomicBoolean);
    }

    @Test
    @Order(2)
    @DisplayName("Test Save Customer Using Subscriber")
    void testSaveCustomerUseSubscriber() {

        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        AtomicReference<CustomerDTO> atomicDto = new AtomicReference<>();

        Mono<CustomerDTO> savedMono = customerService.saveCustomer(Mono.just(customerDTO));

        savedMono.subscribe(savedDto -> {
            System.out.println(savedDto.getId());
            atomicBoolean.set(true);
            atomicDto.set(savedDto);
        });

        await().untilTrue(atomicBoolean);

        CustomerDTO persistedDto = atomicDto.get();
        assertThat(persistedDto).isNotNull();
        assertThat(persistedDto.getId()).isNotNull();
    }

    @Test
    @Order(3)
    @DisplayName("Test Save Customer Using Blocking")
    void testSaveCustomerUseBlock() {

        CustomerDTO savedDto = customerService.saveCustomer(Mono.just(customerDTO)).block();

        assertThat(savedDto).isNotNull();
        assertThat(savedDto.getId()).isNotNull();
    }

    @Test
    @Order(4)
    @DisplayName("Test Update Customer Using Blocking")
    void testUpdateBlocking() {

        final String newName = "New Name Name Name";
        CustomerDTO savedCustomerDto = getSavedCustomerDto();
        savedCustomerDto.setCustomerName(newName);

        CustomerDTO updatedDto = customerService.updateCustomer(savedCustomerDto.getId(), savedCustomerDto).block();

        CustomerDTO fetchedDto = customerService.getCustomerById(updatedDto.getId()).block();
        assertThat(fetchedDto.getCustomerName()).isEqualTo(newName);
        assertThat(fetchedDto.getId()).isEqualTo(savedCustomerDto.getId());
    }

    @Test
    @Order(5)
    @DisplayName("Test Update Using Reactive Streams")
    void testUpdateStreaming() {

        final String newName = "New Name Name Name";
        AtomicReference<CustomerDTO> atomicDto = new AtomicReference<>();

        customerService.saveCustomer(Mono.just(getSavedCustomerDto()))
                .map(savedCustomerDto -> {
                    savedCustomerDto.setCustomerName(newName);
                    return savedCustomerDto;
                }).flatMap(dto -> customerService.updateCustomer(dto.getId(), dto))
                .flatMap(savedUpdatedDto -> customerService.getCustomerById(savedUpdatedDto.getId()))
                .subscribe(atomicDto::set);

        await().until(() -> atomicDto.get() != null);
        assertThat(atomicDto.get().getCustomerName()).isEqualTo(newName);
    }

    @Test
    @Order(6)
    void testDeleteCustomer() {

        CustomerDTO customerToDelete = getSavedCustomerDto();

        customerService.deleteCustomerById(customerToDelete.getId()).block();

        Mono<CustomerDTO> expectedEmptyCustomerMono = customerService.getCustomerById(customerToDelete.getId());
        CustomerDTO emptyCustomer = expectedEmptyCustomerMono.block();

        assertThat(emptyCustomer).isNull();
    }

    public CustomerDTO getSavedCustomerDto() {

        return customerService.saveCustomer(Mono.just(getTestCustomerDto())).block();
    }

    public static CustomerDTO getTestCustomerDto() {

        return new CustomerMapperImpl().customerToCustomerDto(getTestCustomer());
    }

    public static Customer getTestCustomer() {

        return Customer.builder()
                .customerName("Mike")
                .build();
    }
}