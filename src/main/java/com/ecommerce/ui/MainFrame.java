package com.ecommerce.ui;

import com.ecommerce.database.DBConnection;
import com.ecommerce.service.CustomerService;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.ProductService;
import com.ecommerce.service.ReportService;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

/**
 * Main application frame for the E-Commerce Store Management System.
 * Uses a modern BorderLayout architecture with a professional dark navy SidebarPanel
 * on the WEST and a dynamic CardLayout content container in the CENTER.
 */
public class MainFrame extends JFrame {

    // Tab Index Constants for backward compatibility
    public static final int TAB_DASHBOARD = 0;
    public static final int TAB_CUSTOMERS = 1;
    public static final int TAB_PRODUCTS = 2;
    public static final int TAB_CREATE_ORDER = 3;
    public static final int TAB_ORDER_HISTORY = 4;
    public static final int TAB_REPORTS = 5;

    // Service Layer
    private final CustomerService customerService;
    private final ProductService productService;
    private final OrderService orderService;
    private final ReportService reportService;

    // Navigation & Layout Components
    private SidebarPanel sidebarPanel;
    private CardLayout cardLayout;
    private JPanel contentContainer;

    // Feature Panels
    private DashboardPanel dashboardPanel;
    private CustomerPanel customerPanel;
    private ProductPanel productPanel;
    private OrderCreatePanel orderCreatePanel;
    private OrderHistoryPanel orderHistoryPanel;
    private ReportsPanel reportsPanel;

    public MainFrame() {
        super("E-Commerce Store Management System — OOPs & DBMS Academic Project");

        // Initialize Services
        this.customerService = new CustomerService();
        this.productService = new ProductService();
        this.orderService = new OrderService();
        this.reportService = new ReportService();

        initWindow();
        initLayout();
        updateDbStatus();
    }

