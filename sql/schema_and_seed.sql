-- =====================================================================
-- E-Commerce Store Management System (ecom_db)
-- Database Schema and Sample Evaluation Seed Script
-- Scenario 1: E-Commerce Store (Academic Specification)
-- Note: Non-destructive script (uses IF NOT EXISTS and INSERT IGNORE).
-- =====================================================================

CREATE DATABASE IF NOT EXISTS ecom_db;
USE ecom_db;

-- 1. Customers Table
CREATE TABLE IF NOT EXISTS customers (
    customer_id INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    city VARCHAR(50) NOT NULL,
    join_date DATE NOT NULL
);

-- 2. Products Table
CREATE TABLE IF NOT EXISTS products (
    product_id INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    category VARCHAR(30) NOT NULL,
    price DECIMAL(8,2) NOT NULL,
    stock INT NOT NULL
);

-- 3. Orders Table
CREATE TABLE IF NOT EXISTS orders (
    order_id INT PRIMARY KEY,
    customer_id INT NOT NULL,
    order_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

-- 4. Order Items Table
CREATE TABLE IF NOT EXISTS order_items (
    item_id INT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- =====================================================================
-- Sample Records (Preserving exact IDs, values, and order statuses)
-- Uses INSERT IGNORE to prevent duplicate errors or overwriting data.
-- =====================================================================

INSERT IGNORE INTO customers (customer_id, name, city, join_date) VALUES
(1, 'Anita', 'Chennai', '2025-11-10'),
(2, 'Bharath', 'Bangalore', '2026-01-15'),
(3, 'Cathy', 'Mumbai', '2026-02-20'),
(4, 'Dinesh', 'Chennai', '2026-03-05'),
(5, 'Esha', 'Delhi', '2026-04-12');

INSERT IGNORE INTO products (product_id, name, category, price, stock) VALUES
(1, 'Wireless Mouse', 'Electronics', 599.00, 50),
(2, 'Keyboard', 'Electronics', 1299.00, 30),
(3, 'Notebook', 'Stationery', 120.00, 200),
(4, 'Water Bottle', 'Home', 350.00, 80),
(5, 'Desk Lamp', 'Home', 899.00, 0),
(6, 'Pen Pack', 'Stationery', 80.00, 500),
(7, 'Backpack', 'Bags', 1499.00, 25);

INSERT IGNORE INTO orders (order_id, customer_id, order_date, status) VALUES
(1001, 1, '2026-09-01', 'Delivered'),
(1002, 1, '2026-09-05', 'Shipped'),
(1003, 2, '2026-09-06', 'Delivered'),
(1004, 3, '2026-09-10', 'Cancelled'),
(1005, 4, '2026-09-12', 'Delivered'),
(1006, 2, '2026-09-15', 'Delivered'),
(1007, 3, '2026-09-20', 'Pending');

INSERT IGNORE INTO order_items (item_id, order_id, product_id, quantity) VALUES
(1, 1001, 1, 2),
(2, 1001, 3, 5),
(3, 1002, 2, 1),
(4, 1003, 4, 3),
(5, 1003, 6, 10),
(6, 1004, 1, 1),
(7, 1005, 3, 10),
(8, 1005, 5, 1),
(9, 1006, 2, 1),
(10, 1006, 1, 1),
(11, 1007, 4, 2);
