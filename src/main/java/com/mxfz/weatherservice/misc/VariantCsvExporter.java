package com.mxfz.weatherservice.misc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.mxfz.weatherservice.model.shopify.ProductsCatalog;
import com.mxfz.weatherservice.model.shopify.Product;
import com.mxfz.weatherservice.model.shopify.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class VariantCsvExporter {

    private static final Logger log = LoggerFactory.getLogger(VariantCsvExporter.class);

    private final ObjectMapper objectMapper;
    private final CsvMapper csvMapper = new CsvMapper();

    @Value("classpath:merged.json")
    private Resource mergedJsonResource;

    @Value("${merged.json.path:}")
    private String mergedJsonPathOverride;

    @Value("${variant.csv.output:variants.csv}")
    private String outputCsv;

    public VariantCsvExporter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void exportVariantsCsv() {
        try {
            ProductsCatalog catalog = objectMapper.readValue(openMergedJson(), ProductsCatalog.class);

            List<Row> rows = new ArrayList<>();
            List<Product> products = catalog.getProducts();
            if (products != null) {
                // Build well-formatted counts summary
                StringBuilder counts = new StringBuilder();
                counts.append("\n===============================\n")
                      .append("PRODUCT/VARIANT COUNT SUMMARY\n")
                      .append("===============================\n")
                      .append("Total products: ").append(products.size()).append('\n')
                      .append("Per-product variant counts:\n");
                for (Product product : products) {
                    List<Variant> variants = product.getVariants();
                    if (variants == null) {
                        counts.append("- Product ID ").append(product.getId()).append(": 0\n");
                        continue;
                    }
                    counts.append("- Product ID ").append(product.getId()).append(": ").append(variants.size()).append("\n");
                    for (Variant variant : variants) {
                        Row row = new Row();
                        row.setProductId(product.getId());
                        row.setSku(variant.getSku());
                        row.setInventoryItemId(variant.getInventoryItemId());
                        rows.add(row);
                    }
                }
                counts.append("--------------------------------\n");
                log.info(counts.toString());
            }

            CsvSchema schema = CsvSchema.builder()
                    .addColumn("productId")
                    .addColumn("sku")
                    .addColumn("inventoryItemId")
                    .setUseHeader(true)
                    .build();

            Path outputPath = Path.of(outputCsv);
            Files.createDirectories(outputPath.toAbsolutePath().getParent() == null ? Path.of(".") : outputPath.toAbsolutePath().getParent());
            csvMapper.writer(schema).writeValue(outputPath.toFile(), rows);

            log.info("Variant CSV exported to {}", outputPath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to export variants CSV", e);
        }
    }

    private java.io.InputStream openMergedJson() throws IOException {
        // Highest priority: explicit path override via property
        if (mergedJsonPathOverride != null && !mergedJsonPathOverride.isBlank()) {
            Path path = Path.of(mergedJsonPathOverride.trim());
            log.info("Reading merged.json from explicit path: {}", path.toAbsolutePath());
            return Files.newInputStream(path);
        }
        // Try classpath first
        try {
            return mergedJsonResource.getInputStream();
        } catch (IOException e) {
            // Fallback to repository-relative path used in this project
            Path fallback = Path.of("src/merged.json");
            log.warn("classpath:merged.json not found. Falling back to {}", fallback.toAbsolutePath());
            return Files.newInputStream(fallback);
        }
    }

    public static class Row {
        private Long productId;
        private String sku;
        private Long inventoryItemId;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public String getSku() {
            return sku;
        }

        public void setSku(String sku) {
            this.sku = sku;
        }

        public Long getInventoryItemId() {
            return inventoryItemId;
        }

        public void setInventoryItemId(Long inventoryItemId) {
            this.inventoryItemId = inventoryItemId;
        }
    }
}

