import java.sql.*;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/** Statutory rates (Pag-IBIG, PhilHealth, SSS note) for payroll. */
public class StatutoryDao {
    public static BigDecimal getDecimal(String name) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT value_decimal FROM StatutorySetting WHERE name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : null;
            }
        }
    }

    public static String getText(String name) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT value_text FROM StatutorySetting WHERE name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    public static void setDecimal(String name, BigDecimal value) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO StatutorySetting (name, value_decimal) VALUES (?,?) ON DUPLICATE KEY UPDATE value_decimal=?, updated_at=CURRENT_TIMESTAMP")) {
            ps.setString(1, name);
            ps.setBigDecimal(2, value);
            ps.setBigDecimal(3, value);
            ps.executeUpdate();
        }
    }

    public static void setText(String name, String value) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO StatutorySetting (name, value_text) VALUES (?,?) ON DUPLICATE KEY UPDATE value_text=?, updated_at=CURRENT_TIMESTAMP")) {
            ps.setString(1, name);
            ps.setString(2, value);
            ps.setString(3, value);
            ps.executeUpdate();
        }
    }

    /** All settings as name -> value string for display. */
    public static Map<String, String> getAll() throws SQLException {
        Map<String, String> out = new LinkedHashMap<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT name, value_text, value_decimal FROM StatutorySetting ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String v = rs.getString(2);
                if (v == null || v.isEmpty()) {
                    BigDecimal d = rs.getBigDecimal(3);
                    v = d != null ? d.toPlainString() : "";
                }
                out.put(rs.getString(1), v);
            }
        }
        return out;
    }
}
