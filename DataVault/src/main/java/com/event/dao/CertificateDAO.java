package com.event.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Certificate operations
 * Handles certificate generation and tracking
 */
public class CertificateDAO {
    
    /**
     * Generate certificate data for a participant
     * @param registrationId Registration ID
     * @param certificateType Type of certificate (participation/winner)
     * @return Certificate data map or null if failed
     */
    public Map<String, Object> generateCertificateData(int registrationId, String certificateType) {
        String sql = "SELECT r.id, r.name, r.email, r.college, r.event, r.registration_date, r.winner_status " +
                    "FROM registrations r WHERE r.id = ?";
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, registrationId);
            
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                Map<String, Object> certificateData = new HashMap<>();
                
                // Participant details
                certificateData.put("id", resultSet.getInt("id"));
                certificateData.put("name", resultSet.getString("name"));
                certificateData.put("email", resultSet.getString("email"));
                certificateData.put("college", resultSet.getString("college"));
                certificateData.put("event", resultSet.getString("event"));
                certificateData.put("registration_date", resultSet.getTimestamp("registration_date"));
                certificateData.put("winner_status", resultSet.getBoolean("winner_status"));
                
                // Certificate metadata
                certificateData.put("certificate_type", certificateType);
                certificateData.put("event_title", "TARUNYAM - Tech Event 2025");
                certificateData.put("issue_date", new Timestamp(System.currentTimeMillis()));
                
                // Generate certificate ID
                String certificateId = generateCertificateId(registrationId, certificateType);
                certificateData.put("certificate_id", certificateId);
                
                return certificateData;
            }
            
        } catch (SQLException e) {
            System.err.println("Error generating certificate data: " + e.getMessage());
        } finally {
            closeResources(connection, statement, resultSet);
        }
        
        return null;
    }
    
    /**
     * Track certificate generation
     * @param registrationId Registration ID
     * @param certificateType Certificate type
     * @param certificateId Certificate ID
     * @return true if tracked successfully, false otherwise
     */
    public boolean trackCertificateGeneration(int registrationId, String certificateType, String certificateId) {
        String sql = "INSERT INTO certificate_log (registration_id, certificate_type, certificate_id, generated_date) " +
                    "VALUES (?, ?, ?, NOW()) ON DUPLICATE KEY UPDATE generated_date = NOW()";
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(sql);
            
            statement.setInt(1, registrationId);
            statement.setString(2, certificateType);
            statement.setString(3, certificateId);
            
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error tracking certificate generation: " + e.getMessage());
            return false;
        } finally {
            closeResources(connection, statement, null);
        }
    }
    
    /**
     * Get certificate generation history for a registration
     * @param registrationId Registration ID
     * @return List of certificate generation records
     */
    public List<Map<String, Object>> getCertificateHistory(int registrationId) {
        String sql = "SELECT certificate_type, certificate_id, generated_date " +
                    "FROM certificate_log WHERE registration_id = ? ORDER BY generated_date DESC";
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<Map<String, Object>> history = new ArrayList<>();
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, registrationId);
            
            resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                Map<String, Object> record = new HashMap<>();
                record.put("certificate_type", resultSet.getString("certificate_type"));
                record.put("certificate_id", resultSet.getString("certificate_id"));
                record.put("generated_date", resultSet.getTimestamp("generated_date"));
                
                history.add(record);
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching certificate history: " + e.getMessage());
        } finally {
            closeResources(connection, statement, resultSet);
        }
        
        return history;
    }
    
    /**
     * Get all winners for certificate generation
     * @return List of winner registration maps
     */
    public List<Map<String, Object>> getAllWinners() {
        String sql = "SELECT id, name, email, college, event, registration_date " +
                    "FROM registrations WHERE winner_status = 1 ORDER BY event, name";
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<Map<String, Object>> winners = new ArrayList<>();
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                Map<String, Object> winner = new HashMap<>();
                winner.put("id", resultSet.getInt("id"));
                winner.put("name", resultSet.getString("name"));
                winner.put("email", resultSet.getString("email"));
                winner.put("college", resultSet.getString("college"));
                winner.put("event", resultSet.getString("event"));
                winner.put("registration_date", resultSet.getTimestamp("registration_date"));
                
                winners.add(winner);
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching winners: " + e.getMessage());
        } finally {
            closeResources(connection, statement, resultSet);
        }
        
        return winners;
    }
    
    /**
     * Verify certificate authenticity
     * @param certificateId Certificate ID to verify
     * @return Certificate verification map or null if invalid
     */
    public Map<String, Object> verifyCertificate(String certificateId) {
        String sql = "SELECT cl.registration_id, cl.certificate_type, cl.generated_date, " +
                    "r.name, r.email, r.college, r.event " +
                    "FROM certificate_log cl " +
                    "JOIN registrations r ON cl.registration_id = r.id " +
                    "WHERE cl.certificate_id = ?";
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, certificateId);
            
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                Map<String, Object> verification = new HashMap<>();
                verification.put("valid", true);
                verification.put("registration_id", resultSet.getInt("registration_id"));
                verification.put("certificate_type", resultSet.getString("certificate_type"));
                verification.put("generated_date", resultSet.getTimestamp("generated_date"));
                verification.put("name", resultSet.getString("name"));
                verification.put("email", resultSet.getString("email"));
                verification.put("college", resultSet.getString("college"));
                verification.put("event", resultSet.getString("event"));
                
                return verification;
            }
            
        } catch (SQLException e) {
            System.err.println("Error verifying certificate: " + e.getMessage());
        } finally {
            closeResources(connection, statement, resultSet);
        }
        
        // Certificate not found or invalid
        Map<String, Object> invalidResult = new HashMap<>();
        invalidResult.put("valid", false);
        return invalidResult;
    }
    
    /**
     * Generate unique certificate ID
     * @param registrationId Registration ID
     * @param certificateType Certificate type
     * @return Unique certificate ID
     */
    private String generateCertificateId(int registrationId, String certificateType) {
        String prefix = certificateType.equals("winner") ? "WIN" : "PAR";
        long timestamp = System.currentTimeMillis();
        return String.format("%s-%d-%d", prefix, registrationId, timestamp);
    }
    
    /**
     * Get certificate statistics
     * @return Map containing certificate statistics
     */
    public Map<String, Object> getCertificateStatistics() {
        String sql = "SELECT " +
                    "COUNT(CASE WHEN certificate_type = 'participation' THEN 1 END) as participation_certs, " +
                    "COUNT(CASE WHEN certificate_type = 'winner' THEN 1 END) as winner_certs, " +
                    "COUNT(*) as total_certs " +
                    "FROM certificate_log";
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                Map<String, Object> stats = new HashMap<>();
                stats.put("participation_certificates", resultSet.getInt("participation_certs"));
                stats.put("winner_certificates", resultSet.getInt("winner_certs"));
                stats.put("total_certificates", resultSet.getInt("total_certs"));
                
                return stats;
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching certificate statistics: " + e.getMessage());
        } finally {
            closeResources(connection, statement, resultSet);
        }
        
        return new HashMap<>();
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
