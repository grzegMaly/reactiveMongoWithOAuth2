package com.myproject.reactivemongo.mappers;

import com.myproject.reactivemongo.domain.Customer;
import com.myproject.reactivemongo.model.CustomerDTO;
import org.mapstruct.Mapper;

@Mapper
public interface CustomerMapper {

    Customer customerDtoToCustomer(CustomerDTO customerDTO);
    CustomerDTO customerToCustomerDto(Customer customer);
}
