package com.ecommerce.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Order model representing a customer order.
 * Demonstrates:
 * 1. Encapsulation: Validated state transitions and attributes.
 * 2. Composition: An Order HAS-A collection of OrderItems.
 *
 * Mapped to database table: 'orders' (order_id, customer_id, order_date, status).
 */
public class Order {

    public static final String STATUS_DELIVERED = "Delivered";
    public static final String STATUS_SHIPPED = "Shipped";
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_CANCELLED = "Cancelled";

    private int orderId;
    private int customerId;
    private LocalDate orderDate;
    private String status;

    // Joined attribute for display convenience
    private String customerName;

    // Composition: Order has many OrderItems
    private final List<OrderItem> items = new ArrayList<>();

    /**
     * Default constructor.
     */
    public Order() {
        this.status = STATUS_PENDING;
        this.orderDate = LocalDate.now();
    }

    /**
     * Parameterized constructor for database mapping.
     */
    public Order(int orderId, int customerId, LocalDate orderDate, String status) {
        setOrderId(orderId);
        setCustomerId(customerId);
        setOrderDate(orderDate);
        setStatus(status);
    }

    // --- Encapsulated Getters & Setters ---

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        if (orderId < 0) {
            throw new IllegalArgumentException("Order ID cannot be negative: " + orderId);
        }
        this.orderId = orderId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        if (customerId < 0) {
            throw new IllegalArgumentException("Customer ID cannot be negative: " + customerId);
        }
        this.customerId = customerId;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        if (orderDate == null) {
            throw new IllegalArgumentException("Order date cannot be null.");
        }
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Order status cannot be null or empty.");
        }
        String trimmed = status.trim();
        if (!isValidStatus(trimmed)) {
            throw new IllegalArgumentException("Invalid order status '" + trimmed + 
                    "'. Allowed statuses: Delivered, Shipped, Pending, Cancelled.");
        }
        this.status = trimmed;
    }

    public static boolean isValidStatus(String status) {
        return STATUS_DELIVERED.equalsIgnoreCase(status) ||
               STATUS_SHIPPED.equalsIgnoreCase(status) ||
               STATUS_PENDING.equalsIgnoreCase(status) ||
               STATUS_CANCELLED.equalsIgnoreCase(status);
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    // --- Composition: Order Items Management ---

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addItem(OrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException("OrderItem cannot be null.");
        }
        item.setOrderId(this.orderId);
        this.items.add(item);
    }

    public void addAllItems(List<OrderItem> items) {
        if (items != null) {
            for (OrderItem item : items) {
                addItem(item);
            }
        }
    }

    public void clearItems() {
        this.items.clear();
    }

    /**
     * Calculates the total order value by aggregating all line totals.
     */
    public BigDecimal calculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : items) {
            total = total.add(item.getLineTotal());
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    // --- Domain Helpers ---

    public boolean isDelivered() {
        return STATUS_DELIVERED.equalsIgnoreCase(status);
    }

    public boolean isShipped() {
        return STATUS_SHIPPED.equalsIgnoreCase(status);
    }

    public boolean isPending() {
        return STATUS_PENDING.equalsIgnoreCase(status);
    }

    public boolean isCancelled() {
        return STATUS_CANCELLED.equalsIgnoreCase(status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order order)) return false;
        return orderId == order.orderId &&
                customerId == order.customerId &&
                Objects.equals(orderDate, order.orderDate) &&
                Objects.equals(status, order.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, customerId, orderDate, status);
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", customerId=" + customerId +
                ", customerName='" + customerName + '\'' +
                ", orderDate=" + orderDate +
                ", status='" + status + '\'' +
                ", itemsCount=" + items.size() +
                ", totalValue=" + calculateTotal() +
                '}';
    }
}
