import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Exports summary reports to CSV (Excel-compatible). Uses comma separator and quoted fields.
 */
public class ReportExporter {
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.00");

    private static String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }

    private static void writeRow(BufferedWriter w, String... cells) throws java.io.IOException {
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) w.write(",");
            w.write(escapeCsv(cells[i]));
        }
        w.newLine();
    }

    /** Export contributions summary (SSS, PhilHealth, PagIBIG) to CSV. periodId null = all periods. */
    public static File exportContributionsToExcel(java.awt.Component parent, Integer periodId) {
        try {
            Map<String, Map<String, BigDecimal>> summary = DeductionDao.getContributionSummaryForExport(periodId);
            List<DeductionDao.DeductionRow> detail = DeductionDao.findContributionsByPeriod(periodId);
            String defaultName = "contributions_summary_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".csv";
            File file = chooseSaveFile(parent, defaultName);
            if (file == null) return null;
            try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writeRow(w, "Contributions Summary Report (SSS, PhilHealth, PagIBIG)");
                writeRow(w, "Generated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                writeRow(w);
                writeRow(w, "Summary by Period");
                writeRow(w, "Period", "SSS", "PhilHealth", "PagIBIG", "Total");
                BigDecimal grandSss = BigDecimal.ZERO, grandPh = BigDecimal.ZERO, grandPag = BigDecimal.ZERO;
                for (Map.Entry<String, Map<String, BigDecimal>> e : summary.entrySet()) {
                    String period = e.getKey();
                    Map<String, BigDecimal> byType = e.getValue();
                    BigDecimal sss = byType.getOrDefault("SSS", BigDecimal.ZERO);
                    BigDecimal ph = byType.getOrDefault("PhilHealth", BigDecimal.ZERO);
                    BigDecimal pag = byType.getOrDefault("PagIBIG", BigDecimal.ZERO);
                    BigDecimal rowTotal = sss.add(ph).add(pag);
                    grandSss = grandSss.add(sss);
                    grandPh = grandPh.add(ph);
                    grandPag = grandPag.add(pag);
                    writeRow(w, period, MONEY.format(sss), MONEY.format(ph), MONEY.format(pag), MONEY.format(rowTotal));
                }
                writeRow(w, "TOTAL", MONEY.format(grandSss), MONEY.format(grandPh), MONEY.format(grandPag), MONEY.format(grandSss.add(grandPh).add(grandPag)));
                writeRow(w);
                writeRow(w, "Detail by Employee");
                writeRow(w, "Period ID", "Employee", "Type", "Amount", "Status");
                for (DeductionDao.DeductionRow r : detail) {
                    writeRow(w, String.valueOf(r.payrollPeriodId), r.fullName, r.deductionType,
                        r.amount != null ? MONEY.format(r.amount) : "0.00", r.status);
                }
            }
            JOptionPane.showMessageDialog(parent, "Report saved to:\n" + file.getAbsolutePath(), "Export", JOptionPane.INFORMATION_MESSAGE);
            return file;
        } catch (SQLException | java.io.IOException ex) {
            JOptionPane.showMessageDialog(parent, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /** Export deductions summary (Loan, Cash Advance, Other) to CSV. periodId null = all periods. */
    public static File exportDeductionsToExcel(java.awt.Component parent, Integer periodId) {
        try {
            Map<String, Map<String, BigDecimal>> summary = DeductionDao.getDeductionSummaryForExport(periodId);
            List<DeductionDao.DeductionRow> detail = DeductionDao.findDeductionsByPeriod(periodId);
            String defaultName = "deductions_summary_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".csv";
            File file = chooseSaveFile(parent, defaultName);
            if (file == null) return null;
            try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writeRow(w, "Deductions Summary Report (Loan, Cash Advance, Other)");
                writeRow(w, "Generated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                writeRow(w);
                writeRow(w, "Summary by Period");
                writeRow(w, "Period", "Loan", "Cash Advance", "Other", "Total");
                BigDecimal grandLoan = BigDecimal.ZERO, grandCash = BigDecimal.ZERO, grandOther = BigDecimal.ZERO;
                for (Map.Entry<String, Map<String, BigDecimal>> e : summary.entrySet()) {
                    String period = e.getKey();
                    Map<String, BigDecimal> byType = e.getValue();
                    BigDecimal loan = byType.getOrDefault("Loan", BigDecimal.ZERO);
                    BigDecimal cash = byType.getOrDefault("Cash Advance", BigDecimal.ZERO);
                    BigDecimal other = byType.getOrDefault("Other", BigDecimal.ZERO);
                    BigDecimal rowTotal = loan.add(cash).add(other);
                    grandLoan = grandLoan.add(loan);
                    grandCash = grandCash.add(cash);
                    grandOther = grandOther.add(other);
                    writeRow(w, period, MONEY.format(loan), MONEY.format(cash), MONEY.format(other), MONEY.format(rowTotal));
                }
                writeRow(w, "TOTAL", MONEY.format(grandLoan), MONEY.format(grandCash), MONEY.format(grandOther), MONEY.format(grandLoan.add(grandCash).add(grandOther)));
                writeRow(w);
                writeRow(w, "Detail by Employee");
                writeRow(w, "Period ID", "Employee", "Type", "Amount", "Description", "Status");
                for (DeductionDao.DeductionRow r : detail) {
                    writeRow(w, String.valueOf(r.payrollPeriodId), r.fullName, r.deductionType,
                        r.amount != null ? MONEY.format(r.amount) : "0.00", r.description != null ? r.description : "", r.status);
                }
            }
            JOptionPane.showMessageDialog(parent, "Report saved to:\n" + file.getAbsolutePath(), "Export", JOptionPane.INFORMATION_MESSAGE);
            return file;
        } catch (SQLException | java.io.IOException ex) {
            JOptionPane.showMessageDialog(parent, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /** Export deductions by type for a specific employee (or all if employeeId null). */
    public static File exportEmployeeDeductionsToExcel(java.awt.Component parent, Integer periodId, Integer employeeId, String employeeLabel) {
        try {
            var summary = DeductionDao.getSummaryByType(periodId, employeeId);
            var detail = DeductionDao.findByPeriodAndEmployee(periodId, employeeId);
            String defaultName = "deductions_" + (employeeLabel != null && !employeeLabel.isBlank() ? employeeLabel.replaceAll("[^a-zA-Z0-9-_]+", "_") : "employee") +
                "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".csv";
            File file = chooseSaveFile(parent, defaultName);
            if (file == null) return null;
            try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writeRow(w, "Deductions Report");
                writeRow(w, "Employee", employeeLabel != null ? employeeLabel : (employeeId != null ? String.valueOf(employeeId) : "ALL"));
                writeRow(w, "Generated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                writeRow(w);
                writeRow(w, "Summary by Type");
                writeRow(w, "Type", "Total");
                BigDecimal grand = BigDecimal.ZERO;
                for (var e : summary.entrySet()) {
                    BigDecimal t = e.getValue() != null ? e.getValue() : BigDecimal.ZERO;
                    grand = grand.add(t);
                    writeRow(w, e.getKey(), MONEY.format(t));
                }
                writeRow(w, "TOTAL", MONEY.format(grand));
                writeRow(w);
                writeRow(w, "Detail");
                writeRow(w, "Period ID", "Employee", "Type", "Amount", "Description", "Status");
                for (DeductionDao.DeductionRow r : detail) {
                    writeRow(w, String.valueOf(r.payrollPeriodId), r.fullName, r.deductionType,
                        r.amount != null ? MONEY.format(r.amount) : "0.00", r.description != null ? r.description : "", r.status);
                }
            }
            JOptionPane.showMessageDialog(parent, "Report saved to:\n" + file.getAbsolutePath(), "Export", JOptionPane.INFORMATION_MESSAGE);
            return file;
        } catch (SQLException | java.io.IOException ex) {
            JOptionPane.showMessageDialog(parent, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /** Export compensation rows for a specific employee (or all if employeeId null). */
    public static File exportEmployeeCompensationToExcel(java.awt.Component parent, Integer periodId, Integer employeeId, String employeeLabel) {
        try {
            List<CompensationDao.CompensationRow> rows = CompensationDao.findByPeriodAndEmployee(periodId, employeeId);
            String defaultName = "compensation_" + (employeeLabel != null && !employeeLabel.isBlank() ? employeeLabel.replaceAll("[^a-zA-Z0-9-_]+", "_") : "employee") +
                "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".csv";
            File file = chooseSaveFile(parent, defaultName);
            if (file == null) return null;
            try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writeRow(w, "Compensation Report");
                writeRow(w, "Employee", employeeLabel != null ? employeeLabel : (employeeId != null ? String.valueOf(employeeId) : "ALL"));
                writeRow(w, "Generated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                writeRow(w);
                writeRow(w, "Rows");
                writeRow(w, "Comp ID", "Period ID", "Employee", "Basic", "OT", "Total", "Status");
                BigDecimal grand = BigDecimal.ZERO;
                for (CompensationDao.CompensationRow r : rows) {
                    BigDecimal total = r.totalCompensation != null ? r.totalCompensation : BigDecimal.ZERO;
                    grand = grand.add(total);
                    writeRow(w, String.valueOf(r.compensationId), String.valueOf(r.payrollPeriodId), r.fullName,
                        r.basicAmount != null ? MONEY.format(r.basicAmount) : "0.00",
                        r.overtimeAmount != null ? MONEY.format(r.overtimeAmount) : "0.00",
                        MONEY.format(total),
                        r.hrStatus != null ? r.hrStatus : "");
                }
                writeRow(w);
                writeRow(w, "TOTAL", "", "", "", "", MONEY.format(grand), "");
            }
            JOptionPane.showMessageDialog(parent, "Report saved to:\n" + file.getAbsolutePath(), "Export", JOptionPane.INFORMATION_MESSAGE);
            return file;
        } catch (SQLException | java.io.IOException ex) {
            JOptionPane.showMessageDialog(parent, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /** Export one employee's compiled DTR to CSV. periodId null = all periods. */
    public static File exportDTRToCsv(java.awt.Component parent, int employeeId, String employeeName, Integer periodId) {
        try {
            List<DTRDao.DTRRow> rows = DTRDao.findByPeriodAndEmployee(periodId, employeeId);
            String safeName = (employeeName != null && !employeeName.isEmpty()) ? employeeName.replaceAll("[^a-zA-Z0-9_-]", "_") : "employee_" + employeeId;
            String defaultName = "DTR_" + safeName + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".csv";
            File file = chooseSaveFile(parent, defaultName);
            if (file == null) return null;
            try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writeRow(w, "Daily Time Record (DTR)");
                writeRow(w, "Employee", employeeName != null ? employeeName : String.valueOf(employeeId));
                writeRow(w, "Generated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                writeRow(w);
                writeRow(w, "Date", "Time In", "Time Out", "Regular Hours", "Overtime Hours", "Status");
                for (DTRDao.DTRRow r : rows) {
                    writeRow(w,
                        r.dateVal != null ? r.dateVal.toString() : "",
                        r.timeIn != null ? r.timeIn.toString() : "",
                        r.timeOut != null ? r.timeOut.toString() : "",
                        r.regularHours != null ? r.regularHours.toPlainString() : "",
                        r.overtimeHours != null ? r.overtimeHours.toPlainString() : "",
                        r.status != null ? r.status : "");
                }
            }
            JOptionPane.showMessageDialog(parent, "Report saved to:\n" + file.getAbsolutePath(), "Export", JOptionPane.INFORMATION_MESSAGE);
            return file;
        } catch (SQLException | java.io.IOException ex) {
            JOptionPane.showMessageDialog(parent, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private static File chooseSaveFile(java.awt.Component parent, String defaultName) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(defaultName));
        fc.setDialogTitle("Save report as Excel (CSV)");
        if (fc.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return null;
        File f = fc.getSelectedFile();
        if (f != null && !f.getName().toLowerCase().endsWith(".csv")) f = new File(f.getAbsolutePath() + ".csv");
        return f;
    }
}
