package com.example.satsimulationsystem.backend.modules.invoicing.application;

import com.example.satsimulationsystem.backend.modules.invoicing.domain.Order;
import com.example.satsimulationsystem.backend.modules.invoicing.domain.OrderItem;
import com.example.satsimulationsystem.backend.modules.invoicing.domain.OrderRepository;
import com.example.satsimulationsystem.backend.modules.users.domain.User;
import com.example.satsimulationsystem.backend.services.PdfGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class InvoicingServiceImpl implements InvoicingService {

    private final OrderRepository orderRepository;
    private final PdfGenerationService pdfGenerationService;

    @Autowired
    public InvoicingServiceImpl(OrderRepository orderRepository, PdfGenerationService pdfGenerationService) {
        this.orderRepository = orderRepository;
        this.pdfGenerationService = pdfGenerationService;
    }

    @Override
    @Transactional
    public Order createOrderAndCalculateTaxes(Order order, User placingUser) {
        order.setCreatedBy(placingUser);
        double totalOrderAmount = 0.0;
        double totalOrderTaxes = 0.0;

        for (OrderItem item : order.getItems()) {
            // Validate item details (e.g., unitCost, quantity should be positive)
            if (item.getUnitCost() == null || item.getUnitCost() <= 0 || item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Invalid item cost or quantity for product: " + item.getProductNameOrService());
            }

            double itemSubtotal = round(item.getUnitCost() * item.getQuantity());
            item.setItemSubtotal(itemSubtotal);

            double taxRate = getTaxRateForModule(item.getModuleType());
            double taxApplied = round(itemSubtotal * taxRate);
            item.setTaxApplied(taxApplied);

            double itemTotal = round(itemSubtotal + taxApplied);
            item.setItemTotal(itemTotal);

            totalOrderAmount += itemTotal;
            totalOrderTaxes += taxApplied;
        }

        order.setTotalAmount(round(totalOrderAmount));
        order.setTotalTaxes(round(totalOrderTaxes));

        // First save the order to get an ID for PDF naming, if ID is generated on save
        Order savedOrder = orderRepository.save(order);

        try {
            String pdfPath = pdfGenerationService.generateInvoicePdf(savedOrder);
            // The pdfPath from PdfGenerationServiceImpl is like "/invoices/download/fileName.pdf"
            // This should be stored as is, and the controller serving the PDF will use it.
            savedOrder.setInvoicePdfUrl(pdfPath); 
            return orderRepository.save(savedOrder); // Save again to update with PDF URL
        } catch (IOException e) {
            // Log the error, and decide on transaction rollback policy.
            // For now, we'll rethrow as a runtime exception to rollback the transaction.
            // A more sophisticated error handling might try to save the order without a PDF URL
            // or mark it as pending PDF generation.
            System.err.println("Failed to generate or save PDF for order ID: " + savedOrder.getId() + " - " + e.getMessage());
            throw new RuntimeException("Failed to generate invoice PDF after saving order.", e);
        }
    }

    @Override
    @Transactional(readOnly = true) // Reports are typically read-only operations
    public List<Order> getOrdersByNitAndDateRange(String nit, LocalDateTime startDate, LocalDateTime endDate) {
        if (nit == null || nit.isBlank()) {
            // Or throw IllegalArgumentException, depending on desired behavior for blank NIT
            return Collections.emptyList(); 
        }

        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date must be before end date.");
            }
            return orderRepository.findByClientNitAndOrderDateBetween(nit, startDate, endDate);
        } else if (startDate != null) {
            // If only start date is provided, fetch from start date to now (or some logical upper bound)
            // For simplicity, let's assume an open-ended query isn't directly supported by a single repo method
            // or treat as needing both, or fetch all by NIT and filter in service (less efficient).
            // The OrderRepository doesn't have findByClientNitAndOrderDateAfter. For now, require both if one is present.
            // This logic can be refined based on exact reporting needs.
             throw new IllegalArgumentException("If one date is provided, the other must also be provided.");
        } else if (endDate != null) {
             throw new IllegalArgumentException("If one date is provided, the other must also be provided.");
        }else {
            return orderRepository.findByClientNit(nit);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByModuleRoleAndDateRange(String moduleRole, LocalDateTime startDate, LocalDateTime endDate) {
        if (moduleRole == null || moduleRole.isBlank()) {
            return Collections.emptyList();
        }

        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date must be before end date.");
            }
            return orderRepository.findByCreatedByRoleAndOrderDateBetween(moduleRole, startDate, endDate);
        } else if (startDate != null || endDate != null) {
            // If one date is provided, but not the other.
            throw new IllegalArgumentException("Both start date and end date must be provided if filtering by date.");
        } else {
            return orderRepository.findByCreatedByRole(moduleRole);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getGeneralSalesReport(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date must be before end date.");
            }
            return orderRepository.findByOrderDateBetween(startDate, endDate);
        } else if (startDate != null) {
            // If only start date is provided, could fetch all after start date.
            // For now, require both or neither for simplicity with existing repo methods.
            throw new IllegalArgumentException("Both start date and end date must be provided if filtering by date, or neither to get all orders.");
        } else if (endDate != null) {
            throw new IllegalArgumentException("Both start date and end date must be provided if filtering by date, or neither to get all orders.");
        } else {
            // No dates provided, return all orders
            return orderRepository.findAll();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByProviderNameAndDateRange(String providerName, LocalDateTime startDate, LocalDateTime endDate) {
        if (providerName == null || providerName.isBlank()) {
            return Collections.emptyList();
        }
        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date must be before end date.");
            }
            return orderRepository.findByProviderNameAndOrderDateBetween(providerName, startDate, endDate);
        } else if (startDate != null || endDate != null) {
            throw new IllegalArgumentException("Both start date and end date must be provided if filtering by date.");
        } else {
            return orderRepository.findByProviderName(providerName);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByItemCategoryAndDateRange(String category, LocalDateTime startDate, LocalDateTime endDate) {
        if (category == null || category.isBlank()) {
            return Collections.emptyList();
        }
        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date must be before end date.");
            }
            return orderRepository.findDistinctByItems_CategoryAndOrderDateBetween(category, startDate, endDate);
        } else if (startDate != null || endDate != null) {
            throw new IllegalArgumentException("Both start date and end date must be provided if filtering by date.");
        } else {
            return orderRepository.findDistinctByItems_Category(category);
        }
    }

    private double getTaxRateForModule(com.example.satsimulationsystem.backend.modules.invoicing.domain.ModuleType moduleType) {
        switch (moduleType) {
            case PHARMACY:
                return 0.12; // 12%
            case INSURANCE:
                return 0.10; // 10%
            case HOSPITAL:
                return 0.07; // 7%
            default:
                throw new IllegalArgumentException("Unknown module type: " + moduleType);
        }
    }

    private double round(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
} 