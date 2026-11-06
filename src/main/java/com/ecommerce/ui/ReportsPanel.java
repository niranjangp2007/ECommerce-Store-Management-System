package com.ecommerce.ui;

import com.ecommerce.dao.ReportDAO;
import com.ecommerce.reports.ReportResult;
import com.ecommerce.service.ReportService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Interactive SQL Evaluation Reports screen executing all 15 academic benchmark queries (Q1 to Q15).
 */
public class ReportsPanel extends JPanel {

    private final ReportService reportService;

    private JComboBox<ReportDAO.ReportMeta> cmbReports;
    private JLabel lblDifficulty;
    private JLabel lblQuestionText;
    private JTextArea txtSqlQuery;
    private JTable resultsTable;
    private JLabel lblRowCount;

    public ReportsPanel(ReportService reportService) {
        this.reportService = reportService;

        setLayout(new BorderLayout(14, 14));
        setBackground(UIUtils.BG_LIGHT);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        initComponents();
        loadReportMetas();
        if (cmbReports.getItemCount() > 0) {
            cmbReports.setSelectedIndex(0);
            runCurrentReport();
        }
    }

    private void initComponents() {
        // Top Section: Selector + Meta Info + Query Code
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        // Selector Bar
        JPanel selectBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        selectBar.setOpaque(false);

        JLabel lblSelect = new JLabel("Select Evaluation Question:");
        lblSelect.setFont(UIUtils.BOLD_FONT);

        cmbReports = new JComboBox<>();
        cmbReports.setPreferredSize(new Dimension(460, 30));
        cmbReports.setFont(UIUtils.BOLD_FONT);
        cmbReports.addActionListener(e -> updateQuestionDisplay());

        JButton btnRun = UIUtils.createPrimaryButton("▶ Execute Report (Run SQL)");
        btnRun.addActionListener(e -> runCurrentReport());

        selectBar.add(lblSelect);
        selectBar.add(cmbReports);
        selectBar.add(btnRun);

        topContainer.add(selectBar);
        topContainer.add(Box.createVerticalStrut(8));

        // Details Card
        JPanel detailsPanel = new JPanel(new BorderLayout(8, 8));
        detailsPanel.setBackground(Color.WHITE);

        // Header: Difficulty badge + question text
        JPanel qHeader = new JPanel(new BorderLayout(8, 4));
        qHeader.setOpaque(false);

        lblDifficulty = new JLabel("Difficulty: -");
        lblDifficulty.setFont(UIUtils.BOLD_FONT);
        lblDifficulty.setForeground(UIUtils.ACCENT_COLOR);

        lblQuestionText = new JLabel("Question prompt will appear here.");
        lblQuestionText.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        qHeader.add(lblDifficulty, BorderLayout.NORTH);
        qHeader.add(lblQuestionText, BorderLayout.CENTER);
        detailsPanel.add(qHeader, BorderLayout.NORTH);

        // SQL Query display
        txtSqlQuery = new JTextArea(3, 60);
        txtSqlQuery.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtSqlQuery.setBackground(new Color(245, 247, 250));
        txtSqlQuery.setForeground(new Color(30, 40, 50));
        txtSqlQuery.setEditable(false);
        txtSqlQuery.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 215, 222), 1),
                new EmptyBorder(6, 8, 6, 8)
        ));

        detailsPanel.add(txtSqlQuery, BorderLayout.CENTER);
        topContainer.add(UIUtils.createCard("Question Details & SQL Query", detailsPanel));

        add(topContainer, BorderLayout.NORTH);

        // Center Section: Results Table
        resultsTable = new JTable();
        UIUtils.formatTable(resultsTable);
        JScrollPane scrollPane = new JScrollPane(resultsTable);

        // Bottom status row
        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        statusRow.setOpaque(false);
        lblRowCount = new JLabel("Rows: 0");
        lblRowCount.setFont(UIUtils.BOLD_FONT);
        statusRow.add(lblRowCount);

        JPanel tableCard = new JPanel(new BorderLayout(4, 4));
        tableCard.setOpaque(false);
        tableCard.add(scrollPane, BorderLayout.CENTER);
        tableCard.add(statusRow, BorderLayout.SOUTH);

        add(UIUtils.createCard("Query Output (Live MySQL Result Matching Academic Answer Key)", tableCard), BorderLayout.CENTER);
    }

    private void loadReportMetas() {
        cmbReports.removeAllItems();
        List<ReportDAO.ReportMeta> metas = reportService.getAllReportsMeta();
        for (ReportDAO.ReportMeta meta : metas) {
            cmbReports.addItem(meta);
        }
    }

    private void updateQuestionDisplay() {
        ReportDAO.ReportMeta meta = (ReportDAO.ReportMeta) cmbReports.getSelectedItem();
        if (meta != null) {
            lblDifficulty.setText("Difficulty: " + meta.getDifficulty());
            lblQuestionText.setText("<html><b>Problem:</b> " + meta.getQuestion() + "</html>");
            txtSqlQuery.setText(meta.getSql());
        }
    }

    private void runCurrentReport() {
        ReportDAO.ReportMeta meta = (ReportDAO.ReportMeta) cmbReports.getSelectedItem();
        if (meta == null) {
            return;
        }

        try {
            ReportResult result = reportService.executeReport(meta.getNumber());
            resultsTable.setModel(result.toTableModel());
            UIUtils.formatTable(resultsTable);
            lblRowCount.setText("Total Rows: " + result.getRowCount() + " | Columns: " + result.getColumnCount());

        } catch (SQLException e) {
            UIUtils.showError(this, "SQL Execution Failed:\n" + e.getMessage());
            lblRowCount.setText("Query Execution Error");
        }
    }
}
