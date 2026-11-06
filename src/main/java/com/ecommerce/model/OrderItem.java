package com.ecommerce.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * OrderItem model representing an individual line item within an order.
 * Demonstrates Encapsulation and business computation (line total).
 *
 * Mapped to database table: 'order_items' (item_id, order_id, product_id, quantity).
 */
public class OrderItem {

    private int itemId;
    private int orderId;
    private int productId;
    private int quantity;

    // Additional transient / joined fields from Product for reporting and UI convenience
    private String productName;
    private BigDecimal unitPrice;

    /**
     * Default constructor.
     */
    public OrderItem() {
        this.unitPrice = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Constructor for database mapping.
     */
    public OrderItem(int itemId, int orderId, int productId, int quantity) {
        this(itemId, orderId, productId, quantity, null, BigDecimal.ZERO);
    }

    /**
     * Parameterized constructor including joined product details.
     */
    public OrderItem(int itemId, int orderId, int productId, int quantity, String productName, BigDecimal unitPrice) {
        setItemId(itemId);
        setOrderId(orderId);
        setProductId(productId);
        setQuantity(quantity);
        this.productName = productName;
        setUnitPrice(unitPrice);
    }

    // --- Encapsulated Getters & Setters ---

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        if (itemId < 0) {
            throw new IllegalArgumentException("Item ID cannot be negative: " + itemId);
        }
        this.itemId = itemId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        if (orderId < 0) {
            throw new IllegalArgumentException("Order ID cannot be negative: " + orderId);
        }
        this.orderId = orderId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        if (productId < 0) {
            throw new IllegalArgumentException("Product ID cannot be negative: " + productId);
        }
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero. Provided: " + quantity);
        }
        this.quantity = quantity;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice != null ? unitPrice.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    /**
     * Calculates the line total for this item (quantity * unitPrice).
     */
    public BigDecimal getLineTotal() {
        if (unitPrice == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem orderItem)) return false;
        return itemId == orderItem.itemId &&
                orderId == orderItem.orderId &&
                productId == orderItem.productId &&
                quantity == orderItem.quantity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, orderId, productId, quantity);
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "itemId=" + itemId +
                ", orderId=" + orderId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", productName='" + productName + '\'' +
                ", unitPrice=" + unitPrice +
                ", lineTotal=" + getLineTotal() +
                '}';
    }
}
