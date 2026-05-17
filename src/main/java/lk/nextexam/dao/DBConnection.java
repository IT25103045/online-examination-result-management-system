package lk.nextexam.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Central MySQL database connection helper for NextExamLK.
 *
 * Environment variables supported:
 * DATABASE_URL
 * DATABASE_USER
 * DATABASE_PASSWORD
 *
 * Local default:
 * jdbc:mysql://localhost:3306/nextexam_db
 */
public class DBConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/nextexam_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "20031107Cp#";

    private DBConnection() {
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