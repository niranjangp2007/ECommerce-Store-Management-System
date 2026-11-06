# Test Cases and Verification Results

This document records the automated and manual test specifications for evaluating the **E-Commerce Store Management System**.

---

## 1. Automated Test Suite Summary (`.\mvn.cmd clean test`)

| Test Class | Test Case | Category | Result |
| :--- | :--- | :---: | :---: |
| `DBConnectionTest` | URL and user resolution from env/properties | Connectivity | **PASS** |
| `DBConnectionTest` | Sanitized diagnostic reporting without leaking passwords | Security | **PASS** |
| `OOPModelTest` | Person is abstract and defines abstract methods | Abstraction | **PASS** |
| `OOPModelTest` | Customer inherits from Person (`instanceof Person`) | Inheritance | **PASS** |
| `OOPModelTest` | Overridden methods dispatched via Person reference | Polymorphism | **PASS** |
| `OOPModelTest` | Customer setters reject empty names/cities | Encapsulation | **PASS** |
| `OOPModelTest` | Product domain rules: price/stock non-negative, deduction/restoration | Encapsulation | **PASS** |
| `OOPModelTest` | Order contains OrderItems and aggregates line totals | Composition | **PASS** |
| `OOPModelTest` | Order status validation against allowed enum values | Validation | **PASS** |
| `CustomerServiceTest` | Add customer rejects empty name, blank city, null date | Validation | **PASS** |
| `CustomerServiceTest` | Live retrieval verifies sample dataset (>= 5 customers) | Database | **PASS** |
| `ProductServiceTest` | Add product rejects negative price, negative stock | Validation | **PASS** |
| `ProductServiceTest` | Live retrieval detects Desk Lamp with stock = 0 | Database | **PASS** |
| `OrderServiceTransactionTest`| Rejects empty cart / null items list | Validation | **PASS** |
| `OrderServiceTransactionTest`| Insufficient stock triggers rollback; zero partial orders | Atomicity | **PASS** |
| `EvaluationReportsTest` | All 15 evaluation reports (Q1–Q15) are registered with valid SQL | Metadata | **PASS** |
| `EvaluationReportsTest` | Live execution of Q1 to Q15 against MySQL matches answer key | DBMS Verification | **PASS** |

**Summary**: 17 tests executed, 17 Passed, 0 Failures, 0 Errors.

---

## 2. Manual GUI Testing Guide

### Test 1: Dashboard Overview
- **Action**: Launch application and observe the "Dashboard" tab.
- **Expected Result**: Four metric cards display Total Customers (5), Total Products (7), Out-of-Stock Items (1), Total Orders (7). The status table shows order counts grouped by status (`Cancelled: 1`, `Delivered: 4`, `Pending: 1`, `Shipped: 1`).
- **Pass Criteria**: Matches database state.

### Test 2: Customer CRUD & Foreign-Key Protection
- **Add Customer**:
  - Click `+ Add Customer` button.
  - Enter: ID `6`, Name `Fathima`, City `Hyderabad`, Join Date `2026-10-19`.
  - Confirm. Customer #6 appears in the table.
- **Edit Customer**:
  - Select Customer #6, click `Edit Selected`. Change City to `Secunderabad`.
  - Confirm. Table reflects updated city.
- **Delete Customer (Valid)**:
  - Delete Customer #6. Succeeded (since #6 has no orders).
- **Delete Customer (Foreign Key Restriction Enforced)**:
  - Select Customer #1 (Anita). Click `Delete Selected` and confirm.
  - **Expected Result**: Error dialog appears with message:
    `Cannot delete customer #1 ('Anita') because existing orders are linked to this customer. Foreign-key restriction enforced: Delete customer's orders first.`

### Test 3: Product CRUD & Out-of-Stock Filter
- **Add Product**:
  - Click `+ Add Product`.
  - Enter: ID `8`, Name `USB Hub`, Category `Electronics`, Price `450.00`, Stock `15`.
  - Confirm. Product #8 appears in the table.
- **Out of Stock Toggle**:
  - Check `Out of Stock (stock = 0)`.
  - Table instantly filters down to 1 item: `Desk Lamp` (Home category, stock 0).
  - Uncheck toggle. All products reappear.
