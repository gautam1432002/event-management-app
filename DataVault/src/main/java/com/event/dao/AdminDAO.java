package com.event.dao;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Access Object for Admin operations
 * Handles admin authentication and admin-related database operations
 */
public class AdminDAO {
    
    /**
     * Authenticate admin login
     * @param username Admin username
     * @param password Admin password (plain text for now, should be hashed in production)
     * @return Admin details map if successful, null if failed
     */
    public Map<String, Object> authenticateAdmin(String username, String password) {
        String sql = "SELECT id, username, role FROM users WHERE username = ? AND password = ? AND role = 'admin'";
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(sql);
            
            statement.setString(1, username.trim());
            statement.setString(2, password); // TODO: Implement password hashing (BCrypt recommended)
            
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                Map<String, Object> admin = new HashMap<>();
                admin.put("id", resultSet.getInt("id"));
                admin.put("username", resultSet.getString("username"));
                admin.put("role", resultSet.getString("role"));
                
                return admin;
            }
            
            return null;
            
        } catch (SQLException e) {
            System.err.println("Error authenticating admin: " + e.getMessage());
            return null;
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }
    
    /**
     * Create a new admin user
     * @param username Admin username
     * @param password Admin password (plain text for now)
     * @return Admin ID if successful, -1 if failed
     */
    public int createAdmin(String username, String password) {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'admin')";
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            statement.setString(1, username.trim());
            statement.setString(2, password); // TODO: Hash password before storing
            
            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
            
            return -1;
            
        } catch (SQLException e) {
            System.err.println("Error creating admin: " + e.getMessage());
            return -1;
        } finally {
            closeResources(connection, statement, generatedKeys);
        }
    }
    
    /**
     * Check if username already exists
     * @param username Username to check
     * @return true if exists, false otherwise
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, username.trim());
            
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
            
            return false;
            
        } catch (SQLException e) {
            System.err.println("Error checking username existence: " + e.getMessage());
            return true; // Return true to prevent duplicates on error
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }
    
    /**
     * Update admin password
     * @param adminId Admin ID
     * @param newPassword New password
     * @return true if successful, false otherwise
     */
    public boolean updateAdminPassword(int adminId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ? AND role = 'admin'";
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(sql);
            
            statement.setString(1, newPassword); // TODO: Hash password before storing
            statement.setInt(2, adminId);
            
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating admin password: " + e.getMessage());
            return false;
        } finally {
            closeResources(connection, statement, null);
        }
    }
    
    /**
     * Get admin by ID
     * @param adminId Admin ID
     * @return Admin details map or null if not found
     */
    public Map<String, Object> getAdminById(int adminId) {
        String sql = "SELECT id, username, role FROM users WHERE id = ? AND role = 'admin'";
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, adminId);
            
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                Map<String, Object> admin = new HashMap<>();
                admin.put("id", resultSet.getInt("id"));
                admin.put("username", resultSet.getString("username"));
                admin.put("role", resultSet.getString("role"));
                
                return admin;
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching admin by ID: " + e.getMessage());
        } finally {
            closeResources(connection, statement, resultSet);
        }
        
        return null;
    }
    
    /**
     * Log admin action for audit trail
     * @param adminId Admin ID who performed the action
     * @param action Description of the action performed
     * @return true if logged successfully, false otherwise
     */
    public boolean logAdminAction(int adminId, String action) {
        String sql = "INSERT INTO audit_log (admin_id, action, timestamp) VALUES (?, ?, NOW())";
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(sql);
            
            statement.setInt(1, adminId);
            statement.setString(2, action.trim());
            
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error logging admin action: " + e.getMessage());
            return false;
        } finally {
            closeResources(connection, statement, null);
        }
    }
    
    /**
     * Validate admin session (check if admin ID exists and is active)
     * @param adminId Admin ID to validate
     * @return true if valid, false otherwise
     */
    public boolean validateAdminSession(int adminId) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ? AND role = 'admin'";
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, adminId);
            
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
            
            return false;
            
        } catch (SQLException e) {
            System.err.println("Error validating admin session: " + e.getMessage());
            return false;
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }
    
    /**
     * Close database resources safely
     */
    private void closeResources(Connection connection, PreparedStatement statement, ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                System.err.println("Error closing ResultSet: " + e.getMessage());
            }
        }
        
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                System.err.println("Error closing PreparedStatement: " + e.getMessage());
            }
        }
        
        DBConnection.closeConnection(connection);
    }
}