    private void initWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 800);
        setMinimumSize(new Dimension(1050, 680));
        setLocationRelativeTo(null);
    }

    /**
     * Initializes the modern layout:
     * - BorderLayout.WEST: SidebarPanel with navigation items and DB monitor
     * - BorderLayout.CENTER: CardLayout containing the five primary feature screens
     */
    private void initLayout() {
        setLayout(new BorderLayout());

        // 1. Sidebar Navigation (WEST)
        sidebarPanel = new SidebarPanel();
        sidebarPanel.addNavSelectionListener(this::showView);
        sidebarPanel.setOnReconnectRequested(this::updateDbStatus);
        add(sidebarPanel, BorderLayout.WEST);

        // 2. CardLayout Content Container (CENTER)
        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);
        contentContainer.setBackground(UIUtils.BG_LIGHT);

        // Initialize Feature Panels
        dashboardPanel = new DashboardPanel(this, customerService, productService, orderService);
        customerPanel = new CustomerPanel(customerService);
        productPanel = new ProductPanel(productService);
        orderCreatePanel = new OrderCreatePanel(this, customerService, productService, orderService);
        orderHistoryPanel = new OrderHistoryPanel(this, orderService);
        reportsPanel = new ReportsPanel(reportService);

        // Add the five primary feature screens to CardLayout
        contentContainer.add(dashboardPanel, SidebarPanel.NAV_DASHBOARD);
        contentContainer.add(customerPanel, SidebarPanel.NAV_CUSTOMERS);
        contentContainer.add(productPanel, SidebarPanel.NAV_PRODUCTS);
        contentContainer.add(orderCreatePanel, SidebarPanel.NAV_CREATE_ORDER);
        contentContainer.add(orderHistoryPanel, SidebarPanel.NAV_ORDER_HISTORY);

        add(contentContainer, BorderLayout.CENTER);

        // Default to Dashboard view
        showView(SidebarPanel.NAV_DASHBOARD);
    }

    /**
     * Switches the active CardLayout card, syncs the sidebar highlight,
     * and triggers lazy data refresh for the destination panel.
     */
    public void showView(String navId) {
        if (navId == null) {
            return;
        }

        cardLayout.show(contentContainer, navId);
        sidebarPanel.setSelectedNav(navId);
        triggerPanelRefresh(navId);
    }

    /**
     * Preserves lazy panel refreshing whenever a screen is navigated to.
     */
    private void triggerPanelRefresh(String navId) {
        switch (navId) {
            case SidebarPanel.NAV_DASHBOARD -> dashboardPanel.refreshData();
            case SidebarPanel.NAV_CUSTOMERS -> customerPanel.loadCustomers();
            case SidebarPanel.NAV_PRODUCTS -> productPanel.loadProducts();
            case SidebarPanel.NAV_CREATE_ORDER -> orderCreatePanel.reloadDropdowns();
            case SidebarPanel.NAV_ORDER_HISTORY -> orderHistoryPanel.loadOrders();
            default -> {}
        }
    }

    /**
     * Maintains backward compatibility for index-based tab switching (e.g. from DashboardPanel quick actions).
     */
    public void switchToTab(int tabIndex) {
        switch (tabIndex) {
            case TAB_DASHBOARD -> showView(SidebarPanel.NAV_DASHBOARD);
            case TAB_CUSTOMERS -> showView(SidebarPanel.NAV_CUSTOMERS);
            case TAB_PRODUCTS -> showView(SidebarPanel.NAV_PRODUCTS);
            case TAB_CREATE_ORDER -> showView(SidebarPanel.NAV_CREATE_ORDER);
            case TAB_ORDER_HISTORY -> showView(SidebarPanel.NAV_ORDER_HISTORY);
            case TAB_REPORTS -> showReportsDialog();
            default -> showView(SidebarPanel.NAV_DASHBOARD);
        }
    }

    /**
     * Opens the 15 SQL Evaluation Reports screen in a dedicated dialog window.
     * Keeps reports accessible for evaluation without cluttering the primary navigation.
     */
    public void showReportsDialog() {
        JDialog dialog = new JDialog(this, "Academic SQL Evaluation Reports (15 Queries)", false);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setContentPane(reportsPanel);
        dialog.setSize(1000, 700);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Synchronizes and reloads data across all active panels.
     */
    public void refreshAllPanels() {
        if (dashboardPanel != null) dashboardPanel.refreshData();
        if (customerPanel != null) customerPanel.loadCustomers();
        if (productPanel != null) productPanel.loadProducts();
        if (orderCreatePanel != null) orderCreatePanel.reloadDropdowns();
        if (orderHistoryPanel != null) orderHistoryPanel.loadOrders();
    }

    /**
     * Checks database connection status and updates the SidebarPanel monitor.
     */
    public void updateDbStatus() {
        DBConnection.ConnectionDiagnostic diag = DBConnection.diagnoseConnection();
        if (diag.isSuccess()) {
            sidebarPanel.setDatabaseStatus(true, "Connected: MySQL", "MySQL 8.0 connected to ecom_db");
            if (dashboardPanel != null) {
                dashboardPanel.hideConnectionError();
            }
            refreshAllPanels();
        } else {
            String shortStatus = switch (diag.getCategory()) {
                case CONNECTION_REFUSED -> "Offline (No Server)";
                case AUTHENTICATION_FAILED -> "Offline (Auth Failed)";
                case MISSING_CREDENTIALS -> "Offline (No Pass)";
                case DATABASE_NOT_FOUND -> "Offline (No DB)";
                default -> "DB Offline";
            };
            sidebarPanel.setDatabaseStatus(false, shortStatus, diag.getMessage());
            if (dashboardPanel != null) {
                dashboardPanel.showConnectionError(diag.getMessage());
            }
        }
    }

    public SidebarPanel getSidebarPanel() {
        return sidebarPanel;
    }

    public ReportsPanel getReportsPanel() {
        return reportsPanel;
    }

    public static void launch() {
        try {
            // Apply Modern FlatLaf Look and Feel
            FlatLightLaf.setup();
        } catch (Exception e) {
            // Fallback to System Look & Feel
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
