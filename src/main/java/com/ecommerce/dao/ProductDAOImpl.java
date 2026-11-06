package com.ecommerce.dao;

import com.ecommerce.database.DBConnection;
import com.ecommerce.model.Product;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of ProductDAO.
 */
public class ProductDAOImpl implements ProductDAO {

    @Override
    public List<Product> getAllProducts() throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT product_id, name, category, price, stock FROM products ORDER BY product_id";
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
    public Product getProductById(int productId) throws SQLException {
        String sql = "SELECT product_id, name, category, price, stock FROM products WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Product> searchProducts(String query, String category) throws SQLException {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT product_id, name, category, price, stock FROM products WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (category != null && !category.trim().isEmpty() && !"All Categories".equalsIgnoreCase(category.trim())) {
            sql.append("AND category = ? ");
            params.add(category.trim());
        }

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (product_id = ? OR LOWER(name) LIKE ? OR LOWER(category) LIKE ?) ");
            int numericId = -1;
            try {
                numericId = Integer.parseInt(query.trim());
            } catch (NumberFormatException ignored) {
            }
            params.add(numericId);
            String pattern = "%" + query.trim().toLowerCase() + "%";
            params.add(pattern);
            params.add(pattern);
        }

        sql.append("ORDER BY product_id");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof Integer) {
                    ps.setInt(i + 1, (Integer) p);
                } else {
                    ps.setString(i + 1, (String) p);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Product> getOutOfStockProducts() throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT product_id, name, category, price, stock FROM products WHERE stock = 0 ORDER BY product_id";
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
    public List<String> getAllCategories() throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM products ORDER BY category";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getString("category"));
            }
        }
        return list;
    }

    @Override
    public boolean addProduct(Product product) throws SQLException {
        String sql = "INSERT INTO products (product_id, name, category, price, stock) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, product.getProductId());
            ps.setString(2, product.getName());
            ps.setString(3, product.getCategory());
            ps.setBigDecimal(4, product.getPrice());
            ps.setInt(5, product.getStock());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateProduct(Product product) throws SQLException {
        String sql = "UPDATE products SET name = ?, category = ?, price = ?, stock = ? WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getCategory());
            ps.setBigDecimal(3, product.getPrice());
            ps.setInt(4, product.getStock());
            ps.setInt(5, product.getProductId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteProduct(int productId) throws SQLException {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public int getNextProductId() throws SQLException {
        String sql = "SELECT COALESCE(MAX(product_id), 0) + 1 AS next_id FROM products";
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
    public int getProductCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM products";
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
    public int getOutOfStockCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE stock = 0";
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
    public boolean updateStock(Connection conn, int productId, int newStock) throws SQLException {
        String sql = "UPDATE products SET stock = ? WHERE product_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newStock);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        }
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setName(rs.getString("name"));
        p.setCategory(rs.getString("category"));
        BigDecimal price = rs.getBigDecimal("price");
        p.setPrice(price != null ? price : BigDecimal.ZERO);
        p.setStock(rs.getInt("stock"));
        return p;
    }
}
