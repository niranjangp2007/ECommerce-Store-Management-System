package com.ecommerce.dao;

import com.ecommerce.database.DBConnection;
import com.ecommerce.reports.ReportResult;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC implementation of ReportDAO executing the 15 evaluation queries.
 */
public class ReportDAOImpl implements ReportDAO {

    @FunctionalInterface
    public interface ConnectionSupplier {
        Connection getConnection() throws SQLException;
    }

    private final ConnectionSupplier connectionSupplier;

    private static final Map<Integer, ReportMeta> METAS = new LinkedHashMap<>();

    public ReportDAOImpl() {
        this(DBConnection::getConnection);
    }

    public ReportDAOImpl(ConnectionSupplier connectionSupplier) {
        this.connectionSupplier = connectionSupplier != null ? connectionSupplier : DBConnection::getConnection;
    }

    static {
        register(1, "Electronics Products", "Easy - 5 pts",
                "List the product id, name and price of all products in the Electronics category.",
                "SELECT product_id, name, price FROM products WHERE category = 'Electronics'");

        register(2, "Chennai Customers", "Easy - 5 pts",
                "Show the name and city of all customers who live in Chennai.",
                "SELECT name, city FROM customers WHERE city = 'Chennai'");

        register(3, "Products Above 500", "Easy - 5 pts",
                "List products priced above 500, most expensive first.",
                "SELECT name, price FROM products WHERE price > 500 ORDER BY price DESC");

        register(4, "Out of Stock Products", "Easy - 5 pts",
                "Find the products that are out of stock.",
                "SELECT name, category FROM products WHERE stock = 0");

        register(5, "Orders Grouped by Status", "Easy - 5 pts",
                "Count the number of orders in each status.",
                "SELECT status, COUNT(*) AS total_orders FROM orders GROUP BY status ORDER BY status");

        register(6, "Delivered Orders", "Easy - 5 pts",
                "Show the order id, customer name and order date for all Delivered orders.",
                "SELECT o.order_id, c.name AS customer, o.order_date " +
                "FROM orders o JOIN customers c ON o.customer_id = c.customer_id " +
                "WHERE o.status = 'Delivered' ORDER BY o.order_id");

        register(7, "Orders in Date Range", "Easy - 5 pts",
                "List the orders placed between 2026-09-10 and 2026-09-20 (both inclusive).",
                "SELECT order_id, customer_id, order_date, status FROM orders " +
                "WHERE order_date BETWEEN '2026-09-10' AND '2026-09-20' ORDER BY order_date");

        register(8, "Home & Stationery Products", "Easy - 5 pts",
                "List the products that belong to the Home or Stationery category, sorted by category and then name.",
                "SELECT name, category FROM products WHERE category IN ('Home','Stationery') ORDER BY category, name");

        register(9, "Order Items with Line Totals", "Medium - 10 pts",
                "For every order item, show the order id, product name, quantity, unit price and line total (quantity x price).",
                "SELECT oi.order_id, p.name AS product, oi.quantity, p.price, oi.quantity * p.price AS line_total " +
                "FROM order_items oi JOIN products p ON oi.product_id = p.product_id ORDER BY oi.order_id, oi.item_id");

        register(10, "Total Value of Each Order", "Medium - 10 pts",
                "Calculate the total value of each order along with its status.",
                "SELECT o.order_id, o.status, SUM(oi.quantity * p.price) AS order_total " +
                "FROM orders o JOIN order_items oi ON o.order_id = oi.order_id " +
                "JOIN products p ON oi.product_id = p.product_id GROUP BY o.order_id, o.status ORDER BY o.order_id");

        register(11, "Revenue by Category", "Medium - 10 pts",
                "Find the total revenue per product category from Delivered orders, highest first.",
                "SELECT p.category, SUM(oi.quantity * p.price) AS revenue " +
                "FROM orders o JOIN order_items oi ON o.order_id = oi.order_id " +
                "JOIN products p ON oi.product_id = p.product_id " +
                "WHERE o.status = 'Delivered' GROUP BY p.category ORDER BY revenue DESC");

        register(12, "Top 3 Customers by Spend", "Medium - 10 pts",
                "Find the top 3 customers by total spend on Delivered orders.",
                "SELECT c.name, SUM(oi.quantity * p.price) AS total_spent " +
                "FROM customers c JOIN orders o ON c.customer_id = o.customer_id " +
                "JOIN order_items oi ON o.order_id = oi.order_id " +
                "JOIN products p ON oi.product_id = p.product_id " +
                "WHERE o.status = 'Delivered' GROUP BY c.customer_id, c.name ORDER BY total_spent DESC LIMIT 3");

        register(13, "Never Ordered Products", "Medium - 10 pts",
                "Find the products that have never been ordered.",
                "SELECT p.name FROM products p LEFT JOIN order_items oi ON p.product_id = oi.product_id " +
                "WHERE oi.item_id IS NULL");

        register(14, "Repeat Customers", "Medium - 10 pts",
                "Find the customers who have placed more than one order, with their order count.",
                "SELECT c.name, COUNT(*) AS order_count FROM customers c " +
                "JOIN orders o ON c.customer_id = o.customer_id " +
                "GROUP BY c.customer_id, c.name HAVING COUNT(*) > 1 ORDER BY order_count DESC, c.name");

        register(15, "Units Sold per Product", "Medium - 10 pts",
                "Show the total units sold per product, ignoring Cancelled orders. Products with no sales must appear with 0.",
                "SELECT p.name, COALESCE(SUM(CASE WHEN o.status <> 'Cancelled' THEN oi.quantity END), 0) AS units_sold " +
                "FROM products p LEFT JOIN order_items oi ON p.product_id = oi.product_id " +
                "LEFT JOIN orders o ON oi.order_id = o.order_id " +
                "GROUP BY p.product_id, p.name ORDER BY units_sold DESC, p.name");
    }

