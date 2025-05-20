package com.example.satsimulationsystem.backend.modules.invoicing.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.example.satsimulationsystem.backend.modules.users.domain.User;
import java.time.LocalDateTime;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find orders by the user who created them (for module-specific views or reports)
    List<Order> findByCreatedBy(User user);

    // Find orders by client NIT (for Tax Report by NIT)
    List<Order> findByClientNit(String clientNit);

    // Find orders within a date range (for filtered reports)
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find orders by client NIT and date range
    List<Order> findByClientNitAndOrderDateBetween(String clientNit, LocalDateTime startDate, LocalDateTime endDate);

    // Find orders by creator's role and date range
    List<Order> findByCreatedByRoleAndOrderDateBetween(String role, LocalDateTime startDate, LocalDateTime endDate);

    // Find orders by creator's role
    List<Order> findByCreatedByRole(String role);

    // Find orders by provider name and optional date range
    List<Order> findByProviderName(String providerName);
    List<Order> findByProviderNameAndOrderDateBetween(String providerName, LocalDateTime startDate, LocalDateTime endDate);

    // Find orders by item category and optional date range
    // Note: This queries orders that contain AT LEAST ONE item with the specified category.
    List<Order> findDistinctByItems_Category(String category);
    List<Order> findDistinctByItems_CategoryAndOrderDateBetween(String category, LocalDateTime startDate, LocalDateTime endDate);

    // Additional methods for other report types can be added here later
    // e.g., if we need to query based on data within OrderItems, more complex queries might be needed.
} 