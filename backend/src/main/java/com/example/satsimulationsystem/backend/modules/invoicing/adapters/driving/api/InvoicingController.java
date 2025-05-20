package com.example.satsimulationsystem.backend.modules.invoicing.adapters.driving.api;

import com.example.satsimulationsystem.backend.modules.invoicing.application.InvoicingService;
import com.example.satsimulationsystem.backend.modules.invoicing.domain.Order;
import com.example.satsimulationsystem.backend.modules.invoicing.domain.OrderItem;
import com.example.satsimulationsystem.backend.modules.users.domain.User;
import com.example.satsimulationsystem.backend.modules.users.application.UserService;
import com.example.satsimulationsystem.backend.modules.invoicing.adapters.driving.api.dto.CreateOrderRequestDTO;
import com.example.satsimulationsystem.backend.modules.invoicing.adapters.driving.api.dto.OrderResponseDTO;
import com.example.satsimulationsystem.backend.modules.invoicing.adapters.driving.api.dto.OrderItemDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

// TODO: Define CreateOrderRequestDTO and OrderResponseDTO

@RestController
@RequestMapping("/api/invoicing")
public class InvoicingController {

    private final InvoicingService invoicingService;
    private final UserService userService; // To fetch the User entity for the authenticated principal
    private final String pdfStoragePath; // To resolve PDF file paths

