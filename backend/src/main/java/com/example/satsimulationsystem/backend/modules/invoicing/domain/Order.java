package com.example.satsimulationsystem.backend.modules.invoicing.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String clientName;

    @NotBlank
    private String clientNit; // NIT - Tax ID

    @NotBlank
    private String clientAddress;

    private String providerName; // For "Reporte de impuestos por Proveedor"

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    @NotEmpty
    private List<OrderItem> items;

    private Double totalAmount; // Total amount including taxes
    private Double totalTaxes;  // Total taxes applied

    private String invoicePdfUrl;

    private LocalDateTime orderDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // The user (module) that placed this order
    private com.example.satsimulationsystem.backend.modules.users.domain.User createdBy;

    @PrePersist
    protected void onCreate() {
        orderDate = LocalDateTime.now();
    }
} 