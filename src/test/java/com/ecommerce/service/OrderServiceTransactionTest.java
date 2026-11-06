package com.ecommerce.service;

import com.ecommerce.database.DBConnection;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderService Transaction & Stock Atomicity Test")
public class OrderServiceTransactionTest {

    private final OrderService orderService = new OrderService();
    private final ProductService productService = new ProductService();

    @Test
    @DisplayName("Validation: Rejects empty order items list")
    void testEmptyOrderRejection() {
        assertThrows(DatabaseOperationException.class, () ->
                orderService.createOrder(1, new ArrayList<>()));
        assertThrows(DatabaseOperationException.class, () ->
                orderService.createOrder(1, null));
    }

    @Test
    @DisplayName("Transaction Atomicity: Insufficient stock throws InsufficientStockException and rolls back completely")
    void testInsufficientStockRollback() throws SQLException {
        if (!DBConnection.testConnection()) {
            System.out.println("Skipping live transaction test: Database connection not active in test runner environment.");
            return;
        }

        // Dynamically select an existing product from the database
        List<Product> products = productService.getAllProducts();
        assertNotNull(products);
        assertFalse(products.isEmpty(), "At least one product must exist in the database to test stock validation.");
        Product targetProduct = products.get(0);
        int targetProductId = targetProduct.getProductId();

        // Capture BEFORE state
        int initialStock = targetProduct.getStock();
        int initialOrderCount = orderService.getOrderCount();
        int initialOrderItemCount = getOrderItemCount();

        // Formulate an order item that strictly exceeds available inventory (initialStock + 10)
        int excessiveQuantity = Math.max(1, initialStock + 10);
        List<OrderItem> cart = new ArrayList<>();
        cart.add(new OrderItem(0, 0, targetProductId, excessiveQuantity));

        // Attempting to create an order must throw InsufficientStockException
        InsufficientStockException thrown = assertThrows(InsufficientStockException.class, () ->
                orderService.createOrder(1, cart),
                "OrderService must reject orders exceeding available inventory with InsufficientStockException.");
        assertEquals(targetProductId, thrown.getProductId(), "Exception must reference the offending product ID.");

        // Capture AFTER state and verify complete transaction rollback & atomicity
        int orderCountAfter = orderService.getOrderCount();
        assertEquals(initialOrderCount, orderCountAfter, "Order count must remain unchanged after failed transaction.");

        int orderItemCountAfter = getOrderItemCount();
        assertEquals(initialOrderItemCount, orderItemCountAfter, "Order items count must remain unchanged after failed transaction.");

        Product productAfter = productService.getProductById(targetProductId);
        assertNotNull(productAfter);
        assertEquals(initialStock, productAfter.getStock(), "Product stock must remain unchanged after failed transaction.");
    }

    private int getOrderItemCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM order_items";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
