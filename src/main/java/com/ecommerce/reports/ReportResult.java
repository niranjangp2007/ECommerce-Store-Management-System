package com.ecommerce.reports;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the execution result, question metadata, and SQL query of an evaluation report.
 */
public class ReportResult {

    private final int questionNumber;
    private final String title;
    private final String difficulty;
    private final String questionText;
    private final String sqlQuery;
    private final List<String> columnNames;
    private final List<List<Object>> rows;

    public ReportResult(int questionNumber, String title, String difficulty, String questionText,
                        String sqlQuery, List<String> columnNames, List<List<Object>> rows) {
        this.questionNumber = questionNumber;
        this.title = title;
        this.difficulty = difficulty;
        this.questionText = questionText;
        this.sqlQuery = sqlQuery;
        this.columnNames = columnNames != null ? columnNames : new ArrayList<>();
        this.rows = rows != null ? rows : new ArrayList<>();
    }

    public int getQuestionNumber() {
        return questionNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getSqlQuery() {
        return sqlQuery;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public List<List<Object>> getRows() {
        return rows;
    }

    public int getRowCount() {
        return rows.size();
    }

    public int getColumnCount() {
        return columnNames.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= 0 && rowIndex < rows.size()) {
            List<Object> row = rows.get(rowIndex);
            if (columnIndex >= 0 && columnIndex < row.size()) {
                return row.get(columnIndex);
            }
        }
        return null;
    }

    /**
     * Converts the report data into a Swing TableModel for direct UI display.
     */
    public TableModel toTableModel() {
        String[] cols = columnNames.toArray(new String[0]);
        Object[][] data = new Object[rows.size()][columnNames.size()];
        for (int r = 0; r < rows.size(); r++) {
            List<Object> row = rows.get(r);
            for (int c = 0; c < row.size() && c < columnNames.size(); c++) {
                data[r][c] = row.get(c);
            }
        }
        return new DefaultTableModel(data, cols) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }
}
