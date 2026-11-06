package com.ecommerce.service;

import com.ecommerce.dao.ProductDAO;
import com.ecommerce.dao.ProductDAOImpl;
import com.ecommerce.model.Product;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Service class handling Product business logic, validation, and inventory rules.
 */
public class ProductService {

    private final ProductDAO productDAO;

    public ProductService() {
        this(new ProductDAOImpl());
    }

    public ProductService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    public List<Product> getAllProducts() throws SQLException {
        return productDAO.getAllProducts();
    }

    public Product getProductById(int productId) throws SQLException {
        return productDAO.getProductById(productId);
    }

    public List<Product> searchProducts(String query, String category) throws SQLException {
        return productDAO.searchProducts(query, category);
    }

    public List<Product> getOutOfStockProducts() throws SQLException {
        return productDAO.getOutOfStockProducts();
    }

    public List<String> getAllCategories() throws SQLException {
        return productDAO.getAllCategories();
    }

    public int getNextProductId() throws SQLException {
        return productDAO.getNextProductId();
    }

    public int getProductCount() throws SQLException {
        return productDAO.getProductCount();
    }

    public int getOutOfStockCount() throws SQLException {
        return productDAO.getOutOfStockCount();
    }

    public void addProduct(int productId, String name, String category, BigDecimal price, int stock)
            throws DatabaseOperationException, SQLException {
        validateProductData(name, category, price, stock);

        if (productDAO.getProductById(productId) != null) {
            throw new DatabaseOperationException("Product ID " + productId + " already exists. Please choose a unique ID.");
        }

        Product p = new Product(productId, name.trim(), category.trim(), price, stock);
        boolean success = productDAO.addProduct(p);
        if (!success) {
            throw new DatabaseOperationException("Failed to add product to database.");
        }
    }

    public void updateProduct(int productId, String name, String category, BigDecimal price, int stock)
            throws DatabaseOperationException, SQLException {
        validateProductData(name, category, price, stock);

        Product existing = productDAO.getProductById(productId);
        if (existing == null) {
            throw new DatabaseOperationException("Product ID " + productId + " not found.");
        }

        Product p = new Product(productId, name.trim(), category.trim(), price, stock);
        boolean success = productDAO.updateProduct(p);
        if (!success) {
            throw new DatabaseOperationException("Failed to update product details.");
        }
    }

    public void deleteProduct(int productId) throws DatabaseOperationException, SQLException {
        Product existing = productDAO.getProductById(productId);
        if (existing == null) {
            throw new DatabaseOperationException("Product ID " + productId + " not found.");
        }

        try {
            boolean success = productDAO.deleteProduct(productId);
            if (!success) {
                throw new DatabaseOperationException("Failed to delete product.");
            }
        } catch (SQLException e) {
            // MySQL error code 1451: Foreign key constraint fails
            if (e.getErrorCode() == 1451 || "23000".equals(e.getSQLState())) {
                throw new DatabaseOperationException("Cannot delete product #" + productId + 
                        " ('" + existing.getName() + "') because it is referenced in past order records.\n" +
                        "Foreign-key restriction enforced: Product cannot be deleted while orders reference it.");
            }
            throw e;
        }
    }

    private void validateProductData(String name, String category, BigDecimal price, int stock) 
            throws DatabaseOperationException {
        if (name == null || name.trim().isEmpty()) {
            throw new DatabaseOperationException("Product name cannot be empty.");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new DatabaseOperationException("Product category cannot be empty.");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new DatabaseOperationException("Product price must be a non-negative number.");
        }
        if (stock < 0) {
            throw new DatabaseOperationException("Stock quantity cannot be negative.");
        }
    }
}
