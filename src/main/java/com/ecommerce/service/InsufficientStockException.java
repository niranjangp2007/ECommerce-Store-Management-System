package com.ecommerce.service;

/**
 * Exception thrown when an order cannot be fulfilled due to insufficient product inventory.
 */
public class InsufficientStockException extends Exception {

    private final int productId;
    private final String productName;
    private final int availableStock;
    private final int requestedQuantity;

    public InsufficientStockException(int productId, String productName, int availableStock, int requestedQuantity) {
        super(String.format("Insufficient stock for product '%s' (ID: %d). Available: %d, Requested: %d",
                productName, productId, availableStock, requestedQuantity));
        this.productId = productId;
        this.productName = productName;
        this.availableStock = availableStock;
        this.requestedQuantity = requestedQuantity;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }
}
