package com.ecommerce.service;

import com.ecommerce.dao.CustomerDAO;
import com.ecommerce.dao.CustomerDAOImpl;
import com.ecommerce.dao.OrderDAO;
import com.ecommerce.dao.OrderDAOImpl;
import com.ecommerce.dao.OrderItemDAO;
import com.ecommerce.dao.OrderItemDAOImpl;
import com.ecommerce.dao.ProductDAO;
import com.ecommerce.dao.ProductDAOImpl;
import com.ecommerce.database.DBConnection;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service managing transactional order creation, item addition, stock deduction,
 * and order cancellation with automatic inventory restoration.
 */
public class OrderService {

    private final OrderDAO orderDAO;
    private final OrderItemDAO orderItemDAO;
    private final CustomerDAO customerDAO;
    private final ProductDAO productDAO;

    public OrderService() {
        this(new OrderDAOImpl(), new OrderItemDAOImpl(), new CustomerDAOImpl(), new ProductDAOImpl());
    }

    public OrderService(OrderDAO orderDAO, OrderItemDAO orderItemDAO, CustomerDAO customerDAO, ProductDAO productDAO) {
        this.orderDAO = orderDAO;
        this.orderItemDAO = orderItemDAO;
        this.customerDAO = customerDAO;
        this.productDAO = productDAO;
    }

    public List<Order> getAllOrders() throws SQLException {
        List<Order> orders = orderDAO.getAllOrders();
        for (Order o : orders) {
            o.addAllItems(orderItemDAO.getItemsByOrderId(o.getOrderId()));
        }
        return orders;
    }

    public Order getOrderDetails(int orderId) throws SQLException {
        Order order = orderDAO.getOrderById(orderId);
        if (order != null) {
            order.addAllItems(orderItemDAO.getItemsByOrderId(orderId));
        }
        return order;
    }

    public List<Order> getOrdersByStatus(String status) throws SQLException {
        List<Order> orders = orderDAO.getOrdersByStatus(status);
        for (Order o : orders) {
            o.addAllItems(orderItemDAO.getItemsByOrderId(o.getOrderId()));
        }
        return orders;
    }

    public int getOrderCount() throws SQLException {
        return orderDAO.getOrderCount();
    }

    public Map<String, Integer> getOrderCountsByStatus() throws SQLException {
        return orderDAO.getOrderCountsByStatus();
    }

    /**
     * Atomically creates a multi-product order using a JDBC transaction.
     * Validates available stock for ALL products prior to insertion.
     * If ANY product has insufficient stock, the transaction is rolled back completely.
     *
     * @param customerId existing customer ID
     * @param items      list of order items containing productId and quantity
     * @return fully populated Order object with calculated totals
     */
    public Order createOrder(int customerId, List<OrderItem> items) 
            throws SQLException, InsufficientStockException, DatabaseOperationException {
        if (items == null || items.isEmpty()) {
            throw new DatabaseOperationException("Cannot create an empty order. Please add at least one product.");
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            // 1. Verify customer exists
            String customerSql = "SELECT name FROM customers WHERE customer_id = ?";
            String customerName;
            try (PreparedStatement ps = conn.prepareStatement(customerSql)) {
                ps.setInt(1, customerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new DatabaseOperationException("Customer ID " + customerId + " does not exist.");
                    }
                    customerName = rs.getString("name");
                }
            }

            // 2. Validate stock and retrieve product prices/names
            String productSql = "SELECT name, price, stock FROM products WHERE product_id = ? FOR UPDATE";
            for (OrderItem item : items) {
                if (item.getQuantity() <= 0) {
                    throw new DatabaseOperationException("Invalid item quantity: " + item.getQuantity() + ". Must be greater than 0.");
                }

                try (PreparedStatement ps = conn.prepareStatement(productSql)) {
                    ps.setInt(1, item.getProductId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new DatabaseOperationException("Product ID " + item.getProductId() + " does not exist.");
                        }
                        String productName = rs.getString("name");
                        BigDecimal price = rs.getBigDecimal("price");
                        int currentStock = rs.getInt("stock");

                        if (currentStock < item.getQuantity()) {
                            // Insufficient stock: abort transaction immediately
                            throw new InsufficientStockException(item.getProductId(), productName, currentStock, item.getQuantity());
                        }

                        item.setProductName(productName);
                        item.setUnitPrice(price);
                    }
                }
            }

