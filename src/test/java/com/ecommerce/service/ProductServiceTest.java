package com.ecommerce.service;

import com.ecommerce.database.DBConnection;
import com.ecommerce.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductService Validation & Operations Test")
public class ProductServiceTest {

    private final ProductService productService = new ProductService();

    @Test
    @DisplayName("Validation: Rejects negative prices, negative stock, and empty categories")
    void testProductValidation() {
        assertThrows(DatabaseOperationException.class,
                () -> productService.addProduct(999, "Valid", "Electronics", new BigDecimal("-5.00"), 10));

        assertThrows(DatabaseOperationException.class,
                () -> productService.addProduct(999, "Valid", "Electronics", new BigDecimal("10.00"), -1));

        assertThrows(DatabaseOperationException.class,
                () -> productService.addProduct(999, "Valid", "   ", new BigDecimal("10.00"), 5));
    }

    @Test
    @DisplayName("Database Live: Product retrieval and stock query when connected")
    void testProductLiveRetrieval() throws SQLException, DatabaseOperationException {
        if (!DBConnection.testConnection()) {
            System.out.println(
                    "Skipping live product DB assertions: Database connection not active in test runner environment.");
            return;
        }

        List<Product> list = productService.getAllProducts();
        assertNotNull(list);
        assertFalse(list.isEmpty(), "Products dataset must not be empty.");
        assertTrue(list.size() >= 7, "Sample products dataset must contain at least 7 items.");

        // Verify basic product constraints across retrieved products
        for (Product p : list) {
            assertTrue(p.getProductId() > 0, "Product ID must be positive.");
            assertNotNull(p.getName(), "Product name must not be null.");
            assertNotNull(p.getCategory(), "Product category must not be null.");
            assertNotNull(p.getPrice(), "Product price must not be null.");
            assertTrue(p.getStock() >= 0, "Product stock must be non-negative.");
        }

        // Test out-of-stock query behavior deterministically using a controlled test
        // product fixture
        int controlledProductId = 99901;
        // Pre-clean if leftover from any interrupted run
        try {
            productService.deleteProduct(controlledProductId);
        } catch (Exception ignored) {
        }

        try {
            // Add a controlled test product with stock = 0
            productService.addProduct(
                    controlledProductId,
                    "Controlled Test Out-Of-Stock Item",
                    "Testing",
                    new BigDecimal("49.99"),
                    0);

            // Query out-of-stock products
            List<Product> outStock = productService.getOutOfStockProducts();
            assertNotNull(outStock);
            assertFalse(outStock.isEmpty(), "Out-of-stock list must include our newly created zero-stock product.");

            boolean testItemFound = false;
            for (Product p : outStock) {
                assertEquals(0, p.getStock(),
                        "Every product returned by getOutOfStockProducts() must have stock == 0.");
                if (p.getProductId() == controlledProductId) {
                    testItemFound = true;
                    assertEquals("Controlled Test Out-Of-Stock Item", p.getName());
                    assertEquals("Testing", p.getCategory());
                }
            }
            assertTrue(testItemFound,
                    "Controlled zero-stock test product must be returned by getOutOfStockProducts().");
            assertTrue(productService.getOutOfStockCount() >= 1, "getOutOfStockCount() must be >= 1.");

        } finally {
            // Clean up controlled test fixture
            try {
                productService.deleteProduct(controlledProductId);
            } catch (Exception ignored) {
            }
            // Verify cleanup completed
            assertNull(productService.getProductById(controlledProductId),
                    "Controlled test product must be deleted after test.");
        }
    }
}
