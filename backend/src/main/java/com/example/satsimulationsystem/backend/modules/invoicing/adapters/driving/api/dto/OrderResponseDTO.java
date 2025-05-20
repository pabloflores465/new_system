package com.example.satsimulationsystem.backend.modules.invoicing.adapters.driving.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {

    private Long orderId;
    private String clientName;
    private String clientNit;
    private String clientAddress;
    private String providerName;
    private List<OrderItemDTO> items; // Each OrderItemDTO here will have calculated fields populated
    private Double totalAmount;
    private Double totalTaxes;
    private String invoicePdfUrl;
    private LocalDateTime orderDate;
    private String createdByUsername; // Username of the module user who placed the order

} 