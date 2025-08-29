package com.event.servlets;

import com.event.dao.RegistrationDAO;
import com.event.dao.CertificateDAO;
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
 * Servlet for handling participant registration
 * Processes registration forms and generates participation certificates
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    
    private RegistrationDAO registrationDAO;
    private CertificateDAO certificateDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        registrationDAO = new RegistrationDAO();
        certificateDAO = new CertificateDAO();
    }
    
    /**
     * Handle POST requests for participant registration
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Set response content type to JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();
        
        try {
            // Extract form parameters
            String name = request.getParameter("name");
            String email = request.getParameter("email");
            String college = request.getParameter("college");
            String event = request.getParameter("event");
            
            // Validate input parameters
            if (name == null || name.trim().isEmpty()) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Full name is required");
                out.print(jsonResponse.toString());
                return;
            }
            
            if (email == null || email.trim().isEmpty() || !isValidEmail(email)) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Valid email address is required");
                out.print(jsonResponse.toString());
                return;
            }
            
            if (college == null || college.trim().isEmpty()) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "College name is required");
                out.print(jsonResponse.toString());
                return;
            }
            
            if (event == null || event.trim().isEmpty()) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Please select an event");
                out.print(jsonResponse.toString());
                return;
            }
            
            // Check if email is already registered for this event
            if (registrationDAO.isEmailRegistered(email, event)) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Email is already registered for this event");
                out.print(jsonResponse.toString());
                return;
            }
            
            // Register participant
            int registrationId = registrationDAO.registerParticipant(name, email, college, event);
            
            if (registrationId > 0) {
                // Generate participation certificate data
                Map<String, Object> certificateData = certificateDAO.generateCertificateData(
                    registrationId, "participation");
                
                if (certificateData != null) {
                    // Track certificate generation
                    String certificateId = (String) certificateData.get("certificate_id");
                    certificateDAO.trackCertificateGeneration(registrationId, "participation", certificateId);
                    
                    // Success response with certificate data
                    jsonResponse.put("status", "success");
                    jsonResponse.put("message", "Registration successful! Your participation certificate is ready for download.");
                    jsonResponse.put("registration_id", registrationId);
                    jsonResponse.put("certificate_data", new JSONObject(certificateData));
                } else {
                    // Registration successful but certificate generation failed
                    jsonResponse.put("status", "success");
                    jsonResponse.put("message", "Registration successful! Certificate will be available shortly.");
                    jsonResponse.put("registration_id", registrationId);
                }
            } else {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Registration failed. Please try again.");
            }
            
        } catch (Exception e) {
            System.err.println("Error in RegisterServlet: " + e.getMessage());
            e.printStackTrace();
            
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "An unexpected error occurred. Please try again.");
        } finally {
            out.print(jsonResponse.toString());
            out.flush();
        }
    }
    
    /**
     * Handle GET requests - redirect to main page
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("status", "error");
        jsonResponse.put("message", "GET method not supported for registration");
        
        PrintWriter out = response.getWriter();
        out.print(jsonResponse.toString());
        out.flush();
    }
    
    /**
     * Validate email format using simple regex
     * @param email Email to validate
     * @return true if valid format, false otherwise
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.trim().matches(emailRegex);
    }
}
