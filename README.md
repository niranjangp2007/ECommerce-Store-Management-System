# 🛍️ E-Commerce Store Management System

[![Java 17](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![MySQL 8.0](https://img.shields.io/badge/MySQL-8.0%2B-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![FlatLaf](https://img.shields.io/badge/UI-FlatLaf%203.4.1-2563EB?style=for-the-badge)](https://www.formdev.com/flatlaf/)
[![Maven 3.9](https://img.shields.io/badge/Build-Maven%203.9-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![JUnit 5](https://img.shields.io/badge/Testing-JUnit%205-25A162?style=for-the-badge&logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](LICENSE)

A robust, enterprise-grade desktop **E-Commerce Store Management System** built with **Java 17**, **Swing GUI**, **FlatLaf Look & Feel**, **JDBC**, and **MySQL 8**. The project models an end-to-end retail store workflow adhering to **Object-Oriented Programming (OOP)** principles, strict **ACID transactional consistency**, and includes an integrated **15 SQL Academic Evaluation Reports Engine**.

---

## 📌 Table of Contents
1. [Core Features & Modules](#-core-features--modules)
2. [Technologies Used](#-technologies-used)
3. [System Architecture](#-system-architecture)
4. [OOP Concepts Implemented](#-oop-concepts-implemented)
5. [Database Schema & ER Model](#-database-schema--er-model)
6. [Prerequisites & System Requirements](#-prerequisites--system-requirements)
7. [Installation & Setup Guide](#-installation--setup-guide)
8. [Configuration & Environment Variables](#-configuration--environment-variables)
9. [Build, Test & Execution Commands](#-build-test--execution-commands)
10. [Automated Testing Suite](#-automated-testing-suite)
11. [SQL Academic Evaluation Reports (Q1 – Q15)](#-sql-academic-evaluation-reports-q1--q15)
12. [User Interface Screenshots](#-user-interface-screenshots)
13. [Limitations & Future Enhancements](#-limitations--future-enhancements)
14. [Author & Credits](#-author--credits)

---

## 🚀 Core Features & Modules

### 1. 📊 Executive Dashboard (`DashboardPanel`)
- **Key Performance Indicators (KPIs):** Instant metrics for Total Customers, Total Catalog Products, Out-of-Stock Alerts, and Lifetime Orders.
- **Order Distribution:** Real-time percentage share of orders grouped by lifecycle state (`Delivered`, `Shipped`, `Pending`, `Cancelled`).
- **Inventory Alerts:** Dynamic low-stock radar tracking items with available inventory $\le 5$ units.
- **Recent Transactions:** Quick-view table tracking the latest 5 placed customer orders.
- **Connection Health Banner:** Proactive status banner showing database connectivity diagnostics and retry triggers.

### 2. 👥 Customer Management (`CustomerPanel`)
- **Full CRUD Support:** Register new customers, update profiles (Name, City, Join Date), and view comprehensive directories.
- **Referential Integrity Protection:** Deletion blocks prevent deleting customers who have linked transaction history.
- **Search & Filtering:** Real-time search across Customer ID, Name, and City.
- **Zebra Tables & Badges:** Professional tabular layout with custom cell renderers and count badges.

### 3. 📦 Products & Inventory Management (`ProductPanel`)
- **Catalog Management:** Add and update products with strict domain validation (non-negative pricing, positive stock, non-empty categories).
- **Stock Status Badges:** 3-tier visual indicators: `In Stock` (Green), `Low Stock` (Amber $\le 5$), and `OUT OF STOCK` (Red).
- **Search & Categorization:** Filter products by classification (`Electronics`, `Stationery`, `Home`, `Bags`) or search query.
- **Referential Protection:** Foreign-key deletion guards prevent deleting products that appear in past order line items.

### 4. 🛒 Transaction-Safe Place Order (`OrderCreatePanel`)
- **Interactive Multi-Product Cart:** Add multiple distinct products, adjust item quantities ($+ / -$), or clear cart.
- **Live Computations:** Real-time calculation of unit prices, line totals, and grand total amount in Indian Rupees (₹).
- **ACID Transaction Atomicity:** Orders are wrapped in a single database transaction (`setAutoCommit(false)`):
  - Pre-validates stock for all products using `SELECT ... FOR UPDATE` (pessimistic lock).
  - Automatically aborts and rolls back entire transaction if any item exceeds available stock (`InsufficientStockException`).
  - Deducts inventory atomically upon successful commit (`UPDATE products SET stock = stock - ?`).

### 5. 📜 Orders & History Ledger (`OrderHistoryPanel`)
- **Order Tracking:** Complete audit ledger with color-coded status badges (`Delivered`, `Shipped`, `Pending`, `Cancelled`).
- **Detailed Inspection Modal:** View full purchase breakdown with itemized products, quantities, unit prices, and line totals.
- **Order Cancellation with Inventory Restoration:** Cancelling an order updates status to `Cancelled` and automatically refunds the purchased quantities back to the warehouse product stock in a single atomic transaction.
- **Filter by Status:** Isolate orders by lifecycle state or search by Order ID and Customer Name.

### 6. 📑 Academic SQL Reports Engine (`ReportsPanel`)
- Built-in SQL evaluator executing all 15 academic syllabus evaluation queries across Easy and Medium difficulties.
- Renders results dynamically into Swing table models with metadata, execution duration, and query prompts.

---

## 🛠️ Technologies Used

- **Programming Language:** Java 17 (Targeted LTS release)
- **GUI Toolkit:** Java Swing (`JFrame`, `JPanel`, `JTable`, `CardLayout`, `BorderLayout`, `BoxLayout`, `GridBagLayout`)
- **Look & Feel:** [FlatLaf 3.4.1](https://www.formdev.com/flatlaf/) (Modern flat design with native scaling)
- **Database Engine:** MySQL Server 8.0+
- **Database Connectivity:** JDBC (Java Database Connectivity) via [MySQL Connector/J 8.3.0](https://dev.mysql.com/downloads/connector/j/)
- **Build System:** Apache Maven 3.9+
- **Unit & Integration Testing:** JUnit Jupiter (JUnit 5.10.2)
- **Architecture Pattern:** Layered Architecture with Data Access Object (DAO) Pattern

---

## 🏗️ System Architecture

The application is structured into four distinct, loosely coupled layers:

```
┌────────────────────────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER (Java Swing GUI)                  │
│   MainFrame │ SidebarPanel │ DashboardPanel │ CustomerPanel            │
│   ProductPanel │ OrderCreatePanel │ OrderHistoryPanel │ ReportsPanel   │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ Invokes
┌───────────────────────────────────▼────────────────────────────────────┐
│                  SERVICE LAYER (Business & Transactions)               │
│   CustomerService │ ProductService │ OrderService │ ReportService      │
│   • Input Validation  • Business Rules  • JDBC Transaction Boundaries   │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ Uses
┌───────────────────────────────────▼────────────────────────────────────┐
│                    DATA ACCESS LAYER (DAO Interfaces)                  │
│   CustomerDAO  ProductDAO  OrderDAO  OrderItemDAO  ReportDAO           │
│   (Implementations: CustomerDAOImpl, ProductDAOImpl, etc.)             │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ Queries via PreparedStatements
┌───────────────────────────────────▼────────────────────────────────────┐
│                 PERSISTENCE LAYER (MySQL 8 Database)                   │
│   DBConnection Factory ────> MySQL ecom_db                             │
│   (customers, products, orders, order_items)                           │
└────────────────────────────────────────────────────────────────────────┘
```

### Directory Structure

```
D:/ECommerceStore/
├── pom.xml                               # Maven project POM
├── mvn.cmd / mvnw.cmd                    # Maven execution wrappers
├── sql/
│   ├── schema_and_seed.sql               # Database schema & 30 baseline seed records
│   └── all_15_evaluation_queries.sql     # 15 SQL academic evaluation queries
├── docs/
│   ├── architecture_and_oops.md          # Architectural guide & OOP design documentation
│   └── test_cases_and_results.md         # Test specifications & verification records
├── src/
│   ├── main/java/com/ecommerce/
│   │   ├── Main.java                     # Application entry point
│   │   ├── StoreApp.java                 # Entry point alias
│   │   ├── database/
│   │   │   └── DBConnection.java         # JDBC factory with sanitized diagnostic reporting
│   │   ├── model/
│   │   │   ├── Person.java               # Abstract base class
│   │   │   ├── Customer.java             # Subclass inheriting from Person
│   │   │   ├── Product.java              # Encapsulated catalog & inventory model
│   │   │   ├── Order.java                # Composite order header model
│   │   │   └── OrderItem.java            # Normalized line item model
│   │   ├── dao/
│   │   │   ├── CustomerDAO.java & Impl   # Customer CRUD queries
│   │   │   ├── ProductDAO.java & Impl    # Product inventory queries
│   │   │   ├── OrderDAO.java & Impl      # Order persistence queries
│   │   │   ├── OrderItemDAO.java & Impl  # Line item batch operations
│   │   │   └── ReportDAO.java & Impl     # 15 Academic evaluation queries
│   │   ├── service/
│   │   │   ├── CustomerService.java      # Customer validation & rules
│   │   │   ├── ProductService.java       # Inventory validation & alerts
│   │   │   ├── OrderService.java         # Multi-table atomic transactions & stock checks
│   │   │   ├── ReportService.java        # Evaluation reports service
│   │   │   ├── InsufficientStockException.java
│   │   │   └── DatabaseOperationException.java
│   │   ├── reports/
│   │   │   └── ReportResult.java         # Dynamic TableModel report wrapper
│   │   └── ui/
│   │       ├── MainFrame.java            # Main window (Sidebar + CardLayout)
│   │       ├── SidebarPanel.java         # Dark navy sidebar navigation
│   │       ├── DashboardPanel.java       # Executive KPI dashboard
│   │       ├── CustomerPanel.java        # Customer management screen
│   │       ├── ProductPanel.java         # Inventory catalog screen
│   │       ├── OrderCreatePanel.java     # Shopping cart & atomic checkout
│   │       ├── OrderHistoryPanel.java    # Order history & cancellation
│   │       ├── ReportsPanel.java         # 15 SQL evaluation reports dialog
│   │       └── UIUtils.java              # Theme constants & vector icon utilities
│   └── test/java/com/ecommerce/
│       ├── database/DBConnectionTest.java
│       ├── model/OOPModelTest.java
│       ├── service/CustomerServiceTest.java
│       ├── service/ProductServiceTest.java
│       ├── service/OrderServiceTransactionTest.java
│       └── reports/EvaluationReportsTest.java
└── README.md
```

---

## 🧩 OOP Concepts Implemented

### 1. Abstraction
- **Abstract Base Classes:** `Person` is declared `abstract` with abstract contracts `public abstract String getRole()` and `public abstract String getSummary()`.
- **Interface Decoupling:** Data operations are abstracted behind DAO interfaces (`CustomerDAO`, `ProductDAO`, etc.), isolating business and presentation logic from database-specific SQL implementations.

### 2. Inheritance
- `Customer extends Person`: Inherits common person attributes (`id`, `name`, `city`) and constructors through `super(id, name, city)`, while extending domain attributes such as `joinDate`.
- Swing component inheritance: Feature panels extend `JPanel`, while dialogs extend `JDialog`.

### 3. Polymorphism
- **Dynamic Method Dispatch:** Polymorphic instances of `Person` resolve overridden methods dynamically at runtime:
  ```java
  Person actor = new Customer(1, "Anita", "Chennai", LocalDate.now());
  System.out.println(actor.getRole());    // Returns "Customer" polymorphically
  System.out.println(actor.getSummary()); // Dispatches Customer's overridden summary
  ```
- **Interface Polymorphism:** Service constructors accept DAO interfaces, allowing dynamic injection of custom or test connection suppliers.

### 4. Encapsulation & Domain Invariants
- All entity attributes (`price`, `stock`, `status`, `quantity`) are `private`.
- Mutators strictly validate parameters:
  - `Product.setPrice(BigDecimal)` rejects values $< 0.00$.
  - `Product.setStock(int)` rejects negative stock.
  - `OrderItem.setQuantity(int)` rejects quantities $\le 0$.
  - Inventory operations are encapsulated inside domain methods: `hasSufficientStock(qty)`, `reduceStock(qty)`, and `restoreStock(qty)`.
- `DBConnection` securely encapsulates database credentials, ensuring passwords are never logged or exposed in diagnostic outputs.

---

## 🗄️ Database Schema & ER Model

Normalized to **Third Normal Form (3NF)** with strict foreign-key constraints:

```mermaid
erDiagram
    CUSTOMERS ||--o{ ORDERS : "places (1:N)"
    PRODUCTS ||--o{ ORDER_ITEMS : "included in (1:N)"
    ORDERS ||--|{ ORDER_ITEMS : "contains (1:N)"

    CUSTOMERS {
        int customer_id PK
        varchar(50) name
        varchar(50) city
        date join_date
    }

    PRODUCTS {
        int product_id PK
        varchar(50) name
        varchar(30) category
        decimal(8,2) price
        int stock
    }

    ORDERS {
        int order_id PK
        int customer_id FK
        date order_date
        varchar(20) status
    }

    ORDER_ITEMS {
        int item_id PK
        int order_id FK
        int product_id FK
        int quantity
    }
```

### Table Specifications

| Table | Primary Key | Foreign Keys | Columns | Description |
| :--- | :--- | :--- | :--- | :--- |
| **`customers`** | `customer_id` | *None* | `customer_id`, `name`, `city`, `join_date` | Registered customer records |
| **`products`** | `product_id` | *None* | `product_id`, `name`, `category`, `price`, `stock` | Catalog merchandise & live inventory |
| **`orders`** | `order_id` | `customer_id` $\rightarrow$ `customers` | `order_id`, `customer_id`, `order_date`, `status` | Order headers & transaction states |
| **`order_items`** | `item_id` | `order_id` $\rightarrow$ `orders`<br>`product_id` $\rightarrow$ `products` | `item_id`, `order_id`, `product_id`, `quantity` | Line items linking orders to products |

---

## 💻 Prerequisites & System Requirements

- **Java Development Kit (JDK):** Version 17 or higher (tested on Java 17 and Java 21)
- **Database:** MySQL Server 8.0 or newer
- **Build Tool:** Apache Maven 3.8+ (or use the included `mvn.cmd` wrapper)
- **Operating System:** Windows, macOS, or Linux

---

## ⚙️ Installation & Setup Guide

### 1. Clone the Repository
```bash
git clone https://github.com/niranjangp2007/ECommerce-Store-Management-System.git
cd ECommerce-Store-Management-System
```

### 2. Set Up the MySQL Database
Ensure your MySQL service is running, then execute the schema and sample seed script:

```bash
# Using MySQL Command Line Client
mysql -u root -p < sql/schema_and_seed.sql
```

*(The script uses `CREATE DATABASE IF NOT EXISTS ecom_db` and `INSERT IGNORE` to safely set up tables without overwriting existing data).*

---

## 🔐 Configuration & Environment Variables

The application connects to MySQL using a secure hierarchy. Set your credentials in your terminal session before launching:

### On Windows (PowerShell):
```powershell
$env:ECOM_DB_URL = "jdbc:mysql://localhost:3306/ecom_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:ECOM_DB_USER = "root"
$env:ECOM_DB_PASSWORD = "your_mysql_password"
```

### On Windows (Command Prompt):
```cmd
set ECOM_DB_URL=jdbc:mysql://localhost:3306/ecom_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
set ECOM_DB_USER=root
set ECOM_DB_PASSWORD=your_mysql_password
```

### On Linux / macOS (Bash / Zsh):
```bash
export ECOM_DB_URL="jdbc:mysql://localhost:3306/ecom_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export ECOM_DB_USER="root"
export ECOM_DB_PASSWORD="your_mysql_password"
```

> **Security Note:** You can also create a `local.properties` or `db.properties` file in the project root with `db.user=root` and `db.password=your_password`. These files are explicitly excluded in `.gitignore` to prevent committing credentials.

---

## 🔨 Build, Test & Execution Commands

### 1. Compile the Project
```bash
.\mvn.cmd clean compile
```

### 2. Execute Automated Tests
```bash
.\mvn.cmd clean test
```

### 3. Launch the Application
```bash
.\mvn.cmd exec:java
```

*Alternatively, run the compiled application directly using `java`:*
```bash
java -cp "target/classes;target/dependency/*" com.ecommerce.Main
```

---

## 🧪 Automated Testing Suite

The project includes **17 automated JUnit 5 tests** verifying architecture, validation logic, and database operations:

```
[INFO] Running com.ecommerce.database.DBConnectionTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0 -- in DBConnectionTest
[INFO] Running com.ecommerce.model.OOPModelTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0 -- in OOPModelTest
[INFO] Running com.ecommerce.reports.EvaluationReportsTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0 -- in EvaluationReportsTest
[INFO] Running com.ecommerce.service.CustomerServiceTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0 -- in CustomerServiceTest
[INFO] Running com.ecommerce.service.OrderServiceTransactionTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0 -- in OrderServiceTransactionTest
[INFO] Running com.ecommerce.service.ProductServiceTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0 -- in ProductServiceTest
[INFO] 
[INFO] Results: Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

- **`OOPModelTest` (7 tests):** Verifies abstract class rules, polymorphism dispatch, and domain mutator encapsulation.
- **`OrderServiceTransactionTest` (2 tests):** Tests transaction rollback atomicity when stock is insufficient.
- **`ProductServiceTest` (2 tests):** Verifies price/stock validation and controlled out-of-stock query behavior.
- **`CustomerServiceTest` (2 tests):** Tests customer input validation and retrieval.
- **`EvaluationReportsTest` (2 tests):** Validates report metadata and executes all 15 queries against an isolated baseline.
- **`DBConnectionTest` (2 tests):** Confirms configuration parsing and verifies error diagnostics never expose passwords.

---

## 📊 SQL Academic Evaluation Reports (Q1 – Q15)

All 15 evaluation queries from [`sql/all_15_evaluation_queries.sql`](file:///d:/ECommerceStore/sql/all_15_evaluation_queries.sql) are integrated into the system:

| # | Title | Difficulty | SQL Concepts Applied |
| :---: | :--- | :---: | :--- |
| **Q1** | Electronics Products | Easy | Simple `SELECT`, `WHERE` string literal filter |
| **Q2** | Chennai Customers | Easy | Projection, equality filtering |
| **Q3** | Products Above 500 | Easy | Numeric comparison, `ORDER BY ... DESC` |
| **Q4** | Out of Stock Products | Easy | Equality zero check (`stock = 0`) |
| **Q5** | Orders Grouped by Status | Easy | `GROUP BY status`, aggregate `COUNT(*)` |
| **Q6** | Delivered Orders | Easy | Inner `JOIN customers`, status filter, date selection |
| **Q7** | Orders in Date Range | Easy | `BETWEEN ... AND ...` date range filtering |
| **Q8** | Home & Stationery Products | Easy | Set membership `IN ('Home', 'Stationery')`, multi-column ordering |
| **Q9** | Order Items with Line Totals | Medium | Multi-table `JOIN`, calculated expressions (`quantity * price AS line_total`) |
| **Q10** | Total Value of Each Order | Medium | `JOIN`, `GROUP BY`, aggregate `SUM(quantity * price)` |
| **Q11** | Revenue by Category | Medium | Multi-table `JOIN`, `WHERE status = 'Delivered'`, `GROUP BY`, `SUM`, `ORDER BY DESC` |
| **Q12** | Top 3 Customers by Spend | Medium | Multi-table `JOIN`, aggregation, `ORDER BY total_spent DESC LIMIT 3` |
| **Q13** | Never Ordered Products | Medium | `LEFT JOIN order_items` where `oi.item_id IS NULL` |
| **Q14** | Repeat Customers | Medium | `JOIN`, `GROUP BY`, filter on aggregation with `HAVING COUNT(*) > 1` |
| **Q15** | Units Sold per Product | Medium | `LEFT JOIN`, conditional aggregation `COALESCE(SUM(CASE WHEN ... END), 0)` |

---

## 🖼️ User Interface Screenshots

| Screen | Description | Preview |
| :--- | :--- | :---: |
| **Dashboard** | Store metrics, order distribution, low stock alerts, and quick actions | *[Dashboard Preview Placeholder]* |
| **Customers** | Customer directory with search, count badges, and modal CRUD dialogs | *[Customer Management Preview Placeholder]* |
| **Products & Inventory** | Inventory catalog, 3-tier status badges, price and stock controls | *[Inventory Preview Placeholder]* |
| **Place Order** | Multi-item interactive shopping cart with live subtotal/grand total | *[Place Order Preview Placeholder]* |
| **Order History** | Order lifecycle ledger, detailed item inspection, and stock-restoring cancellation | *[Order History Preview Placeholder]* |

---

## 🔮 Limitations & Future Enhancements

### Current Limitations:
1. **Desktop Client Architecture:** The client connects directly to MySQL via port 3306 rather than through an intermediate REST API gateway.
2. **Single-User Simulation:** The system currently runs as a store operations console without multi-tenant authentication or role-based access control (RBAC).
3. **Simulated Payment Gateway:** Checkout processes orders directly without real-world credit card or payment gateway SDK processing (e.g., Stripe, Razorpay).

### Future Roadmap:
- [ ] Implement a RESTful microservice backend using **Spring Boot** or **Quarkus**.
- [ ] Add JWT-based multi-user authentication (Cashier, Store Admin, Customer roles).
- [ ] Integrate online payment gateway APIs and webhook handlers.
- [ ] Add automated PDF invoice generation using iText or Apache PDFBox.
- [ ] Implement data visualization charts using JFreeChart.

---

## 👨‍💻 Author & Credits

- **Developer:** Niranjan G P ([@niranjangp2007](https://github.com/niranjangp2007))
- **Project:** Academic E-Commerce Store Management System (OOPs & DBMS)
- **Year:** 2026