    private static void register(int num, String title, String difficulty, String question, String sql) {
        METAS.put(num, new ReportMeta(num, title, difficulty, question, sql));
    }

    @Override
    public List<ReportMeta> getAllReportsMeta() {
        return Collections.unmodifiableList(new ArrayList<>(METAS.values()));
    }

    @Override
    public ReportResult executeReport(int questionNumber) throws SQLException {
        ReportMeta meta = METAS.get(questionNumber);
        if (meta == null) {
            throw new IllegalArgumentException("Unknown question number: " + questionNumber);
        }
        return runQuery(meta);
    }

    private ReportResult runQuery(ReportMeta meta) throws SQLException {
        List<String> colNames = new ArrayList<>();
        List<List<Object>> rows = new ArrayList<>();

        try (Connection conn = connectionSupplier.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(meta.getSql())) {

            ResultSetMetaData rsmd = rs.getMetaData();
            int colCount = rsmd.getColumnCount();
            for (int i = 1; i <= colCount; i++) {
                colNames.add(rsmd.getColumnLabel(i));
            }

            while (rs.next()) {
                List<Object> row = new ArrayList<>();
                for (int i = 1; i <= colCount; i++) {
                    row.add(rs.getObject(i));
                }
                rows.add(row);
            }
        }

        return new ReportResult(meta.getNumber(), meta.getTitle(), meta.getDifficulty(),
                meta.getQuestion(), meta.getSql(), colNames, rows);
    }

    @Override public ReportResult getQ1ElectronicsProducts() throws SQLException { return executeReport(1); }
    @Override public ReportResult getQ2ChennaiCustomers() throws SQLException { return executeReport(2); }
    @Override public ReportResult getQ3ProductsAbove500() throws SQLException { return executeReport(3); }
    @Override public ReportResult getQ4OutOfStockProducts() throws SQLException { return executeReport(4); }
    @Override public ReportResult getQ5OrdersByStatus() throws SQLException { return executeReport(5); }
    @Override public ReportResult getQ6DeliveredOrders() throws SQLException { return executeReport(6); }
    @Override public ReportResult getQ7OrdersBetweenDates() throws SQLException { return executeReport(7); }
    @Override public ReportResult getQ8HomeAndStationery() throws SQLException { return executeReport(8); }
    @Override public ReportResult getQ9OrderItemsLineTotal() throws SQLException { return executeReport(9); }
    @Override public ReportResult getQ10OrderTotalValue() throws SQLException { return executeReport(10); }
    @Override public ReportResult getQ11DeliveredCategoryRevenue() throws SQLException { return executeReport(11); }
    @Override public ReportResult getQ12Top3DeliveredCustomers() throws SQLException { return executeReport(12); }
    @Override public ReportResult getQ13NeverOrderedProducts() throws SQLException { return executeReport(13); }
    @Override public ReportResult getQ14RepeatCustomers() throws SQLException { return executeReport(14); }
    @Override public ReportResult getQ15UnitsSoldPerProduct() throws SQLException { return executeReport(15); }
}
