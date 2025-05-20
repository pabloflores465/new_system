package com.example.satsimulationsystem.backend.modules.invoicing.adapters.driving.api.dto;

import com.example.satsimulationsystem.backend.modules.invoicing.domain.ModuleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {

    @NotBlank(message = "Product or service name must not be blank")
    private String productNameOrService;

    @NotNull(message = "Unit cost must not be null")
    @Positive(message = "Unit cost must be positive")
    private Double unitCost; // Cost before this system's VAT, but including profit margin

    @NotNull(message = "Quantity must not be null")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    @NotNull(message = "Module type must be specified")
    private ModuleType moduleType; // PHARMACY, HOSPITAL, INSURANCE

    // Optional: Category for the item (e.g., hospital service type, medication category)
    private String category;

    // Fields for response (calculated values)
    private Double itemSubtotal;
    private Double taxApplied;
    private Double itemTotal;
} 