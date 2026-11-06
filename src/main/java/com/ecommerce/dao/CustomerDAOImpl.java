package com.ecommerce.dao;

import com.ecommerce.database.DBConnection;
import com.ecommerce.model.Customer;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of CustomerDAO.
 * Uses parameterized PreparedStatements and try-with-resources for all operations.
 */
public class CustomerDAOImpl implements CustomerDAO {

    @Override
    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT customer_id, name, city, join_date FROM customers ORDER BY customer_id";
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
    public Customer getCustomerById(int customerId) throws SQLException {
        String sql = "SELECT customer_id, name, city, join_date FROM customers WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Customer> searchCustomers(String query) throws SQLException {
        List<Customer> list = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return getAllCustomers();
        }
        String sql = "SELECT customer_id, name, city, join_date FROM customers " +
                     "WHERE customer_id = ? OR LOWER(name) LIKE ? OR LOWER(city) LIKE ? ORDER BY customer_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int numericId = -1;
            try {
                numericId = Integer.parseInt(query.trim());
            } catch (NumberFormatException ignored) {
            }
            ps.setInt(1, numericId);
            String pattern = "%" + query.trim().toLowerCase() + "%";
            ps.setString(2, pattern);
            ps.setString(3, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public boolean addCustomer(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (customer_id, name, city, join_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customer.getCustomerId());
            ps.setString(2, customer.getName());
            ps.setString(3, customer.getCity());
            ps.setDate(4, Date.valueOf(customer.getJoinDate()));
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateCustomer(Customer customer) throws SQLException {
        String sql = "UPDATE customers SET name = ?, city = ?, join_date = ? WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getCity());
            ps.setDate(3, Date.valueOf(customer.getJoinDate()));
            ps.setInt(4, customer.getCustomerId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteCustomer(int customerId) throws SQLException {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public int getNextCustomerId() throws SQLException {
        String sql = "SELECT COALESCE(MAX(customer_id), 0) + 1 AS next_id FROM customers";
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
    public int getCustomerCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM customers";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getInt("customer_id"));
        c.setName(rs.getString("name"));
        c.setCity(rs.getString("city"));
        Date d = rs.getDate("join_date");
        if (d != null) {
            c.setJoinDate(d.toLocalDate());
        }
        return c;
    }
}
