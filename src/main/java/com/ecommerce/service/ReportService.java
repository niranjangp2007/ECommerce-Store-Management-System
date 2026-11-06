package com.ecommerce.service;

import com.ecommerce.dao.ReportDAO;
import com.ecommerce.dao.ReportDAOImpl;
import com.ecommerce.reports.ReportResult;

import java.sql.SQLException;
import java.util.List;

/**
 * Service managing all 15 SQL Academic Evaluation Reports.
 */
public class ReportService {

    private final ReportDAO reportDAO;

    public ReportService() {
        this(new ReportDAOImpl());
    }

    public ReportService(ReportDAO reportDAO) {
        this.reportDAO = reportDAO;
    }

    public List<ReportDAO.ReportMeta> getAllReportsMeta() {
        return reportDAO.getAllReportsMeta();
    }

    public ReportResult executeReport(int questionNumber) throws SQLException {
        return reportDAO.executeReport(questionNumber);
    }
}
