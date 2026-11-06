package com.ecommerce.dao;

import com.ecommerce.model.Order;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object interface for Orders.
 */
public interface OrderDAO {

    List<Order> getAllOrders() throws SQLException;

    Order getOrderById(int orderId) throws SQLException;

    List<Order> getOrdersByCustomer(int customerId) throws SQLException;

    List<Order> getOrdersByStatus(String status) throws SQLException;

    int getNextOrderId() throws SQLException;

    int getOrderCount() throws SQLException;

    Map<String, Integer> getOrderCountsByStatus() throws SQLException;

    // Transactional methods accepting an active connection
    int insertOrder(Connection conn, Order order) throws SQLException;

    boolean updateOrderStatus(Connection conn, int orderId, String newStatus) throws SQLException;

    // Standalone status update
    boolean updateOrderStatus(int orderId, String newStatus) throws SQLException;
}
