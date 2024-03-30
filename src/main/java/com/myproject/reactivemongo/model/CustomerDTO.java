package com.myproject.reactivemongo.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDTO {

    private String id;

    @NotNull
    @Size(min = 3, max = 255)
    private String customerName;
    private Instant createdDate;
    private Instant lastModifiedDate;
}