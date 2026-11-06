-- =====================================================================
-- E-Commerce Store Management System (ecom_db)
-- All 15 Academic Evaluation SQL Queries (Q1 to Q15)
-- Scenario 1: E-Commerce Store (Aligned with Faculty Answer Key)
-- =====================================================================

USE ecom_db;

-- ---------------------------------------------------------------------
-- Q1 (Easy - 5 pts)
-- List the product id, name and price of all products in the Electronics category.
-- Expected output:
-- +------------+----------------+---------+
-- | product_id | name           | price   |
-- +------------+----------------+---------+
-- | 1          | Wireless Mouse | 599.00  |
-- | 2          | Keyboard       | 1299.00 |
-- +------------+----------------+---------+
-- ---------------------------------------------------------------------
SELECT product_id, name, price 
FROM products 
WHERE category = 'Electronics';


-- ---------------------------------------------------------------------
-- Q2 (Easy - 5 pts)
-- Show the name and city of all customers who live in Chennai.
-- Expected output:
-- +--------+---------+
-- | name   | city    |
-- +--------+---------+
-- | Anita  | Chennai |
-- | Dinesh | Chennai |
-- +--------+---------+
-- ---------------------------------------------------------------------
SELECT name, city 
FROM customers 
WHERE city = 'Chennai';


-- ---------------------------------------------------------------------
-- Q3 (Easy - 5 pts)
-- List products priced above 500, most expensive first.
-- Expected output:
-- +----------------+---------+
-- | name           | price   |
-- +----------------+---------+
-- | Backpack       | 1499.00 |
-- | Keyboard       | 1299.00 |
-- | Desk Lamp      | 899.00  |
-- | Wireless Mouse | 599.00  |
-- +----------------+---------+
-- ---------------------------------------------------------------------
SELECT name, price 
FROM products 
WHERE price > 500 
ORDER BY price DESC;


-- ---------------------------------------------------------------------
-- Q4 (Easy - 5 pts)
-- Find the products that are out of stock.
-- Expected output:
-- +-----------+----------+
-- | name      | category |
-- +-----------+----------+
-- | Desk Lamp | Home     |
-- +-----------+----------+
-- ---------------------------------------------------------------------
SELECT name, category 
FROM products 
WHERE stock = 0;


-- ---------------------------------------------------------------------
-- Q5 (Easy - 5 pts)
-- Count the number of orders in each status.
-- Expected output:
-- +-----------+--------------+
-- | status    | total_orders |
-- +-----------+--------------+
-- | Cancelled | 1            |
-- | Delivered | 4            |
-- | Pending   | 1            |
-- | Shipped   | 1            |
-- +-----------+--------------+
-- ---------------------------------------------------------------------
SELECT status, COUNT(*) AS total_orders 
FROM orders 
GROUP BY status 
ORDER BY status;


-- ---------------------------------------------------------------------
-- Q6 (Easy - 5 pts)
-- Show the order id, customer name and order date for all Delivered orders.
-- Expected output:
-- +----------+----------+------------+
-- | order_id | customer | order_date |
-- +----------+----------+------------+
-- | 1001     | Anita    | 2026-09-01 |
-- | 1003     | Bharath  | 2026-09-06 |
-- | 1005     | Dinesh   | 2026-09-12 |
-- | 1006     | Bharath  | 2026-09-15 |
-- +----------+----------+------------+
-- ---------------------------------------------------------------------
SELECT o.order_id, c.name AS customer, o.order_date
FROM orders o 
JOIN customers c ON o.customer_id = c.customer_id
WHERE o.status = 'Delivered'
ORDER BY o.order_id;


-- ---------------------------------------------------------------------
-- Q7 (Easy - 5 pts)
-- List the orders placed between 2026-09-10 and 2026-09-20 (both inclusive).
-- Expected output:
-- +----------+-------------+------------+-----------+
-- | order_id | customer_id | order_date | status    |
-- +----------+-------------+------------+-----------+
-- | 1004     | 3           | 2026-09-10 | Cancelled |
-- | 1005     | 4           | 2026-09-12 | Delivered |
-- | 1006     | 2           | 2026-09-15 | Delivered |
-- | 1007     | 3           | 2026-09-20 | Pending   |
-- +----------+-------------+------------+-----------+
-- ---------------------------------------------------------------------
SELECT order_id, customer_id, order_date, status 
FROM orders
WHERE order_date BETWEEN '2026-09-10' AND '2026-09-20'
ORDER BY order_date;


-- ---------------------------------------------------------------------
-- Q8 (Easy - 5 pts)
-- List the products that belong to the Home or Stationery category, sorted by category and then name.
-- Expected output:
-- +--------------+------------+
-- | name         | category   |
-- +--------------+------------+
-- | Desk Lamp    | Home       |
-- | Water Bottle | Home       |
-- | Notebook     | Stationery |
-- | Pen Pack     | Stationery |
-- +--------------+------------+
-- ---------------------------------------------------------------------
SELECT name, category 
FROM products 
WHERE category IN ('Home','Stationery') 
ORDER BY category, name;


