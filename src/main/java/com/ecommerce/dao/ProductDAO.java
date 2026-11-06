package com.ecommerce.dao;

import com.ecommerce.model.Product;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object interface for Product operations.
 */
public interface ProductDAO {

    List<Product> getAllProducts() throws SQLException;

    Product getProductById(int productId) throws SQLException;

    List<Product> searchProducts(String query, String category) throws SQLException;

    List<Product> getOutOfStockProducts() throws SQLException;

    List<String> getAllCategories() throws SQLException;

    boolean addProduct(Product product) throws SQLException;

    boolean updateProduct(Product product) throws SQLException;

    boolean deleteProduct(int productId) throws SQLException;

    int getNextProductId() throws SQLException;

    int getProductCount() throws SQLException;

    int getOutOfStockCount() throws SQLException;

    // Transaction-capable stock deduction and restoration
    boolean updateStock(Connection conn, int productId, int newStock) throws SQLException;
}
