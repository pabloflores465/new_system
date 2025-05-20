package com.example.satsimulationsystem.backend.modules.invoicing.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String productNameOrService;

    @NotNull
    @Positive
    private Double unitCost; // Cost before this system's VAT, but including profit margin as per requirement

    @NotNull
    @Positive
    private Integer quantity; // Assuming quantity, defaults to 1 if not applicable for service

    @NotNull
    private Double itemSubtotal; // unitCost * quantity

    @NotNull
    private Double taxApplied; // VAT calculated by this system

    @NotNull
    private Double itemTotal; // itemSubtotal + taxApplied

    @Enumerated(EnumType.STRING)
    @NotNull
    private ModuleType moduleType; // PHARMACY, HOSPITAL, INSURANCE

    private String category; // For hospital service type or medication category reports

    // Profit percentage and tax burden from original module are part of 'unitCost' as per requirement:
    // "listado de productos/servicio con el costo final de cada producto (incluyendo costo, carga tributaria y porcentaje de ganancia)"
    // This 'unitCost' is the base for our system's VAT calculation.
} 