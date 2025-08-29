package com.event.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Registration operations
 * Handles all database operations related to user registrations
 */
public class RegistrationDAO {
    
    /**
     * Register a new participant for an event
     * @param name Full name of participant
     * @param email Email address
     * @param college College name
     * @param event Event name
     * @return Registration ID if successful, -1 if failed
     */
    public int registerParticipant(String name, String email, String college, String event) {
        String sql = "INSERT INTO registrations (name, email, college, event, registration_date, winner_status) VALUES (?, ?, ?, ?, NOW(), 0)";
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            statement.setString(1, name.trim());
            statement.setString(2, email.trim().toLowerCase());
            statement.setString(3, college.trim());
            statement.setString(4, event.trim());
            
            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
            
            return -1;
            
        } catch (SQLException e) {
            System.err.println("Error registering participant: " + e.getMessage());
            return -1;
        } finally {
            closeResources(connection, statement, generatedKeys);
        }
    }
    
    /**
     * Check if email is already registered for a specific event
     * @param email Email to check
     * @param event Event name
     * @return true if already registered, false otherwise
     */
    public boolean isEmailRegistered(String email, String event) {
        String sql = "SELECT COUNT(*) FROM registrations WHERE email = ? AND event = ?";
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, email.trim().toLowerCase());
            statement.setString(2, event.trim());
            
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
            
            return false;
            
        } catch (SQLException e) {
            System.err.println("Error checking email registration: " + e.getMessage());
            return true; // Return true to prevent duplicate registrations on error
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }
    
    /**
     * Get all registrations with pagination
     * @param offset Starting position
     * @param limit Number of records to fetch
     * @return List of registration maps
     */
    public List<Map<String, Object>> getAllRegistrations(int offset, int limit) {
        String sql = "SELECT id, name, email, college, event, registration_date, winner_status " +
                    "FROM registrations ORDER BY registration_date DESC LIMIT ? OFFSET ?";
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<Map<String, Object>> registrations = new ArrayList<>();
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, limit);
            statement.setInt(2, offset);
            
            resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                Map<String, Object> registration = new HashMap<>();
                registration.put("id", resultSet.getInt("id"));
                registration.put("name", resultSet.getString("name"));
                registration.put("email", resultSet.getString("email"));
                registration.put("college", resultSet.getString("college"));
                registration.put("event", resultSet.getString("event"));
                registration.put("registration_date", resultSet.getTimestamp("registration_date"));
                registration.put("winner_status", resultSet.getBoolean("winner_status"));
                
                registrations.add(registration);
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching registrations: " + e.getMessage());
        } finally {
            closeResources(connection, statement, resultSet);
        }
        
        return registrations;
    }
    
    /**
     * Get total count of registrations
     * @return Total registration count
     */
    public int getTotalRegistrationCount() {
        String sql = "SELECT COUNT(*) FROM registrations";
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting registration count: " + e.getMessage());
        } finally {
            closeResources(connection, statement, resultSet);
        }
        
        return 0;
    }
    
    /**
     * Update winner status for a participant
     * @param registrationId Registration ID
     * @param isWinner Winner status
     * @return true if successful, false otherwise
     */
    public boolean updateWinnerStatus(int registrationId, boolean isWinner) {
        String sql = "UPDATE registrations SET winner_status = ? WHERE id = ?";
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(sql);
            statement.setBoolean(1, isWinner);
            statement.setInt(2, registrationId);
            
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating winner status: " + e.getMessage());
            return false;
        } finally {
            closeResources(connection, statement, null);
        }
    }
    
    /**
     * Delete a registration
     * @param registrationId Registration ID to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteRegistration(int registrationId) {
        String sql = "DELETE FROM registrations WHERE id = ?";
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, registrationId);
            
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting registration: " + e.getMessage());
            return false;
        } finally {
            closeResources(connection, statement, null);
        }
    }
    
    /**
     * Get registration by ID
     * @param registrationId Registration ID
     * @return Registration map or null if not found
     */
    public Map<String, Object> getRegistrationById(int registrationId) {
        String sql = "SELECT id, name, email, college, event, registration_date, winner_status " +
                    "FROM registrations WHERE id = ?";
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, registrationId);
            
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                Map<String, Object> registration = new HashMap<>();
                registration.put("id", resultSet.getInt("id"));
                registration.put("name", resultSet.getString("name"));
                registration.put("email", resultSet.getString("email"));
                registration.put("college", resultSet.getString("college"));
                registration.put("event", resultSet.getString("event"));
                registration.put("registration_date", resultSet.getTimestamp("registration_date"));
                registration.put("winner_status", resultSet.getBoolean("winner_status"));
                
                return registration;
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching registration by ID: " + e.getMessage());
        } finally {
            closeResources(connection, statement, resultSet);
        }
        
        return null;
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
