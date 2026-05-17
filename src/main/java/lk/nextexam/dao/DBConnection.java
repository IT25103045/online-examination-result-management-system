package lk.nextexam.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Central MySQL database connection helper for NextExamLK.
 */
public class DBConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/nextexam_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static final String DEFAULT_USER = "root";

    /*
     * Local testing only:
     * Put your MySQL password here temporarily if environment variables are not working.
     * Before GitHub push, change this back to "".
     */
    private static final String DEFAULT_PASSWORD = "";

    private DBConnection() {
    }

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("DBConnection -> MySQL JDBC driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.out.println("DBConnection ERROR -> MySQL JDBC driver not found");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = getEnvOrDefault("DATABASE_URL", DEFAULT_URL);
        String user = getEnvOrDefault("DATABASE_USER", DEFAULT_USER);
        String password = getEnvOrDefault("DATABASE_PASSWORD", DEFAULT_PASSWORD);

        return DriverManager.getConnection(url, user, password);
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);

        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        return value.trim();
    }
}