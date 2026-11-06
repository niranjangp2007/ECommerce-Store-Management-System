package com.ecommerce.dao;

import com.ecommerce.model.OrderItem;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object interface for Order Items.
 */
public interface OrderItemDAO {

    List<OrderItem> getItemsByOrderId(int orderId) throws SQLException;

    int getNextItemId() throws SQLException;

    // Transaction-capable batch insertion
    boolean insertOrderItems(Connection conn, List<OrderItem> items) throws SQLException;
}
