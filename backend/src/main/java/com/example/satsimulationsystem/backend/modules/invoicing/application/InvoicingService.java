package com.example.satsimulationsystem.backend.modules.invoicing.application;

import com.example.satsimulationsystem.backend.modules.invoicing.domain.Order;
import com.example.satsimulationsystem.backend.modules.users.domain.User;

// Define DTOs for request and response if needed, or use domain objects directly for simplicity if internal
// For example, a DTO for creating an order might be beneficial.

public interface InvoicingService {

    /**
     * Creates a new order, calculates taxes, and prepares it for persistence.
     * This method will also eventually trigger PDF generation and URL storage.
     *
     * @param order The Order object containing client details and items. Item costs are pre-SAT-VAT.
     * @param placingUser The user (representing a module) placing the order.
     * @return The processed and saved Order with calculated taxes and (eventually) PDF URL.
     */
    Order createOrderAndCalculateTaxes(Order order, User placingUser);

    /**
     * Retrieves a list of orders for a specific client NIT, optionally filtered by a date range.
     *
     * @param nit The client's NIT.
     * @param startDate Optional start date for the filter (inclusive).
     * @param endDate Optional end date for the filter (inclusive).
     * @return A list of matching orders.
     */
    java.util.List<Order> getOrdersByNitAndDateRange(String nit, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    /**
     * Retrieves a list of orders for a specific module (based on creator's role),
     * optionally filtered by a date range.
     *
     * @param moduleRole The role identifying the module (e.g., "MODULE_HOSPITAL").
     * @param startDate Optional start date for the filter (inclusive).
     * @param endDate Optional end date for the filter (inclusive).
     * @return A list of matching orders.
     */
    java.util.List<Order> getOrdersByModuleRoleAndDateRange(String moduleRole, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    /**
     * Retrieves a general list of all orders, optionally filtered by a date range.
     *
     * @param startDate Optional start date for the filter (inclusive).
     * @param endDate Optional end date for the filter (inclusive).
     * @return A list of matching orders.
     */
    java.util.List<Order> getGeneralSalesReport(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    /**
     * Retrieves a list of orders for a specific provider name, optionally filtered by a date range.
     */
    java.util.List<Order> getOrdersByProviderNameAndDateRange(String providerName, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    /**
     * Retrieves a list of orders containing items of a specific category, optionally filtered by a date range.
     */
    java.util.List<Order> getOrdersByItemCategoryAndDateRange(String category, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    // Other methods for fetching orders, reports etc. will be added here or in a separate ReportService
} 