    @Autowired
    public InvoicingController(InvoicingService invoicingService, 
                               UserService userService, 
                               @Value("${app.pdf.storage-path:invoices-pdf}") String pdfStoragePath) {
        this.invoicingService = invoicingService;
        this.userService = userService;
        this.pdfStoragePath = pdfStoragePath;
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderRequestDTO orderRequestDTO, Authentication authentication) {
        String username = authentication.getName();
        User placingUser = userService.getUserByUsername(username)
                .orElseThrow(() -> new SecurityException("Authenticated user not found in the system."));

        try {
            Order orderToProcess = convertToOrderEntity(orderRequestDTO);
            Order processedOrder = invoicingService.createOrderAndCalculateTaxes(orderToProcess, placingUser);
            OrderResponseDTO responseDTO = convertToOrderResponseDTO(processedOrder);
            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Consider a more structured error response
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    private Order convertToOrderEntity(CreateOrderRequestDTO requestDTO) {
        Order order = new Order();
        order.setClientName(requestDTO.getClientName());
        order.setClientNit(requestDTO.getClientNit());
        order.setClientAddress(requestDTO.getClientAddress());
        order.setProviderName(requestDTO.getProviderName());

        if (requestDTO.getItems() != null) {
            order.setItems(requestDTO.getItems().stream().map(itemDTO -> {
                OrderItem item = new OrderItem();
                item.setProductNameOrService(itemDTO.getProductNameOrService());
                item.setUnitCost(itemDTO.getUnitCost());
                item.setQuantity(itemDTO.getQuantity());
                item.setModuleType(itemDTO.getModuleType());
                item.setCategory(itemDTO.getCategory());
                // Calculated fields (subtotal, taxApplied, itemTotal) will be set by the service
                return item;
            }).collect(Collectors.toList()));
        } else {
            order.setItems(new ArrayList<>());
        }
        return order;
    }

    private OrderResponseDTO convertToOrderResponseDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setOrderId(order.getId());
        dto.setClientName(order.getClientName());
        dto.setClientNit(order.getClientNit());
        dto.setClientAddress(order.getClientAddress());
        dto.setProviderName(order.getProviderName());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setTotalTaxes(order.getTotalTaxes());
        dto.setInvoicePdfUrl(order.getInvoicePdfUrl());
        dto.setOrderDate(order.getOrderDate());
        if (order.getCreatedBy() != null) {
            dto.setCreatedByUsername(order.getCreatedBy().getUsername());
        }

        if (order.getItems() != null) {
            dto.setItems(order.getItems().stream().map(itemEntity -> {
                OrderItemDTO itemDTO = new OrderItemDTO();
                itemDTO.setProductNameOrService(itemEntity.getProductNameOrService());
                itemDTO.setUnitCost(itemEntity.getUnitCost()); // This is the original unit cost
                itemDTO.setQuantity(itemEntity.getQuantity());
                itemDTO.setModuleType(itemEntity.getModuleType());
                itemDTO.setCategory(itemEntity.getCategory());
                itemDTO.setItemSubtotal(itemEntity.getItemSubtotal());
                itemDTO.setTaxApplied(itemEntity.getTaxApplied());
                itemDTO.setItemTotal(itemEntity.getItemTotal());
                return itemDTO;
            }).collect(Collectors.toList()));
        } else {
            dto.setItems(new ArrayList<>());
        }
        return dto;
    }

    @GetMapping("/invoices/download/{fileName:.+}")
    public ResponseEntity<InputStreamResource> downloadInvoice(@PathVariable String fileName) {
        Path filePath = Paths.get(pdfStoragePath).resolve(fileName).normalize();
        File file = filePath.toFile();

        if (!file.exists() || !filePath.startsWith(Paths.get(pdfStoragePath).normalize())) {
            // Basic security check to prevent path traversal. Ensure the file is within the storage directory.
            return ResponseEntity.notFound().build();
        }

        try {
            InputStreamResource resource = new InputStreamResource(Files.newInputStream(filePath));

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers.add(HttpHeaders.PRAGMA, "no-cache");
            headers.add(HttpHeaders.EXPIRES, "0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(file.length())
                    .body(resource);
        } catch (IOException e) {
            System.err.println("Error downloading PDF: " + fileName + " - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/reports/by-nit")
    public ResponseEntity<?> getReportByNit(
            @RequestParam String nit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            List<Order> orders = invoicingService.getOrdersByNitAndDateRange(nit, startDate, endDate);
            if (orders.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            List<OrderResponseDTO> responseDTOs = orders.stream()
                                                     .map(this::convertToOrderResponseDTO)
                                                     .collect(Collectors.toList());
            return ResponseEntity.ok(responseDTOs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/reports/by-module")
    public ResponseEntity<?> getReportByModule(
            @RequestParam String moduleRole, // e.g., "MODULE_HOSPITAL", "MODULE_PHARMACY"
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            List<Order> orders = invoicingService.getOrdersByModuleRoleAndDateRange(moduleRole, startDate, endDate);
            if (orders.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            List<OrderResponseDTO> responseDTOs = orders.stream()
                                                     .map(this::convertToOrderResponseDTO)
                                                     .collect(Collectors.toList());
            return ResponseEntity.ok(responseDTOs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/reports/general")
    public ResponseEntity<?> getGeneralSalesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            List<Order> orders = invoicingService.getGeneralSalesReport(startDate, endDate);
            if (orders.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            List<OrderResponseDTO> responseDTOs = orders.stream()
                                                     .map(this::convertToOrderResponseDTO)
                                                     .collect(Collectors.toList());
            return ResponseEntity.ok(responseDTOs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/reports/by-provider")
    public ResponseEntity<?> getReportByProvider(
            @RequestParam String providerName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<Order> orders = invoicingService.getOrdersByProviderNameAndDateRange(providerName, startDate, endDate);
            if (orders.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            List<OrderResponseDTO> responseDTOs = orders.stream()
                                                     .map(this::convertToOrderResponseDTO)
                                                     .collect(Collectors.toList());
            return ResponseEntity.ok(responseDTOs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/reports/by-item-category")
    public ResponseEntity<?> getReportByItemCategory(
            @RequestParam String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<Order> orders = invoicingService.getOrdersByItemCategoryAndDateRange(category, startDate, endDate);
            if (orders.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            List<OrderResponseDTO> responseDTOs = orders.stream()
                                                     .map(this::convertToOrderResponseDTO)
                                                     .collect(Collectors.toList());
            return ResponseEntity.ok(responseDTOs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 