-- ---------------------------------------------------------------------
-- Q9 (Medium - 10 pts)
-- For every order item, show the order id, product name, quantity, unit price and line total (quantity x price).
-- Expected output:
-- +----------+----------------+----------+---------+------------+
-- | order_id | product        | quantity | price   | line_total |
-- +----------+----------------+----------+---------+------------+
-- | 1001     | Wireless Mouse | 2        | 599.00  | 1198.00    |
-- | 1001     | Notebook       | 5        | 120.00  | 600.00     |
-- | 1002     | Keyboard       | 1        | 1299.00 | 1299.00    |
-- | 1003     | Water Bottle   | 3        | 350.00  | 1050.00    |
-- | 1003     | Pen Pack       | 10       | 80.00   | 800.00     |
-- | 1004     | Wireless Mouse | 1        | 599.00  | 599.00     |
-- | 1005     | Notebook       | 10       | 120.00  | 1200.00    |
-- | 1005     | Desk Lamp      | 1        | 899.00  | 899.00     |
-- | 1006     | Keyboard       | 1        | 1299.00 | 1299.00    |
-- | 1006     | Wireless Mouse | 1        | 599.00  | 599.00     |
-- | 1007     | Water Bottle   | 2        | 350.00  | 700.00     |
-- +----------+----------------+----------+---------+------------+
-- ---------------------------------------------------------------------
SELECT oi.order_id, p.name AS product, oi.quantity, p.price, oi.quantity * p.price AS line_total
FROM order_items oi 
JOIN products p ON oi.product_id = p.product_id
ORDER BY oi.order_id, oi.item_id;


-- ---------------------------------------------------------------------
-- Q10 (Medium - 10 pts)
-- Calculate the total value of each order along with its status.
-- Expected output:
-- +----------+-----------+-------------+
-- | order_id | status    | order_total |
-- +----------+-----------+-------------+
-- | 1001     | Delivered | 1798.00     |
-- | 1002     | Shipped   | 1299.00     |
-- | 1003     | Delivered | 1850.00     |
-- | 1004     | Cancelled | 599.00      |
-- | 1005     | Delivered | 2099.00     |
-- | 1006     | Delivered | 1898.00     |
-- | 1007     | Pending   | 700.00      |
-- +----------+-----------+-------------+
-- ---------------------------------------------------------------------
SELECT o.order_id, o.status, SUM(oi.quantity * p.price) AS order_total
FROM orders o
JOIN order_items oi ON o.order_id = oi.order_id
JOIN products p ON oi.product_id = p.product_id
GROUP BY o.order_id, o.status
ORDER BY o.order_id;


-- ---------------------------------------------------------------------
-- Q11 (Medium - 10 pts)
-- Find the total revenue per product category from Delivered orders, highest first.
-- Expected output:
-- +-------------+---------+
-- | category    | revenue |
-- +-------------+---------+
-- | Electronics | 3096.00 |
-- | Stationery  | 2600.00 |
-- | Home        | 1949.00 |
-- +-------------+---------+
-- ---------------------------------------------------------------------
SELECT p.category, SUM(oi.quantity * p.price) AS revenue
FROM orders o
JOIN order_items oi ON o.order_id = oi.order_id
JOIN products p ON oi.product_id = p.product_id
WHERE o.status = 'Delivered'
GROUP BY p.category
ORDER BY revenue DESC;


-- ---------------------------------------------------------------------
-- Q12 (Medium - 10 pts)
-- Find the top 3 customers by total spend on Delivered orders.
-- Expected output:
-- +---------+-------------+
-- | name    | total_spent |
-- +---------+-------------+
-- | Bharath | 3748.00     |
-- | Dinesh  | 2099.00     |
-- | Anita   | 1798.00     |
-- +---------+-------------+
-- ---------------------------------------------------------------------
SELECT c.name, SUM(oi.quantity * p.price) AS total_spent
FROM customers c
JOIN orders o ON c.customer_id = o.customer_id
JOIN order_items oi ON o.order_id = oi.order_id
JOIN products p ON oi.product_id = p.product_id
WHERE o.status = 'Delivered'
GROUP BY c.customer_id, c.name
ORDER BY total_spent DESC
LIMIT 3;


-- ---------------------------------------------------------------------
-- Q13 (Medium - 10 pts)
-- Find the products that have never been ordered.
-- Expected output:
-- +----------+
-- | name     |
-- +----------+
-- | Backpack |
-- +----------+
-- ---------------------------------------------------------------------
SELECT p.name
FROM products p 
LEFT JOIN order_items oi ON p.product_id = oi.product_id
WHERE oi.item_id IS NULL;


-- ---------------------------------------------------------------------
-- Q14 (Medium - 10 pts)
-- Find the customers who have placed more than one order, with their order count.
-- Expected output:
-- +---------+-------------+
-- | name    | order_count |
-- +---------+-------------+
-- | Anita   | 2           |
-- | Bharath | 2           |
-- | Cathy   | 2           |
-- +---------+-------------+
-- ---------------------------------------------------------------------
SELECT c.name, COUNT(*) AS order_count
FROM customers c 
JOIN orders o ON c.customer_id = o.customer_id
GROUP BY c.customer_id, c.name
HAVING COUNT(*) > 1
ORDER BY order_count DESC, c.name;


-- ---------------------------------------------------------------------
-- Q15 (Medium - 10 pts)
-- Show the total units sold per product, ignoring Cancelled orders. Products with no sales must appear with 0.
-- Expected output:
-- +----------------+------------+
-- | name           | units_sold |
-- +----------------+------------+
-- | Notebook       | 15         |
-- | Pen Pack       | 10         |
-- | Water Bottle   | 5          |
-- | Wireless Mouse | 3          |
-- | Keyboard       | 2          |
-- | Desk Lamp      | 1          |
-- | Backpack       | 0          |
-- +----------------+------------+
-- ---------------------------------------------------------------------
SELECT p.name, COALESCE(SUM(CASE WHEN o.status <> 'Cancelled' THEN oi.quantity END), 0) AS units_sold
FROM products p
LEFT JOIN order_items oi ON p.product_id = oi.product_id
LEFT JOIN orders o ON oi.order_id = o.order_id
GROUP BY p.product_id, p.name
ORDER BY units_sold DESC, p.name;
