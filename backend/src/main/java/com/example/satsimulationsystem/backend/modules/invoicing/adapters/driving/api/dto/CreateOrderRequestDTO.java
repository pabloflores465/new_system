package com.example.satsimulationsystem.backend.modules.invoicing.adapters.driving.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequestDTO {

    @NotBlank(message = "Client name must not be blank")
    private String clientName;

    @NotBlank(message = "Client NIT must not be blank")
    private String clientNit;

    @NotBlank(message = "Client address must not be blank")
    private String clientAddress;

    // Optional: Provider name for the order
    private String providerName;

    @NotEmpty(message = "Order must contain at least one item")
    @NotNull
    @Valid // This ensures nested validation of OrderItemDTOs
    private List<OrderItemDTO> items;
} 