import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * MySQL connection. For deployment, use config.properties (copy from config.properties.example).
 * Add MySQL Connector/J to classpath: lib/mysql-connector-j-*.jar
 */
public class Database {
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DB = "payroll";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "";

    private static String jdbcUrl;
    private static String user;
    private static String pass;
    private static boolean driverMissing = false;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            driverMissing = true;
        }
        loadConfig();
    }

    private static void loadConfig() {
        String host = DEFAULT_HOST;
        String port = DEFAULT_PORT;
        String db = DEFAULT_DB;
        user = DEFAULT_USER;
        pass = DEFAULT_PASS;
        try {
            Properties p = new Properties();
            try (FileInputStream in = new FileInputStream("config.properties")) {
                p.load(in);
            }
            if (p.containsKey("db.host")) host = p.getProperty("db.host").trim();
            if (p.containsKey("db.port")) port = p.getProperty("db.port").trim();
            if (p.containsKey("db.name")) db = p.getProperty("db.name").trim();
            if (p.containsKey("db.user")) user = p.getProperty("db.user").trim();
            if (p.containsKey("db.password")) pass = p.getProperty("db.password");
        } catch (Exception ignored) {
            /* use defaults */
        }
        jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false&allowPublicKeyRetrieval=true";
    }

    public static Connection getConnection() throws SQLException {
        if (driverMissing) {
            throw new SQLException("Database driver not found. Add mysql-connector-j JAR to lib/.");
        }
        return DriverManager.getConnection(jdbcUrl, user, pass);
    }
}
