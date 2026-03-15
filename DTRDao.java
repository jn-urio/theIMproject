import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DTRDao {
    public static List<DTRRow> findByPeriodAndEmployee(Integer periodId, Integer employeeId) throws SQLException {
        List<DTRRow> list = new ArrayList<>();
        String sql = "SELECT d.dtr_id, d.employee_id, e.full_name, d.date_val, d.time_in, d.time_out, d.regular_hours, d.overtime_hours, COALESCE(d.offset_hours_used,0), d.status " +
            "FROM DTR d JOIN Employee e ON e.employee_id = d.employee_id WHERE 1=1";
        if (periodId != null) sql += " AND d.date_val >= (SELECT start_date FROM PayrollPeriod WHERE period_id=?) AND d.date_val <= (SELECT end_date FROM PayrollPeriod WHERE period_id=?)";
        if (employeeId != null) sql += " AND d.employee_id=?";
        sql += " ORDER BY d.date_val DESC";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            if (periodId != null) { ps.setInt(i++, periodId); ps.setInt(i++, periodId); }
            if (employeeId != null) ps.setInt(i++, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new DTRRow(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getDate(4), rs.getTime(5), rs.getTime(6),
                        rs.getBigDecimal(7), rs.getBigDecimal(8), rs.getBigDecimal(9) != null ? rs.getBigDecimal(9) : java.math.BigDecimal.ZERO, rs.getString(10)));
            }
        }
        return list;
    }

    /** Get attendance status for an employee on a given date (Present, Late, Absent, or null if no record). */
    public static String getStatusForDate(int employeeId, Date dateVal) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT status FROM DTR WHERE employee_id=? AND date_val=? LIMIT 1")) {
            ps.setInt(1, employeeId);
            ps.setDate(2, dateVal);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    /** Set attendance status for an employee on a date (Present, Late, Absent). Updates existing DTR row or inserts minimal row. */
    public static void setAttendanceStatus(int employeeId, Date dateVal, String status) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement upd = c.prepareStatement("UPDATE DTR SET status=? WHERE employee_id=? AND date_val=?")) {
            upd.setString(1, status);
            upd.setInt(2, employeeId);
            upd.setDate(3, dateVal);
            if (upd.executeUpdate() > 0) return;
        }
        try (Connection c = Database.getConnection();
             PreparedStatement ins = c.prepareStatement(
                 "INSERT INTO DTR (employee_id, date_val, time_in, time_out, regular_hours, overtime_hours, offset_hours_used, status) VALUES (?,?,NULL,NULL,0,0,0,?)")) {
            ins.setInt(1, employeeId);
            ins.setDate(2, dateVal);
            ins.setString(3, status);
            ins.executeUpdate();
        }
    }

    /** Set or add offset_hours_used for a date. Creates DTR row if missing (so the day shows offset used, no absence). */
    public static void setOffsetHoursUsed(int employeeId, Date dateVal, java.math.BigDecimal offsetHours) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement upd = c.prepareStatement("UPDATE DTR SET offset_hours_used = COALESCE(offset_hours_used,0) + ? WHERE employee_id = ? AND date_val = ?")) {
            upd.setBigDecimal(1, offsetHours);
            upd.setInt(2, employeeId);
            upd.setDate(3, dateVal);
            if (upd.executeUpdate() > 0) return;
        }
        try (Connection c = Database.getConnection();
             PreparedStatement ins = c.prepareStatement(
                 "INSERT INTO DTR (employee_id, date_val, time_in, time_out, regular_hours, overtime_hours, offset_hours_used, status) VALUES (?,?,NULL,NULL,0,0,?, 'Present')")) {
            ins.setInt(1, employeeId);
            ins.setDate(2, dateVal);
            ins.setBigDecimal(3, offsetHours);
            ins.executeUpdate();
        }
    }

    public static void insert(int employeeId, Date dateVal, Time timeIn, Time timeOut, java.math.BigDecimal regularHours, java.math.BigDecimal overtimeHours, String status) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO DTR (employee_id, date_val, time_in, time_out, regular_hours, overtime_hours, offset_hours_used, status) VALUES (?,?,?,?,?,?,0,?)")) {
            ps.setInt(1, employeeId);
            ps.setDate(2, dateVal);
            ps.setTime(3, timeIn);
            ps.setTime(4, timeOut);
            ps.setBigDecimal(5, regularHours);
            ps.setBigDecimal(6, overtimeHours);
            ps.setString(7, status);
            ps.executeUpdate();
        }
    }

    public static class DTRRow {
        public final int dtrId, employeeId;
        public final String fullName;
        public final Date dateVal;
        public final Time timeIn, timeOut;
        public final java.math.BigDecimal regularHours, overtimeHours, offsetHoursUsed;
        public final String status;
        public DTRRow(int dtrId, int empId, String name, Date d, Time in, Time out, java.math.BigDecimal reg, java.math.BigDecimal ot, java.math.BigDecimal offsetUsed, String st) {
            this.dtrId = dtrId; employeeId = empId; fullName = name; dateVal = d; timeIn = in; timeOut = out;
            regularHours = reg; overtimeHours = ot; offsetHoursUsed = offsetUsed != null ? offsetUsed : java.math.BigDecimal.ZERO; status = st;
        }
    }
}
