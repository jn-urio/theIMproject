import javax.swing.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
                writeRow(w, "Date", "Time In", "Time Out", "Regular Hours", "Overtime Hours", "Offset Used", "Status");
                for (DTRDao.DTRRow r : rows) {
                    writeRow(w,
                        r.dateVal != null ? r.dateVal.toString() : "",
                        r.timeIn != null ? r.timeIn.toString() : "",
                        r.timeOut != null ? r.timeOut.toString() : "",
                        r.regularHours != null ? r.regularHours.toPlainString() : "",
                        r.overtimeHours != null ? r.overtimeHours.toPlainString() : "",
                        r.offsetHoursUsed != null ? r.offsetHoursUsed.toPlainString() : "0",
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

    // ---------- Company-style reports (Rancho Palos Verdes format) ----------

    private static final String COMPANY_HEADER = "RANCHO PALOS VERDES GOLF AND COUNTRY CLUB";
    private static final String[] DEDUCTION_TYPE_COLUMNS = {
        "SSS", "Calamity Loan", "Pag-IBIG", "Pag-IBIG Loan", "Emp. loan", "Cash Advance", "Emerg. Loan", "Emp. Savings",
        "Rice Coop", "Mandug", "Bag/umbrella", "RAFFLE", "Relief", "GLOBE", "Alsons", "CHARGES", "W/Held TAX"
    };
    private static final Map<String, String> DEDUCTION_TYPE_ALIASES = new LinkedHashMap<>();
    static {
        DEDUCTION_TYPE_ALIASES.put("PagIBIG", "Pag-IBIG");
        DEDUCTION_TYPE_ALIASES.put("PhilHealth", "PhilHealth");
    }
    private static String normalizeDeductionType(String type) {
        if (type == null) return "Other";
        String t = type.trim();
        return DEDUCTION_TYPE_ALIASES.getOrDefault(t, t);
    }

    /** Export deduction summary by department in company Excel format (pivot by employee, columns = deduction types). departmentFilter null = all; employeeIds null = all. */
    public static File exportDeductionSummaryCompanyFormat(java.awt.Component parent, int periodId, String periodLabel, String departmentFilter, Set<Integer> employeeIds) {
        try {
            List<DeductionDao.DeductionExportRow> rows = DeductionDao.getDeductionDetailForExportByDepartment(periodId);
            if (departmentFilter != null && !departmentFilter.isEmpty() && !"All (CLUBHOUSE)".equals(departmentFilter))
                rows = rows.stream().filter(r -> departmentFilter.equals(r.departmentName)).collect(Collectors.toList());
            if (employeeIds != null && !employeeIds.isEmpty())
                rows = rows.stream().filter(r -> employeeIds.contains(r.employeeId)).collect(Collectors.toList());
            Map<String, Map<String, BigDecimal>> pivot = new LinkedHashMap<>(); // (dept|empId|name) -> (type -> amount)
            for (DeductionDao.DeductionExportRow r : rows) {
                String key = r.departmentName + "|" + r.employeeId + "|" + r.employeeName;
                String type = normalizeDeductionType(r.deductionType);
                pivot.computeIfAbsent(key, k -> new LinkedHashMap<>()).merge(type, r.amount, BigDecimal::add);
            }
            Map<String, BigDecimal> grandByType = new LinkedHashMap<>();
            for (String t : DEDUCTION_TYPE_COLUMNS) grandByType.put(t, BigDecimal.ZERO);

            String defaultName = "Deduction_Summary_" + periodLabel.replaceAll("[^a-zA-Z0-9-]+", "_") + ".csv";
            File file = chooseSaveFile(parent, defaultName);
            if (file == null) return null;
            try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writeRow(w, "", "", COMPANY_HEADER);
                writeRow(w, "", "", "DEDUCTION SUMMARY");
                writeRow(w, "", "", "PAYROLL PERIOD:", periodLabel);
                writeRow(w);
                String[] headerCells = new String[3 + DEDUCTION_TYPE_COLUMNS.length + 1];
                headerCells[0] = ""; headerCells[1] = ""; headerCells[2] = "EMPLOYEE NAME";
                for (int i = 0; i < DEDUCTION_TYPE_COLUMNS.length; i++) headerCells[3 + i] = DEDUCTION_TYPE_COLUMNS[i];
                headerCells[headerCells.length - 1] = "Total";
                writeRow(w, headerCells);
                writeRow(w);
                String lastDept = "";
                Map<String, BigDecimal> deptByType = new LinkedHashMap<>();
                int rowNum = 0;
                for (String key : pivot.keySet()) {
                    String[] parts = key.split("\\|", 3);
                    String dept = parts[0];
                    String empName = parts.length > 2 ? parts[2] : "";
                    Map<String, BigDecimal> amounts = pivot.get(key);
                    if (!dept.equals(lastDept)) {
                        if (!lastDept.isEmpty()) {
                            List<String> subCells = new ArrayList<>();
                            subCells.add(""); subCells.add(""); subCells.add("SUB TOTAL >>");
                            BigDecimal subTotal = BigDecimal.ZERO;
                            for (String col : DEDUCTION_TYPE_COLUMNS) {
                                BigDecimal v = deptByType.getOrDefault(col, BigDecimal.ZERO);
                                subTotal = subTotal.add(v);
                                subCells.add(v.compareTo(BigDecimal.ZERO) == 0 ? "  -   " : "  " + MONEY.format(v) + " ");
                            }
                            subCells.add(MONEY.format(subTotal));
                            writeRow(w, subCells.toArray(new String[0]));
                            writeRow(w);
                        }
                        lastDept = dept;
                        deptByType.clear();
                        for (String t : DEDUCTION_TYPE_COLUMNS) deptByType.put(t, BigDecimal.ZERO);
                        writeRow(w, "", dept);
                        writeRow(w);
                    }
                    rowNum++;
                    List<String> cells = new ArrayList<>();
                    cells.add(String.valueOf(rowNum));
                    cells.add(String.valueOf(rowNum));
                    cells.add(empName);
                    BigDecimal rowTotal = BigDecimal.ZERO;
                    for (String col : DEDUCTION_TYPE_COLUMNS) {
                        BigDecimal amt = amounts.getOrDefault(col, BigDecimal.ZERO);
                        rowTotal = rowTotal.add(amt);
                        deptByType.merge(col, amt, BigDecimal::add);
                        grandByType.merge(col, amt, BigDecimal::add);
                        cells.add(amt.compareTo(BigDecimal.ZERO) == 0 ? "" : "  " + MONEY.format(amt) + " ");
                    }
                    cells.add("  " + MONEY.format(rowTotal) + " ");
                    writeRow(w, cells.toArray(new String[0]));
                }
                if (!lastDept.isEmpty()) {
                    List<String> subCells = new ArrayList<>();
                    subCells.add(""); subCells.add(""); subCells.add("SUB TOTAL >>");
                    BigDecimal subTotal = BigDecimal.ZERO;
                    for (String col : DEDUCTION_TYPE_COLUMNS) {
                        BigDecimal v = deptByType.getOrDefault(col, BigDecimal.ZERO);
                        subTotal = subTotal.add(v);
                        subCells.add(v.compareTo(BigDecimal.ZERO) == 0 ? "  -   " : "  " + MONEY.format(v) + " ");
                    }
                    subCells.add(MONEY.format(subTotal));
                    writeRow(w, subCells.toArray(new String[0]));
                }
                writeRow(w);
                List<String> grandCells = new ArrayList<>();
                grandCells.add(""); grandCells.add(""); grandCells.add("GRAND TOTAL");
                BigDecimal grandTotal = BigDecimal.ZERO;
                for (String col : DEDUCTION_TYPE_COLUMNS) {
                    BigDecimal v = grandByType.getOrDefault(col, BigDecimal.ZERO);
                    grandTotal = grandTotal.add(v);
                    grandCells.add(formatMoney(v));
                }
                grandCells.add(MONEY.format(grandTotal));
                writeRow(w, grandCells.toArray(new String[0]));
            }
            JOptionPane.showMessageDialog(parent, "Report saved to:\n" + file.getAbsolutePath(), "Export", JOptionPane.INFORMATION_MESSAGE);
            return file;
        } catch (SQLException | java.io.IOException ex) {
            JOptionPane.showMessageDialog(parent, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public static File exportDeductionSummaryCompanyFormat(java.awt.Component parent, int periodId, String periodLabel) {
        return exportDeductionSummaryCompanyFormat(parent, periodId, periodLabel, null, null);
    }

    private static String formatMoney(BigDecimal bd) {
        if (bd == null || bd.compareTo(BigDecimal.ZERO) == 0) return "  -   ";
        return "  " + MONEY.format(bd) + " ";
    }

    /** Export compensation summary by department in company format. departmentFilter null = all; employeeIds null = all. */
    public static File exportCompensationSummaryCompanyFormat(java.awt.Component parent, int periodId, String periodLabel, String departmentFilter, Set<Integer> employeeIds) {
        try {
            List<CompensationDao.CompensationExportRow> rows = CompensationDao.findByPeriodWithDepartment(periodId);
            if (departmentFilter != null && !departmentFilter.isEmpty() && !"All (CLUBHOUSE)".equals(departmentFilter))
                rows = rows.stream().filter(r -> departmentFilter.equals(r.departmentName)).collect(Collectors.toList());
            if (employeeIds != null && !employeeIds.isEmpty())
                rows = rows.stream().filter(r -> employeeIds.contains(r.employeeId)).collect(Collectors.toList());
            String defaultName = "Compensation_Summary_" + periodLabel.replaceAll("[^a-zA-Z0-9-]+", "_") + ".csv";
            File file = chooseSaveFile(parent, defaultName);
            if (file == null) return null;
            try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writeRow(w, "", "", COMPANY_HEADER);
                writeRow(w, "", "", "COMPENSATION SUMMARY");
                writeRow(w, "", "", "PAYROLL PERIOD:", " " + periodLabel + " ");
                writeRow(w);
                writeRow(w, "", "", "EMPLOYEE", "", "", "", "", "", "basic", "", "s-leave", "", "ot", "", "Total");
                writeRow(w, "", "", "", "", "Rate", "per hr.", "hrs.", "Basic", "hrs.", "Sick Leave", "nd-hrs.", "Night Diff", "hrs.", "Ot-Reg.", "hrs.", "Legal Hol.", "hrs.", "Special Hol.", "hrs.", "RD Duty", "Adjustment", "");
                String lastDept = "";
                BigDecimal deptTotal = BigDecimal.ZERO;
                BigDecimal grandTotal = BigDecimal.ZERO;
                int rowNum = 0;
                for (CompensationDao.CompensationExportRow r : rows) {
                    if (!r.departmentName.equals(lastDept)) {
                        if (!lastDept.isEmpty()) {
                            writeRow(w, "", "", "SUB TOTAL >>", "", "", "", "", MONEY.format(deptTotal), "", "", "", "", "", "", "", "", "", "", "", "", MONEY.format(deptTotal));
                            writeRow(w);
                        }
                        lastDept = r.departmentName;
                        writeRow(w, "", r.departmentName);
                        writeRow(w);
                        deptTotal = BigDecimal.ZERO;
                    }
                    rowNum++;
                    BigDecimal total = r.totalCompensation != null ? r.totalCompensation : BigDecimal.ZERO;
                    deptTotal = deptTotal.add(total);
                    grandTotal = grandTotal.add(total);
                    writeRow(w, String.valueOf(rowNum), ".", r.fullName,
                        r.basicSalary != null ? MONEY.format(r.basicSalary) : "",
                        r.hourlyRate != null ? MONEY.format(r.hourlyRate) : "",
                        r.basicHours != null ? MONEY.format(r.basicHours) : "",
                        r.basicAmount != null ? MONEY.format(r.basicAmount) : "",
                        "", "0.00",
                        r.overtimeHours != null ? MONEY.format(r.overtimeHours) : "",
                        r.overtimeAmount != null ? MONEY.format(r.overtimeAmount) : "",
                        "", "", "", "", "", "", "", "", MONEY.format(total));
                }
                if (!lastDept.isEmpty())
                    writeRow(w, "", "", "SUB TOTAL >>", "", "", "", "", MONEY.format(deptTotal), "", "", "", "", "", "", "", "", "", "", "", "", MONEY.format(deptTotal));
                writeRow(w);
                writeRow(w, "", "", "GRAND TOTAL", "", "", "", "", MONEY.format(grandTotal), "", "", "", "", "", "", "", "", "", "", "", "", MONEY.format(grandTotal));
            }
            JOptionPane.showMessageDialog(parent, "Report saved to:\n" + file.getAbsolutePath(), "Export", JOptionPane.INFORMATION_MESSAGE);
            return file;
        } catch (SQLException | java.io.IOException ex) {
            JOptionPane.showMessageDialog(parent, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public static File exportCompensationSummaryCompanyFormat(java.awt.Component parent, int periodId, String periodLabel) {
        return exportCompensationSummaryCompanyFormat(parent, periodId, periodLabel, null, null);
    }

    /** Export signature ledger: by department, employee [code], gross, deductions, net. departmentFilter null = all; employeeIds null = all. */
    public static File exportSignatureLedger(java.awt.Component parent, int periodId, String periodLabel, String departmentFilter, Set<Integer> employeeIds) {
        try {
            List<PayrollDao.PayrollRowWithDept> rows = PayrollDao.findByPeriodWithDepartment(periodId);
            if (departmentFilter != null && !departmentFilter.isEmpty() && !"All (CLUBHOUSE)".equals(departmentFilter))
                rows = rows.stream().filter(r -> departmentFilter.equals(r.departmentName)).collect(Collectors.toList());
            if (employeeIds != null && !employeeIds.isEmpty())
                rows = rows.stream().filter(r -> employeeIds.contains(r.employeeId)).collect(Collectors.toList());
            String defaultName = "Signature_Ledger_" + periodLabel.replaceAll("[^a-zA-Z0-9-]+", "_") + ".csv";
            File file = chooseSaveFile(parent, defaultName);
            if (file == null) return null;
            try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writeRow(w, "", "", "SIGNATURE LEDGER");
                writeRow(w, "", "", "PAYROLL PERIOD: " + periodLabel);
                writeRow(w);
                writeRow(w, "", "", "CLUB HOUSE", "", "", "", "", "", "");
                writeRow(w, "", "", "", "", "", "GROSS", "TOTAL", "NET");
                writeRow(w, "", "", "", "", "", "PAY", "DEDUCTIONS", "PAY");
                writeRow(w);
                String lastDept = "";
                BigDecimal deptGross = BigDecimal.ZERO, deptDed = BigDecimal.ZERO, deptNet = BigDecimal.ZERO;
                BigDecimal divGross = BigDecimal.ZERO, divDed = BigDecimal.ZERO, divNet = BigDecimal.ZERO;
                int rowNum = 0;
                for (PayrollDao.PayrollRowWithDept r : rows) {
                    if (!r.departmentName.equals(lastDept)) {
                        if (!lastDept.isEmpty()) {
                            writeRow(w, "", "", "", "DEPARTMENT TOTAL >>", "", MONEY.format(deptGross), MONEY.format(deptDed), MONEY.format(deptNet));
                            writeRow(w);
                        }
                        lastDept = r.departmentName;
                        writeRow(w, "", "", r.departmentName);
                        writeRow(w);
                        deptGross = deptDed = deptNet = BigDecimal.ZERO;
                    }
                    rowNum++;
                    BigDecimal gross = r.grossPay != null ? r.grossPay : BigDecimal.ZERO;
                    BigDecimal ded = r.totalDeductions != null ? r.totalDeductions : BigDecimal.ZERO;
                    BigDecimal net = r.netPay != null ? r.netPay : BigDecimal.ZERO;
                    deptGross = deptGross.add(gross); deptDed = deptDed.add(ded); deptNet = deptNet.add(net);
                    divGross = divGross.add(gross); divDed = divDed.add(ded); divNet = divNet.add(net);
                    String empDisplay = r.employeeCode != null && !r.employeeCode.isEmpty() ? r.fullName + " [" + r.employeeCode + "]" : r.fullName;
                    writeRow(w, String.valueOf(rowNum), ".", empDisplay, "", "", MONEY.format(gross), MONEY.format(ded), MONEY.format(net));
                }
                if (!lastDept.isEmpty())
                    writeRow(w, "", "", "", "DEPARTMENT TOTAL >>", "", MONEY.format(deptGross), MONEY.format(deptDed), MONEY.format(deptNet));
                writeRow(w);
                writeRow(w, "", "", "", "DIVISION TOTAL >>", "", MONEY.format(divGross), MONEY.format(divDed), MONEY.format(divNet));
            }
            JOptionPane.showMessageDialog(parent, "Report saved to:\n" + file.getAbsolutePath(), "Export", JOptionPane.INFORMATION_MESSAGE);
            return file;
        } catch (SQLException | java.io.IOException ex) {
            JOptionPane.showMessageDialog(parent, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public static File exportSignatureLedger(java.awt.Component parent, int periodId, String periodLabel) {
        return exportSignatureLedger(parent, periodId, periodLabel, null, null);
    }

    /** Export payroll funding (bank list). departmentFilter null = all (CLUBHOUSE); or filter by department name (e.g. GM, SUP). */
    public static File exportPayrollFunding(java.awt.Component parent, int periodId, String periodLabel, java.util.Date payDate, String departmentFilter) {
        try {
            List<PayrollDao.PayrollRow> rows;
            if (departmentFilter != null && !departmentFilter.trim().isEmpty()) {
                List<PayrollDao.PayrollRowWithDept> withDept = PayrollDao.findByPeriodWithDepartment(periodId);
                rows = new java.util.ArrayList<>();
                for (PayrollDao.PayrollRowWithDept r : withDept) {
                    if (departmentFilter.equals(r.departmentName))
                        rows.add(new PayrollDao.PayrollRow(r.payrollId, r.employeeId, r.fullName, r.employeeCode, r.payrollPeriodId, r.grossPay, r.totalDeductions, r.netPay, r.status));
                }
            } else {
                rows = PayrollDao.findByPeriod(periodId);
            }
            BigDecimal totalAmount = rows.stream().map(r -> r.netPay != null ? r.netPay : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
            String defaultName = "Payroll_Funding_" + periodLabel.replaceAll("[^a-zA-Z0-9-]+", "_") + ".csv";
            File file = chooseSaveFile(parent, defaultName);
            if (file == null) return null;
            try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                String payDateStr = payDate != null ? new java.text.SimpleDateFormat("MMMMM dd, yyyy").format(payDate) : periodLabel;
                writeRow(w, "H", "Payroll Date", payDateStr, "Payroll Time", "", "Total Amount", MONEY.format(totalAmount), "Total Count", String.valueOf(rows.size()), "FUNDING ACCOUNT", "");
                writeRow(w, "DETAIL CONSTANT", "EMPLOYEE NAME", "EMPLOYEE ACCOUNT", "AMOUNT", "REMARKS", "", "", "", "");
                for (PayrollDao.PayrollRow r : rows) {
                    String acct = (r.employeeCode != null && !r.employeeCode.isEmpty()) ? r.employeeCode : String.valueOf(r.employeeId);
                    writeRow(w, "D", r.fullName, acct, MONEY.format(r.netPay != null ? r.netPay : BigDecimal.ZERO), "PAYROLL", "", "", "", "");
                }
            }
            JOptionPane.showMessageDialog(parent, "Report saved to:\n" + file.getAbsolutePath(), "Export", JOptionPane.INFORMATION_MESSAGE);
            return file;
        } catch (SQLException | java.io.IOException ex) {
            JOptionPane.showMessageDialog(parent, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /** Export payroll funding for all employees (no department filter). */
    public static File exportPayrollFunding(java.awt.Component parent, int periodId, String periodLabel, java.util.Date payDate) {
        return exportPayrollFunding(parent, periodId, periodLabel, payDate, null);
    }

    /**
     * Export 13th-month report for a quarter.
     * Rule: Monthly 13th-month pay = (sum of earnings from both cutoffs in that month) / 12.
     * Total 13th Month = (sum of all basic in the quarter) / 12.
     */
    public static File export13thMonth(java.awt.Component parent, java.sql.Date quarterStart, java.sql.Date quarterEnd) {
        try {
            List<PayrollPeriodDao.PayrollPeriod> periods = PayrollPeriodDao.getPeriodsInDateRange(quarterStart, quarterEnd);
            if (periods.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "No payroll periods found in the selected date range.", "Export", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            List<Integer> periodIds = periods.stream().map(p -> p.periodId).collect(Collectors.toList());
            Map<Integer, java.sql.Date> periodIdToStart = new HashMap<>();
            for (PayrollPeriodDao.PayrollPeriod p : periods) periodIdToStart.put(p.periodId, p.startDate);

            List<CompensationDao.CompensationRow> compRows = CompensationDao.findByPeriodsFor13thMonth(periodIds);
            Map<String, List<CompensationDao.CompensationRow>> byEmployee = compRows.stream().collect(Collectors.groupingBy(r -> r.employeeId + "|" + r.fullName));
            Set<String> months = new TreeSet<>();
            for (PayrollPeriodDao.PayrollPeriod p : periods) {
                if (p.startDate != null) months.add(p.startDate.toString().substring(0, 7));
            }
            List<String> monthList = new ArrayList<>(months);

            String defaultName = "13th_Month_" + quarterStart + "_to_" + quarterEnd + ".csv";
            File file = chooseSaveFile(parent, defaultName);
            if (file == null) return null;
            try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writeRow(w, "13th Month Report", "", "", "", "", "", "", "", "", "", "", "");
                writeRow(w, "SIGNATURE LEDGER", "", "", "", "", "", "", "", "", "", "", "");
                writeRow(w, "", "", "EMPLOYEE", "", "", "");
                List<String> headerRow1 = new ArrayList<>(Arrays.asList("", "", "", ""));
                for (String mon : monthList) {
                    headerRow1.add(mon);
                    headerRow1.add("");
                    headerRow1.add("");
                }
                headerRow1.add("TOTAL");
                headerRow1.add("13th Month");
                writeRow(w, headerRow1.toArray(new String[0]));
                List<String> headerRow2 = new ArrayList<>(Arrays.asList("", "", "", ""));
                for (String mon : monthList) {
                    headerRow2.add("Basic");
                    headerRow2.add("Basic");
                    headerRow2.add("13th Month");
                }
                headerRow2.add("Basic");
                headerRow2.add("13th Month");
                writeRow(w, headerRow2.toArray(new String[0]));
                writeRow(w);

                Map<String, BigDecimal> employeeMonthBasic = new LinkedHashMap<>();
                for (String empKey : new TreeSet<>(byEmployee.keySet())) {
                    List<CompensationDao.CompensationRow> list = byEmployee.get(empKey);
                    for (CompensationDao.CompensationRow r : list) {
                        BigDecimal tot = r.totalCompensation != null ? r.totalCompensation : BigDecimal.ZERO;
                        java.sql.Date start = periodIdToStart.get(r.payrollPeriodId);
                        if (start != null) {
                            String ym = start.toString().substring(0, 7);
                            String key = empKey + "|" + ym;
                            employeeMonthBasic.merge(key, tot, BigDecimal::add);
                        }
                    }
                }

                for (String empKey : new TreeSet<>(byEmployee.keySet())) {
                    String name = empKey.contains("|") ? empKey.substring(empKey.indexOf("|") + 1) : empKey;
                    List<String> cells = new ArrayList<>();
                    cells.add("");
                    cells.add(".");
                    cells.add(name);
                    cells.add("");
                    BigDecimal rowTotalBasic = BigDecimal.ZERO;
                    for (String mon : monthList) {
                        String key = empKey + "|" + mon;
                        BigDecimal basic = employeeMonthBasic.getOrDefault(key, BigDecimal.ZERO);
                        rowTotalBasic = rowTotalBasic.add(basic);
                        BigDecimal thirteenth = BigDecimal.ZERO;
                        if (basic.compareTo(BigDecimal.ZERO) > 0) {
                            thirteenth = basic.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
                        }
                        cells.add(MONEY.format(basic));
                        cells.add(MONEY.format(basic));
                        cells.add(MONEY.format(thirteenth));
                    }
                    BigDecimal total13th = BigDecimal.ZERO;
                    if (rowTotalBasic.compareTo(BigDecimal.ZERO) > 0) {
                        total13th = rowTotalBasic.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
                    }
                    cells.add(MONEY.format(rowTotalBasic));
                    cells.add(MONEY.format(total13th));
                    writeRow(w, cells.toArray(new String[0]));
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

    private static File chooseOpenFile(java.awt.Component parent, String title) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(title != null ? title : "Open CSV");
        if (fc.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) return null;
        return fc.getSelectedFile();
    }

    /** Parse one CSV line into cells (handles quoted commas). */
    private static List<String> parseCsvLine(String line) {
        List<String> cells = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cell.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                cells.add(cell.toString().trim());
                cell.setLength(0);
            } else {
                cell.append(c);
            }
        }
        cells.add(cell.toString().trim());
        return cells;
    }

    /**
     * Import deductions from CSV. Expected columns (header row optional):
     * Period ID, Employee (ID or code), Type, Amount, Description, Status
     * or: Employee, Type, Amount, Description (period from parameter).
     * Matching: first column can be period_id or period name; employee by ID or employee_code.
     */
    public static boolean importDeductionsFromCsv(java.awt.Component parent, int periodId, Integer appliedBy) {
        File file = chooseOpenFile(parent, "Select deductions CSV to import");
        if (file == null || !file.exists()) return false;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String firstLine = r.readLine();
            if (firstLine == null) {
                JOptionPane.showMessageDialog(parent, "File is empty.", "Import", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            List<String> header = parseCsvLine(firstLine);
            int periodCol = -1, empCol = -1, typeCol = -1, amountCol = -1, descCol = -1, statusCol = -1;
            for (int i = 0; i < header.size(); i++) {
                String h = header.get(i).toLowerCase();
                if (h.contains("period")) periodCol = i;
                else if (h.contains("employee") || h.equals("emp")) empCol = i;
                else if (h.contains("type")) typeCol = i;
                else if (h.contains("amount")) amountCol = i;
                else if (h.contains("description") || h.contains("desc")) descCol = i;
                else if (h.contains("status")) statusCol = i;
            }
            if (empCol < 0 || typeCol < 0 || amountCol < 0) {
                JOptionPane.showMessageDialog(parent, "CSV must have Employee, Type, and Amount columns (or similar).", "Import", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            int imported = 0, skipped = 0;
            Map<String, Integer> periodNameToId = new HashMap<>();
            for (PayrollPeriodDao.PayrollPeriod p : PayrollPeriodDao.findAll()) {
                if (p.periodName != null) periodNameToId.put(p.periodName.trim().toLowerCase(), p.periodId);
            }
            String line;
            while ((line = r.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                List<String> cells = parseCsvLine(line);
                if (cells.size() <= Math.max(empCol, Math.max(typeCol, amountCol))) { skipped++; continue; }
                int rowPeriodId = periodId;
                if (periodCol >= 0 && periodCol < cells.size() && !cells.get(periodCol).isEmpty()) {
                    String pVal = cells.get(periodCol).trim();
                    try {
                        rowPeriodId = Integer.parseInt(pVal);
                    } catch (NumberFormatException e) {
                        Integer id = periodNameToId.get(pVal.toLowerCase());
                        if (id != null) rowPeriodId = id;
                    }
                }
                String empVal = cells.get(empCol).trim();
                if (empVal.isEmpty()) { skipped++; continue; }
                int employeeId = -1;
                try {
                    employeeId = Integer.parseInt(empVal);
                    if (EmployeeDao.findById(employeeId) == null) employeeId = -1;
                } catch (NumberFormatException ignored) {
                    EmployeeDao.Employee emp = EmployeeDao.findByCode(empVal);
                    if (emp != null) employeeId = emp.employeeId;
                }
                if (employeeId <= 0) { skipped++; continue; }
                String type = cells.get(typeCol).trim();
                if (type.isEmpty()) type = "Other";
                BigDecimal amount;
                try {
                    amount = new BigDecimal(cells.get(amountCol).replaceAll("[^0-9.\\-]", ""));
                } catch (Exception e) { skipped++; continue; }
                String desc = descCol >= 0 && descCol < cells.size() ? cells.get(descCol) : "Imported from CSV";
                String status = statusCol >= 0 && statusCol < cells.size() ? cells.get(statusCol) : "active";
                if (status.isEmpty()) status = "active";
                DeductionDao.insert(employeeId, rowPeriodId, type, amount, desc, status, appliedBy);
                imported++;
            }
            JOptionPane.showMessageDialog(parent, "Imported " + imported + " deduction(s). Skipped " + skipped + " row(s).", "Import", JOptionPane.INFORMATION_MESSAGE);
            return imported > 0;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "Import failed: " + ex.getMessage(), "Import", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Import signature ledger from CSV (same format as export: row number, ".", employee name [code], "", "", gross, deductions, net).
     * Creates or updates Payroll rows for the given period.
     */
    public static boolean importSignatureLedgerFromCsv(java.awt.Component parent, int periodId) {
        File file = chooseOpenFile(parent, "Select signature ledger CSV to import");
        if (file == null || !file.exists()) return false;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            int imported = 0, updated = 0, skipped = 0;
            List<EmployeeDao.EmployeeRow> allEmps = EmployeeDao.findAll();
            Map<String, Integer> nameToId = new HashMap<>();
            Map<String, Integer> codeToId = new HashMap<>();
            for (EmployeeDao.EmployeeRow e : allEmps) {
                if (e.fullName != null) nameToId.put(e.fullName.trim().toLowerCase(), e.employeeId);
                if (e.employeeCode != null && !e.employeeCode.isEmpty()) codeToId.put(e.employeeCode.trim().toLowerCase(), e.employeeId);
            }
            while ((line = r.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                List<String> cells = parseCsvLine(line);
                if (cells.size() < 8) continue;
                String cell0 = cells.get(0).trim();
                if (cell0.isEmpty() || "SIGNATURE".equalsIgnoreCase(cell0) || "PAYROLL".equalsIgnoreCase(cell0) || "CLUB".equalsIgnoreCase(cell0)
                    || "GROSS".equalsIgnoreCase(cells.get(5).trim()) || "DEPARTMENT".equalsIgnoreCase(cell0))
                    continue;
                if ("DEPARTMENT TOTAL >>".equalsIgnoreCase(cells.get(3).trim()) || "DIVISION TOTAL >>".equalsIgnoreCase(cells.get(3).trim()))
                    continue;
                String empDisplay = cells.size() > 2 ? cells.get(2).trim() : "";
                if (empDisplay.isEmpty()) continue;
                String code = null;
                if (empDisplay.contains("[") && empDisplay.contains("]")) {
                    int start = empDisplay.indexOf('[');
                    int end = empDisplay.indexOf(']');
                    if (start < end) code = empDisplay.substring(start + 1, end).trim();
                }
                Integer employeeId = code != null ? codeToId.get(code.toLowerCase()) : null;
                if (employeeId == null) employeeId = nameToId.get(empDisplay.replaceAll("\\s*\\[.*\\]\\s*", "").trim().toLowerCase());
                if (employeeId == null) { skipped++; continue; }
                BigDecimal gross = parseMoney(cells.get(5));
                BigDecimal ded = parseMoney(cells.get(6));
                BigDecimal net = parseMoney(cells.get(7));
                if (gross == null) gross = BigDecimal.ZERO;
                if (ded == null) ded = BigDecimal.ZERO;
                if (net == null) net = BigDecimal.ZERO;
                PayrollDao.PayrollRow existing = PayrollDao.findByEmployeeAndPeriod(employeeId, periodId);
                if (existing != null) {
                    PayrollDao.updateAmounts(existing.payrollId, gross, ded, net);
                    updated++;
                } else {
                    PayrollDao.insert(employeeId, periodId, gross, ded, net, "draft");
                    imported++;
                }
            }
            JOptionPane.showMessageDialog(parent, "Ledger import: " + imported + " created, " + updated + " updated, " + skipped + " skipped.", "Import", JOptionPane.INFORMATION_MESSAGE);
            return (imported + updated) > 0;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "Import failed: " + ex.getMessage(), "Import", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private static BigDecimal parseMoney(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return new BigDecimal(s.replaceAll("[^0-9.\\-]", "").trim());
        } catch (Exception e) { return null; }
    }
}
