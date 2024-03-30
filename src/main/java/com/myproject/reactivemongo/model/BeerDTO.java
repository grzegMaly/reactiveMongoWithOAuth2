package com.myproject.reactivemongo.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeerDTO {

    private String id;

    @NotNull
    @Size(min = 3, max = 255)
    private String beerName;

    @Size(min = 3, max = 255)
    private String beerStyle;

    @NotNull
    @Size(min = 3, max = 255)
    private String upc;
    private Integer quantityOnHand;
    private BigDecimal price;

    @CreatedDate
    private Instant createdDate;

    @LastModifiedDate
    private Instant lastModifiedDate;
}