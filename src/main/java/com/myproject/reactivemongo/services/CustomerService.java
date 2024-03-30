package com.myproject.reactivemongo.services;

import com.myproject.reactivemongo.model.CustomerDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerService {

    Flux<CustomerDTO> listCustomers();
    Mono<CustomerDTO> getCustomerById(String customerId);
    Flux<CustomerDTO> findByCustomerName(String customerName);
    Mono<CustomerDTO> saveCustomer(Mono<CustomerDTO> customerDTO);
    Mono<CustomerDTO> saveCustomer(CustomerDTO customerDTO);
    Mono<CustomerDTO> updateCustomer(String customerId, CustomerDTO customerDTO);
    Mono<CustomerDTO> patchCustomer(String customerId, CustomerDTO customerDTO);
    Mono<Void> deleteCustomerById(String customerId);
}
