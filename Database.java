import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * MySQL connection for XAMPP.
 * You run the schema and seed yourself (e.g. in phpMyAdmin).
 * Add MySQL Connector/J to classpath: lib/mysql-connector-j-*.jar
 */
public class Database {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/payroll?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "";

    private static boolean driverMissing = false;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            driverMissing = true;
        }
    }

    public static Connection getConnection() throws SQLException {
        if (driverMissing) {
            throw new SQLException("Database not available.");
        }
        return DriverManager.getConnection(JDBC_URL, USER, PASS);
    }
}