- **Foreign Key Restriction on Product**:
  - Select Product #1 (Wireless Mouse). Click `Delete Product` and confirm.
  - **Expected Result**: Error dialog appears:
    `Cannot delete product #1 ('Wireless Mouse') because it is referenced in past order records. Foreign-key restriction enforced.`

### Test 4: Transaction-Safe Multi-Item Order Creation
- **Action**:
  - Switch to `Place Order` tab.
  - Select Customer `Anita (Chennai)`.
  - Select Product `Keyboard` (Price ₹1299.00), quantity `1` -> click `Add to Cart`.
  - Select Product `Notebook` (Price ₹120.00), quantity `3` -> click `Add to Cart`.
  - Observe Cart Table:
    - Row 1: Keyboard, Unit ₹1299.00, Qty 1, Line Total ₹1299.00
    - Row 2: Notebook, Unit ₹120.00, Qty 3, Line Total ₹360.00
    - Grand Total: `₹1659.00`
  - Click `Confirm & Place Order`.
- **Expected Result**:
  - Success alert displays new Order ID (e.g. #1008), customer name, items count (2), total ₹1659.00, status `Pending`.
  - Cart resets automatically.
  - Product stock is decremented in database (Keyboard stock drops by 1, Notebook stock drops by 3).

### Test 5: Stock Deficit Rollback (Zero Partial Orders)
- **Action**:
  - In `Place Order` tab, select Customer `Bharath`.
  - Add `Water Bottle`, quantity `2` (Available stock is plenty).
  - Add `Desk Lamp`, quantity `1` (Desk Lamp has stock = 0).
  - Click `Confirm & Place Order`.
- **Expected Result**:
  - Error dialog displays: `TRANSACTION ROLLED BACK: Insufficient inventory! Insufficient stock for product 'Desk Lamp' (ID: 5). Available: 0, Requested: 1. Zero partial orders were created.`
  - In `Order History` tab, no order was placed.
  - Water Bottle stock was NOT decremented (complete atomic rollback).

### Test 6: Order Cancellation with Inventory Restoration
- **Action**:
  - In `Orders & History` tab, select order #1002 (Shipped, contains 1 Keyboard).
  - Note current Keyboard stock in `Products & Stock` tab.
  - Click `✕ Cancel Order (Restore Stock)` and confirm.
- **Expected Result**:
  - Order #1002 status updates to `Cancelled`.
  - Keyboard inventory stock is automatically incremented by 1.
  - Confirmation alert reports: `Order #1002 has been Cancelled. Stock has been restored successfully!`

### Test 7: 15 Evaluation Reports Verification
- **Action**:
  - Open `15 SQL Reports` tab.
  - Cycle through all 15 questions from Q1 to Q15.
- **Expected Result**:
  - Each selection displays question text, difficulty badge, and exact SQL query.
  - Table outputs match the answer key exactly:
    - **Q1**: 2 rows (`Wireless Mouse` 599.00, `Keyboard` 1299.00)
    - **Q2**: 2 rows (`Anita`, `Dinesh`)
    - **Q3**: 4 rows (Backpack, Keyboard, Desk Lamp, Wireless Mouse)
    - **Q4**: 1 row (`Desk Lamp`)
    - **Q5**: 4 rows (`Cancelled 1`, `Delivered 4`, `Pending 1`, `Shipped 1`)
    - **Q6**: 4 rows (Orders 1001, 1003, 1005, 1006)
    - **Q7**: 4 rows (Orders 1004, 1005, 1006, 1007)
    - **Q8**: 4 rows (`Desk Lamp`, `Water Bottle`, `Notebook`, `Pen Pack`)
    - **Q9**: 11 rows (Line items with calculated line totals)
    - **Q10**: 7 rows (Order totals matching 1798.00, 1299.00, 1850.00, 599.00, 2099.00, 1898.00, 700.00)
    - **Q11**: 3 rows (Electronics ₹3096.00, Stationery ₹2600.00, Home ₹1949.00)
    - **Q12**: 3 rows (Bharath ₹3748.00, Dinesh ₹2099.00, Anita ₹1798.00)
    - **Q13**: 1 row (`Backpack`)
    - **Q14**: 3 rows (`Anita 2`, `Bharath 2`, `Cathy 2`)
    - **Q15**: 7 rows (`Notebook 15`, `Pen Pack 10`, `Water Bottle 5`, `Wireless Mouse 3`, `Keyboard 2`, `Desk Lamp 1`, `Backpack 0`)
