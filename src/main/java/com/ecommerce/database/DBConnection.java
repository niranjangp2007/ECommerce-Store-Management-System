package com.ecommerce.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Database Connection Factory for MySQL JDBC.
 * Reads connection settings securely with the following priority:
 * 1. Environment variables: ECOM_DB_URL, ECOM_DB_USER, ECOM_DB_PASSWORD
 * 2. Optional local git-ignored properties file: local.properties or db.properties
 * 3. Defaults: URL = jdbc:mysql://localhost:3306/ecom_db, USER = root, PASSWORD = empty
 *
 * Security Guarantee: Credentials are never hardcoded and passwords are never printed or logged.
 */
public final class DBConnection {

    public static final String DEFAULT_DATABASE_NAME = "ecom_db";
    public static final String DEFAULT_URL = 
            "jdbc:mysql://localhost:3306/" + DEFAULT_DATABASE_NAME + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    public static final String DEFAULT_USER = "root";

    public static final List<String> REQUIRED_TABLES = Arrays.asList(
            "customers", "products", "orders", "order_items"
    );

    private static final Properties localFileProperties = new Properties();

    static {
        loadLocalProperties();
    }

    private DBConnection() {
    }

    private static void loadLocalProperties() {
        File[] candidates = new File[] {
                new File("local.properties"),
                new File("db.properties"),
                new File("src/main/resources/db.properties")
        };
        for (File candidate : candidates) {
            if (candidate.exists() && candidate.isFile()) {
                try (FileInputStream fis = new FileInputStream(candidate)) {
                    localFileProperties.load(fis);
                    break;
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static String getDatabaseUrl() {
        String envUrl = System.getenv("ECOM_DB_URL");
        if (envUrl != null && !envUrl.trim().isEmpty()) {
            return envUrl.trim();
        }
        String propUrl = localFileProperties.getProperty("db.url");
        if (propUrl != null && !propUrl.trim().isEmpty()) {
            return propUrl.trim();
        }
        return DEFAULT_URL;
    }

    public static String getDatabaseUser() {
        String envUser = System.getenv("ECOM_DB_USER");
        if (envUser != null && !envUser.trim().isEmpty()) {
            return envUser.trim();
        }
        String propUser = localFileProperties.getProperty("db.user");
        if (propUser != null && !propUser.trim().isEmpty()) {
            return propUser.trim();
        }
        return DEFAULT_USER;
    }

    public static boolean isPasswordConfigured() {
        String envPass = System.getenv("ECOM_DB_PASSWORD");
        if (envPass != null && !envPass.isEmpty()) {
            return true;
        }
        String propPass = localFileProperties.getProperty("db.password");
        return propPass != null && !propPass.isEmpty();
    }

    private static String getDatabasePassword() {
        String envPass = System.getenv("ECOM_DB_PASSWORD");
        if (envPass != null) {
            return envPass;
        }
        String propPass = localFileProperties.getProperty("db.password");
        return propPass != null ? propPass : "";
    }

    /**
     * Establishes and returns a new JDBC Connection using the default configured URL.
     */
    public static Connection getConnection() throws SQLException {
        return getConnection(getDatabaseUrl());
    }

    /**
     * Establishes and returns a new JDBC Connection for a specific URL using configured credentials.
     */
    public static Connection getConnection(String url) throws SQLException {
        String user = getDatabaseUser();
        String pass = getDatabasePassword();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, user, pass);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found in classpath: " + e.getMessage(), e);
        }
    }

    /**
     * Performs a comprehensive diagnostic check of the database connection
     * and required tables, returning a structured sanitized report.
     */
    public static ConnectionDiagnostic diagnoseConnection() {
        String url = getDatabaseUrl();
        String user = getDatabaseUser();
        boolean passwordSet = isPasswordConfigured();

        try (Connection conn = getConnection()) {
            // Connection succeeded; check table presence
            List<String> foundTables = new ArrayList<>();
            List<String> missingTables = new ArrayList<>();

            DatabaseMetaData metaData = conn.getMetaData();
            for (String table : REQUIRED_TABLES) {
                try (ResultSet rs = metaData.getTables(null, null, table, new String[]{"TABLE"})) {
                    if (rs.next()) {
                        foundTables.add(table);
                    } else {
                        missingTables.add(table);
                    }
                }
            }

            return new ConnectionDiagnostic(
                    true,
                    ConnectionDiagnostic.Category.SUCCESS,
                    "Connection successful to MySQL on " + url + " as user '" + user + "'.",
                    user,
                    passwordSet,
                    foundTables,
                    missingTables
            );

        } catch (SQLException e) {
            int errorCode = e.getErrorCode();
            String sqlState = e.getSQLState() != null ? e.getSQLState() : "";

            ConnectionDiagnostic.Category category;
            String message;

            if (errorCode == 1045 || "28000".equals(sqlState)) {
                if (!passwordSet) {
                    category = ConnectionDiagnostic.Category.MISSING_CREDENTIALS;
                    message = "Authentication failed: MySQL server rejected user '" + user + 
                            "' with empty password. ECOM_DB_PASSWORD is not set.";
                } else {
                    category = ConnectionDiagnostic.Category.AUTHENTICATION_FAILED;
                    message = "Authentication failed: Access denied for user '" + user + 
                            "'. Please verify the password in ECOM_DB_PASSWORD.";
                }
            } else if (errorCode == 1049 || "42000".equals(sqlState)) {
                category = ConnectionDiagnostic.Category.DATABASE_NOT_FOUND;
                message = "Authentication succeeded, but database '" + DEFAULT_DATABASE_NAME + 
                        "' was not found on the MySQL server. Run sql/schema_and_seed.sql to create it.";
            } else if (sqlState.startsWith("08")) {
                category = ConnectionDiagnostic.Category.CONNECTION_REFUSED;
                message = "Connection refused: Unable to connect to MySQL on localhost:3306. Verify the MySQL service is running.";
            } else {
                category = ConnectionDiagnostic.Category.SQL_ERROR;
                message = "SQL Error [Code: " + errorCode + ", State: " + sqlState + "]: " + e.getMessage();
            }

            return new ConnectionDiagnostic(
                    false,
                    category,
                    message,
                    user,
                    passwordSet,
                    List.of(),
                    REQUIRED_TABLES
            );
        }
    }

    /**
     * Backward-compatible simple connectivity check.
     */
    public static boolean testConnection() {
        return diagnoseConnection().isSuccess();
    }

    /**
     * Safely closes an AutoCloseable resource without throwing.
     */
    public static void closeQuietly(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Diagnostic report class.
     */
    public static class ConnectionDiagnostic {
        public enum Category {
            SUCCESS,
            MISSING_CREDENTIALS,
            AUTHENTICATION_FAILED,
            DATABASE_NOT_FOUND,
            CONNECTION_REFUSED,
            SQL_ERROR
        }

        private final boolean success;
        private final Category category;
        private final String message;
        private final String user;
        private final boolean passwordConfigured;
        private final List<String> foundTables;
        private final List<String> missingTables;

        public ConnectionDiagnostic(boolean success, Category category, String message, String user,
                                    boolean passwordConfigured, List<String> foundTables, List<String> missingTables) {
            this.success = success;
            this.category = category;
            this.message = message;
            this.user = user;
            this.passwordConfigured = passwordConfigured;
            this.foundTables = foundTables;
            this.missingTables = missingTables;
        }

        public boolean isSuccess() {
            return success;
        }

        public Category getCategory() {
            return category;
        }

        public String getMessage() {
            return message;
        }

        public String getUser() {
            return user;
        }

        public boolean isPasswordConfigured() {
            return passwordConfigured;
        }

        public List<String> getFoundTables() {
            return foundTables;
        }

        public List<String> getMissingTables() {
            return missingTables;
        }

        public boolean areAllTablesPresent() {
            return success && missingTables.isEmpty();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Connection Diagnostic:\n");
            sb.append("  Status: ").append(success ? "CONNECTED" : "FAILED").append("\n");
            sb.append("  Category: ").append(category).append("\n");
            sb.append("  Configured User: ").append(user).append("\n");
            sb.append("  Password Set: ").append(passwordConfigured ? "Yes (value hidden)" : "No (empty)").append("\n");
            sb.append("  Message: ").append(message).append("\n");
            if (success) {
                sb.append("  Tables Found: ").append(foundTables).append("\n");
                if (!missingTables.isEmpty()) {
                    sb.append("  Missing Tables: ").append(missingTables).append("\n");
                }
            }
            return sb.toString();
        }
    }
}
