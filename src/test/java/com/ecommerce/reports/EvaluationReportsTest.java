package com.ecommerce.reports;

import com.ecommerce.dao.ReportDAO;
import com.ecommerce.dao.ReportDAOImpl;
import com.ecommerce.database.DBConnection;
import com.ecommerce.service.ProductService;
import com.ecommerce.service.ReportService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("All 15 SQL Academic Evaluation Queries Test")
public class EvaluationReportsTest {

    private final ReportService reportService = new ReportService();

    private static final String TEST_DB_NAME = "ecom_eval_test";
    private static String testDbUrl;
    private static boolean isolatedDbAvailable = false;

    @BeforeAll
    static void setUpIsolatedFixture() {
        if (!DBConnection.testConnection()) {
            return;
        }

        try {
            // Attempt to create isolated test database to protect live customer/order data
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + TEST_DB_NAME);
            }

            String mainUrl = DBConnection.getDatabaseUrl();
            if (mainUrl.contains("/" + DBConnection.DEFAULT_DATABASE_NAME)) {
                testDbUrl = mainUrl.replace("/" + DBConnection.DEFAULT_DATABASE_NAME, "/" + TEST_DB_NAME);
            } else {
                testDbUrl = "jdbc:mysql://localhost:3306/" + TEST_DB_NAME + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            }

            try (Connection testConn = DBConnection.getConnection(testDbUrl);
                 Statement stmt = testConn.createStatement()) {

                // Create isolated schema
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS customers (" +
                        "customer_id INT PRIMARY KEY, " +
                        "name VARCHAR(50) NOT NULL, " +
                        "city VARCHAR(50) NOT NULL, " +
                        "join_date DATE NOT NULL)");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS products (" +
                        "product_id INT PRIMARY KEY, " +
                        "name VARCHAR(50) NOT NULL, " +
                        "category VARCHAR(30) NOT NULL, " +
                        "price DECIMAL(8,2) NOT NULL, " +
                        "stock INT NOT NULL)");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS orders (" +
                        "order_id INT PRIMARY KEY, " +
                        "customer_id INT NOT NULL, " +
                        "order_date DATE NOT NULL, " +
                        "status VARCHAR(20) NOT NULL, " +
                        "FOREIGN KEY (customer_id) REFERENCES customers(customer_id))");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS order_items (" +
                        "item_id INT PRIMARY KEY, " +
                        "order_id INT NOT NULL, " +
                        "product_id INT NOT NULL, " +
                        "quantity INT NOT NULL, " +
                        "FOREIGN KEY (order_id) REFERENCES orders(order_id), " +
                        "FOREIGN KEY (product_id) REFERENCES products(product_id))");

                // Clean existing test data in reverse FK order
                stmt.executeUpdate("DELETE FROM order_items");
                stmt.executeUpdate("DELETE FROM orders");
                stmt.executeUpdate("DELETE FROM products");
                stmt.executeUpdate("DELETE FROM customers");

                // Seed exact canonical academic evaluation baseline
                stmt.executeUpdate("INSERT INTO customers (customer_id, name, city, join_date) VALUES " +
                        "(1, 'Anita', 'Chennai', '2025-11-10'), " +
                        "(2, 'Bharath', 'Bangalore', '2026-01-15'), " +
                        "(3, 'Cathy', 'Mumbai', '2026-02-20'), " +
                        "(4, 'Dinesh', 'Chennai', '2026-03-05'), " +
                        "(5, 'Esha', 'Delhi', '2026-04-12')");

                stmt.executeUpdate("INSERT INTO products (product_id, name, category, price, stock) VALUES " +
                        "(1, 'Wireless Mouse', 'Electronics', 599.00, 50), " +
                        "(2, 'Keyboard', 'Electronics', 1299.00, 30), " +
                        "(3, 'Notebook', 'Stationery', 120.00, 200), " +
                        "(4, 'Water Bottle', 'Home', 350.00, 80), " +
                        "(5, 'Desk Lamp', 'Home', 899.00, 0), " +
                        "(6, 'Pen Pack', 'Stationery', 80.00, 500), " +
                        "(7, 'Backpack', 'Bags', 1499.00, 25)");

                stmt.executeUpdate("INSERT INTO orders (order_id, customer_id, order_date, status) VALUES " +
                        "(1001, 1, '2026-09-01', 'Delivered'), " +
                        "(1002, 1, '2026-09-05', 'Shipped'), " +
                        "(1003, 2, '2026-09-06', 'Delivered'), " +
                        "(1004, 3, '2026-09-10', 'Cancelled'), " +
                        "(1005, 4, '2026-09-12', 'Delivered'), " +
                        "(1006, 2, '2026-09-15', 'Delivered'), " +
                        "(1007, 3, '2026-09-20', 'Pending')");

                stmt.executeUpdate("INSERT INTO order_items (item_id, order_id, product_id, quantity) VALUES " +
                        "(1, 1001, 1, 2), " +
                        "(2, 1001, 3, 5), " +
                        "(3, 1002, 2, 1), " +
                        "(4, 1003, 4, 3), " +
                        "(5, 1003, 6, 10), " +
                        "(6, 1004, 1, 1), " +
                        "(7, 1005, 3, 10), " +
                        "(8, 1005, 5, 1), " +
                        "(9, 1006, 2, 1), " +
                        "(10, 1006, 1, 1), " +
                        "(11, 1007, 4, 2)");
            }

