package com.example.satsimulationsystem.backend.services;

import com.example.satsimulationsystem.backend.modules.invoicing.domain.Order;
import java.io.IOException;

public interface PdfGenerationService {

    /**
     * Generates a PDF invoice for the given order and saves it.
     *
     * @param order The order for which to generate the invoice.
     * @return The file path or a unique identifier for the generated PDF.
     * @throws IOException If there is an error during PDF generation or saving.
     */
    String generateInvoicePdf(Order order) throws IOException;
} 