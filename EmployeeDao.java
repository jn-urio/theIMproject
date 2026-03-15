import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDao {
    public static List<EmployeeRow> findAllWithDepartment(String departmentFilter, String search) throws SQLException {
        List<EmployeeRow> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT e.employee_id, e.employee_code, e.full_name, e.basic_salary, e.is_active, d.department_name " +
            "FROM Employee e LEFT JOIN EmployeeRole er ON er.employee_id = e.employee_id AND er.is_active = 1 " +
            "LEFT JOIN Department d ON d.department_id = er.department_id WHERE 1=1");
        if (departmentFilter != null && !departmentFilter.isEmpty() && !"All Departments".equals(departmentFilter))
            sql.append(" AND d.department_name = ?");
        if (search != null && !search.trim().isEmpty())
            sql.append(" AND (e.full_name LIKE ? OR e.employee_code LIKE ?)");
        sql.append(" ORDER BY e.employee_code");
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int i = 1;
            if (departmentFilter != null && !departmentFilter.isEmpty() && !"All Departments".equals(departmentFilter))
                ps.setString(i++, departmentFilter);
            if (search != null && !search.trim().isEmpty()) {
                String like = "%" + search.trim() + "%";
                ps.setString(i++, like);
                ps.setString(i++, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new EmployeeRow(rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getBigDecimal(4), rs.getBoolean(5), rs.getString(6)));
            }
        }
        return list;
    }

    public static List<EmployeeDao.EmployeeRow> findAll() throws SQLException {
        return findAllWithDepartment(null, null);
    }

    public static Employee findById(int id) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT employee_id, employee_code, full_name, basic_salary, daily_rate, hourly_rate, bank_account, is_active FROM Employee WHERE employee_id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return new Employee(rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getBigDecimal(4), rs.getBigDecimal(5), rs.getBigDecimal(6), rs.getString(7), rs.getBoolean(8));
            }
        }
        return null;
    }

    /** Find employee by employee_code (exact match). Returns null if not found. */
    public static Employee findByCode(String code) throws SQLException {
        if (code == null || code.trim().isEmpty()) return null;
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT employee_id, employee_code, full_name, basic_salary, daily_rate, hourly_rate, bank_account, is_active FROM Employee WHERE employee_code=?")) {
            ps.setString(1, code.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return new Employee(rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getBigDecimal(4), rs.getBigDecimal(5), rs.getBigDecimal(6), rs.getString(7), rs.getBoolean(8));
            }
        }
        return null;
    }

    /** Insert employee; returns the new employee_id. */
    public static int insert(String code, String fullName, java.math.BigDecimal basicSalary, java.math.BigDecimal dailyRate, java.math.BigDecimal hourlyRate, String bankAccount, boolean active) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO Employee (employee_code, full_name, basic_salary, daily_rate, hourly_rate, bank_account, is_active) VALUES (?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code);
            ps.setString(2, fullName);
            ps.setBigDecimal(3, basicSalary);
            ps.setBigDecimal(4, dailyRate);
            ps.setBigDecimal(5, hourlyRate);
            ps.setString(6, bankAccount != null && !bankAccount.trim().isEmpty() ? bankAccount.trim() : null);
            ps.setBoolean(7, active);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Insert employee failed, no ID returned");
    }

    public static void update(int id, String code, String fullName, java.math.BigDecimal basicSalary, java.math.BigDecimal dailyRate, java.math.BigDecimal hourlyRate, String bankAccount, boolean active) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE Employee SET employee_code=?, full_name=?, basic_salary=?, daily_rate=?, hourly_rate=?, bank_account=?, is_active=? WHERE employee_id=?")) {
            ps.setString(1, code);
            ps.setString(2, fullName);
            ps.setBigDecimal(3, basicSalary);
            ps.setBigDecimal(4, dailyRate);
            ps.setBigDecimal(5, hourlyRate);
            ps.setString(6, bankAccount != null && !bankAccount.trim().isEmpty() ? bankAccount.trim() : null);
            ps.setBoolean(7, active);
            ps.setInt(8, id);
            ps.executeUpdate();
        }
    }

    public static class Employee {
        public final int employeeId;
        public final String employeeCode, fullName, bankAccount;
        public final java.math.BigDecimal basicSalary, dailyRate, hourlyRate;
        public final boolean isActive;
        public Employee(int id, String code, String name, java.math.BigDecimal basic, java.math.BigDecimal daily, java.math.BigDecimal hourly, String bank, boolean active) {
            employeeId = id; employeeCode = code; fullName = name; bankAccount = bank;
            basicSalary = basic; dailyRate = daily; hourlyRate = hourly; isActive = active;
        }
    }

    public static class EmployeeRow {
        public final int employeeId;
        public final String employeeCode, fullName, departmentName;
        public final java.math.BigDecimal basicSalary;
        public final boolean isActive;
        public EmployeeRow(int id, String code, String name, java.math.BigDecimal basic, boolean active, String dept) {
            employeeId = id; employeeCode = code; fullName = name; basicSalary = basic; isActive = active; departmentName = dept;
        }
        @Override public String toString() { return fullName != null ? fullName + " (" + employeeCode + ")" : String.valueOf(employeeId); }
    }
}