            isolatedDbAvailable = true;
        } catch (SQLException e) {
            System.err.println("Warning: Unable to initialize isolated test database " + TEST_DB_NAME + ": " + e.getMessage());
            isolatedDbAvailable = false;
        }
    }

    @AfterAll
    static void tearDownIsolatedFixture() {
        if (isolatedDbAvailable) {
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DROP DATABASE IF EXISTS " + TEST_DB_NAME);
            } catch (SQLException ignored) {
            }
        }
    }

    @Test
    @DisplayName("Metadata Check: All 15 Questions (Q1 - Q15) are properly configured")
    void testAll15ReportsRegistered() {
        List<ReportDAO.ReportMeta> metas = reportService.getAllReportsMeta();
        assertNotNull(metas);
        assertEquals(15, metas.size(), "All 15 evaluation reports must be registered.");

        for (int i = 1; i <= 15; i++) {
            ReportDAO.ReportMeta m = metas.get(i - 1);
            assertEquals(i, m.getNumber(), "Report index must match question number.");
            assertNotNull(m.getTitle(), "Title must not be null.");
            assertNotNull(m.getDifficulty(), "Difficulty must not be null.");
            assertNotNull(m.getQuestion(), "Question prompt must not be null.");
            assertNotNull(m.getSql(), "SQL query must not be null.");
            assertFalse(m.getSql().trim().isEmpty(), "SQL query must not be empty.");
        }
    }

    @Test
    @DisplayName("Database Live: Run and assert all 15 evaluation queries against MySQL ecom_db")
    void testExecuteAll15QueriesAgainstLiveDb() throws SQLException {
        if (!DBConnection.testConnection()) {
            System.out.println("Skipping live 15-report evaluation: Database connection not active in test runner environment.");
            return;
        }

        ReportService activeReportService;
        if (isolatedDbAvailable) {
            activeReportService = new ReportService(new ReportDAOImpl(() -> DBConnection.getConnection(testDbUrl)));
        } else {
            activeReportService = reportService;
        }

        // Q1: Electronics (Expected 2 rows: Wireless Mouse, Keyboard)
        ReportResult q1 = activeReportService.executeReport(1);
        if (isolatedDbAvailable) {
            assertEquals(2, q1.getRowCount(), "Q1 must return exactly 2 Electronics products.");
        } else {
            assertTrue(q1.getRowCount() >= 2, "Q1 must return at least 2 Electronics products.");
        }

        // Q2: Chennai customers (Expected 2 rows: Anita, Dinesh)
        ReportResult q2 = activeReportService.executeReport(2);
        if (isolatedDbAvailable) {
            assertEquals(2, q2.getRowCount(), "Q2 must return 2 customers from Chennai.");
        } else {
            assertTrue(q2.getRowCount() >= 2, "Q2 must return at least 2 customers from Chennai.");
        }

        // Q3: Products above 500 (Expected 4 rows: Backpack, Keyboard, Desk Lamp, Wireless Mouse)
        ReportResult q3 = activeReportService.executeReport(3);
        if (isolatedDbAvailable) {
            assertEquals(4, q3.getRowCount(), "Q3 must return 4 products priced above 500.");
        } else {
            assertTrue(q3.getRowCount() >= 4, "Q3 must return at least 4 products priced above 500.");
        }

        // Q4: Out of stock (Expected 1 row: Desk Lamp in canonical baseline)
        ReportResult q4 = activeReportService.executeReport(4);
        if (isolatedDbAvailable) {
            assertEquals(1, q4.getRowCount(), "Q4 must return 1 out-of-stock product in canonical dataset.");
            assertEquals("Desk Lamp", q4.getValueAt(0, 0));
        } else {
            int actualOutOfStock = new ProductService().getOutOfStockCount();
            assertEquals(actualOutOfStock, q4.getRowCount(), "Q4 must match the actual out-of-stock count.");
        }

        // Q5: Orders count by status (Expected 4 rows: Cancelled, Delivered, Pending, Shipped)
        ReportResult q5 = activeReportService.executeReport(5);
        if (isolatedDbAvailable) {
            assertEquals(4, q5.getRowCount(), "Q5 must group orders into 4 distinct statuses.");
        } else {
            assertTrue(q5.getRowCount() >= 1, "Q5 must group orders into statuses.");
        }

        // Q6: Delivered orders (Expected 4 rows: 1001, 1003, 1005, 1006)
        ReportResult q6 = activeReportService.executeReport(6);
        if (isolatedDbAvailable) {
            assertEquals(4, q6.getRowCount(), "Q6 must return 4 Delivered orders.");
        } else {
            assertTrue(q6.getRowCount() >= 1, "Q6 must return Delivered orders.");
        }

        // Q7: Orders between 2026-09-10 and 2026-09-20 (Expected 4 rows: 1004, 1005, 1006, 1007)
        ReportResult q7 = activeReportService.executeReport(7);
        if (isolatedDbAvailable) {
            assertEquals(4, q7.getRowCount(), "Q7 must return 4 orders within date range.");
        } else {
            assertTrue(q7.getRowCount() >= 1, "Q7 must return orders within date range.");
        }

        // Q8: Home or Stationery (Expected 4 rows: Desk Lamp, Water Bottle, Notebook, Pen Pack)
        ReportResult q8 = activeReportService.executeReport(8);
        if (isolatedDbAvailable) {
            assertEquals(4, q8.getRowCount(), "Q8 must return 4 products in Home/Stationery.");
        } else {
            assertTrue(q8.getRowCount() >= 4, "Q8 must return at least 4 products in Home/Stationery.");
        }

        // Q9: Order items with line totals (Expected 11 rows)
        ReportResult q9 = activeReportService.executeReport(9);
        if (isolatedDbAvailable) {
            assertEquals(11, q9.getRowCount(), "Q9 must return all 11 order items with line totals.");
        } else {
            assertTrue(q9.getRowCount() >= 11, "Q9 must return order items with line totals.");
        }

        // Q10: Total value of each order (Expected 7 rows: orders 1001 to 1007)
        ReportResult q10 = activeReportService.executeReport(10);
        if (isolatedDbAvailable) {
            assertEquals(7, q10.getRowCount(), "Q10 must calculate totals for all 7 orders.");
        } else {
            assertTrue(q10.getRowCount() >= 1, "Q10 must calculate order totals.");
        }

        // Q11: Revenue per category from Delivered orders (Expected 3 rows: Electronics, Stationery, Home)
        ReportResult q11 = activeReportService.executeReport(11);
        if (isolatedDbAvailable) {
            assertEquals(3, q11.getRowCount(), "Q11 must return revenue for 3 categories.");
        } else {
            assertTrue(q11.getRowCount() >= 1, "Q11 must return revenue per category.");
        }

        // Q12: Top 3 customers by spend on Delivered orders (Expected 3 rows: Bharath, Dinesh, Anita)
        ReportResult q12 = activeReportService.executeReport(12);
        if (isolatedDbAvailable) {
            assertEquals(3, q12.getRowCount(), "Q12 must return top 3 customers.");
            assertEquals("Bharath", q12.getValueAt(0, 0));
        } else {
            assertTrue(q12.getRowCount() >= 1, "Q12 must return top customers.");
        }

        // Q13: Products never ordered (Expected 1 row: Backpack)
        ReportResult q13 = activeReportService.executeReport(13);
        if (isolatedDbAvailable) {
            assertEquals(1, q13.getRowCount(), "Q13 must return 1 unordered product.");
            assertEquals("Backpack", q13.getValueAt(0, 0));
        } else {
            assertNotNull(q13.getRows(), "Q13 rows list must not be null.");
        }

        // Q14: Customers with >1 order (Expected 3 rows: Anita, Bharath, Cathy)
        ReportResult q14 = activeReportService.executeReport(14);
        if (isolatedDbAvailable) {
            assertEquals(3, q14.getRowCount(), "Q14 must return 3 customers with multiple orders.");
        } else {
            assertNotNull(q14.getRows(), "Q14 rows list must not be null.");
        }

        // Q15: Units sold per product ignoring Cancelled (Expected 7 rows, Backpack with 0)
        ReportResult q15 = activeReportService.executeReport(15);
        if (isolatedDbAvailable) {
            assertEquals(7, q15.getRowCount(), "Q15 must list all 7 products with units sold.");
        } else {
            assertTrue(q15.getRowCount() >= 7, "Q15 must list products with units sold.");
        }
    }
}
