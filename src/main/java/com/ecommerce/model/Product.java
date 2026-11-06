package com.ecommerce.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Product model class representing a store item.
 * Demonstrates Encapsulation with strict field validations and business domain logic.
 *
 * Mapped to database table: 'products' (product_id, name, category, price, stock).
 */
public class Product {

    private int productId;
    private String name;
    private String category;
    private BigDecimal price;
    private int stock;

    /**
     * Default constructor.
     */
    public Product() {
        this.price = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Parameterized constructor using double for price convenience.
     */
    public Product(int productId, String name, String category, double price, int stock) {
        this(productId, name, category, BigDecimal.valueOf(price), stock);
    }

    /**
     * Parameterized constructor with BigDecimal price.
     *
     * @param productId unique product ID
     * @param name      product name
     * @param category  product category (e.g., Electronics, Stationery, Home)
     * @param price     unit price
     * @param stock     available inventory quantity
     */
    public Product(int productId, String name, String category, BigDecimal price, int stock) {
        setProductId(productId);
        setName(name);
        setCategory(category);
        setPrice(price);
        setStock(stock);
    }

    // --- Encapsulated Getters & Setters with Business Validation ---

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        if (productId < 0) {
            throw new IllegalArgumentException("Product ID cannot be negative: " + productId);
        }
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty.");
        }
        this.name = name.trim();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Product category cannot be null or empty.");
        }
        this.category = category.trim();
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        if (price == null) {
            throw new IllegalArgumentException("Product price cannot be null.");
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Product price cannot be negative: " + price);
        }
        this.price = price.setScale(2, RoundingMode.HALF_UP);
    }

    public void setPrice(double price) {
        setPrice(BigDecimal.valueOf(price));
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("Product stock cannot be negative: " + stock);
        }
        this.stock = stock;
    }

    // --- Domain Helpers ---

    /**
     * Checks if product has zero stock.
     */
    public boolean isOutOfStock() {
        return stock == 0;
    }

    /**
     * Checks if inventory can fulfill requested quantity.
     */
    public boolean hasSufficientStock(int requestedQuantity) {
        return requestedQuantity > 0 && stock >= requestedQuantity;
    }

    /**
     * Deducts stock when an order is placed.
     */
    public void reduceStock(int quantity) {
        if (!hasSufficientStock(quantity)) {
            throw new IllegalStateException("Insufficient stock for product '" + name + 
                    "'. Available: " + stock + ", Requested: " + quantity);
        }
        this.stock -= quantity;
    }

    /**
     * Restores stock when an order is cancelled.
     */
    public void restoreStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Restoration quantity cannot be negative: " + quantity);
        }
        this.stock += quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product product)) return false;
        return productId == product.productId &&
                stock == product.stock &&
                Objects.equals(name, product.name) &&
                Objects.equals(category, product.category) &&
                Objects.equals(price, product.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, name, category, price, stock);
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                '}';
    }
}
