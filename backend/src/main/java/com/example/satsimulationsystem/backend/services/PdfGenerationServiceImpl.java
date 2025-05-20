package com.example.satsimulationsystem.backend.services;

import com.example.satsimulationsystem.backend.modules.invoicing.domain.Order;
import com.example.satsimulationsystem.backend.modules.invoicing.domain.OrderItem;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGenerationServiceImpl implements PdfGenerationService {

    private final String pdfStoragePath;

    public PdfGenerationServiceImpl(@Value("${app.pdf.storage-path:invoices-pdf}") String pdfStoragePath) {
        this.pdfStoragePath = pdfStoragePath;
        // Create the directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(this.pdfStoragePath));
        } catch (IOException e) {
            // Handle directory creation failure - log or throw a runtime exception
            System.err.println("Could not create PDF storage directory: " + e.getMessage());
            // Depending on policy, you might want to throw a more specific unchecked exception here
        }
    }

    @Override
    public String generateInvoicePdf(Order order) throws IOException {
        String fileName = "invoice-" + order.getId() + "-" + System.currentTimeMillis() + ".pdf";
        Path filePath = Paths.get(pdfStoragePath, fileName);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            float margin = 50;
            float yStart = page.getMediaBox().getHeight() - margin;
            float yPosition = yStart;
            float leading = 15f; // Line spacing

            // Title
            contentStream.setFont(font, 18);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("SAT Simulation - Invoice");
            contentStream.endText();
            yPosition -= leading * 2;

            // Order Details
            contentStream.setFont(normalFont, 12);
            yPosition = addTextLine(contentStream, normalFont, "Order ID: " + order.getId(), margin, yPosition, leading);
            yPosition = addTextLine(contentStream, normalFont, "Order Date: " + order.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), margin, yPosition, leading);
            yPosition = addTextLine(contentStream, normalFont, "Client: " + order.getClientName(), margin, yPosition, leading);
            yPosition = addTextLine(contentStream, normalFont, "NIT: " + order.getClientNit(), margin, yPosition, leading);
            yPosition = addTextLine(contentStream, normalFont, "Address: " + order.getClientAddress(), margin, yPosition, leading);
            yPosition -= leading; // Extra space

            // Items Header
            yPosition = addTextLine(contentStream, font, String.format("%-40s %10s %10s %10s %10s", "Product/Service", "Qty", "Unit $", "Tax $", "Total $"), margin, yPosition, leading);
            yPosition -= 5; // Small space after header

            // Items
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    String line = String.format("%-40.40s %10d %10.2f %10.2f %10.2f",
                            item.getProductNameOrService(),
                            item.getQuantity(),
                            item.getItemSubtotal() / item.getQuantity(), // Approx unit price before tax
                            item.getTaxApplied(),
                            item.getItemTotal());
                    yPosition = addTextLine(contentStream, normalFont, line, margin, yPosition, leading);
                }
            }
            yPosition -= leading; // Extra space

            // Totals
            yPosition = addTextLine(contentStream, font, String.format("%-60s %10.2f", "Total Taxes:", order.getTotalTaxes()), margin, yPosition, leading);
            yPosition = addTextLine(contentStream, font, String.format("%-60s %10.2f", "Total Amount:", order.getTotalAmount()), margin, yPosition, leading);

            contentStream.close();
            document.save(filePath.toFile());
        }
        // Return a relative path or a specific URL part that can be used to construct a download link
        return "/invoices/download/" + fileName; // This will be part of the URL
    }

    private float addTextLine(PDPageContentStream contentStream, PDType1Font font, String text, float x, float y, float leading) throws IOException {
        contentStream.setFont(font, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
        return y - leading;
    }
} 