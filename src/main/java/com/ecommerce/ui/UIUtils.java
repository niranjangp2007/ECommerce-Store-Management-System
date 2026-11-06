package com.ecommerce.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Supplier;

/**
 * Styling and utility helpers for Java Swing components.
 * Provides color palettes, typography, card containers, vector icons, and button factories.
 */
public final class UIUtils {

    // Content Area & Status Palette
    public static final Color PRIMARY_COLOR = new Color(33, 115, 70); // Modern Forest Emerald
    public static final Color ACCENT_COLOR = new Color(41, 128, 185);  // Ocean Blue
    public static final Color DANGER_COLOR = new Color(192, 57, 43);   // Crimson Red
    public static final Color WARNING_COLOR = new Color(211, 84, 0);   // Dark Orange
    public static final Color SUCCESS_COLOR = new Color(39, 174, 96);  // Green
    public static final Color BG_LIGHT = new Color(248, 249, 250);
    public static final Color CARD_BG = Color.WHITE;

    // Dark Navy Sidebar Palette
    public static final Color SIDEBAR_BG = new Color(17, 24, 39);           // Deep Dark Navy Slate
    public static final Color SIDEBAR_HOVER = new Color(31, 41, 55);        // Hover state dark slate
    public static final Color SIDEBAR_ACTIVE = new Color(30, 58, 102);       // Active item navy/blue tint
    public static final Color SIDEBAR_BORDER = new Color(38, 48, 65);       // Subtle borders/separators
    public static final Color SIDEBAR_CARD_BG = new Color(24, 33, 47);      // Darker inner status card
    public static final Color SIDEBAR_TEXT = new Color(156, 163, 175);       // Inactive item text (slate grey)
    public static final Color SIDEBAR_TEXT_HOVER = new Color(243, 244, 246); // Hovered text (crisp light)
    public static final Color SIDEBAR_TEXT_ACTIVE = Color.WHITE;             // Active item text (pure white)

    // Vibrant Modern Accents & Status Indicators
    public static final Color NAV_ACTIVE_ACCENT = new Color(59, 130, 246);   // Active blue indicator bar
    public static final Color STATUS_ONLINE = new Color(34, 197, 94);        // Green online status dot
    public static final Color STATUS_OFFLINE = new Color(239, 68, 68);       // Crimson offline status dot

    // Typography
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font BRAND_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font BRAND_SUB_FONT = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font NAV_ITEM_FONT = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font BADGE_FONT = new Font("Segoe UI", Font.BOLD, 10);
    public static final Font STATUS_FONT = new Font("Segoe UI", Font.BOLD, 11);

    private UIUtils() {
    }

