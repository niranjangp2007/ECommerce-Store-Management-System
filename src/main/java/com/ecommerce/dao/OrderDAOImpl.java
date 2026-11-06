package com.ecommerce.dao;

import com.ecommerce.database.DBConnection;
import com.ecommerce.model.Order;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC implementation of OrderDAO.
 */
public class OrderDAOImpl implements OrderDAO {

    @Override
    public List<Order> getAllOrders() throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.order_id, o.customer_id, o.order_date, o.status, c.name AS customer_name " +
                     "FROM orders o JOIN customers c ON o.customer_id = c.customer_id " +
                     "ORDER BY o.order_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public Order getOrderById(int orderId) throws SQLException {
        String sql = "SELECT o.order_id, o.customer_id, o.order_date, o.status, c.name AS customer_name " +
                     "FROM orders o JOIN customers c ON o.customer_id = c.customer_id " +
                     "WHERE o.order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Order> getOrdersByCustomer(int customerId) throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.order_id, o.customer_id, o.order_date, o.status, c.name AS customer_name " +
                     "FROM orders o JOIN customers c ON o.customer_id = c.customer_id " +
                     "WHERE o.customer_id = ? ORDER BY o.order_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Order> getOrdersByStatus(String status) throws SQLException {
        if (status == null || status.trim().isEmpty() || "All".equalsIgnoreCase(status.trim())) {
            return getAllOrders();
        }
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.order_id, o.customer_id, o.order_date, o.status, c.name AS customer_name " +
                     "FROM orders o JOIN customers c ON o.customer_id = c.customer_id " +
                     "WHERE o.status = ? ORDER BY o.order_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public int getNextOrderId() throws SQLException {
        String sql = "SELECT COALESCE(MAX(order_id), 1000) + 1 AS next_id FROM orders";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("next_id");
            }
        }
        return 1001;
    }

    @Override
    public int getOrderCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public Map<String, Integer> getOrderCountsByStatus() throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT status, COUNT(*) AS total_orders FROM orders GROUP BY status ORDER BY status";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("status"), rs.getInt("total_orders"));
            }
        }
        return map;
    }

    @Override
    public int insertOrder(Connection conn, Order order) throws SQLException {
        String sql = "INSERT INTO orders (order_id, customer_id, order_date, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, order.getOrderId());
            ps.setInt(2, order.getCustomerId());
            ps.setDate(3, Date.valueOf(order.getOrderDate()));
            ps.setString(4, order.getStatus());
            ps.executeUpdate();
            return order.getOrderId();
        }
    }

    @Override
    public boolean updateOrderStatus(Connection conn, int orderId, String newStatus) throws SQLException {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateOrderStatus(int orderId, String newStatus) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return updateOrderStatus(conn, orderId, newStatus);
        }
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id"));
        o.setCustomerId(rs.getInt("customer_id"));
        Date d = rs.getDate("order_date");
        if (d != null) {
            o.setOrderDate(d.toLocalDate());
        }
        o.setStatus(rs.getString("status"));
        o.setCustomerName(rs.getString("customer_name"));
        return o;
    }
}
