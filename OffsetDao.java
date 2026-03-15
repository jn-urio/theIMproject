import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Offset: balance (earned from OT converted), requests to use offset on a date (e.g. leave early, no absence). */
public class OffsetDao {

    // --- Balance ---
    public static BigDecimal getBalance(int employeeId) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT balance_hours FROM OffsetBalance WHERE employee_id = ?")) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    /** Add hours to balance (e.g. OT converted to offset). Creates row if none. */
    public static void addToBalance(int employeeId, BigDecimal hours, String notes) throws SQLException {
        if (hours == null || hours.compareTo(BigDecimal.ZERO) <= 0) return;
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO OffsetBalance (employee_id, balance_hours) VALUES (?, ?) ON DUPLICATE KEY UPDATE balance_hours = balance_hours + ?")) {
            ps.setInt(1, employeeId);
            ps.setBigDecimal(2, hours);
            ps.setBigDecimal(3, hours);
            ps.executeUpdate();
        }
    }

    /** Deduct hours from balance (when request approved). */
    public static void deductFromBalance(int employeeId, BigDecimal hours) throws SQLException {
        if (hours == null || hours.compareTo(BigDecimal.ZERO) <= 0) return;
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE OffsetBalance SET balance_hours = balance_hours - ? WHERE employee_id = ?")) {
            ps.setBigDecimal(1, hours);
            ps.setInt(2, employeeId);
            ps.executeUpdate();
        }
    }

    // --- Requests ---
    public static int insertRequest(int employeeId, BigDecimal hoursToUse, Date dateToApply, String notes) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO OffsetRequest (employee_id, hours_to_use, date_to_apply, status, notes) VALUES (?,?,?, 'pending', ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, employeeId);
            ps.setBigDecimal(2, hoursToUse);
            ps.setDate(3, dateToApply);
            ps.setString(4, notes);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Insert offset request failed");
    }

    public static List<OffsetRequestRow> findRequests(String statusFilter, Integer employeeId) throws SQLException {
        List<OffsetRequestRow> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT r.request_id, r.employee_id, e.full_name, r.hours_to_use, r.date_to_apply, r.status, r.requested_at, r.approved_by, r.approved_at, r.notes " +
            "FROM OffsetRequest r JOIN Employee e ON e.employee_id = r.employee_id WHERE 1=1");
        if (statusFilter != null && !statusFilter.isEmpty() && !"All".equals(statusFilter))
            sql.append(" AND r.status = ?");
        if (employeeId != null) sql.append(" AND r.employee_id = ?");
        sql.append(" ORDER BY r.requested_at DESC");
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int i = 1;
            if (statusFilter != null && !statusFilter.isEmpty() && !"All".equals(statusFilter)) ps.setString(i++, statusFilter);
            if (employeeId != null) ps.setInt(i++, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new OffsetRequestRow(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getBigDecimal(4), rs.getDate(5),
                        rs.getString(6), rs.getTimestamp(7), rs.getObject(8) != null ? ((Number) rs.getObject(8)).intValue() : null, rs.getTimestamp(9), rs.getString(10)));
            }
        }
        return list;
    }

    /** Approve request: deduct balance, set DTR.offset_hours_used for that date (create/update DTR row). */
    public static void approveRequest(int requestId, int approvedBy) throws SQLException {
        OffsetRequestRow r = getRequestById(requestId);
        if (r == null) throw new SQLException("Request not found");
        if (!"pending".equalsIgnoreCase(r.status)) throw new SQLException("Request already " + r.status);
        BigDecimal balance = getBalance(r.employeeId);
        if (balance.compareTo(r.hoursToUse) < 0) throw new SQLException("Insufficient offset balance. Balance: " + balance + ", requested: " + r.hoursToUse);
        deductFromBalance(r.employeeId, r.hoursToUse);
        DTRDao.setOffsetHoursUsed(r.employeeId, r.dateToApply, r.hoursToUse);
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE OffsetRequest SET status = 'approved', approved_by = ?, approved_at = CURRENT_TIMESTAMP WHERE request_id = ?")) {
            ps.setInt(1, approvedBy);
            ps.setInt(2, requestId);
            ps.executeUpdate();
        }
    }

    public static void rejectRequest(int requestId) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE OffsetRequest SET status = 'rejected' WHERE request_id = ?")) {
            ps.setInt(1, requestId);
            ps.executeUpdate();
        }
    }

    public static OffsetRequestRow getRequestById(int requestId) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT request_id, employee_id, (SELECT full_name FROM Employee WHERE employee_id = r.employee_id), hours_to_use, date_to_apply, status, requested_at, approved_by, approved_at, notes FROM OffsetRequest r WHERE request_id = ?")) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new OffsetRequestRow(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getBigDecimal(4), rs.getDate(5),
                    rs.getString(6), rs.getTimestamp(7), rs.getObject(8) != null ? rs.getInt(8) : null, rs.getTimestamp(9), rs.getString(10));
            }
        }
    }

    public static class OffsetRequestRow {
        public final int requestId, employeeId;
        public final String fullName;
        public final BigDecimal hoursToUse;
        public final Date dateToApply;
        public final String status;
        public final Timestamp requestedAt;
        public final Integer approvedBy;
        public final Timestamp approvedAt;
        public final String notes;
        public OffsetRequestRow(int id, int empId, String name, BigDecimal hours, Date dateToApply, String status, Timestamp requested, Integer approvedBy, Timestamp approvedAt, String notes) {
            requestId = id; employeeId = empId; fullName = name; hoursToUse = hours; this.dateToApply = dateToApply; this.status = status;
            requestedAt = requested; this.approvedBy = approvedBy; this.approvedAt = approvedAt; this.notes = notes;
        }
    }
}
