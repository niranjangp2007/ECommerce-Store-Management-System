package com.ecommerce.ui;

import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.Product;
import com.ecommerce.service.CustomerService;
import com.ecommerce.service.DatabaseOperationException;
import com.ecommerce.service.InsufficientStockException;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.ProductService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Enterprise Order Creation & Cart Management screen.
 * Features:
 * - Real-time customer selection with quick details preview
 * - Searchable product catalog selection with live price, category and stock indicators
 * - Fine-grained quantity adjustments (Add, Increase, Decrease, Remove)
 * - Transparent live subtotal and grand total calculations
 * - Structured atomic checkout confirmation modal
 * - Strict rollback preservation for InsufficientStockException and database failures
 */
public class OrderCreatePanel extends JPanel {

    private final CustomerService customerService;
    private final ProductService productService;
    private final OrderService orderService;
    private final MainFrame mainFrame;

    // Cart Items State
    private final List<OrderItem> cartItems = new ArrayList<>();
    private final List<Product> cachedProducts = new ArrayList<>();

    // Header & Summary Badges
    private JLabel lblCartBadge;
    private JLabel lblItemsCountSummary;
    private JLabel lblDistinctCountSummary;
    private JLabel lblGrandTotal;

    // Customer Selection Components
    private JComboBox<CustomerItem> cmbCustomer;
    private JLabel lblCustomerDetails;

    // Product Selector Components
    private JTextField txtProductSearch;
    private JComboBox<ProductItem> cmbProduct;
    private JLabel lblProductCategory;
    private JLabel lblUnitPrice;
    private JLabel lblAvailableStock;
    private JSpinner spinQuantity;
    private JButton btnAddToCart;

    // Cart Table & Controls
    private JTable cartTable;
    private DefaultTableModel cartTableModel;
    private JButton btnIncreaseQty;
    private JButton btnDecreaseQty;
    private JButton btnRemoveSelected;
    private JButton btnClearCart;
    private JButton btnCheckout;

    public OrderCreatePanel(MainFrame mainFrame, CustomerService customerService,
                            ProductService productService, OrderService orderService) {
        this.mainFrame = mainFrame;
        this.customerService = customerService;
        this.productService = productService;
        this.orderService = orderService;

        setLayout(new BorderLayout(14, 14));
        setBackground(UIUtils.BG_LIGHT);
        setBorder(new EmptyBorder(18, 20, 20, 20));

        initComponents();
        reloadDropdowns();
    }

    private void initComponents() {
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);

        // 1. Header Panel
        mainContent.add(createHeaderPanel());
        mainContent.add(Box.createVerticalStrut(14));

        // 2. Top Grid: Step 1 (Customer Selection) & Step 2 (Product Selector)
        mainContent.add(createSelectorsGrid());
        mainContent.add(Box.createVerticalStrut(14));

        // 3. Center: Shopping Cart Table Card
        mainContent.add(createCartTableCard());
        mainContent.add(Box.createVerticalStrut(14));

        // 4. Bottom: Order Summary & Atomic Checkout Card
        mainContent.add(createCheckoutSummaryCard());

