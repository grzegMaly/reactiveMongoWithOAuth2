package com.myproject.reactivemongo.mappers;

import com.myproject.reactivemongo.domain.Beer;
import com.myproject.reactivemongo.model.BeerDTO;
import org.mapstruct.Mapper;

@Mapper
public interface BeerMapper {

    Beer beerDtoToBeer(BeerDTO beerDTO);
    BeerDTO beerToBeerDto(Beer beer);
}
