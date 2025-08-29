package com.event.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Database Connection Manager for EventTech Application
 * Handles MySQL database connectivity using JDBC
 */
public class DBConnection {
    
    // Database configuration - parsed from DATABASE_URL or fallback
    private static String DB_URL;
    private static String DB_USERNAME;
    private static String DB_PASSWORD;
    
    // JDBC Driver class
    private static final String JDBC_DRIVER = "org.postgresql.Driver";
    
    static {
        try {
            // Load PostgreSQL JDBC Driver first
            Class.forName(JDBC_DRIVER);
            // Then parse database URL
            parseDatabaseURL();
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver not found: " + e.getMessage());
            throw new RuntimeException("Failed to load PostgreSQL JDBC Driver", e);
        }
    }
    
    private static void parseDatabaseURL() {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl != null) {
            try {
                URI uri = new URI(databaseUrl);
                String host = uri.getHost();
                int port = uri.getPort();
                String database = uri.getPath().substring(1); // Remove leading '/'
                String query = uri.getQuery();
                
                DB_URL = "jdbc:postgresql://" + host + ":" + port + "/" + database;
                if (query != null) {
                    DB_URL += "?" + query;
                }
                
                String userInfo = uri.getUserInfo();
                if (userInfo != null) {
                    String[] credentials = userInfo.split(":");
                    DB_USERNAME = credentials[0];
                    DB_PASSWORD = credentials.length > 1 ? credentials[1] : "";
                } else {
                    DB_USERNAME = System.getenv("PGUSER") != null ? System.getenv("PGUSER") : "postgres";
                    DB_PASSWORD = System.getenv("PGPASSWORD") != null ? System.getenv("PGPASSWORD") : "";
                }
            } catch (URISyntaxException e) {
                System.err.println("Error parsing DATABASE_URL: " + e.getMessage());
                setFallbackValues();
            }
        } else {
            setFallbackValues();
        }
    }
    
    private static void setFallbackValues() {
        DB_URL = "jdbc:postgresql://localhost:5432/eventtech";
        DB_USERNAME = "postgres";
        DB_PASSWORD = "";
    }
    
    /**
     * Get database connection
     * @return Connection object for database operations
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            connection.setAutoCommit(true); // Enable auto-commit for simplicity
            return connection;
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            throw new SQLException("Unable to connect to database", e);
        }
    }
    
    /**
     * Close database connection safely
     * @param connection Connection to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Test database connectivity
     * @return true if connection successful, false otherwise
     */
    public static boolean testConnection() {
        Connection connection = null;
        try {
            connection = getConnection();
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        } finally {
            closeConnection(connection);
        }
    }
}
