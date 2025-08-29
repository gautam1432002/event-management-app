package com.event.servlets;

import com.event.dao.RegistrationDAO;
import com.event.dao.CertificateDAO;
import com.event.dao.AdminDAO;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Servlet for winner management operations
 * Handles selecting and revoking winners, and generating winner certificates
 */
@WebServlet("/winner")
public class WinnerServlet extends HttpServlet {
    
    private RegistrationDAO registrationDAO;
    private CertificateDAO certificateDAO;
    private AdminDAO adminDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        registrationDAO = new RegistrationDAO();
        certificateDAO = new CertificateDAO();
        adminDAO = new AdminDAO();
    }
    
    /**
     * Handle POST requests for winner management
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check admin authentication
        if (!AdminLoginServlet.isAdminAuthenticated(request)) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Unauthorized access. Please login first.");
            
            PrintWriter out = response.getWriter();
            out.print(jsonResponse.toString());
            return;
        }
        
        String action = request.getParameter("action");
        
        if ("select_winner".equals(action)) {
            selectWinner(request, response);
        } else if ("revoke_winner".equals(action)) {
            revokeWinner(request, response);
        } else if ("generate_winner_certificate".equals(action)) {
            generateWinnerCertificate(request, response);
        } else {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Invalid action specified");
            
            PrintWriter out = response.getWriter();
            out.print(jsonResponse.toString());
        }
    }
    
    /**
     * Select a participant as winner
     */
    private void selectWinner(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();
        
        try {
            String idParam = request.getParameter("id");
            
            if (idParam == null || idParam.trim().isEmpty()) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Participant ID is required");
                out.print(jsonResponse.toString());
                return;
            }
            
            int participantId;
            try {
                participantId = Integer.parseInt(idParam);
            } catch (NumberFormatException e) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Invalid participant ID format");
                out.print(jsonResponse.toString());
                return;
            }
            
            // Check if participant exists
            Map<String, Object> participant = registrationDAO.getRegistrationById(participantId);
            if (participant == null) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Participant not found");
                out.print(jsonResponse.toString());
                return;
            }
            
            // Check if already a winner
            if ((Boolean) participant.get("winner_status")) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Participant is already a winner");
                out.print(jsonResponse.toString());
                return;
            }
            
            // Update winner status
            boolean updated = registrationDAO.updateWinnerStatus(participantId, true);
            
            if (updated) {
                // Generate winner certificate
                Map<String, Object> winnerCertificateData = certificateDAO.generateCertificateData(
                    participantId, "winner");
                
                if (winnerCertificateData != null) {
                    String certificateId = (String) winnerCertificateData.get("certificate_id");
                    certificateDAO.trackCertificateGeneration(participantId, "winner", certificateId);
                }
                
                // Log admin action
                Integer adminId = AdminLoginServlet.getAdminId(request);
                if (adminId != null) {
                    String participantName = (String) participant.get("name");
                    String event = (String) participant.get("event");
                    adminDAO.logAdminAction(adminId, 
                        "Selected winner: " + participantName + " for event: " + event);
                }
                
                jsonResponse.put("status", "success");
                jsonResponse.put("message", "Winner selected successfully! Winner certificate is ready for download.");
                
                if (winnerCertificateData != null) {
                    jsonResponse.put("certificate_data", new JSONObject(winnerCertificateData));
                }
                
            } else {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Failed to select winner");
            }
            
        } catch (Exception e) {
            System.err.println("Error selecting winner: " + e.getMessage());
            e.printStackTrace();
            
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "An unexpected error occurred");
        } finally {
            out.print(jsonResponse.toString());
            out.flush();
        }
    }
    
    /**
     * Revoke winner status from a participant
     */
    private void revokeWinner(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();
        
        try {
            String idParam = request.getParameter("id");
            
            if (idParam == null || idParam.trim().isEmpty()) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Participant ID is required");
                out.print(jsonResponse.toString());
                return;
            }
            
            int participantId;
            try {
                participantId = Integer.parseInt(idParam);
            } catch (NumberFormatException e) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Invalid participant ID format");
                out.print(jsonResponse.toString());
                return;
            }
            
            // Check if participant exists
            Map<String, Object> participant = registrationDAO.getRegistrationById(participantId);
            if (participant == null) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Participant not found");
                out.print(jsonResponse.toString());
                return;
            }
            
            // Check if is a winner
            if (!(Boolean) participant.get("winner_status")) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Participant is not a winner");
                out.print(jsonResponse.toString());
                return;
            }
            
            // Update winner status to false
            boolean updated = registrationDAO.updateWinnerStatus(participantId, false);
            
            if (updated) {
                // Log admin action
                Integer adminId = AdminLoginServlet.getAdminId(request);
                if (adminId != null) {
                    String participantName = (String) participant.get("name");
                    String event = (String) participant.get("event");
                    adminDAO.logAdminAction(adminId, 
                        "Revoked winner status: " + participantName + " for event: " + event);
                }
                
                jsonResponse.put("status", "success");
                jsonResponse.put("message", "Winner status revoked successfully");
                
            } else {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Failed to revoke winner status");
            }
            
        } catch (Exception e) {
            System.err.println("Error revoking winner: " + e.getMessage());
            e.printStackTrace();
            
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "An unexpected error occurred");
        } finally {
            out.print(jsonResponse.toString());
            out.flush();
        }
    }
    
    /**
     * Generate winner certificate for an existing winner
     */
    private void generateWinnerCertificate(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();
        
        try {
            String idParam = request.getParameter("id");
            
            if (idParam == null || idParam.trim().isEmpty()) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Participant ID is required");
                out.print(jsonResponse.toString());
                return;
            }
            
            int participantId;
            try {
                participantId = Integer.parseInt(idParam);
            } catch (NumberFormatException e) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Invalid participant ID format");
                out.print(jsonResponse.toString());
                return;
            }
            
            // Check if participant exists and is a winner
            Map<String, Object> participant = registrationDAO.getRegistrationById(participantId);
            if (participant == null) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Participant not found");
                out.print(jsonResponse.toString());
                return;
            }
            
            if (!(Boolean) participant.get("winner_status")) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Participant is not a winner");
                out.print(jsonResponse.toString());
                return;
            }
            
            // Generate winner certificate
            Map<String, Object> winnerCertificateData = certificateDAO.generateCertificateData(
                participantId, "winner");
            
            if (winnerCertificateData != null) {
                String certificateId = (String) winnerCertificateData.get("certificate_id");
                certificateDAO.trackCertificateGeneration(participantId, "winner", certificateId);
                
                // Log admin action
                Integer adminId = AdminLoginServlet.getAdminId(request);
                if (adminId != null) {
                    String participantName = (String) participant.get("name");
                    adminDAO.logAdminAction(adminId, 
                        "Generated winner certificate for: " + participantName);
                }
                
                jsonResponse.put("status", "success");
                jsonResponse.put("message", "Winner certificate generated successfully");
                jsonResponse.put("certificate_data", new JSONObject(winnerCertificateData));
                
            } else {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Failed to generate winner certificate");
            }
            
        } catch (Exception e) {
            System.err.println("Error generating winner certificate: " + e.getMessage());
            e.printStackTrace();
            
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "An unexpected error occurred");
        } finally {
            out.print(jsonResponse.toString());
            out.flush();
        }
    }
    
    /**
     * Handle GET requests - not allowed for this servlet
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("status", "error");
        jsonResponse.put("message", "GET method not supported for winner operations");
        
        PrintWriter out = response.getWriter();
        out.print(jsonResponse.toString());
        out.flush();
    }
}
