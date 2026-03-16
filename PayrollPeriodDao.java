import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PayrollPeriodDao {
    public static List<PayrollPeriod> findAll() throws SQLException {
        List<PayrollPeriod> list = new ArrayList<>();
        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT period_id, period_name, start_date, end_date, pay_date, status FROM PayrollPeriod ORDER BY start_date DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt(1);
                String name = rs.getString(2);
                Date start = rs.getDate(3);
                Date end = rs.getDate(4);
                Date pay = rs.getDate(5);
                String status = rs.getString(6);
                String computedStatus = status;
                if (start != null && end != null) {
                    // Auto-open when today is within [start, end]; auto-close when today is after end.
                    if (!today.before(start) && !today.after(end)) {
                        computedStatus = "open";
                    } else if (today.after(end)) {
                        computedStatus = "closed";
                    }
                }
                list.add(new PayrollPeriod(id, name, start, end, pay, computedStatus));
            }
        }
        return list;
    }

    /** Periods whose start_date is within [startInclusive, endInclusive]. For 13th-month quarter range. */
    public static List<PayrollPeriod> getPeriodsInDateRange(Date startInclusive, Date endInclusive) throws SQLException {
        List<PayrollPeriod> list = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT period_id, period_name, start_date, end_date, pay_date, status FROM PayrollPeriod WHERE start_date >= ? AND start_date <= ? ORDER BY start_date")) {
            ps.setDate(1, startInclusive);
            ps.setDate(2, endInclusive);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new PayrollPeriod(rs.getInt(1), rs.getString(2), rs.getDate(3), rs.getDate(4), rs.getDate(5), rs.getString(6)));
            }
        }
        return list;
    }

    public static void insert(String name, Date start, Date end, Date payDate, String status) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO PayrollPeriod (period_name, start_date, end_date, pay_date, status) VALUES (?,?,?,?,?)")) {
            ps.setString(1, name);
            ps.setDate(2, start);
            ps.setDate(3, end);
            ps.setDate(4, payDate);
            ps.setString(5, status);
            ps.executeUpdate();
        }
    }

    public static void update(int periodId, String name, Date start, Date end, Date payDate, String status) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE PayrollPeriod SET period_name=?, start_date=?, end_date=?, pay_date=?, status=? WHERE period_id=?")) {
            ps.setString(1, name);
            ps.setDate(2, start);
            ps.setDate(3, end);
            ps.setDate(4, payDate);
            ps.setString(5, status);
            ps.setInt(6, periodId);
            ps.executeUpdate();
        }
    }

    public static class PayrollPeriod {
        public final int periodId;
        public final String periodName;
        public final Date startDate, endDate, payDate;
        public final String status;
        public PayrollPeriod(int id, String name, Date start, Date end, Date pay, String status) {
            periodId = id; periodName = name; startDate = start; endDate = end; payDate = pay; this.status = status;
        }
        @Override public String toString() { return periodName; }
    }
}
