package com.ecommerce.dao;

import com.ecommerce.database.DBConnection;
import com.ecommerce.model.OrderItem;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of OrderItemDAO.
 */
public class OrderItemDAOImpl implements OrderItemDAO {

    @Override
    public List<OrderItem> getItemsByOrderId(int orderId) throws SQLException {
        List<OrderItem> list = new ArrayList<>();
        String sql = "SELECT oi.item_id, oi.order_id, oi.product_id, oi.quantity, p.name AS product_name, p.price AS unit_price " +
                     "FROM order_items oi JOIN products p ON oi.product_id = p.product_id " +
                     "WHERE oi.order_id = ? ORDER BY oi.item_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setItemId(rs.getInt("item_id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setProductName(rs.getString("product_name"));
                    BigDecimal price = rs.getBigDecimal("unit_price");
                    item.setUnitPrice(price != null ? price : BigDecimal.ZERO);
                    list.add(item);
                }
            }
        }
        return list;
    }

    @Override
    public int getNextItemId() throws SQLException {
        String sql = "SELECT COALESCE(MAX(item_id), 0) + 1 AS next_id FROM order_items";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("next_id");
            }
        }
        return 1;
    }

    @Override
    public boolean insertOrderItems(Connection conn, List<OrderItem> items) throws SQLException {
        if (items == null || items.isEmpty()) {
            return true;
        }
        String sql = "INSERT INTO order_items (item_id, order_id, product_id, quantity) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (OrderItem item : items) {
                ps.setInt(1, item.getItemId());
                ps.setInt(2, item.getOrderId());
                ps.setInt(3, item.getProductId());
                ps.setInt(4, item.getQuantity());
                ps.addBatch();
            }
            int[] results = ps.executeBatch();
            for (int r : results) {
                if (r == Statement.EXECUTE_FAILED) {
                    return false;
                }
            }
            return true;
        }
    }
}
