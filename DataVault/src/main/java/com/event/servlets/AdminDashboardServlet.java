package com.event.servlets;

import com.event.dao.RegistrationDAO;
import com.event.dao.EventDAO;
import com.event.dao.CertificateDAO;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * Servlet for admin dashboard operations
 * Handles participant management, data export, and dashboard data
 */
@WebServlet("/admin-dashboard")
public class AdminDashboardServlet extends HttpServlet {
    
    private RegistrationDAO registrationDAO;
    private EventDAO eventDAO;
    private CertificateDAO certificateDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        registrationDAO = new RegistrationDAO();
        eventDAO = new EventDAO();
        certificateDAO = new CertificateDAO();
    }
    
    /**
     * Handle GET requests for dashboard data and exports
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
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
        
        if ("get_participants".equals(action)) {
            getParticipants(request, response);
        } else if ("get_statistics".equals(action)) {
            getStatistics(request, response);
        } else if ("export_csv".equals(action)) {
            exportCSV(request, response);
        } else if ("export_html".equals(action)) {
            exportHTML(request, response);
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
     * Handle POST requests for participant management
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
        
        if ("delete_participant".equals(action)) {
            deleteParticipant(request, response);
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
     * Get participants with pagination
     */
    private void getParticipants(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();
        
        try {
            // Parse pagination parameters
            int page = 1;
            int limit = 10;
            
            String pageParam = request.getParameter("page");
            String limitParam = request.getParameter("limit");
            
            if (pageParam != null && !pageParam.trim().isEmpty()) {
                try {
                    page = Integer.parseInt(pageParam);
                    page = Math.max(1, page); // Ensure page is at least 1
                } catch (NumberFormatException e) {
                    page = 1;
                }
            }
            
            if (limitParam != null && !limitParam.trim().isEmpty()) {
                try {
                    limit = Integer.parseInt(limitParam);
                    limit = Math.max(1, Math.min(100, limit)); // Limit between 1 and 100
                } catch (NumberFormatException e) {
                    limit = 10;
                }
            }
            
            int offset = (page - 1) * limit;
            
            // Get participants and total count
            List<Map<String, Object>> participants = registrationDAO.getAllRegistrations(offset, limit);
            int totalCount = registrationDAO.getTotalRegistrationCount();
            int totalPages = (int) Math.ceil((double) totalCount / limit);
            
            // Convert to JSON array
            JSONArray participantsArray = new JSONArray();
            for (Map<String, Object> participant : participants) {
                participantsArray.put(new JSONObject(participant));
            }
            
            // Success response
            jsonResponse.put("status", "success");
            jsonResponse.put("participants", participantsArray);
            jsonResponse.put("pagination", new JSONObject()
                .put("current_page", page)
                .put("total_pages", totalPages)
                .put("total_count", totalCount)
                .put("limit", limit)
                .put("has_next", page < totalPages)
                .put("has_previous", page > 1)
            );
            
        } catch (Exception e) {
            System.err.println("Error fetching participants: " + e.getMessage());
            e.printStackTrace();
            
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Failed to fetch participants");
        } finally {
            out.print(jsonResponse.toString());
            out.flush();
        }
    }
    
    /**
     * Get dashboard statistics
     */
    private void getStatistics(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();
        
        try {
            // Get various statistics
            int totalRegistrations = registrationDAO.getTotalRegistrationCount();
            Map<String, Integer> eventCounts = eventDAO.getEventRegistrationCounts();
            Map<String, Object> certificateStats = certificateDAO.getCertificateStatistics();
            List<Map<String, Object>> winners = certificateDAO.getAllWinners();
            
            // Prepare statistics object
            JSONObject statistics = new JSONObject();
            statistics.put("total_registrations", totalRegistrations);
            statistics.put("total_winners", winners.size());
            statistics.put("event_registrations", new JSONObject(eventCounts));
            statistics.put("certificate_statistics", new JSONObject(certificateStats));
            
            // Success response
            jsonResponse.put("status", "success");
            jsonResponse.put("statistics", statistics);
            
        } catch (Exception e) {
            System.err.println("Error fetching statistics: " + e.getMessage());
            e.printStackTrace();
            
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Failed to fetch statistics");
        } finally {
            out.print(jsonResponse.toString());
            out.flush();
        }
    }
    
    /**
     * Delete a participant
     */
    private void deleteParticipant(HttpServletRequest request, HttpServletResponse response) 
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
            
            // Delete participant
            boolean deleted = registrationDAO.deleteRegistration(participantId);
            
            if (deleted) {
                // Log admin action
                Integer adminId = AdminLoginServlet.getAdminId(request);
                if (adminId != null) {
                    // Note: AdminDAO instance needed for logging
                    // adminDAO.logAdminAction(adminId, "Deleted participant ID: " + participantId);
                }
                
                jsonResponse.put("status", "success");
                jsonResponse.put("message", "Participant deleted successfully");
            } else {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Failed to delete participant");
            }
            
        } catch (Exception e) {
            System.err.println("Error deleting participant: " + e.getMessage());
            e.printStackTrace();
            
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "An unexpected error occurred");
        } finally {
            out.print(jsonResponse.toString());
            out.flush();
        }
    }
    
    /**
     * Export participants data as CSV
     */
    private void exportCSV(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"participants.csv\"");
        
        PrintWriter out = response.getWriter();
        
        try {
            // Get all participants (no pagination for export)
            List<Map<String, Object>> participants = registrationDAO.getAllRegistrations(0, Integer.MAX_VALUE);
            
            // CSV header
            out.println("ID,Name,Email,College,Event,Registration Date,Winner Status");
            
            // CSV data
            for (Map<String, Object> participant : participants) {
                out.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%s%n",
                    participant.get("id"),
                    escapeCSV((String) participant.get("name")),
                    escapeCSV((String) participant.get("email")),
                    escapeCSV((String) participant.get("college")),
                    escapeCSV((String) participant.get("event")),
                    participant.get("registration_date"),
                    (Boolean) participant.get("winner_status") ? "Winner" : "Participant"
                );
            }
            
        } catch (Exception e) {
            System.err.println("Error exporting CSV: " + e.getMessage());
            e.printStackTrace();
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Failed to export CSV");
            
            out.print(jsonResponse.toString());
        } finally {
            out.flush();
        }
    }
    
    /**
     * Export participants data as HTML
     */
    private void exportHTML(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"participants.html\"");
        
        PrintWriter out = response.getWriter();
        
        try {
            // Get all participants
            List<Map<String, Object>> participants = registrationDAO.getAllRegistrations(0, Integer.MAX_VALUE);
            
            // HTML structure
            out.println("<!DOCTYPE html>");
            out.println("<html lang=\"en\">");
            out.println("<head>");
            out.println("<meta charset=\"UTF-8\">");
            out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
            out.println("<title>TARUNYAM - Participants List</title>");
            out.println("<style>");
            out.println("body { font-family: Arial, sans-serif; margin: 20px; }");
            out.println("table { border-collapse: collapse; width: 100%; }");
            out.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
            out.println("th { background-color: #f2f2f2; }");
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>TARUNYAM - Tech Event 2025 - Participants List</h1>");
            out.println("<table>");
            out.println("<tr><th>ID</th><th>Name</th><th>Email</th><th>College</th><th>Event</th><th>Registration Date</th><th>Status</th></tr>");
            
            for (Map<String, Object> participant : participants) {
                out.printf("<tr><td>%d</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>%n",
                    participant.get("id"),
                    escapeHTML((String) participant.get("name")),
                    escapeHTML((String) participant.get("email")),
                    escapeHTML((String) participant.get("college")),
                    escapeHTML((String) participant.get("event")),
                    participant.get("registration_date"),
                    (Boolean) participant.get("winner_status") ? "Winner" : "Participant"
                );
            }
            
            out.println("</table>");
            out.println("</body>");
            out.println("</html>");
            
        } catch (Exception e) {
            System.err.println("Error exporting HTML: " + e.getMessage());
            e.printStackTrace();
        } finally {
            out.flush();
        }
    }
    
    /**
     * Escape CSV special characters
     */
    private String escapeCSV(String text) {
        if (text == null) return "";
        return text.replace("\"", "\"\"");
    }
    
    /**
     * Escape HTML special characters
     */
    private String escapeHTML(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#x27;");
    }
}