        // Wrap in clean borderless scrollpane for adaptive resizing
        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Professional page header with title, subtitle, cart badge, and clear trigger.
     */
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JPanel titleBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titleBox.setOpaque(false);

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);

        JLabel title = new JLabel("Place Order");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(17, 24, 39));

        JLabel sub = new JLabel("Create multi-item orders with atomic stock validation and real-time checkout.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(107, 114, 128));

        textCol.add(title);
        textCol.add(Box.createVerticalStrut(2));
        textCol.add(sub);
        titleBox.add(textCol);

        lblCartBadge = UIUtils.createPillBadge("0 Items in Cart", new Color(239, 246, 255), new Color(37, 99, 235));
        titleBox.add(lblCartBadge);

        header.add(titleBox, BorderLayout.WEST);

        // Compact Reload Button (Aligned top-right, approx 40–42px high)
        JButton btnReloadAll = UIUtils.createSecondaryButton("↻ Reload Data");
        btnReloadAll.setPreferredSize(new Dimension(130, 40));
        btnReloadAll.setMaximumSize(new Dimension(130, 42));
        btnReloadAll.addActionListener(e -> reloadDropdowns());

        JPanel rightBox = new JPanel(new GridBagLayout());
        rightBox.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        rightBox.add(btnReloadAll, gbc);

        header.add(rightBox, BorderLayout.EAST);

        return header;
    }

    /**
     * 2-Column Selectors Grid:
     * - Left: Step 1 Select Customer
     * - Right: Step 2 Select Product, Stock & Quantity
     */
    private JPanel createSelectorsGrid() {
        JPanel grid = new JPanel(new GridLayout(1, 2, 14, 14));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        // ---------------- Left: Step 1 Customer Selector ----------------
        JPanel custCard = createCardPanel("Step 1: Select Customer");
        custCard.setLayout(new BoxLayout(custCard, BoxLayout.Y_AXIS));

        JPanel custRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        custRow.setOpaque(false);

        JLabel lblCust = new JLabel("Customer:");
        lblCust.setFont(UIUtils.BOLD_FONT);
        lblCust.setForeground(new Color(55, 65, 81));

        cmbCustomer = new JComboBox<>();
        cmbCustomer.setPreferredSize(new Dimension(260, 30));
        cmbCustomer.addActionListener(e -> updateCustomerDetailsDisplay());

        JButton btnReloadCust = UIUtils.createSecondaryButton("↻");
        btnReloadCust.setPreferredSize(new Dimension(38, 30));
        btnReloadCust.setToolTipText("Reload Customer Directory");
        btnReloadCust.addActionListener(e -> reloadCustomers());

        custRow.add(lblCust);
        custRow.add(cmbCustomer);
        custRow.add(btnReloadCust);
        custCard.add(custRow);

        lblCustomerDetails = new JLabel("Select a registered customer to start order");
        lblCustomerDetails.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCustomerDetails.setForeground(new Color(107, 114, 128));
        lblCustomerDetails.setBorder(new EmptyBorder(6, 12, 4, 12));
        custCard.add(lblCustomerDetails);

        grid.add(custCard);

        // ---------------- Right: Step 2 Product Selector ----------------
        JPanel prodCard = createCardPanel("Step 2: Add Products to Cart");
        prodCard.setLayout(new BoxLayout(prodCard, BoxLayout.Y_AXIS));

        // Row 1: Search & Product Dropdown
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        searchRow.setOpaque(false);

        txtProductSearch = new JTextField(12);
        txtProductSearch.setPreferredSize(new Dimension(130, 28));
        txtProductSearch.putClientProperty("JTextField.placeholderText", "Search product...");
        txtProductSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterProductDropdown(txtProductSearch.getText().trim());
            }
        });

        cmbProduct = new JComboBox<>();
        cmbProduct.setPreferredSize(new Dimension(240, 28));
        cmbProduct.addActionListener(e -> updateProductDetailsDisplay());

        searchRow.add(new JLabel("Product:"));
        searchRow.add(txtProductSearch);
        searchRow.add(cmbProduct);
        prodCard.add(searchRow);

        // Row 2: Live Details Badges (Category, Price, Stock) + Quantity + Add Button
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        actionRow.setOpaque(false);

        lblUnitPrice = new JLabel("Price: ₹0.00");
        lblUnitPrice.setFont(UIUtils.BOLD_FONT);
        lblUnitPrice.setForeground(UIUtils.NAV_ACTIVE_ACCENT);

        lblAvailableStock = new JLabel("Stock: 0");
        lblAvailableStock.setFont(UIUtils.BOLD_FONT);

        lblProductCategory = new JLabel("Category: -");
        lblProductCategory.setFont(UIUtils.REGULAR_FONT);
        lblProductCategory.setForeground(new Color(107, 114, 128));

        // Quantity Spinner with decrement & increment buttons
        JButton btnMinus = UIUtils.createSecondaryButton("-");
        btnMinus.setPreferredSize(new Dimension(30, 28));

        spinQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        spinQuantity.setPreferredSize(new Dimension(55, 28));

        JButton btnPlus = UIUtils.createSecondaryButton("+");
        btnPlus.setPreferredSize(new Dimension(30, 28));

        btnMinus.addActionListener(e -> {
            int val = (Integer) spinQuantity.getValue();
            if (val > 1) spinQuantity.setValue(val - 1);
        });
        btnPlus.addActionListener(e -> {
            int val = (Integer) spinQuantity.getValue();
            spinQuantity.setValue(val + 1);
        });

        btnAddToCart = UIUtils.createPrimaryButton("+ Add to Cart");
        btnAddToCart.setPreferredSize(new Dimension(120, 28));
        btnAddToCart.addActionListener(e -> addItemToCart());

        actionRow.add(lblUnitPrice);
        actionRow.add(lblAvailableStock);
        actionRow.add(lblProductCategory);
        actionRow.add(Box.createHorizontalStrut(8));
        actionRow.add(btnMinus);
        actionRow.add(spinQuantity);
        actionRow.add(btnPlus);
        actionRow.add(btnAddToCart);

        prodCard.add(actionRow);
        grid.add(prodCard);

        return grid;
    }

    /**
     * Step 3: Shopping Cart Table with item controls.
     */
    private JPanel createCartTableCard() {
        JPanel card = createCardPanel("Step 3: Shopping Cart Items");
        card.setLayout(new BorderLayout(8, 8));

        String[] columns = {"#", "Product ID", "Product Name", "Unit Price (₹)", "Quantity", "Line Total (₹)"};
        cartTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        cartTable = new JTable(cartTableModel);
        UIUtils.formatTable(cartTable);

        // Center alignments
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        cartTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        cartTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        cartTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        // Currency format right aligned
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        cartTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        cartTable.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);

        // Column widths
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(90);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(260);

        // Update button states on selection
        cartTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = cartTable.getSelectedRow() >= 0;
            btnIncreaseQty.setEnabled(hasSelection);
            btnDecreaseQty.setEnabled(hasSelection);
            btnRemoveSelected.setEnabled(hasSelection);
        });

        JScrollPane scrollCart = new JScrollPane(cartTable);
        scrollCart.setPreferredSize(new Dimension(500, 200));
        scrollCart.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1));
        card.add(scrollCart, BorderLayout.CENTER);

        // Item Adjustment Toolbar below table
        JPanel cartToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        cartToolbar.setOpaque(false);

        btnIncreaseQty = UIUtils.createSecondaryButton("[+] Increase Qty");
        btnIncreaseQty.setPreferredSize(new Dimension(130, 28));
        btnIncreaseQty.setEnabled(false);
        btnIncreaseQty.addActionListener(e -> increaseSelectedCartItem());

        btnDecreaseQty = UIUtils.createSecondaryButton("[-] Decrease Qty");
        btnDecreaseQty.setPreferredSize(new Dimension(130, 28));
        btnDecreaseQty.setEnabled(false);
        btnDecreaseQty.addActionListener(e -> decreaseSelectedCartItem());

        btnRemoveSelected = UIUtils.createDangerButton("✕ Remove Item");
        btnRemoveSelected.setPreferredSize(new Dimension(120, 28));
        btnRemoveSelected.setEnabled(false);
        btnRemoveSelected.addActionListener(e -> removeSelectedItem());

        btnClearCart = UIUtils.createSecondaryButton("Clear Cart");
        btnClearCart.setPreferredSize(new Dimension(95, 28));
        btnClearCart.addActionListener(e -> clearCart());

        cartToolbar.add(btnIncreaseQty);
        cartToolbar.add(btnDecreaseQty);
        cartToolbar.add(btnRemoveSelected);
        cartToolbar.add(btnClearCart);

        card.add(cartToolbar, BorderLayout.SOUTH);
        return card;
    }

    /**
     * Bottom Summary & Atomic Checkout Card.
     */
    private JPanel createCheckoutSummaryCard() {
        JPanel bar = new JPanel(new BorderLayout(16, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(new Color(229, 231, 235));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(14, 18, 14, 18));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));

        // Left Summary Details
        JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        leftBox.setOpaque(false);

        lblItemsCountSummary = new JLabel("Total Units: 0");
        lblItemsCountSummary.setFont(UIUtils.BOLD_FONT);
        lblItemsCountSummary.setForeground(new Color(55, 65, 81));

        lblDistinctCountSummary = new JLabel("Distinct Products: 0");
        lblDistinctCountSummary.setFont(UIUtils.REGULAR_FONT);
        lblDistinctCountSummary.setForeground(new Color(107, 114, 128));

        JLabel lblSecurity = new JLabel("• Atomic JDBC Transaction Protected");
        lblSecurity.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSecurity.setForeground(UIUtils.SUCCESS_COLOR);

        leftBox.add(lblItemsCountSummary);
        leftBox.add(lblDistinctCountSummary);
        leftBox.add(lblSecurity);
        bar.add(leftBox, BorderLayout.WEST);

        // Right Grand Total & Checkout Button
        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightBox.setOpaque(false);

        JLabel lblTotalText = new JLabel("Order Grand Total:");
        lblTotalText.setFont(UIUtils.SUBTITLE_FONT);
        lblTotalText.setForeground(new Color(55, 65, 81));

        lblGrandTotal = new JLabel("₹0.00");
        lblGrandTotal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblGrandTotal.setForeground(UIUtils.NAV_ACTIVE_ACCENT);

        btnCheckout = UIUtils.createPrimaryButton("✔ Confirm & Place Order");
        btnCheckout.setPreferredSize(new Dimension(210, 36));
        btnCheckout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCheckout.addActionListener(e -> checkoutOrder());

        rightBox.add(lblTotalText);
        rightBox.add(lblGrandTotal);
        rightBox.add(btnCheckout);

        bar.add(rightBox, BorderLayout.EAST);
        return bar;
    }

    private JPanel createCardPanel(String title) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(new Color(229, 231, 235));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UIUtils.SUBTITLE_FONT);
        lblTitle.setForeground(new Color(31, 41, 55));
        card.add(lblTitle);

        return card;
    }

    public void reloadDropdowns() {
        reloadCustomers();
        reloadProducts();
    }

    public void reloadCustomers() {
        try {
            CustomerItem current = (CustomerItem) cmbCustomer.getSelectedItem();
            cmbCustomer.removeAllItems();
            List<Customer> list = customerService.getAllCustomers();
            for (Customer c : list) {
                cmbCustomer.addItem(new CustomerItem(c));
            }
            if (current != null) {
                for (int i = 0; i < cmbCustomer.getItemCount(); i++) {
                    if (cmbCustomer.getItemAt(i).getId() == current.getId()) {
                        cmbCustomer.setSelectedIndex(i);
                        break;
                    }
                }
            }
            updateCustomerDetailsDisplay();
        } catch (SQLException ignored) {
        }
    }

    public void reloadProducts() {
        try {
            cachedProducts.clear();
            cachedProducts.addAll(productService.getAllProducts());
            filterProductDropdown(txtProductSearch != null ? txtProductSearch.getText().trim() : "");
        } catch (SQLException ignored) {
        }
    }

    private void filterProductDropdown(String query) {
        ProductItem current = (ProductItem) cmbProduct.getSelectedItem();
        cmbProduct.removeAllItems();

        String q = query.toLowerCase();
        for (Product p : cachedProducts) {
            if (q.isEmpty() || p.getName().toLowerCase().contains(q) ||
                p.getCategory().toLowerCase().contains(q) || String.valueOf(p.getProductId()).contains(q)) {
                cmbProduct.addItem(new ProductItem(p));
            }
        }

        if (current != null) {
            for (int i = 0; i < cmbProduct.getItemCount(); i++) {
                if (cmbProduct.getItemAt(i).getId() == current.getId()) {
                    cmbProduct.setSelectedIndex(i);
                    break;
                }
            }
        }
        updateProductDetailsDisplay();
    }

    private void updateCustomerDetailsDisplay() {
        CustomerItem item = (CustomerItem) cmbCustomer.getSelectedItem();
        if (item != null) {
            lblCustomerDetails.setText("Selected Customer: #" + item.getId() + " — " + item.getName() + " (" + item.getCity() + ")");
            lblCustomerDetails.setForeground(new Color(37, 99, 235));
        } else {
            lblCustomerDetails.setText("No customer selected.");
            lblCustomerDetails.setForeground(UIUtils.DANGER_COLOR);
        }
    }

    private void updateProductDetailsDisplay() {
        ProductItem item = (ProductItem) cmbProduct.getSelectedItem();
        if (item != null) {
            Product p = item.getProduct();
            lblUnitPrice.setText("Price: ₹" + p.getPrice());
            lblProductCategory.setText("Category: " + p.getCategory());

            if (p.isOutOfStock()) {
                lblAvailableStock.setText("Stock: 0 (OUT OF STOCK)");
                lblAvailableStock.setForeground(UIUtils.DANGER_COLOR);
                btnAddToCart.setEnabled(false);
            } else if (p.getStock() <= 5) {
                lblAvailableStock.setText("Stock: " + p.getStock() + " (Low Stock)");
                lblAvailableStock.setForeground(UIUtils.WARNING_COLOR);
                btnAddToCart.setEnabled(true);
            } else {
                lblAvailableStock.setText("Stock: " + p.getStock() + " (In Stock)");
                lblAvailableStock.setForeground(UIUtils.SUCCESS_COLOR);
                btnAddToCart.setEnabled(true);
            }
        } else {
            lblUnitPrice.setText("Price: ₹0.00");
            lblAvailableStock.setText("Stock: 0");
            lblProductCategory.setText("Category: -");
            btnAddToCart.setEnabled(false);
        }
    }

    private void addItemToCart() {
        ProductItem selected = (ProductItem) cmbProduct.getSelectedItem();
        if (selected == null) {
            UIUtils.showError(this, "Please select a product to add.");
            return;
        }

        Product p = selected.getProduct();
        int requestedQty = (Integer) spinQuantity.getValue();

        if (requestedQty <= 0) {
            UIUtils.showError(this, "Quantity must be at least 1.");
            return;
        }

        // Calculate quantity already in cart
        int inCartQty = 0;
        for (OrderItem item : cartItems) {
            if (item.getProductId() == p.getProductId()) {
                inCartQty += item.getQuantity();
            }
        }

        if (inCartQty + requestedQty > p.getStock()) {
            UIUtils.showError(this, String.format(
                    "Insufficient stock for '%s'!\nAvailable inventory: %d units\nAlready in cart: %d units\nCannot add %d more.",
                    p.getName(), p.getStock(), inCartQty, requestedQty));
            return;
        }

        boolean existing = false;
        for (OrderItem item : cartItems) {
            if (item.getProductId() == p.getProductId()) {
                item.setQuantity(item.getQuantity() + requestedQty);
                existing = true;
                break;
            }
        }

        if (!existing) {
            OrderItem newItem = new OrderItem(0, 0, p.getProductId(), requestedQty, p.getName(), p.getPrice());
            cartItems.add(newItem);
        }

        spinQuantity.setValue(1);
        refreshCartTable();
    }

    private void increaseSelectedCartItem() {
        int row = cartTable.getSelectedRow();
        if (row < 0) return;

        OrderItem item = cartItems.get(row);
        Product p = findProductById(item.getProductId());
        if (p != null) {
            if (item.getQuantity() + 1 > p.getStock()) {
                UIUtils.showError(this, "Cannot increase quantity for '" + p.getName() + "'. Maximum available stock is " + p.getStock() + ".");
                return;
            }
        }

        item.setQuantity(item.getQuantity() + 1);
        refreshCartTable();
        cartTable.setRowSelectionInterval(row, row);
    }

    private void decreaseSelectedCartItem() {
        int row = cartTable.getSelectedRow();
        if (row < 0) return;

        OrderItem item = cartItems.get(row);
        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            refreshCartTable();
            cartTable.setRowSelectionInterval(row, row);
        } else {
            removeSelectedItem();
        }
    }

    private void removeSelectedItem() {
        int row = cartTable.getSelectedRow();
        if (row < 0) {
            UIUtils.showError(this, "Please select an item from the cart to remove.");
            return;
        }
        cartItems.remove(row);
        refreshCartTable();
    }

    private void clearCart() {
        cartItems.clear();
        refreshCartTable();
    }

    private Product findProductById(int productId) {
        for (Product p : cachedProducts) {
            if (p.getProductId() == productId) return p;
        }
        return null;
    }

    private void refreshCartTable() {
        cartTableModel.setRowCount(0);
        BigDecimal grandTotal = BigDecimal.ZERO;
        int totalUnits = 0;

        int index = 1;
        for (OrderItem item : cartItems) {
            BigDecimal lineTotal = item.getLineTotal();
            grandTotal = grandTotal.add(lineTotal);
            totalUnits += item.getQuantity();

            cartTableModel.addRow(new Object[]{
                    index++,
                    item.getProductId(),
                    item.getProductName(),
                    String.format("₹%.2f", item.getUnitPrice()),
                    item.getQuantity(),
                    String.format("₹%.2f", lineTotal)
            });
        }

        lblGrandTotal.setText("₹" + grandTotal.setScale(2, RoundingMode.HALF_UP));
        lblItemsCountSummary.setText("Total Units: " + totalUnits);
        lblDistinctCountSummary.setText("Distinct Products: " + cartItems.size());
        lblCartBadge.setText(totalUnits + " Items in Cart");

        boolean hasItems = !cartItems.isEmpty();
        btnCheckout.setEnabled(hasItems);
        btnClearCart.setEnabled(hasItems);
    }

    /**
     * Executes atomic order checkout with structured confirmation modal and error rollback handling.
     */
    private void checkoutOrder() {
        CustomerItem customer = (CustomerItem) cmbCustomer.getSelectedItem();
        if (customer == null) {
            UIUtils.showError(this, "Please select a customer before placing the order.");
            return;
        }

        if (cartItems.isEmpty()) {
            UIUtils.showError(this, "Your cart is empty! Please add products before placing an order.");
            return;
        }

        // Custom Confirmation Dialog
        JDialog confirmDialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Confirm Order Checkout", Dialog.ModalityType.APPLICATION_MODAL);
        confirmDialog.setLayout(new BorderLayout());
        confirmDialog.setResizable(false);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(24, 34, 54));
        header.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel lblTitle = new JLabel("Review & Confirm Order");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Atomic transaction: Verifies inventory and reserves stock atomically.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(180, 195, 215));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(lblSub);
        header.add(titleBox, BorderLayout.WEST);
        confirmDialog.add(header, BorderLayout.NORTH);

        // Details Body
        JPanel body = new JPanel(new GridLayout(4, 1, 6, 6));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(16, 20, 16, 20));

        body.add(new JLabel("Customer: " + customer.getName() + " (#" + customer.getId() + " - " + customer.getCity() + ")"));
        body.add(new JLabel("Total Items: " + cartItems.size() + " distinct product lines"));
        body.add(new JLabel("Total Amount Payable: " + lblGrandTotal.getText()));
        JLabel lblNotice = new JLabel("Database Transaction: All items will commit atomically or rollback on error.");
        lblNotice.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblNotice.setForeground(UIUtils.PRIMARY_COLOR);
        body.add(lblNotice);

        confirmDialog.add(body, BorderLayout.CENTER);

        // Buttons Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(new Color(245, 247, 250));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)));

        final boolean[] userConfirmed = {false};

        JButton btnCancel = UIUtils.createSecondaryButton("Cancel");
        btnCancel.setPreferredSize(new Dimension(85, 30));
        btnCancel.addActionListener(e -> confirmDialog.dispose());

        JButton btnConfirm = UIUtils.createPrimaryButton("Confirm Checkout");
        btnConfirm.setPreferredSize(new Dimension(145, 30));
        btnConfirm.addActionListener(e -> {
            userConfirmed[0] = true;
            confirmDialog.dispose();
        });

        footer.add(btnCancel);
        footer.add(btnConfirm);
        confirmDialog.add(footer, BorderLayout.SOUTH);

        confirmDialog.pack();
        confirmDialog.setLocationRelativeTo(this);
        confirmDialog.setVisible(true);

        if (!userConfirmed[0]) {
            return;
        }

        try {
            // Transactional Order Placement via OrderService
            Order newOrder = orderService.createOrder(customer.getId(), cartItems);

            UIUtils.showInfo(this, String.format(
                    "Order #%d created successfully!\n\nCustomer: %s\nTotal Items: %d\nGrand Total: ₹%s\nStatus: %s",
                    newOrder.getOrderId(), newOrder.getCustomerName(), newOrder.getItems().size(),
                    newOrder.calculateTotal(), newOrder.getStatus()));

            clearCart();
            reloadProducts();
            mainFrame.refreshAllPanels();

        } catch (InsufficientStockException e) {
            UIUtils.showError(this, "TRANSACTION ROLLED BACK: Insufficient inventory!\n\n" + e.getMessage() +
                    "\nZero partial orders were created.");
            reloadProducts();
        } catch (DatabaseOperationException e) {
            UIUtils.showError(this, "Order processing error: " + e.getMessage());
        } catch (SQLException e) {
            UIUtils.showError(this, "Database transaction error: " + e.getMessage());
        }
    }

    // ComboBox Wrappers
    private static class CustomerItem {
        private final Customer customer;
        public CustomerItem(Customer customer) { this.customer = customer; }
        public int getId() { return customer.getCustomerId(); }
        public String getName() { return customer.getName(); }
        public String getCity() { return customer.getCity(); }
        @Override
        public String toString() {
            return String.format("#%d — %s (%s)", customer.getCustomerId(), customer.getName(), customer.getCity());
        }
    }

    private static class ProductItem {
        private final Product product;
        public ProductItem(Product product) { this.product = product; }
        public int getId() { return product.getProductId(); }
        public Product getProduct() { return product; }
        @Override
        public String toString() {
            return String.format("#%d — %s [₹%s | Stock: %d]",
                    product.getProductId(), product.getName(), product.getPrice(), product.getStock());
        }
    }
}
