package com.event.servlets;

import com.event.dao.AdminDAO;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Servlet for handling admin authentication
 * Manages admin login and session creation
 */
@WebServlet("/admin-login")
public class AdminLoginServlet extends HttpServlet {
    
    private AdminDAO adminDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        adminDAO = new AdminDAO();
    }
    
    /**
     * Handle POST requests for admin login
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
            // Extract login parameters
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            
            // Validate input parameters
            if (username == null || username.trim().isEmpty()) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Username is required");
                out.print(jsonResponse.toString());
                return;
            }
            
            if (password == null || password.trim().isEmpty()) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Password is required");
                out.print(jsonResponse.toString());
                return;
            }
            
            // Authenticate admin
            Map<String, Object> admin = adminDAO.authenticateAdmin(username, password);
            
            if (admin != null) {
                // Create admin session
                HttpSession session = request.getSession(true);
                session.setAttribute("admin_id", admin.get("id"));
                session.setAttribute("admin_username", admin.get("username"));
                session.setAttribute("admin_role", admin.get("role"));
                session.setMaxInactiveInterval(3600); // 1 hour session timeout
                
                // Log admin login
                int adminId = (Integer) admin.get("id");
                adminDAO.logAdminAction(adminId, "Admin login successful");
                
                // Success response
                jsonResponse.put("status", "success");
                jsonResponse.put("message", "Login successful! Redirecting to dashboard...");
                jsonResponse.put("redirect_url", "dashboard.jsp");
                jsonResponse.put("admin_username", admin.get("username"));
                
            } else {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Invalid username or password");
            }
            
        } catch (Exception e) {
            System.err.println("Error in AdminLoginServlet: " + e.getMessage());
            e.printStackTrace();
            
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "An unexpected error occurred. Please try again.");
        } finally {
            out.print(jsonResponse.toString());
            out.flush();
        }
    }
    
    /**
     * Handle GET requests for logout
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();
        
        try {
            if ("logout".equals(action)) {
                // Handle logout
                HttpSession session = request.getSession(false);
                
                if (session != null) {
                    // Log admin logout
                    Integer adminId = (Integer) session.getAttribute("admin_id");
                    if (adminId != null) {
                        adminDAO.logAdminAction(adminId, "Admin logout");
                    }
                    
                    // Invalidate session
                    session.invalidate();
                }
                
                jsonResponse.put("status", "success");
                jsonResponse.put("message", "Logged out successfully");
                jsonResponse.put("redirect_url", "admin-login.jsp");
                
            } else {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Invalid action");
            }
            
        } catch (Exception e) {
            System.err.println("Error in AdminLoginServlet GET: " + e.getMessage());
            e.printStackTrace();
            
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "An unexpected error occurred");
        } finally {
            out.print(jsonResponse.toString());
            out.flush();
        }
    }
    
    /**
     * Check if admin is authenticated
     * @param request HTTP request
     * @return true if authenticated, false otherwise
     */
    public static boolean isAdminAuthenticated(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        
        if (session == null) {
            return false;
        }
        
        Integer adminId = (Integer) session.getAttribute("admin_id");
        String adminRole = (String) session.getAttribute("admin_role");
        
        return adminId != null && "admin".equals(adminRole);
    }
    
    /**
     * Get admin ID from session
     * @param request HTTP request
     * @return Admin ID or null if not authenticated
     */
    public static Integer getAdminId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        
        if (session == null) {
            return null;
        }
        
        return (Integer) session.getAttribute("admin_id");
    }
}
