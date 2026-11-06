package com.ecommerce.dao;

import com.ecommerce.reports.ReportResult;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object interface for all 15 Academic Evaluation Reports.
 */
public interface ReportDAO {

    ReportResult executeReport(int questionNumber) throws SQLException;

    List<ReportMeta> getAllReportsMeta();

    ReportResult getQ1ElectronicsProducts() throws SQLException;
    ReportResult getQ2ChennaiCustomers() throws SQLException;
    ReportResult getQ3ProductsAbove500() throws SQLException;
    ReportResult getQ4OutOfStockProducts() throws SQLException;
    ReportResult getQ5OrdersByStatus() throws SQLException;
    ReportResult getQ6DeliveredOrders() throws SQLException;
    ReportResult getQ7OrdersBetweenDates() throws SQLException;
    ReportResult getQ8HomeAndStationery() throws SQLException;
    ReportResult getQ9OrderItemsLineTotal() throws SQLException;
    ReportResult getQ10OrderTotalValue() throws SQLException;
    ReportResult getQ11DeliveredCategoryRevenue() throws SQLException;
    ReportResult getQ12Top3DeliveredCustomers() throws SQLException;
    ReportResult getQ13NeverOrderedProducts() throws SQLException;
    ReportResult getQ14RepeatCustomers() throws SQLException;
    ReportResult getQ15UnitsSoldPerProduct() throws SQLException;

    class ReportMeta {
        private final int number;
        private final String title;
        private final String difficulty;
        private final String question;
        private final String sql;

        public ReportMeta(int number, String title, String difficulty, String question, String sql) {
            this.number = number;
            this.title = title;
            this.difficulty = difficulty;
            this.question = question;
            this.sql = sql;
        }

        public int getNumber() { return number; }
        public String getTitle() { return title; }
        public String getDifficulty() { return difficulty; }
        public String getQuestion() { return question; }
        public String getSql() { return sql; }

        @Override
        public String toString() {
            return String.format("Q%d: %s (%s)", number, title, difficulty);
        }
    }
}