            // 3. Allocate new Order ID
            int newOrderId;
            String nextOrderSql = "SELECT COALESCE(MAX(order_id), 1000) + 1 AS next_id FROM orders";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(nextOrderSql)) {
                rs.next();
                newOrderId = rs.getInt("next_id");
            }

            // 4. Insert into orders table
            String insertOrderSql = "INSERT INTO orders (order_id, customer_id, order_date, status) VALUES (?, ?, ?, ?)";
            LocalDate today = LocalDate.now();
            String initialStatus = Order.STATUS_PENDING;

            try (PreparedStatement ps = conn.prepareStatement(insertOrderSql)) {
                ps.setInt(1, newOrderId);
                ps.setInt(2, customerId);
                ps.setDate(3, Date.valueOf(today));
                ps.setString(4, initialStatus);
                ps.executeUpdate();
            }

            // 5. Allocate starting Item ID
            int nextItemId;
            String nextItemSql = "SELECT COALESCE(MAX(item_id), 0) + 1 AS next_id FROM order_items";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(nextItemSql)) {
                rs.next();
                nextItemId = rs.getInt("next_id");
            }

            // 6. Insert order items & deduct product stock
            String insertItemSql = "INSERT INTO order_items (item_id, order_id, product_id, quantity) VALUES (?, ?, ?, ?)";
            String deductStockSql = "UPDATE products SET stock = stock - ? WHERE product_id = ?";

            try (PreparedStatement psItem = conn.prepareStatement(insertItemSql);
                 PreparedStatement psStock = conn.prepareStatement(deductStockSql)) {

                for (OrderItem item : items) {
                    item.setItemId(nextItemId++);
                    item.setOrderId(newOrderId);

                    // Insert item
                    psItem.setInt(1, item.getItemId());
                    psItem.setInt(2, item.getOrderId());
                    psItem.setInt(3, item.getProductId());
                    psItem.setInt(4, item.getQuantity());
                    psItem.executeUpdate();

                    // Deduct stock
                    psStock.setInt(1, item.getQuantity());
                    psStock.setInt(2, item.getProductId());
                    psStock.executeUpdate();
                }
            }

            // Commit Transaction
            conn.commit();

            // Assemble resulting Order object
            Order order = new Order(newOrderId, customerId, today, initialStatus);
            order.setCustomerName(customerName);
            order.addAllItems(items);
            return order;

        } catch (SQLException | InsufficientStockException | DatabaseOperationException ex) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on ANY failure
                } catch (SQLException rollbackEx) {
                    // Suppressed
                }
            }
            throw ex;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Cancels an order and restores inventory stock for each product in the order.
     */
    public boolean cancelOrder(int orderId) throws SQLException, DatabaseOperationException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            // 1. Check current order status
            String checkSql = "SELECT status FROM orders WHERE order_id = ? FOR UPDATE";
            String currentStatus;
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new DatabaseOperationException("Order #" + orderId + " does not exist.");
                    }
                    currentStatus = rs.getString("status");
                }
            }

            if (Order.STATUS_CANCELLED.equalsIgnoreCase(currentStatus)) {
                throw new DatabaseOperationException("Order #" + orderId + " is already Cancelled.");
            }

            // 2. Load order items and restore stock
            String itemsSql = "SELECT product_id, quantity FROM order_items WHERE order_id = ?";
            String restoreStockSql = "UPDATE products SET stock = stock + ? WHERE product_id = ?";

            try (PreparedStatement psItems = conn.prepareStatement(itemsSql);
                 PreparedStatement psRestore = conn.prepareStatement(restoreStockSql)) {

                psItems.setInt(1, orderId);
                try (ResultSet rs = psItems.executeQuery()) {
                    while (rs.next()) {
                        int productId = rs.getInt("product_id");
                        int quantity = rs.getInt("quantity");

                        psRestore.setInt(1, quantity);
                        psRestore.setInt(2, productId);
                        psRestore.executeUpdate();
                    }
                }
            }

            // 3. Update order status to Cancelled
            String updateStatusSql = "UPDATE orders SET status = ? WHERE order_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateStatusSql)) {
                ps.setString(1, Order.STATUS_CANCELLED);
                ps.setInt(2, orderId);
                ps.executeUpdate();
            }

            conn.commit(); // Commit Transaction
            return true;

        } catch (SQLException | DatabaseOperationException ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            throw ex;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Updates an order status (e.g. from Pending to Shipped, or to Delivered).
     * If transitioning to Cancelled, delegates to cancelOrder() to ensure inventory restoration.
     */
    public boolean updateOrderStatus(int orderId, String newStatus) throws SQLException, DatabaseOperationException {
        if (!Order.isValidStatus(newStatus)) {
            throw new DatabaseOperationException("Invalid status: " + newStatus);
        }

        if (Order.STATUS_CANCELLED.equalsIgnoreCase(newStatus)) {
            return cancelOrder(orderId);
        }

        return orderDAO.updateOrderStatus(orderId, newStatus);
    }
}
