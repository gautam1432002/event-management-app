package com.event.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Event operations
 * Handles all database operations related to events management
 */
public class EventDAO {
    
    /**
     * Get all available events
     * @return List of event maps containing id, name, and description
     */
    public List<Map<String, Object>> getAllEvents() {
        String sql = "SELECT id, event_name, description FROM events ORDER BY event_name";
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<Map<String, Object>> events = new ArrayList<>();
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                Map<String, Object> event = new HashMap<>();
                event.put("id", resultSet.getInt("id"));
                event.put("event_name", resultSet.getString("event_name"));
                event.put("description", resultSet.getString("description"));
                
                events.add(event);
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching events: " + e.getMessage());
        } finally {
            closeResources(connection, statement, resultSet);
        }
        
        return events;
    }
    
    /**
     * Add a new event
     * @param eventName Name of the event
     * @param description Event description
     * @return Event ID if successful, -1 if failed
     */
    public int addEvent(String eventName, String description) {
        String sql = "INSERT INTO events (event_name, description) VALUES (?, ?)";
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            statement.setString(1, eventName.trim());
            statement.setString(2, description.trim());
            
            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
            
            return -1;
            
        } catch (SQLException e) {
            System.err.println("Error adding event: " + e.getMessage());
            return -1;
        } finally {
            closeResources(connection, statement, generatedKeys);
        }
    }
    
    /**
     * Update an existing event
     * @param eventId Event ID to update
     * @param eventName New event name
     * @param description New event description
     * @return true if successful, false otherwise
     */
    public boolean updateEvent(int eventId, String eventName, String description) {
        String sql = "UPDATE events SET event_name = ?, description = ? WHERE id = ?";
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(sql);
            
            statement.setString(1, eventName.trim());
            statement.setString(2, description.trim());
            statement.setInt(3, eventId);
            
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating event: " + e.getMessage());
            return false;
        } finally {
            closeResources(connection, statement, null);
        }
    }
    
    /**
     * Delete an event (only if no registrations exist)
     * @param eventId Event ID to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteEvent(int eventId) {
        String checkSql = "SELECT COUNT(*) FROM registrations WHERE event = (SELECT event_name FROM events WHERE id = ?)";
        String deleteSql = "DELETE FROM events WHERE id = ?";
        
        Connection connection = null;
        PreparedStatement checkStatement = null;
        PreparedStatement deleteStatement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            
            // Check if event has registrations
            checkStatement = connection.prepareStatement(checkSql);
            checkStatement.setInt(1, eventId);
            resultSet = checkStatement.executeQuery();
            
            if (resultSet.next() && resultSet.getInt(1) > 0) {
                // Event has registrations, cannot delete
                return false;
            }
            
            // No registrations, safe to delete
            deleteStatement = connection.prepareStatement(deleteSql);
            deleteStatement.setInt(1, eventId);
            
            int rowsAffected = deleteStatement.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting event: " + e.getMessage());
            return false;
        } finally {
            closeResources(connection, checkStatement, resultSet);
            if (deleteStatement != null) {
                try {
                    deleteStatement.close();
                } catch (SQLException e) {
                    System.err.println("Error closing delete statement: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Get event by ID
     * @param eventId Event ID
     * @return Event map or null if not found
     */
    public Map<String, Object> getEventById(int eventId) {
        String sql = "SELECT id, event_name, description FROM events WHERE id = ?";
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, eventId);
            
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                Map<String, Object> event = new HashMap<>();
                event.put("id", resultSet.getInt("id"));
                event.put("event_name", resultSet.getString("event_name"));
                event.put("description", resultSet.getString("description"));
                
                return event;
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching event by ID: " + e.getMessage());
        } finally {
            closeResources(connection, statement, resultSet);
        }
        
        return null;
    }
    
    /**
     * Check if event name already exists
     * @param eventName Event name to check
     * @return true if exists, false otherwise
     */
    public boolean eventNameExists(String eventName) {
        String sql = "SELECT COUNT(*) FROM events WHERE event_name = ?";
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, eventName.trim());
            
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
            
            return false;
            
        } catch (SQLException e) {
            System.err.println("Error checking event name existence: " + e.getMessage());
            return true; // Return true to prevent duplicates on error
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }
    
    /**
     * Get registration count by event
     * @return Map of event names and their registration counts
     */
    public Map<String, Integer> getEventRegistrationCounts() {
        String sql = "SELECT event, COUNT(*) as count FROM registrations GROUP BY event";
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        Map<String, Integer> counts = new HashMap<>();
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                counts.put(resultSet.getString("event"), resultSet.getInt("count"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching event registration counts: " + e.getMessage());
        } finally {
            closeResources(connection, statement, resultSet);
        }
        
        return counts;
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