    public static void formatTable(JTable table) {
        table.setFont(REGULAR_FONT);
        table.setRowHeight(34);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setGridColor(new Color(243, 244, 246));
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(new Color(30, 58, 138));

        JTableHeader header = table.getTableHeader();
        header.setFont(BOLD_FONT);
        header.setBackground(new Color(243, 244, 246));
        header.setForeground(new Color(31, 41, 55));
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setReorderingAllowed(false);

        // Standard text renderer with zebra striping and 12px cell padding
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 250, 251));
                    c.setForeground(new Color(31, 41, 55));
                } else {
                    c.setBackground(new Color(239, 246, 255));
                    c.setForeground(new Color(30, 58, 138));
                }
                return c;
            }
        });

        // Numeric renderer with center alignment, zebra striping and selection
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 250, 251));
                    c.setForeground(new Color(31, 41, 55));
                } else {
                    c.setBackground(new Color(239, 246, 255));
                    c.setForeground(new Color(30, 58, 138));
                }
                return c;
            }
        };
        table.setDefaultRenderer(Integer.class, centerRenderer);
        table.setDefaultRenderer(int.class, centerRenderer);
        table.setDefaultRenderer(Double.class, centerRenderer);
        table.setDefaultRenderer(double.class, centerRenderer);
    }

    public static JPanel createCard(String title, JComponent content) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                new EmptyBorder(12, 12, 12, 12)
        ));

        if (title != null && !title.isEmpty()) {
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(SUBTITLE_FONT);
            titleLabel.setForeground(new Color(40, 40, 40));
            card.add(titleLabel, BorderLayout.NORTH);
        }

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(BOLD_FONT);
        btn.setBackground(PRIMARY_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(BOLD_FONT);
        btn.setBackground(new Color(240, 242, 245));
        btn.setForeground(new Color(50, 50, 50));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(BOLD_FONT);
        btn.setBackground(DANGER_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createSidebarSecondaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(20, 28, 40));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(45, 58, 78));
                } else {
                    g2.setColor(new Color(31, 41, 55));
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 6, 6));
                g2.setColor(SIDEBAR_BORDER);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 6, 6));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(STATUS_FONT);
        btn.setForeground(new Color(209, 213, 219));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, 28));
        return btn;
    }

    public static JSeparator createDarkSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(SIDEBAR_BORDER);
        sep.setBackground(SIDEBAR_BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    public static JLabel createPillBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setFont(BADGE_FONT);
        badge.setForeground(fg);
        badge.setBorder(new EmptyBorder(3, 10, 3, 10));
        return badge;
    }

    /**
     * Creates a lightweight, clean 2D vector icon for navigation items and branding.
     * Eliminates emojis and external image file dependencies with resolution-independent vector rendering.
     */
    public static Icon createNavIcon(String iconType, int size, Supplier<Color> colorSupplier) {
        return new VectorNavIcon(iconType, size, colorSupplier);
    }

    public static Icon createNavIcon(String iconType, int size, Color staticColor) {
        return new VectorNavIcon(iconType, size, () -> staticColor);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirmAction(Component parent, String message, String title) {
        int opt = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return opt == JOptionPane.YES_OPTION;
    }

    /**
     * Vector icon implementation painted via Java 2D Graphics.
     */
    private static class VectorNavIcon implements Icon {
        private final String type;
        private final int size;
        private final Supplier<Color> colorSupplier;

        public VectorNavIcon(String type, int size, Supplier<Color> colorSupplier) {
            this.type = type != null ? type.toUpperCase() : "";
            this.size = size;
            this.colorSupplier = colorSupplier;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            Color color = colorSupplier != null ? colorSupplier.get() : Color.WHITE;
            g2.setColor(color);
            g2.translate(x, y);

            // Scale standard 16x16 geometry to target icon size
            double scale = size / 16.0;
            g2.scale(scale, scale);

            switch (type) {
                case "DASHBOARD":
                    // 2x2 grid of modern dashboard metrics
                    g2.fillRoundRect(1, 1, 6, 6, 2, 2);
                    g2.fillRoundRect(9, 1, 6, 6, 2, 2);
                    g2.fillRoundRect(1, 9, 6, 6, 2, 2);
                    g2.fillRoundRect(9, 9, 6, 6, 2, 2);
                    break;

                case "CUSTOMERS":
                    // User silhouette: head circle + torso rounded arc
                    g2.fillOval(5, 1, 6, 6);
                    g2.fillRoundRect(2, 9, 12, 6, 4, 4);
                    break;

                case "PRODUCTS":
                    // Inventory package / cargo carton
                    g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawRoundRect(2, 3, 12, 10, 2, 2);
                    g2.drawLine(2, 7, 14, 7);
                    g2.drawLine(8, 7, 8, 13);
                    break;

                case "CREATE_ORDER":
                    // Shopping cart
                    g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(1, 2, 3, 2);
                    g2.drawLine(3, 2, 5, 10);
                    g2.drawLine(5, 10, 13, 10);
                    g2.drawLine(13, 10, 15, 4);
                    g2.drawLine(3, 4, 15, 4);
                    g2.fillOval(5, 12, 2, 2);
                    g2.fillOval(11, 12, 2, 2);
                    break;

                case "ORDER_HISTORY":
                    // Clipboard / order receipt ledger
                    g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawRoundRect(2, 2, 12, 12, 2, 2);
                    g2.drawLine(5, 6, 11, 6);
                    g2.drawLine(5, 9, 11, 9);
                    g2.drawLine(5, 12, 9, 12);
                    break;

                case "STORE_LOGO":
                    // Shopping bag emblem
                    g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawRoundRect(2, 5, 12, 10, 2, 2);
                    g2.drawArc(5, 1, 6, 6, 0, 180);
                    break;

                case "STATUS_DOT":
                    // Status dot
                    g2.fillOval(3, 3, 10, 10);
                    break;

                case "ALERT":
                    // Modern triangle warning indicator
                    g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    Polygon poly = new Polygon(new int[]{8, 1, 15}, new int[]{1, 14, 14}, 3);
                    g2.drawPolygon(poly);
                    g2.drawLine(8, 5, 8, 9);
                    g2.fillOval(7, 11, 2, 2);
                    break;

                default:
                    // Fallback diamond mark
                    g2.setStroke(new BasicStroke(1.4f));
                    g2.drawRoundRect(3, 3, 10, 10, 2, 2);
                    break;
            }

            g2.dispose();
        }
    }
}
